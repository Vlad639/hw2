package org.example;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Log4j2
@Getter
public class Truck extends Thread {

    private final int capacity;

    private final List<Block> blocks;

    private volatile boolean isReadyToGo;

    private final Queue<Warehouse> routeList = new LinkedList<>();

    private final long travelTime = Math.round(Math.random() * 2000);


    public Truck(String name, int capacity, List<Warehouse> route) {
        super(name);
        this.capacity = capacity;
        this.blocks = new ArrayList<>(capacity);
        this.routeList.addAll(route);
    }

    /**
     * Логика работы грузовика:
     * Пока есть точки в маршруте, берем следующую точку.
     * Ожидаем время travelTime и запускаем процедуру прибытия на склад.
     */
    @Override
    public void run() {
        log.info("Truck started");
        while (!routeList.isEmpty()) {
            Warehouse warehouse = routeList.poll();
            travel(warehouse);
            warehouse.arrive(this);
        }
        log.info("Truck finished");
    }

    private void travel(Warehouse warehouse) {
        log.info("Traveling to warehouse: {}", warehouse.getName());
        try {
            Thread.sleep(travelTime);
        } catch (InterruptedException e) {
            log.error(e);
        }
        log.info("Arrived to warehouse: {}", warehouse.getName());
    }

    public void setReadyToGo(boolean readyToGo) {
        isReadyToGo = readyToGo;
    }
}
