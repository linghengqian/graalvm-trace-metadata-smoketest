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
                com.lingh.http.sharing.HttpServerVerticle.class.getName(),
                new DeploymentOptions().setInstances(2));
    }
}
