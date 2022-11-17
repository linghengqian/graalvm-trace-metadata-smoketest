package com.lingh.http.sharing;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;

public class Client extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runExample(Client.class, null);
    }

    @Override
    public void start() {
        vertx.setPeriodic(1000, l -> {
            HttpClient client = vertx.createHttpClient();
            client.request(HttpMethod.GET, 8080, "localhost", "/")
                    .compose(req -> req.send()
                            .compose(HttpClientResponse::body))
                    .onSuccess(body -> System.out.println(body.toString("ISO-8859-1")))
                    .onFailure(Throwable::printStackTrace);
        });
    }
}
