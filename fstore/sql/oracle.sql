/**
 * Oracle scripts
 * 
 * Get length of lob:
 * select dbms_lob.getlength(f.file_data) from fs_cms_file f
 */

/*ALTER TABLE FS_FILE_RESOURCE DROP CONSTRAINT FK_FS_FILE_RESOURCE;*/
ALTER TABLE FS_FILE_META_RESOURCE DROP CONSTRAINT FK_FS_FILE_META_RESOURCE;
ALTER TABLE FS_DIRECTORY_RESOURCE DROP CONSTRAINT FK_FS_DIRECTORY_RESOURCE;
ALTER TABLE FS_PATH_RESOURCE DROP CONSTRAINT FK_FS_PATH_RESOURCE;

drop sequence FS_NODE_ID_SEQUENCE;
drop sequence FS_LINK_ID_SEQUENCE;
drop sequence FS_PRUNE_ID_SEQUENCE;
drop sequence FS_TREE_ID_SEQUENCE;

drop sequence FS_FILE_ID_SEQUENCE;
drop sequence FS_STORE_ID_SEQUENCE;

drop sequence FS_UPLD_LOG_ID_SEQUENCE 
drop sequence FS_UPLD_LOG_RES_ID_SEQUENCE

drop sequence FS_CMS_SITE_ID_SEQUENCE;

drop sequence FS_USER_ID_SEQUENCE;
drop sequence FS_USER_GROUP_ID_SEQUENCE;
drop sequence FS_USER_ROLE_ID_SEQUENCE;

drop index fs_parent_depth_child_idx;
drop index fs_child_parent_depth_idx;

drop table FS_NODE;
drop table FS_CLOSURE;
drop table FS_PRUNE;
drop table FS_TREE;

drop table FS_TEST_NODE;

drop table FS_FILE_RESOURCE;
drop table FS_FILE_META_RESOURCE;
drop table FS_DIRECTORY_RESOURCE;
drop table FS_PATH_RESOURCE;
drop table FS_RESOURCE_STORE;

drop table FS_UPLOAD_LOG;
drop table FS_UPLOAD_LOG_RESOURCE;
drop table FS_UPLOAD_LOG_RESOURCE_LINK;

drop table FS_CMS_SITE;

drop table FS_USER
drop table FS_USER_GROUP
drop table FS_USER_ROLE
drop table FS_USER_ROLE_LINK
drop table FS_USER_GROUP_LINK

/**
 * core tables
 */
create table FS_NODE ( 
	NODE_ID NUMBER(15,0) NOT NULL, 
	PARENT_NODE_ID NUMBER(15,0) NOT NULL, 
	/*NODE_TYPE VARCHAR2(100) NOT NULL,*/
	NAME VARCHAR2(250) NOT NULL, 
	CREATION_DATE date NOT NULL, 
	UPDATED_DATE date NOT NULL, 
	PRIMARY KEY (NODE_ID) 
);
create table FS_CLOSURE ( 
	LINK_ID NUMBER(15,0) NOT NULL, 
	PARENT_NODE_ID NUMBER(15,0) NOT NULL, 
	CHILD_NODE_ID NUMBER(15,0) NOT NULL, 
	DEPTH NUMBER(5,0) NOT NULL, 
	PRIMARY KEY (LINK_ID) 
);
create table FS_PRUNE ( 
	PRUNE_ID NUMBER(15,0) NOT NULL, 
	NODE_ID NUMBER(15,0) NOT NULL, 
	PRIMARY KEY (PRUNE_ID,NODE_ID) 
);	
create table FS_TREE ( 
	TREE_ID NUMBER(15,0) NOT NULL, 
	ROOT_NODE_ID NUMBER(15,0) NOT NULL, 
	NAME VARCHAR2(250) NOT NULL, 
	DESCRIPTION VARCHAR2(2000), 
	CREATION_DATE date NOT NULL, 
	UPDATED_DATE date NOT NULL, 
	PRIMARY KEY (TREE_ID) 
);

/**
 * Permission tables
 */
