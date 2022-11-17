package com.lingh.http.websockets;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;

public class Server extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runExample(Server.class);
    }

    @Override
    public void start() throws Exception {
        vertx.createHttpServer().webSocketHandler(ws -> ws.handler(ws::writeBinaryMessage)).requestHandler(req -> {
            if (req.uri().equals("/")) req.response().sendFile("ws.html");
        }).listen(8080);
    }
}
