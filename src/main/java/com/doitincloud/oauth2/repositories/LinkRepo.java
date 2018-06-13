package com.doitincloud.oauth2.repositories;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface LinkRepo {

    public List<Object> get(String key, String type);

    public List<Object> matchKey(String key, String type);

    public void put(String key, String type, Set<String> set);

    public void put(String key, String type, String token);

    public void add(String key, String type, String token);

    public void remove(String key, String type, String token);

    public void add(String key, String type, Set<String> set);

    public void remove(String key, String type);

    public boolean hasLink(String key, String type);
}
