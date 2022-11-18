package com.lingh.http.https;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;

public class Server extends AbstractVerticle {
    public static void main(String[] args) {
        Runner.runExample(Server.class, null);
    }

    @Override
    public void start() {
        HttpServer server = vertx.createHttpServer(new HttpServerOptions().setSsl(true).setKeyStoreOptions(
                new JksOptions().setPath("src/test/java/com/lingh/http/https/server-keystore.jks").setPassword("wibble")
        ));
        server.requestHandler(req -> req.response()
                        .putHeader("content-type", "text/html")
                        .end("<html><body><h1>Hello from vert.x!</h1></body></html>"))
                .listen(4443);
    }
}
