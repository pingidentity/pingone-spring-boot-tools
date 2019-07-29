package com.pingidentity.spring.example.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.security.oauth2.common.util.RandomValueStringGenerator;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

/**
 * Utility class to create, verify and remove the token nonce used to mitigate replay attacks.
 */
@Component
@SessionScope
public class NonceProvider {

  private List<String> nonces = new ArrayList<>();
  private RandomValueStringGenerator generator = new RandomValueStringGenerator(32);

  public boolean verify(OidcUser oidcUser) {
    return nonces.contains(oidcUser.getIdToken().getNonce());
  }

  public boolean remove(OidcUser oidcUser) {
    return nonces.remove(oidcUser.getIdToken().getNonce());
  }

  public String generate() {
    String nonce = generator.generate();
    nonces.add(nonce);
    return nonce;
  }

}
