package com.lingh;

import com.lingh.eventbus.messagecodec.util.CustomMessage;
import com.lingh.eventbus.messagecodec.util.CustomMessageCodec;
import com.lingh.ha.Server;
import com.lingh.http.sharing.HttpServerVerticle;
import com.lingh.jsonstreaming.DataPoint;
import com.lingh.net.stream.Batch;
import com.lingh.net.stream.BatchStream;
import com.lingh.verticle.deploy.OtherVerticle;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.*;
import io.vertx.core.parsetools.JsonEventType;
import io.vertx.core.parsetools.JsonParser;
import io.vertx.core.streams.Pump;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(VertxExtension.class)
public class VertxCoreTest {
    @Test
    void testEmbedding(VertxTestContext testContext) throws Throwable {
        int firstPort = 8282;
        Vertx.vertx().createHttpServer().requestHandler(req -> req.response().end("Hello World!"))
                .listen(firstPort).onComplete(testContext.succeedingThenComplete());
        assertThat(testContext.awaitCompletion(5, TimeUnit.SECONDS)).isTrue();
        if (testContext.failed()) {
            throw testContext.causeOfFailure();
        }
    }

    @Test
    void testNetInEcho(VertxTestContext testContext) {
        int firstPort = 8283;
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        Vertx clientVertx = Vertx.vertx(new VertxOptions());
        serverVertx.createNetServer().connectHandler(sock -> Pump.pump(sock, sock).start()).listen(firstPort);
        clientVertx.createNetClient().connect(firstPort, "localhost", res -> {
            if (res.succeeded()) {
                NetSocket socket = res.result();
                socket.handler(buffer -> assertThat(buffer.toString(StandardCharsets.UTF_8)).startsWith("hello "));
                IntStream.range(0, 10).mapToObj(args -> "hello " + args + "\n").forEach(socket::write);
                testContext.completeNow();
            }
        });
    }

    @Test
    void testNetInEchoSSL(VertxTestContext testContext) {
        int firstPort = 8284;
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        Vertx clientVertx = Vertx.vertx(new VertxOptions());
        serverVertx.createNetServer(new NetServerOptions().setSsl(true)
                .setKeyStoreOptions(new JksOptions().setPath("src/test/resources/net/echossl/server-keystore.jks")
                        .setPassword("wibble"))).connectHandler(sock -> Pump.pump(sock, sock).start()).listen(firstPort);
        clientVertx.createNetClient(new NetClientOptions().setSsl(true)
                        .setTrustAll(true).setKeyStoreOptions(new JksOptions().setPath("src/test/resources/net/echossl/server-keystore.jks").setPassword("wibble")))
                .connect(firstPort, "localhost", res -> {
                    if (res.succeeded()) {
                        NetSocket socket = res.result();
                        socket.handler(buffer -> assertThat(buffer.toString(StandardCharsets.UTF_8)).startsWith("hello "));
                        IntStream.range(0, 10).mapToObj(args -> "hello " + args + "\n").forEach(socket::write);
                        testContext.completeNow();
                    }
                });

    }

    @Test
    void testHttpInSimple(VertxTestContext testContext) {
        int firstPort = 8285;
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        Vertx clientVertx = Vertx.vertx(new VertxOptions());
        serverVertx.createHttpServer().requestHandler(req -> req.response().putHeader("content-type", "text/html")
                .end("<html><body><h1>Hello from vert.x!</h1></body></html>")).listen(firstPort);
        clientVertx.createHttpClient().request(HttpMethod.GET, firstPort, "localhost", "/").compose(req -> req.send().compose(resp -> {
            assertThat(resp.statusCode()).isEqualTo(200);
            return resp.body();
        })).onSuccess(body -> {
            assertThat(body.toString(StandardCharsets.ISO_8859_1)).isEqualTo("<html><body><h1>Hello from vert.x!</h1></body></html>");
            testContext.completeNow();
        });
    }

