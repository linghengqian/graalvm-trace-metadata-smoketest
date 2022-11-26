package com.lingh.fixture;

import lombok.Getter;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.context.EncryptContext;

import java.util.Properties;

@Getter
public final class TestQueryAssistedShardingEncryptAlgorithm implements EncryptAlgorithm<Object, String> {
    
    private Properties props;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
    }
    
    @Override
    public String encrypt(final Object plainValue, final EncryptContext encryptContext) {
        return "assistedEncryptValue";
    }
    
    @Override
    public Object decrypt(final String cipherValue, final EncryptContext encryptContext) {
        return "decryptValue";
    }
    
    @Override
    public String getType() {
        return "assistedTest";
    }
}
