package com.fintech.common.messaging;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.jspecify.annotations.Nullable;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class AuthContextRecordInterceptor implements RecordInterceptor<String, Object> {

    private final JsonMapper jsonMapper;

    public AuthContextRecordInterceptor(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public @Nullable ConsumerRecord<String, Object> intercept(ConsumerRecord<String, Object> record, Consumer<String, Object> consumer) {

        if(record != null && record.headers() != null)  {
            Header header = record.headers().lastHeader("auth-context");

            if (header != null) {
                try {
                    String json = new String(header.value(), StandardCharsets.UTF_8);
                    AuthContext ctx = jsonMapper.readValue(json, AuthContext.class);

                    Authentication auth = buildAuthentication(ctx);

                    SecurityContextHolder.getContext().setAuthentication(auth);

                } catch (Exception e) {
                    throw new RuntimeException("Invalid auth context", e);
                }
            }
        }
        return record;

    }

    @Override
    public void afterRecord(ConsumerRecord<String, Object> record, Consumer<String, Object> consumer) {
        SecurityContextHolder.clearContext();
    }

    private Authentication buildAuthentication(AuthContext ctx) {

        List<GrantedAuthority> authorities = new ArrayList<>();

        if (ctx.getRoles() != null) {
            ctx.getRoles().forEach(r ->
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + r))
            );
        }

        if (ctx.getScopes() != null) {
            ctx.getScopes().forEach(s ->
                    authorities.add(new SimpleGrantedAuthority("SCOPE_" + s))
            );
        }

        return new UsernamePasswordAuthenticationToken(
                ctx.getSubject(),
                null,
                authorities
        );
    }
}
