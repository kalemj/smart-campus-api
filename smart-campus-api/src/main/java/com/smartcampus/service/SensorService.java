package com.smartcampus.service;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SensorService {

    private final DataStore store = DataStore.INSTANCE;

    public List<Sensor> listSensors(String type) {
        if (type == null || type.isBlank()) {
            return new ArrayList<>(store.allSensors());
        }

        List<Sensor> result = new ArrayList<>();
        for (Sensor sensor : store.allSensors()) {
            if (type.equalsIgnoreCase(sensor.getType())) {
                result.add(sensor);
            }
        }
        return result;
    }

    public Sensor getSensor(String sensorId) {
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor not found: " + sensorId);
        }
        return sensor;
    }

    public Sensor createSensor(Sensor sensor) {
        if (sensor.getType() == null || sensor.getType().isBlank()) {
            throw new BadRequestException("Sensor type is required.");
        }
        if (sensor.getRoomId() == null || sensor.getRoomId().isBlank()) {
            throw new BadRequestException("roomId is required.");
        }
        if (store.getRoom(sensor.getRoomId()) == null) {
            throw new LinkedResourceNotFoundException(
                    "roomId",
                    sensor.getRoomId(),
                    "Cannot register sensor because the room does not exist."
            );
        }
        if (sensor.getId() == null || sensor.getId().isBlank()) {
            sensor.setId("SENSOR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
            sensor.setStatus("ACTIVE");
        }
        store.saveSensor(sensor);
        return sensor;
    }
}
