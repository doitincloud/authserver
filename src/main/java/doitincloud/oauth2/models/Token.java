/**
 * @link http://rdbcache.com/
 * @copyright Copyright (c) 2017-2018 Sam Wen
 * @license http://rdbcache.com/license/
 */

package doitincloud.oauth2.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import doitincloud.commons.Utils;

import java.util.*;

public class Token implements Cloneable {

    @JsonProperty("token_key")
    private String tokenKey;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonIgnore
    private Map<String, Object> data;

    public Token(String key, String type, String value) {
        this.tokenKey = key;
        this.tokenType = type;
        this.data = Utils.toMap(value);
    }

    public Token(String key, String type, Map<String, Object> map) {
        this.tokenKey = key;
        this.tokenType = type;
        this.data = map;
    }

    public Token(String key, String type) {
        this.tokenKey = key;
        this.tokenType = type;
        data = new LinkedHashMap<>();
    }

    public Token() {
        data = new LinkedHashMap<>();
    }

    public String getTokenKey() {
        return tokenKey;
    }

    public void setTokenKey(String key) {
        this.tokenKey = key;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String type) {
        this.tokenType = type;
    }

    public String getValue() {
        return Utils.toJsonMap(data);
    }

    public void setValue(String value) {
        data.clear();
        Map<String, Object> map = Utils.toMap(value);
        for (Map.Entry<String, Object> entry: map.entrySet()) {
            data.put(entry.getKey(), entry.getValue());
        }
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> map) {
        data = map;
    }

    public void clearData() {
        data.clear();;
    }

    public boolean hasData() {
        if (data != null && data.size() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public Token clone() {
        Token clone = new Token(tokenKey, tokenType);
        for (Map.Entry<String , Object> entry : data.entrySet()) {
            clone.data.put(entry.getKey(), entry.getValue());
        }
        return clone;
    }

    @Override
    public String toString() {
        return "Token{" +
                "token='" + tokenKey + '\'' +
                ", type='" + tokenType + '\'' +
                ", value=" +  getValue()+
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Token token = (Token) o;

        if (tokenKey != null ? !tokenKey.equals(token.tokenKey) : token.tokenKey != null) return false;
        if (tokenType != null ? !tokenType.equals(token.tokenType) : token.tokenType != null) return false;
        return data != null ? data.equals(token.data) : token.data == null;
    }

    @Override
    public int hashCode() {
        int result = tokenKey != null ? tokenKey.hashCode() : 0;
        result = 31 * result + (tokenType != null ? tokenType.hashCode() : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }
}
