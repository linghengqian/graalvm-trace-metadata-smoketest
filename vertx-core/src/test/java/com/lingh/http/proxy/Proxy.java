package com.lingh.http.proxy;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServerResponse;

public class Proxy extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runExample(Proxy.class);
    }

    @Override
    public void start() {
        HttpClient client = vertx.createHttpClient(new HttpClientOptions());
        vertx.createHttpServer().requestHandler(serverRequest -> {
            System.out.println("Proxying request: " + serverRequest.uri());
            serverRequest.pause();
            HttpServerResponse serverResponse = serverRequest.response();
            client.request(serverRequest.method(), 8282, "localhost", serverRequest.uri())
                    .onSuccess(clientRequest -> {
                        clientRequest.headers().setAll(serverRequest.headers());
                        clientRequest.send(serverRequest).onSuccess(clientResponse -> {
                            System.out.println("Proxying response: " + clientResponse.statusCode());
                            serverResponse.setStatusCode(clientResponse.statusCode());
                            serverResponse.headers().setAll(clientResponse.headers());
                            serverResponse.send(clientResponse);
                        }).onFailure(err -> {
                            System.out.println("Back end failure");
                            serverResponse.setStatusCode(500).end();
                        });
                    }).onFailure(err -> {
                        System.out.println("Could not connect to localhost");
                        serverResponse.setStatusCode(500).end();
                    });
        }).listen(8080);
    }
}
