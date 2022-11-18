package com.lingh.http2.customframes;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.net.PemKeyCertOptions;

import java.nio.charset.StandardCharsets;


public class Server extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runExample(Server.class, null);
    }

    @Override
    public void start() throws Exception {
        vertx.createHttpServer(new HttpServerOptions().
                setUseAlpn(true).
                setSsl(true).
                setPemKeyCertOptions(new PemKeyCertOptions().setKeyPath("src/test/java/com/lingh/http2/customframes/server-key.pem")
                        .setCertPath("src/test/java/com/lingh/http2/customframes/server-cert.pem")
                )).requestHandler(req -> {
            HttpServerResponse resp = req.response();
            req.customFrameHandler(frame -> {
                System.out.printf("Received client frame %s%n", frame.payload().toString(StandardCharsets.UTF_8));
                resp.writeCustomFrame(10, 0, Buffer.buffer("pong"));
            });
        }).listen(8443);
    }
}
