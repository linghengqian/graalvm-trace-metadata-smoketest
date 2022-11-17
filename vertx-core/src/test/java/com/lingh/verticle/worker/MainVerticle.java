package com.lingh.verticle.worker;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;

public class MainVerticle extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runExample(MainVerticle.class, null);
    }

    @Override
    public void start() {
        System.out.println("[Main] Running in " + Thread.currentThread().getName());
        vertx.deployVerticle("io.vertx.example.core.verticle.worker.WorkerVerticle",
                new DeploymentOptions().setWorker(true));
        vertx.eventBus().request(
                "sample.data",
                "hello vert.x",
                r -> System.out.println("[Main] Receiving reply ' " + r.result().body()
                        + "' in " + Thread.currentThread().getName())
        );
    }
}
