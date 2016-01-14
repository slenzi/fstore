/**
 * PostgreSQL scripts
 *
 *
 *
 */

/*ALTER TABLE TEST.FS_FILE_RESOURCE DROP CONSTRAINT FK_FS_FILE_RESOURCE;*/
ALTER TABLE TEST.FS_FILE_META_RESOURCE DROP CONSTRAINT FK_FS_FILE_META_RESOURCE;
ALTER TABLE TEST.FS_DIRECTORY_RESOURCE DROP CONSTRAINT FK_FS_DIRECTORY_RESOURCE;
ALTER TABLE TEST.FS_PATH_RESOURCE DROP CONSTRAINT FK_FS_PATH_RESOURCE;

drop sequence TEST.FS_NODE_ID_SEQUENCE;
drop sequence TEST.FS_LINK_ID_SEQUENCE;	
drop sequence TEST.FS_PRUNE_ID_SEQUENCE;
drop sequence TEST.FS_TREE_ID_SEQUENCE;

drop sequence TEST.FS_FILE_ID_SEQUENCE;
drop sequence TEST.FS_STORE_ID_SEQUENCE;

drop sequence TEST.FS_CMS_SITE_ID_SEQUENCE;

drop sequence TEST.FS_USER_ID_SEQUENCE;
drop sequence TEST.FS_USER_GROUP_ID_SEQUENCE;
drop sequence TEST.FS_USER_ROLE_ID_SEQUENCE;

drop index TEST.fs_parent_depth_child_idx;
drop index TEST.fs_child_parent_depth_idx;	

drop table TEST.FS_NODE;
drop table TEST.FS_CLOSURE;	
drop table TEST.FS_PRUNE;
drop table TEST.FS_TREE;

drop table TEST.FS_TEST_NODE;

drop table TEST.FS_FILE_RESOURCE;
drop table TEST.FS_FILE_META_RESOURCE;
drop table TEST.FS_DIRECTORY_RESOURCE;
drop table TEST.FS_PATH_RESOURCE;
drop table TEST.FS_RESOURCE_STORE;

drop table TEST.FS_CMS_SITE;

drop table TEST.FS_USER
drop table TEST.FS_USER_GROUP
drop table TEST.FS_USER_ROLE
drop table TEST.FS_USER_ROLE_LINK
drop table TEST.FS_USER_GROUP_LINK

/**
 * core tree tables
 */
create table TEST.FS_NODE ( 
	NODE_ID NUMERIC(15,0) NOT NULL, 
	PARENT_NODE_ID NUMERIC(15,0) NOT NULL, 
	/*NODE_TYPE CHARACTER VARYING(100) NOT NULL,*/
	NAME CHARACTER VARYING(250) NOT NULL, 
	CREATION_DATE TIMESTAMP NOT NULL, 
	UPDATED_DATE TIMESTAMP NOT NULL, 
	PRIMARY KEY (NODE_ID) 
);
create table TEST.FS_CLOSURE ( 
	LINK_ID NUMERIC(15,0) NOT NULL, 
	PARENT_NODE_ID NUMERIC(15,0) NOT NULL, 
	CHILD_NODE_ID NUMERIC(15,0) NOT NULL, 
	DEPTH NUMERIC(5,0) NOT NULL, 
	PRIMARY KEY (LINK_ID) 
);
create table TEST.FS_PRUNE ( 
	PRUNE_ID NUMERIC(15,0) NOT NULL, 
	NODE_ID NUMERIC(15,0) NOT NULL, 
	PRIMARY KEY (PRUNE_ID,NODE_ID) 
);
create table TEST.FS_TREE ( 
	TREE_ID NUMERIC(15,0) NOT NULL, 
	ROOT_NODE_ID NUMERIC(15,0) NOT NULL, 
	NAME CHARACTER VARYING(250) NOT NULL, 
	DESCRIPTION CHARACTER VARYING(2000), 
	CREATION_DATE TIMESTAMP NOT NULL, 
	UPDATED_DATE TIMESTAMP NOT NULL, 
	PRIMARY KEY (TREE_ID) 
);
create table TEST.FS_USER ( 
	USER_ID NUMERIC(15,0) NOT NULL,
	USERNAME CHARACTER VARYING(20) NOT NULL,
	PASSWORD CHARACTER VARYING(20) NOT NULL,
	FIRST_NAME CHARACTER VARYING(40) NOT NULL,
	MIDDLE_NAME CHARACTER VARYING(40) NOT NULL,
	LAST_NAME CHARACTER VARYING(40) NOT NULL,
	PRIMARY_EMAIL CHARACTER VARYING(256) NOT NULL,
	PRIMARY KEY (USER_ID) 
);
create table TEST.FS_USER_GROUP ( 
	GROUP_ID NUMERIC(15,0) NOT NULL,
	GROUP_CODE CHARACTER VARYING(250) NOT NULL,
	GROUP_DESC CHARACTER VARYING(2000) NOT NULL,
	PRIMARY KEY (GROUP_ID) 
);
create table TEST.FS_USER_ROLE ( 
	ROLE_ID NUMERIC(15,0) NOT NULL,
	ROLE_CODE CHARACTER VARYING(250) NOT NULL,
	ROLE_DESC CHARACTER VARYING(2000) NOT NULL,
	PRIMARY KEY (ROLE_ID) 
);
create table TEST.FS_USER_ROLE_LINK ( 
	USER_ID NUMERIC(15,0) NOT NULL,
	ROLE_ID NUMERIC(15,0) NOT NULL,
	PRIMARY KEY (USER_ID,ROLE_ID) 
);
create table TEST.FS_USER_GROUP_LINK ( 
	USER_ID NUMERIC(15,0) NOT NULL,
	GROUP_ID NUMERIC(15,0) NOT NULL,
	PRIMARY KEY (USER_ID,GROUP_ID) 
);

