package org.example;

import com.coveo.saml.SamlClient;
import com.coveo.saml.SamlException;
import com.coveo.saml.SamlResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    static final private Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException {

        try (var resourceStream = Main.class.getResourceAsStream("/saml-metadata.xml")) {
            if (resourceStream != null) {
                try (var br = new BufferedReader(new InputStreamReader(resourceStream))) {

                    SamlClient client = SamlClient.fromMetadata(
                            "MyRelyingPartyIdentifier",
                            "http://some/url/that/processes/assertions",
                            br,
                            SamlClient.SamlIdpBinding.POST);

                    String encodedRequest = client.getSamlRequest();
                    String idpUrl = client.getIdentityProviderUrl();

                    log.info("Request: {}", encodedRequest);
                    log.info("idpUrl: {}", idpUrl);

                    launchJettyServer(client);

                }
            } else {
                log.info("Resource is null");
            }

        } catch (IOException | SamlException e) {
            log.error("SAML client creating error", e);
        }


    }

    public static void launchJettyServer(final SamlClient client) throws InterruptedException {
        var server = new Server(8090);
        var handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        handler.setContextPath("/");
        handler.addServlet(new ServletHolder(new BlockingServlet(client)), "/test");
        server.setHandler(handler);
        try {
            server.start();
        } catch (Exception e) {
            log.error("Jetty server launch exception", e);
        }
        server.join();
    }

}