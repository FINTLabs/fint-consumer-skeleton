package no.fint.consumer;

import com.github.springfox.loader.EnableSpringfox;
import no.fint.audit.EnableFintAudit;
import no.fint.dependencies.annotations.EnableFintDependencies;
import no.fint.events.EnableFintEvents;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableFintDependencies
@EnableFintEvents
@EnableFintAudit
@EnableScheduling
@EnableSpringfox
@SpringBootApplication
public class Application {


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
