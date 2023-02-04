

package org.apache.shardingsphere.test.e2e.driver.fixture.keygen;

import lombok.Getter;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;

import java.util.concurrent.atomic.AtomicInteger;

public final class ResetIncrementKeyGenerateAlgorithmFixture implements KeyGenerateAlgorithm {
    
    @Getter
    private static final AtomicInteger COUNT = new AtomicInteger();
    
    @Override
    public Comparable<?> generateKey() {
        return COUNT.incrementAndGet();
    }
    
    @Override
    public String getType() {
        return "JDBC.RESET_INCREMENT.FIXTURE";
    }
    
    @Override
    public boolean isSupportAutoIncrement() {
        return true;
    }
}
