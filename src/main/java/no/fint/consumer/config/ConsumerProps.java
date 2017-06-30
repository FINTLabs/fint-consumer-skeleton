package no.fint.consumer.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ConsumerProps {

    @Value("${fint.events.orgIds:mock.no}")
    private String[] orgs;

}

