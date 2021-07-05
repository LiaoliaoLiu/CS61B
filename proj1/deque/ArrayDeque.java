package deque;

public class ArrayDeque<T> {
    private T[] items;
    private int size;
    private int nextFirst;
    private int nextLast;

    private final double loiteringRatio = 0.25;

    /**
     * Creates an empty array deque.
     */
    public ArrayDeque() {
        items = (T[]) new Object[8];
        size = 0;
        nextFirst = 3;
        nextLast = 4;
    }

    private boolean isFull() {
        return size == items.length;
    }

    private int getClockwise(int pos) {
        return getClockwise(pos, 1);
    }

    private int getClockwise(int pos, int offset) {
        return (pos + offset) % items.length;
    }

    private int getAntiClockwise(int pos) {
        return getAntiClockwise(pos, 1);
    }

    private int getAntiClockwise(int pos, int offset) {
        return (pos + items.length - offset) % items.length;
    }

    private void resize() {
        int newSize = size < 8 ? 8 : size * 2;
        T[] newArray = (T[]) new Object[newSize];
        // items copy
        for (int i = 0; i < size; ++i) {
            newArray[i] = get(i);
        }
        // align invariant
        nextFirst = newArray.length - 1;
        nextLast = size;
        items = newArray;
    }

    private boolean isLoitering() {
        double usageRatio = (float) size / items.length;
        return usageRatio <= loiteringRatio;
    }

    /**
     * Adds an item of type T to the front of the deque.
     */
    public void addFirst(T x) {
        if (isFull()) {
            resize();
        }
        items[nextFirst] = x;
        nextFirst = getAntiClockwise(nextFirst);
        ++size;
    }

    /**
     * Adds an item of type T to the back of the deque.
     */
    public void addLast(T x) {
        if (isFull()) {
            resize();
        }
        items[nextLast] = x;
        nextLast = getClockwise(nextLast);
        ++size;
    }

    /**
     * Returns true if deque is empty, false otherwise.
     */
    public boolean isEmpty() {
        return size == 0;
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
        for (int i = getClockwise(nextFirst); i <= getAntiClockwise(nextLast); ++i) {
            if (i == getAntiClockwise(nextLast)) {
                System.out.print(items[i].toString());
                System.out.println();
            }
            System.out.print(items[i].toString() + " ");
        }
    }

    /**
     * Removes and returns the item at the front of the deque. If no such item exists, returns null.
     */
    public T removeFirst() {
        return removeHelper(true);
    }

    private T removeHelper(boolean first) {
        if (size == 0) {
            return null;
        }

        int target;
        if (first) {
            nextFirst = getClockwise(nextFirst);
            target = nextFirst;
        } else {
            nextLast = getAntiClockwise(nextLast);
            target = nextLast;
        }
        T returnItem = items[target];
        items[target] = null;
        --size;

        if (isLoitering()) {
            resize();
        }

        return returnItem;
    }

    /**
     * Removes and returns the item at the back of the deque. If no such item exists, returns null.
     */
    public T removeLast() {
        return removeHelper(false);
    }

    /**
     * Gets the item at the given index, where 0 is the front, 1 is the next item, and so forth.
     * If no such item exists, returns null.
     */
    public T get(int index) {
        return items[getClockwise(nextFirst, index + 1)];
    }
}
