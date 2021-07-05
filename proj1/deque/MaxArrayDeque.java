package deque;

import java.util.Comparator;
import java.util.Iterator;

public class MaxArrayDeque<T> extends ArrayDeque implements Iterable<T> {
    Comparator<T> maxComparator;

    public MaxArrayDeque(Comparator<T> c) {
        maxComparator = c;
    }

    public T max(Comparator<T> c) {
        if (isEmpty()) {
            return null;
        }
        T maxItem = (T) this.get(0);
        for (T i : this) {
            if (c.compare(maxItem, i) < 0) {
                maxItem = i;
            }
        }
        return maxItem;
    }

    public T max() {
        return max(maxComparator);
    }

    public Iterator<T> iterator() {
        return new ArrayIterator();
    }

    private class ArrayIterator implements Iterator<T> {
        int pos;

        ArrayIterator() {
            pos = 0;
        }

        public boolean hasNext() {
            return pos < size();
        }

        public T next() {
            T returnItem = (T) get(pos);
            ++pos;
            return returnItem;
        }
    }
}