/**
 * test table
 */
create table TEST.FS_TEST_NODE ( 
	NODE_ID NUMERIC(15,0) NOT NULL, 
	TEST_VALUE CHARACTER VARYING(250), 
	PRIMARY KEY (NODE_ID) 
);

/**
 * File 2 tables
 */
create table TEST.FS_PATH_RESOURCE ( 
	NODE_ID NUMERIC(15,0) NOT NULL,
	STORE_ID NUMERIC(15,0) NOT NULL,
	NAME CHARACTER VARYING(250)) NOT NULL,
	PATH_TYPE CHARACTER VARYING(250) NOT NULL,
	RELATIVE_PATH CHARACTER VARYING(250) NOT NULL,
	PRIMARY KEY (NODE_ID) 
);
create table TEST.FS_FILE_META_RESOURCE ( 
	NODE_ID NUMERIC(15,0) NOT NULL, 
	FILE_SIZE NUMERIC(15,0) NOT NULL,
	MIME_TYPE CHARACTER VARYING(100),
	IS_FILE_DATA_IN_DB CHARACTER(1) DEFAULT 'Y',
	PRIMARY KEY (NODE_ID) 
);
create table TEST.FS_FILE_RESOURCE ( 
	NODE_ID NUMERIC(15,0) NOT NULL,
	FILE_DATA OID NOT NULL,
	PRIMARY KEY (NODE_ID) 
);
create table TEST.FS_DIRECTORY_RESOURCE ( 
	NODE_ID NUMERIC(15,0) NOT NULL,
	PRIMARY KEY (NODE_ID) 
);
create table TEST.FS_RESOURCE_STORE ( 
	STORE_ID NUMERIC(15,0) NOT NULL,
	STORE_NAME CHARACTER VARYING(250) NOT NULL,
	STORE_DESCRIPTION CHARACTER VARYING(4000) NOT NULL,
	STORE_PATH CHARACTER VARYING(2000) NOT NULL,
	NODE_ID NUMERIC(15,0) NOT NULL,
	MAX_FILE_SIZE_IN_DB NUMERIC(15,0) DEFAULT 26214400 NOT NULL,
	CREATION_DATE TIMESTAMP NOT NULL, 
	UPDATED_DATE TIMESTAMP NOT NULL, 	
	PRIMARY KEY (STORE_ID) 
);

/**
 * CMS tables
 */
create table TEST.FS_CMS_SITE ( 
	SITE_ID NUMERIC(15,0) NOT NULL,
	SITE_NAME CHARACTER VARYING(250) NOT NULL,
	SITE_DESCRIPTION CHARACTER VARYING(4000) NOT NULL,
	OFFLINE_STORE_ID NUMERIC(15,0) NOT NULL,
	ONLINE_STORE_ID NUMERIC(15,0) NOT NULL,
	CREATION_DATE TIMESTAMP NOT NULL, 
	UPDATED_DATE TIMESTAMP NOT NULL, 	
	PRIMARY KEY (SITE_ID) 
);

