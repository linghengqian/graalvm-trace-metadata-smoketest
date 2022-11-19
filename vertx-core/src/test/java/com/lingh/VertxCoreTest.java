package com.lingh;

import com.lingh.eventbus.messagecodec.util.CustomMessage;
import com.lingh.eventbus.messagecodec.util.CustomMessageCodec;
import com.lingh.jsonstreaming.DataPoint;
import com.lingh.net.stream.Batch;
import com.lingh.net.stream.BatchStream;
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
import org.junit.jupiter.api.Disabled;
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
        Vertx.vertx().createHttpServer().requestHandler(req -> req.response().end("Hello World!")).listen(8080)
                .onComplete(testContext.succeedingThenComplete());
        assertThat(testContext.awaitCompletion(5, TimeUnit.SECONDS)).isTrue();
        if (testContext.failed()) {
            throw testContext.causeOfFailure();
        }
    }

    @Test
    @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
    void testNetInEcho(VertxTestContext testContext) {
        int firstPort = 1234;
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        Vertx clientVertx = Vertx.vertx(new VertxOptions());
        serverVertx.createNetServer()
                .connectHandler(sock -> Pump.pump(sock, sock).start())
                .listen(firstPort);
        clientVertx.createNetClient()
                .connect(firstPort, "localhost", res -> {
                    assertThat(res.succeeded()).isTrue();
                    NetSocket socket = res.result();
                    socket.handler(buffer -> assertThat(buffer.toString(StandardCharsets.UTF_8)).isEqualTo("hello 0"));
                    IntStream.range(0, 10).mapToObj("hello %d\n"::formatted).forEach(socket::write);
                    testContext.completeNow();
                });
    }

    @Test
    @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
    void testNetInEchoSSL(VertxTestContext testContext) {
        int firstPort = 1234;
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        Vertx clientVertx = Vertx.vertx(new VertxOptions());
        serverVertx.createNetServer(
                new NetServerOptions().setSsl(true)
                        .setKeyStoreOptions(new JksOptions().setPath("src/test/java/com/lingh/net/echossl/server-keystore.jks").setPassword("wibble"))
        ).connectHandler(sock -> Pump.pump(sock, sock).start()).listen(firstPort);
        clientVertx.createNetClient(new NetClientOptions().setSsl(true).setTrustAll(true))
                .connect(firstPort, "localhost", res -> {
                    assertThat(res.succeeded()).isTrue();
                    NetSocket socket = res.result();
                    socket.handler(buffer -> assertThat(buffer.toString(StandardCharsets.UTF_8)).isEqualTo("hello 0"));
                    IntStream.range(0, 10).mapToObj("hello %d\n"::formatted).forEach(socket::write);
                    testContext.completeNow();
                });

    }

    @Test
    @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
    void testHttpInSimple(VertxTestContext testContext) {
        int firstPort = 8080;
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        Vertx clientVertx = Vertx.vertx(new VertxOptions());
        serverVertx.createHttpServer()
                .requestHandler(req -> req.response()
                        .putHeader("content-type", "text/html")
                        .end("<html><body><h1>Hello from vert.x!</h1></body></html>"))
                .listen(firstPort);
        clientVertx.createHttpClient()
                .request(HttpMethod.GET, firstPort, "localhost", "/")
                .compose(req -> req.send()
                        .compose(resp -> {
                            assertThat(resp.statusCode()).isEqualTo(200);
                            return resp.body();
                        })).onSuccess(body -> {
                    assertThat(body.toString(StandardCharsets.ISO_8859_1)).isEqualTo("<html><body><h1>Hello from vert.x!</h1></body></html>");
                    testContext.completeNow();
                }).onFailure(Throwable::printStackTrace);
    }

    @Test
    @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
    void testHttpInHttps(VertxTestContext testContext) {
        int firstPort = 4443;
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        Vertx clientVertx = Vertx.vertx(new VertxOptions());
        serverVertx.createHttpServer(new HttpServerOptions().setSsl(true).setKeyStoreOptions(
                        new JksOptions().setPath("src/test/java/com/lingh/http/https/server-keystore.jks").setPassword("wibble")
                ))
                .requestHandler(req -> req.response()
                        .putHeader("content-type", "text/html")
                        .end("<html><body><h1>Hello from vert.x!</h1></body></html>"))
                .listen(firstPort);
        clientVertx.createHttpClient(new HttpClientOptions().setSsl(true).setTrustAll(true))
                .request(HttpMethod.GET, firstPort, "localhost", "/")
                .compose(req -> req.send()
                        .compose(resp -> {
                            assertThat(resp.statusCode()).isEqualTo(200);
                            return resp.body();
                        }))
                .onSuccess(body -> {
                    assertThat(body.toString(StandardCharsets.ISO_8859_1)).isEqualTo("<html><body><h1>Hello from vert.x!</h1></body></html>");
                    testContext.completeNow();
                }).onFailure(Throwable::printStackTrace);
    }

    @Test
    @Disabled
    @Timeout(value = 20, timeUnit = TimeUnit.SECONDS)
    void testHttpInProxyConnect(VertxTestContext testContext) { // todo too many timeout and fail
//        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.http.proxyconnect.Server.class, new VertxOptions(), null, false);
//        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.http.proxyconnect.Proxy.class, new VertxOptions(), null, false);
//        // TODO It seems that no matter what, only using vertx-core for proxy connect, the client will throw strange exception information.
//        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.http.proxyconnect.Client.class, new VertxOptions(), null, false);

        int firstPort = 8282;
        int secondPort = 8080;
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        Vertx proxyVertx = Vertx.vertx(new VertxOptions());
        Vertx clientVertx = Vertx.vertx(new VertxOptions());
        SelfSignedCertificate certificate = SelfSignedCertificate.create();
        HttpServerOptions serverOptions = new HttpServerOptions()
                .setSsl(true)
                .setKeyCertOptions(certificate.keyCertOptions());
        serverVertx.createHttpServer(serverOptions).requestHandler(req -> {
            System.out.println("Got request " + req.uri());
            for (String name : req.headers().names()) {
                System.out.println(name + ": " + req.headers().get(name));
            }
            req.handler(data -> System.out.println("Got data " + data.toString("ISO-8859-1")));
            req.endHandler(v -> {
                req.response().setChunked(true);
                for (int i = 0; i < 10; i++) {
                    req.response().write("server-data-chunk-" + i);
                }
                req.response().end();
            });
        }).listen(firstPort);

        proxyVertx.createHttpServer().requestHandler(req -> {
            if (req.method() == HttpMethod.CONNECT) {
                String proxyAddress = req.uri();
                int idx = proxyAddress.indexOf(':');
                String host = proxyAddress.substring(0, idx);
                int port = Integer.parseInt(proxyAddress.substring(idx + 1));
                System.out.println("Connecting to proxy " + proxyAddress);
                proxyVertx.createNetClient(new NetClientOptions()).connect(port, host, ar -> {
                    if (ar.succeeded()) {
                        System.out.println("Connected to proxy");
                        NetSocket serverSocket = ar.result();
                        serverSocket.pause();
                        req.toNetSocket().onComplete(ar2 -> {
                            if (ar2.succeeded()) {
                                NetSocket clientSocket = ar2.result();
                                serverSocket.handler(buff -> {
                                    System.out.println("Forwarding server packet to the client");
                                    clientSocket.write(buff);
                                });
                                serverSocket.closeHandler(v -> {
                                    System.out.println("Server socket closed");
                                    clientSocket.close();
                                });
                                clientSocket.handler(buff -> {
                                    System.out.println("Forwarding client packet to the server");
                                    serverSocket.write(buff);
                                });
                                clientSocket.closeHandler(v -> {
                                    System.out.println("Client socket closed");
                                    serverSocket.close();
                                });
                                serverSocket.resume();
                            } else {
                                serverSocket.close();
                            }
                        });
                    } else {
                        System.out.println("Fail proxy connection");
                        req.response().setStatusCode(403).end();
                    }
                });
            } else {
                req.response().setStatusCode(405).end();
            }
        }).listen(secondPort);

        clientVertx.createHttpClient(new HttpClientOptions()
                        .setSsl(true)
                        .setTrustAll(true)
                        .setVerifyHost(false)
                        .setProxyOptions(new ProxyOptions().setType(ProxyType.HTTP).setHost("localhost").setPort(secondPort))
                ).request(HttpMethod.GET, secondPort, "localhost", "/")
                .compose(request -> {
                            request.setChunked(true);
                            for (int i = 0; i < 10; i++) {
                                request.write("client-chunk-" + i);
                            }
                            request.end();
                            return request.response().compose(resp -> {
                                System.out.println("Got response " + resp.statusCode());
                                return resp.body();
                            });
                        }
                )
                .onSuccess(body -> {
                    System.out.println("Got data " + body.toString("ISO-8859-1"));
                    testContext.completeNow();
                })
                .onFailure(Throwable::printStackTrace);
    }

    @Test
    @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
    void testHttpInProxy(VertxTestContext testContext) {
        int firstPort = 8282;
        int secondPort = 8080;
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        Vertx proxyVertx = Vertx.vertx(new VertxOptions());
        Vertx clientVertx = Vertx.vertx(new VertxOptions());
        serverVertx.createHttpServer()
                .requestHandler(req -> {
                    assertThat(req.uri()).isEqualTo("/");
                    req.headers().names().forEach(name -> {
                        switch (name) {
                            case "host" -> assertThat(req.headers().get(name)).isEqualTo("localhost:" + secondPort);
                            case "transfer-encoding" -> assertThat(req.headers().get(name)).isEqualTo("chunked");
                        }
                    });
                    req.handler(data -> assertThat(data.toString(StandardCharsets.ISO_8859_1).startsWith("client-chunk-")).isTrue());
                    req.endHandler(v -> {
                        req.response().setChunked(true);
                        IntStream.range(0, 10).forEach(i -> req.response().write("server-data-chunk-%d".formatted(i)));
                        req.response().end();
                    });
                }).listen(firstPort);
        proxyVertx.createHttpServer()
                .requestHandler(serverRequest -> {
                    assertThat(serverRequest.uri()).isEqualTo("/");
                    serverRequest.pause();
                    HttpServerResponse serverResponse = serverRequest.response();
                    proxyVertx.createHttpClient(new HttpClientOptions())
                            .request(serverRequest.method(), firstPort, "localhost", serverRequest.uri())
                            .onSuccess(clientRequest -> {
                                clientRequest.headers().setAll(serverRequest.headers());
                                clientRequest.send(serverRequest).onSuccess(clientResponse -> {
                                    assertThat(clientResponse.statusCode()).isEqualTo(200);
                                    serverResponse.setStatusCode(clientResponse.statusCode());
                                    serverResponse.headers().setAll(clientResponse.headers());
                                    serverResponse.send(clientResponse);
                                }).onFailure(err -> serverResponse.setStatusCode(500).end());
                            }).onFailure(err -> serverResponse.setStatusCode(500).end());
                }).listen(secondPort);
        clientVertx.createHttpClient()
                .request(HttpMethod.GET, secondPort, "localhost", "/")
                .compose(request -> {
                            request.setChunked(true);
                            IntStream.range(0, 10).mapToObj("client-chunk-%d"::formatted).forEach(request::write);
                            request.end();
                            return request.response().compose(resp -> {
                                assertThat(resp.statusCode()).isEqualTo(200);
                                return resp.body();
                            });
                        }
                )
                .onSuccess(body -> {
                    assertThat(body.toString(StandardCharsets.ISO_8859_1))
                            .isEqualTo("server-data-chunk-0server-data-chunk-1server-data-chunk-2server-data-chunk-3server-data-chunk-4server-data-chunk-5server-data-chunk-6server-data-chunk-7server-data-chunk-8server-data-chunk-9");
                    testContext.completeNow();
                }).onFailure(Throwable::printStackTrace);
    }

    @Test
    void testHttpInSendfile(VertxTestContext testContext) throws Throwable {
        int firstPort = 8080;
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        serverVertx.createHttpServer().requestHandler(req -> {
                    String filename = null;
                    switch (req.path()) {
                        case "/" -> filename = "src/test/java/com/lingh/http/sendfile/index.html";
                        case "/page1.html" -> filename = "src/test/java/com/lingh/http/sendfile/page1.html";
                        case "/page2.html" -> filename = "src/test/java/com/lingh/http/sendfile/page2.html";
                        default -> req.response().setStatusCode(404).end();
                    }
                    if (filename != null) {
                        req.response().sendFile(filename);
                    }
                }).listen(firstPort)
                .onComplete(testContext.succeedingThenComplete());
        assertThat(testContext.awaitCompletion(5, TimeUnit.SECONDS)).isTrue();
        if (testContext.failed()) {
            throw testContext.causeOfFailure();
        }
    }

    @Test
    void testHttpInSimpleForm(VertxTestContext testContext) throws Throwable {
        int firstPort = 8080;
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        serverVertx.createHttpServer().requestHandler(req -> {
                    if (req.uri().equals("/")) {
                        req.response().sendFile("src/test/java/com/lingh/http/simpleform/index.html");
                    } else if (req.uri().startsWith("/form")) {
                        req.response().setChunked(true);
                        req.setExpectMultipart(true);
                        req.endHandler((v) -> {
                            req.formAttributes().names()
                                    .forEach(attr -> req.response().write("Got attr %s : %s\n".formatted(attr, req.formAttributes().get(attr))));
                            req.response().end();
                        });
                    } else {
                        req.response().setStatusCode(404).end();
                    }
                }).listen(firstPort)
                .onComplete(testContext.succeedingThenComplete());
        assertThat(testContext.awaitCompletion(5, TimeUnit.SECONDS)).isTrue();
        if (testContext.failed()) {
            throw testContext.causeOfFailure();
        }
    }

    @Test
    void testHttpInSimpleFormFileUpload(VertxTestContext testContext) throws Throwable {
        int firstPort = 8080;
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        serverVertx.createHttpServer().requestHandler(req -> {
                    if (req.uri().equals("/")) {
                        req.response().sendFile("src/test/java/com/lingh/http/simpleformupload/index.html");
                    } else if (req.uri().startsWith("/form")) {
                        req.setExpectMultipart(true);
                        req.uploadHandler(upload -> upload.streamToFileSystem(upload.filename())
                                .onSuccess(v -> req.response().end("Successfully uploaded to %s".formatted(upload.filename())))
                                .onFailure(err -> req.response().end("Upload failed")));
                    } else {
                        req.response().setStatusCode(404).end();
                    }
                })
                .listen(firstPort)
                .onComplete(testContext.succeedingThenComplete());
        assertThat(testContext.awaitCompletion(5, TimeUnit.SECONDS)).isTrue();
        if (testContext.failed()) {
            throw testContext.causeOfFailure();
        }
    }

    @Test
    @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
    void testHttpInHttpRequestBodyUpload(VertxTestContext testContext) {
        int firstPort = 8080;
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        Vertx clientVertx = Vertx.vertx(new VertxOptions());
        serverVertx.createHttpServer().requestHandler(req -> {
            req.pause();
            String filename = "%s.uploaded".formatted(UUID.randomUUID());
            serverVertx.fileSystem().open(filename, new OpenOptions(), ares -> {
                AsyncFile file = ares.result();
                Pump pump = Pump.pump(req, file);
                req.endHandler(v1 -> file.close(v2 -> {
                    assertThat(filename.endsWith(".uploaded")).isTrue();
                    req.response().end();
                }));
                pump.start();
                req.resume();
            });
        }).listen(firstPort);
        clientVertx.createHttpClient(new HttpClientOptions())
                .request(HttpMethod.PUT, firstPort, "localhost", "/someurl")
                .compose(req -> {
                    String filename = "src/test/java/com/lingh/http/upload/upload.txt";
                    FileSystem fs = clientVertx.fileSystem();
                    return fs.props(filename).compose(props -> {
                        assertThat(props.getClass().getName()).isEqualTo(io.vertx.core.file.impl.FilePropsImpl.class.getName());
                        req.headers().set("content-length", "%d".formatted(props.size()));
                        return fs.open(filename, new OpenOptions());
                    }).compose(file -> req.send(file)
                            .map(HttpClientResponse::statusCode));
                })
                .onSuccess(statusCode -> {
                    assertThat(statusCode).isEqualTo(200);
                    testContext.completeNow();
                }).onFailure(Throwable::printStackTrace);
    }

    @Test
    @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
    void testHttpInHTTPServerSharing(VertxTestContext testContext) {
        int firstPort = 8080;
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        Vertx clientVertx = Vertx.vertx(new VertxOptions());
        assertDoesNotThrow(() -> serverVertx.deployVerticle(
                com.lingh.http.sharing.HttpServerVerticle.class.getName(),
                new DeploymentOptions().setInstances(2)));
        clientVertx.setPeriodic(1000, l -> {
            HttpClient client = clientVertx.createHttpClient();
            client.request(HttpMethod.GET, firstPort, "localhost", "/")
                    .compose(req -> req.send().compose(HttpClientResponse::body))
                    .onSuccess(body -> {
                        assertThat(body.toString(StandardCharsets.ISO_8859_1).startsWith("<html><body><h1>Hello from %s@"
                                .formatted(com.lingh.http.sharing.HttpServerVerticle.class.getName())))
                                .isTrue();
                        assertThat(body.toString(StandardCharsets.ISO_8859_1).endsWith("</h1></body></html>")).isTrue();
                        testContext.completeNow();
                    })
                    .onFailure(Throwable::printStackTrace);
        });
    }

    @Test
    @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
    void testHttpInWebSocketsEcho(VertxTestContext testContext) {
        int firstPort = 8080;
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        Vertx clientVertx = Vertx.vertx(new VertxOptions());
        serverVertx.createHttpServer()
                .webSocketHandler(ws -> ws.handler(ws::writeBinaryMessage))
                .requestHandler(req -> {
                    if ("/".equals(req.uri())) {
                        req.response().sendFile("src/test/java/com/lingh/http/websockets/ws.html");
                    }
                })
                .listen(firstPort);
        HttpClient client = clientVertx.createHttpClient();
        client.webSocket(firstPort, "localhost", "/some-uri")
                .onSuccess(webSocket -> {
                    webSocket.handler(data -> {
                        assertThat(data.toString(StandardCharsets.ISO_8859_1)).isEqualTo("Hello world");
                        client.close();
                    });
                    webSocket.writeBinaryMessage(Buffer.buffer("Hello world"));
                    testContext.completeNow();
                }).onFailure(Throwable::printStackTrace);
    }

    @Test
    @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
    @Disabled
    void testHttp2InSimple(VertxTestContext testContext) { // TODO fail
//        Runner.runExample(com.lingh.http2.simple.Server.class, null);

        int firstPort = 8443;
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        Vertx clientVertx = Vertx.vertx(new VertxOptions());

        serverVertx.createHttpServer(new HttpServerOptions().setUseAlpn(true).setSsl(true).
                setPemKeyCertOptions(new PemKeyCertOptions().setKeyPath("src/test/java/com/lingh/http2/simple/server-key.pem")
                        .setCertPath("src/test/java/com/lingh/http2/simple/server-cert.pem")
                )).requestHandler(req -> req.response().putHeader("content-type", "text/html")
                .end("<html><body><h1>Hello from vert.x!</h1><p>version = %s</p></body></html>".formatted(req.version()))
        ).listen(firstPort);
        clientVertx.createHttpClient(new HttpClientOptions().setSsl(true).setUseAlpn(true).setProtocolVersion(HttpVersion.HTTP_2).setTrustAll(true))
                .request(HttpMethod.GET, 8080, "localhost", "/")
                .compose(req -> req.send()
                        .compose(resp -> {
                            System.out.println("Got response " + resp.statusCode());
                            return resp.body();
                        }))
                .onSuccess(body -> {
                    System.out.println("Got data " + body.toString("ISO-8859-1"));
                    testContext.completeNow();
                }).onFailure(Throwable::printStackTrace);
    }

    @Test
    void testHttp2InPush(VertxTestContext testContext) {  // todo
        Runner.runExample(com.lingh.http2.push.Server.class, null);
        testContext.completeNow();
    }

    @Test
    @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
    void testHttp2InH2C(VertxTestContext testContext) {
        int firstPort = 8080;
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        Vertx clientVertx = Vertx.vertx(new VertxOptions());
        serverVertx.createHttpServer(new HttpServerOptions())
                .requestHandler(req -> req.response()
                        .putHeader("content-type", "text/html")
                        .end("<html><body><h1>Hello from vert.x!</h1><p>version = %s</p></body></html>".formatted(req.version())))
                .listen(firstPort);
        clientVertx.createHttpClient(new HttpClientOptions().setProtocolVersion(HttpVersion.HTTP_2))
                .request(HttpMethod.GET, firstPort, "localhost", "/")
                .compose(req -> req.send()
                        .compose(resp -> {
                            assertThat(resp.statusCode()).isEqualTo(200);
                            return resp.body();
                        })).onSuccess(body -> {
                    assertThat(body.toString(StandardCharsets.ISO_8859_1))
                            .isEqualTo("<html><body><h1>Hello from vert.x!</h1><p>version = HTTP_2</p></body></html>");
                    testContext.completeNow();
                }).onFailure(Throwable::printStackTrace);
    }
    
    @Test
    @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
    void testHttp2InCustomFrames(VertxTestContext testContext) {
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        Vertx clientVertx = Vertx.vertx(new VertxOptions());
        serverVertx.createHttpServer(new HttpServerOptions().
                setUseAlpn(true).
                setSsl(true).
                setPemKeyCertOptions(new PemKeyCertOptions().setKeyPath("src/test/java/com/lingh/http2/customframes/server-key.pem")
                        .setCertPath("src/test/java/com/lingh/http2/customframes/server-cert.pem")
                )).requestHandler(req -> {
            HttpServerResponse resp = req.response();
            req.customFrameHandler(frame -> {
                System.out.printf("Received client frame %s%n", frame.payload().toString(StandardCharsets.UTF_8));
                resp.writeCustomFrame(10, 0, Buffer.buffer("pong"));
            });
        }).listen(8443);
        clientVertx.createHttpClient(new HttpClientOptions().
                        setSsl(true).
                        setUseAlpn(true).
                        setProtocolVersion(HttpVersion.HTTP_2).setTrustAll(true)
                        .setPemKeyCertOptions(new PemKeyCertOptions().setKeyPath("src/test/java/com/lingh/http2/customframes/server-key.pem")
                                .setCertPath("src/test/java/com/lingh/http2/customframes/server-cert.pem")))    // todo fix master branch
                .request(HttpMethod.GET, 8443, "localhost", "/")
                .onSuccess(request -> {
                    request.response().onSuccess(resp -> {
                        resp.customFrameHandler(frame -> System.out.println("Got frame from server " + frame.payload().toString(StandardCharsets.UTF_8)));
                        testContext.completeNow();
                    });
                    request.sendHead().onSuccess(v -> clientVertx.setPeriodic(1000, timerID -> {
                        System.out.println("Sending ping frame to server");
                        request.writeCustomFrame(10, 0, Buffer.buffer("ping"));
                    }));
                });// todo onFailure ????
    }

    @Test
    @Timeout(value = 15, timeUnit = TimeUnit.SECONDS)
    void testEventBusByPointToPoint(VertxTestContext testContext) {
        Vertx.clusteredVertx(new VertxOptions(), res -> {
            if (res.succeeded()) {
                Vertx vertx = res.result();
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
                res.cause().printStackTrace();
            }
        });
        Vertx.clusteredVertx(new VertxOptions(), res -> {
            if (res.succeeded()) {
                Vertx vertx = res.result();
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
                res.cause().printStackTrace();
            }
        });
    }

    @Test
    @Timeout(value = 15, timeUnit = TimeUnit.SECONDS)
    void testEventBusByMessageCodec(VertxTestContext testContext) {
        Vertx.clusteredVertx(new VertxOptions(), res -> {
            if (res.succeeded()) {
                Vertx vertx = res.result();
                try {
                    EventBus clusterReceiverEventBus = vertx.eventBus();
                    clusterReceiverEventBus.registerDefaultCodec(CustomMessage.class, new CustomMessageCodec());
                    clusterReceiverEventBus.consumer("cluster-message-receiver", message -> {
                        CustomMessage customMessage = (CustomMessage) message.body();
                        System.out.println("Custom message received: " + customMessage.summary());
                        message.reply(new CustomMessage(200, "a00000002", "Message sent from cluster receiver!"));
                    });
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            } else {
                res.cause().printStackTrace();
            }
        });
        Vertx.clusteredVertx(new VertxOptions(), res -> {
            if (res.succeeded()) {
                Vertx vertx = res.result();
                try {
                    EventBus senderEventBus = vertx.eventBus();
                    senderEventBus.registerDefaultCodec(CustomMessage.class, new CustomMessageCodec());
                    vertx.setPeriodic(1000, _id -> senderEventBus.request("cluster-message-receiver",
                            new CustomMessage(200, "a00000001", "Message sent from publisher!"),
                            reply -> {
                                if (reply.succeeded()) {
                                    assertThat(((CustomMessage) reply.result().body()).summary())
                                            .isEqualTo("Message sent from cluster receiver!");

                                } else {
                                    System.out.println("No reply from cluster receiver");
                                }
                            }));
                    vertx.deployVerticle(com.lingh.eventbus.messagecodec.LocalReceiver.class.getName(), deployResult -> {
                        if (deployResult.succeeded()) {
                            vertx.setPeriodic(2000, _id -> senderEventBus.request("local-message-receiver",
                                    new CustomMessage(200, "a0000001", "Local message!"),
                                    reply -> {
                                        if (reply.succeeded()) {
                                            assertThat(((CustomMessage) reply.result().body()).summary())
                                                    .isEqualTo("Message sent from local receiver!");
                                            testContext.completeNow();
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
        Runner.runClusteredExample(com.lingh.eventbus.ssl.Receiver.class, new VertxOptions().setEventBusOptions(
                        new EventBusOptions().setSsl(true)
                                .setKeyStoreOptions(new JksOptions().setPath("src/test/java/com/lingh/eventbus/ssl/keystore.jks").setPassword("wibble"))
                                .setTrustStoreOptions(new JksOptions().setPath("src/test/java/com/lingh/eventbus/ssl/keystore.jks").setPassword("wibble"))
                )
        );
        Runner.runClusteredExample(com.lingh.eventbus.ssl.Sender.class);
        testContext.completeNow();
    }

    @Test
    @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
    void testFuture(VertxTestContext testContext) {
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        Promise<String> promise = Promise.promise();
        serverVertx.setTimer(100, l -> promise.complete("world"));
        promise.future().compose(name -> {
                    Promise<String> anotherAsyncActionPromise = Promise.promise();
                    serverVertx.setTimer(100, l -> anotherAsyncActionPromise.complete("hello " + name));
                    return anotherAsyncActionPromise.future();
                })
                .onComplete(ar -> {
                    assertThat(ar.failed()).isFalse();
                    assertThat(ar.result()).isEqualTo("hello world");
                    testContext.completeNow();
                });
    }

    @Test
    @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
    void testVerticleInDeploy(VertxTestContext testContext) {
        Vertx firstVertx = Vertx.vertx(new VertxOptions());
        System.out.println("Main verticle has started, let's deploy some others...");
        firstVertx.deployVerticle(com.lingh.verticle.deploy.OtherVerticle.class.getName());
        firstVertx.deployVerticle(com.lingh.verticle.deploy.OtherVerticle.class.getName(), res -> {
            if (res.succeeded()) {
                String deploymentID = res.result();
                System.out.println("Other verticle deployed ok, deploymentID = " + deploymentID);
                firstVertx.undeploy(deploymentID, res2 -> {
                    if (res2.succeeded()) {
                        System.out.println("Undeployed ok!");
                    } else {
                        res2.cause().printStackTrace();
                    }
                });
            } else {
                res.cause().printStackTrace();
            }
        });
        JsonObject config = new JsonObject().put("foo", "bar");
        firstVertx.deployVerticle(com.lingh.verticle.deploy.OtherVerticle.class.getName(), new DeploymentOptions().setConfig(config));
        firstVertx.deployVerticle(com.lingh.verticle.deploy.OtherVerticle.class.getName(), new DeploymentOptions().setInstances(10));
        firstVertx.deployVerticle(com.lingh.verticle.deploy.OtherVerticle.class.getName(), new DeploymentOptions().setWorker(true),
                res -> {
                    if (res.succeeded()) {
                        testContext.completeNow();
                    }
                });
    }

    /**
     * @see com.lingh.verticle.deploy.DeployPolyglotExample
     */
    @Test
    @Timeout(value = 20, timeUnit = TimeUnit.SECONDS)
    @Disabled
    void testVerticleInPolyglotDeploy(VertxTestContext testContext) {   // todo fail
        Vertx firstVertx = Vertx.vertx(new VertxOptions());
        System.out.println("Main verticle has started, let's deploy A JS one...");
        firstVertx.deployVerticle("src/test/resources/jsverticle.js", deployResult -> {
            if (deployResult.succeeded()) {
                testContext.completeNow();
            } else {
                deployResult.cause().printStackTrace();
            }
        });
    }

    @Test
    @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
    void testVerticleInAsynchronousDeployment(VertxTestContext testContext) {
        Vertx firstVertx = Vertx.vertx(new VertxOptions());
        System.out.println("Main verticle has started, let's deploy some others...");
        firstVertx.deployVerticle(com.lingh.verticle.asyncstart.OtherVerticle.class.getName(), res -> {
            if (res.succeeded()) {
                String deploymentID = res.result();
                System.out.println("Other verticle deployed ok, deploymentID = " + deploymentID);
                firstVertx.undeploy(deploymentID, res2 -> {
                    if (res2.succeeded()) {
                        System.out.println("Undeployed ok!");
                        testContext.completeNow();
                    } else {
                        res2.cause().printStackTrace();
                    }
                });
            } else {
                res.cause().printStackTrace();
            }
        });
    }

    @Test
    void testVerticleInWorkerVerticle(VertxTestContext testContext) {
        Runner.runExample(com.lingh.verticle.worker.MainVerticle.class, null);
        testContext.completeNow();
    }

    @SuppressWarnings("unused")
    @Test
    void testExecuteBlocking(VertxTestContext testContext) throws Throwable {   // todo Test writing is unreasonable
        int firstPort = 8080;
        int secondPort = 8081;
        Vertx firstVertx = Vertx.vertx(new VertxOptions());
        firstVertx.createHttpServer()
                .requestHandler(request -> firstVertx.<String>executeBlocking(promise -> {
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
                }))
                .listen(secondPort)
                .onComplete(testContext.succeedingThenComplete());

        Vertx secondVertx = Vertx.vertx(new VertxOptions());
        secondVertx.deployVerticle(com.lingh.execblocking.ExecBlockingExample.class, new DeploymentOptions()
                .setWorkerPoolName("dedicated-pool")
                .setMaxWorkerExecuteTime(120000)
                .setWorkerPoolSize(5));

        assertThat(testContext.awaitCompletion(5, TimeUnit.SECONDS)).isTrue();
        if (testContext.failed()) {
            throw testContext.causeOfFailure();
        }
    }

    @Test
    void testHighAvailability(VertxTestContext testContext) {
        Launcher.main(new String[]{"run", com.lingh.ha.Server.class.getName(), "-ha"});
        Launcher.main(new String[]{"bare"});
        testContext.completeNow();
    }

    @Test
    @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
    void testJSONStreamingParser(VertxTestContext testContext) {
        Vertx firstVertx = Vertx.vertx(new VertxOptions());
        firstVertx.fileSystem().open("src/test/resources/large.json", new OpenOptions(), ar -> {
            if (ar.succeeded()) {
                AsyncFile asyncFile = ar.result();
                AtomicInteger counter = new AtomicInteger();
                JsonParser jsonParser = JsonParser.newParser(asyncFile);
                jsonParser.objectValueMode()
                        .exceptionHandler(t -> {
                            t.printStackTrace();
                            asyncFile.close();
                        })
                        .endHandler(v -> {
                            System.out.println("Done!");
                            asyncFile.close();
                            testContext.completeNow();
                        }).handler(event -> {
                            if (event.type() == JsonEventType.VALUE) {
                                DataPoint dataPoint = event.mapTo(DataPoint.class);
                                if (counter.incrementAndGet() % 100 == 0) {
                                    System.out.printf("DataPoint = %s%n", dataPoint);
                                }
                            }
                        });
            } else {
                ar.cause().printStackTrace();
            }
        });

    }

    @Test
    @Timeout(value = 20, timeUnit = TimeUnit.SECONDS)
    void testCustomReadStreamAndWriteStreamImplementation(VertxTestContext testContext) {
        int firstPort = 1234;
        Checkpoint requestsServed = testContext.checkpoint(3);
        Checkpoint responsesReceived = testContext.checkpoint(3);
        Vertx serverVertx = Vertx.vertx(new VertxOptions());
        Vertx clientVertx = Vertx.vertx(new VertxOptions());
        serverVertx.createNetServer().connectHandler(socket -> {
            BatchStream batchStream = new BatchStream(socket, socket);
            batchStream.pause();
            batchStream.handler(batch -> {
                        System.out.println("Server Received : " + batch.getRaw().toString());
                        batchStream.write(batch);
                        if (batchStream.writeQueueFull()) {
                            batchStream.pause();
                            batchStream.drainHandler(done -> batchStream.resume());
                        }
                        requestsServed.flag();
                    }).endHandler(v -> batchStream.end())
                    .exceptionHandler(t -> {
                        t.printStackTrace();
                        batchStream.end();
                    });
            batchStream.resume();
        }).listen(firstPort);
        System.out.println("Batch server is now listening to port : 1234");
        clientVertx.createNetClient()
                .connect(firstPort, "localhost", ar -> {
                    assertThat(ar.succeeded()).isTrue();
                    NetSocket socket = ar.result();
                    BatchStream batchStream = new BatchStream(socket, socket);
                    batchStream.pause();
                    batchStream.handler(batch -> {
                                System.out.println("Client Received : " + batch.getRaw().toString());
                                responsesReceived.flag();
                            })
                            .endHandler(v -> batchStream.end())
                            .exceptionHandler(t -> {
                                t.printStackTrace();
                                batchStream.end();
                            });
                    batchStream.resume();
                    JsonObject jsonObject = new JsonObject().put("id", UUID.randomUUID().toString()).put("name", "Vert.x").put("timestamp", Instant.now());
                    JsonArray jsonArray = new JsonArray().add(UUID.randomUUID().toString()).add("Vert.x").add(Instant.now());
                    Buffer buffer = Buffer.buffer("Vert.x is awesome!");
                    batchStream.write(new Batch(jsonObject));
                    batchStream.write(new Batch(jsonArray));
                    batchStream.write(new Batch(buffer));
                });
    }
}
