package org.example;

import org.example.my.list.PolledList;

import java.util.ArrayList;
import java.util.List;

public class TestConsumer<T> extends Thread {

    private List<T> consumed = new ArrayList<>();
    private final PolledList<T> source;
    private final int max;

    public TestConsumer(PolledList<T> source, int max) {
        this.source = source;
        this.max = max;
    }


    @Override
    public void run() {
        consumed = source.poll(max);
    }

    public List<T> getConsumed() {
        return consumed;
    }
}
