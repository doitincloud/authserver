package com.doitincloud.oauth2.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class TestController {

    @PreAuthorize("#oauth2.hasScope('read') || #oauth2.hasScope('read')")
    @RequestMapping(method = RequestMethod.GET, value = "/api/v1/test/{subject}")
    @ResponseBody
    public Map<String, Object> testRead(@PathVariable String subject) {

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("subject", subject);
        return map;
    }

    @PreAuthorize("#oauth2.hasScope('write') || #oauth2.hasScope('write')")
    @RequestMapping(method = RequestMethod.GET, value = "/api/v1/test-write/{subject}")
    @ResponseBody
    public Map<String, Object> testWrite(@PathVariable String subject) {

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("subject", subject);
        return map;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(method = RequestMethod.GET, value = "/api/v1/test-admin/{subject}")
    @ResponseBody
    public Map<String, Object> testAdmin(@PathVariable String subject) {

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("subject", subject);
        return map;
    }

    @PreAuthorize("(#oauth2.hasScope('delete') || #oauth2.hasScope('delete')) AND hasRole('ADMIN')")
    @RequestMapping(method = RequestMethod.GET, value = "/api/v1/test-admin-delete/{subject}")
    @ResponseBody
    public Map<String, Object> testAdminDelete(@PathVariable String subject) {

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("subject", subject);
        return map;
    }
}
