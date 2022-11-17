package com.lingh.http2.customframes;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.net.PemKeyCertOptions;


public class Server extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runExample(Server.class);
    }

    @Override
    public void start() {
        HttpServer server = vertx.createHttpServer(new HttpServerOptions().
                        setUseAlpn(true).
                        setSsl(true).
                        setPemKeyCertOptions(new PemKeyCertOptions().setKeyPath("server-key.pem").setCertPath("server-cert.pem")
                        ));
        server.requestHandler(req -> {
            HttpServerResponse resp = req.response();
            req.customFrameHandler(frame -> {
                System.out.println("Received client frame " + frame.payload().toString("UTF-8"));
                resp.writeCustomFrame(10, 0, Buffer.buffer("pong"));
            });
        }).listen(8443);
    }
}
