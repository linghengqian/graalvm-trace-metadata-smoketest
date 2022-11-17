package com.lingh.net.stream;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;

import java.time.Instant;
import java.util.UUID;

public class Client extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runExample(Client.class);
    }

    @Override
    public void start() {
        vertx.createNetClient().connect(1234, "localhost", ar -> {
            if (ar.succeeded()) {
                NetSocket socket = ar.result();
                BatchStream batchStream = new BatchStream(socket, socket);
                batchStream.pause();

                batchStream.handler(batch -> System.out.println("Client Received : " + batch.getRaw().toString())).endHandler(v -> batchStream.end())
                        .exceptionHandler(t -> {
                            t.printStackTrace();
                            batchStream.end();
                        });
                batchStream.resume();
                JsonObject jsonObject = new JsonObject()
                        .put("id", UUID.randomUUID().toString())
                        .put("name", "Vert.x")
                        .put("timestamp", Instant.now());
                JsonArray jsonArray = new JsonArray()
                        .add(UUID.randomUUID().toString())
                        .add("Vert.x")
                        .add(Instant.now());
                Buffer buffer = Buffer.buffer("Vert.x is awesome!");
                batchStream.write(new Batch(jsonObject));
                batchStream.write(new Batch(jsonArray));
                batchStream.write(new Batch(buffer));
            } else {
                System.out.println("Failed to connect " + ar.cause());
            }
        });
    }
}
