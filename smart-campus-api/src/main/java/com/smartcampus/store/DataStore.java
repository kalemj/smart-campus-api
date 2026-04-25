package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataStore {

    public static final DataStore INSTANCE = new DataStore();

    private final Map<String, Room> rooms = new HashMap<>();
    private final Map<String, Sensor> sensors = new HashMap<>();
    private final Map<String, List<SensorReading>> readings = new HashMap<>();

    private DataStore() {
        seed();
    }

    public synchronized Collection<Room> allRooms() {
        return new ArrayList<>(rooms.values());
    }

    public synchronized Room getRoom(String id) {
        return rooms.get(id);
    }

    public synchronized Room saveRoom(Room room) {
        rooms.put(room.getId(), room);
        return room;
    }

    public synchronized Room removeRoom(String id) {
        return rooms.remove(id);
    }

    public synchronized Collection<Sensor> allSensors() {
        return new ArrayList<>(sensors.values());
    }

    public synchronized Sensor getSensor(String id) {
        return sensors.get(id);
    }

    public synchronized Sensor saveSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
        Room parent = rooms.get(sensor.getRoomId());
        if (parent != null && !parent.getSensorIds().contains(sensor.getId())) {
            parent.getSensorIds().add(sensor.getId());
        }
        return sensor;
    }

    public synchronized List<SensorReading> getReadings(String sensorId) {
        List<SensorReading> list = readings.get(sensorId);
        if (list == null) {
            list = new ArrayList<>();
            readings.put(sensorId, list);
        }
        return new ArrayList<>(list);
    }

    public synchronized SensorReading addReading(String sensorId, SensorReading reading) {
        List<SensorReading> list = readings.get(sensorId);
        if (list == null) {
            list = new ArrayList<>();
            readings.put(sensorId, list);
        }
        list.add(reading);
        return reading;
    }

    public synchronized void reset() {
        rooms.clear();
        sensors.clear();
        readings.clear();
        seed();
    }

    private void seed() {
        Room library = new Room("LIB-301", "Library Quiet Study", 40);
        Room lab = new Room("LAB-110", "Computer Science Lab", 30);
        rooms.put(library.getId(), library);
        rooms.put(lab.getId(), lab);

        Sensor temp = new Sensor("TEMP-001", "Temperature", "ACTIVE", 21.4, library.getId());
        Sensor co2 = new Sensor("CO2-014", "CO2", "ACTIVE", 412.0, library.getId());
        Sensor occ = new Sensor("OCC-007", "Occupancy", "MAINTENANCE", 0.0, lab.getId());
        saveSensor(temp);
        saveSensor(co2);
        saveSensor(occ);
    }
}