/**
 * File 2 Constraints
 */
/*
ALTER TABLE TEST.FS_FILE_RESOURCE ADD CONSTRAINT FK_FS_FILE_RESOURCE
  FOREIGN KEY (NODE_ID)
  REFERENCES TEST.FS_FILE_META_RESOURCE(NODE_ID);
*/
  
ALTER TABLE TEST.FS_FILE_META_RESOURCE ADD CONSTRAINT FK_FS_FILE_META_RESOURCE
  FOREIGN KEY (NODE_ID)
  REFERENCES TEST.FS_PATH_RESOURCE(NODE_ID);
  
ALTER TABLE TEST.FS_DIRECTORY_RESOURCE ADD CONSTRAINT FK_FS_DIRECTORY_RESOURCE
  FOREIGN KEY (NODE_ID)
  REFERENCES TEST.FS_PATH_RESOURCE(NODE_ID);
  
ALTER TABLE TEST.FS_PATH_RESOURCE ADD CONSTRAINT FK_FS_PATH_RESOURCE
  FOREIGN KEY (NODE_ID)
  REFERENCES TEST.FS_NODE(NODE_ID);

/**
 * Core Indexes
 */
create unique index fs_parent_depth_child_idx on TEST.fs_closure(parent_node_id,depth,child_node_id);
create unique index fs_child_parent_depth_idx on TEST.fs_closure(child_node_id,parent_node_id,depth);	

/**
 * Core Sequences
 */
CREATE SEQUENCE TEST.FS_NODE_ID_SEQUENCE  
INCREMENT BY 1 
START WITH 1 
CACHE 10  
NO CYCLE;	

CREATE SEQUENCE TEST.FS_LINK_ID_SEQUENCE  
INCREMENT BY 1 
START WITH 1 
CACHE 10  
NO CYCLE;

CREATE SEQUENCE TEST.FS_PRUNE_ID_SEQUENCE  
INCREMENT BY 1 
START WITH 1 
CACHE 10  
NO CYCLE;

CREATE SEQUENCE TEST.FS_TREE_ID_SEQUENCE  
INCREMENT BY 1 
START WITH 1 
CACHE 10  
NO CYCLE;

/* start with 2. default row is 1 for admin account. */
CREATE SEQUENCE TEST.FS_USER_ID_SEQUENCE  
INCREMENT BY 1 
START WITH 2 
CACHE 10  
NO CYCLE;

CREATE SEQUENCE TEST.FS_USER_GROUP_ID_SEQUENCE  
INCREMENT BY 1 
START WITH 1 
CACHE 10  
NO CYCLE;

/* start with 8 to account for default data */
CREATE SEQUENCE TEST.FS_USER_ROLE_ID_SEQUENCE  
INCREMENT BY 1 
START WITH 8 
CACHE 10  
NO CYCLE;

/**
 * File 2 Sequences
 */
CREATE SEQUENCE TEST.FS_FILE_ID_SEQUENCE  
INCREMENT BY 1 
START WITH 1 
CACHE 10  
NO CYCLE;

CREATE SEQUENCE TEST.FS_STORE_ID_SEQUENCE  
INCREMENT BY 1 
START WITH 1 
CACHE 10  
NO CYCLE;

/**
 * CMS Sequences
 */
CREATE SEQUENCE TEST.FS_CMS_SITE_ID_SEQUENCE  
INCREMENT BY 1 
START WITH 1 
CACHE 10  
NO CYCLE;

/**
 * Default data
 */
INSERT INTO TEST.FS_USER (USER_ID, USERNAME, PASSWORD, FIRST_NAME, MIDDLE_NAME, LAST_NAME, PRIMARY_EMAIL) VALUES (1, 'admin', 'admin', 'admin_first', 'admin_middle', 'admin_last', 'admin@your.domain.com');

