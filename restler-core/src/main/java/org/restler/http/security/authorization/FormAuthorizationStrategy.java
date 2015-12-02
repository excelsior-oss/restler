package org.restler.http.security.authorization;

import org.restler.client.RestlerException;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * The implementation that performs an HTTP(S) post request of a login form data to obtain a session id.
 */
public class FormAuthorizationStrategy implements AuthorizationStrategy {

    protected final URI url;
    protected final String loginParameterName;
    protected final String loginParameterValue;
    protected final String passwordParameterName;
    protected final String passwordParameterValue;
    protected final String cookieName = "JSESSIONID";

    /**
     * Creates a strategy that uses custom parameter names.
     *
     * @param url                   url of the login service.
     * @param login                 login of the user
     * @param loginParameterName    name of the form parameter holding the <tt>login</tt> value
     * @param password              password of the user
     * @param passwordParameterName name of the form parameter holding the <tt>password</tt> value
     */
    public FormAuthorizationStrategy(URI url, String login, String loginParameterName, String password, String passwordParameterName) {
        this.url = url;
        this.loginParameterValue = login;
        this.passwordParameterValue = password;
        this.loginParameterName = loginParameterName;
        this.passwordParameterName = passwordParameterName;
    }

    @Override
    public Object authorize() {
        try {
            byte[] postData = getPostData();
            HttpURLConnection conn = (HttpURLConnection) url.toURL().openConnection();
            submitForm(postData, conn);
            return getCookies(conn).findAny().
                    map(s -> s.split("[=;]")[1]).
                    orElseThrow(() -> new RestlerException("Cookie " + cookieName + " not found in response on authorization request"));
        } catch (IOException e) {
            throw new RestlerException("Could not authorize request", e);
        }
    }

    private byte[] getPostData() {
        String urlParameters = loginParameterName + "=" + loginParameterValue + "&" +
                passwordParameterName + "=" + passwordParameterValue;
        return urlParameters.getBytes(StandardCharsets.UTF_8);
    }

    private HttpURLConnection submitForm(byte[] postData, HttpURLConnection conn) throws IOException {
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("charset", "utf-8");
        conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
        conn.setUseCaches(false);
        try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
            wr.write(postData);
        }
        return conn;
    }

    private Stream<String> getCookies(HttpURLConnection conn) {
        String headerName;
        for (int i = 1; (headerName = conn.getHeaderFieldKey(i)) != null; i++) {
            if (headerName.equals("Set-Cookie")) {
                String cookie = conn.getHeaderField(i);
                return Arrays.stream(cookie.split(";"));
            }
        }
        return Stream.empty();
    }

}
