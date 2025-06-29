package com.aistudyassistant.backend.AI_Study_Assistant_Backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtHelper jwtHelper;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        boolean shouldSkip = path.startsWith("/api/v1/auth/") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.equals("/");

        return shouldSkip;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");
        String token;
        String username = null;

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        token = authorizationHeader.substring(7);
        try {
            username = jwtHelper.extractUsername(token);
        }
        catch(IllegalStateException e) {
            sendErrorResponse(response, request.getRequestURI(), "Invalid JWT token format", "INVALID_TOKEN");
            return;
        }
        catch(ExpiredJwtException e) {
            // Send specific error code for expired tokens so frontend can trigger refresh
            sendErrorResponse(response, request.getRequestURI(), "JWT token has expired", "TOKEN_EXPIRED");
            return;
        }
        catch(MalformedJwtException e) {
            sendErrorResponse(response, request.getRequestURI(), "JWT token is malformed", "MALFORMED_TOKEN");
            return;
        }
        catch(Exception e) {
            sendErrorResponse(response, request.getRequestURI(), "Invalid JWT token", "INVALID_TOKEN");
            return;
        }

        if(username != null && SecurityContextHolder.getContext().getAuthentication() == null)
        {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if(jwtHelper.isTokenValid(token, userDetails)) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
            else {
                log.error("Token is invalid");
                sendErrorResponse(response, request.getRequestURI(), "JWT token is invalid or expired", "INVALID_TOKEN");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, String path, String message, String errorCode) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", ZonedDateTime.now().toString());
        errorBody.put("status", 401);
        errorBody.put("error", "Unauthorized");
        errorBody.put("path", path);
        errorBody.put("message", message);
        errorBody.put("errorCode", errorCode); // Add error code for frontend handling

        String jsonResponse = objectMapper.writeValueAsString(errorBody);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}