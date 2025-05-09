package org.zerox80.coingeckowebapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestAttributeHandler;
import reactor.core.publisher.Mono;
import org.zerox80.coingeckowebapp.repository.UserRepository;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;

@Configuration
@EnableWebFluxSecurity // Wichtig: WebFlux Security aktivieren
public class SecurityConfig {

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService() {
        // Wickelt den blockierenden findByUsername Aufruf in ein Mono ein, um ihn in einen reaktiven Kontext zu bringen.
        // Beachte: Für eine vollständig reaktive Anwendung sollte das Repository selbst reaktive Publisher zurückgeben.
        return username -> Mono.fromCallable(() -> userRepository.findByUsername(username))
                // Wenn das Mono leer ist (Benutzer nicht gefunden), wirf eine UsernameNotFoundException reaktiv.
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found")))
                // Das User-Entity-Objekt in Spring Security's UserDetails umwandeln.
                .map(user -> {
                    if (user == null) {
                        // Dieser Fall sollte durch switchIfEmpty abgedeckt sein, aber zur Sicherheit.
                        throw new UsernameNotFoundException("User not found");
                    }
                    return org.springframework.security.core.userdetails.User
                            .withUsername(user.getUsername())
                            .password(user.getPassword())
                            .roles(user.getRole().replace("ROLE_", ""))
                            .build();
                });
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager(ReactiveUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        UserDetailsRepositoryReactiveAuthenticationManager authenticationManager = new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        authenticationManager.setPasswordEncoder(passwordEncoder);
        return authenticationManager;
    }

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http, ReactiveAuthenticationManager authenticationManager) {
        // ServerCsrfTokenRequestAttributeHandler requestHandler = new ServerCsrfTokenRequestAttributeHandler(); // Nicht mehr benötigt bei deaktiviertem CSRF
        // ServerWebExchangeMatcher registerPostMatcher = 
        //     new PathPatternParserServerWebExchangeMatcher("/register", HttpMethod.POST); // Nicht mehr benötigt

        return http
                .authenticationManager(authenticationManager) // Setze den reaktiven AuthenticationManager
                .authorizeExchange(exchanges -> exchanges // Verwende authorizeExchange für WebFlux
                        .pathMatchers("/register", "/login", "/css/**", "/js/**", "/images/**").permitAll()
                        .pathMatchers("/portfolio/**").authenticated() // Portfolio-Pfade explizit schützen
                        .anyExchange().authenticated() // Alle anderen Anfragen erfordern Authentifizierung
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/login") // Eigene Login-Seite
                        // Weiterleitung nach erfolgreicher Anmeldung zur Startseite
                        .authenticationSuccessHandler(new RedirectServerAuthenticationSuccessHandler("/"))
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") // Logout-URL
                        // **Korrigiert:** Verwende RedirectServerLogoutSuccessHandler für die Weiterleitung nach dem Logout
                        .logoutSuccessHandler(new RedirectServerLogoutSuccessHandler()) // Standardmäßig wird zu /login?logout weitergeleitet
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // CSRF global deaktivieren
                .build();
    }
}