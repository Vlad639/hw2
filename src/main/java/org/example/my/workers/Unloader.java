package org.example.my.workers;

import lombok.extern.log4j.Log4j2;
import org.example.Block;
import org.example.Truck;
import org.example.my.list.PolledList;

import java.util.List;

/**
 * Разгрузчик ящиков
 *
 */
@Log4j2
public class Unloader extends AbstractWorker {

    private final Truck truck;
    private final PolledList<Block> storage;

    public Unloader(Truck truck, PolledList<Block> storage, Runnable callback) {
        super(callback);
        this.truck = truck;
        this.storage = storage;
    }

    @Override
    void work() {
        log.info("Unloading truck {}", truck.getName());
        List<Block> arrivedBlocks = truck.getBlocks();
        try {
            sleep(100L * arrivedBlocks.size());
        } catch (InterruptedException e) {
            log.error("Interrupted while unloading truck", e);
        }
        returnBlocksToStorage(arrivedBlocks);
        truck.getBlocks().clear();
        log.info("Truck unloaded {}", truck.getName());
    }

    private void returnBlocksToStorage(List<Block> returnedBlocks) {
       storage.addAll(returnedBlocks);
    }
}
