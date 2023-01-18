
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
import java.util.Map;
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

    /**
     * This test checks that the concurrent map is linearizable with bounded model checking. Unlike
     * stress testing, this approach can also provide a trace of an incorrect execution. However, it
     * uses sequential consistency model, so it can not find any low-level bugs (e.g., missing
     * 'volatile'), and thus, it is recommended to have both test modes.
     * <p>
     * This test requires the following JVM arguments,
     * <ul>
     *   <li>--add-opens java.base/jdk.internal.misc=ALL-UNNAMED
     *   <li>--add-exports java.base/jdk.internal.util=ALL-UNNAMED
     * </ul>
     */
    @Test(groups = "lincheck")
    public void modelCheckingTest() {
        var options = new ModelCheckingOptions()
                .iterations(100)                  // the number of different scenarios
                .invocationsPerIteration(10_000); // how deeply each scenario is tested
        new LinChecker(getClass(), options).check();
    }

    /**
     * This test checks that the concurrent map is linearizable with stress testing.
     */
    @Test(groups = "lincheck")
    public void stressTest() {
        var options = new StressOptions()
                .iterations(100)                  // the number of different scenarios
                .invocationsPerIteration(10_000); // how deeply each scenario is tested
        new LinChecker(getClass(), options).check();
    }

    /**
     * Provides something with correct <tt>equals</tt> and <tt>hashCode</tt> methods that can be
     * interpreted as an internal data structure state for faster verification. The only limitation is
     * that it should be different for different data structure states. For {@link Map} it itself is
     * used.
     *
     * @return object representing internal state
     */
    @Override
    protected Object extractState() {
        return new ArrayList<Integer>(queue);
    }
}
