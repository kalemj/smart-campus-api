package com.smartcampus.exception;

public class SensorUnavailableException extends RuntimeException {

    private final String sensorId;
    private final String sensorStatus;

    public SensorUnavailableException(String sensorId, String sensorStatus) {
        super("Sensor '" + sensorId + "' is currently in '" + sensorStatus
                + "' state and cannot accept new readings.");
        this.sensorId = sensorId;
        this.sensorStatus = sensorStatus;
    }

    public String getSensorId() { return sensorId; }
    public String getSensorStatus() { return sensorStatus; }
}