    @Test
    void testHttpInHttps(VertxTestContext testContext) {
        int firstPort = 8286;
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        Vertx clientVertx = Vertx.vertx(new VertxOptions());
        serverVertx.createHttpServer(new HttpServerOptions().setSsl(true)
                .setKeyStoreOptions(new JksOptions().setPath("src/test/resources/http/https/server-keystore.jks")
                        .setPassword("wibble"))).requestHandler(req -> req.response().putHeader("content-type", "text/html")
                .end("<html><body><h1>Hello from vert.x!</h1></body></html>")).listen(firstPort);
        clientVertx.createHttpClient(new HttpClientOptions().setSsl(true).setTrustAll(true)
                .setKeyStoreOptions(new JksOptions().setPath("src/test/resources/http/https/server-keystore.jks").
                        setPassword("wibble"))).request(HttpMethod.GET, firstPort, "localhost", "/").compose(req -> req.send().compose(resp -> {
            assertThat(resp.statusCode()).isEqualTo(200);
            return resp.body();
        })).onSuccess(body -> {
            assertThat(body.toString(StandardCharsets.ISO_8859_1)).isEqualTo("<html><body><h1>Hello from vert.x!</h1></body></html>");
            testContext.completeNow();
        }).onFailure(Throwable::printStackTrace);
    }

    @Test
    void testHttpInProxy(VertxTestContext testContext) {
        int firstPort = 8287;
        int secondPort = 8288;
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        Vertx proxyVertx = Vertx.vertx(new VertxOptions());
        Vertx clientVertx = Vertx.vertx(new VertxOptions());
        serverVertx.createHttpServer().requestHandler(req -> {
            assertThat(req.uri()).isEqualTo("/");
            req.headers().names().forEach(name -> {
                switch (name) {
                    case "host":
                        assertThat(req.headers().get(name)).isEqualTo("localhost:" + secondPort);
                        break;
                    case "transfer-encoding":
                        assertThat(req.headers().get(name)).isEqualTo("chunked");
                        break;
                }
            });
            req.handler(data -> assertThat(data.toString(StandardCharsets.ISO_8859_1)).startsWith("client-chunk-"));
            req.endHandler(v -> {
                req.response().setChunked(true);
                IntStream.range(0, 10).forEach(i -> req.response().write("server-data-chunk-" + i));
                req.response().end();
            });
        }).listen(firstPort);
        proxyVertx.createHttpServer().requestHandler(serverRequest -> {
            assertThat(serverRequest.uri()).isEqualTo("/");
            serverRequest.pause();
            HttpServerResponse serverResponse = serverRequest.response();
            proxyVertx.createHttpClient(new HttpClientOptions()).request(serverRequest.method(), firstPort, "localhost", serverRequest.uri()).onSuccess(clientRequest -> {
                clientRequest.headers().setAll(serverRequest.headers());
                clientRequest.send(serverRequest).onSuccess(clientResponse -> {
                    assertThat(clientResponse.statusCode()).isEqualTo(200);
                    serverResponse.setStatusCode(clientResponse.statusCode());
                    serverResponse.headers().setAll(clientResponse.headers());
                    serverResponse.send(clientResponse);
                }).onFailure(err -> serverResponse.setStatusCode(500).end());
            }).onFailure(err -> serverResponse.setStatusCode(500).end());
        }).listen(secondPort);
        clientVertx.createHttpClient().request(HttpMethod.GET, secondPort, "localhost", "/").compose(request -> {
            request.setChunked(true);
            IntStream.range(0, 10).mapToObj(i -> "client-chunk-" + i).forEach(request::write);
            request.end();
            return request.response().compose(resp -> {
                assertThat(resp.statusCode()).isEqualTo(200);
                return resp.body();
            });
        }).onSuccess(body -> {
            assertThat(body.toString(StandardCharsets.ISO_8859_1))
                    .isEqualTo("server-data-chunk-0server-data-chunk-1server-data-chunk-2server-data-chunk-3server-data-chunk-4server-data-chunk-5server-data-chunk-6server-data-chunk-7server-data-chunk-8server-data-chunk-9");
            testContext.completeNow();
        });
    }