create table FS_USER ( 
	USER_ID NUMBER(15,0) NOT NULL,
	USERNAME VARCHAR2(20) NOT NULL,
	PASSWORD VARCHAR2(20) NOT NULL,
	FIRST_NAME VARCHAR2(40) NOT NULL,
	MIDDLE_NAME VARCHAR2(40) NOT NULL,
	LAST_NAME VARCHAR2(40) NOT NULL,
	PRIMARY_EMAIL VARCHAR2(256) NOT NULL,
	PRIMARY KEY (USER_ID) 
);
create table FS_USER_GROUP ( 
	GROUP_ID NUMBER(15,0) NOT NULL,
	GROUP_CODE VARCHAR2(250) NOT NULL,
	GROUP_DESC VARCHAR2(2000) NOT NULL,
	PRIMARY KEY (GROUP_ID) 
);
create table FS_USER_ROLE ( 
	ROLE_ID NUMBER(15,0) NOT NULL,
	ROLE_CODE VARCHAR2(250) NOT NULL,
	ROLE_DESC VARCHAR2(2000) NOT NULL,
	PRIMARY KEY (ROLE_ID) 
);
create table FS_USER_ROLE_LINK ( 
	USER_ID NUMBER(15,0) NOT NULL,
	ROLE_ID NUMBER(15,0) NOT NULL,
	PRIMARY KEY (USER_ID,ROLE_ID) 
);
create table FS_USER_GROUP_LINK ( 
	USER_ID NUMBER(15,0) NOT NULL,
	GROUP_ID NUMBER(15,0) NOT NULL,
	PRIMARY KEY (USER_ID,GROUP_ID) 
);

/**
 * test table
 */
create table FS_TEST_NODE ( 
	NODE_ID NUMBER(15,0) NOT NULL, 
	TEST_VALUE VARCHAR2(250), 
	PRIMARY KEY (NODE_ID) 
);

/**
 * File 2 tables
 */
create table FS_PATH_RESOURCE ( 
	NODE_ID NUMBER(15,0) NOT NULL,
	STORE_ID NUMBER(15,0) NOT NULL,
	NAME VARCHAR2(250) NOT NULL,
	PATH_TYPE VARCHAR2(250) NOT NULL,
	RELATIVE_PATH VARCHAR2(250) NOT NULL,
	PRIMARY KEY (NODE_ID) 
);
create table FS_FILE_META_RESOURCE ( 
	NODE_ID NUMBER(15,0) NOT NULL, 
	FILE_SIZE NUMBER(15,0) NOT NULL,
	MIME_TYPE VARCHAR2(100),
	IS_FILE_DATA_IN_DB CHAR(1) default 'Y',
	PRIMARY KEY (NODE_ID) 
);
create table FS_FILE_RESOURCE ( 
	NODE_ID NUMBER(15,0) NOT NULL,
	FILE_DATA BLOB NOT NULL,
	PRIMARY KEY (NODE_ID) 
);
create table FS_DIRECTORY_RESOURCE ( 
	NODE_ID NUMBER(15,0) NOT NULL,
	PRIMARY KEY (NODE_ID)
);
create table FS_RESOURCE_STORE ( 
	STORE_ID NUMBER(15,0) NOT NULL,
	STORE_NAME VARCHAR2(250) NOT NULL,
	STORE_DESCRIPTION VARCHAR2(4000) NOT NULL,
	STORE_PATH VARCHAR2(2000) NOT NULL,
	NODE_ID NUMBER(15,0) NOT NULL,
	MAX_FILE_SIZE_IN_DB NUMBER(15,0) DEFAULT 26214400 NOT NULL,
	CREATION_DATE date NOT NULL, 
	UPDATED_DATE date NOT NULL, 	
	PRIMARY KEY (STORE_ID) 
);

/**
 * Upload log specific tables
 */
create table FS_UPLOAD_LOG ( 
	UPLD_ID NUMBER(15,0) NOT NULL,
	UPLD_DATE date NOT NULL,
	UPLD_TEMP_PATH VARCHAR2(2000) NOT NULL,
	UPLD_USER_ID NUMBER(15,0) NOT NULL,
	UPLD_NODE_ID NUMBER(15,0) NOT NULL,
	PRIMARY KEY (UPLD_ID) 
);
create table FS_UPLOAD_LOG_RESOURCE ( 
	UPLD_RESOURCE_ID NUMBER(15,0) NOT NULL,
	UPLD_RESOURCE_NAME VARCHAR2(2000) NOT NULL,
	PRIMARY KEY (UPLD_RESOURCE_ID) 
);
create table FS_UPLOAD_LOG_RESOURCE_LINK ( 
	UPLD_ID NUMBER(15,0) NOT NULL,
	UPLD_RESOURCE_ID NUMBER(15,0) NOT NULL,
	PRIMARY KEY (UPLD_ID,UPLD_RESOURCE_ID) 
);

