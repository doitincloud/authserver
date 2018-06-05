package doitincloud.oauth2.stores;

import doitincloud.oauth2.models.Token;
import doitincloud.oauth2.repositories.TokenRepo;
import doitincloud.rdbcache.supports.Context;
import doitincloud.commons.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.approval.Approval;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.stereotype.Service;

import java.util.*;

public class ApprovalStoreImpl implements ApprovalStore {

    private String tokenType = "ApprovalStore";

    private TokenRepo tokenRepo;

    private Context getContext() {
        Context context = new Context();
        return context;
    }

    public ApprovalStoreImpl(TokenRepo tokenRepo) {
        this.tokenRepo = tokenRepo;
    }

    @Override
    public boolean addApprovals(Collection<Approval> approvals) {

        boolean allOk = true;

        Map<String, Map<String, Object>> todoMap = new LinkedHashMap<>();
        for (Approval approval: approvals) {

            String key = approval.getClientId() + "/" + approval.getUserId();

            Map<String, Object> approvalMap = Utils.toMap(approval);
            String scope = approval.getScope();
            approvalMap.remove("scope");
            approvalMap.remove("clientId");
            approvalMap.remove("userId");

            Map<String, Object> scopeMap = todoMap.get(key);
            if (scopeMap == null) {
                scopeMap = new LinkedHashMap<>();
                todoMap.put(key, scopeMap);
            }
            scopeMap.put(scope, approvalMap);
        }

        for (Map.Entry<String, Map<String, Object>> entry: todoMap.entrySet()) {

            String key = entry.getKey();
            Map<String, Object> scopeMap = entry.getValue();

            Token token = tokenRepo.get(key, tokenType);

            if (token == null) {
                token = new Token(key, tokenType);
                token.setData(scopeMap);
            } else {
                Map<String, Object> map = token.getData();
                for (Map.Entry<String, Object> scopeEntry: scopeMap.entrySet()) {
                    map.put(scopeEntry.getKey(), scopeEntry.getValue());
                }
                token.setData(map);
            }
            tokenRepo.put(token);
        }
        return allOk;
    }

    @Override
    public boolean revokeApprovals(Collection<Approval> approvals) {

        boolean allOk = true;

        Map<String, Set<String>> todoMap = new LinkedHashMap<>();
        for (Approval approval: approvals) {

            String key = approval.getClientId() + "/" + approval.getUserId();

            String scope = approval.getScope();

            Set<String> scopeSet = todoMap.get(key);
            if (scopeSet == null) {
                scopeSet = new HashSet<>();
                todoMap.put(key, scopeSet);
            }
            scopeSet.add(scope);
        }

        for (Map.Entry<String,Set<String>> entry: todoMap.entrySet()) {

            String key = entry.getKey();
            Token token = tokenRepo.get(key, tokenType);

            if (token == null) {
                continue;
            }

            Set<String> scopeSet = entry.getValue();
            Map<String, Object> map = token.getData();
            for (String scope: scopeSet) {
                map.remove(scope);
            }
            if (map.size() == 0) {
                tokenRepo.remove(key, tokenType);
                continue;
            }
            token.setData(map);
            tokenRepo.put(token);
        }
        return allOk;
    }

    @Override
    public Collection<Approval> getApprovals(String userId, String clientId) {

        List<Approval> approvals = new ArrayList<>();

        String key = clientId + "/" + userId;
        Token token = tokenRepo.get(key, tokenType);
        if (token == null) {
            return approvals;
        }
        Map<String, Object> map = token.getData();
        for (Map.Entry<String, Object> entry: map.entrySet()) {
            String scope = entry.getKey();
            Map<String, Object> approvalMap = (Map<String, Object>) entry.getValue();
            approvalMap.put("scope", scope);
            approvalMap.put("clientId", clientId);
            approvalMap.put("userId", userId);
            approvals.add(Utils.toPojo(approvalMap, Approval.class));
        }
        return approvals;
    }
}
