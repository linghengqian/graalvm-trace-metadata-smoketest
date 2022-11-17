package com.lingh.verticle.deploy;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;


public class DeployExample extends AbstractVerticle {


    public static void main(String[] args) {
        Runner.runExample(DeployExample.class);
    }

    @Override
    public void start() throws Exception {
        System.out.println("Main verticle has started, let's deploy some others...");
        vertx.deployVerticle("io.vertx.example.core.verticle.deploy.OtherVerticle");
        vertx.deployVerticle("io.vertx.example.core.verticle.deploy.OtherVerticle", res -> {
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

        JsonObject config = new JsonObject().put("foo", "bar");
        vertx.deployVerticle("io.vertx.example.core.verticle.deploy.OtherVerticle", new DeploymentOptions().setConfig(config));
        vertx.deployVerticle("io.vertx.example.core.verticle.deploy.OtherVerticle", new DeploymentOptions().setInstances(10));
        vertx.deployVerticle("io.vertx.example.core.verticle.deploy.OtherVerticle", new DeploymentOptions().setWorker(true));
    }
}
