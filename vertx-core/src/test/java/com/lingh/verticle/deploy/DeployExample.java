package com.lingh.verticle.deploy;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;


public class DeployExample extends AbstractVerticle {


    public static void main(String[] args) {
        Runner.runExample(DeployExample.class, null);
    }

    @Override
    public void start() {
        System.out.println("Main verticle has started, let's deploy some others...");
        vertx.deployVerticle(com.lingh.verticle.deploy.OtherVerticle.class.getName());
        vertx.deployVerticle(com.lingh.verticle.deploy.OtherVerticle.class.getName(), res -> {
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
        vertx.deployVerticle(com.lingh.verticle.deploy.OtherVerticle.class.getName(), new DeploymentOptions().setConfig(config));
        vertx.deployVerticle(com.lingh.verticle.deploy.OtherVerticle.class.getName(), new DeploymentOptions().setInstances(10));
        vertx.deployVerticle(com.lingh.verticle.deploy.OtherVerticle.class.getName(), new DeploymentOptions().setWorker(true));
    }
}
