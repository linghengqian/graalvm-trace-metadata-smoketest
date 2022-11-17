package com.lingh;

import io.vertx.core.Vertx;
import org.junit.jupiter.api.Test;

public class VertxCoreTests {
    @Test
    void test1() {
        Vertx.vertx().createHttpServer().requestHandler(req -> req.response().end("Hello World!")).listen(8080);
    }

    @Test
    void test2(){

    }
}
