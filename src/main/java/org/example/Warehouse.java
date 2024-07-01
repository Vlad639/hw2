package org.example;

import lombok.extern.log4j.Log4j2;
import org.example.my.list.PolledList;
import org.example.my.workers.Loader;
import org.example.my.workers.Unloader;

import java.util.Collection;
import java.util.List;

@Log4j2
public class Warehouse extends Thread {

    private final PolledList<Block> storage = new PolledList<>();
    private final PolledList<Truck> trucks = new PolledList<>();

    public Warehouse(String name) {
        super(name);
    }

    public Warehouse(String name, Collection<Block> initialStorage) {
        this(name);
        storage.addAll(initialStorage);
    }

    public List<Block> getStorage() {
        return storage.asList();
    }

    @Override
    public void run() {
        Truck truck;
        while (!currentThread().isInterrupted()) {
            truck = getNextArrivedTruck();
            if (truck == null) {
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    if (currentThread().isInterrupted()) {

                        break;
                    }
                }
                continue;
            }
            // Параллельная загрузка/ выгрузка, после выполнения операции через коллбэк уведомляет ждущие грузовики о завершении операции
            Truck finalTruck = truck;
            Runnable callback = () -> finalTruck.setReadyToGo(true);
            if (truck.getBlocks().isEmpty()) {
                new Loader(truck, getFreeBlocks(truck.getCapacity()), callback).start();
            } else {
                new Unloader(truck, storage, callback).start();
            }
        }
        log.info("Warehouse thread interrupted");

    }

    private Collection<Block> getFreeBlocks(int maxItems) {
        return storage.poll(maxItems);
    }

    private Truck getNextArrivedTruck() {
        return trucks.poll();
    }

    public void arrive(Truck truck) {
        trucks.add(truck);

        // Ждём, пока завершится операция по загрузке/разгрузке грузовика
        while (true) {
            if (truck.isReadyToGo()) {
                break;
            }
        }

        truck.setReadyToGo(false);
    }
}
