package org.example;

import com.coveo.saml.SamlClient;
import com.coveo.saml.SamlException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.example.servlet.AuthServlet;
import org.example.servlet.SecuredServlet;
import org.example.util.ExtendedSamlClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class Main {

    static final private Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException {
        try (var isr = new InputStreamReader(new FileInputStream("idp-metadata.xml"))) {
            final var client = ExtendedSamlClient.fromMetadata(
                "http://CiscoISE/4d02140a-a821-43ee-a7ea-bf818a43b76c",
                "http://127.0.0.1:8090/secured",
                isr,
                ExtendedSamlClient.SamlIdpBinding.Redirect);
            client.setSPKeys("cert.pem", "private.pkcs8");
            launchJettyServer(client);
        } catch (IOException | SamlException e) {
            log.error("SAML client creating error", e);
        }
    }

    public static void launchJettyServer(final ExtendedSamlClient client) {
        var server = new Server(8090);
        var handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        handler.setContextPath("/");
        handler.addServlet(new ServletHolder(new AuthServlet(client)), "/auth");
        handler.addServlet(new ServletHolder(new SecuredServlet(client)), "/secured");
        server.setHandler(handler);
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            log.error("Jetty server launch exception", e);
        }
    }

}