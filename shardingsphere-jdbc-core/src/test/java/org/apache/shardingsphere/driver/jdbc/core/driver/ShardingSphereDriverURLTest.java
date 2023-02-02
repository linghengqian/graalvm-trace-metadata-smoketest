

package org.apache.shardingsphere.driver.jdbc.core.driver;

import org.junit.Test;

import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class ShardingSphereDriverURLTest {

    private final int fooDriverConfigLength = 995;

    @Test(expected = IllegalArgumentException.class)
    public void assertNewConstructorWithEmptyURL() {
        new ShardingSphereDriverURL("jdbc:shardingsphere:");
    }

    @Test
    public void assertToClasspathConfigurationFile() {
        ShardingSphereDriverURL actual = new ShardingSphereDriverURL("jdbc:shardingsphere:classpath:config/driver/foo-driver-fixture.yaml");
        assertThat(actual.toConfigurationBytes().length, is(fooDriverConfigLength));
    }

    @Test
    public void assertToConfigurationFile() {
        String absolutePath = Objects.requireNonNull(ShardingSphereDriverURLTest.class.getClassLoader().getResource("config/driver/foo-driver-fixture.yaml")).getPath();
        ShardingSphereDriverURL actual = new ShardingSphereDriverURL("jdbc:shardingsphere:" + absolutePath);
        assertThat(actual.toConfigurationBytes().length, is(fooDriverConfigLength));
    }

    @Test
    public void assertToConfigurationFileWithOtherParameters() {
        String absolutePath = Objects.requireNonNull(ShardingSphereDriverURLTest.class.getClassLoader().getResource("config/driver/foo-driver-fixture.yaml")).getPath();
        ShardingSphereDriverURL actual = new ShardingSphereDriverURL("jdbc:shardingsphere:" + absolutePath + "?xxx=xxx&yyy=yyy");
        assertThat(actual.toConfigurationBytes().length, is(fooDriverConfigLength));
    }
}