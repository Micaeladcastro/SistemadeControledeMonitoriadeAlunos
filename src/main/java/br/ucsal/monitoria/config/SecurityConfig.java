package br.ucsal.monitoria.config;

import br.ucsal.monitoria.exception.ErroResponse;
import br.ucsal.monitoria.repository.UsuarioRepository;
import br.ucsal.monitoria.security.FrontendOnlyFilter;
import br.ucsal.monitoria.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity // habilita @PreAuthorize nos controllers
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtFilter,
                                                   FrontendOnlyFilter frontendOnlyFilter,
                                                   ObjectMapper objectMapper) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable) // API stateless com JWT
                .cors(cors -> {})                      // usa o bean corsConfigurationSource abaixo
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Cadastros exclusivos do administrador (bloqueados antes mesmo de ler o corpo da requisicao)
                        .requestMatchers("/api/usuarios/**", "/api/auditoria/**", "/api/docentes/**",
                                "/api/relatorios/estrutura-academica", "/api/relatorios/docentes").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/ies/**", "/api/centros-academicos/**", "/api/cursos/**").authenticated()
                        .requestMatchers("/api/ies/**", "/api/centros-academicos/**", "/api/cursos/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/discentes/**", "/api/disciplinas/**", "/api/historico/**")
                            .hasAnyRole("ADMINISTRADOR", "COLABORADOR")
                        // Demais regras finas (dono do registro etc.) ficam nos @PreAuthorize e nos services
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) ->
                                escreverErro(res, objectMapper, 401, "Unauthorized", "Faca login para acessar este recurso"))
                        .accessDeniedHandler((req, res, e) ->
                                escreverErro(res, objectMapper, 403, "Forbidden", "Seu perfil nao tem permissao para esta operacao")))
                .addFilterBefore(frontendOnlyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtFilter, FrontendOnlyFilter.class)
                .build();
    }

    // Os filtros sao @Component; isso evita que o Spring Boot os registre uma segunda vez fora do Security
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
        var registro = new FilterRegistrationBean<>(filter);
        registro.setEnabled(false);
        return registro;
    }

    @Bean
    public FilterRegistrationBean<FrontendOnlyFilter> frontendFilterRegistration(FrontendOnlyFilter filter) {
        var registro = new FilterRegistrationBean<>(filter);
        registro.setEnabled(false);
        return registro;
    }

    /** CORS: somente a origem do front-end Angular e aceita. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${app.frontend-origin}") String origens) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(origens.split(",")).map(String::trim).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", FrontendOnlyFilter.HEADER));
        config.setExposedHeaders(List.of("Content-Disposition"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /** Spring Security buscando os usuarios no PostgreSQL (restricao 1.3.1). */
    @Bean
    public UserDetailsService userDetailsService(UsuarioRepository usuarioRepository) {
        return login -> usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado"));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    private static void escreverErro(HttpServletResponse res, ObjectMapper om, int status, String erro, String msg)
            throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        om.writeValue(res.getWriter(), ErroResponse.of(status, erro, msg));
    }
}