    @Test
    void testHttpInSendfile(VertxTestContext testContext) throws Throwable {
        int firstPort = 8289;
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        serverVertx.createHttpServer().requestHandler(req -> {
            String filename = null;
            if (req.path().equals("/")) {
                filename = "src/test/resources/http/sendfile/index.html";
            } else if (req.path().equals("/page1.html")) {
                filename = "src/test/resources/http/sendfile/page1.html";
            } else if (req.path().equals("/page2.html")) {
                filename = "src/test/resources/http/sendfile/page2.html";
            } else {
                req.response().setStatusCode(404).end();
            }
            if (filename != null) {
                req.response().sendFile(filename);
            }
        }).listen(firstPort).onComplete(testContext.succeedingThenComplete());
        assertThat(testContext.awaitCompletion(5, TimeUnit.SECONDS)).isTrue();
        if (testContext.failed()) {
            throw testContext.causeOfFailure();
        }
    }

    @Test
    void testHttpInSimpleForm(VertxTestContext testContext) throws Throwable {
        int firstPort = 8290;
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        serverVertx.createHttpServer().requestHandler(req -> {
            if (req.uri().equals("/")) {
                req.response().sendFile("src/test/resources/http/simpleform/index.html");
            } else if (req.uri().startsWith("/form")) {
                req.response().setChunked(true);
                req.setExpectMultipart(true);
                req.endHandler((v) -> {
                    req.formAttributes().names().forEach(attr -> req.response()
                            .write("Got attr " + attr + " : " + req.formAttributes().get(attr) + "\n"));
                    req.response().end();
                });
            } else {
                req.response().setStatusCode(404).end();
            }
        }).listen(firstPort).onComplete(testContext.succeedingThenComplete());
        assertThat(testContext.awaitCompletion(5, TimeUnit.SECONDS)).isTrue();
        if (testContext.failed()) {
            throw testContext.causeOfFailure();
        }
    }

    @Test
    void testHttpInSimpleFormFileUpload(VertxTestContext testContext) throws Throwable {
        int firstPort = 8291;
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        serverVertx.createHttpServer().requestHandler(req -> {
            if (req.uri().equals("/")) {
                req.response().sendFile("src/test/resources/http/simpleformupload/index.html");
            } else if (req.uri().startsWith("/form")) {
                req.setExpectMultipart(true);
                req.uploadHandler(upload -> upload.streamToFileSystem(upload.filename())
                        .onSuccess(v -> req.response().end("Successfully uploaded to " + upload.filename()))
                        .onFailure(err -> req.response().end("Upload failed")));
            } else {
                req.response().setStatusCode(404).end();
            }
        }).listen(firstPort).onComplete(testContext.succeedingThenComplete());
        assertThat(testContext.awaitCompletion(5, TimeUnit.SECONDS)).isTrue();
        if (testContext.failed()) {
            throw testContext.causeOfFailure();
        }
    }

