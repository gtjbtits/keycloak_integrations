package org.example;

import com.coveo.saml.SamlClient;
import com.coveo.saml.SamlException;
import com.coveo.saml.SamlResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class SecuredServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(SecuredServlet.class);

    private final SamlClient client;

    public SecuredServlet(final SamlClient client) {
        this.client = client;
    }

    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

            final var encodedResponse = request.getParameter("SAMLResponse");
            if (encodedResponse != null && !encodedResponse.isEmpty()) {
                try {
                    final SamlResponse samlResponse = client.decodeAndValidateSamlResponse(encodedResponse, "POST");
                    final String authenticatedUser = samlResponse.getNameID();
                    response.setContentType("text/plain");
                    try (final var writer = response.getWriter()) {
                        writer.write("Hello, " + authenticatedUser + "!\n");
                    }
                } catch (SamlException e) {
                    response.setContentType("text/html");
                    response.setStatus(500);
                    try (
                            final var writer = response.getWriter();
                            StringWriter sw = new StringWriter();
                            PrintWriter pw = new PrintWriter(sw)) {
                        writer.write("<html><body>");
                        writer.write("<h3>Error while SAML response processing</h3>");
                        writer.write("<pre>");
                        e.printStackTrace(pw);
                        String sStackTrace = sw.toString();
                        writer.write(sStackTrace);
                        writer.write("</pre></body></html>");
                    }
                    log.error("Error while SAML response processing", e);
                }
            } else {
                response.sendError(401, "No SAML response provided");
            }
    }

}
