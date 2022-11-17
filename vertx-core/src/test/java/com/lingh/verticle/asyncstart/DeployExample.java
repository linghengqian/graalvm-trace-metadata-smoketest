package com.lingh.verticle.asyncstart;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;


public class DeployExample extends AbstractVerticle {
    public static void main(String[] args) {
        Runner.runExample(DeployExample.class, null);
    }

    @Override
    public void start() {
        System.out.println("Main verticle has started, let's deploy some others...");
        vertx.deployVerticle("io.vertx.example.core.verticle.asyncstart.OtherVerticle", res -> {
            if (res.succeeded()) {
                String deploymentID = res.result();
                System.out.println("Other verticle deployed ok, deploymentID = " + deploymentID);
                vertx.undeploy(deploymentID, res2 -> {
                    if (res2.succeeded()) {
                        System.out.println("Undeployed ok!");
                    } else {
                        res2.cause().printStackTrace();
                    }
                });
            } else {
                res.cause().printStackTrace();
            }
        });
    }
}