    @Test
    void testHttpInHttpRequestBodyUpload(VertxTestContext testContext) {
        int firstPort = 8292;
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        Vertx clientVertx = Vertx.vertx(new VertxOptions());
        String uploadedFilename = UUID.randomUUID() + ".uploaded";
        serverVertx.createHttpServer().requestHandler(req -> {
            req.pause();
            serverVertx.fileSystem().open(uploadedFilename, new OpenOptions(), ares -> {
                AsyncFile file = ares.result();
                Pump pump = Pump.pump(req, file);
                req.endHandler(v1 -> file.close(v2 -> {
                    assertThat(uploadedFilename).endsWith(".uploaded");
                    req.response().end();
                }));
                pump.start();
                req.resume();
            });
        }).listen(firstPort);
        clientVertx.createHttpClient(new HttpClientOptions()).request(HttpMethod.PUT, firstPort, "localhost", "/someurl").compose(req -> {
            String filename = "src/test/resources/http/upload/upload.txt";
            FileSystem fs = clientVertx.fileSystem();
            return fs.props(filename).compose(props -> {
                assertThat(props.getClass().getName()).isEqualTo(io.vertx.core.file.impl.FilePropsImpl.class.getName());
                req.headers().set("content-length", String.valueOf(props.size()));
                return fs.open(filename, new OpenOptions());
            }).compose(file -> req.send(file).map(HttpClientResponse::statusCode));
        }).onSuccess(statusCode -> {
            assertThat(statusCode).isEqualTo(200);
            clientVertx.fileSystem().delete(uploadedFilename, ares -> testContext.completeNow());
        });
    }

    @Test
    void testHttpInHTTPServerSharing(VertxTestContext testContext) {
        int firstPort = 8293;
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        Vertx clientVertx = Vertx.vertx(new VertxOptions());
        assertDoesNotThrow(() -> serverVertx.deployVerticle(HttpServerVerticle.class.getName(), new DeploymentOptions().setInstances(2)));
        clientVertx.setPeriodic(1000, l -> {
            HttpClient client = clientVertx.createHttpClient();
            client.request(HttpMethod.GET, firstPort, "localhost", "/")
                    .compose(req -> req.send().compose(HttpClientResponse::body)).onSuccess(body -> {
                        assertThat(body.toString(StandardCharsets.ISO_8859_1)).startsWith("<html><body><h1>Hello from " + HttpServerVerticle.class.getName() + "@");
                        assertThat(body.toString(StandardCharsets.ISO_8859_1)).endsWith("</h1></body></html>");
                        testContext.completeNow();
                    });
        });
    }

    @Test
    @Timeout(value = 2, timeUnit = TimeUnit.MINUTES)
    void testHttpInWebSocketsEcho(VertxTestContext testContext) {
        int firstPort = 8294;
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        Vertx clientVertx = Vertx.vertx(new VertxOptions());
        serverVertx.createHttpServer().webSocketHandler(ws -> ws.handler(ws::writeBinaryMessage)).requestHandler(req -> {
            if ("/".equals(req.uri())) {
                req.response().sendFile("src/test/resources/http/websockets/ws.html");
            }
        }).listen(firstPort);
        HttpClient client = clientVertx.createHttpClient();
        client.webSocket(firstPort, "localhost", "/some-uri").onSuccess(webSocket -> {
            webSocket.handler(data -> {
                assertThat(data.toString(StandardCharsets.ISO_8859_1)).isEqualTo("Hello world");
                client.close();
            });
            webSocket.writeBinaryMessage(Buffer.buffer("Hello world"));
            testContext.completeNow();
        });
    }

    @Test
    void testHttp2InSimple(VertxTestContext testContext) {
        int firstPort = 8295;
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        Vertx clientVertx = Vertx.vertx(new VertxOptions());
        serverVertx.createHttpServer(new HttpServerOptions().setUseAlpn(true).setSsl(true)
                        .setPemKeyCertOptions(new PemKeyCertOptions().setKeyPath("src/test/resources/http2/simple/server-key.pem")
                                .setCertPath("src/test/resources/http2/simple/server-cert.pem"))).requestHandler(req -> req.response()
                        .putHeader("content-type", "text/html")
                        .end("<html><body>" + "<h1>Hello from vert.x!</h1>" + "<p>version = " + req.version() + "</p>" + "</body></html>"))
                .listen(firstPort);
        clientVertx.createHttpClient(new HttpClientOptions().setSsl(true).setUseAlpn(true)
                        .setProtocolVersion(HttpVersion.HTTP_2).setTrustAll(true).setPemKeyCertOptions(new PemKeyCertOptions()
                                .setKeyPath("src/test/resources/http2/simple/server-key.pem").setCertPath("src/test/resources/http2/simple/server-cert.pem")))
                .request(HttpMethod.GET, firstPort, "localhost", "/").compose(req -> req.send().compose(resp -> {
                    assertThat(resp.statusCode()).isEqualTo(200);
                    return resp.body();
                })).onSuccess(body -> {
                    assertThat(body.toString(StandardCharsets.ISO_8859_1))
                            .isEqualTo("<html><body><h1>Hello from vert.x!</h1><p>version = HTTP_2</p></body></html>");
                    testContext.completeNow();
                });
    }

