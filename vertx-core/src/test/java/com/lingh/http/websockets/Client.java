package com.lingh.http.websockets;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;

public class Client extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runExample(Client.class, null);
    }

    @Override
    public void start() {
        HttpClient client = vertx.createHttpClient();

        client.webSocket(8080, "localhost", "/some-uri").onSuccess(webSocket -> {
            webSocket.handler(data -> {
                System.out.println("Received data " + data.toString("ISO-8859-1"));
                client.close();
            });
            webSocket.writeBinaryMessage(Buffer.buffer("Hello world"));
        }).onFailure(Throwable::printStackTrace);
    }
}
