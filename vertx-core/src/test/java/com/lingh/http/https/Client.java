package com.lingh.http.https;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;

public class Client extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runExample(Client.class, null);
    }

    @Override
    public void start() {
        HttpClientOptions options = new HttpClientOptions().setSsl(true).setTrustAll(true);
        HttpClient client = vertx.createHttpClient(options);
        client.request(HttpMethod.GET, 4443, "localhost", "/")
                .compose(req -> req.send()
                        .compose(resp -> {
                            System.out.println("Got response " + resp.statusCode());
                            return resp.body();
                        })).onSuccess(body -> System.out.println("Got data " + body.toString("ISO-8859-1"))).onFailure(Throwable::printStackTrace);
    }
}
