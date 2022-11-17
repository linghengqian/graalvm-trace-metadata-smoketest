package com.lingh.verticle.deploy;

import io.vertx.core.AbstractVerticle;

public class OtherVerticle extends AbstractVerticle {

    @Override
    public void start() {
        System.out.println("In OtherVerticle.start");
        System.out.println("Config is " + config());
    }

    @Override
    public void stop() {
        System.out.println("In OtherVerticle.stop");
    }
}
