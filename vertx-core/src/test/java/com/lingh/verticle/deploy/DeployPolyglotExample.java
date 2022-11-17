package com.lingh.verticle.deploy;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;

public class DeployPolyglotExample extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runExample(DeployPolyglotExample.class);
    }

    @Override
    public void start() throws Exception {
        System.out.println("Main verticle has started, let's deploy A JS one...");
        vertx.deployVerticle("jsverticle.js");
    }
}
