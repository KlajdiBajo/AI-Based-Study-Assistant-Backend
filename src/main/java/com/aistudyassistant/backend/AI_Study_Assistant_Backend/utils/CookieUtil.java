package com.aistudyassistant.backend.AI_Study_Assistant_Backend.utils;

import com.aistudyassistant.backend.AI_Study_Assistant_Backend.constants.ApplicationConstants;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    public Cookie createAccessTokenCookie(String token) {
        Cookie cookie = new Cookie("accessToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); //set true for real-world production, set false for localhost
        cookie.setPath("/");
        cookie.setMaxAge((int) ApplicationConstants.ACCESS_TOKEN_VALIDITY_SECONDS);
        return cookie;
    }

    public Cookie createRefreshTokenCookie(String token) {
        Cookie cookie = new Cookie("refreshToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); //set true for real-world production, set false for localhost
        cookie.setPath("/");
        cookie.setMaxAge((int) ApplicationConstants.REFRESH_TOKEN_VALIDITY_SECONDS);
        return cookie;
    }

    public Cookie deleteAccessTokenCookie() {
        Cookie cookie = new Cookie("accessToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); //set true for real-world production, set false for localhost
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }

    public Cookie deleteRefreshTokenCookie() {
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); //set true for real-world production, set false for localhost
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }

    public void addTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        response.addCookie(createAccessTokenCookie(accessToken));
        response.addCookie(createRefreshTokenCookie(refreshToken));
    }

    public void clearAuthCookies(HttpServletResponse response) {
        response.addCookie(deleteAccessTokenCookie());
        response.addCookie(deleteRefreshTokenCookie());
    }

}
