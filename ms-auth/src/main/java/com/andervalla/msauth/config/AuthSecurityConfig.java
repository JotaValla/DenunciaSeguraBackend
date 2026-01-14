package com.andervalla.msauth.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import com.andervalla.msauth.clients.dtos.UsuarioResponse;
import com.andervalla.msauth.repositories.CredencialRepository;
import com.andervalla.msauth.services.UsuarioProvisioningService;
import java.time.Instant;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@EnableWebSecurity
public class AuthSecurityConfig {

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

    @Bean
    public AuthorizationServerSettings authorizationServerSettings(
            @Value("${app.security.issuer:http://localhost:9092}") String issuer) {
        return AuthorizationServerSettings.builder().issuer(issuer).build();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = generateRsaKey();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(0)
    public SecurityFilterChain publicEndpointsChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/register/**", "/actuator/health")
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();

        http.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")))
                .csrf(csrf -> csrf.ignoringRequestMatchers(authorizationServerConfigurer.getEndpointsMatcher()))
                .with(authorizationServerConfigurer, authServer -> authServer.oidc(Customizer.withDefaults()))
                // Habilita validación de tokens Bearer (userinfo, introspection, etc.)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .formLogin(Customizer.withDefaults());
        return http.build();
    }

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
}
