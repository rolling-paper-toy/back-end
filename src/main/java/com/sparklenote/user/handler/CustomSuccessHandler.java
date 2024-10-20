package com.sparklenote.user.handler;


import com.sparklenote.domain.enumType.Role;
import com.sparklenote.user.jwt.JWTUtil;
import com.sparklenote.user.oAuth2.CustomOAuth2User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@RequiredArgsConstructor
@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;

    @Value("${jwt.accessExpiration}") // 30분
    private Long accessTokenExpiration;

    @Value("${jwt.refreshExpiration}") // 1일
    private Long refreshTokenExpiration;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        //OAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        String username = customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String accessToken = jwtUtil.createAccessToken(username, Role.TEACHER, accessTokenExpiration);
        String refreshToken = jwtUtil.createRefreshToken(username, refreshTokenExpiration);

        response.addCookie(createCookie("Authorization", accessToken));
        response.addCookie(createCookie("RefreshToken", refreshToken));

        response.sendRedirect("http://localhost:3000/");
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(Math.toIntExact(accessTokenExpiration / 1000)); // 초 단위로 설정
        //cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }
}