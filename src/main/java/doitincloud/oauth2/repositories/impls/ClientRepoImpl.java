package doitincloud.oauth2.repositories.impls;

import doitincloud.commons.Utils;
import doitincloud.oauth2.models.Client;
import doitincloud.oauth2.repositories.ClientRepo;
import doitincloud.rdbcache.configs.AppCtx;
import doitincloud.rdbcache.configs.PropCfg;
import doitincloud.rdbcache.models.KeyInfo;
import doitincloud.rdbcache.models.KvIdType;
import doitincloud.rdbcache.models.KvPair;
import doitincloud.rdbcache.supports.Context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Repository
public class ClientRepoImpl implements ClientRepo {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRepoImpl.class);

    private static String table = "oauth2_client_details";

    private static String indexKey = "client_id";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Context getContext(String action) {
        Context context = new Context();
        if (PropCfg.getEnableMonitor()) {
            context.enableMonitor("oauth2", table, action);
        }
        return context;
    }

    @Override
    public Client findByClientId(String clientId) {

        LOGGER.trace("call findByClientId: " + clientId);

        KvIdType idType = new KvIdType(clientId, table);
        KvPair pair = new KvPair(idType);
        KeyInfo keyInfo = new KeyInfo(table, indexKey, clientId);
        keyInfo.setQueryKey("NOOPS");
        keyInfo.setIsNew(true);

        Context context = getContext("findByClientId");

        if (AppCtx.getRedisRepo().find(context, pair, keyInfo)) {
            Map<String, Object> map = pair.getData();
            return Utils.toPojo(map, Client.class);
        }
        if (AppCtx.getDbaseRepo().find(context, pair, keyInfo)) {
            Utils.getExcutorService().submit(() -> {
                AppCtx.getRedisRepo().save(context, pair, keyInfo);
                AppCtx.getExpireOps().setExpireKey(context, pair, keyInfo);
            });
            Map<String, Object> map = pair.getData();
            return Utils.toPojo(map, Client.class);
        }
        LOGGER.trace("client not found from anywhere: " + clientId);
        return null;
    }

    // no database operation
    //
    @Override
    public void delete(String clientId) {

        LOGGER.trace("call delete: " + clientId);

        KvIdType idType = new KvIdType(clientId, table);

        AppCtx.getCacheOps().removeData(idType);

        KvPair pair = new KvPair(idType);
        KeyInfo keyInfo = new KeyInfo(table, indexKey, clientId);

        Context context = getContext("delete");

        AppCtx.getRedisRepo().delete(context, pair, keyInfo);
        AppCtx.getKeyInfoRepo().delete(context, pair);

        AppCtx.getAsyncOps().deleteKvPairKeyInfo(context, pair, keyInfo);
    }
}
