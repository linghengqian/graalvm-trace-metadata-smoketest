package com.lingh.jsonstreaming;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record DataPoint(long timestamp, double value) {

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public DataPoint(@JsonProperty("ts") long timestamp, @JsonProperty("val") double value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    @Override
    @JsonProperty("ts")
    public long timestamp() {
        return timestamp;
    }

    @Override
    @JsonProperty("val")
    public double value() {
        return value;
    }

    @Override
    public String toString() {
        return "DataPoint{" + "timestamp=" + timestamp + ", value=" + value + '}';
    }
}
