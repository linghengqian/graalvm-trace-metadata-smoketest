package com.lingh.globalSerializer;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

@SuppressWarnings("DataFlowIssue")
public class GlobalSerializer implements StreamSerializer<Object> {
    @Override
    public int getTypeId() {
        return 20;
    }

    @Override
    public void destroy() {
    }

    @Override
    public void write(ObjectDataOutput out, Object object) {
    }

    @Override
    public Object read(ObjectDataInput in) {
        return null;
    }
}