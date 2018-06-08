package doitincloud.oauth2.repositories.impls;

import doitincloud.rdbcache.configs.PropCfg;
import doitincloud.rdbcache.supports.Context;
import doitincloud.commons.Utils;
import doitincloud.oauth2.repositories.LinkRepo;

import doitincloud.rdbcache.configs.AppCtx;
import doitincloud.rdbcache.models.KeyInfo;
import doitincloud.rdbcache.models.KvIdType;
import doitincloud.rdbcache.models.KvPair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class LinkRepoImpl implements LinkRepo {

    private static final Logger LOGGER = LoggerFactory.getLogger(LinkRepoImpl.class);

    private static String table = "oauth2_token_link";

    private static String[] indexKeys = new String[]{"token_key", "link_type"};

    private JdbcTemplate jdbcTemplate;

    @EventListener
    public void handleApplicationReadyEvent(ApplicationReadyEvent event) {
        jdbcTemplate = AppCtx.getSystemJdbcTemplate();
    }

    private Context getContext(String action) {
        Context context = new Context();
        if (PropCfg.getEnableMonitor()) {
            context.enableMonitor("oauth2", table, action);
        }
        return context;
    }

    @Override
    public List<Object> get(String key, String type) {

        LOGGER.trace("call get " + key + " " + type);

        KvIdType idType = new KvIdType(key, table + "/" + type);
        KvPair pair = new KvPair(idType);
        KeyInfo keyInfo = new KeyInfo(table, indexKeys, new String[]{key, type});
        keyInfo.setQueryKey("NOOPS");

        Context context = getContext("get");

        if (AppCtx.getRedisRepo().find(context, pair, keyInfo)) {
            Map<String, Object> map = pair.getData();
            return Utils.convertMapToList(map);
        }

        String sql = "select linked_token from " + table + " where token_key = ? AND link_type = ?";
        String[] params = new String[]{key, type};
        List<Object> list = jdbcTemplate.queryForList(sql, params, Object.class);

        if (list == null || list.size() == 0) {
            LOGGER.trace("no token found anythere " + idType.toString());
            return null;
        }
        Map<String, Object> map = Utils.convertListToMap(list);
        pair.setData(map);

        Utils.getExcutorService().submit(() -> {
            AppCtx.getRedisRepo().save(context, pair, keyInfo);
            AppCtx.getExpireOps().setExpireKey(context, pair, keyInfo);
        });

        return list;
    }

    @Override
    public List<Object> matchKey(String key, String type) {

        LOGGER.trace("call matchKey " + key + " " + type);

        KvIdType idType = new KvIdType(key, table + "/" + type);
        KvPair pair = new KvPair(idType);
        KeyInfo keyInfo = new KeyInfo(table, indexKeys, new String[]{key, type});
        keyInfo.setQueryKey("NOOPS");

        Context context = getContext("matchKey");

        if (AppCtx.getRedisRepo().find(context, pair, keyInfo)) {
            Map<String, Object> map = pair.getData();
            return Utils.convertMapToList(map);
        }

        String sql = "select linked_token from " + table + " where token_key like ? AND link_type = ?";
        String[] params = new String[]{key, type};
        List<Object> list = jdbcTemplate.queryForList(sql, params, Object.class);

        if (list == null || list.size() == 0) {
            LOGGER.trace("no token found anythere " + idType.toString());
            return null;
        }
        Map<String, Object> map = Utils.convertListToMap(list);
        pair.setData(map);

        Utils.getExcutorService().submit(() -> {
            AppCtx.getRedisRepo().save(context, pair, keyInfo);
            AppCtx.getExpireOps().setExpireKey(context, pair, keyInfo);
        });

        return list;
    }

    @Override
    public void put(String key, String type, String token) {

        LOGGER.trace("call put " + key + " " + type + " " + token);

        KvIdType idType = new KvIdType(key,  table + "/" + type);
        KvPair pair = new KvPair(idType);
        KeyInfo keyInfo = new KeyInfo(table, indexKeys, new String[]{key, type});
        keyInfo.setQueryKey("NOOPS");

        Context context = getContext("put");

        List<Object> list = null;
        if (AppCtx.getRedisRepo().find(context, pair, keyInfo)) {
            Map<String, Object> map = pair.getData();
            list = Utils.convertMapToList(map);
            if (list.size() == 1 && list.contains(token)) {
                LOGGER.trace("token is the same");
                return;
            }
            list.clear();
            list.add(token);
        } else {
            list = new ArrayList<>();
            list.add(token);
        }
        Map<String, Object> map = Utils.convertListToMap(list);
        pair.setData(map);
        AppCtx.getRedisRepo().save(context, pair, keyInfo);

        Utils.getExcutorService().submit(() -> {

            String sql1 = "delete from " + table + " where token_key = ? AND link_type = ?";
            String[] params1 = new String[]{key, type};
            jdbcTemplate.update(sql1, params1);

            String sql2 = "insert into " + table + " (token_key, link_type, linked_token) values(?, ?, ?)";
            String[] params2 = new String[]{key, type, token};
            jdbcTemplate.update(sql2, params2);

            AppCtx.getExpireOps().setExpireKey(context, pair, keyInfo);
        });
    }

    @Override
    public void put(String key, String type, Set<String> tokenKeySet) {

        LOGGER.trace("call put " + key + " " + type + " " + tokenKeySet.toString());

        KvIdType idType = new KvIdType(key, table + "/" + type);
        KvPair pair = new KvPair(idType);
        KeyInfo keyInfo = new KeyInfo(table, indexKeys, new String[]{key, type});
        keyInfo.setQueryKey("NOOPS");

        Context context = getContext("put");

        List<Object> list = null;
        if (AppCtx.getRedisRepo().find(context, pair, keyInfo)) {
            Map<String, Object> map = pair.getData();
            list = Utils.convertMapToList(map);
            if (list.size() == tokenKeySet.size()) {
                boolean same = true;
                for (String tokenKey : tokenKeySet) {
                    if (!list.contains(tokenKey)) {
                        same = false;
                        break;
                    }
                }
                if (same) {
                    LOGGER.trace("tokens are the same");
                    return;
                }
            }
            list.clear();
            for (String tokenKey: tokenKeySet) {
                list.add(tokenKey);
            }
        } else {
            list = new ArrayList<>();
            for (String tokenKey: tokenKeySet) {
                list.add(tokenKey);
            }
        }
        Map<String, Object> map = Utils.convertListToMap(list);
        pair.setData(map);
        AppCtx.getRedisRepo().save(context, pair, keyInfo);

        Utils.getExcutorService().submit(() -> {

            String sql1 = "delete from " + table + " where token_key = ? AND link_type = ?";
            String[] params1 = new String[]{key, type};
            jdbcTemplate.update(sql1, params1);

            String sql2 = "insert into " + table + " (token_key, link_type, linked_token) values(?, ?, ?)";
            List<Object[]> params2 = new ArrayList<Object[]>();
            for (String token: tokenKeySet) {
                params2.add(new String[]{key, type, token});
            }
            jdbcTemplate.batchUpdate(sql2, params2);

            AppCtx.getExpireOps().setExpireKey(context, pair, keyInfo);
        });
    }

    @Override
    public void add(String key, String type, String token) {

        LOGGER.trace("call add " + key + " " + type + " " + token);

        KvIdType idType = new KvIdType(key, table + "/" + type);
        KvPair pair = new KvPair(idType);
        KeyInfo keyInfo = new KeyInfo(table, indexKeys, new String[]{key, type});
        keyInfo.setQueryKey("NOOPS");

        Context context = getContext("add");

        List<Object> list = null;
        if (AppCtx.getRedisRepo().find(context, pair, keyInfo)) {
            Map<String, Object> map = pair.getData();
            list = Utils.convertMapToList(map);
            if (list.contains(token)) {
                LOGGER.trace("token " + token + " existed");
                return;
            }
            list.add(0, token);
        } else {
            list = new ArrayList<>();
            list.add(0, token);
        }
        Map<String, Object> map = Utils.convertListToMap(list);
        pair.setData(map);
        AppCtx.getRedisRepo().save(context, pair, keyInfo);

        Utils.getExcutorService().submit(() -> {

            String sql1 = "select count(*) from " + table + " where token_key = ? AND link_type = ? AND linked_token = ?";
            String[] params = new String[]{key, type, token};
            Long count = jdbcTemplate.queryForObject(sql1, params, Long.class);
            if (count == null || count == 0L) {
                String sql2 = "insert into " + table + " (token_key, link_type, linked_token) values(?, ?, ?)";
                jdbcTemplate.update(sql2, params);
            }
            AppCtx.getExpireOps().setExpireKey(context, pair, keyInfo);
        });
    }

    @Override
    public void remove(String key, String type, String token) {

        LOGGER.trace("call remove " + key + " " + type + " " + token);

        KvIdType idType = new KvIdType(key, table + "/" + type);
        KvPair pair = new KvPair(idType);
        KeyInfo keyInfo = new KeyInfo(table, indexKeys, new String[]{key, type});
        keyInfo.setQueryKey("NOOPS");

        Context context = getContext("remove");

        List<Object> list = null;
        if (AppCtx.getRedisRepo().find(context, pair, keyInfo)) {
            Map<String, Object> map = pair.getData();
            list = Utils.convertMapToList(map);
            if (!list.contains(token)) {
                LOGGER.trace("token " + token + " not existed");
                return;
            }
            list.remove(token);

            if (list.size() == 0) {
                pair.clearData();
                AppCtx.getRedisRepo().delete(context, pair, keyInfo);
            } else {
                map = Utils.convertListToMap(list);
                pair.setData(map);
                AppCtx.getRedisRepo().save(context, pair, keyInfo);
            }

            Utils.getExcutorService().submit(() -> {
                String sql = "delete from " + table + " where token_key = ? AND link_type = ? AND linked_token = ?";
                String[] params = new String[]{key, type, token};
                jdbcTemplate.update(sql, params);

                if (pair.hasContent()) {
                    AppCtx.getExpireOps().setExpireKey(context, pair, keyInfo);
                }
            });
        } else {
            String sql1 = "delete from " + table + " where token_key = ? AND link_type = ? AND linked_token = ?";
            String[] params1 = new String[]{key, type, token};
            jdbcTemplate.update(sql1, params1);

            String sql2 = "select linked_token from " + table + " where token_key = ? AND link_type = ?";
            String[] params2 = new String[]{key, type};
            list = jdbcTemplate.queryForList(sql2, params2, Object.class);

            if (list == null || list.size() == 0) {
                return;
            }
            Map<String, Object> map = Utils.convertListToMap(list);
            pair.setData(map);
            AppCtx.getRedisRepo().save(context, pair, keyInfo);

            Utils.getExcutorService().submit(() -> {
                AppCtx.getExpireOps().setExpireKey(context, pair, keyInfo);
            });
        }
    }

    @Override
    public void add(String key, String type, Set<String> tokenKeySet) {

        LOGGER.trace("call add " + key + " " + type + " " + tokenKeySet.toString());

        KvIdType idType = new KvIdType(key, table + "/" + type);
        KvPair pair = new KvPair(idType);
        KeyInfo keyInfo = new KeyInfo(table, indexKeys, new String[]{key, type});
        keyInfo.setQueryKey("NOOPS");

        Context context = getContext("add2");

        List<Object> list = null;
        Set<String> todoSet = new HashSet<>();
        if (AppCtx.getRedisRepo().find(context, pair, keyInfo)) {
            Map<String, Object> map = pair.getData();
            list = Utils.convertMapToList(map);
            for (String tokenKey: tokenKeySet) {
                if (!list.contains(tokenKey)) {
                    todoSet.add(tokenKey);
                }
            }
            if (todoSet.size() == 0) {
                LOGGER.trace("todoSet is empty");
                return;
            }
            for (String tokenKey: todoSet) {
                list.add(0, tokenKey);
            }
        } else {
            list = new ArrayList<>();
            for (String tokenKey: tokenKeySet) {
                list.add(0, tokenKey);
            }
        }
        Map<String, Object> map = Utils.convertListToMap(list);
        pair.setData(map);
        AppCtx.getRedisRepo().save(context, pair, keyInfo);

        Utils.getExcutorService().submit(() -> {

            String sql = "select linked_token from " + table + " where token_key = ? AND link_type = ?";
            String[] params = new String[]{key, type};
            List<Object> resultList = jdbcTemplate.queryForList(sql, params, Object.class);

            Set<String> todoSet2 = new HashSet<>();
            if (resultList != null && resultList.size() > 0) {
                for (String tokenKey: tokenKeySet) {
                    if (!resultList.contains(tokenKey)) {
                        todoSet2.add(tokenKey);
                    }
                }
            }
            if (todoSet2.size() > 0) {
                String sql2 = "insert into " + table + " (token_key, link_type, linked_token) values(?, ?, ?)";
                List<Object[]> params2 = new ArrayList<Object[]>();
                for (String token: todoSet2) {
                    params2.add(new String[]{key, type, token});
                }
                jdbcTemplate.batchUpdate(sql2, params2);
            }

            AppCtx.getExpireOps().setExpireKey(context, pair, keyInfo);
        });

    }

    @Override
    public void remove(String key, String type) {

        LOGGER.trace("call remove " + key + " " + type);

        KvIdType idType = new KvIdType(key, table + "/" + type);
        KvPair pair = new KvPair(idType);
        KeyInfo keyInfo = new KeyInfo(table, indexKeys, new String[]{key, type});
        keyInfo.setQueryKey("NOOPS");

        Context context = getContext("remove");

        AppCtx.getRedisRepo().delete(context, pair, keyInfo);

        String sql = "delete from " + table + " where token_key = ? AND link_type = ?";
        String[] params = new String[]{key, type};
        jdbcTemplate.update(sql, params);
    }

    @Override
    public boolean hasLink(String key, String type) {

        LOGGER.trace("call hashLink " + key + " " + type);

        KvIdType idType = new KvIdType(key, table + "/" + type);
        if (AppCtx.getCacheOps().containsData(idType)) {
            return true;
        }
        KvPair pair = new KvPair(idType);
        KeyInfo keyInfo = new KeyInfo(table, indexKeys, new String[]{key, type});
        keyInfo.setQueryKey("NOOPS");
        Context context = getContext("hasLink");
        if (AppCtx.getRedisRepo().ifExist(context, pair, keyInfo)) {
            return true;
        }
        String sql1 = "select count(*) from " + table + " where token_key = ? AND link_type = ?";
        String[] params = new String[]{key, type};
        Long count = jdbcTemplate.queryForObject(sql1, params, Long.class);
        if (count != null && count > 0L) {
            return true;
        }
        return false;
    }
}
