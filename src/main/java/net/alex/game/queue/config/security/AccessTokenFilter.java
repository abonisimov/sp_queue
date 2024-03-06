package net.alex.game.queue.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Optional;

@Slf4j
public class AccessTokenFilter extends GenericFilterBean {

    private final AccessTokenService accessTokenService;

    public AccessTokenFilter(AccessTokenService accessTokenService) {
        this.accessTokenService = accessTokenService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        Optional<Authentication> authentication = accessTokenService.getAuthentication(httpServletRequest);
        authentication.ifPresent(value -> SecurityContextHolder.getContext().setAuthentication(value));
        filterChain.doFilter(request, response);
    }
}