    @Test
    void testHttp2InPush(VertxTestContext testContext) {
        int firstPort = 8296;
        Checkpoint responsesReceived = testContext.checkpoint(2);
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        Vertx clientVertx = Vertx.vertx(new VertxOptions());
        serverVertx.createHttpServer(new HttpServerOptions().setUseAlpn(true).setSsl(true)
                .setPemKeyCertOptions(new PemKeyCertOptions().setKeyPath("src/test/resources/http2/push/server-key.pem")
                        .setCertPath("src/test/resources/http2/push/server-cert.pem"))).requestHandler(req -> {
            String path = req.path();
            HttpServerResponse resp = req.response();
            if ("/".equals(path)) {
                resp.push(HttpMethod.GET, "/script.js", ar -> {
                    if (ar.succeeded()) {
                        ar.result().sendFile("src/test/resources/http2/push/script.js");
                    }
                });
                resp.sendFile("src/test/resources/http2/push/index.html");
            } else if ("/script.js".equals(path)) {
                resp.sendFile("src/test/resources/http2/push/script.js");
            } else {
                System.out.println("Not found " + path);
                resp.setStatusCode(404).end();
            }
        }).listen(firstPort, "localhost", ar -> {
            if (ar.succeeded()) {
                responsesReceived.flag();
            } else {
                ar.cause().printStackTrace();
            }
        });

        clientVertx.createHttpClient(new HttpClientOptions().setSsl(true).setUseAlpn(true)
                        .setProtocolVersion(HttpVersion.HTTP_2).setTrustAll(true)
                        .setKeyCertOptions(new PemKeyCertOptions().setKeyPath("src/test/resources/http2/push/server-key.pem")
                                .setCertPath("src/test/resources/http2/push/server-cert.pem"))).request(HttpMethod.GET, firstPort, "localhost", "/")
                .compose(request -> {
                    request.pushHandler(pushedReq -> pushedReq.response().compose(HttpClientResponse::body)
                            .onSuccess(body -> assertThat(body.toString(StandardCharsets.ISO_8859_1)).isEqualTo("alert(\"hello world\");")));
                    return request.send().compose(resp -> {
                        assertThat(resp.statusCode()).isEqualTo(200);
                        assertThat(resp.version()).isEqualTo(HttpVersion.HTTP_2);
                        return resp.body();
                    });
                }).onSuccess(body -> {
                    assertThat(body.toString(StandardCharsets.ISO_8859_1))
                            .isEqualTo("<!DOCTYPE html>\n" + "<html lang=\"en\">\n" + "<head>\n" +
                                       "    <meta charset=\"UTF-8\">\n" + "    <title>Hello world</title>\n" +
                                       "    <script src=\"script.js\"></script>\n" + "</head>\n" + "<body>\n" +
                                       "<h1>Hello World</h1>\n" + "\n" + "</body>\n" + "</html>");
                    responsesReceived.flag();
                });
    }

