package com.lingh.http2.push;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.net.PemKeyCertOptions;

public class Server extends AbstractVerticle {


    public static void main(String[] args) {
        Runner.runExample(Server.class, null);
    }

    @Override
    public void start() {

        HttpServer server =
                vertx.createHttpServer(new HttpServerOptions().
                        setUseAlpn(true).
                        setSsl(true).
                        setPemKeyCertOptions(new PemKeyCertOptions().setKeyPath("src/test/java/com/lingh/http2/push/server-key.pem")
                                .setCertPath("src/test/java/com/lingh/http2/push/server-cert.pem")
                        ));

        server.requestHandler(req -> {
            String path = req.path();
            HttpServerResponse resp = req.response();
            if ("/".equals(path)) {
                resp.push(HttpMethod.GET, "/script.js", ar -> {
                    if (ar.succeeded()) {
                        System.out.println("sending push");
                        HttpServerResponse pushedResp = ar.result();
                        pushedResp.sendFile("script.js");
                    } else {
                    }
                });

                resp.sendFile("index.html");
            } else if ("/script.js".equals(path)) {
                resp.sendFile("script.js");
            } else {
                System.out.println("Not found " + path);
                resp.setStatusCode(404).end();
            }
        });

        server.listen(8443, "localhost", ar -> {
            if (ar.succeeded()) {
                System.out.println("Server started");
            } else {
                ar.cause().printStackTrace();
            }
        });
    }
}
