package no.fint.consumer.config;

import no.fint.consumer.utils.RestEndpoints;
import no.fint.security.access.policy.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;

import java.util.Arrays;

@Configuration
public class ApplicationSecurity extends WebSecurityConfigurerAdapter {

    @Value("${fint.idp.well-known-oauth-configuration}")
    String wellKnownLocation;

    @Value("${fint.security.debug:false}")
    boolean debug;

    @Value("${fint.security.scope}")
    String scope;

    @Value("${fint.security.role}")
    String role;

    @Bean
    FintAccessPolicyVoter fintAccessPolicyVoter() {
        return new FintAccessPolicyVoter();
    }

    @Bean
    FintAccessScopeVoter fintAccessScopeVoter() {
        return new FintAccessScopeVoter(scope);
    }

    @Bean
    FintAccessRoleVoter fintAccessRoleVoter() {
        return new FintAccessRoleVoter(role);
    }

    @Bean
    FintBearerTokenJwtPreAuthenticatedProcessingFilter fintBearerTokenJwtPreAuthenticatedProcessingFilter() throws Exception {
        final FintBearerTokenJwtPreAuthenticatedProcessingFilter fintBearerTokenJwtPreAuthenticatedProcessingFilter
                = new FintBearerTokenJwtPreAuthenticatedProcessingFilter(wellKnownLocation);
        fintBearerTokenJwtPreAuthenticatedProcessingFilter.setAuthenticationManager(authenticationManager());
        return fintBearerTokenJwtPreAuthenticatedProcessingFilter;
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
        return new UnanimousBased(Arrays.asList(fintAccessPolicyVoter(), fintAccessScopeVoter(), fintAccessRoleVoter()));
    }

    @Override
    public void configure(WebSecurity web) {
        web.debug(debug)
                .ignoring()
                .antMatchers(RestEndpoints.ADMIN + "/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .addFilter(fintBearerTokenJwtPreAuthenticatedProcessingFilter())
                .authenticationProvider(preAuthenticatedAuthenticationProvider())
                .authorizeRequests()
                .anyRequest()
                .authenticated()
                .accessDecisionManager(accessDecisionManager());
    }
}