/**
 * CMS Tables
 */
create table FS_CMS_SITE ( 
	SITE_ID NUMBER(15,0) NOT NULL,
	SITE_NAME VARCHAR2(250) NOT NULL,
	SITE_DESCRIPTION VARCHAR2(4000) NOT NULL,
	OFFLINE_STORE_ID NUMBER(15,0) NOT NULL,
	ONLINE_STORE_ID NUMBER(15,0) NOT NULL,
	CREATION_DATE date NOT NULL, 
	UPDATED_DATE date NOT NULL, 	
	PRIMARY KEY (SITE_ID) 
);

/**
 * File 2 Constraints
 */
/*
ALTER TABLE FS_FILE_RESOURCE ADD CONSTRAINT FK_FS_FILE_RESOURCE
  FOREIGN KEY (NODE_ID)
  REFERENCES FS_FILE_META_RESOURCE(NODE_ID);
*/
  
ALTER TABLE FS_FILE_META_RESOURCE ADD CONSTRAINT FK_FS_FILE_META_RESOURCE
  FOREIGN KEY (NODE_ID)
  REFERENCES FS_PATH_RESOURCE(NODE_ID);
  
ALTER TABLE FS_DIRECTORY_RESOURCE ADD CONSTRAINT FK_FS_DIRECTORY_RESOURCE
  FOREIGN KEY (NODE_ID)
  REFERENCES FS_PATH_RESOURCE(NODE_ID);
  
ALTER TABLE FS_PATH_RESOURCE ADD CONSTRAINT FK_FS_PATH_RESOURCE
  FOREIGN KEY (NODE_ID)
  REFERENCES FS_NODE(NODE_ID);

/**
 * Core Indexes
 */
create unique index fs_parent_depth_child_idx on fs_closure(parent_node_id,depth,child_node_id);
create unique index fs_child_parent_depth_idx on fs_closure(child_node_id,parent_node_id,depth);	

/**
 * Core Sequences
 */
CREATE SEQUENCE FS_NODE_ID_SEQUENCE  
MINVALUE 1 
MAXVALUE 999999999999999999999999999  
INCREMENT BY 1 
START WITH 1 
CACHE 10  
ORDER  
NOCYCLE;

CREATE SEQUENCE FS_LINK_ID_SEQUENCE  
MINVALUE 1 
MAXVALUE 999999999999999999999999999  
INCREMENT BY 1 
START WITH 1 
CACHE 10  
ORDER  
NOCYCLE;

CREATE SEQUENCE FS_PRUNE_ID_SEQUENCE  
MINVALUE 1 
MAXVALUE 999999999999999999999999999  
INCREMENT BY 1 
START WITH 1 
CACHE 10  
ORDER  
NOCYCLE;

CREATE SEQUENCE FS_TREE_ID_SEQUENCE  
MINVALUE 1 
MAXVALUE 999999999999999999999999999  
INCREMENT BY 1 
START WITH 1 
CACHE 10  
ORDER  
NOCYCLE;

/**
 * Permission sequences
 */

/* start with 2. default row is 1 for admin account. */
CREATE SEQUENCE FS_USER_ID_SEQUENCE  
MINVALUE 1 
MAXVALUE 999999999999999999999999999  
INCREMENT BY 1 
START WITH 2 
CACHE 10  
ORDER  
NOCYCLE;

CREATE SEQUENCE FS_USER_GROUP_ID_SEQUENCE  
MINVALUE 1 
MAXVALUE 999999999999999999999999999  
INCREMENT BY 1 
START WITH 1 
CACHE 10  
ORDER  
NOCYCLE;

/* start with 8 to account for default data */
CREATE SEQUENCE FS_USER_ROLE_ID_SEQUENCE  
MINVALUE 1 
MAXVALUE 999999999999999999999999999  
INCREMENT BY 1 
START WITH 8 
CACHE 10  
ORDER  
NOCYCLE;

/**
 * File 2 Sequences
 */
