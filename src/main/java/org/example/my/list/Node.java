package org.example.my.list;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Node<T> {
    private final T value;
    private Node<T> next;
    private Node<T> prev;
    private volatile boolean mark;
    private final Lock lock = new ReentrantLock();

    Node(T value) {
        this.value = value;
    }

    public Node(Node<T> prev, T value, Node<T> next) {
        this.prev = prev;
        if (prev != null) {
            this.prev.setNext(this);
        }

        this.value = value;

        this.next = next;
        if (next != null) {
            this.next.setPrev(this);
        }
    }

    T getValue() {
        return value;
    }

    Node<T> getNext() {
        return next;
    }

    void setNext(Node<T> next) {
        this.next = next;
    }

    Node<T> getPrev() {
        return prev;
    }

    void setPrev(Node<T> prev) {
        this.prev = prev;
    }


    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isMarked() {
        return mark;
    }

    void mark() {
        this.mark = true;
    }

    boolean tryLock() {
        return lock.tryLock();
    }

    void unlock() {
        lock.unlock();
    }
}
