package org.example.servlet;

import com.coveo.saml.SamlClient;
import com.coveo.saml.SamlException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.util.ExtendedSamlClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AuthServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(AuthServlet.class);

    private final ExtendedSamlClient client;

    public AuthServlet(final ExtendedSamlClient client) {
        this.client = client;
    }

    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        try {
            String encodedRequest = client.getSamlRequest();
            String idpUrl = client.getIdentityProviderUrl();
            log.info("Redirecting to IDP at url {}...", idpUrl);
            log.info("Generated and urlencoded SAML request: {}", encodedRequest);

            client.redirectToIdentityProvider(response, null);

        } catch (SamlException e) {
            log.error("Saml error", e);
        }
    }

}
