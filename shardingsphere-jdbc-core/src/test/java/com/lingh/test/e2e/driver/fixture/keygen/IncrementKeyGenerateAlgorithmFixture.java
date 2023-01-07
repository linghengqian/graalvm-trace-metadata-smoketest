

package com.lingh.test.e2e.driver.fixture.keygen;

import lombok.Getter;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public final class IncrementKeyGenerateAlgorithmFixture implements KeyGenerateAlgorithm {
    
    private final AtomicInteger count = new AtomicInteger();
    
    private Properties props;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
    }
    
    @Override
    public Comparable<?> generateKey() {
        return count.incrementAndGet();
    }
    
    @Override
    public String getType() {
        return "JDBC.INCREMENT.FIXTURE";
    }
}
