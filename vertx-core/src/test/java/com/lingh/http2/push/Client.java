package com.lingh.http2.push;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.*;

public class Client extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runExample(Client.class, null);
    }

    @Override
    public void start() {
        HttpClientOptions options = new HttpClientOptions().
                setSsl(true).
                setUseAlpn(true).
                setProtocolVersion(HttpVersion.HTTP_2).
                setTrustAll(true);
        HttpClient client = vertx.createHttpClient(options);
        client.request(HttpMethod.GET, 8080, "localhost", "/").compose(request -> {
            request.pushHandler(pushedReq -> {
                System.out.println("Receiving pushed content");
                pushedReq.response().compose(HttpClientResponse::body).onSuccess(body -> System.out.println("Got pushed data " + body.toString("ISO-8859-1")));
            });
            return request.send().compose(resp -> {
                System.out.println("Got response " + resp.statusCode() + " with protocol " + resp.version());
                return resp.body();
            });
        }).onSuccess(body -> System.out.println("Got data " + body.toString("ISO-8859-1"))).onFailure(Throwable::printStackTrace);
    }
}
