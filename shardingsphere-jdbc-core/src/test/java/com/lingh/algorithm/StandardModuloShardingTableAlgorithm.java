
package com.lingh.algorithm;

import com.google.common.collect.Range;
import lombok.Getter;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public final class StandardModuloShardingTableAlgorithm implements StandardShardingAlgorithm<Long> {

    private Properties props;

    @Override
    public void init(final Properties props) {
        this.props = props;
    }

    @Override
    public String doSharding(final Collection<String> tableNames, final PreciseShardingValue<Long> shardingValue) {
        for (String each : tableNames) {
            if (each.endsWith(String.valueOf(shardingValue.getValue() % 2))) {
                return each;
            }
        }
        throw new UnsupportedOperationException("");
    }

    @Override
    public Collection<String> doSharding(final Collection<String> tableNames, final RangeShardingValue<Long> shardingValue) {
        Set<String> result;
        if (Range.closed(200000000000000000L, 400000000000000000L).encloses(shardingValue.getValueRange())) {
            result = tableNames.stream().filter(each -> each.endsWith("0"))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        } else {
            throw new UnsupportedOperationException("");
        }
        return result;
    }

    @Override
    public String getType() {
        return "STANDARD_TEST_TBL";
    }
}
