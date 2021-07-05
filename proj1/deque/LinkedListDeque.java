package deque;

public class LinkedListDeque<T> {
    private class StuffNode {
        T item;
        StuffNode next;
        StuffNode prev;

        /**
         * create a dual linked node for element i
         */
        StuffNode(T i) {
            item = i;
            next = prev = null;
        }

        StuffNode(T i, StuffNode p, StuffNode n) {
            item = i;
            next = n;
            prev = p;
        }
    }

    private StuffNode sentinel;
    private int size;

    /**
     * create an empty deque.
     */
    public LinkedListDeque() {
        sentinel = new StuffNode(null);
        sentinel.next = sentinel.prev = sentinel;
        size = 0;
    }

    /**
     * Adds x to the front of the deque.
     */
    public void addFirst(T x) {
        StuffNode xNode = new StuffNode(x, sentinel, sentinel.next);
        sentinel.next.prev = xNode;
        sentinel.next = xNode;
        size += 1;
    }

    /**
     * Adds an item to the end of the deque.
     */
    public void addLast(T x) {
        StuffNode xNode = new StuffNode(x, sentinel.prev, sentinel);
        sentinel.prev.next = xNode;
        sentinel.prev = xNode;
        size += 1;
    }

    /**
     * Returns true if deque is empty, false otherwise.
     */
    public boolean isEmpty() {
        return sentinel.next == sentinel;
    }

    /**
     * Returns the number of items in the deque.
     */
    public int size() {
        return size;
    }

    /**
     * Prints the items in the deque from first to last, separated by a space.
     * Once all the items have been printed, print out a new line.
     */
    public void printDeque() {
        StuffNode p = sentinel.next;
        while (p != sentinel) {
            if (p.next == sentinel) {
                System.out.print(p.item.toString());
                System.out.println();
            }
            System.out.print(p.item.toString() + " ");
            p = p.next;
        }
    }

    /**
     * Removes and returns the item at the front of the deque.
     */
    public T removeFirst() {
        if (sentinel.next == sentinel) {
            return null;
        }
        T item = sentinel.next.item;
        sentinel.next = sentinel.next.next;
        sentinel.next.prev = sentinel;
        --size;
        return item;
    }

    /**
     * Removes and returns the item at the back of the deque.
     */
    public T removeLast() {
        if (sentinel.prev == sentinel) {
            return null;
        }
        T item = sentinel.prev.item;
        sentinel.prev = sentinel.prev.prev;
        sentinel.prev.next = sentinel;
        --size;
        return item;
    }

    /**
     * Gets the item at the given index, where 0 is the front, 1 is the next item, and so forth.
     * If no such item exists, returns null.
     */
    public T get(int index) {
        StuffNode p = sentinel.next;
        while (index > 0) {
            p = p.next;
            if (p == sentinel) {
                return null;
            }
            --index;
        }
        return p.item;
    }

    public T getRecursive(int index) {
        return getRecursive(sentinel.next, index);
    }

    private T getRecursive(StuffNode p, int index) {
        if (p == sentinel) {
            return null;
        } else if (index == 0) {
            return p.item;
        } else {
            return getRecursive(p.next, --index);
        }
    }
}
