package iotplatform.androidapp.utils;

/**
 * Created by ioan.vranau on 10/4/2016.
 */

public enum SensorType {
    TORCH("torch"),
    ACC("acc");

    private final String value;

    SensorType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
