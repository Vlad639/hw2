package org.example;

import org.example.my.list.PolledList;

import java.util.List;

public class TestProducer<T> extends Thread {

    private final PolledList<T> target;
    private final List<T> produced;

    public TestProducer(PolledList<T> target, List<T> producedValues) {
        this.target = target;
        this.produced = producedValues;
    }

    @Override
    public void run() {
        for (T value: produced) {
            target.add(value);
        }
    }
}
