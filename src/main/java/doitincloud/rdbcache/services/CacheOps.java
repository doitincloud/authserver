/**
 * @link http://rdbcache.com/
 * @copyright Copyright (c) 2017-2018 Sam Wen
 * @license http://rdbcache.com/license/
 */

package doitincloud.rdbcache.services;

import doitincloud.commons.LocalCache;
import doitincloud.rdbcache.configs.PropCfg;
import doitincloud.commons.Utils;
import doitincloud.rdbcache.models.KeyInfo;
import doitincloud.rdbcache.models.KvIdType;
import doitincloud.rdbcache.models.KvPair;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CacheOps extends LocalCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheOps.class);

    private Long keyMinCacheTTL = 600l;

    private Long dataMaxCacheTLL = 300l;

    @EventListener
    public void handleEvent(ContextRefreshedEvent event) {
        setRecycleSecs(PropCfg.getCacheRecycleSecs());
        setMaxCacheSize(PropCfg.getMaxCacheSize());
        keyMinCacheTTL = PropCfg.getKeyMinCacheTTL();
        dataMaxCacheTLL = PropCfg.getDataMaxCacheTLL();
        setMaxSecsToLive(keyMinCacheTTL > dataMaxCacheTLL ? keyMinCacheTTL : dataMaxCacheTLL);

        if (cache == null) {
            initializeCache();
        }
    }

    @EventListener
    public void handleApplicationReadyEvent(ApplicationReadyEvent event) {
        start();
    }

    public Long getKeyMinCacheTTL() {
        return keyMinCacheTTL;
    }

    public void setKeyMinCacheTTL(Long keyMinCacheTTL) {
        this.keyMinCacheTTL = keyMinCacheTTL;
    }

    public Long getDataMaxCacheTLL() {
        return dataMaxCacheTLL;
    }

    public void setDataMaxCacheTLL(Long dataMaxCacheTLL) {
        this.dataMaxCacheTLL = dataMaxCacheTLL;
    }


    public KeyInfo putKeyInfo(KvIdType idType, KeyInfo keyInfo) {
        if (keyMinCacheTTL <= 0l) {
            return null;
        }
        Long ttl = keyInfo.getExpireTTL();
        if (ttl < keyMinCacheTTL) ttl = keyMinCacheTTL;
        Map<String, Object> map = Utils.toMap(keyInfo);
        String hashKey = "keyInfo::"+ idType.getType() + ":" + idType.getId();
        put(hashKey, map, ttl);
        return keyInfo;
    }

    public KeyInfo getKeyInfo(KvIdType idType) {
        String hashKey = "keyInfo::"+ idType.getType() + ":" + idType.getId();
        Map<String, Object> map = get(hashKey);
        if (map == null) {
            return null;
        }
        KeyInfo keyInfo = Utils.toPojo(map, KeyInfo.class);
        return keyInfo;
    }

    public boolean containsKeyInfo(KvIdType idType) {
        String hashKey = "keyInfo::"+ idType.getType() + ":" + idType.getId();
        return containsKey(hashKey);
    }

    public void removeKeyInfo(KvIdType idType) {
        String hashKey = "keyInfo::"+ idType.getType() + ":" + idType.getId();
        cache.remove(hashKey);
    }

    public void removeKeyInfo(List<KvPair> pairs) {
        for (KvPair pair: pairs) {
            removeKeyInfo(pair.getIdType());
        }
    }

    public void putData(KvPair pair, KeyInfo keyInfo) {
        if (dataMaxCacheTLL <= 0L) {
            return;
        }
        Long ttl = keyInfo.getExpireTTL();
        if (ttl > dataMaxCacheTLL) ttl = dataMaxCacheTLL;
        String hashKey = pair.getType() + "::" + pair.getId();
        put(hashKey, pair.getDataClone(), ttl);
    }

    public void updateData(KvPair pair) {
        Map<String, Object> update = pair.getData();
        if (dataMaxCacheTLL <= 0L || update.size() == 0) {
            return ;
        }
        String type = pair.getType();
        String hashKey = pair.getType() + "::" + pair.getId();
        update(hashKey, update);
    }

    public Map<String, Object> getData(KvIdType idType) {
        String hashKey = idType.getType() + "::" + idType.getId();
        return (Map<String, Object>) get(hashKey);
    }

    public boolean containsData(KvIdType idType) {
        String hashKey = idType.getType() + "::" + idType.getId();
        return containsKey(hashKey);
    }

    public void removeData(KvIdType idType) {
        String hashKey = idType.getType() + "::" + idType.getId();
        cache.remove(hashKey);
    }

    public void removeKeyAndData(KvPair pair) {
        KvIdType idType = pair.getIdType();
        removeKeyInfo(idType);
        removeData(idType);
    }

    public void removeKeyAndData(List<KvPair> pairs) {
        for (KvPair pair: pairs) {
            removeKeyAndData(pair);
        }
    }

    public Map<String, Object> listAllKeyInfo(String typePrefix) {
        Map<String, Object> map = new LinkedHashMap<>();
        Set<String> keys = cache.keySet();
        for (String key: keys) {
            if (typePrefix == null || typePrefix.length() == 0) {
                if (!key.startsWith("keyInfo::")) {
                    continue;
                }
            } else {
                if (!key.startsWith("keyInfo::"+typePrefix)) {
                    continue;
                }
            }
            Object object = get(key);
            if (object == null) continue;
            map.put(key, object);
        }
        return map;
    }

    public void removeAllKeyInfo(String typePrefix) {
        Set<String> keys = cache.keySet();
        for (String key: keys) {
            if (key.startsWith("keyInfo::")) {
                if (typePrefix == null || typePrefix.length() == 0) {
                    if (!key.startsWith("keyInfo::")) {
                        continue;
                    }
                } else {
                    if (!key.startsWith("keyInfo::"+typePrefix)) {
                        continue;
                    }
                }
                cache.remove(key);
            }
        }
    }

    public Map<String, Object> listAllData(String type) {
        Map<String, Object> map = new LinkedHashMap<>();
        Set<String> keys = cache.keySet();
        for (String key: keys) {
            if (type == null || type.length() == 0) {
                if (key.startsWith("keyInfo::") || key.startsWith("table_")) {
                    continue;
                }
            } else {
                if (!key.startsWith(type + "::")) {
                    continue;
                }
            }
            Object value = get(key);
            if (value == null) {
                continue;
            }
            map.put(key, value);
        }
        return map;
    }

    public void removeAllData(String type) {
        Set<String> keys = cache.keySet();
        for (String key: keys) {
            if (type == null || type.length() == 0) {
                if (key.startsWith("keyInfo::") || key.startsWith("table_")) {
                    continue;
                }
            } else {
                if (!key.startsWith(type + "::")) {
                    continue;
                }
            }
            cache.remove(key);
        }
    }

    public void removeAllKeyAndData() {
        Map<String, Object> map = new LinkedHashMap<>();
        Set<String> keys = cache.keySet();
        for (String key: keys) {
            if (key.startsWith("table")) {
                continue;
            }
            cache.remove(key);
        }
    }

    public Map<String, Object> listAllTables() {
        Map<String, Object> map = new LinkedHashMap<>();
        Set<String> keys = cache.keySet();
        for (String key: keys) {
            if (!key.startsWith("table")) {
                continue;
            }
            Object value = get(key);
            if (value == null) {
                continue;
            }
            map.put(key, value);
        }
        return map;
    }

    public void removeAllTables() {
        Set<String> keys = cache.keySet();
        for (String key: keys) {
            if (!key.startsWith("table")) {
                continue;
            }
            cache.remove(key);
        }
    }
}

