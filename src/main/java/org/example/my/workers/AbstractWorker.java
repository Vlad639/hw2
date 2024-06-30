package org.example.my.workers;

/**
 * Worker для операций загрузки/ разгрузки с коллбэком, через который уведомляет об окончании оперции.
 */
public abstract class AbstractWorker extends Thread {

    private final Runnable callback;

    public AbstractWorker(Runnable callback) {
        this.callback = callback;
    }

    @Override
    public void run() {
        work();
        callback.run();
    }

    abstract void work();
}
