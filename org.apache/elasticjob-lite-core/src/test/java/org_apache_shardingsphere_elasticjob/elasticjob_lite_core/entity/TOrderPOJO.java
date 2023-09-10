package org_apache_shardingsphere_elasticjob.elasticjob_lite_core.entity;

import java.io.Serializable;

public record TOrderPOJO(
        long id,
        String location,
        Status status

) implements Serializable {
}