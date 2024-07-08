package cat.udl.eps.softarch.demo.config;

import java.util.Arrays;
import java.util.List;

import cat.udl.eps.softarch.demo.filter.AuthTokenFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {
    @Value("${allowed-origins}")
    String[] allowedOrigins;

    private final AuthEntryPointJwt authEntryPointJwt;
    private final AuthTokenFilter authTokenFilter;

    public WebSecurityConfig(AuthEntryPointJwt authEntryPointJwt, AuthTokenFilter authTokenFilter) {
        this.authEntryPointJwt = authEntryPointJwt;
        this.authTokenFilter = authTokenFilter;
    }

    @Bean
    protected SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers(HttpMethod.GET, "/identity").authenticated()
                        .requestMatchers(HttpMethod.POST, "/signin").permitAll()
                        .requestMatchers(HttpMethod.POST, "/signup").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users").anonymous()
                        .requestMatchers(HttpMethod.POST, "/users/*").denyAll()
                        .requestMatchers(HttpMethod.DELETE, "/suppliers/*").hasRole("SUPPLIER")
                        .requestMatchers(HttpMethod.PATCH, "/suppliers/*").hasRole("SUPPLIER")
                        .requestMatchers(HttpMethod.POST, "/suppliers").anonymous()
                        .requestMatchers(HttpMethod.POST, "/mappings").hasRole("SUPPLIER")
                        .requestMatchers(HttpMethod.DELETE, "/mappings/*").hasRole("SUPPLIER")
                        .requestMatchers(HttpMethod.PATCH, "/mappings/{id}").hasRole("SUPPLIER")
                        .requestMatchers(HttpMethod.POST, "/mappings/{id}/columns").hasRole("SUPPLIER")
                        .requestMatchers(HttpMethod.PUT, "/mappings/{id}/columns/{columnId}").hasRole("SUPPLIER")
                        .requestMatchers(HttpMethod.DELETE, "/columns/*").hasRole("SUPPLIER")
                        .requestMatchers(HttpMethod.PATCH, "/columns/*").hasRole("SUPPLIER")
                        .requestMatchers(HttpMethod.POST, "/mappings/{id}/yaml/generate").hasRole("SUPPLIER")
                        .requestMatchers(HttpMethod.GET, "/mappings/{id}/columns").hasRole("SUPPLIER")
                        .requestMatchers(HttpMethod.POST, "/mappings/{id}/generate").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/**/*").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/**/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/**/*").authenticated()
                        .anyRequest().authenticated()
                )
                .exceptionHandling((exceptionHandling) -> exceptionHandling.authenticationEntryPoint(authEntryPointJwt))
                .sessionManagement((sessionManagement) -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors((cors) -> cors.configurationSource(corsConfigurationSource()))
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOriginPatterns(List.of("*"));
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    public SecurityEvaluationContextExtension securityEvaluationContextExtension() {
        return new SecurityEvaluationContextExtension();
    }
}
