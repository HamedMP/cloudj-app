package org.phnq.core.util.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

/**
 *
 * @author pgostovic
 */
public class Cache implements Serializable {

    private HashMap<String, CacheEntry> entryMap;
    private TreeSet<CacheEntry> entrySet;

    public Cache() {
        this.entryMap = new HashMap<String, CacheEntry>();
        this.entrySet = new TreeSet<CacheEntry>();
    }

    public Cacheable get(String key) {
        CacheEntry ce = getAndTouchEntry(key);
        return ce == null ? null : ce.getCacheable();
    }

    public void put(Cacheable cacheable) {
        String key = cacheable.getKey();
        CacheEntry ce = getAndTouchEntry(key);
        if (ce == null) {
            ce = new CacheEntry(cacheable);
            entryMap.put(key, ce);
            entrySet.add(ce);
        } else {
            ce.setCacheable(cacheable);
        }
    }

    public synchronized void pruneStaleEntries() {
        List<CacheEntry> toRemove = new ArrayList<CacheEntry>();

        long now = System.currentTimeMillis();
        for (CacheEntry ce : entrySet) {
            if (now > ce.getStaleTime()) {
                toRemove.add(ce);
                entryMap.remove(ce.getCacheable().getKey());
            } else {
                break;
            }
        }

        entrySet.removeAll(toRemove);
    }

    private CacheEntry getAndTouchEntry(String key) {
        pruneStaleEntries();

        CacheEntry ce = entryMap.get(key);
        if (ce == null) {
            return null;
        } else {
            ce.touch();
            return ce;
        }
    }

    private class CacheEntry implements Serializable, Comparable<CacheEntry> {

        private Cacheable cacheable;
        private long staleTime;

        private CacheEntry(Cacheable cacheable) {
            this.cacheable = cacheable;
            this.touch();
        }

        private void touch() {
            this.staleTime = System.currentTimeMillis() + this.cacheable.getCacheTime();

            /*
             * Remove the entry from the set and then immediately put it back in
             * so the order is updated.
             */
            if (entrySet.remove(this)) {
                entrySet.add(this);
            }
        }

        public Cacheable getCacheable() {
            return cacheable;
        }

        public void setCacheable(Cacheable cacheable) {
            this.cacheable = cacheable;
        }

        public long getStaleTime() {
            return staleTime;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }

            try {
                CacheEntry ce = (CacheEntry) o;
                return ce.getCacheable() == cacheable;
            } catch (ClassCastException ex) {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return cacheable.hashCode();
        }

        public int compareTo(CacheEntry ce) {
            long st1 = getStaleTime();
            long st2 = ce.getStaleTime();
            if (st1 < st2) {
                return -1;
            } else if (st1 > st2) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
