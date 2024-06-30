package org.example.my.workers;

import lombok.extern.log4j.Log4j2;
import org.example.Block;
import org.example.Truck;

import java.util.Collection;

/**
 * Погрузчик ящиков
 */
/*
 *        ________      ||
 *       ||      ||     ||______
 *     _||_______||____ ||     |
 *   /                 \||     |
 *   (*)-----------(*)--||=========
 */
@Log4j2
public class Loader extends AbstractWorker {

    private final Collection<Block> blocksToLoad;
    private final Truck truck;

    public Loader(Truck truck, Collection<Block> blocksToLoad, Runnable callback) {
        super(callback);
        this.truck = truck;
        this.blocksToLoad = blocksToLoad;
    }

    @Override
    void work() {
        log.info("Loading truck {}", truck.getName());
        try {
            sleep(10L * blocksToLoad.size());
        } catch (InterruptedException e) {
            log.error("Interrupted while loading truck", e);
        }
        truck.getBlocks().addAll(blocksToLoad);
        log.info("Truck loaded {}", truck.getName());
    }

}
