package com.lingh.identifiedDataSerializable;

import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class SampleDataSerializableFactory implements DataSerializableFactory {
    public static final int FACTORY_ID = 1000;

    @Override
    public IdentifiedDataSerializable create(int typeId) {
        return typeId == 100 ? new Employee() : null;
    }
}