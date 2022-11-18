
package com.lingh.jsonstreaming;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.parsetools.JsonParser;

import java.util.concurrent.atomic.AtomicInteger;

import static io.vertx.core.parsetools.JsonEventType.VALUE;

public class JsonStreamingExample extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runExample(JsonStreamingExample.class, null);
    }

    @Override
    public void start() {
        vertx.fileSystem().open("src/test/resources/large.json", new OpenOptions(), ar -> {
            if (ar.succeeded()) {
                AsyncFile asyncFile = ar.result();
                AtomicInteger counter = new AtomicInteger();
                JsonParser jsonParser = JsonParser.newParser(asyncFile);
                jsonParser.objectValueMode()
                        .exceptionHandler(t -> {
                            t.printStackTrace();
                            asyncFile.close();
                        })
                        .endHandler(v -> {
                            System.out.println("Done!");
                            asyncFile.close();
                        }).handler(event -> {
                            if (event.type() == VALUE) {
                                DataPoint dataPoint = event.mapTo(DataPoint.class);
                                if (counter.incrementAndGet() % 100 == 0) {
                                    System.out.println("DataPoint = " + dataPoint);
                                }
                            }
                        });
            } else {
                ar.cause().printStackTrace();
            }
        });
    }
}
