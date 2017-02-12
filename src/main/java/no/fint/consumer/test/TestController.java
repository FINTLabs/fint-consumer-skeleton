package no.fint.consumer.test;

import com.google.common.collect.ImmutableMap;
import no.fint.consumer.admin.AdminService;
import no.fint.consumer.admin.Health;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "/test", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class TestController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private TestService testService;

    @RequestMapping(value = "/health", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Health healthCheck(@RequestHeader("x-org-id") String orgId, @RequestHeader("x-client") String client) {
        return adminService.healthCheck(orgId, client);
    }

    @RequestMapping(value = "/last-updated", method = RequestMethod.GET)
    private Map<String, String> getLastUpdated(@RequestHeader("x-org-id") String orgId) {
        String lastUpdated = testService.getLastUpdated(orgId);
        return ImmutableMap.of("lastUpdated", lastUpdated);
    }

}
