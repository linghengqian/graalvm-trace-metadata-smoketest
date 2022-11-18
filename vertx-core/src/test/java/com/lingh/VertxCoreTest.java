package com.lingh;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Launcher;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class VertxCoreTest {
    @Test
    void testEmbedding(VertxTestContext testContext) {
        Vertx.vertx().createHttpServer().requestHandler(req -> req.response().end("Hello World!")).listen(8080);
        testContext.completeNow();
    }

    @Test
    void testNetInEcho(VertxTestContext testContext) {
        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.net.echo.Server.class, new VertxOptions(), null, false);
        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.net.echo.Client.class, new VertxOptions(), null, false);
        testContext.completeNow();
    }

    @Test
    void testNetInEchoSSL(VertxTestContext testContext) {
        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.net.echossl.Server.class, new VertxOptions(), null, false);
        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.net.echossl.Client.class, new VertxOptions(), null, false);
        testContext.completeNow();
    }

    @Test
    void testHttpInSimple(VertxTestContext testContext) {
        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.http.simple.Server.class, new VertxOptions(), null, false);
        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.http.simple.Client.class, new VertxOptions(), null, false);
        testContext.completeNow();
    }

    @Test
    void testHttpInHttps(VertxTestContext testContext) {
        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.http.https.Server.class, new VertxOptions(), null, false);
        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.http.https.Client.class, new VertxOptions(), null, false);
        testContext.completeNow();
    }

    @Test
    @Disabled
    void testHttpInProxyConnect(VertxTestContext testContext) {
        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.http.proxyconnect.Server.class, new VertxOptions(), null, false);
        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.http.proxyconnect.Proxy.class, new VertxOptions(), null, false);
        // TODO It seems that no matter what, only using vertx-core for proxy connect, the client will throw strange exception information.
        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.http.proxyconnect.Client.class, new VertxOptions(), null, false);
        testContext.completeNow();
    }

    @Test
    void testHttpInProxy(VertxTestContext testContext) {
        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.http.proxy.Server.class, new VertxOptions(), null, false);
        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.http.proxy.Proxy.class, new VertxOptions(), null, false);
        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.http.proxy.Client.class, new VertxOptions(), null, false);
        testContext.completeNow();
    }

    @Test
    void testHttpInSendfile(VertxTestContext testContext) {
        Runner.runExample(com.lingh.http.sendfile.SendFile.class, null);
        testContext.completeNow();
    }

    @Test
    void testHttpInSimpleForm(VertxTestContext testContext) {
        Runner.runExample(com.lingh.http.simpleform.SimpleFormServer.class, null);
        testContext.completeNow();
    }

    @Test
    void testHttpInSimpleFormFileUpload(VertxTestContext testContext) {
        Runner.runExample(com.lingh.http.simpleformupload.SimpleFormUploadServer.class, null);
        testContext.completeNow();
    }

    @Test
    void testHttpInHttpRequestBodyUpload(VertxTestContext testContext) {
        Runner.runExample(com.lingh.http.upload.Server.class, null);
        Runner.runExample(com.lingh.http.upload.Client.class, null);
        testContext.completeNow();
    }

    @Test
    void testHttpInHTTPServerSharing(VertxTestContext testContext) {
        Runner.runExample(com.lingh.http.sharing.Server.class, null);
        Runner.runExample(com.lingh.http.sharing.Client.class, null);
        testContext.completeNow();
    }

    @Test
    void testHttpInWebSocketsEcho(VertxTestContext testContext) {
        Runner.runExample(com.lingh.http.websockets.Server.class, null);
        // TODO
//         Runner.runExample(com.lingh.http.websockets.Client.class, null);
        testContext.completeNow();
    }

    @Test
    void testHttp2InSimple(VertxTestContext testContext) {
        Runner.runExample(com.lingh.http2.simple.Server.class, null);
        testContext.completeNow();
    }

    @Test
    void testHttp2InPush(VertxTestContext testContext) {
        Runner.runExample(com.lingh.http2.push.Server.class, null);
        testContext.completeNow();
    }

    @Test
    void testHttp2InH2C(VertxTestContext testContext) {
        Runner.runExample(com.lingh.http2.h2c.Server.class, null);
        Runner.runExample(com.lingh.http2.h2c.Client.class, null);
        testContext.completeNow();
    }

    @Test
    void testHttp2InCustomFrames(VertxTestContext testContext) {
        Runner.runExample(com.lingh.http2.customframes.Server.class, null);
        Runner.runExample(com.lingh.http2.customframes.Client.class, null);
        testContext.completeNow();
    }

    @Test
    void testEventBusByPointToPoint(VertxTestContext testContext) {
        Runner.runClusteredExample(com.lingh.eventbus.pointtopoint.Receiver.class);
        Runner.runClusteredExample(com.lingh.eventbus.pointtopoint.Sender.class);
        testContext.completeNow();
    }

    @Test
    void testEventBusByMessageCodec(VertxTestContext testContext) {
        Runner.runClusteredExample(com.lingh.eventbus.messagecodec.ClusterReceiver.class);
        Runner.runClusteredExample(com.lingh.eventbus.messagecodec.Sender.class);
        testContext.completeNow();
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
    void testFuture(VertxTestContext testContext) {
        Runner.runExample(com.lingh.future.ComposeExample.class, null);
        testContext.completeNow();
    }

    @Test
    void testVerticleInDeploy(VertxTestContext testContext) {
        Runner.runExample(com.lingh.verticle.deploy.DeployExample.class, null);
        testContext.completeNow();
    }

    @Test
    void testVerticleInPolyglotDeploy(VertxTestContext testContext) {
        Runner.runExample(com.lingh.verticle.deploy.DeployPolyglotExample.class, null);
        testContext.completeNow();
    }

    @Test
    void testVerticleInAsynchronousDeployment(VertxTestContext testContext) {
        Runner.runExample(com.lingh.verticle.asyncstart.DeployExample.class, null);
        testContext.completeNow();
    }

    @Test
    void testVerticleInWorkerVerticle(VertxTestContext testContext) {
        Runner.runExample(com.lingh.verticle.worker.MainVerticle.class, null);
        testContext.completeNow();
    }

    @Test
    void testExecuteBlocking(VertxTestContext testContext) {
        Runner.runExample(com.lingh.execblocking.ExecBlockingExample.class, null);
        Runner.runExample(com.lingh.execblocking.ExecBlockingExample.class, new DeploymentOptions()
                .setWorkerPoolName("dedicated-pool")
                .setMaxWorkerExecuteTime(120000)
                .setWorkerPoolSize(5));
        testContext.completeNow();
    }

    @Test
    @Disabled
    void testHighAvailability(VertxTestContext testContext) { // TODO too many log
        Launcher.main(new String[]{"run", com.lingh.ha.Server.class.getName(), "-ha"});
        Launcher.main(new String[]{"bare"});
        testContext.completeNow();
    }

    @Test
    void testJSONStreamingParser(VertxTestContext testContext) {
        Runner.runExample(com.lingh.jsonstreaming.JsonStreamingExample.class, null);
        testContext.completeNow();
    }

    @Test
    void testCustomReadStreamAndWriteStreamImplementation(VertxTestContext testContext) {
        Runner.runExample(com.lingh.net.stream.Server.class, null);
        Runner.runExample(com.lingh.net.stream.Client.class, null);
        testContext.completeNow();
    }
}