    @Test
    void testHttp2InH2C(VertxTestContext testContext) {
        int firstPort = 8297;
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        Vertx clientVertx = Vertx.vertx(new VertxOptions());
        serverVertx.createHttpServer(new HttpServerOptions()).requestHandler(req -> req.response()
                        .putHeader("content-type", "text/html")
                        .end("<html><body>" + "<h1>Hello from vert.x!</h1>" + "<p>version = " + req.version() + "</p>" + "</body></html>"))
                .listen(firstPort);
        clientVertx.createHttpClient(new HttpClientOptions().setProtocolVersion(HttpVersion.HTTP_2))
                .request(HttpMethod.GET, firstPort, "localhost", "/").compose(req -> req.send().compose(resp -> {
                    assertThat(resp.statusCode()).isEqualTo(200);
                    return resp.body();
                })).onSuccess(body -> {
                    assertThat(body.toString(StandardCharsets.ISO_8859_1))
                            .isEqualTo("<html><body><h1>Hello from vert.x!</h1><p>version = HTTP_2</p></body></html>");
                    testContext.completeNow();
                });
    }

    @Test
    void testHttp2InCustomFrames(VertxTestContext testContext) {
        int firstPort = 8298;
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        Vertx clientVertx = Vertx.vertx(new VertxOptions());
        serverVertx.createHttpServer(new HttpServerOptions().setUseAlpn(true).setSsl(true)
                .setPemKeyCertOptions(new PemKeyCertOptions().setKeyPath("src/test/resources/http2/customframes/server-key.pem")
                        .setCertPath("src/test/resources/http2/customframes/server-cert.pem"))).requestHandler(req -> {
            HttpServerResponse resp = req.response();
            req.customFrameHandler(frame -> {
                assertThat(frame.payload().toString(StandardCharsets.UTF_8)).isEqualTo("ping");
                resp.writeCustomFrame(10, 0, Buffer.buffer("pong"));
            });
        }).listen(firstPort);
        clientVertx.createHttpClient(new HttpClientOptions().setSsl(true).setUseAlpn(true)
                        .setProtocolVersion(HttpVersion.HTTP_2).setTrustAll(true).setPemKeyCertOptions(new PemKeyCertOptions()
                                .setKeyPath("src/test/resources/http2/customframes/server-key.pem").setCertPath("src/test/resources/http2/customframes/server-cert.pem")))
                .request(HttpMethod.GET, firstPort, "localhost", "/").onSuccess(request -> {
                    request.response().onSuccess(resp -> resp.customFrameHandler(frame ->
                            assertThat(frame.payload().toString(StandardCharsets.UTF_8))
                                    .isEqualTo("pong")));
                    request.sendHead().onSuccess(v -> clientVertx.setPeriodic(1000, timerID -> {
                        request.writeCustomFrame(10, 0, Buffer.buffer("ping"));
                        testContext.completeNow();
                    }));
                });
    }

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
    void testFuture(VertxTestContext testContext) {
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        Promise<String> promise = Promise.promise();
        serverVertx.setTimer(100, l -> promise.complete("world"));
        promise.future().compose(name -> {
            Promise<String> anotherAsyncActionPromise = Promise.promise();
            serverVertx.setTimer(100, l -> anotherAsyncActionPromise.complete("hello " + name));
            return anotherAsyncActionPromise.future();
        }).onComplete(ar -> {
            assertThat(ar.failed()).isFalse();
            assertThat(ar.result()).isEqualTo("hello world");
            testContext.completeNow();
        });
    }

