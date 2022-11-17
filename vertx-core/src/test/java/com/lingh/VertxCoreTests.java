package com.lingh;

import com.lingh.http.sendfile.SendFile;
import com.lingh.http.simpleform.SimpleFormServer;
import com.lingh.http.simpleformupload.SimpleFormUploadServer;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.junit.jupiter.api.Test;

public class VertxCoreTests {
    @Test
    void testEmbedding() {
        Vertx.vertx().createHttpServer().requestHandler(req -> req.response().end("Hello World!")).listen(8080);
    }

    @Test
    void testNetInEcho() {
        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.net.echo.Server.class, new VertxOptions(), null, false);
        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.net.echo.Client.class, new VertxOptions(), null, false);
    }

    @Test
    void testNetInEchoSSL() {
        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.net.echossl.Server.class, new VertxOptions(), null, false);
        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.net.echossl.Client.class, new VertxOptions(), null, false);
    }

    @Test
    void testHttpInSimple() {
        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.http.simple.Server.class, new VertxOptions(), null, false);
        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.http.simple.Client.class, new VertxOptions(), null, false);
    }

    @Test
    void testHttpInHttps() {
        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.http.https.Server.class, new VertxOptions(), null, false);
        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.http.https.Client.class, new VertxOptions(), null, false);
    }

    @Test
    void testHttpInProxyConnect() {
        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.http.proxyconnect.Server.class, new VertxOptions(), null, false);
        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.http.proxyconnect.Proxy.class, new VertxOptions(), null, false);
        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.http.proxyconnect.Client.class, new VertxOptions(), null, false);
    }

    @Test
    void testHttpInProxy() {
        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.http.proxy.Server.class, new VertxOptions(), null, false);
        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.http.proxy.Proxy.class, new VertxOptions(), null, false);
        Runner.runExample(Runner.getCORE_EXAMPLES_JAVA_DIR(), com.lingh.http.proxy.Client.class, new VertxOptions(), null, false);
    }

    @Test
    void testHttpInSendfile() {
        Runner.runExample(SendFile.class, null);
    }

    @Test
    void testHttpInSimpleForm() {
        Runner.runExample(SimpleFormServer.class, null);
    }

    @Test
    void testHttpInSimpleFormFileUpload() {
        Runner.runExample(SimpleFormUploadServer.class, null);
    }

    @Test
    void testHttpInHttpRequestBodyUpload() {
        Runner.runExample(com.lingh.http.upload.Server.class, null);
        Runner.runExample(com.lingh.http.upload.Client.class, null);
    }

    @Test
    void testHttpInHTTPServerSharing() {
        Runner.runExample(com.lingh.http.sharing.Server.class, null);
        Runner.runExample(com.lingh.http.sharing.Client.class, null);
    }

    @Test
    void testHttpInWebSocketsEcho() {
        Runner.runExample(com.lingh.http.websockets.Server.class, null);
        Runner.runExample(com.lingh.http.websockets.Client.class, null);
    }

    @Test
    void testHttp2InSimple() {
        Runner.runExample(com.lingh.http2.simple.Server.class, null);
        Runner.runExample(com.lingh.http2.simple.Client.class, null);
    }

    @Test
    void testHttp2InPush() {
        Runner.runExample(com.lingh.http2.push.Server.class, null);
    }

    @Test
    void testHttp2InH2C() {
        Runner.runExample(com.lingh.http2.h2c.Server.class, null);
        Runner.runExample(com.lingh.http2.h2c.Client.class, null);
    }

    @Test
    void testHttp2InCustomFrames() {
        Runner.runExample(com.lingh.http2.customframes.Server.class, null);
        Runner.runExample(com.lingh.http2.customframes.Client.class, null);
    }

}
