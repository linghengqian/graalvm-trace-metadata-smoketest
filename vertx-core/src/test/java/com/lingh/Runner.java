package com.lingh;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class Runner {

    private static final String CORE_EXAMPLES_DIR = "core-examples";
    private static final String CORE_EXAMPLES_JAVA_DIR = CORE_EXAMPLES_DIR + "/src/main/java/";

    public static String getCORE_EXAMPLES_JAVA_DIR(){
        return CORE_EXAMPLES_JAVA_DIR;
    }

    public static void runClusteredExample(Class clazz) {
        runExample(CORE_EXAMPLES_JAVA_DIR, clazz, new VertxOptions(), null, true);
    }

    public static void runClusteredExample(Class clazz, VertxOptions options) {
        runExample(CORE_EXAMPLES_JAVA_DIR, clazz, options, null, true);
    }

    public static void runExample(Class clazz, DeploymentOptions options) {
        runExample(CORE_EXAMPLES_JAVA_DIR, clazz, new VertxOptions(), options, false);
    }

    public static void runExample(String exampleDir, Class clazz, VertxOptions options, DeploymentOptions deploymentOptions, boolean clustered) {
        runExample(exampleDir + clazz.getPackage().getName().replace(".", "/"), clazz.getName(), options, deploymentOptions, clustered);
    }


    public static void runExample(String exampleDir, String verticleID, VertxOptions options, DeploymentOptions deploymentOptions, boolean clustered) {
        if (options == null) {
            options = new VertxOptions();
        }
        try {
            File current = new File(".").getCanonicalFile();
            if (exampleDir.startsWith(current.getName()) && !exampleDir.equals(current.getName())) {
                exampleDir = exampleDir.substring(current.getName().length() + 1);
            }
        } catch (IOException e) {
        }
        System.setProperty("vertx.cwd", exampleDir);
        Consumer<Vertx> runner = vertx -> {
            try {
                if (deploymentOptions != null) {
                    vertx.deployVerticle(verticleID, deploymentOptions);
                } else {
                    vertx.deployVerticle(verticleID);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        };
        if (clustered) {
            Vertx.clusteredVertx(options, res -> {
                if (res.succeeded()) {
                    Vertx vertx = res.result();
                    runner.accept(vertx);
                } else {
                    res.cause().printStackTrace();
                }
            });
        } else {
            Vertx vertx = Vertx.vertx(options);
            runner.accept(vertx);
        }
    }
}
