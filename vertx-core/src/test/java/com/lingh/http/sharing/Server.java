package com.lingh.http.sharing;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;


public class Server extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runExample(Server.class, null);
    }

    @Override
    public void start() {
        vertx.deployVerticle(
                "io.vertx.example.core.http.sharing.HttpServerVerticle",
                new DeploymentOptions().setInstances(2));
    }
}
