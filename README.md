# PingOne for Customers Spring Boot SDK
This SDK using PingOne for Customers (Ping14C) API with a help of spring boot framework allows you to:
- manage your organization’s users and applications
- implement users authorization and authentication 

## OAuth 2.0 Basics
 
### OAuth 2.0 roles:

+ Resource owner (the User) – An entity capable of granting access to a protected resource. When the resource owner is a person, it is referred to as an end-user.
+ Resource server (the API server) – The server hosting the protected resources, capable of accepting and responding to protected resource requests using access tokens.
+ Client – An application making protected resource requests on behalf of the resource owner and with its authorization.
+ Authorization server – The server issuing access tokens to the client after successfully authenticating the resource owner and obtaining authorization.

### OAuth 2.0 Grant Types:

When a client application wants access to the resources of a resource owner hosted on a resource server, the client application must first obtain an authorization grant.
OAuth2 provides several authorization grants. Each grant type serves a different purpose and is used in a different way. Depending on what type of service you are building, you might need to use one or more of these grant types to make your application work.

The grant types defined are:
- Authorization Code
- Implicit
- Resource Owner Password Credentials
- Client Credentials

### PingOne for Customers Authorization Request Tips

+ `acr_values` - A string that designates whether the authentication request includes steps for a single-factor or multi-factor authentication flow. The value specified must be the name of a sign-on policy for which the application has a sign-on policy assignment. 
The acr_values parameter values are sign-on policy names and should be listed in order of preference. Only scopes from one resource access grant can be minted in an access token (except for scopes for the OpenID Connect platform resource).
+ `prompt` - A string that specifies whether the user is prompted to login for re-authentication. The prompt parameter can be used as a way to check for existing authentication, verifying that the user is still present for the current session. 
For `prompt=none`, the user is never prompted to login to re-authenticate, which can result in an error if authentication is required. 
For `prompt=login`, if time since last login is greater than the max-age, then the current session is stashed away in the flow state and treated in the flow as if there was no previous existing session. When the flow completes, if the flow’s user is the same as the user from the stashed away session, the stashed away session is updated with the new flow data and persisted (preserving the existing session ID). If the flow’s user is not the same as the user from the stashed away session, the stashed away session is deleted (logout) and the new session is persisted.

## Spring Nuances

__CSRF Protection__
A general requirement for proper CSRF prevention is to ensure your website uses proper HTTP verbs: be certain that your application is using PATCH, POST, PUT, and/or DELETE for anything that modifies state. CSRF protection is enabled by default with our Java Configuration. 
Also, make sure you use the `_csrf` request attribute to obtain the current `CsrfToken` in your forms.
```
<input type="hidden"
    name="${_csrf.parameterName}"
    value="${_csrf.token}"/>
```


## Developer Notes:
1. For more information about Spring OAuth2 Boot please check [Spring Security Reference about OAuth 2.0 Client](https://docs.spring.io/spring-security/site/docs/current/reference/html5/#oauth2client).
2. If you want to force logout in all sessions of a user then use `sessionRegistry.getAllSessions().expireNow();`.
This requires `ConcurrentSessionFilter` (or any other filter in the chain), that checks SessionInformation and calls all logout handlers and then do redirect.
3. To enable [SSL](https://ru.wikipedia.org/wiki/SSL) (Secure Sockets Layer) in your applications you should:
   - change server port `server.port: 8433`
   - enable ssl by its self: via properties - `server.ssl.enabled: true` **or** programmatically in the Spring security
configuration class, override `protected void configure(HttpSecurity http) throws Exception` method 
   ```
     @Override
     protected void configure(HttpSecurity http) throws Exception {

       http.requiresChannel()
           .anyRequest()
           .requiresSecure()
     }
   ```
   - fill certificate related params:
      - `server.ssl.key-store-type`  the format used for the keystore (i.e PKCS12, JKS file)
      - `server.ssl.key-store` the path to the keystore containing the certificate
      - `server.ssl.key-store-password` the password used to generate the certificate
      - `server.ssl.key-alias` the alias mapped to self-signed certificate
<br>_`TIP:`_ ["how to create self-signed SSL certificate"](https://oracle-base.com/articles/linux/create-self-signed-ssl-certificates)
4. Thymeleaf LEGACYHTM5 configuration (`spring.thymeleaf.mode=LEGACYHTML5`) will allow you to use more casual HTML5 tags if you want to. Otherwise, Thymeleaf will be very strict and may not parse your HTML. For instance, if you do not close an input tag, Thymeleaf will not parse your HTML.
5. We use [`pattern`](https://html.spec.whatwg.org/multipage/input.html#the-pattern-attribute) attribute for password input HTML elements. It allows us to define our own rule to validate the input value using Regular Expressions ([RegEx](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Regular_Expressions)). It works on most browsers - those that support JavaScript 1.5 (Firefox, Chrome, Safari, Opera 7 and Internet Explorer 8 and higher), but very old browsers may not recognise these patterns.
One thing that would be good to know about here is a [lookahead assertion](https://www.rexegg.com/regex-disambiguation.html#lookarounds)(`(?= … )`groups)
6. Until we are storing `spring-boot-sdk` jar in GitHub with [GitHub Maven Plugins](https://github.com/github/maven-plugins) (that should not be a case until at least [October of 2019](https://rawgit.com/)), you need to keep this server configuration in maven `settings.xml`:
```xml
<server>
 <id>github</id>
 <password>OAUTH2TOKEN</password>
</server>
```
where `OAUTH2TOKEN` is a [personal access token](https://help.github.com/en/articles/creating-a-personal-access-token-for-the-command-line) you need to create (unless you have some) if you have Two-factor Authentication, or
```xml
<server>
 <id>github</id>
 <username>GitHubLogin</username>
 <password>GitHubPassw0rd</password>
</server>
```
in a simple user:password case.

So, every time you run: `mvn deploy -DcommitMessage="Custom message"`, the new archive from the local repository `${project.build.directory}/mvn-repo` is uploaded to `https://github.com/pingidentity/pingone-customers-spring-boot-tools/tree/mvn-repo`.
Also, be aware, that if you run `mvn clean deploy ...` you will clean the old versions and github repo will contain only the last built one.

Please don't forget to set `OAUTH2TOKEN` as environment variable(if you are using it) for login failures prevention:
```bash
export GITHUB_OAUTH_TOKEN={OAUTH2TOKEN}
``` 
 


