package com.lingh.net.echossl;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.streams.Pump;

public class Server extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runExample(Server.class);
    }

    @Override
    public void start() {
        NetServerOptions options = new NetServerOptions()
                .setSsl(true).setKeyStoreOptions(new JksOptions().setPath("server-keystore.jks").setPassword("wibble"));
        vertx.createNetServer(options).connectHandler(sock -> Pump.pump(sock, sock).start()).listen(1234);
        System.out.println("Echo server is now listening");
    }
}
