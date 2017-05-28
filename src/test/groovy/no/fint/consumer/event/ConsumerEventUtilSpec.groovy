package no.fint.consumer.event

import no.fint.audit.FintAuditService
import no.fint.event.model.Event
import no.fint.events.FintEvents
import no.fint.events.FintEventsHealth
import spock.lang.Specification

class ConsumerEventUtilSpec extends Specification {
    private ConsumerEventUtil consumerEventUtil
    private FintEvents fintEvents
    private FintEventsHealth fintEventsHealth

    void setup() {
        fintEvents = Mock(FintEvents)
        fintEventsHealth = Mock(FintEventsHealth)
        consumerEventUtil = new ConsumerEventUtil(fintEventsHealth: fintEventsHealth, fintEvents: fintEvents, fintAuditService: Mock(FintAuditService))
    }

    def "Send and receive health check"() {
        given:
        def event = new Event(orgId: 'rogfk.no')

        when:
        def response = consumerEventUtil.healthCheck(event)

        then:
        1 * fintEventsHealth.sendHealthCheck(event.getOrgId(), event.getCorrId(), event) >> event
        response.isPresent()
    }

    def "Send downstream event"() {
        given:
        def event = new Event(orgId: 'rogfk.no')

        when:
        consumerEventUtil.send(event)

        then:
        1 * fintEvents.sendDownstream('rogfk.no', _ as Event)
    }
}
