

package org.apache.shardingsphere.test.e2e.driver.fixture.keygen;

import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;

import java.util.concurrent.atomic.AtomicInteger;

public final class IncrementKeyGenerateAlgorithmFixture implements KeyGenerateAlgorithm {
    
    private final AtomicInteger count = new AtomicInteger();
    
    @Override
    public Comparable<?> generateKey() {
        return count.incrementAndGet();
    }
    
    @Override
    public String getType() {
        return "JDBC.INCREMENT.FIXTURE";
    }
    
    @Override
    public boolean isSupportAutoIncrement() {
        return true;
    }
}
