package com.lingh.hint;

import org.apache.shardingsphere.sharding.api.sharding.hint.HintShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.hint.HintShardingValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

public final class ModuloHintShardingAlgorithm implements HintShardingAlgorithm<Long> {

    private Properties props;

    @Override
    public void init(final Properties props) {
        this.props = props;
    }

    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final HintShardingValue<Long> shardingValue) {
        Collection<String> result = new LinkedList<>();
        availableTargetNames.forEach(each -> shardingValue.getValues()
                .stream()
                .filter(value -> each.endsWith(String.valueOf(value % 2)))
                .map(value -> each).forEach(result::add));
        return result;
    }

    @Override
    public String getType() {
        return "HINT_TEST";
    }

    public Properties getProps() {
        return this.props;
    }
}
