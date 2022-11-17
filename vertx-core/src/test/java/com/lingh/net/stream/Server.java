package com.lingh.net.stream;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;

public class Server extends AbstractVerticle {
    public static void main(String[] args) {
        Runner.runExample(Server.class, null);
    }

    @Override
    public void start() {
        vertx.createNetServer().connectHandler(socket -> {
            BatchStream batchStream = new BatchStream(socket, socket);
            batchStream.pause();
            batchStream.handler(batch -> {
                        System.out.println("Server Received : " + batch.getRaw().toString());
                        batchStream.write(batch);
                        if (batchStream.writeQueueFull()) {
                            batchStream.pause();
                            batchStream.drainHandler(done -> batchStream.resume());
                        }
                    }).endHandler(v -> batchStream.end())
                    .exceptionHandler(t -> {
                        t.printStackTrace();
                        batchStream.end();
                    });
            batchStream.resume();

        }).listen(1234);
        System.out.println("Batch server is now listening to port : 1234");
    }
}
