package com.lingh.http2.customframes;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;

import java.nio.charset.StandardCharsets;

public class Client extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runExample(Client.class, null);
    }

    @Override
    public void start() throws Exception {
        HttpClient client = vertx.createHttpClient(new HttpClientOptions().
                setSsl(true).
                setUseAlpn(true).
                setProtocolVersion(HttpVersion.HTTP_2).
                setTrustAll(true));

        client.request(HttpMethod.GET, 8443, "localhost", "/")
                .onSuccess(request -> {
                    request.response().onSuccess(resp -> resp.customFrameHandler(
                            frame -> System.out.println("Got frame from server " + frame.payload().toString(StandardCharsets.UTF_8))
                    ));
                    request.sendHead().onSuccess(v -> vertx.setPeriodic(1000, timerID -> {
                        System.out.println("Sending ping frame to server");
                        request.writeCustomFrame(10, 0, Buffer.buffer("ping"));
                    }));
                });
    }
}