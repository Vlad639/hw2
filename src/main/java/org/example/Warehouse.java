package org.example;

import lombok.extern.log4j.Log4j2;
import org.example.my.list.PolledList;
import org.example.my.workers.Loader;
import org.example.my.workers.Unloader;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class Warehouse extends Thread {

    private final PolledList<Block> storage = new PolledList<>();
    private final PolledList<Truck> trucks = new PolledList<>();
    private final Map<Long, Boolean> trucksStatus = new HashMap<>(); // true - грузовик загружен/ разгружен, false - ещё в процессе

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
            long threadId = truck.getId();
            Runnable callback = () -> trucksStatus.put(threadId, true);
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
        long threadId = truck.getId();

        trucks.add(truck);
        trucksStatus.put(threadId, false);

        // Ждём, пока завершится операция по загрузке/разгрузке грузовика
        while (true) {
            if (trucksStatus.getOrDefault(threadId, false)) {
                trucksStatus.remove(threadId);
                break;
            }

        }
    }
}