CREATE SEQUENCE FS_FILE_ID_SEQUENCE  
MINVALUE 1 
MAXVALUE 999999999999999999999999999  
INCREMENT BY 1 
START WITH 1 
CACHE 10  
ORDER  
NOCYCLE;

CREATE SEQUENCE FS_STORE_ID_SEQUENCE  
MINVALUE 1 
MAXVALUE 999999999999999999999999999  
INCREMENT BY 1 
START WITH 1 
CACHE 10  
ORDER  
NOCYCLE;

/**
 * Upload specific Sequences
 */
CREATE SEQUENCE FS_UPLD_LOG_ID_SEQUENCE  
MINVALUE 1 
MAXVALUE 999999999999999999999999999  
INCREMENT BY 1 
START WITH 1 
CACHE 10  
ORDER  
NOCYCLE;

CREATE SEQUENCE FS_UPLD_LOG_RES_ID_SEQUENCE  
MINVALUE 1 
MAXVALUE 999999999999999999999999999  
INCREMENT BY 1 
START WITH 1 
CACHE 10  
ORDER  
NOCYCLE;

/**
 * CMS Sequences
 */
CREATE SEQUENCE FS_CMS_SITE_ID_SEQUENCE  
MINVALUE 1 
MAXVALUE 999999999999999999999999999  
INCREMENT BY 1 
START WITH 1 
CACHE 10  
ORDER  
NOCYCLE;

/**
 * Default data
 */
INSERT INTO FS_USER (USER_ID, USERNAME, PASSWORD, FIRST_NAME, MIDDLE_NAME, LAST_NAME, PRIMARY_EMAIL) VALUES (1, 'admin', 'admin', 'admin_first', 'admin_middle', 'admin_last', 'admin@your.domain.com');

/* Role codes MUST start with 'ROLE_' to be compatible with spring security */
INSERT INTO FS_USER_ROLE (ROLE_ID, ROLE_CODE, ROLE_DESC) VALUES (1, 'ROLE_ADMINISTRATOR', 'Administrators have access to everything');
INSERT INTO FS_USER_ROLE (ROLE_ID, ROLE_CODE, ROLE_DESC) VALUES (2, 'ROLE_FILE_MANAGER_ADMINISTRATOR', 'Administrative access to file manager section');
INSERT INTO FS_USER_ROLE (ROLE_ID, ROLE_CODE, ROLE_DESC) VALUES (3, 'ROLE_CMS_WORKPLACE_ADMINISTRATOR', 'Administrative access to CMS workplace section');
INSERT INTO FS_USER_ROLE (ROLE_ID, ROLE_CODE, ROLE_DESC) VALUES (4, 'ROLE_FILE_MANAGER_USER', 'Access to the file manager section');
INSERT INTO FS_USER_ROLE (ROLE_ID, ROLE_CODE, ROLE_DESC) VALUES (5, 'ROLE_CMS_WORKPLACE_USER', 'Access to the CMS workplace section');
INSERT INTO FS_USER_ROLE (ROLE_ID, ROLE_CODE, ROLE_DESC) VALUES (6, 'ROLE_USER', 'All users, other than guests. All users with accounts are members of this role.');
INSERT INTO FS_USER_ROLE (ROLE_ID, ROLE_CODE, ROLE_DESC) VALUES (7, 'ROLE_GUEST', 'Default role for users not logged into the system');

INSERT INTO FS_USER_ROLE_LINK (USER_ID, ROLE_ID) VALUES (1, 1);
INSERT INTO FS_USER_ROLE_LINK (USER_ID, ROLE_ID) VALUES (1, 6);


/****************************************************************************************
 * Spring ACL
 * 
 * General ACL schema info:
 * http://docs.spring.io/spring-security/site/docs/4.0.3.RELEASE/reference/htmlsingle/#dbschema-acl
 * 
 * Oracle scripts
 * http://docs.spring.io/spring-security/site/docs/4.0.3.RELEASE/reference/htmlsingle/#oracle-database
 * 
 * The original scripts are available in the Spring ACL dependency jar, e.g. spring-security-acl-X.Y.Z.RELEASE.jar
 * where X.Y.Z in the version number.
 */

-- ACL Schema SQL for Oracle Database 10g+

