package com.handegunaydin.habit_tracker.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RequestMetadataFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        requestAttributes.setAttribute("ip", request.getAttribute("ip"), RequestAttributes.SCOPE_REQUEST);
        requestAttributes.setAttribute("user-agent", request.getHeader("user-agent"), RequestAttributes.SCOPE_REQUEST);
        try {
            filterChain.doFilter(request, response);
        } finally {
            requestAttributes.removeAttribute("ip", RequestAttributes.SCOPE_REQUEST);
            requestAttributes.removeAttribute("user-agent", RequestAttributes.SCOPE_REQUEST);
        }

    }
}
