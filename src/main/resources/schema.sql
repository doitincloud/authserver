CREATE TABLE IF NOT EXISTS rdbcache_kv_pair (
  id varchar(255) not null,
  type varchar(255) not null,
  value text,
  created_at timestamp DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id, type)
);

CREATE TABLE IF NOT EXISTS rdbcache_monitor (
  id bigint not null auto_increment,
  name varchar(255) not null,
  thread_id int,
  duration bigint,
  main_duration bigint,
  client_duration bigint,
  started_at bigint,
  ended_at bigint,
  trace_id varchar(64),
  built_info varchar(255),
  KEY (name),
  KEY (trace_id),
  KEY (built_info),
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS rdbcache_client_test (
  id bigint not null auto_increment,
  trace_id varchar(64),
  status varchar(32),
  passed boolean,
  verify_passed boolean,
  duration bigint,
  process_duration bigint,
  route varchar(255),
  url varchar(255),
  data text,
  KEY (trace_id),
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS rdbcache_stopwatch (
  id bigint not null auto_increment,
  monitor_id bigint not null,
  type varchar(16) not null,
  action varchar(255),
  thread_id int,
  duration bigint,
  started_at bigint,
  ended_at bigint,
  KEY (monitor_id),
  PRIMARY KEY(id),
  FOREIGN KEY(monitor_id) REFERENCES rdbcache_monitor(id) ON DELETE CASCADE ON UPDATE CASCADE
);

create table if not exists security_user_details (
  user_id varchar(64) NOT NULL,
  username varchar(128) NOT NULL PRIMARY KEY,
  password varchar(128) NOT NULL DEFAULT '',
  first_name varchar(32),
  last_name varchar(32),
  phone_number varchar(32),
  enabled boolean DEFAULT true,
  account_non_locked boolean DEFAULT true,
  credentials_non_expired boolean DEFAULT true,
  expires_at bigint DEFAULT NULL,
  created_at timestamp DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  map_value VARCHAR(8192) DEFAULT '{"authorities":["ROLE_USER"]}',
  UNIQUE KEY(user_id),
  KEY first_name (first_name),
  KEY last_name (last_name),
  KEY phone_number_key (phone_number)
);

create table if not exists oauth2_client_details (
  client_id varchar(64) NOT NULL PRIMARY KEY,
  client_name varchar(255) NOT NULL DEFAULT '',
  client_secret varchar(128) NOT NULL DEFAULT '',
  contact_name varchar(64),
  contact_email varchar(128) NOT NULL DEFAULT '',
  contact_phone_number varchar(32),
  access_token_validity_seconds int DEFAULT 3600,
  refresh_token_validity_seconds int DEFAULT 7200,
  expires_at bigint DEFAULT NULL,
  created_at timestamp DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  map_value VARCHAR(8192) DEFAULT '{"resource_ids":["oauth2-resource"],"authorized_grant_types":["client_credentials"],"authorities":["ROLE_CLIENT"],"scope":["read"],"auto_approve_scopes":["read"]}',
  UNIQUE KEY(client_name),
  KEY contact_name (contact_name),
  KEY contact_email (contact_email),
  KEY contact_phone_number (contact_phone_number)
);

CREATE TABLE IF NOT EXISTS oauth2_token_store (
  token_key varchar(64) not null,
  token_type varchar(64) not null,
  value VARCHAR(8192),
  created_at timestamp DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (token_key, token_type)
);

CREATE TABLE IF NOT EXISTS oauth2_token_link (
  token_key varchar(128) not null,
  link_type varchar(128) not null,
  linked_token varchar(128) not null,
  created_at timestamp DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY (token_key, link_type),
  PRIMARY KEY (token_key, link_type, linked_token)
);

CREATE TABLE IF NOT EXISTS oauth2_term (
  id int not null auto_increment,
  type varchar(64) not null,
  name varchar(64) not null,
  map_value VARCHAR(2048),
  created_at timestamp DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY type (type),
  KEY code (name),
  UNIQUE INDEX type_name (type, name)
);