package no.fint.consumer.event;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;
import no.fint.audit.FintAuditService;
import no.fint.cache.CacheService;
import no.fint.consumer.config.Constants;
import no.fint.consumer.config.ConsumerProps;
import no.fint.consumer.status.StatusCache;
import no.fint.event.model.*;
import no.fint.events.FintEventListener;
import no.fint.events.FintEvents;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class EventListener implements FintEventListener {

    @Autowired(required = false)
    private List<CacheService> cacheServices;

    @Autowired
    private FintEvents fintEvents;

    @Autowired
    private FintAuditService fintAuditService;

    @Autowired
    StatusCache statusCache;

    @Autowired
    private ConsumerProps props;

    @Autowired
    private SynchronousEvents synchronousEvents;

    @Autowired
    private MeterRegistry meterRegistry;

    private static final String CUSTOM_METRIC_EVENTS = "fint.core.events";

    @Value("${fint.consumer.custom.metric.events:false}")
    private boolean customMetricEvents;

    @PostConstruct
    public void init() {
        fintEvents.registerUpstreamSystemListener(this);
        if (cacheServices == null)
            cacheServices = Collections.emptyList();
        for (String orgId : props.getAssets()) {
            fintEvents.registerUpstreamListener(orgId, this);
        }
        log.info("Upstream listeners registered.");
    }

    @Scheduled(initialDelayString = "${fint.consumer.register-delay:70000}", fixedDelay = Long.MAX_VALUE)
    public void registerOrgIds() {
        log.info("Bootstrapping orgId registration ...");
        Event event = new Event("", Constants.COMPONENT, DefaultActions.REGISTER_ORG_ID, Constants.COMPONENT_CONSUMER);
        fintEvents.sendDownstream(event);
    }

    @Override
    public void accept(Event event) {
        log.debug("Received event: {}", event);
        log.trace("Event data: {}", event.getData());
        if (event.isRegisterOrgId()) {
            if (props.getAssets().add(event.getOrgId())) {
                log.info("Registering orgId {} for {}", event.getOrgId(), event.getClient());
                fintEvents.registerUpstreamListener(event.getOrgId(), this);
                cacheServices.forEach(c -> {
                    c.createCache(event.getOrgId());
                    c.populateCache(event.getOrgId());
                });
            }
            return;
        }

        if (customMetricEvents) {
            updateCustomMetric(event);
        }

        if (statusCache.containsKey(event.getCorrId())) {
            statusCache.put(event.getCorrId(), event);
        }
        if (synchronousEvents.dispatch(event)) {
            return;
        }
        if (event.getOperation() == Operation.VALIDATE) {
            log.debug("Ignoring validation event.");
            return;
        }
        if (event.isHealthCheck()) {
            log.debug("Ignoring health check.");
            return;
        }
        if (event.getResponseStatus() == ResponseStatus.REJECTED || event.getResponseStatus() == ResponseStatus.ERROR) {
            log.debug("Ignoring response status {}", event.getResponseStatus());
            return;
        }
        try {
            cacheServices
                    .stream()
                    .filter(cacheService -> cacheService.supportsAction(event.getAction()))
                    .forEach(cacheService -> cacheService.onAction(event));
            fintAuditService.audit(event, Status.CACHE);
        } catch (Exception e) {
            log.debug("Error handling event {} {}", event.getOrgId(), event.getCorrId(), e);
            event.setMessage(ExceptionUtils.getStackTrace(e));
            fintAuditService.audit(event, Status.ERROR);
        }
    }

    private void updateCustomMetric(Event event) {
        meterRegistry.counter(CUSTOM_METRIC_EVENTS, getTags(event)).increment();
    }

    private List<Tag> getTags(Event event) {
        return Arrays.asList(
                Tag.of("orgId", event.getOrgId()),
                Tag.of("eventType", event.getAction()),
                Tag.of("eventOperation", Optional.ofNullable(event.getOperation()).map(Operation::name).orElse("READ")),
                Tag.of("eventResponseStatus", event.getResponseStatus().name()));
    }
}