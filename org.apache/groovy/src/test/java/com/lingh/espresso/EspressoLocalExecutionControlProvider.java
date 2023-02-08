package com.lingh.espresso;

import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.spi.ExecutionControlProvider;
import jdk.jshell.spi.ExecutionEnv;

import java.util.Map;

public final class EspressoLocalExecutionControlProvider implements ExecutionControlProvider {

    public static final String NAME = "espresso";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public ExecutionControl generate(ExecutionEnv executionEnv, Map<String, String> map) throws Throwable {
        return new EspressoLocalExecutionControl(executionEnv.extraRemoteVMOptions());
    }
}