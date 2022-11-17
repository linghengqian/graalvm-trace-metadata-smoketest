package com.lingh.http.simpleform;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;

public class SimpleFormServer extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runExample(SimpleFormServer.class);
    }

    @Override
    public void start() {
        vertx.createHttpServer().requestHandler(req -> {
            if (req.uri().equals("/")) {
                req.response().sendFile("index.html");
            } else if (req.uri().startsWith("/form")) {
                req.response().setChunked(true);
                req.setExpectMultipart(true);
                req.endHandler((v) -> {
                    for (String attr : req.formAttributes().names()) {
                        req.response().write("Got attr " + attr + " : " + req.formAttributes().get(attr) + "\n");
                    }
                    req.response().end();
                });
            } else {
                req.response().setStatusCode(404).end();
            }
        }).listen(8080);
    }
}
