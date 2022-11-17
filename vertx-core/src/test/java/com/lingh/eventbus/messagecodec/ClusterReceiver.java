package com.lingh.eventbus.messagecodec;

import com.lingh.Runner;
import com.lingh.eventbus.messagecodec.util.CustomMessage;
import com.lingh.eventbus.messagecodec.util.CustomMessageCodec;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;


public class ClusterReceiver extends AbstractVerticle {
    public static void main(String[] args) {
        Runner.runClusteredExample(ClusterReceiver.class);
    }

    @Override
    public void start() throws Exception {
        EventBus eventBus = getVertx().eventBus();
        eventBus.registerDefaultCodec(CustomMessage.class, new CustomMessageCodec());
        eventBus.consumer("cluster-message-receiver", message -> {
            CustomMessage customMessage = (CustomMessage) message.body();
            System.out.println("Custom message received: " + customMessage.getSummary());
            CustomMessage replyMessage = new CustomMessage(200, "a00000002", "Message sent from cluster receiver!");
            message.reply(replyMessage);
        });
    }
}
