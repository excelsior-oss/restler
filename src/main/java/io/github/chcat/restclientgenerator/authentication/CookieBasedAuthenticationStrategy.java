package io.github.chcat.restclientgenerator.authentication;

import io.github.chcat.restclientgenerator.AuthenticationStrategy;
import org.springframework.http.RequestEntity;

/**
 * Created by pasa on 19.02.2015.
 */
public class CookieBasedAuthenticationStrategy implements AuthenticationStrategy {

    private final String COOKIE_HEADER = "Cookie";

    private String cookieName;
    private String cookieValue;

    public CookieBasedAuthenticationStrategy(){
        this.cookieName = "JSESSIONID";
    }

    public CookieBasedAuthenticationStrategy(String cookieName){
        this.cookieName = cookieName;
    }

    @Override
    public void addAuthentication(RequestEntity<?> request) {
        request.getHeaders().add(COOKIE_HEADER, cookieName + "=" + cookieValue);
    }

    public String getCookieName() {
        return cookieName;
    }

    public String getCookieValue() {
        return cookieValue;
    }

    public void setCookieValue(String cookieValue) {
        this.cookieValue = cookieValue;
    }
}
