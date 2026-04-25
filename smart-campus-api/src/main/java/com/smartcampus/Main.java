package com.smartcampus;

import com.smartcampus.config.SmartCampusApplication;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import java.net.URI;

public class Main {

    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws Exception {
        int port = (args.length > 0) ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        URI baseUri = URI.create("http://localhost:" + port + "/api/v1/");

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
                baseUri,
                new SmartCampusApplication()
        );

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("\nShutting down Smart Campus API ...");
                server.shutdownNow();
            }
        }));

        System.out.println("===========================================================");
        System.out.println(" Smart Campus API is RUNNING");
        System.out.println(" Base URL : " + baseUri);
        System.out.println(" Try     : curl " + baseUri);
        System.out.println(" Stop    : Ctrl-C");
        System.out.println("===========================================================");

        Thread.currentThread().join();
    }
}
