package com.lingh.eventbus.ssl;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.net.JksOptions;

public class Receiver extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runClusteredExample(Receiver.class, new VertxOptions().setEventBusOptions(new EventBusOptions()
                        .setSsl(true)
                        .setKeyStoreOptions(new JksOptions().setPath("src/test/java/com/lingh/eventbus/ssl/keystore.jks").setPassword("wibble"))
                        .setTrustStoreOptions(new JksOptions().setPath("src/test/java/com/lingh/eventbus/ssl/keystore.jks").setPassword("wibble"))
                )
        );
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
