package com.lingh.jsonstreaming;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DataPoint {

    private final long timestamp;
    private final double value;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public DataPoint(@JsonProperty("ts") long timestamp, @JsonProperty("val") double value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    @JsonProperty("ts")
    public long getTimestamp() {
        return timestamp;
    }

    @JsonProperty("val")
    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "DataPoint{" + "timestamp=" + timestamp + ", value=" + value + '}';
    }
}