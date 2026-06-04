package uk.gov.justice.laa_civil_manage_api.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;

class SecurityConfigTest {

  private static final String ISSUER = "https://login.microsoftonline.com/test-tenant/v2.0";
  private static final String AUDIENCE = "test-client-id";

  private final OAuth2TokenValidator<Jwt> validator =
      SecurityConfig.jwtValidator(ISSUER, List.of(AUDIENCE));

  private Jwt.Builder validJwt() {
    Instant now = Instant.now();
    return Jwt.withTokenValue("token")
        .header("alg", "RS256")
        .header("typ", "JWT")
        .issuer(ISSUER)
        .audience(List.of(AUDIENCE))
        .subject("user")
        .issuedAt(now)
        .expiresAt(now.plusSeconds(300));
  }

  @Test
  void acceptsTokenWithExpectedIssuerAndAudience() {
    assertFalse(validator.validate(validJwt().build()).hasErrors());
  }

  @Test
  void rejectsTokenWithWrongAudience() {
    Jwt jwt = validJwt().audience(List.of("api://some-other-application")).build();
    assertTrue(validator.validate(jwt).hasErrors());
  }

  @Test
  void rejectsTokenFromWrongIssuer() {
    Jwt jwt = validJwt().issuer("https://login.microsoftonline.com/other-tenant/v2.0").build();
    assertTrue(validator.validate(jwt).hasErrors());
  }
}
