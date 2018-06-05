/**
 * @link http://rdbcache.com/
 * @copyright Copyright (c) 2017-2018 Sam Wen
 * @license http://rdbcache.com/license/
 */

package doitincloud.commons;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalCache extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalCache.class);

    private Long recycleSecs = 1800l;

    private Long maxCacheSize = 8192l;

    private Long maxSecsToLive = 900L;

    protected ConcurrentHashMap<String, Cached> cache = null;

    public Long getRecycleSecs() {
        return recycleSecs;
    }

    public void setRecycleSecs(Long recycleSecs) {
        this.recycleSecs = recycleSecs;
    }

    public Long getMaxCacheSize() {
        return maxCacheSize;
    }

    public void setMaxCacheSize(Long maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    public Long getMaxSecsToLive() {
        return maxSecsToLive;
    }

    public void setMaxSecsToLive(Long maxSecsToLive) {
        this.maxSecsToLive = maxSecsToLive;
    }

    @Override
    public synchronized void start() {
        if (cache == null) {
            initializeCache();
        }
        super.start();
    }

    protected void initializeCache() {
        int initCapacity = maxCacheSize.intValue();
        int concurrentLevel = (initCapacity / 256 < 32 ? 32 : initCapacity / 256);
        cache = new ConcurrentHashMap<String, Cached>(initCapacity, 0.75f, concurrentLevel);
    }

    public void put(String key, Map<String, Object> map) {
        cache.put(key, new Cached(map, maxSecsToLive));
    }

    public void put(String key, Map<String, Object> map, long secsToLive) {
        cache.put(key, new Cached(map, secsToLive > maxSecsToLive ? maxSecsToLive : secsToLive));
    }

    public Map<String, Object> put(String key, Long secsToLive, Refreshable refreshable) {
        Map<String, Object> map = refresh(refreshable);
        if (map == null) {
            return null;
        }
        Cached cached = new Cached(map, secsToLive);
        cached.refreshable = refreshable;
        cache.put(key, cached);
        return map;
    }

    public Map<String, Object> update(String key, Map<String, Object> update) {
        Cached cached = cache.get(key);
        if (cached == null) {
            return null;
        }
        if (cached.isTimeout()) {
            if (!cached.isRefreshable()) {
                cache.remove(key);
                return null;
            } else {
                Map<String, Object> map = refresh(cached.refreshable);
                cached.setMap(map);
                return cached.updateMap(update);
            }
        } else {
            Map<String, Object> map = cached.updateMap(update);
            return map;
        }
    }

    public Map<String, Object> get(String key) {
        Cached cached = cache.get(key);
        if (cached == null) {
            return null;
        }
        if (cached.isTimeout()) {
            if (!cached.isRefreshable()) {
                cache.remove(key);
                return null;
            } else {
                Map<String, Object> map = refresh(cached.refreshable);
                if (map == null) {
                    cache.remove(key);
                    return null;
                }
                cached.setMap(map);
                return map;
            }
        } else {
            return cached.getMap();
        }
    }

    public Map<String, Object> getWithoutTimeout(String key) {
        Cached cached = cache.get(key);
        if (cached == null) {
            return null;
        }
        return cached.getMap();
    }

    private Map<String, Object> refresh(Refreshable refreshable) {
        try {
            return refreshable.call();
        } catch (Exception e) {
            String msg = e.getCause().getMessage();
            LOGGER.error(msg);
            throw new RuntimeException(msg);
        }
    }

    public boolean containsKey(String key) {
        if (!cache.containsKey(key)) {
            return false;
        }
        Cached cached = cache.get(key);
        if (cached.isTimeout() && !cached.isRefreshable()) {
            cache.remove(key);
            return false;
        } else {
            return true;
        }
    }

    public void remove(String key) {
        cache.remove(key);
    }

    public Map<String, Object> listAll() {
        Map<String, Object> map = new LinkedHashMap<>();
        Set<String> keys = cache.keySet();
        for (String key: keys) {
            Object value = get(key);
            if (value == null) {
                continue;
            }
            map.put(key, value);
        }
        return map;
    }

    public void removeAll() {
        cache.clear();
    }

    private List<String> refreshKeys = new ArrayList<String>();
    private List<String> timeoutKeys = new ArrayList<String>();

    private SortedSet<KeyLastAccess> lastAccessSortedKeys = new TreeSet<>(new Comparator<KeyLastAccess>(){
        @Override
        public int compare(KeyLastAccess o1, KeyLastAccess o2) {
            return (int) (o1.lastAccess - o2.lastAccess);
        }
    });

    private boolean isRunning = false;

    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void interrupt() {
        isRunning = false;
        super.interrupt();
    }

    @Override
    public void run() {

        isRunning = true;

        while (isRunning) {

            try {

                try {
                    Thread.sleep(recycleSecs * 1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (!isRunning) break;

                timeoutKeys.clear();
                refreshKeys.clear();
                lastAccessSortedKeys.clear();

                final AtomicInteger atomicInteger = new AtomicInteger(0);
                cache.forEach(1, (key, cached) -> {
                    if (key == null || cached == null) return;
                    atomicInteger.incrementAndGet();
                    if (cached.isRefreshable()) {
                        if (cached.isAlmostTimeout()) refreshKeys.add(key);
                    } else if (cached.isTimeout()) {
                        timeoutKeys.add(key);
                    } else {
                        lastAccessSortedKeys.add(new KeyLastAccess(key, cached.lastAccessAt));
                    }
                });

                int size = atomicInteger.intValue();
                long almostMax = 3 * maxCacheSize / 4;

                LOGGER.trace("recycle start -> cache size: " + size + ", max: " + maxCacheSize  + ", almostMax: " + almostMax);

                LOGGER.trace("timeoutKeys size: " + timeoutKeys.size());

                for (String key: timeoutKeys) {
                    size--;
                    if (key == null) continue;
                    cache.remove(key);
                    LOGGER.trace("timeout key: " + key);
                }
                timeoutKeys.clear();

                LOGGER.trace("refreshKeys size: " + refreshKeys.size());

                for (String key: refreshKeys) {
                    if (key == null) continue;
                    Cached cached = cache.get(key);
                    if (cached == null) continue;
                    Cached clone = cached.clone();
                    try {
                        Map<String, Object> map = clone.refreshable.call();
                        clone.setMap(map);
                        cache.put(key, clone);
                        LOGGER.trace("refresh key: " + key);
                    } catch (Exception e) {
                        cache.remove(key);
                        size--;
                        String msg = e.getCause().getMessage();
                        LOGGER.error(msg);
                        e.printStackTrace();
                    }
                }
                refreshKeys.clear();

                if (size <= almostMax) {
                    continue;
                }

                LOGGER.trace("reduce cache size");

                LOGGER.trace("lastAccessSortedKeys size: " + lastAccessSortedKeys.size());

                for (KeyLastAccess keyLastAccess: lastAccessSortedKeys) {
                    size--;
                    if (keyLastAccess == null || keyLastAccess.key == null) continue;
                    cache.remove(keyLastAccess.key);
                    LOGGER.trace("remove key (" + keyLastAccess.lastAccess + "): " + keyLastAccess.key);
                    if (size <= almostMax) {
                        break;
                    }
                }
                lastAccessSortedKeys.clear();

            } catch (Exception e) {
                String msg = e.getCause().getMessage();
                LOGGER.error(msg);
                e.printStackTrace();
            }
        }

        isRunning = false;
    }

    class KeyLastAccess {

        String key;
        long lastAccess;

        KeyLastAccess(String key, long lastAccess) {
            this.key = key;
            this.lastAccess = lastAccess;
        }
    }

    class Cached implements Cloneable {

        private Map<String, Object> map;

        long createdAt;

        long timeToLive = 900000L;  // in millisecond, default 15 minutes

        long lastAccessAt;

        Refreshable refreshable;

        private Cached() {}

        Cached(Map<String, Object> map, long secsToLive) {
            createdAt = System.currentTimeMillis();
            lastAccessAt = System.nanoTime();
            this.map = map;
            this.timeToLive = secsToLive * 1000;
        }

        synchronized Map<String, Object> getMap() {
            lastAccessAt = System.nanoTime();
            return map;
        }

        synchronized void setMap(Map<String, Object> map) {
            lastAccessAt = System.nanoTime();
            this.map = map;
        }

        synchronized Map<String, Object> updateMap(Map<String, Object> update) {
            lastAccessAt = System.nanoTime();
            if (map == null) {
                map = update;
            } else {
                for (Map.Entry<String, Object> entry: update.entrySet()) {
                    map.put(entry.getKey(), entry.getValue());
                }
            }
            return map;
        }

        boolean isRefreshable() {
            return (refreshable != null);
        }

        boolean isTimeout() {
            long now = System.currentTimeMillis();
            if (now > createdAt + timeToLive) {
                return true;
            } else {
                return false;
            }
        }

        boolean isAlmostTimeout() {
            long now = System.currentTimeMillis();
            if (now > createdAt + timeToLive * 3 / 4) {
                return true;
            } else {
                return false;
            }
        }

        void renew() {
            lastAccessAt = System.nanoTime();
            createdAt = System.currentTimeMillis();
        }

        protected synchronized Cached clone() {
            Cached clone = new Cached();
            if (map != null) {
                clone.map = new LinkedHashMap<>(map);
            }
            clone.createdAt = createdAt;
            clone.timeToLive = timeToLive;
            try {
                clone.refreshable = refreshable.clone();
            } catch (CloneNotSupportedException e) {
                clone.refreshable = refreshable;
                //e.printStackTrace();
            }
            return clone;
        }
    }
}
