package com.lingh.eventbus.messagecodec;

import com.lingh.Runner;
import com.lingh.eventbus.messagecodec.util.CustomMessage;
import com.lingh.eventbus.messagecodec.util.CustomMessageCodec;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;


public class Sender extends AbstractVerticle {
    public static void main(String[] args) {
        Runner.runClusteredExample(Sender.class);
    }

    @Override
    public void start() {
        EventBus eventBus = getVertx().eventBus();
        eventBus.registerDefaultCodec(CustomMessage.class, new CustomMessageCodec());
        CustomMessage clusterWideMessage = new CustomMessage(200, "a00000001", "Message sent from publisher!");
        CustomMessage localMessage = new CustomMessage(200, "a0000001", "Local message!");
        getVertx().setPeriodic(1000, _id -> eventBus.request("cluster-message-receiver", clusterWideMessage, reply -> {
            if (reply.succeeded()) {
                CustomMessage replyMessage = (CustomMessage) reply.result().body();
                System.out.println("Received reply: " + replyMessage.summary());
            } else {
                System.out.println("No reply from cluster receiver");
            }
        }));
        getVertx().deployVerticle(LocalReceiver.class.getName(), deployResult -> {
            if (deployResult.succeeded()) {
                getVertx().setPeriodic(2000, _id -> eventBus.request("local-message-receiver", localMessage, reply -> {
                    if (reply.succeeded()) {
                        CustomMessage replyMessage = (CustomMessage) reply.result().body();
                        System.out.println("Received local reply: " + replyMessage.summary());
                    } else {
                        System.out.println("No reply from local receiver");
                    }
                }));
            } else {
                deployResult.cause().printStackTrace();
            }
        });
    }
}
