package udea.fabrica.bookify.infrastructure.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityConfig.class);

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ROLE_PREFIX = "ROLE_";
    private static final String ROLE_CLAIM = "role";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService)
            throws Exception {

        OncePerRequestFilter jwtFilter = new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain)
                    throws ServletException, IOException {

                String header = request.getHeader("Authorization");

                if (header == null) {
                    LOG.warn("JWT: la petición {} no trae cabecera Authorization", request.getRequestURI());
                } else if (!header.startsWith(BEARER_PREFIX)) {
                    LOG.warn("JWT: la cabecera Authorization no empieza con 'Bearer '");
                } else if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    String token = header.substring(BEARER_PREFIX.length()).trim();
                    try {
                        Claims claims = jwtService.extractClaims(token);
                        String subject = claims.getSubject();
                        String rawRole = claims.get(ROLE_CLAIM, String.class);

                        if (subject == null || rawRole == null || rawRole.isBlank()) {
                            LOG.warn("JWT: token válido pero sin subject o sin claim '{}' (subject={}, role={})",
                                    ROLE_CLAIM, subject, rawRole);
                        } else {
                            String role = rawRole.trim().toUpperCase();
                            if (role.startsWith(ROLE_PREFIX)) {
                                role = role.substring(ROLE_PREFIX.length());
                            }

                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            subject,
                                            token,
                                            List.of(new SimpleGrantedAuthority(role)));
                            authentication.setDetails(
                                    new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            LOG.info("JWT: usuario {} autenticado con rol {}", subject, role);
                        }
                    } catch (JwtException | IllegalArgumentException ex) {
                        LOG.warn("JWT: token rechazado -> {}: {}",
                                ex.getClass().getSimpleName(), ex.getMessage());
                        SecurityContextHolder.clearContext();
                    }
                }

                filterChain.doFilter(request, response);
            }
        };

        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/v1/auth/**").permitAll()
                    .requestMatchers("/error").permitAll()
                    .anyRequest().authenticated())
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((request, response, authException) ->
                            response.setStatus(HttpStatus.UNAUTHORIZED.value()))
                    .accessDeniedHandler((request, response, accessDeniedException) ->
                            response.setStatus(HttpStatus.FORBIDDEN.value())))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}