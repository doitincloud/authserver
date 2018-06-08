use doitincloud_db;

delete from rdbcache_kv_pair;

delete from security_user_details;

delete from oauth2_client_details;

delete from oauth2_term;
    
insert into oauth2_term (type, name, map_value) values ('user_role', 'ROLE_USER', '{"options": [""], "visible":["ALL"]}');
insert into oauth2_term (type, name, map_value) values ('user_role', 'ROLE_ADMIN', '{"options": ["ROLE_USER", "ROLE_ADMIN", "ROLE_CLIENT", "ROLE_TRUSTED"], "visible":["ROLE_ADMIN", "ROLE_SUPER", "ROLE_ROOT"]}');
insert into oauth2_term (type, name, map_value) values ('user_role', 'ROLE_SUPER', '{"options": ["ALL"], "visible":["ROLE_SUPER", "ROLE_ROOT"]}');
insert into oauth2_term (type, name, map_value) values ('user_role', 'ROLE_ROOT', '{"options": ["ALL"], "visible":["ROLE_ROOT"]}');

insert into oauth2_term (type, name, map_value) values ('client_role', 'ROLE_CLIENT', '{"visible":["ALL"]}');
insert into oauth2_term (type, name, map_value) values ('client_role', 'ROLE_TRUSTED', '{"visible":["ROLE_ADMIN", "ROLE_SUPER", "ROLE_ROOT"]}');
insert into oauth2_term (type, name, map_value) values ('client_role', 'ROLE_INTERNAL', '{"visible":["ROLE_SUPER", "ROLE_ROOT"]}');

insert into oauth2_term (type, name, map_value) values ('scope', 'read', '{"visible":["ALL"]}');
insert into oauth2_term (type, name, map_value) values ('scope', 'write', '{"visible":["ALL"]}');
insert into oauth2_term (type, name, map_value) values ('scope', 'delete', '{"visible":["ROLE_ADMIN", "ROLE_SUPER", "ROLE_ROOT"]}');

insert into oauth2_term (type, name, map_value) values ('grant_type', 'password', '{"visible":["ROLE_ADMIN", "ROLE_SUPER", "ROLE_ROOT"]}');
insert into oauth2_term (type, name, map_value) values ('grant_type', 'client_credentials', '{"visible":["ROLE_SUPER", "ROLE_ROOT"]}');
insert into oauth2_term (type, name, map_value) values ('grant_type', 'authorization_code', '{"visible":["ROLE_ADMIN", "ROLE_SUPER", "ROLE_ROOT"]}');
insert into oauth2_term (type, name, map_value) values ('grant_type', 'implicit', '{"visible":["ROLE_ADMIN", "ROLE_SUPER", "ROLE_ROOT"]}');

insert into oauth2_term (type, name, map_value) values ('resource_id', 'oauth2-resource', '{"visible":["ALL"]}');
insert into oauth2_term (type, name, map_value) values ('resource_id', 'oauth2-admin-api', '{"visible":["ROLE_ADMIN", "ROLE_SUPER", "ROLE_ROOT"]}');
insert into oauth2_term (type, name, map_value) values ('resource_id', 'rdbcache', '{"visible":["ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER", "ROLE_ROOT"]}');
insert into oauth2_term (type, name, map_value) values ('resource_id', 'digits-trie', '{"visible":["ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER", "ROLE_ROOT"]}');

insert into security_user_details (
    user_id,
    username,
    password,
    first_name,
    last_name,
    map_value
) values(
    '116ca7119e0f4df79f4e2abcc5fae66a',
    'root@example.com',
    '$2a$10$N2.ChT44QUdbk/CS2lwv9u4SBLHCVg7sM2zrbcvqYtsO/RDYSnAqy',
    'DoItInCloud',
    'Root',
    '{"authorities":["ROLE_ROOT"]}'
);

