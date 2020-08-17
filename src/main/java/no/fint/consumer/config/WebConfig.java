package no.fint.consumer.config;

import no.fint.consumer.utils.FintAccessPolicyInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    @Qualifier("linkMapper")
    @Autowired
    private Map<String, String> links;

    @Value("${server.context-path:}")
    private String contextPath;

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        AntPathMatcher matcher = new AntPathMatcher();
        matcher.setCaseSensitive(false);
        configurer.setPathMatcher(matcher);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new FintAccessPolicyInterceptor(links.values().stream().filter(s -> s.startsWith(contextPath)).collect(Collectors.toList())))
                .addPathPatterns(links.values().stream().filter(s -> s.startsWith(contextPath)).map(s -> StringUtils.removeStart(s, contextPath) + "/**").toArray(String[]::new));
    }
}
