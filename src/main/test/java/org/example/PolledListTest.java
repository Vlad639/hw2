package org.example;

import lombok.extern.log4j.Log4j2;
import org.example.my.list.PolledList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
public class PolledListTest {

    private final PolledList<Integer> polledList = new PolledList<>();

    @AfterEach
    public void after() {
        polledList.clear();
    }

    @Test
    void testAddAndPoll() {
        polledList.add(1);
        polledList.add(2);
        polledList.add(3);
        polledList.add(4);
        polledList.add(5);

        List<Integer> actual = polledList.poll(3);
        assertArrayEquals(new Integer[]{1, 2, 3}, actual.toArray());

        actual = polledList.poll(2);
        assertArrayEquals(new Integer[]{4, 5}, actual.toArray());

        actual = polledList.poll(Integer.MAX_VALUE);
        assertEquals(0, actual.size());
    }

    @Test
    public void testAddAndPollInSingeThread() {
        int size = 100_000;
        List<Integer> expected = IntStream.range(0, size).boxed().collect(Collectors.toList());
        polledList.addAll(expected);

        List<Integer> actual = polledList.poll(size);
        Collections.sort(actual);
        assertArrayEquals(expected.toArray(), actual.toArray());
    }

    @Test
    public void testAddMultiThread() throws InterruptedException {
        int size = 100_000;
        Object[] expected = IntStream.range(0, size).boxed().toArray();

        List<TestProducer<Integer>> producers = new ArrayList<>();
        producers.add(createProducer(0, 25000));
        producers.add(createProducer(25001, 50000));
        producers.add(createProducer(50001, 75000));
        producers.add(createProducer(75001, 99999));

        producers.forEach(TestProducer::start);
        Thread.sleep(1500);

        List<Integer> actual = polledList.asList();
        Collections.sort(actual);
        assertArrayEquals(expected, actual.toArray());
    }

    @Test
    public void testPollInMultiThread() throws InterruptedException {
        int size = 100_000;
        List<Integer> expected = IntStream.range(0, size).boxed().collect(Collectors.toList());
        polledList.addAll(expected);

        List<Integer> actual = new ArrayList<>();
        int consumersCount = 3;
        List<TestConsumer<Integer>> consumers = new ArrayList<>();
        for (int i = 0; i < consumersCount; i++) {
            TestConsumer<Integer> consumer = new TestConsumer<>(polledList, Integer.MAX_VALUE);
            consumers.add(consumer);
        }

        consumers.forEach(TestConsumer::start);
        Thread.sleep(1500);

        for (TestConsumer<Integer> consumer : consumers) {
            List<Integer> consumed = consumer.getConsumed();
            log.info("Consumer {} consume {} values", consumer, consumed.size());
            actual.addAll(consumed);
        }

        Collections.sort(actual);
        assertArrayEquals(expected.toArray(), actual.toArray());

        long workingConsumers = consumers.stream().filter(consumer -> consumer.getConsumed().size() > 0).count();
        assertTrue(workingConsumers >= 2); // Проверяем, что действительно отработало несколько консюмеров, а не один
    }


    private TestProducer<Integer> createProducer(int rangeStart, int rangeFinish) {
        return new TestProducer<>(polledList, IntStream.rangeClosed(rangeStart, rangeFinish).boxed().collect(Collectors.toList()));
    }
}
