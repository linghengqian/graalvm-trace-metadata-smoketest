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

@Getter
public final class StandardModuloShardingDatabaseAlgorithm implements StandardShardingAlgorithm<Integer> {
    
    private Properties props;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
    }
    
    @Override
    public String doSharding(final Collection<String> databaseNames, final PreciseShardingValue<Integer> shardingValue) {
        for (String each : databaseNames) {
            if (each.endsWith(String.valueOf(shardingValue.getValue() % 2))) {
                return each;
            }
        }
        throw new UnsupportedOperationException("");
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> databaseNames, final RangeShardingValue<Integer> shardingValueRange) {
        Set<String> result = new LinkedHashSet<>();
        if (Range.closed(1, 5).encloses(shardingValueRange.getValueRange())) {
            for (String each : databaseNames) {
                if (each.endsWith("0")) {
                    result.add(each);
                }
            }
        } else if (Range.closed(6, 10).encloses(shardingValueRange.getValueRange())) {
            for (String each : databaseNames) {
                if (each.endsWith("1")) {
                    result.add(each);
                }
            }
        } else if (Range.closed(1, 10).encloses(shardingValueRange.getValueRange())) {
            result.addAll(databaseNames);
        } else {
            throw new UnsupportedOperationException("");
        }
        return result;
    }
    
    @Override
    public String getType() {
        return "STANDARD_TEST_DB";
    }
}
