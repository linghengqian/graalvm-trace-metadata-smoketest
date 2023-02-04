

package org.apache.shardingsphere.test.e2e.driver.fixture.encrypt;

import org.apache.shardingsphere.encrypt.api.encrypt.standard.StandardEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.context.EncryptContext;

public final class JDBCEncryptAlgorithmFixture implements StandardEncryptAlgorithm<Object, String> {
    
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
