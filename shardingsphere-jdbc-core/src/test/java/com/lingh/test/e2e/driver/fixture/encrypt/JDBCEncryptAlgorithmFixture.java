

package com.lingh.test.e2e.driver.fixture.encrypt;

import lombok.Getter;
import org.apache.shardingsphere.encrypt.api.encrypt.standard.StandardEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.context.EncryptContext;

import java.util.Properties;

@Getter
public final class JDBCEncryptAlgorithmFixture implements StandardEncryptAlgorithm<Object, String> {
    
    private Properties props;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
    }
    
    @Override
    public String encrypt(final Object plainValue, final EncryptContext encryptContext) {
        return "encryptValue";
    }
    
    @Override
    public Object decrypt(final String cipherValue, final EncryptContext encryptContext) {
        return "decryptValue";
    }
    
    @Override
    public String getType() {
        return "JDBC.FIXTURE";
    }
}
