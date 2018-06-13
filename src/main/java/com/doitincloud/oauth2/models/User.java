package com.doitincloud.oauth2.models;

import com.doitincloud.commons.Utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {

    @Id
    @JsonProperty("user_id")
    private String userId;

    private String username;

    private String password;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("phone_number")
    private String phoneNumber;

    private Boolean enabled = true;

    @JsonProperty("account_non_locked")
    private Boolean accountNonLocked = true;

    @JsonProperty("credentials_non_expired")
    private Boolean credentialsNonExpired = true;

    @JsonProperty("expires_at")
    private Long expiresAt;

    @JsonProperty("created_at")
    private Date createdAt;

    @JsonProperty("updated_at")
    private Date updatedAt;

    @JsonIgnore
    private Map<String, Object> mapValue = new LinkedHashMap<>();

    public User() {
    }

    public User(String username) {
        this();
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String id) {
        this.userId = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String name) {
        this.firstName = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String name) {
        this.lastName = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @JsonIgnore
    public boolean isAccountNonExpired() {
        if (expiresAt == null) {
            return true;
        }
        return System.currentTimeMillis() < expiresAt;
    }

    public void setAccountNonExpired(Boolean accountNonExpired) {
        this.expiresAt = System.currentTimeMillis();
    }

    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(Boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    public void setCredentialsNonExpired(Boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    @JsonIgnore
    public Set<String> getAuthorities() {
        return Utils.getSetProperty(mapValue, "authorities");
    }

    public void setAuthorities(Set<String> authorities) {
        Utils.setSetProperty(mapValue, "authorities", authorities);
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
        return Utils.toJson(mapValue);
    }

    public void setMapValue(String value) {
        if (value == null || value.length() == 0) {
            this.mapValue.clear();
            return;
        }
        mapValue = Utils.toMap(value);
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
