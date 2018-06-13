package com.doitincloud.oauth2.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.doitincloud.commons.Utils;

import javax.persistence.*;
import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Client {

    @Id
    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("client_name")
    private String clientName;

    @JsonProperty("client_secret")
    private String clientSecret;

    @JsonProperty("contact_name")
    private String contactName;

    @JsonProperty("contact_email")
    private String contactEmail;

    @JsonProperty("contact_phone_number")
    private String contactPhoneNumber;

    @JsonProperty("access_token_validity_seconds")
    private Integer accessTokenValiditySeconds;

    @JsonProperty("refresh_token_validity_seconds")
    private Integer refreshTokenValiditySeconds;

    @JsonProperty("expires_at")
    private Long expiresAt;

    @JsonProperty("created_at")
    private Date createdAt;

    @JsonProperty("updated_at")
    private Date updatedAt;

    @JsonIgnore
    private Map<String, Object> mapValue = new LinkedHashMap<>();

    public Client() {
    }

    public Client(String name) {
        this();
        clientName = name;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String id) {
        this.clientId = id;
    }

    public void setClientName(String name) {
        this.clientName = name;
    }

    public String getClientName() {
        return clientName;
    }

    @JsonIgnore
    public boolean isSecretRequired() {
        return clientSecret == null || clientSecret.length() != 0;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public void setContactEmail(String email) {
        this.contactEmail = email;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getContactPhoneNumber() {
        return contactPhoneNumber;
    }

    public void setContactPhoneNumber(String contactPhoneNumber) {
        this.contactPhoneNumber = contactPhoneNumber;
    }

    @JsonIgnore
    public boolean isScoped() {
        Set<String> scope = getScope();
        if (scope == null || scope.size() ==0) {
            return false;
        } else {
            return true;
        }
    }

    public Integer getAccessTokenValiditySeconds() {
        return accessTokenValiditySeconds;
    }

    public void setAccessTokenValiditySeconds(Integer accessTokenValiditySeconds) {
        this.accessTokenValiditySeconds = accessTokenValiditySeconds;
    }

    public Integer getRefreshTokenValiditySeconds() {
        return refreshTokenValiditySeconds;
    }

    public void setRefreshTokenValiditySeconds(Integer refreshTokenValiditySeconds) {
        this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
    }

    public boolean isAutoApprove(String scope) {
        Set<String> set = getAutoApproveScopes();
        if (set == null || set.size() == 0) {
            return false;
        }
        return set.contains(scope);
    }

    @JsonIgnore
    public Set<String> getResourceIds() {
        return Utils.getSetProperty(mapValue,"resource_ids");
    }

    public void setResourceIds(Set<String> set) {
        Utils.setSetProperty(mapValue, "resource_ids", set);
    }

    @JsonIgnore
    public Set<String> getScope() {
        return Utils.getSetProperty(mapValue, "scope");
    }

    public void setScope(Set<String> set) {
        Utils.setSetProperty(mapValue,"scope", set);
    }

    @JsonIgnore
    public Set<String> getAuthorizedGrantTypes() {
        return Utils.getSetProperty(mapValue, "authorized_grant_types");
    }

    public void setAuthorizedGrantTypes(Set<String> set) {
        Utils.setSetProperty(mapValue,"authorized_grant_types", set);
    }

    @JsonIgnore
    public Set<String> getAutoApproveScopes() {
        return Utils.getSetProperty(mapValue, "auto_approve_scopes");
    }

    public void setAutoApproveScopes(Set<String> set) {
        Utils.setSetProperty(mapValue,"auto_approve_scopes", set);
    }

    @JsonIgnore
    public Set<String> getRegisteredRedirectUri() {
        return Utils.getSetProperty(mapValue, "registered_redirect_uri");
    }

    public void setRegisteredRedirectUri(Set<String> set) {
        Utils.setSetProperty(mapValue,"registered_redirect_uri", set);
    }

    @JsonIgnore
    public Set<String> getAuthorities() {
        return Utils.getSetProperty(mapValue, "authorities");
    }

    public void setAuthorities(Set<String> authorities) {
        Utils.setSetProperty(mapValue, "authorities", authorities);
    }

    private static Set<String> existedPropertySet = new HashSet<>(Arrays.asList(
            "resource_ids",
            "scope",
            "authorized_grant_types",
            "auto_approve_scopes",
            "registered_redirect_uri",
            "authorities"
    ));

    @JsonProperty("additional_information")
    private Map<String, Object> additionalInformation;

    public Map<String, Object> getAdditionalInformation() {
        if (additionalInformation == null) {
            additionalInformation = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry: mapValue.entrySet()) {
                String key = entry.getKey();
                if (!existedPropertySet.contains(key)) {
                    additionalInformation.put(key, entry.getValue());
                }
            }
        }
        return additionalInformation;
    }

    public void setAdditionalInformation(Map<String, Object> map) {
        additionalInformation = map;
    }

    public void clearAdditionalInformation() {
        if (additionalInformation != null) {
            additionalInformation.clear();
        }
    }

    public Long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @JsonProperty("map_value")
    public String getMapValue() {
        if (additionalInformation != null && additionalInformation.size() > 0) {
            for (Map.Entry<String, Object> entry: additionalInformation.entrySet()) {
                mapValue.put(entry.getKey(), entry.getValue());
            }
        }
        return Utils.toJson(mapValue);
    }

    public void setMapValue(String value) {
        if (value == null || value.length() == 0) {
            this.mapValue.clear();
            return;
        }
        Map<String, Object> map = Utils.toMap(value);
        for (Map.Entry<String, Object> entry: map.entrySet()) {
            String key = entry.getKey();
            if (!existedPropertySet.contains(key)) {
                if (additionalInformation == null) {
                    additionalInformation = new LinkedHashMap<>();
                }
                additionalInformation.put(key, entry.getValue());
            } else {
                mapValue.put(key, entry.getValue());
            }
        }
    }

    @JsonIgnore
    public Object getAdditionalProperty(String name) {
        return mapValue.get(name);
    }

    public void setAdditionalProperty(String name, Object value) {
        mapValue.put(name, value);
    }

    public void removeAdditionalProperty(String name) {
        mapValue.remove(name);
    }
}