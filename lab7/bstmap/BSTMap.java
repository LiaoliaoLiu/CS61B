package bstmap;

import java.util.*;

public class BSTMap<K extends Comparable, V> implements Map61B<K, V> {
    private BSTNode root;
    private int size;

    private class BSTNode<K, V> {
        private K key;
        private V val;
        private BSTNode left, right;

        public BSTNode(K key, V val) {
            this.key = key;
            this.val = val;
        }
    }
    /** Creates a empty BSTMap. */
    public BSTMap() {
        this.root = null;
        this.size = 0;
    }

    /** Removes all of the mappings from this map. */
    @Override
    public void clear() {
        root = null;
        size = 0;
    }


    /** Returns true if and only if this dictionary contains KEY as the
     *  key of some key-value pair. */
    @Override
    public boolean containsKey(K key) {
        return containsKey(key, root);
    }

    private boolean containsKey(K key, BSTNode node) {
        if (node == null) {
            return false;
        }
        int cmp = key.compareTo(node.key);
        if (cmp == 0) {
            return true;
        } else if (cmp < 0) {
            return containsKey(key, node.left);
        } else {
            return containsKey(key, node.right);
        }
    }

    /** Returns the value corresponding to KEY or null if no such value exists. */
    @Override
    public V get(K key) {
        BSTNode returnNode = get(key, root);
        if (returnNode == null || returnNode.val == null) return null;
        else return (V) returnNode.val;
    }

    private BSTNode get(K key, BSTNode node) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp == 0) {
            return node;
        } else if (cmp < 0) {
            return get(key, node.left);
        } else {
            return get(key, node.right);
        }
    }

    @Override
    public int size() {
        return this.size;
    }

    /** Inserts the key-value pair of KEY and VALUE into this dictionary,
     *  replacing the previous value associated to KEY, if any. */
    @Override
    public void put(K key, V val) {
        root = put(key, val, root);
    }

    private BSTNode put(K key, V val, BSTNode node) {
        if (node == null) {
            this.size++;
            return new BSTNode(key, val);
        }
        int cmp = key.compareTo(node.key);
        if (cmp == 0) {
            node.val = val;
        } else if (cmp < 0) {
            node.left = put(key, val, node.left);
        } else {
            node. right = put(key, val, node.right);
        }
        return node;
    }

    /** Returns a Set view of the keys contained in this map. */
    @Override
    public Set<K> keySet() {
        Set<K> keys = new HashSet<>();
        for (K k : this) {
            keys.add(k);
        }
        return keys;
    }

    /** Removes the mapping for the specified key from this map if present. */
    @Override
    public V remove(K key) {
        V returnKey = get(key);
        root = remove(key, root);
        return returnKey;
    }

    private BSTNode remove(K key, BSTNode root) {
        if (root == null) return null;

        int cmp = key.compareTo(root.key);
        if (cmp < 0) root.left = remove(key, root.left);
        else if (cmp > 0) root.right = remove(key, root.right);
        else {
            this.size --;
            if (root.left == null) return root.right;
            else if (root.right == null) return root.left;
            else root = hibbardDeletion(root.right, root);
        }

        return root;
    }
    /* An temptation to write deletion iteratively
    1. You need a special case for root deletion
    2. You need two pointers: one for the node to be deleted (prob), another for its parent.
    3. You need a flag to trace where the prob goes (left or right).
    4. Check empty BST -> Check if it's the 'root' deletion -> Traverse the tree with its parent
    public V remove(K key) {
        if (root == null) return null;

        V returnVal;
        if (key.compareTo(root.key) == 0) {
            returnVal = (V) root.val;
            root = rootDeletion(root);
            this.size--;
            return returnVal;
        }

        BSTNode prob, parent;
        prob = parent = root;

        boolean probOnLeft = true;
        for (int cmp = key.compareTo(prob.key); cmp != 0; cmp = key.compareTo(prob.key)) {
            parent = prob;
            if (cmp < 0) {
                prob = prob.left;
                probOnLeft = true;
            }
            else if (cmp > 0) {
                prob = prob.right;
                probOnLeft = false;
            }
            if (prob == null) return null;
        }

        returnVal = (V) prob.val;
        if (probOnLeft) parent.left = rootDeletion(prob);
        else parent.right = rootDeletion(prob);
        this.size--;
        return returnVal;
    }

    private BSTNode rootDeletion (BSTNode root) {
        if (root.left == null) return root.right;
        else if (root.right == null) return root.left;
        else {
           root = hibbardDeletion(root.right, root);
        }
        return root;
    }
    */
    private BSTNode hibbardDeletion(BSTNode x, BSTNode xParent) {
        boolean sideChanged = false;
        while (x.left != null) {
            xParent = x;
            x = x.left;
            sideChanged = true;
        }
        if (sideChanged) xParent.left = x.right;
        else xParent.right = x.right;
        xParent.val = x.val;
        xParent.key = x.key;
        return xParent;
    }

    /* Removes the entry for the specified key only if it is currently mapped to
     * the specified value. Not required for Lab 7. If you don't implement this,
     * throw an UnsupportedOperationException.*/
    public V remove(K key, V value) {
        if (get(key) != value) return null;
        return remove(key);
    }

    @Override
    public Iterator<K> iterator() {
        return new BSTIterator();
    }

    /** An iterator that iterates over the keys of the dictionary. */
    private class BSTIterator implements Iterator<K> {
        Queue<K> keys = new LinkedList<>();
        Queue<BSTNode> nodes = new LinkedList<>();

        public BSTIterator() {
            nodes.add(root);
            while (!nodes.isEmpty()) {
                BSTNode tmp = nodes.remove();
                if (tmp == null) continue;
                keys.add((K) tmp.key);
                nodes.add(tmp.left);
                nodes.add(tmp.right);
            }
        }

        @Override
        public boolean hasNext() {
            return !keys.isEmpty();
        }

        @Override
        public K next() {
            return keys.remove();
        }
    }

}
