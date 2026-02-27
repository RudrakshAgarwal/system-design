package com.airlinemanagementsystem.booking.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        // Get the current token from the Security Context (saved by the Controller)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            // Add the "Authorization: Bearer <token>" header to the outgoing Feign request
            template.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken.getToken().getTokenValue());
        }
    }
}
