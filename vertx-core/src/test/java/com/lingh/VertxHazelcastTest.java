package com.lingh;

import com.lingh.eventbus.messagecodec.util.CustomMessage;
import com.lingh.eventbus.messagecodec.util.CustomMessageCodec;
import com.lingh.ha.Server;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GraalVM doesn't seem to support Hazelcast at all
 */
@ExtendWith(VertxExtension.class)
@Disabled
@DisabledInNativeImage
public class VertxHazelcastTest {

    @Test
    void testEventBusByPointToPoint(VertxTestContext testContext) {
        Vertx.clusteredVertx(new VertxOptions(), deployResult -> {
            if (deployResult.succeeded()) {
                Vertx vertx = deployResult.result();
                try {
                    EventBus receiverEb = vertx.eventBus();
                    receiverEb.consumer("ping-address", message -> {
                        assertThat(message.body()).isEqualTo("ping!");
                        message.reply("pong!");
                    });
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            } else {
                deployResult.cause().printStackTrace();
            }
        });
        Vertx.clusteredVertx(new VertxOptions(), deployResult -> {
            if (deployResult.succeeded()) {
                Vertx vertx = deployResult.result();
                try {
                    EventBus senderEb = vertx.eventBus();
                    vertx.setPeriodic(1000, v -> senderEb.request("ping-address", "ping!", reply -> {
                        if (reply.succeeded()) {
                            assertThat(reply.result().body()).isEqualTo("pong!");
                            testContext.completeNow();
                        } else {
                            System.out.println("No reply");
                        }
                    }));
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            } else {
                deployResult.cause().printStackTrace();
            }
        });
    }

    @Test
    void testEventBusByPublishSubscribe(VertxTestContext testContext) {
        Checkpoint responsesReceived = testContext.checkpoint(6);
        Vertx.clusteredVertx(new VertxOptions(), deployResult -> {
            if (deployResult.succeeded()) {
                Vertx vertx = deployResult.result();
                try {
                    EventBus eb = vertx.eventBus();
                    eb.consumer("news-feed", message -> {
                        assertThat(message.body()).isEqualTo("Some news!");
                        responsesReceived.flag();
                    });
                    eb.consumer("news-feed", message -> {
                        assertThat(message.body()).isEqualTo("Some news!");
                        responsesReceived.flag();
                    });
                    eb.consumer("news-feed", message -> {
                        assertThat(message.body()).isEqualTo("Some news!");
                        responsesReceived.flag();
                    });

                } catch (Throwable t) {
                    t.printStackTrace();
                }
            } else {
                deployResult.cause().printStackTrace();
            }
        });
        Vertx.clusteredVertx(new VertxOptions(), deployResult -> {
            if (deployResult.succeeded()) {
                Vertx vertx = deployResult.result();
                try {
                    EventBus eb = vertx.eventBus();
                    vertx.setPeriodic(1000, v -> eb.publish("news-feed", "Some news!"));
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            } else {
                deployResult.cause().printStackTrace();
            }
        });
    }

    @Test
    void testEventBusByMessageCodec(VertxTestContext testContext) {
        Verticle localReceiver = new AbstractVerticle() {
            @Override
            public void start() {
                getVertx().eventBus().consumer("local-message-receiver", message -> {
                    assertThat(((CustomMessage) message.body()).getSummary()).isIn("Message sent from publisher!", "Local message!");
                    message.reply(new CustomMessage(200, "a00000002", "Message sent from local receiver!"));
                });
            }
        };
        Vertx.clusteredVertx(new VertxOptions(), deployResult -> {
            if (deployResult.succeeded()) {
                Vertx vertx = deployResult.result();
                try {
                    EventBus clusterReceiverEventBus = vertx.eventBus();
                    clusterReceiverEventBus.registerDefaultCodec(CustomMessage.class, new CustomMessageCodec());
                    clusterReceiverEventBus.consumer("cluster-message-receiver", message -> {
                        assertThat(((CustomMessage) message.body()).getSummary()).isEqualTo("Message sent from publisher!");
                        message.reply(new CustomMessage(200, "a00000002", "Message sent from cluster receiver!"));
                        testContext.completeNow();
                    });
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            } else {
                deployResult.cause().printStackTrace();
            }
        });
        Vertx.clusteredVertx(new VertxOptions(), res -> {
            if (res.succeeded()) {
                Vertx vertx = res.result();
                try {
                    EventBus senderEventBus = vertx.eventBus();
                    senderEventBus.registerDefaultCodec(CustomMessage.class, new CustomMessageCodec());
                    vertx.setPeriodic(1000, _id -> senderEventBus
                            .request("cluster-message-receiver", new CustomMessage(200, "a00000001",
                                            "Message sent from publisher!"),
                                    reply -> {
                                        if (reply.succeeded()) {
                                            assertThat(((CustomMessage) reply.result().body()).getSummary()).isEqualTo("Message sent from cluster receiver!");
                                        } else {
                                            System.out.println("No reply from cluster receiver");
                                        }
                                    }));
                    vertx.deployVerticle(localReceiver, deployResult -> {
                        if (deployResult.succeeded()) {
                            vertx.setPeriodic(2000, _id -> senderEventBus
                                    .request("local-message-receiver",
                                            new CustomMessage(200, "a0000001", "Local message!"), reply -> {
                                                if (reply.succeeded()) {
                                                    assertThat(((CustomMessage) reply.result().body()).getSummary())
                                                            .isEqualTo("Message sent from local receiver!");
                                                } else {
                                                    System.out.println("No reply from local receiver");
                                                }
                                            }));
                        } else {
                            deployResult.cause().printStackTrace();
                        }
                    });
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            } else {
                res.cause().printStackTrace();
            }
        });
    }

    @Test
    void testEventBusBySSL(VertxTestContext testContext) {
        Vertx.clusteredVertx(new VertxOptions().setEventBusOptions(new EventBusOptions().setSsl(true)
                        .setKeyStoreOptions(new JksOptions().setPath("src/test/resources/eventbus/ssl/keystore.jks").setPassword("wibble"))
                        .setTrustStoreOptions(new JksOptions().setPath("src/test/resources/eventbus/ssl/keystore.jks").setPassword("wibble"))),
                deployResult -> {
                    if (deployResult.succeeded()) {
                        Vertx vertx = deployResult.result();
                        try {
                            vertx.eventBus().consumer("ping-address", message -> {
                                assertThat(message.body()).isEqualTo("ping!");
                                message.reply("pong!");
                            });
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    } else {
                        deployResult.cause().printStackTrace();
                    }
                });
        Vertx.clusteredVertx(new VertxOptions().setEventBusOptions(new EventBusOptions().setSsl(true)
                        .setKeyStoreOptions(new JksOptions().setPath("src/test/resources/eventbus/ssl/keystore.jks").setPassword("wibble"))
                        .setTrustStoreOptions(new JksOptions().setPath("src/test/resources/eventbus/ssl/keystore.jks").setPassword("wibble"))),
                deployResult -> {
                    if (deployResult.succeeded()) {
                        Vertx vertx = deployResult.result();
                        try {
                            vertx.setPeriodic(1000, v -> vertx.eventBus().request("ping-address", "ping!",
                                    ar -> {
                                        if (ar.succeeded()) {
                                            assertThat(ar.result().body()).isEqualTo("pong!");
                                            testContext.completeNow();
                                        } else {
                                            System.out.println("No reply");
                                        }
                                    }));
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    } else {
                        deployResult.cause().printStackTrace();
                    }
                });
    }

    @Test
    void testHighAvailability(VertxTestContext testContext) {
        Launcher.main(new String[]{"run", Server.class.getName(), "-ha"});
        Launcher.main(new String[]{"bare"});
        testContext.completeNow();
    }
}