    @Test
    void testVerticleInDeploy(VertxTestContext testContext) {
        Checkpoint requestsServed = testContext.checkpoint(1);
        Checkpoint responsesReceived = testContext.checkpoint(1);
        Verticle otherVerticle = new AbstractVerticle() {
            @Override
            public void start() {
                assertThat(config()).isEqualTo(new JsonObject());
            }

            @Override
            public void stop() {
                requestsServed.flag();
            }
        };
        Vertx firstVertx = Vertx.vertx(new VertxOptions());
        firstVertx.deployVerticle(otherVerticle);
        firstVertx.deployVerticle(otherVerticle, deployResult -> {
            if (deployResult.succeeded()) {
                String deploymentID = deployResult.result();
                assertThat(deploymentID).isNotBlank();
                firstVertx.undeploy(deploymentID, res2 -> {
                    if (res2.succeeded()) {
                        responsesReceived.flag();
                    } else {
                        res2.cause().printStackTrace();
                    }
                });
            } else {
                deployResult.cause().printStackTrace();
            }
        });
        firstVertx.deployVerticle(otherVerticle, new DeploymentOptions().setConfig(new JsonObject().put("foo", "bar")));
        firstVertx.deployVerticle(OtherVerticle.class.getName(), new DeploymentOptions().setInstances(10));
        firstVertx.deployVerticle(otherVerticle, new DeploymentOptions().setWorker(true));
    }

    @Test
    void testVerticleInAsynchronousDeployment(VertxTestContext testContext) {
        Checkpoint requestsServed = testContext.checkpoint(1);
        Checkpoint responsesReceived = testContext.checkpoint(2);
        Verticle otherVerticle = new AbstractVerticle() {
            @Override
            public void start(Promise<Void> startPromise) {
                vertx.setTimer(2000, tid -> {
                    requestsServed.flag();
                    startPromise.complete();
                });
            }

            @Override
            public void stop(Promise<Void> stopPromise) {
                vertx.setTimer(2000, tid -> {
                    responsesReceived.flag();
                    stopPromise.complete();
                });
            }
        };
        Vertx firstVertx = Vertx.vertx(new VertxOptions());
        firstVertx.deployVerticle(otherVerticle, deployResult -> {
            if (deployResult.succeeded()) {
                String deploymentID = deployResult.result();
                assertThat(deploymentID).isNotBlank();
                firstVertx.undeploy(deploymentID, res2 -> {
                    if (res2.succeeded()) {
                        responsesReceived.flag();
                    } else {
                        res2.cause().printStackTrace();
                    }
                });
            } else {
                deployResult.cause().printStackTrace();
            }
        });
    }

    @Test
    void testVerticleInWorkerVerticle(VertxTestContext testContext) {
        Verticle workerVerticle = new AbstractVerticle() {
            @Override
            public void start() {
                assertThat(Thread.currentThread().getName()).isEqualTo("vert.x-worker-thread-0");
                vertx.eventBus().<String>consumer("sample.data", message -> {
                    assertThat(Thread.currentThread().getName()).isEqualTo("vert.x-worker-thread-1");
                    String body = message.body();
                    message.reply(body.toUpperCase());
                });
            }
        };
        Vertx firstVertx = Vertx.vertx(new VertxOptions());
        assertThat(Thread.currentThread().getName()).isEqualTo("Test worker");
        firstVertx.deployVerticle(workerVerticle, new DeploymentOptions().setWorker(true), deployResult -> {
            if (deployResult.succeeded()) {
                firstVertx.eventBus().request("sample.data", "hello vert.x", r -> {
                    assertThat(r.result().body()).isEqualTo("HELLO VERT.X");
                    assertThat(Thread.currentThread().getName()).isEqualTo("vert.x-eventloop-thread-0");
                    testContext.completeNow();
                });
            }
        });
    }

    @Test
    void testExecuteBlocking(VertxTestContext testContext) {
        Checkpoint serverStarted = testContext.checkpoint(2);
        Verticle execBlockingExample = new AbstractVerticle() {
            @Override
            public void start() {
                vertx.createHttpServer().requestHandler(request -> vertx.<String>executeBlocking(promise -> {
                    try {
                        Thread.sleep(500);
                    } catch (Exception ignore) {
                    }
                    promise.complete("armadillos!");
                }, res -> {
                    if (res.succeeded()) {
                        request.response().putHeader("content-type", "text/plain").end(res.result());
                    } else {
                        res.cause().printStackTrace();
                    }
                })).listen(8080);

            }
        };
        Vertx firstVertx = Vertx.vertx(new VertxOptions());
        firstVertx.deployVerticle(execBlockingExample).onSuccess(h -> serverStarted.flag());
        Vertx secondVertx = Vertx.vertx(new VertxOptions());
        secondVertx.deployVerticle(execBlockingExample, new DeploymentOptions().setWorkerPoolName("dedicated-pool")
                .setMaxWorkerExecuteTime(120000).setWorkerPoolSize(5)).onSuccess(h -> serverStarted.flag());
    }

