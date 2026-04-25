package com.smartcampus.service;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;

import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.UUID;

public class SensorReadingService {

    private final DataStore store = DataStore.INSTANCE;

    public List<SensorReading> getHistory(String sensorId) {
        if (store.getSensor(sensorId) == null) {
            throw new NotFoundException("Sensor not found: " + sensorId);
        }
        return store.getReadings(sensorId);
    }

    public SensorReading addReading(String sensorId, SensorReading reading) {
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor not found: " + sensorId);
        }
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId, sensor.getStatus());
        }
        if (reading.getId() == null || reading.getId().isBlank()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0L) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        store.addReading(sensorId, reading);
        sensor.setCurrentValue(reading.getValue());
        return reading;
    }
}
