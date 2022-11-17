package com.lingh.execblocking;

import com.lingh.Runner;
import io.vertx.core.DeploymentOptions;

public class ExecBlockingDedicatedPoolExample {
    public static void main(String[] args) {
        Runner.runExample(ExecBlockingExample.class, new DeploymentOptions()
                .setWorkerPoolName("dedicated-pool")
                .setMaxWorkerExecuteTime(120000)
                .setWorkerPoolSize(5));
    }

}
