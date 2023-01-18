package com.github.benmanes.caffeine.cache;

import org.jetbrains.kotlinx.lincheck.LinChecker;
import org.jetbrains.kotlinx.lincheck.annotations.OpGroupConfig;
import org.jetbrains.kotlinx.lincheck.annotations.Operation;
import org.jetbrains.kotlinx.lincheck.annotations.Param;
import org.jetbrains.kotlinx.lincheck.paramgen.IntGen;
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions;
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions;
import org.jetbrains.kotlinx.lincheck.verifier.VerifierState;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Queue;

@OpGroupConfig(name = "consumer", nonParallel = true)
@Param(name = "element", gen = IntGen.class, conf = "1:5")
public final class MpscGrowableArrayQueueLincheckTest extends VerifierState {
    private final Queue<Integer> queue;

    public MpscGrowableArrayQueueLincheckTest() {
        queue = new org.jctools.queues.MpscGrowableArrayQueue<>(4, 65_536);
    }

    @Operation
    public boolean offer(@Param(name = "element") int e) {
        return queue.offer(e);
    }

    @Operation(group = "consumer")
    public Integer poll() {
        return queue.poll();
    }

    @Test(groups = "lincheck")
    public void modelCheckingTest() {
        var options = new ModelCheckingOptions()
                .iterations(100)
                .invocationsPerIteration(10_000);
        new LinChecker(getClass(), options).check();
    }

    @Test(groups = "lincheck")
    public void stressTest() {
        var options = new StressOptions()
                .iterations(100)
                .invocationsPerIteration(10_000);
        new LinChecker(getClass(), options).check();
    }

    @Override
    protected Object extractState() {
        return new ArrayList<>(queue);
    }
}
