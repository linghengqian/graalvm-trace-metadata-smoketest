package com.lingh.verticle.deploy;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

import static org.assertj.core.api.Assertions.assertThat;

public class OtherVerticle extends AbstractVerticle {

    @Override
    public void start() {
        assertThat(config()).isEqualTo(new JsonObject());
    }

    @Override
    public void stop() {
        System.out.println("In OtherVerticle.stop");
    }
}
