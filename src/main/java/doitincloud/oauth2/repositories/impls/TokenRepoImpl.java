package doitincloud.oauth2.repositories.impls;

import doitincloud.rdbcache.configs.PropCfg;
import doitincloud.rdbcache.supports.Context;
import doitincloud.commons.Utils;
import doitincloud.oauth2.models.Token;
import doitincloud.rdbcache.configs.AppCtx;
import doitincloud.rdbcache.models.KeyInfo;
import doitincloud.rdbcache.models.KvIdType;
import doitincloud.rdbcache.models.KvPair;

import doitincloud.oauth2.repositories.TokenRepo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class TokenRepoImpl implements TokenRepo {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenRepoImpl.class);

    private static String table = "oauth2_token_store";

    private static String[] indexKeys = new String[]{"token_key", "token_type"};

    private Context getContext(String action) {
        Context context = new Context();
        if (PropCfg.getEnableMonitor()) {
            context.enableMonitor("oauth2", table, action);
        }
        return context;
    }

    @Override
    public Token get(String key, String type) {

        LOGGER.trace("call get " + key + " " + type);

        KvIdType idType = new KvIdType(key, "TokenStore/" + type);
        KvPair pair = new KvPair(idType);
        KeyInfo keyInfo = new KeyInfo(table, indexKeys, new String[]{key, type});

        Context context = getContext("get");

        Map<String, Object> map = null;

        if (AppCtx.getRedisRepo().find(context, pair, keyInfo)) {
            map = pair.getData();
        } else if (AppCtx.getDbaseRepo().find(context, pair, keyInfo)) {
            Utils.getExcutorService().submit(() -> {
                AppCtx.getRedisRepo().save(context, pair, keyInfo);
                AppCtx.getExpireOps().setExpireKey(context, pair, keyInfo);
            });
            map = pair.getData();
        }
        if (map == null) {
            return null;
        }
        return Utils.toPojo(map, Token.class);
    }

    @Override
    public String put(Token token) {

        String key = token.getTokenKey();
        String type = token.getTokenType();

        LOGGER.trace("call put " + key + " " + type);

        KvIdType idType = new KvIdType(key, "TokenStore/" + type);
        KvPair pair = new KvPair(idType);
        Map<String, Object> map = Utils.toMap(token);
        pair.setData(map);
        KeyInfo keyInfo = new KeyInfo(table, indexKeys, new String[]{key, type});
        keyInfo.setIsNew(true);

        Context context = getContext("put");

        AppCtx.getRedisRepo().save(context, pair, keyInfo);
        Utils.getExcutorService().submit(() -> {
            AppCtx.getDbaseRepo().save(context, pair, keyInfo);
            AppCtx.getExpireOps().setExpireKey(context, pair, keyInfo);
        });
        return key;
    }

    @Override
    public Token remove(String key, String type) {

        LOGGER.trace("call remove " + key + " " + type);

        KvIdType idType = new KvIdType(key, "TokenStore/" + type);
        KvPair pair = new KvPair(idType);
        KeyInfo keyInfo = new KeyInfo(table, indexKeys, new String[]{key, type});

        Context context = getContext("remove");

        Map<String, Object> map = null;
        if (AppCtx.getRedisRepo().find(context, pair, keyInfo)) {
            map = pair.getData();
        } else if (AppCtx.getDbaseRepo().find(context, pair, keyInfo)) {
            map = pair.getData();
        }

        if (map == null) {
            return null;
        }

        Token token = Utils.toPojo(map, Token.class);

        AppCtx.getRedisRepo().delete(context, pair, keyInfo);
        AppCtx.getDbaseRepo().delete(context, pair, keyInfo);
        AppCtx.getKeyInfoRepo().delete(context, pair);

        AppCtx.getAsyncOps().deleteKvPairKeyInfo(context, pair, keyInfo);

        return token;
    }

    @Override
    public boolean hasToken(String key, String type) {

        LOGGER.trace("call hasToken " + key + " " + type);

        KvIdType idType = new KvIdType(key, "TokenStore/" + type);
        KvPair pair = new KvPair(idType);
        KeyInfo keyInfo = new KeyInfo(table, indexKeys, new String[]{key, type});

        Context context = getContext("hasToken");

        if (AppCtx.getRedisRepo().ifExist(context, pair, keyInfo)) {
            return true;
        } else if (AppCtx.getDbaseRepo().find(context, pair, keyInfo)) {
            return true;
        }

        return false;
    }
}