/* Role codes MUST start with 'ROLE_' to be compatible with spring security */
INSERT INTO TEST.FS_USER_ROLE (ROLE_ID, ROLE_CODE, ROLE_DESC) VALUES (1, 'ROLE_ADMINISTRATOR', 'Administrators have access to everything');
INSERT INTO TEST.FS_USER_ROLE (ROLE_ID, ROLE_CODE, ROLE_DESC) VALUES (2, 'ROLE_FILE_MANAGER_ADMINISTRATOR', 'Administrative access to file manager section');
INSERT INTO TEST.FS_USER_ROLE (ROLE_ID, ROLE_CODE, ROLE_DESC) VALUES (3, 'ROLE_CMS_WORKPLACE_ADMINISTRATOR', 'Administrative access to CMS workplace section');
INSERT INTO TEST.FS_USER_ROLE (ROLE_ID, ROLE_CODE, ROLE_DESC) VALUES (4, 'ROLE_FILE_MANAGER_USER', 'Access to the file manager section');
INSERT INTO TEST.FS_USER_ROLE (ROLE_ID, ROLE_CODE, ROLE_DESC) VALUES (5, 'ROLE_CMS_WORKPLACE_USER', 'Access to the CMS workplace section');
INSERT INTO TEST.FS_USER_ROLE (ROLE_ID, ROLE_CODE, ROLE_DESC) VALUES (6, 'ROLE_USER', 'All users, other than guests. All users with accounts are members of this role.');
INSERT INTO TEST.FS_USER_ROLE (ROLE_ID, ROLE_CODE, ROLE_DESC) VALUES (7, 'ROLE_GUEST', 'Default role for users not logged into the system');

INSERT INTO TEST.FS_USER_ROLE_LINK (USER_ID, ROLE_ID) VALUES (1, 1);
INSERT INTO TEST.FS_USER_ROLE_LINK (USER_ID, ROLE_ID) VALUES (1, 6);


/****************************************************************************************
 * Spring ACL
 * 
 * General ACL schema info:
 * http://docs.spring.io/spring-security/site/docs/4.0.3.RELEASE/reference/htmlsingle/#dbschema-acl
 * 
 * PostgreSQL scripts
 * http://docs.spring.io/spring-security/site/docs/4.0.3.RELEASE/reference/htmlsingle/#postgresql
 * 
 * You will have to set the 'classIdentityQuery' and 'sidIdentityQuery' properties of JdbcMutableAclService to the following values, respectively:
 *
 * select currval(pg_get_serial_sequence('acl_class', 'id'))
 * org.springframework.security.acls.jdbc.JdbcMutableAclService.setClassIdentityQuery(String classIdentityQuery)
 * 
 * select currval(pg_get_serial_sequence('acl_sid', 'id'))
 * org.springframework.security.acls.jdbc.JdbcMutableAclService.setSidIdentityQuery(String sidIdentityQuery)
 * 
 * Note: Below table scripts were modified to inlcude the postgres schema name, in this case, 'test'
 * 
 * The original scripts are available in the Spring ACL dependency jar, e.g. spring-security-acl-X.Y.Z.RELEASE.jar
 * where X.Y.Z in the version number.
 */

-- ACL Schema SQL for PostgreSQL

drop table test.acl_entry;
drop table test.acl_object_identity;
drop table test.acl_class;
drop table test.acl_sid;

create table acl_sid(
    id bigserial test.not null primary key,
    principal boolean not null,
    sid varchar(100) not null,
    constraint unique_uk_1 unique(sid,principal)
);

create table test.acl_class(
    id bigserial not null primary key,
    class varchar(100) not null,
    constraint unique_uk_2 unique(class)
);

create table test.acl_object_identity(
    id bigserial primary key,
    object_id_class bigint not null,
    object_id_identity bigint not null,
    parent_object bigint,
    owner_sid bigint,
    entries_inheriting boolean not null,
    constraint unique_uk_3 unique(object_id_class,object_id_identity),
    constraint foreign_fk_1 foreign key(parent_object)references acl_object_identity(id),
    constraint foreign_fk_2 foreign key(object_id_class)references acl_class(id),
    constraint foreign_fk_3 foreign key(owner_sid)references acl_sid(id)
);

create table test.acl_entry(
    id bigserial primary key,
    acl_object_identity bigint not null,
    ace_order int not null,
    sid bigint not null,
    mask integer not null,
    granting boolean not null,
    audit_success boolean not null,
    audit_failure boolean not null,
    constraint unique_uk_4 unique(acl_object_identity,ace_order),
    constraint foreign_fk_4 foreign key(acl_object_identity) references acl_object_identity(id),
    constraint foreign_fk_5 foreign key(sid) references acl_sid(id)
);