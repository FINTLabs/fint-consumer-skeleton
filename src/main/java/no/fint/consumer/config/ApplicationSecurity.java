package no.fint.consumer.config;

import no.fint.consumer.utils.RestEndpoints;
import no.fint.security.access.policy.FintAccessDecisionVoter;
import no.fint.security.access.policy.FintAccessUserDetailsService;
import no.fint.security.access.policy.FintRequestHeaderPreauthProcessingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;

import java.util.Collections;

@Configuration
public class ApplicationSecurity extends WebSecurityConfigurerAdapter {

    @Bean
    FintAccessDecisionVoter fintAccessDecisionVoter() {
        return new FintAccessDecisionVoter();
    }

    @Bean
    FintRequestHeaderPreauthProcessingFilter fintRequestHeaderPreauthProcessingFilter() throws Exception {
        final FintRequestHeaderPreauthProcessingFilter fintRequestHeaderPreauthProcessingFilter = new FintRequestHeaderPreauthProcessingFilter();
        fintRequestHeaderPreauthProcessingFilter.setAuthenticationManager(authenticationManager());
        return fintRequestHeaderPreauthProcessingFilter;
    }

    @Bean
    PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider() {
        PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider = new PreAuthenticatedAuthenticationProvider();
        preAuthenticatedAuthenticationProvider.setPreAuthenticatedUserDetailsService(fintAccessUserDetailsService());
        return preAuthenticatedAuthenticationProvider;
    }

    @Bean
    FintAccessUserDetailsService fintAccessUserDetailsService() {
        return new FintAccessUserDetailsService();
    }

    @Bean
    AccessDecisionManager accessDecisionManager() {
        return new UnanimousBased(Collections.singletonList(fintAccessDecisionVoter()));
    }

    @Override
    public void configure(WebSecurity web) {
        web.debug(true)
                .ignoring()
                .antMatchers(RestEndpoints.ADMIN + "/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().disable()
                .addFilter(fintRequestHeaderPreauthProcessingFilter())
                .authenticationProvider(preAuthenticatedAuthenticationProvider())
                .authorizeRequests()
                .anyRequest()
                .authenticated()
                .accessDecisionManager(accessDecisionManager());
    }
}
