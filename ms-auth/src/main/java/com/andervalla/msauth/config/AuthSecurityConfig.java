package com.andervalla.msauth.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.jackson.OAuth2AuthorizationServerJacksonModule;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import com.andervalla.msauth.clients.dtos.response.UsuarioResponse;
import com.andervalla.msauth.repositories.CredencialRepository;
import com.andervalla.msauth.services.security.ForceRefreshTokenGenerator;
import com.andervalla.msauth.services.auth.UsuarioProvisioningService;
import java.time.Instant;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import com.andervalla.msauth.services.security.CredencialPasswordService;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2AccessTokenGenerator;
import org.springframework.security.jackson.SecurityJacksonModules;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.json.JsonMapper;

/**
 * Configuración de seguridad para el Authorization Server y los endpoints de autenticación.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(TokenCookieProperties.class)
public class AuthSecurityConfig {

    /**
     * Registra (si no existe) el cliente público utilizado por la aplicación web.
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository(
            @Value("${app.security.client.id:ds-web}") String clientId,
            @Value("${app.security.client.redirect-uris:http://localhost:5173/login/oauth2/code}") String redirectUris,
            JdbcTemplate jdbcTemplate) {

        JdbcRegisteredClientRepository repository = new JdbcRegisteredClientRepository(jdbcTemplate);

        if (repository.findByClientId(clientId) == null) {
            RegisteredClient.Builder builder = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId(clientId)
                    .clientName(clientId)
                    .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .scope(OidcScopes.OPENID)
                    .scope(OidcScopes.PROFILE)
                    .scope("offline_access")
                    .clientSettings(ClientSettings.builder()
                            .requireAuthorizationConsent(false)
                            .requireProofKey(true)
                            .build())
                    .tokenSettings(TokenSettings.builder()
                            .accessTokenTimeToLive(Duration.ofMinutes(15))
                            .refreshTokenTimeToLive(Duration.ofDays(7))
                            .reuseRefreshTokens(false)
                            .build())
                    ;

            for (String uri : redirectUris.split(",")) {
                String trimmed = uri.trim();
                if (!trimmed.isEmpty()) {
                    builder.redirectUri(trimmed);
                }
            }

            RegisteredClient registeredClient = builder.build();
            repository.save(registeredClient);
        }

        return repository;
    }

    /**
     * Servicio JDBC para almacenar autorizaciones y revocar tokens.
     */
    @Bean
    public OAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate,
                                                           RegisteredClientRepository registeredClientRepository) {
        JdbcOAuth2AuthorizationService service =
                new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
        JsonMapper jsonMapper = authorizationJsonMapper();
        service.setAuthorizationRowMapper(
                new JdbcOAuth2AuthorizationService.JsonMapperOAuth2AuthorizationRowMapper(
                        registeredClientRepository, jsonMapper));
        service.setAuthorizationParametersMapper(
                new JdbcOAuth2AuthorizationService.JsonMapperOAuth2AuthorizationParametersMapper(jsonMapper));
        return service;
    }

    /**
     * ObjectMapper seguro para serializar/deserializar autorizaciones OAuth2.
     */
    @Bean
    public JsonMapper authorizationJsonMapper() {
        BasicPolymorphicTypeValidator.Builder ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("java.lang")
                .allowIfSubType("java.time")
                .allowIfSubType("java.util")
                .allowIfSubType("org.springframework.security")
                .allowIfSubType("org.springframework.security.oauth2");

        ClassLoader classLoader = AuthSecurityConfig.class.getClassLoader();
        java.util.List<JacksonModule> modules = SecurityJacksonModules.getModules(classLoader, ptv);
        modules.add(new OAuth2AuthorizationServerJacksonModule());

        return JsonMapper.builder()
                .addModules(modules)
                .build();
    }

    /**
     * Servicio JDBC para consentimientos OAuth2.
     */
    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(JdbcTemplate jdbcTemplate,
                                                                         RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
    }

    /**
     * Define el issuer del servidor de autorización.
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings(
            @Value("${app.security.issuer:http://localhost:9092}") String issuer) {
        return AuthorizationServerSettings.builder().issuer(issuer).build();
    }

    /**
     * Fuente de llaves JWK generadas en caliente para firmar tokens.
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = generateRsaKey();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    /**
     * Filtro que registra información de sesión en los flujos OAuth2.
     */
    @Bean
    public SessionLoggingFilter sessionLoggingFilter() {
        return new SessionLoggingFilter();
    }

    /**
     * Decodificador JWT usado por el recurso protegido del Authorization Server.
     */
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    /**
     * Codificador JWT que firma con la llave generada.
     */
    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    /**
     * Generador de tokens que asegura la emisión de refresh tokens cuando corresponde.
     */
    @Bean
    public OAuth2TokenGenerator<?> tokenGenerator(JwtEncoder jwtEncoder,
                                                  OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer,
                                                  ForceRefreshTokenGenerator forceRefreshTokenGenerator) {
        JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
        jwtGenerator.setJwtCustomizer(jwtCustomizer);
        OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
        return new DelegatingOAuth2TokenGenerator(jwtGenerator, accessTokenGenerator, forceRefreshTokenGenerator);
    }

    /**
     * Password encoder para credenciales locales del Authorization Server.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Coste elevado para BCrypt (rehash automático vía UserDetailsPasswordService)
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public UserDetailsPasswordService userDetailsPasswordService(CredencialRepository credencialRepository) {
        return new CredencialPasswordService(credencialRepository);
    }

    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder,
                                                       UserDetailsService userDetailsService,
                                                       UserDetailsPasswordService userDetailsPasswordService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsPasswordService(userDetailsPasswordService);
        return new ProviderManager(provider);
    }

    /**
     * Cadena de filtros para endpoints públicos como registro y reset de contraseña.
     */
    @Bean
    @Order(0)
    public SecurityFilterChain publicEndpointsChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/register/**", "/actuator/health", "/auth/password/**", "/auth/logout", "/password/**", "/error")
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    /**
     * Cadena de filtros específica del Authorization Server (tokens, OIDC, etc.).
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http,
                                                                      SessionLoggingFilter sessionLoggingFilter,
                                                                      OAuth2TokenGenerator<?> tokenGenerator,
                                                                      RefreshTokenCookieFilter refreshTokenCookieFilter,
                                                                      AuthenticationEntryPoint tokenErrorEntryPoint,
                                                                      TokenCookieAuthenticationSuccessHandler tokenSuccessHandler) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();

        http.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .sessionManagement(session -> session.sessionFixation().none())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")))
                .csrf(csrf -> csrf.ignoringRequestMatchers(authorizationServerConfigurer.getEndpointsMatcher()))
                .with(authorizationServerConfigurer, authServer -> authServer
                        .oidc(Customizer.withDefaults())
                        .tokenGenerator(tokenGenerator)
                        .tokenEndpoint(tokenEndpoint -> tokenEndpoint
                                .accessTokenResponseHandler(tokenSuccessHandler)
                                .errorResponseHandler((request, response, exception) ->
                                        tokenErrorEntryPoint.commence(request, response, exception))))
                // Habilita validación de tokens Bearer (userinfo, introspection, etc.)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .addFilterBefore(refreshTokenCookieFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(sessionLoggingFilter, LogoutFilter.class);
        return http.build();
    }

    /**
     * Cadena de filtros para el resto de endpoints protegidos con login de formulario.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, SessionLoggingFilter sessionLoggingFilter) throws Exception {

        http.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .sessionManagement(session -> session.sessionFixation().none())
                .formLogin(Customizer.withDefaults())
                // El login se usa solo para el flujo OAuth2; evitamos fallos de CSRF con navegadores/proxies intermedios.
                .csrf(csrf -> csrf.ignoringRequestMatchers("/login"))
                .addFilterBefore(sessionLoggingFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Entrada estándar para errores del endpoint de token.
     */
    @Bean
    public AuthenticationEntryPoint tokenErrorEntryPoint() {
        return new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handler que emite cookies HttpOnly con access/refresh tokens.
     */
    @Bean
    public TokenCookieAuthenticationSuccessHandler tokenSuccessHandler(TokenCookieProperties cookieProperties,
                                                                        AuthenticationEntryPoint tokenErrorEntryPoint) {
        return new TokenCookieAuthenticationSuccessHandler(cookieProperties, tokenErrorEntryPoint);
    }

    /**
     * Filtro para aceptar refresh_token desde cookie.
     */
    @Bean
    public RefreshTokenCookieFilter refreshTokenCookieFilter(TokenCookieProperties cookieProperties) {
        return new RefreshTokenCookieFilter(cookieProperties);
    }

    /**
     * Adapta credenciales locales a {@link org.springframework.security.core.userdetails.UserDetailsService}.
     */
    @Bean
    public UserDetailsService userDetailsService(CredencialRepository credencialRepository) {
        return username -> credencialRepository.findByCedula(username)
                .map(cred -> {
                    if (!cred.isEnabled()) {
                        throw new DisabledException("Usuario deshabilitado");
                    }
                    if (cred.getLockedUntil() != null && cred.getLockedUntil().isAfter(Instant.now())) {
                        throw new LockedException("Usuario bloqueado temporalmente");
                    }
                    return User.withUsername(cred.getCedula())
                            .password(cred.getPasswordHash())
                            .roles("USER")
                            .build();
                })
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales no encontradas"));
    }

    /**
     * Personaliza el access token/id token con claims del usuario (rol, alias, ids).
     */
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer(UsuarioProvisioningService usuarioProvisioningService) {
        return context -> {
            String tokenType = context.getTokenType().getValue();
            if (!"access_token".equals(tokenType) && !"id_token".equals(tokenType)) {
                return;
            }
            String cedula = context.getPrincipal().getName();
            UsuarioResponse usuario = usuarioProvisioningService.obtenerPorCedula(cedula);
            if (usuario == null || usuario.id() == null) {
                return;
            }

            context.getClaims()
                    .subject(String.valueOf(usuario.id()))
                    .claim("usuario_id", usuario.id())
                    .claim("rol", usuario.rol());

            if (usuario.entidad() != null && !usuario.entidad().isBlank()) {
                context.getClaims().claim("entidad", usuario.entidad());
            }
            if (usuario.aliasPublico() != null && !usuario.aliasPublico().isBlank()) {
                context.getClaims().claim("alias_publico", usuario.aliasPublico());
            }
            if (usuario.publicCitizenId() != null && !usuario.publicCitizenId().isBlank()) {
                context.getClaims().claim("public_citizen_id", usuario.publicCitizenId());
            }
        };
    }

    /**
     * Genera un par de llaves RSA para firmar y publicar en el JWK set.
     */
    private static RSAKey generateRsaKey() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(UUID.randomUUID().toString())
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate RSA key", e);
        }
    }

    /**
     * Filtro para registrar actividad de sesión durante el flujo OAuth2.
     */
    private static class SessionLoggingFilter extends OncePerRequestFilter {
        private static final Logger log = LoggerFactory.getLogger(SessionLoggingFilter.class);

        /**
         * Solo filtra rutas relevantes al flujo OAuth2.
         */
        @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
            String uri = request.getRequestURI();
            return !(uri.startsWith("/oauth2/authorize") || uri.startsWith("/login") || uri.startsWith("/error"));
        }

        /**
         * Registra información antes y después de procesar la petición.
         */
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            HttpSession session = request.getSession(false);
            String cookies = request.getHeader("Cookie");
            log.info("SessionLoggingFilter[req] uri={} query={} sessionId={} cookies={}",
                    request.getRequestURI(),
                    request.getQueryString(),
                    session != null ? session.getId() : "none",
                    cookies != null ? cookies : "none");
            filterChain.doFilter(request, response);
            HttpSession sessionAfter = request.getSession(false);
            var setCookies = response.getHeaders("Set-Cookie");
            if (setCookies != null && !setCookies.isEmpty()) {
                log.info("SessionLoggingFilter[res] uri={} sessionId={} set-cookie={}",
                        request.getRequestURI(),
                        sessionAfter != null ? sessionAfter.getId() : "none",
                        setCookies);
            }
        }
    }
}
