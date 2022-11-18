package com.lingh.http.sendfile;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;


public class SendFile extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runExample(SendFile.class, null);
    }

    @Override
    public void start() {
        vertx.createHttpServer().requestHandler(req -> {
            String filename = null;
            if (req.path().equals("/")) {
                filename = "src/test/java/com/lingh/http/sendfile/index.html";
            } else if (req.path().equals("/page1.html")) {
                filename = "src/test/java/com/lingh/http/sendfile/page1.html";
            } else if (req.path().equals("/page2.html")) {
                filename = "src/test/java/com/lingh/http/sendfile/page2.html";
            } else {
                req.response().setStatusCode(404).end();
            }
            if (filename != null) {
                req.response().sendFile(filename);
            }
        }).listen(8080);
    }
}