    @Test
    void testHighAvailability(VertxTestContext testContext) {
        Launcher.main(new String[]{"run", Server.class.getName(), "-ha"});
        Launcher.main(new String[]{"bare"});
        testContext.completeNow();
    }

    @Test
    void testJSONStreamingParser(VertxTestContext testContext) {
        Vertx firstVertx = Vertx.vertx(new VertxOptions());
        firstVertx.fileSystem().open("src/test/resources/large.json", new OpenOptions(), ar -> {
            if (ar.succeeded()) {
                AsyncFile asyncFile = ar.result();
                AtomicInteger counter = new AtomicInteger();
                JsonParser.newParser(asyncFile).objectValueMode().exceptionHandler(t -> {
                    t.printStackTrace();
                    asyncFile.close();
                }).endHandler(v -> {
                    asyncFile.close();
                    testContext.completeNow();
                }).handler(event -> {
                    if (event.type() == JsonEventType.VALUE && counter.incrementAndGet() % 100 == 0) {
                        assertThat(event.mapTo(DataPoint.class)).isNotNull();
                    }
                });
            } else {
                ar.cause().printStackTrace();
            }
        });

    }

    @Test
    @Timeout(value = 30, timeUnit = TimeUnit.SECONDS)
    void testCustomReadStreamAndWriteStreamImplementation(VertxTestContext testContext) {
        int firstPort = 8299;
        Checkpoint requestsServed = testContext.checkpoint(3);
        Checkpoint responsesReceived = testContext.checkpoint(3);
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        Vertx clientVertx = Vertx.vertx(new VertxOptions());
        serverVertx.createNetServer().connectHandler(socket -> {
            BatchStream batchStream = new BatchStream(socket, socket);
            batchStream.pause();
            batchStream.handler(batch -> {
                assertThat(batch.getRaw().toString()).isNotBlank();
                batchStream.write(batch);
                if (batchStream.writeQueueFull()) {
                    batchStream.pause();
                    batchStream.drainHandler(done -> batchStream.resume());
                }
                requestsServed.flag();
            }).endHandler(v -> batchStream.end()).exceptionHandler(t -> {
                t.printStackTrace();
                batchStream.end();
            });
            batchStream.resume();
        }).listen(firstPort);
        clientVertx.createNetClient().connect(firstPort, "localhost", ar -> {
            if (ar.succeeded()) {
                NetSocket socket = ar.result();
                BatchStream batchStream = new BatchStream(socket, socket);
                batchStream.pause();
                batchStream.handler(batch -> {
                    assertThat(batch.getRaw().toString()).isNotBlank();
                    responsesReceived.flag();
                }).endHandler(v -> batchStream.end()).exceptionHandler(t -> {
                    t.printStackTrace();
                    batchStream.end();
                });
                batchStream.resume();
                JsonObject jsonObject = new JsonObject().put("id", UUID.randomUUID().toString())
                        .put("name", "Vert.x")
                        .put("timestamp", Instant.now());
                JsonArray jsonArray = new JsonArray().add(UUID.randomUUID().toString()).add("Vert.x").add(Instant.now());
                Buffer buffer = Buffer.buffer("Vert.x is awesome!");
                batchStream.write(new Batch(jsonObject));
                batchStream.write(new Batch(jsonArray));
                batchStream.write(new Batch(buffer));
            }
        });
    }
}
