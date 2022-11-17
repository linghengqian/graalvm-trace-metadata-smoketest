package com.lingh.http2.simple;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;

public class Client extends AbstractVerticle {

  @Override
  public void start() throws Exception {


    HttpClientOptions options = new HttpClientOptions().
      setSsl(true).
      setUseAlpn(true).
      setProtocolVersion(HttpVersion.HTTP_2).
      setTrustAll(true);

    HttpClient client = vertx.createHttpClient(options);
    client.request(HttpMethod.GET, 8080, "localhost", "/")
      .compose(req -> req.send()
        .compose(resp -> {
          System.out.println("Got response " + resp.statusCode());
          return resp.body();
        })).onSuccess(body -> {
      System.out.println("Got data " + body.toString("ISO-8859-1"));
    }).onFailure(Throwable::printStackTrace);
  }
}
