package com.lingh.http.simpleformupload;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;

public class SimpleFormUploadServer extends AbstractVerticle {
    public static void main(String[] args) {
        Runner.runExample(SimpleFormUploadServer.class, null);
    }

    @Override
    public void start() {
        vertx.createHttpServer().requestHandler(req -> {
            if (req.uri().equals("/")) {
                req.response().sendFile("index.html");
            } else if (req.uri().startsWith("/form")) {
                req.setExpectMultipart(true);
                req.uploadHandler(upload -> {
                    // FIXME - Potential security exploit! In a real system you must check this filename
                    // to make sure you're not saving to a place where you don't want!
                    // Or better still, just use Vert.x-Web which controls the upload area.
                    upload.streamToFileSystem(upload.filename())
                            .onSuccess(v -> req.response().end("Successfully uploaded to " + upload.filename()))
                            .onFailure(err -> req.response().end("Upload failed"));
                });
            } else {
                req.response()
                        .setStatusCode(404)
                        .end();
            }
        }).listen(8080);

    }
}
