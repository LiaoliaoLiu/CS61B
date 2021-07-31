package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    // You should probably define some more!
    private int m;
    private int n = 0;
    private double loadFactor;
    private Set<K> keySet = null;

    /** Constructors */
    public MyHashMap() {
        this(16, 0.75);
    }

    public MyHashMap(int initialSize) {
        this(initialSize, 0.75);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        m = initialSize;
        loadFactor = maxLoad;
        buckets = createTable(m);
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return null;
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new ArrayList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] table = new Collection[tableSize];
        for (int i = 0; i < tableSize; i++) {
            table[i] = createBucket();
        }
        return table;
    }

    private int getIndex(K key) {
        return getIndex(key, m);
    }

    private int getIndex(K key, int dividend) {
        return Math.floorMod(key.hashCode(), dividend);
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!

    /** Removes all of the mappings from this map. */
    @Override
    public void clear() {
        buckets = createTable(m);
        n = 0;
    }

    /** Returns true if this map contains a mapping for the specified key. */
    @Override
    public boolean containsKey(K key) {
        Node x = getNode(key);
        return x != null;
    }

    private Node getNode(K key) {
        Node x = null;
        int i = getIndex(key);
        Collection<Node> bucket = buckets[i];
        if (bucket != null) {
            for (Node elem : bucket) {
                if (elem.key.equals(key)) x = elem;
            }
        }
        return x;
    }
    /**
     * Returns the value to which the specified key is mapped, or null if this
     * map contains no mapping for the key.
     */
    @Override
    public V get(K key) {
        Node x = getNode(key);
        return x == null ? null : x.value;
    }

    /** Returns the number of key-value mappings in this map. */
    @Override
    public int size() {
        return n;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key,
     * the old value is replaced.
     */
    @Override
    public void put(K key, V value) {
        Node x = getNode(key);
        if (x != null) {
            x.value = value;
        } else {
            x = new Node(key, value);
            int i = getIndex(key);
            buckets[i].add(x);
            n++;
            if ((double) n/m > loadFactor) resize();
        }
    }

    private void resize() {
        int newSize = m * 2;
        Collection<Node>[] newTable = createTable(newSize);
        for (K key : this) {
            int i = getIndex(key, newSize);
            newTable[i].add(getNode(key));
        }
        m = newSize;
        buckets = newTable;
    }

    /** Returns a Set view of the keys contained in this map without using a second instance variable to store the set of keys. */
    @Override
    public Set<K> keySet() {
        return keySet == null ? (keySet = new KeySet()) : keySet;
    }

    final class KeySet extends AbstractSet<K> {
        public int size() { return m; }

        public void clear() { MyHashMap.this.clear(); }

        public Iterator<K> iterator() { return new KeyIterator(); }
    }

    @Override
    public Iterator<K> iterator() {
        return new KeyIterator();
    }

    private class KeyIterator implements Iterator<K> {
        Iterator<Node> mapIter;

        public KeyIterator() {
            mapIter = new MapIterator();
        }

        @Override
        public boolean hasNext() {
            return mapIter.hasNext();
        }

        @Override
        public K next() {
            return mapIter.next().key;
        }

    }

    private class MapIterator implements Iterator<Node>{
    Node next;
    Node current;
    Iterator<Node> bucketIter;
    int index = 0;

    public MapIterator() {
        next = current = null;
        findNextEntry();
    }

    private void findNextEntry() {
        while (index < buckets.length && buckets[index].isEmpty()) {index++;}
        if (index < buckets.length) {
            bucketIter = buckets[index].iterator();
            next = bucketIter.next();
            index++;
        } else {
            next = null;
        }
    }

        public boolean hasNext() {
        return next != null;
    }

    public Node next() {
        current = next;

        if (bucketIter.hasNext()) {
            next = bucketIter.next();
        } else {
            findNextEntry();
        }

        return current;
        }
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     * Not required for Lab 8. If you don't implement this, throw an
     * UnsupportedOperationException.
     */
    @Override
    public V remove(K key) {
        Node x = getNode(key);
        if (x != null) {
            int i = getIndex(key);
            buckets[i].remove(x);
            n--;
            V returnVal = x.value;
            return returnVal;
        }
        return null;
    }

    /**
     * Removes the entry for the specified key only if it is currently mapped to
     * the specified value. Not required for Lab 8. If you don't implement this,
     * throw an UnsupportedOperationException.
     */
    public V remove(K key, V value) {
        if (get(key) == value) return remove(key);
        return null;
    }
}
