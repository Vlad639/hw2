package org.example.my.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Потокобезопасная (вроде-бы) структура данных для задания. Представляет собой
 * двусвязный список, но с возможность поллинга указанного количества значений. По сути очередь.
 * <p>
 * Такая структура данных на мой взгляд вписывается в концепцию задания,
 * её можно использовать и как очередь (грузовики по очереди заезжают на склад) и как список (для хранения ящиков на самом складе)
 *
 * @param <T> тип хранимых значений
 */
public class PolledList<T> {

    private final Node<T> head = new Node<>(null);
    private final Node<T> tail = new Node<>(null);

    public void addAll(Collection<T> collection) {
        collection.forEach(this::add);
    }

    /**
     * Добавить элемент. Принцип работы следующий:
     * Блокируем хвост и двигаемся от него влево до первого свободного элемента,
     * если нашли элемент - меняем ссылки на следующие/ предыдущие узлы
     *
     * @param value добавляемое значение
     */
    public void add(T value) {
        if (value == null) {
            throw new IllegalArgumentException("Added value is null!");
        }

        while (true) {
            if (head.tryLock()) {
                try {
                    if (head.getNext() == null) {
                        new Node<>(head, value, tail);
                        return;
                    }
                } finally {
                    head.unlock();
                }
            }

            if (tail.tryLock()) {
                try {
                    Node<T> current = tail.getPrev();
                    while (current != null) {
                        if (!current.isMarked()) {
                            new Node<>(current, value, tail);
                            return;
                        }
                        current = current.getPrev();
                    }

                } finally {
                    tail.unlock();
                }
            }
        }

    }

    /**
     * Получить первый свободный элемент
     * @return первый свободный элемент или null
     */
    public T poll() {
        List<T> list = poll(1);
        if (list.isEmpty()) {
            return null;
        }

        return list.get(0);

    }

    /**
     * Поллинг указанного количества элементов. Принцип работы следующий:
     * Блокируем голову списка, двигаемся в право, игнорируя узлы, помеченыне меткой и добавляя
     * свободные элементы в результирующий список (при этом меняем ссылки на следующие/
     * предыдущие узлы иначе говоря удаляем споленные элементы)
     *
     * @param max макс. кол-во возвращаемых элементов
     * @return список размером <= max элементов, если элементов не найдено - список нулевой длины
     */
    public List<T> poll(int max) {
        if (max <= 0) {
            throw new IllegalArgumentException("max must be greater than 0");
        }
        List<T> polled = new ArrayList<>();

        while (true) {
            if (head.tryLock()) {
                try {
                    if (head.getNext() == null) {
                        return polled;
                    }

                    Node<T> current = head.getNext();
                    while (current != null) {
                        if (!current.isMarked()) {
                            current.mark();
                            polled.add(current.getValue());

                            current.setPrev(head);
                            head.setNext(current);

                            break;
                        }
                        current = current.getNext();
                        if (current == tail || polled.size() >= max) {
                            return polled;
                        }

                    }
                } finally {
                    head.unlock();
                }
            }
        }
    }

    /**
     * Получить копию в виде списка. Принцип работы следующий:
     * Начиная с головы двигаемся вправо, пропуская помеченные элементы,
     * найденные же элементы добавляем в результирующий список.
     *
     * @return копия текущего состояния в виде списка
     */
    public List<T> asList() {
        List<T> list = new ArrayList<>();
        while (true) {
            if (head.tryLock()) {
                try {
                    Node<T> current = head.getNext();
                    while (current != null) {
                        if (!current.isMarked()) {
                            list.add(current.getValue());
                        }
                        current = current.getNext();
                        if (current == tail) {
                            return list;
                        }
                    }
                } finally {
                    head.unlock();
                }
            }
        }

    }


    public void clear() {
        resetNode(head);
        resetNode(tail);
    }

    private void resetNode(Node<T> node) {
        node.setPrev(null);
        node.setNext(null);
    }
}
