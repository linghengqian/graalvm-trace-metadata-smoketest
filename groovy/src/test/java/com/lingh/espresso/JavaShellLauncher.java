package com.lingh.espresso;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.graalvm.home.HomeFinder;

import jdk.jshell.tool.JavaShellToolBuilder;

public final class JavaShellLauncher {

    public static void main(String[] args) throws Exception {
        findJavaHome();
        EspressoLocalExecutionControl.initializeInParallel(extractRemoteOptions(args));
        System.exit(JavaShellToolBuilder.builder().start(withEspressoExecutionEngine(args)));
    }

    /**
     * Finds and set a Java home to run espresso-jshell.
     *
     * espresso-jshell is not fully standalone, it requires a Java home + Espresso home to run.
     *
     * The following sources are checked:
     * <ul type=1>
     * <li>{@code org.graalvm.home} property
     * <li>{@code GRAALVM_HOME} environment variable
     * <li>{@code HomeFinder.getInstance().getHomeFolder()}
     * </ul>
     */
    private static void findJavaHome() {
        String javaHome = System.getProperty("java.home");
        if (javaHome == null) {
            System.err.println("java.home (required by javac) is not defined; trying fallback to 'org.graalvm.home' and $GRAALVM_HOME");
            if (System.getProperty("org.graalvm.home") == null) {
                String envVariable = System.getenv("GRAALVM_HOME");
                if (envVariable != null) {
                    System.err.println("Setting org.graalvm.home=" + envVariable);
                    System.setProperty("org.graalvm.home", envVariable);
                }
            }
            Path graalvmHome = HomeFinder.getInstance().getHomeFolder();
            if (graalvmHome != null) {
                System.err.println("Setting java.home=" + graalvmHome);
                System.setProperty("java.home", graalvmHome.toString());
            } else {
                System.err.println("Cannot find GraalVM home; 'org.graalvm.home' nor $GRAALVM_HOME are defined.");
                System.exit(-1);
            }
        }
    }

    private static Map<String, String> extractRemoteOptions(String[] args) {
        Map<String, String> options = new HashMap<>();
        for (String arg : args) {
            if (arg.startsWith("-R-D")) {
                String key = arg.substring("-R-D".length());
                int splitAt = key.indexOf("=");
                String value = "";
                if (splitAt >= 0) {
                    value = key.substring(splitAt + 1);
                    key = key.substring(0, splitAt);
                }
                options.put("java.Properties." + key, value);
            } else if (arg.startsWith("-R")) {
                String key = arg.substring("-R".length());
                int splitAt = key.indexOf("=");
                String value = "true";
                if (splitAt >= 0) {
                    value = key.substring(splitAt + 1);
                    key = key.substring(0, splitAt);
                }
                options.put(key, value);
            }
        }
        return options;
    }

    private static String[] withEspressoExecutionEngine(String[] args) {
        boolean engineHasBeenSet = false;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("-execution".equals(arg) || "--execution".equals(arg)) {
                engineHasBeenSet = true;
                if (i + 1 < args.length) {
                    String provider = args[i + 1];
                    if (!EspressoLocalExecutionControlProvider.NAME.equals(provider)) {
                        throw new RuntimeException("espresso-jshell only supports the 'espresso' execution engine");
                    }
                }
                ++i;
            }
        }
        if (engineHasBeenSet) {
            return args; // nothing to do
        }
        return Stream.concat(Arrays.stream(args), Stream.of("-execution", "espresso")).toArray(String[]::new);
    }
}