drop trigger acl_sid_id_trigger;
drop trigger acl_class_id_trigger;
drop trigger acl_object_identity_id_trigger;
drop trigger acl_entry_id_trigger;
drop sequence acl_sid_sequence;
drop sequence acl_class_sequence;
drop sequence acl_object_identity_sequence;
drop sequence acl_entry_sequence;
drop table acl_entry;
drop table acl_object_identity;
drop table acl_class;
drop table acl_sid;

CREATE TABLE acl_sid (
    id NUMBER(38) NOT NULL PRIMARY KEY,
    principal NUMBER(1) NOT NULL CHECK (principal in (0, 1)),
    sid NVARCHAR2(100) NOT NULL,
    CONSTRAINT unique_acl_sid UNIQUE (sid, principal)
);
CREATE SEQUENCE acl_sid_sequence START WITH 1 INCREMENT BY 1 NOMAXVALUE;
CREATE OR REPLACE TRIGGER acl_sid_id_trigger
    BEFORE INSERT ON acl_sid
    FOR EACH ROW
BEGIN
    SELECT acl_sid_sequence.nextval INTO :new.id FROM dual;
END;

CREATE TABLE acl_class (
    id NUMBER(38) NOT NULL PRIMARY KEY,
    class NVARCHAR2(100) NOT NULL,
    CONSTRAINT uk_acl_class UNIQUE (class)
);
CREATE SEQUENCE acl_class_sequence START WITH 1 INCREMENT BY 1 NOMAXVALUE;
CREATE OR REPLACE TRIGGER acl_class_id_trigger
    BEFORE INSERT ON acl_class
    FOR EACH ROW
BEGIN
    SELECT acl_class_sequence.nextval INTO :new.id FROM dual;
END;

CREATE TABLE acl_object_identity (
    id NUMBER(38) NOT NULL PRIMARY KEY,
    object_id_class NUMBER(38) NOT NULL,
    object_id_identity NUMBER(38) NOT NULL,
    parent_object NUMBER(38),
    owner_sid NUMBER(38),
    entries_inheriting NUMBER(1) NOT NULL CHECK (entries_inheriting in (0, 1)),
    CONSTRAINT uk_acl_object_identity UNIQUE (object_id_class, object_id_identity),
    CONSTRAINT fk_acl_object_identity_parent FOREIGN KEY (parent_object) REFERENCES acl_object_identity (id),
    CONSTRAINT fk_acl_object_identity_class FOREIGN KEY (object_id_class) REFERENCES acl_class (id),
    CONSTRAINT fk_acl_object_identity_owner FOREIGN KEY (owner_sid) REFERENCES acl_sid (id)
);
CREATE SEQUENCE acl_object_identity_sequence START WITH 1 INCREMENT BY 1 NOMAXVALUE;
CREATE OR REPLACE TRIGGER acl_object_identity_id_trigger
    BEFORE INSERT ON acl_object_identity
    FOR EACH ROW
BEGIN
    SELECT acl_object_identity_sequence.nextval INTO :new.id FROM dual;
END;

CREATE TABLE acl_entry (
    id NUMBER(38) NOT NULL PRIMARY KEY,
    acl_object_identity NUMBER(38) NOT NULL,
    ace_order INTEGER NOT NULL,
    sid NUMBER(38) NOT NULL,
    mask INTEGER NOT NULL,
    granting NUMBER(1) NOT NULL CHECK (granting in (0, 1)),
    audit_success NUMBER(1) NOT NULL CHECK (audit_success in (0, 1)),
    audit_failure NUMBER(1) NOT NULL CHECK (audit_failure in (0, 1)),
    CONSTRAINT unique_acl_entry UNIQUE (acl_object_identity, ace_order),
    CONSTRAINT fk_acl_entry_object FOREIGN KEY (acl_object_identity) REFERENCES acl_object_identity (id),
    CONSTRAINT fk_acl_entry_acl FOREIGN KEY (sid) REFERENCES acl_sid (id)
);
CREATE SEQUENCE acl_entry_sequence START WITH 1 INCREMENT BY 1 NOMAXVALUE;
CREATE OR REPLACE TRIGGER acl_entry_id_trigger
    BEFORE INSERT ON acl_entry
    FOR EACH ROW
BEGIN
    SELECT acl_entry_sequence.nextval INTO :new.id FROM dual;
END;
