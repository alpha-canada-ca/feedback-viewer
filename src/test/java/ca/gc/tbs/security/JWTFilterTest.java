package ca.gc.tbs.security;

import ca.gc.tbs.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JWTFilterTest {

    private JWTFilter jwtFilter;
    private JWTUtil jwtUtil;
    private UserService userService;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtFilter = new JWTFilter();
        jwtUtil = mock(JWTUtil.class);
        userService = mock(UserService.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);

        ReflectionTestUtils.setField(jwtFilter, "jwtUtil", jwtUtil);
        ReflectionTestUtils.setField(jwtFilter, "userService", userService);

        SecurityContextHolder.clearContext();
    }

    @Test
    void testValidTokenWithAdminAuthority() throws ServletException, IOException {
        String token = "good-token";
        String userName = "user1";
        List<String> authorities = Arrays.asList("ADMIN");

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(userName);

        UserDetails userDetails = new User(userName, "password",
                Arrays.asList(new SimpleGrantedAuthority("ADMIN")));
        when(userService.loadUserByUsername(userName)).thenReturn(userDetails);
        when(jwtUtil.validateToken(token, userDetails)).thenReturn(true);
        when(jwtUtil.extractClaim(eq(token), any())).thenReturn(authorities);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(SecurityContextHolder.getContext().getAuthentication() instanceof UsernamePasswordAuthenticationToken);
        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    void testValidTokenWithApiAuthority() throws ServletException, IOException {
        String token = "good-token";
        String userName = "apiman";
        List<String> authorities = Arrays.asList("API");

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(userName);

        UserDetails userDetails = new User(userName, "password",
                Arrays.asList(new SimpleGrantedAuthority("API")));
        when(userService.loadUserByUsername(userName)).thenReturn(userDetails);
        when(jwtUtil.validateToken(token, userDetails)).thenReturn(true);
        when(jwtUtil.extractClaim(eq(token), any())).thenReturn(authorities);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(SecurityContextHolder.getContext().getAuthentication() instanceof UsernamePasswordAuthenticationToken);
        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    void testInvalidTokenExtractUsernameThrows() throws ServletException, IOException {
        String token = "bad-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenThrow(new RuntimeException("Bad JWT!"));

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        pw.flush();
        assertTrue(sw.toString().contains("Bad JWT!"));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testValidTokenWithInsufficientAuthority() throws ServletException, IOException {
        String token = "good-token";
        String userName = "user2";
        List<String> authorities = Arrays.asList("USER");

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(userName);

        UserDetails userDetails = new User(userName, "password",
                Arrays.asList(new SimpleGrantedAuthority("USER")));
        when(userService.loadUserByUsername(userName)).thenReturn(userDetails);
        when(jwtUtil.validateToken(token, userDetails)).thenReturn(true);
        when(jwtUtil.extractClaim(eq(token), any())).thenReturn(authorities);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        pw.flush();
        assertTrue(sw.toString().contains("Access denied"));
        verify(filterChain, never()).doFilter(request, response);
    }
}