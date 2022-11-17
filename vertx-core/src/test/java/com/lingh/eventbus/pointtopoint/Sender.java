package com.lingh.eventbus.pointtopoint;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;


public class Sender extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runClusteredExample(Sender.class);
    }

    @Override
    public void start() {
        EventBus eb = vertx.eventBus();
        vertx.setPeriodic(1000, v -> eb.request("ping-address", "ping!", reply -> {
            if (reply.succeeded()) {
                System.out.println("Received reply " + reply.result().body());
            } else {
                System.out.println("No reply");
            }
        }));
    }
}
