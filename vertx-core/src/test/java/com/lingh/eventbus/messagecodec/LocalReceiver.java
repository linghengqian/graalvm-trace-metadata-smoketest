package com.lingh.eventbus.messagecodec;

import com.lingh.eventbus.messagecodec.util.CustomMessage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;

public class LocalReceiver extends AbstractVerticle {
    @Override
    public void start() {
        EventBus eventBus = getVertx().eventBus();
        eventBus.consumer("local-message-receiver", message -> {
            CustomMessage customMessage = (CustomMessage) message.body();
            System.out.println("Custom message received: " + customMessage.summary());
            CustomMessage replyMessage = new CustomMessage(200, "a00000002", "Message sent from local receiver!");
            message.reply(replyMessage);
        });
    }
}
