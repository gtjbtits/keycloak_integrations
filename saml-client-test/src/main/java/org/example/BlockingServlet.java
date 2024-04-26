package org.example;

import com.coveo.saml.SamlClient;
import com.coveo.saml.SamlException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class BlockingServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(BlockingServlet.class);

    private final SamlClient client;

    public BlockingServlet(final SamlClient client) {
        this.client = client;
    }

    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

//        response.setContentType("application/json");
//        response.setStatus(HttpServletResponse.SC_OK);
//        response.getWriter().println("{ \"status\": \"ok\"}");

        try {
            client.redirectToIdentityProvider(response, null);
        } catch (SamlException e) {
            log.error("Saml error", e);
        }
    }

}
