package org_apache_shardingsphere_elasticjob.elasticjob_lite_core.repository;

import org_apache_shardingsphere_elasticjob.elasticjob_lite_core.entity.Status;
import org_apache_shardingsphere_elasticjob.elasticjob_lite_core.entity.TOrderPOJO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class TOrderRepository {
    private final Map<Long, TOrderPOJO> data = new ConcurrentHashMap<>(300, 1);

    public TOrderRepository() {
        addData(0L, 100L, "Beijing");
        addData(100L, 200L, "Shanghai");
        addData(200L, 300L, "Guangzhou");
    }

    private void addData(final long idFrom, final long idTo, final String location) {
        LongStream.range(idFrom, idTo).forEachOrdered(i -> data.put(i, new TOrderPOJO(i, location, Status.TODO)));
    }

    public List<TOrderPOJO> findTodoData(final String location, final int limit) {
        return data.entrySet().stream()
                .limit(limit)
                .map(Map.Entry::getValue)
                .filter(TOrderPOJO -> TOrderPOJO.location().equals(location) && TOrderPOJO.status() == Status.TODO)
                .collect(Collectors.toCollection(() -> new ArrayList<>(limit)));
    }

    public void setCompleted(final long id) {
        data.replace(id, new TOrderPOJO(id, data.get(id).location(), Status.COMPLETED));
    }
}
