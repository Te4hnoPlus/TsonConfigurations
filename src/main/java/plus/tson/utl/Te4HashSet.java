package plus.tson.utl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;


public class Te4HashSet<K> extends AbstractSet<K> implements Set<K> {
    private static final Object PRESENT = new Object();
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;
    static final int MAXIMUM_CAPACITY = 1 << 30;
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    static final int UNTREEIFY_THRESHOLD = 6;
    static final int MIN_TREEIFY_CAPACITY = 64;

    private static class Node<K>{
        final int hash;
        final K key;
        Object value;
        Node<K> next;

        Node(int hash, K key, Object value, Node<K> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public final String toString() {return key.toString();}

        public final int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Node) {
                Node<?> e = (Node<?>)o;
                return Objects.equals(key, e.key) && value == e.value;
            }
            return false;
        }
    }


    protected int hash(Object key) {
        int h;
        return (h = key.hashCode()) ^ (h >>> 16);
    }


    static Class<?> comparableClassFor(Object x) {
        if (x instanceof Comparable) {
            Class<?> c;
            if ((c = x.getClass()) == String.class)
                return c;

            ParameterizedType p;
            Type[] ts = c.getGenericInterfaces(), as;
            for (Type t : ts) {
                if ((t instanceof ParameterizedType) &&
                        ((p = (ParameterizedType) t).getRawType() ==
                                Comparable.class) &&
                        (as = p.getActualTypeArguments()) != null &&
                        as.length == 1 && as[0] == c)
                    return c;
            }
        }
        return null;
    }

    @SuppressWarnings({"rawtypes","unchecked"})
    static int compareComparables(Class<?> kc, Object k, Object x) {
        return (x == null || x.getClass() != kc ? 0 :
                ((Comparable)k).compareTo(x));
    }

    static int tableSizeFor(int cap) {
        int n = -1 >>> Integer.numberOfLeadingZeros(cap - 1);
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    private Node<K>[] table;
    private int size;
    protected int modCount;
    private int threshold;
    private final float loadFactor;

    public Te4HashSet(int initialCapacity, float loadFactor) {
        if (loadFactor <= 0)
            throw new IllegalArgumentException("Illegal load factor: " +
                    loadFactor);
        this.loadFactor = loadFactor;
        this.threshold = tableSizeFor(initialCapacity);
    }


    public Te4HashSet(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }


    public Te4HashSet() {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        table = resize();
    }


    @Override
    public final Iterator<K> iterator() {
        return new KeyIterator();
    }


    @Override
    public final int size() {
        return size;
    }


    @Override
    public final boolean isEmpty() {
        return size == 0;
    }


    @Override
    public final Spliterator<K> spliterator() {
        return new KeySpliterator<>(this, 0, -1, 0, 0);
    }


    @Override
    public boolean contains(Object o) {
        Node<K>[] tab = table; Node<K> first, e; int n, hash; K k;
        if ((n = tab.length) > 0 && (first = tab[(n - 1) & (hash = hash(o))]) != null) {
            if (first.hash == hash && ((k = first.key) == o || (k.equals(o))))
                return true;
            if ((e = first.next) != null) {
                if (first instanceof TreeNode)
                    return ((TreeNode<K>)first).getTreeNode(hash, o) != null;
                do {
                    if (e.hash == hash && ((k = e.key) == o || o.equals(k)))
                        return true;
                } while ((e = e.next) != null);
            }
        }
        return false;
    }


    @Override
    public boolean add(K key) {
        int hash = hash(key);
        Node<K>[] tab = table; Node<K> node; int i;
        if ((node = tab[i = (tab.length - 1) & hash]) == null) {
            tab[i] = new Node<>(hash, key, Te4HashSet.PRESENT, null);
        } else {
            Node<K> e; K k;
            if (node.hash == hash && ((k = node.key) == key || key.equals(k)))
                e = node;
            else if (node instanceof TreeNode)
                e = ((TreeNode<K>)node).putTreeVal(tab, hash, key);
            else {
                for (i = 0; ; ++i) {
                    if ((e = node.next) == null) {
                        node.next = new Node<>(hash, key, Te4HashSet.PRESENT, null);
                        if (i >= 7) // -1 for 1st
                            treeifyBin(tab, hash);
                        break;
                    }
                    if (e.hash == hash && ((k = e.key) == key || key.equals(k)))
                        break;
                    node = e;
                }
            }
            if (e != null) {
                e.value = Te4HashSet.PRESENT;
                return false;
            }
        }
        ++modCount;
        if (++size > threshold)
            resize();
        return true;
    }


    @Override
    public boolean remove(Object key) {
        Node<K> e = removeNode(hash(key), key, true);
        return !(e == null || e.value == null);
    }


    final Node<K>[] resize() {
        Node<K>[] oldTab = table;
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        int oldThr = threshold;
        int newCap, newThr = 0;
        if (oldCap > 0) {
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                    oldCap >= DEFAULT_INITIAL_CAPACITY)
                newThr = oldThr << 1; // double threshold
        }
        else if (oldThr > 0) // initial capacity was placed in threshold
            newCap = oldThr;
        else {               // zero initial threshold signifies using defaults
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        if (newThr == 0) {
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                    (int)ft : Integer.MAX_VALUE);
        }
        threshold = newThr;
        @SuppressWarnings({"unchecked"})
        Node<K>[] newTab = (Node<K>[])new Node[newCap];
        table = newTab;
        if (oldTab != null) {
            for (int j = 0; j < oldCap; ++j) {
                Node<K> e;
                if ((e = oldTab[j]) != null) {
                    oldTab[j] = null;
                    if (e.next == null)
                        newTab[e.hash & (newCap - 1)] = e;
                    else if (e instanceof TreeNode)
                        ((TreeNode<K>)e).split(newTab, j, oldCap);
                    else { // preserve order
                        Node<K> loHead = null, loTail = null;
                        Node<K> hiHead = null, hiTail = null;
                        Node<K> next;
                        do {
                            next = e.next;
                            if ((e.hash & oldCap) == 0) {
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            }
                            else {
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }


    final void treeifyBin(Node<K>[] tab, int hash) {
        int n, index; Node<K> e;
        if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
            resize();
        else if ((e = tab[index = (n - 1) & hash]) != null) {
            TreeNode<K> hd = null, tl = null;
            do {
                TreeNode<K> p = new TreeNode<>(e.hash, e.key, e.value, null);
                if (tl == null)
                    hd = p;
                else {
                    p.prev = tl;
                    tl.next = p;
                }
                tl = p;
            } while ((e = e.next) != null);
            tab[index] = hd;
            hd.treeify(tab);
        }
    }


    protected final Node<K> removeNode(int hash, Object key, boolean movable) {
        Node<K>[] tab; Node<K> p; int n, index;
        if ((tab = table) != null && (n = tab.length) > 0 &&
                (p = tab[index = (n - 1) & hash]) != null) {
            Node<K> node = null, e; K k;
            if (p.hash == hash &&
                    ((k = p.key) == key || (key != null && key.equals(k))))
                node = p;
            else if ((e = p.next) != null) {
                if (p instanceof TreeNode)
                    node = ((TreeNode<K>)p).getTreeNode(hash, key);
                else {
                    do {
                        if (e.hash == hash &&
                                ((k = e.key) == key ||
                                        (key != null && key.equals(k)))) {
                            node = e;
                            break;
                        }
                        p = e;
                    } while ((e = e.next) != null);
                }
            }
            if (node != null) {
                if (node instanceof TreeNode)
                    ((TreeNode<K>)node).removeTreeNode(tab, movable);
                else if (node == p)
                    tab[index] = node.next;
                else
                    p.next = node.next;
                ++modCount;
                --size;
                return node;
            }
        }
        return null;
    }


    @Override
    public void clear() {
        Node<K>[] tab;
        modCount++;
        if ((tab = table) != null && size > 0) {
            size = 0;
            for (int i = 0; i < tab.length; ++i)
                tab[i] = null;
        }
    }


    @Override
    public Object[] toArray() {
        return toArray(new Object[size]);
    }


    @Override
    public <T> T[] toArray(T[] a) {
        Node<K>[] tab;
        int idx = 0;
        if (size > 0 && (tab = table) != null) {
            for (Node<K> e : tab) {
                for (; e != null; e = e.next) {
                    ((Object[]) a)[idx++] = e.key;
                }
            }
        }
        return a;
    }


    @Override
    public void forEach(Consumer<? super K> action) {
        Node<K>[] tab;
        if (action == null)
            throw new NullPointerException();
        if (size > 0 && (tab = table) != null) {
            int mc = modCount;
            for (Node<K> e : tab) {
                for (; e != null; e = e.next)
                    action.accept(e.key);
            }
            if (modCount != mc)
                throw new ConcurrentModificationException();
        }
    }


    @Override
    @SuppressWarnings("unchecked")
    public Object clone() {
        Te4HashSet<K> result;
        try {
            result = (Te4HashSet<K>)super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
        result.reinitialize();
        result.fillOnCopy(this);
        return result;
    }


    private void fillOnCopy(Te4HashSet<K> m) {
        int s = m.size();
        if (s > 0) {
            while (s > threshold && table.length < MAXIMUM_CAPACITY)
                resize();
            for (K e : m) add(e);
        }
    }


    private final class KeyIterator implements Iterator<K>{
        Node<K> next, current;
        int expectedModCount, index;

        KeyIterator() {
            expectedModCount = modCount;
            Node<K>[] t = table;
            index = 0;
            if (t != null && size > 0) {
                do {} while (index < t.length && (next = t[index++]) == null);
            }
        }

        public final boolean hasNext() {
            return next != null;
        }

        final Node<K> nextNode() {
            Node<K> e = next;
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (e == null)
                throw new NoSuchElementException();
            Node<K>[] t;
            if ((next = (current = e).next) == null && (t = table) != null) {
                do {} while (index < t.length && (next = t[index++]) == null);
            }
            return e;
        }

        public final void remove() {
            Node<K> p = current;
            if (p == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            current = null;
            removeNode(p.hash, p.key, false);
            expectedModCount = modCount;
        }

        public K next() { return nextNode().key; }
    }


    static class HashMapSpliterator<K> {
        final Te4HashSet<K> map;
        Node<K> current;
        int index, fence, est, expectedModCount;

        HashMapSpliterator(Te4HashSet<K> m, int origin,
                           int fence, int est,
                           int expectedModCount) {
            this.map = m;
            this.index = origin;
            this.fence = fence;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getFence() { // initialize fence and size on first use
            int hi;
            if ((hi = fence) < 0) {
                Te4HashSet<K> m = map;
                est = m.size;
                expectedModCount = m.modCount;
                Node<K>[] tab = m.table;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            return hi;
        }

        public final long estimateSize() {
            getFence(); // force init
            return est;
        }
    }

    static final class KeySpliterator<K>
            extends HashMapSpliterator<K>
            implements Spliterator<K> {
        KeySpliterator(Te4HashSet<K> m, int origin, int fence, int est,
                       int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public KeySpliterator<K> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null :
                    new KeySpliterator<>(map, lo, index = mid, est >>>= 1,
                            expectedModCount);
        }

        public void forEachRemaining(Consumer<? super K> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            Te4HashSet<K> m = map;
            Node<K>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            else
                mc = expectedModCount;
            if (tab != null && tab.length >= hi &&
                    (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Node<K> p = current;
                current = null;
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        action.accept(p.key);
                        p = p.next;
                    }
                } while (p != null || i < hi);
                if (m.modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Node<K>[] tab = map.table;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        K k = current.key;
                        current = current.next;
                        action.accept(k);
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0) |
                    Spliterator.DISTINCT;
        }
    }

    /**
     * Reset to initial default state.  Called by clone and readObject.
     */
    void reinitialize() {
        table = null;
        table = resize();
        modCount = 0;
        threshold = 0;
        size = 0;
    }


    private static final class TreeNode<K> extends Node<K> {
        private TreeNode<K> parent, left, right, prev;
        private boolean red;
        TreeNode(int hash, K key, Object val, Node<K> next) {
            super(hash, key, val, next);
        }

        private TreeNode<K> root() {
            for (TreeNode<K> r = this, p;;) {
                if ((p = r.parent) == null)
                    return r;
                r = p;
            }
        }

        private static <K> void moveRootToFront(Node<K>[] tab, TreeNode<K> root) {
            int n;
            if (root != null && tab != null && (n = tab.length) > 0) {
                int index = (n - 1) & root.hash;
                TreeNode<K> first = (TreeNode<K>)tab[index];
                if (root != first) {
                    Node<K> rn;
                    tab[index] = root;
                    TreeNode<K> rp = root.prev;
                    if ((rn = root.next) != null)
                        ((TreeNode<K>)rn).prev = rp;
                    if (rp != null)
                        rp.next = rn;
                    if (first != null)
                        first.prev = root;
                    root.next = first;
                    root.prev = null;
                }
            }
        }


        private TreeNode<K> find(int h, Object k, Class<?> kc) {
            TreeNode<K> p = this;
            do {
                int ph, dir; K pk;
                TreeNode<K> pl = p.left, pr = p.right, q;
                if ((ph = p.hash) > h)
                    p = pl;
                else if (ph < h)
                    p = pr;
                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                    return p;
                else if (pl == null)
                    p = pr;
                else if (pr == null)
                    p = pl;
                else if ((kc != null ||
                        (kc = comparableClassFor(k)) != null) &&
                        (dir = compareComparables(kc, k, pk)) != 0)
                    p = (dir < 0) ? pl : pr;
                else if ((q = pr.find(h, k, kc)) != null)
                    return q;
                else
                    p = pl;
            } while (p != null);
            return null;
        }


        private TreeNode<K> getTreeNode(int h, Object k) {
            return ((parent != null) ? root() : this).find(h, k, null);
        }


        private int tieBreakOrder(Object a, Object b) {
            int d;
            if (a == null || b == null ||
                    (d = a.getClass().getName().
                            compareTo(b.getClass().getName())) == 0)
                d = (System.identityHashCode(a) <= System.identityHashCode(b) ?
                        -1 : 1);
            return d;
        }


        private void treeify(Node<K>[] tab) {
            TreeNode<K> root = null;
            for (TreeNode<K> x = this, next; x != null; x = next) {
                next = (TreeNode<K>)x.next;
                x.left = x.right = null;
                if (root == null) {
                    x.parent = null;
                    x.red = false;
                    root = x;
                }
                else {
                    K k = x.key;
                    int h = x.hash;
                    Class<?> kc = null;
                    for (TreeNode<K> p = root;;) {
                        int dir, ph;
                        K pk = p.key;
                        if ((ph = p.hash) > h)
                            dir = -1;
                        else if (ph < h)
                            dir = 1;
                        else if ((kc == null &&
                                (kc = comparableClassFor(k)) == null) ||
                                (dir = compareComparables(kc, k, pk)) == 0)
                            dir = tieBreakOrder(k, pk);

                        TreeNode<K> xp = p;
                        if ((p = (dir <= 0) ? p.left : p.right) == null) {
                            x.parent = xp;
                            if (dir <= 0)
                                xp.left = x;
                            else
                                xp.right = x;
                            root = balanceInsertion(root, x);
                            break;
                        }
                    }
                }
            }
            moveRootToFront(tab, root);
        }


        private Node<K> untreeify() {
            Node<K> hd = null, tl = null;
            for (Node<K> q = this; q != null; q = q.next) {
                Node<K> p = new Node<>(q.hash, q.key, q.value, null);
                if (tl == null)
                    hd = p;
                else
                    tl.next = p;
                tl = p;
            }
            return hd;
        }


        private TreeNode<K> putTreeVal(Node<K>[] tab, int h, K k) {
            Class<?> kc = null;
            boolean searched = false;
            TreeNode<K> root = (parent != null) ? root() : this;
            for (TreeNode<K> p = root;;) {
                int dir, ph; K pk;
                if ((ph = p.hash) > h)
                    dir = -1;
                else if (ph < h)
                    dir = 1;
                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                    return p;
                else if ((kc == null &&
                        (kc = comparableClassFor(k)) == null) ||
                        (dir = compareComparables(kc, k, pk)) == 0) {
                    if (!searched) {
                        TreeNode<K> q, ch;
                        searched = true;
                        if (((ch = p.left) != null &&
                                (q = ch.find(h, k, kc)) != null) ||
                                ((ch = p.right) != null &&
                                        (q = ch.find(h, k, kc)) != null))
                            return q;
                    }
                    dir = tieBreakOrder(k, pk);
                }

                TreeNode<K> xp = p;
                if ((p = (dir <= 0) ? p.left : p.right) == null) {
                    Node<K> xpn = xp.next;
                    TreeNode<K> x = new TreeNode<>(h, k, Te4HashSet.PRESENT, xpn);
                    if (dir <= 0)
                        xp.left = x;
                    else
                        xp.right = x;
                    xp.next = x;
                    x.parent = x.prev = xp;
                    if (xpn != null)
                        ((TreeNode<K>)xpn).prev = x;
                    moveRootToFront(tab, balanceInsertion(root, x));
                    return null;
                }
            }
        }


        private void removeTreeNode(Node<K>[] tab, boolean movable) {
            int index = (tab.length - 1) & hash;
            TreeNode<K> first = (TreeNode<K>)tab[index], root = first, rl,
                    succ = (TreeNode<K>)next, pred = prev;
            if (pred == null)
                tab[index] = first = succ;
            else
                pred.next = succ;
            if (succ != null)
                succ.prev = pred;
            if (first == null)
                return;
            if (root.parent != null)
                root = root.root();
            if (movable && (root.right == null || (rl = root.left) == null || rl.left == null)) {
                tab[index] = first.untreeify();
                return;
            }
            TreeNode<K> p = this, pl = left, pr = right, replacement;
            if (pl != null && pr != null) {
                TreeNode<K> s = pr, sl;
                while ((sl = s.left) != null) // find successor
                    s = sl;
                boolean c = s.red; s.red = p.red; p.red = c; // swap colors
                TreeNode<K> sr = s.right, pp = p.parent;
                if (s == pr) { // p was s's direct parent
                    p.parent = s;
                    s.right = p;
                }
                else {
                    TreeNode<K> sp = s.parent;
                    if ((p.parent = sp) != null) {
                        if (s == sp.left)
                            sp.left = p;
                        else
                            sp.right = p;
                    }
                    s.right = pr;
                    pr.parent = s;
                }
                p.left = null;
                if ((p.right = sr) != null)
                    sr.parent = p;
                s.left = pl;
                pl.parent = s;
                if ((s.parent = pp) == null)
                    root = s;
                else if (p == pp.left)
                    pp.left = s;
                else
                    pp.right = s;
                if (sr != null)
                    replacement = sr;
                else
                    replacement = p;
            }
            else if (pl != null)
                replacement = pl;
            else if (pr != null)
                replacement = pr;
            else
                replacement = p;
            if (replacement != p) {
                TreeNode<K> pp = replacement.parent = p.parent;
                if (pp == null)
                    (root = replacement).red = false;
                else if (p == pp.left)
                    pp.left = replacement;
                else
                    pp.right = replacement;
                p.left = p.right = p.parent = null;
            }

            TreeNode<K> r = p.red ? root : balanceDeletion(root, replacement);

            if (replacement == p) {
                TreeNode<K> pp = p.parent;
                p.parent = null;
                if (pp != null) {
                    if (p == pp.left)
                        pp.left = null;
                    else if (p == pp.right)
                        pp.right = null;
                }
            }
            if (movable)
                moveRootToFront(tab, r);
        }


        void split(Node<K>[] tab, int index, int bit) {
            TreeNode<K> loHead = null, loTail = null, hiHead = null, hiTail = null;
            int lc = 0, hc = 0;
            for (TreeNode<K> e = this, next; e != null; e = next) {
                next = (TreeNode<K>)e.next;
                e.next = null;
                if ((e.hash & bit) == 0) {
                    if ((e.prev = loTail) == null)
                        loHead = e;
                    else
                        loTail.next = e;
                    loTail = e;
                    ++lc;
                }
                else {
                    if ((e.prev = hiTail) == null)
                        hiHead = e;
                    else
                        hiTail.next = e;
                    hiTail = e;
                    ++hc;
                }
            }
            if (loHead != null) {
                if (lc <= UNTREEIFY_THRESHOLD)
                    tab[index] = loHead.untreeify();
                else {
                    tab[index] = loHead;
                    if (hiHead != null) loHead.treeify(tab);
                }
            }
            if (hiHead != null) {
                if (hc <= UNTREEIFY_THRESHOLD)
                    tab[index + bit] = hiHead.untreeify();
                else {
                    tab[index + bit] = hiHead;
                    if (loHead != null)
                        hiHead.treeify(tab);
                }
            }
        }


        static <K> TreeNode<K> rotateLeft(TreeNode<K> root, TreeNode<K> p) {
            TreeNode<K> r, pp, rl;
            if (p != null && (r = p.right) != null) {
                if ((rl = p.right = r.left) != null)
                    rl.parent = p;
                if ((pp = r.parent = p.parent) == null)
                    (root = r).red = false;
                else if (pp.left == p)
                    pp.left = r;
                else
                    pp.right = r;
                r.left = p;
                p.parent = r;
            }
            return root;
        }


        private static <K> TreeNode<K> rotateRight(TreeNode<K> root, TreeNode<K> p) {
            TreeNode<K> l, pp, lr;
            if (p != null && (l = p.left) != null) {
                if ((lr = p.left = l.right) != null)
                    lr.parent = p;
                if ((pp = l.parent = p.parent) == null)
                    (root = l).red = false;
                else if (pp.right == p)
                    pp.right = l;
                else
                    pp.left = l;
                l.right = p;
                p.parent = l;
            }
            return root;
        }


        private static <K> TreeNode<K> balanceInsertion(TreeNode<K> root, TreeNode<K> x) {
            x.red = true;
            for (TreeNode<K> xp, xpp, xppl, xppr;;) {
                if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                }
                else if (!xp.red || (xpp = xp.parent) == null)
                    return root;
                if (xp == (xppl = xpp.left)) {
                    if ((xppr = xpp.right) != null && xppr.red) {
                        xppr.red = xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    } else {
                        if (x == xp.right) {
                            root = rotateLeft(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateRight(root, xpp);
                            }
                        }
                    }
                } else {
                    if (xppl != null && xppl.red) {
                        xppl.red = xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    }
                    else {
                        if (x == xp.left) {
                            root = rotateRight(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateLeft(root, xpp);
                            }
                        }
                    }
                }
            }
        }


        private static <K> TreeNode<K> balanceDeletion(TreeNode<K> root, TreeNode<K> x) {
            for (TreeNode<K> xp, xpl, xpr;;) {
                if (x == null || x == root)
                    return root;
                else if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                }
                else if (x.red) {
                    x.red = false;
                    return root;
                }
                else if ((xpl = xp.left) == x) {
                    if ((xpr = xp.right) != null && xpr.red) {
                        xpr.red = false;
                        xp.red = true;
                        root = rotateLeft(root, xp);
                        xpr = (xp = x.parent) == null ? null : xp.right;
                    }
                    if (xpr == null)
                        x = xp;
                    else {
                        TreeNode<K> sl = xpr.left, sr = xpr.right;
                        if ((sr == null || !sr.red) &&
                                (sl == null || !sl.red)) {
                            xpr.red = true;
                            x = xp;
                        }
                        else {
                            if (sr == null || !sr.red) {
                                sl.red = false;
                                xpr.red = true;
                                root = rotateRight(root, xpr);
                                xpr = (xp = x.parent) == null ?
                                        null : xp.right;
                            }
                            if (xpr != null) {
                                xpr.red = xp.red;
                                if ((sr = xpr.right) != null)
                                    sr.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateLeft(root, xp);
                            }
                            x = root;
                        }
                    }
                }
                else { // symmetric
                    if (xpl != null && xpl.red) {
                        xpl.red = false;
                        xp.red = true;
                        root = rotateRight(root, xp);
                        xpl = (xp = x.parent) == null ? null : xp.left;
                    }
                    if (xpl == null)
                        x = xp;
                    else {
                        TreeNode<K> sl = xpl.left, sr = xpl.right;
                        if ((sl == null || !sl.red) &&
                                (sr == null || !sr.red)) {
                            xpl.red = true;
                            x = xp;
                        }
                        else {
                            if (sl == null || !sl.red) {
                                sr.red = false;
                                xpl.red = true;
                                root = rotateLeft(root, xpl);
                                xpl = (xp = x.parent) == null ?
                                        null : xp.left;
                            }
                            if (xpl != null) {
                                xpl.red = xp.red;
                                if ((sl = xpl.left) != null)
                                    sl.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateRight(root, xp);
                            }
                            x = root;
                        }
                    }
                }
            }
        }
    }
}