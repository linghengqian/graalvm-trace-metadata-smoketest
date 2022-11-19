package com.lingh;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.*;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
public class UnSupportedVertxCoreTest {
    /**
     * @see com.lingh.http.proxyconnect.Server
     * @see com.lingh.http.proxyconnect.Proxy
     * @see com.lingh.http.proxyconnect.Client
     */
    @Test
    @Disabled
    @DisabledInNativeImage
    @Timeout(value = 20, timeUnit = TimeUnit.SECONDS)
    void testHttpInProxyConnect(VertxTestContext testContext) { // todo need to fix in master branch
//        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.http.proxyconnect.Server.class, new VertxOptions(), null, false);
//        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.http.proxyconnect.Proxy.class, new VertxOptions(), null, false);
        // TODO It seems that no matter what, only using vertx-core for proxy connect, the client will throw strange exception information.
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
                proxyVertx.createNetClient(new NetClientOptions()
                        .setKeyCertOptions(certificate.keyCertOptions())
                ).connect(port, host, ar -> {
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
                )
                .request(HttpMethod.GET, secondPort, "localhost", "/")
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

    /**
     * @see com.lingh.verticle.deploy.DeployPolyglotExample
     */
    @Test
    @DisabledInNativeImage
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

}
