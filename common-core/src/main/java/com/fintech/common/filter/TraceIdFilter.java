package com.fintech.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class TraceIdFilter extends OncePerRequestFilter {

    private static final String TRACE_ID = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response
            , FilterChain filterChain) throws ServletException, IOException {

        //Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        //System.out.println("AUTH IN TRACE FILTER: " + auth);

        String traceId = request.getHeader("X-Trace-Id");
        if(traceId == null || traceId.isEmpty()){
            traceId = UUID.randomUUID().toString();
        }
        MDC.put(TRACE_ID, traceId);

        try {
            response.setHeader("X-Trace-Id", traceId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
