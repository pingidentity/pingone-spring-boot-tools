package com.pingidentity.spring.example.config;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class OAuth2LoginSecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  private ClientRegistrationRepository clientRegistrationRepository;
  @Autowired
  private NonceProvider nonceProvider;

  @Override
  protected void configure(HttpSecurity http) throws Exception {

    http.authorizeRequests()
        .antMatchers("/login", "/js/**", "/css/**", "/img/**", "/webjars/**").permitAll()
        .anyRequest().authenticated()

        // Handling Login
        .and().oauth2Login()
        .authorizationEndpoint()
        .authorizationRequestResolver(
            new CustomAuthorizationRequestResolver(
                this.clientRegistrationRepository))
        .and().loginPage("/login")
        .and().csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())

        // Handling Logout
        .and().logout()
        .logoutSuccessUrl("/")
        .logoutSuccessHandler(new CustomLogoutSuccessHandler())
        .invalidateHttpSession(true)
        .deleteCookies("JSESSIONID")
        .clearAuthentication(true);

  }

  /**
   * Customized {@link OAuth2AuthorizationRequest} resolver with additional parameters above the standard parameters
   * defined in the OAuth 2.0 Authorization Framework that matches on the (default) path
   * /oauth2/authorization/{registrationId}
   * <p>
   * Additional parameters are: prompt and acr_values.
   */
  public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver defaultAuthorizationRequestResolver;

    public CustomAuthorizationRequestResolver(
        ClientRegistrationRepository clientRegistrationRepository) {

      this.defaultAuthorizationRequestResolver =
          new DefaultOAuth2AuthorizationRequestResolver(
              clientRegistrationRepository, "/oauth2/authorization");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
      OAuth2AuthorizationRequest authorizationRequest =
          this.defaultAuthorizationRequestResolver.resolve(request);

      return authorizationRequest != null ?
          customAuthorizationRequest(authorizationRequest, request) :
          null;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(
        HttpServletRequest request, String clientRegistrationId) {

      OAuth2AuthorizationRequest authorizationRequest =
          this.defaultAuthorizationRequestResolver.resolve(
              request, clientRegistrationId);

      return authorizationRequest != null ? customAuthorizationRequest(authorizationRequest, request) : null;
    }

    private OAuth2AuthorizationRequest customAuthorizationRequest(
        OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request) {

      Map<String, Object> additionalParameters =
          new LinkedHashMap<>(authorizationRequest.getAdditionalParameters());
      additionalParameters.put("prompt", "login");

      additionalParameters.put("nonce", nonceProvider.generate());

      return OAuth2AuthorizationRequest.from(authorizationRequest)
          .additionalParameters(additionalParameters)
          .build();
    }
  }

  public class CustomLogoutSuccessHandler extends
      SimpleUrlLogoutSuccessHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
        throws IOException, ServletException {

      if (authentication.getPrincipal() instanceof OidcUser) {
        nonceProvider.remove((OidcUser) authentication.getPrincipal());
      }

      super.onLogoutSuccess(request, response, authentication);
    }
  }
}