insert into security_user_details (
    user_id,
    username,
    password,
    first_name,
    last_name,
    map_value
) values(
    '226ca7119e0f4df79f4e2abcc5fae77b',
    'super@example.com',
    '$2a$10$N2.ChT44QUdbk/CS2lwv9u4SBLHCVg7sM2zrbcvqYtsO/RDYSnAqy',
    'DoItInCloud',
    'Super',
    '{"authorities":["ROLE_SUPER"]}'
);

insert into security_user_details (
    user_id,
    username,
    password,
    first_name,
    last_name,
    map_value
) values(
    '3b23023b725c4262934d7339556b6d2d',
    'admin@example.com',
    '$2a$10$N2.ChT44QUdbk/CS2lwv9u4SBLHCVg7sM2zrbcvqYtsO/RDYSnAqy',
    'DoItInCloud',
    'Admin',
    '{"authorities":["ROLE_ADMIN"]}'
);

insert into security_user_details (
    user_id,
    username,
    password,
    first_name,
    last_name,
    map_value
) values(
    '3b23023b725c4262934d7339556b6d1d',
    'user@example.com',
    '$2a$10$N2.ChT44QUdbk/CS2lwv9u4SBLHCVg7sM2zrbcvqYtsO/RDYSnAqy',
    'DoItInCloud',
    'User',
    '{"authorities":["ROLE_USER"]}'
);

insert into oauth2_client_details (
    client_id,
    client_name,
    client_secret,
    contact_email,
    map_value
) values(
    '04aa9802e4d145f8b2f8f3b2207b9416',
    'root_client',
    '$2a$10$EX91cBtPWVR31fVGWCUhmu4AQdKENlAdKZtzSsacurwZYZmn0BzNO',
    'root_client@example.com',
'{"resource_ids":["oauth2-resource","oauth2-admin-api"],"scope":["read","write","delete"],"authorized_grant_types":["client_credentials","password"],"auto_approve_scopes":["read","write","delete"],"authorities":["ROLE_INTERNAL"]}'
);

insert into oauth2_client_details (
    client_id,
    client_name,
    client_secret,
    contact_email,
    map_value
) values(
    'fff007a807304b9a8d983f5eaa095c98',
    'admin_client',
    '$2a$10$EX91cBtPWVR31fVGWCUhmu4AQdKENlAdKZtzSsacurwZYZmn0BzNO',
    'admin_client@example',
    '{"resource_ids":["oauth2-resource","oauth2-admin-api","rdbcache","digits-trie"],"scope":["read","write","delete"],"authorized_grant_types":["client_credentials","password"],"auto_approve_scopes":["read","write","delete"],"authorities":["ROLE_INTERNAL"]}'
);

insert into oauth2_client_details (
    client_id,
    client_name,
    client_secret,
    contact_email,
    map_value
) values(
    'ccf681950c2f4ce8b12fc37fd35481a6',
    'trusted_client',
    '$2a$10$EX91cBtPWVR31fVGWCUhmu4AQdKENlAdKZtzSsacurwZYZmn0BzNO',
    'trusted_client@example.com',
'{"resource_ids":["oauth2-resource","rdbcache","digits-trie"],"registered_redirect_uri":["http://localhost:8080/login/oauth2"],"scope":["read","write","delete"],"authorized_grant_types":["refresh_token","implicit","client_credentials","password","authorization_code"],"auto_approve_scopes":["read","write","delete"],"authorities":["ROLE_TRUSTED"]}'
);

insert into oauth2_client_details (
    client_id,
    client_name,
    client_secret,
    contact_email,
    map_value
) values(
    'dcdfbbdfd6b54b7a93236a0ca70041a5',
    'client',
    '$2a$10$EX91cBtPWVR31fVGWCUhmu4AQdKENlAdKZtzSsacurwZYZmn0BzNO',
    'client@example.com',
'{"resource_ids":["oauth2-resource","digits-trie"],"registered_redirect_uri":["http://localhost:8080/login/oauth2"],"scope":["read","write"],"authorized_grant_types":["refresh_token","implicit","client_credentials","password","authorization_code"],"auto_approve_scopes":["read"],"authorities":["ROLE_CLIENT"]}'
);
