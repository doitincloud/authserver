package doitincloud.oauth2.repositories.impls;

import doitincloud.commons.Utils;
import doitincloud.oauth2.models.User;
import doitincloud.oauth2.repositories.UserRepo;
import doitincloud.rdbcache.configs.AppCtx;
import doitincloud.rdbcache.configs.PropCfg;
import doitincloud.rdbcache.models.KeyInfo;
import doitincloud.rdbcache.models.KvIdType;
import doitincloud.rdbcache.models.KvPair;
import doitincloud.rdbcache.supports.Context;

import org.springframework.stereotype.Repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Repository
public class UserRepoImpl implements UserRepo {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRepoImpl.class);

    private static String table = "security_user_details";

    private static String indexKey = "username";

    private Context getContext(String action) {
        Context context = new Context();
        if (PropCfg.getEnableMonitor()) {
            context.enableMonitor("oauth2", table, "");
        }
        return context;
    }

    @Override
    public User findByUsername(String username) {

        LOGGER.trace("call findByUsername: " + username);

        KvIdType idType = new KvIdType(username, table);
        KvPair pair = new KvPair(idType);
        KeyInfo keyInfo = new KeyInfo(table, indexKey, username);
        keyInfo.setQueryKey("NOOPS");
        keyInfo.setIsNew(true);

        Context context = getContext("findByUsername");

        if (AppCtx.getRedisRepo().find(context, pair, keyInfo)) {
            Map<String, Object> map = pair.getData();
            return Utils.toPojo(map, User.class);
        }

        if (AppCtx.getDbaseRepo().find(context, pair, keyInfo)) {
            Utils.getExcutorService().submit(() -> {
                AppCtx.getRedisRepo().save(context, pair, keyInfo);
                AppCtx.getExpireOps().setExpireKey(context, pair, keyInfo);
            });
            Map<String, Object> map = pair.getData();
            return Utils.toPojo(map, User.class);
        }
        LOGGER.trace("user not found from anywhere: " + username);
        return null;
    }

    // no database operations
    //
    @Override
    public void delete(String username) {

        LOGGER.trace("call delete: " + username);

        KvIdType idType = new KvIdType(username, table);
        KvPair pair = new KvPair(idType);
        KeyInfo keyInfo = new KeyInfo(table, indexKey, username);

        Context context = getContext("delete");

        AppCtx.getRedisRepo().delete(context, pair, keyInfo);
        AppCtx.getKeyInfoRepo().delete(context, pair);

        AppCtx.getAsyncOps().deleteKvPairKeyInfo(context, pair, keyInfo);
    }
}
