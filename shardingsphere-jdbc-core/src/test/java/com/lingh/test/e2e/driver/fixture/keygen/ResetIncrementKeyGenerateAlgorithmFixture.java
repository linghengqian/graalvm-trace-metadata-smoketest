

package com.lingh.test.e2e.driver.fixture.keygen;

import lombok.Getter;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public final class ResetIncrementKeyGenerateAlgorithmFixture implements KeyGenerateAlgorithm {
    
    @Getter
    private static final AtomicInteger COUNT = new AtomicInteger();
    
    private Properties props;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
    }
    
    @Override
    public Comparable<?> generateKey() {
        return COUNT.incrementAndGet();
    }
    
    @Override
    public String getType() {
        return "JDBC.RESET_INCREMENT.FIXTURE";
    }
}
