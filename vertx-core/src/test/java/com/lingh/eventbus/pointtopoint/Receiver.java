package com.lingh.eventbus.pointtopoint;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;


public class Receiver extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runClusteredExample(Receiver.class);
    }

    @Override
    public void start() {
        EventBus eb = vertx.eventBus();
        eb.consumer("ping-address", message -> {
            System.out.println("Received message: " + message.body());
            message.reply("pong!");
        });
        System.out.println("Receiver ready!");
    }
}
