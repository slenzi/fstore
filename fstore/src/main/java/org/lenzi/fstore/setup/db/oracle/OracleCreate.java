package org.lenzi.fstore.setup.db.oracle;

import java.util.Arrays;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reset Oracle database objects by deleting and recreating tables, indexes, sequence, etc.
 * 
 * @author sal
 */
@Repository
@Transactional
public class OracleCreate {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7175619343965011323L;
	
	@PersistenceContext
	private EntityManager entityManager;		
	
	
	
	/***********************************************************************************
	 * 
	 * core tables
	 * 
	 */
	private String SQL_DROP_TABLE_FS_NODE =
		"drop table FS_NODE";
	private String SQL_CREATE_TABLE_FS_NODE =
		"create table FS_NODE ( " +
		"	NODE_ID NUMBER(15,0) NOT NULL, " +
		"	PARENT_NODE_ID NUMBER(15,0) NOT NULL, " +
		//"   NODE_TYPE VARCHAR2(100) NOT NULL, " +
		"	NAME VARCHAR2(250) NOT NULL, " +
		"	CREATION_DATE date NOT NULL, " +
		"	UPDATED_DATE date NOT NULL, " +
		"	PRIMARY KEY (NODE_ID) " +
		")";
	private String SQL_DROP_TABLE_FS_CLOSURE =
		"drop table FS_CLOSURE";	
	private String SQL_CREATE_TABLE_FS_CLOSURE =
		"create table FS_CLOSURE ( " +
		"	LINK_ID NUMBER(15,0) NOT NULL, " +
		"	PARENT_NODE_ID NUMBER(15,0) NOT NULL, " +
		"	CHILD_NODE_ID NUMBER(15,0) NOT NULL, " +
		"	DEPTH NUMBER(5,0) NOT NULL, " +
		"	PRIMARY KEY (LINK_ID) " +
		")";
	private String SQL_DROP_TABLE_FS_PRUNE =
		"drop table FS_PRUNE";	
	private String SQL_CREATE_TABLE_FS_PRUNE =
		"create table FS_PRUNE ( " +
		"	PRUNE_ID NUMBER(15,0) NOT NULL, " +
		"	NODE_ID NUMBER(15,0) NOT NULL, " +
		"	PRIMARY KEY (PRUNE_ID,NODE_ID) " +
		")";
	private String SQL_DROP_TABLE_FS_TREE =
		"drop table FS_TREE";		
	private String SQL_CREATE_TABLE_FS_TREE =
		"create table FS_TREE ( " +
		"	TREE_ID NUMBER(15,0) NOT NULL, " +
		"	ROOT_NODE_ID NUMBER(15,0) NOT NULL, " +
		"	NAME VARCHAR2(250) NOT NULL, " +
		"	DESCRIPTION VARCHAR2(2000), " +
		"	CREATION_DATE date NOT NULL, " +
		"	UPDATED_DATE date NOT NULL, " +
		"	PRIMARY KEY (TREE_ID) " +
		")";
	private String SQL_DROP_TABLE_FS_USER =
		"drop table FS_USER";
	private String SQL_CREATE_TABLE_FS_USER =	
		"create table FS_USER (  " +
		"	USER_ID NUMBER(15,0) NOT NULL, " +
		"	USERNAME VARCHAR2(20) NOT NULL, " +
		"	PASSWORD VARCHAR2(20) NOT NULL, " +
		"	FIRST_NAME VARCHAR2(40) NOT NULL, " +
		"	MIDDLE_NAME VARCHAR2(40) NOT NULL, " +
		"	LAST_NAME VARCHAR2(40) NOT NULL, " +
		"	PRIMARY_EMAIL VARCHAR2(256) NOT NULL, " +
		"	PRIMARY KEY (USER_ID)  " +
		")";
	private String SQL_DROP_TABLE_FS_USER_GROUP =
		"drop table FS_USER_GROUP";
	private String SQL_CREATE_TABLE_FS_USER_GROUP =		
		"create table FS_USER_GROUP (  " +
		"	GROUP_ID NUMBER(15,0) NOT NULL, " +
		"	GROUP_CODE VARCHAR2(250) NOT NULL, " +
		"	GROUP_DESC VARCHAR2(2000) NOT NULL, " +
		"	PRIMARY KEY (GROUP_ID) " +
		")";
	private String SQL_DROP_TABLE_FS_USER_ROLE =
		"drop table FS_USER_ROLE";
	private String SQL_CREATE_TABLE_FS_USER_ROLE =		
		"create table FS_USER_ROLE ( " + 
		"	ROLE_ID NUMBER(15,0) NOT NULL, " +
		"	ROLE_CODE VARCHAR2(250) NOT NULL, " +
		"	ROLE_DESC VARCHAR2(2000) NOT NULL, " +
		"	PRIMARY KEY (ROLE_ID) " +
		")";
	private String SQL_DROP_TABLE_FS_USER_ROLE_LINK =
		"drop table FS_USER_ROLE_LINK";
	private String SQL_CREATE_TABLE_FS_USER_ROLE_LINK =		
		"create table FS_USER_ROLE_LINK ( " + 
		"	USER_ID NUMBER(15,0) NOT NULL, " +
		"	ROLE_ID NUMBER(15,0) NOT NULL, " +
		"	PRIMARY KEY (USER_ID,ROLE_ID) " + 
		")";
	private String SQL_DROP_TABLE_FS_USER_GROUP_LINK =
		"drop table FS_USER_GROUP_LINK";
	private String SQL_CREATE_TABLE_FS_USER_GROUP_LINK =		
		"create table FS_USER_GROUP_LINK ( " + 
		"	USER_ID NUMBER(15,0) NOT NULL, " +
		"	GROUP_ID NUMBER(15,0) NOT NULL, " +
		"	PRIMARY KEY (USER_ID,GROUP_ID) " + 
		")";	
	
	/***********************************************************************************
	 * 
	 * test table
	 * 
	 */
	private String SQL_DROP_TABLE_FS_TEST_NODE =
		"drop table FS_TEST_NODE";
	private String SQL_CREATE_TABLE_FS_TEST_NODE =
		"create table FS_TEST_NODE ( " +
		"	NODE_ID NUMBER(15,0) NOT NULL, " +
		"	TEST_VALUE VARCHAR2(250), " +
		"	PRIMARY KEY (NODE_ID) " +
		")";
	
	/***********************************************************************************
	 * 
	 * File 2 tables
	 * 
	 */
	private String SQL_DROP_TABLE_FS_PATH_RESOURCE =
		"drop table FS_PATH_RESOURCE";	
	private String SQL_CREATE_TABLE_FS_PATH_RESOURCE =
		"create table FS_PATH_RESOURCE ( " +  
		"	NODE_ID NUMBER(15,0) NOT NULL, " +
		"   STORE_ID NUMBER(15,0) NOT NULL, " +
		"	NAME VARCHAR2(250) NOT NULL, " +
		"	PATH_TYPE VARCHAR2(250) NOT NULL, " + 
		"	RELATIVE_PATH VARCHAR2(250) NOT NULL, " + 
		"	PRIMARY KEY (NODE_ID) " + 
		")";
	private String SQL_DROP_TABLE_FS_FILE_META_RESOURCE =
		"drop table FS_FILE_META_RESOURCE";	
	private String SQL_CREATE_TABLE_FS_FILE_META_RESOURCE =
		"create table FS_FILE_META_RESOURCE ( " +  
		"	NODE_ID NUMBER(15,0) NOT NULL, " + 
		"	FILE_SIZE NUMBER(15,0) NOT NULL, " +
		"   MIME_TYPE VARCHAR2(100), " +
		"	IS_FILE_DATA_IN_DB CHAR(1) default 'Y', " +
		"	PRIMARY KEY (NODE_ID) " + 
		")";
	private String SQL_DROP_TABLE_FS_FILE_RESOURCE =
		"drop table FS_FILE_RESOURCE";	
	private String SQL_CREATE_TABLE_FS_FILE_RESOURCE =
		"create table FS_FILE_RESOURCE ( " +  
		"	NODE_ID NUMBER(15,0) NOT NULL, " + 
		"	FILE_DATA BLOB NOT NULL, " + 
		"	PRIMARY KEY (NODE_ID) " + 
		")";	
	private String SQL_DROP_TABLE_FS_DIRECTORY_RESOURCE =
		"drop table FS_DIRECTORY_RESOURCE";	
	private String SQL_CREATE_TABLE_FS_DIRECTORY_RESOURCE =
		"create table FS_DIRECTORY_RESOURCE ( " +  
		"	NODE_ID NUMBER(15,0) NOT NULL, " +
		"	PRIMARY KEY (NODE_ID) " + 
		")";
	private String SQL_DROP_TABLE_FS_RESOURCE_STORE =
		"drop table FS_RESOURCE_STORE";	
	private String SQL_CREATE_TABLE_FS_RESOURCE_STORE =
		"create table FS_RESOURCE_STORE ( " +  
		"	STORE_ID NUMBER(15,0) NOT NULL, " + 
		"	STORE_NAME VARCHAR2(250) NOT NULL, " + 
		"	STORE_DESCRIPTION VARCHAR2(4000) NOT NULL, " + 
		"	STORE_PATH VARCHAR2(2000) NOT NULL, " + 
		"	NODE_ID NUMBER(15,0) NOT NULL, " +
		"	MAX_FILE_SIZE_IN_DB NUMBER(15,0) DEFAULT 26214400 NOT NULL, " +
		"	CREATION_DATE date NOT NULL,  " + 
		"	UPDATED_DATE date NOT NULL,  " + 
		"	PRIMARY KEY (STORE_ID)  " + 
		")";
	
	/***********************************************************************************
	 * 
	 * CMS tables
	 * 
	 */
	private String SQL_DROP_TABLE_FS_CMS_SITE =
		"drop table FS_CMS_SITE";	
	private String SQL_CREATE_TABLE_FS_CMS_SITE =
		"create table FS_CMS_SITE ( " +  
		"	SITE_ID NUMBER(15,0) NOT NULL, " + 
		"	SITE_NAME VARCHAR2(250) NOT NULL, " + 
		"	SITE_DESCRIPTION VARCHAR2(4000) NOT NULL, " +
		"	OFFLINE_STORE_ID NUMBER(15,0) NOT NULL, " +
		"	ONLINE_STORE_ID NUMBER(15,0) NOT NULL, " +
		"	CREATION_DATE date NOT NULL,  " + 
		"	UPDATED_DATE date NOT NULL,  " + 
		"	PRIMARY KEY (SITE_ID)  " + 
		")";	

	/***********************************************************************************
	 * 
	 * Constraints
	 * 
	 */
	/*
	private String SQL_DROP_CONSTRAINT_FK_FS_FILE_RESOURCE =
		"ALTER TABLE FS_FILE_RESOURCE DROP CONSTRAINT FK_FS_FILE_RESOURCE";		
	private String SQL_CREATE_CONSTRAINT_FK_FS_FILE_RESOURCE =
		"ALTER TABLE FS_FILE_RESOURCE ADD CONSTRAINT FK_FS_FILE_RESOURCE " +  
		"  FOREIGN KEY (NODE_ID) " +  
		"  REFERENCES FS_FILE_META_RESOURCE(NODE_ID)";
	*/
	private String SQL_DROP_CONSTRAINT_FK_FS_FILE_META_RESOURCE =
		"ALTER TABLE FS_FILE_META_RESOURCE DROP CONSTRAINT FK_FS_FILE_META_RESOURCE";		
	private String SQL_CREATE_CONSTRAINT_FK_FS_FILE_META_RESOURCE =
		"ALTER TABLE FS_FILE_META_RESOURCE ADD CONSTRAINT FK_FS_FILE_META_RESOURCE " +  
		"  FOREIGN KEY (NODE_ID) " +  
		"  REFERENCES FS_PATH_RESOURCE(NODE_ID)";
	private String SQL_DROP_CONSTRAINT_FK_FS_DIRECTORY_RESOURCE =
		"ALTER TABLE FS_DIRECTORY_RESOURCE DROP CONSTRAINT FK_FS_DIRECTORY_RESOURCE";		
	private String SQL_CREATE_CONSTRAINT_FK_FS_DIRECTORY_RESOURCE =
		"ALTER TABLE FS_DIRECTORY_RESOURCE ADD CONSTRAINT FK_FS_DIRECTORY_RESOURCE " +  
		"  FOREIGN KEY (NODE_ID) " +  
		"  REFERENCES FS_PATH_RESOURCE(NODE_ID)";
	private String SQL_DROP_CONSTRAINT_FK_FS_PATH_RESOURCE =
		"ALTER TABLE FS_PATH_RESOURCE DROP CONSTRAINT FK_FS_PATH_RESOURCE";		
	private String SQL_CREATE_CONSTRAINT_FK_FS_PATH_RESOURCE =
		"ALTER TABLE FS_PATH_RESOURCE ADD CONSTRAINT FK_FS_PATH_RESOURCE " +  
		"  FOREIGN KEY (NODE_ID) " +  
		"  REFERENCES FS_NODE(NODE_ID)";
	
	/***********************************************************************************
	 * 
	 * Indexes
	 * 
	 */
	private String SQL_DROP_INDEX_FS_PARENT_DEPTH_CHILD =
		"drop index fs_parent_depth_child_idx";
	private String SQL_CREATE_INDEX_FS_PARENT_DEPTH_CHILD =
		"create unique index fs_parent_depth_child_idx on fs_closure(parent_node_id,depth,child_node_id)";
	private String SQL_DROP_INDEX_FS_CHILD_PARENT_DEPTH =
		"drop index fs_child_parent_depth_idx";	
	private String SQL_CREATE_INDEX_FS_CHILD_PARENT_DEPTH =
		"create unique index fs_child_parent_depth_idx on fs_closure(child_node_id,parent_node_id,depth)";	
	
	/***********************************************************************************
	 * 
	 * Sequences
	 * 
	 */
	private String SQL_DROP_SEQUENCE_FS_NODE_ID =
		"drop sequence FS_NODE_ID_SEQUENCE";
	private String SQL_CREATE_SEQUENCE_FS_NODE_ID =
		"CREATE SEQUENCE FS_NODE_ID_SEQUENCE " + 
		"MINVALUE 1 " +
		"MAXVALUE 999999999999999999999999999 " + 
		"INCREMENT BY 1 " +
		"START WITH 1 " +
		"CACHE 10  " +
		"ORDER  " +
		"NOCYCLE";
	private String SQL_DROP_SEQUENCE_FS_LINK_ID =
		"drop sequence FS_LINK_ID_SEQUENCE";	
	private String SQL_CREATE_SEQUENCE_FS_LINK_ID =
		"CREATE SEQUENCE FS_LINK_ID_SEQUENCE " + 
		"MINVALUE 1 " +
		"MAXVALUE 999999999999999999999999999 " + 
		"INCREMENT BY 1 " +
		"START WITH 1 " +
		"CACHE 10  " +
		"ORDER  " +
		"NOCYCLE";
	// used in our prune table
	private String SQL_DROP_SEQUENCE_FS_PRUNE_ID =
		"drop sequence FS_PRUNE_ID_SEQUENCE";	
	private String SQL_CREATE_SEQUENCE_FS_PRUNE_ID =
		"CREATE SEQUENCE FS_PRUNE_ID_SEQUENCE " + 
		"MINVALUE 1 " +
		"MAXVALUE 999999999999999999999999999 " + 
		"INCREMENT BY 1 " +
		"START WITH 1 " +
		"CACHE 10  " +
		"ORDER  " +
		"NOCYCLE";
	private String SQL_DROP_SEQUENCE_FS_TREE_ID =
		"drop sequence FS_TREE_ID_SEQUENCE";	
	private String SQL_CREATE_SEQUENCE_FS_TREE_ID =
		"CREATE SEQUENCE FS_TREE_ID_SEQUENCE " + 
		"MINVALUE 1 " +
		"MAXVALUE 999999999999999999999999999 " + 
		"INCREMENT BY 1 " +
		"START WITH 1 " +
		"CACHE 10  " +
		"ORDER  " +
		"NOCYCLE";
	private String SQL_DROP_SEQUENCE_FS_FILE_ID =
		"drop sequence FS_FILE_ID_SEQUENCE";	
	private String SQL_CREATE_SEQUENCE_FS_FILE_ID =
		"CREATE SEQUENCE FS_FILE_ID_SEQUENCE " + 
		"MINVALUE 1 " +
		"MAXVALUE 999999999999999999999999999 " + 
		"INCREMENT BY 1 " +
		"START WITH 1 " +
		"CACHE 10  " +
		"ORDER  " +
		"NOCYCLE";
	private String SQL_DROP_SEQUENCE_FS_STORE_ID =
		"drop sequence FS_STORE_ID_SEQUENCE";	
	private String SQL_CREATE_SEQUENCE_FS_STORE_ID =
		"CREATE SEQUENCE FS_STORE_ID_SEQUENCE " + 
		"MINVALUE 1 " +
		"MAXVALUE 999999999999999999999999999 " + 
		"INCREMENT BY 1 " +
		"START WITH 1 " +
		"CACHE 10  " +
		"ORDER  " +
		"NOCYCLE";
	private String SQL_DROP_SEQUENCE_FS_CMS_SITE_ID =
		"drop sequence FS_CMS_SITE_ID_SEQUENCE";	
	private String SQL_CREATE_SEQUENCE_FS_CMS_SITE_ID =
		"CREATE SEQUENCE FS_CMS_SITE_ID_SEQUENCE " + 
		"MINVALUE 1 " +
		"MAXVALUE 999999999999999999999999999 " + 
		"INCREMENT BY 1 " +
		"START WITH 1 " +
		"CACHE 10  " +
		"ORDER  " +
		"NOCYCLE";
	private String SQL_DROP_SEQUENCE_FS_USER_ID =
		"drop sequence FS_USER_ID_SEQUENCE";
	/* start with 2. default row is 1 for admin account. */
	private String SQL_CREATE_SEQUENCE_FS_USER_ID =
		"CREATE SEQUENCE FS_USER_ID_SEQUENCE " + 
		"MINVALUE 1 " +
		"MAXVALUE 999999999999999999999999999 " + 
		"INCREMENT BY 1 " +
		"START WITH 2 " +
		"CACHE 10  " +
		"ORDER  " +
		"NOCYCLE";
	private String SQL_DROP_SEQUENCE_FS_USER_GROUP_ID =
		"drop sequence FS_USER_GROUP_ID_SEQUENCE";	
	private String SQL_CREATE_SEQUENCE_FS_USER_GROUP_ID =
		"CREATE SEQUENCE FS_USER_GROUP_ID_SEQUENCE " + 
		"MINVALUE 1 " +
		"MAXVALUE 999999999999999999999999999 " + 
		"INCREMENT BY 1 " +
		"START WITH 1 " +
		"CACHE 10  " +
		"ORDER  " +
		"NOCYCLE";	
	private String SQL_DROP_SEQUENCE_FS_USER_ROLE_ID =
		"drop sequence FS_USER_ROLE_ID_SEQUENCE";
	/* start with 8 to account for default data */
	private String SQL_CREATE_SEQUENCE_FS_USER_ROLE_ID =
		"CREATE SEQUENCE FS_USER_ROLE_ID_SEQUENCE " + 
		"MINVALUE 1 " +
		"MAXVALUE 999999999999999999999999999 " + 
		"INCREMENT BY 1 " +
		"START WITH 8 " +
		"CACHE 10  " +
		"ORDER  " +
		"NOCYCLE";
	
	private String[] DEFAULT_DATA = new String[]{
			
		"INSERT INTO FS_USER (USER_ID, USERNAME, PASSWORD, FIRST_NAME, MIDDLE_NAME, LAST_NAME, PRIMARY_EMAIL) VALUES (1, 'admin', 'admin', 'admin_first', 'admin_middle', 'admin_last', 'admin@your.domain.com')",
		
		/* Role codes MUST start with 'ROLE_' to be compatible with spring security */
		"INSERT INTO FS_USER_ROLE (ROLE_ID, ROLE_CODE, ROLE_DESC) VALUES (1, 'ROLE_ADMINISTRATOR', 'Administrators have access to everything')",
		"INSERT INTO FS_USER_ROLE (ROLE_ID, ROLE_CODE, ROLE_DESC) VALUES (2, 'ROLE_FILE_MANAGER_ADMINISTRATOR', 'Administrative access to file manager section')",
		"INSERT INTO FS_USER_ROLE (ROLE_ID, ROLE_CODE, ROLE_DESC) VALUES (3, 'ROLE_CMS_WORKPLACE_ADMINISTRATOR', 'Administrative access to CMS workplace section')",
		"INSERT INTO FS_USER_ROLE (ROLE_ID, ROLE_CODE, ROLE_DESC) VALUES (4, 'ROLE_FILE_MANAGER_USER', 'Access to the file manager section')",
		"INSERT INTO FS_USER_ROLE (ROLE_ID, ROLE_CODE, ROLE_DESC) VALUES (5, 'ROLE_CMS_WORKPLACE_USER', 'Access to the CMS workplace section')",
		"INSERT INTO FS_USER_ROLE (ROLE_ID, ROLE_CODE, ROLE_DESC) VALUES (6, 'ROLE_USER', 'All users, other than guests. All users with accounts are members of this role.')",
		"INSERT INTO FS_USER_ROLE (ROLE_ID, ROLE_CODE, ROLE_DESC) VALUES (7, 'ROLE_GUEST', 'Default role for users not logged into the system')",
		
		"INSERT INTO FS_USER_ROLE_LINK (USER_ID, ROLE_ID) VALUES (1, 1)",
		"INSERT INTO FS_USER_ROLE_LINK (USER_ID, ROLE_ID) VALUES (1, 6)"
		
	};
	
	// drop statements for Spring Security ACL
	private String[] SPRING_SECURITY_ACL_DROP = new String[]{
		"drop trigger acl_sid_id_trigger",
		"drop trigger acl_class_id_trigger",
		"drop trigger acl_object_identity_id_trigger",
		"drop trigger acl_entry_id_trigger",
		"drop sequence acl_sid_sequence",
		"drop sequence acl_class_sequence",
		"drop sequence acl_object_identity_sequence",
		"drop sequence acl_entry_sequence",
		"drop table acl_entry",
		"drop table acl_object_identity",
		"drop table acl_class",
		"drop table acl_sid"
	};
	
	// create statements for Spring Security ACL
	private String[] SPRING_SECURITY_ACL_CREATE = new String[]{
		"CREATE TABLE acl_sid ( " +
		"    id NUMBER(38) NOT NULL PRIMARY KEY, " +
		"    principal NUMBER(1) NOT NULL CHECK (principal in (0, 1)), " +
		"    sid NVARCHAR2(100) NOT NULL, " +
		"    CONSTRAINT unique_acl_sid UNIQUE (sid, principal) " +
		")",
		
		"CREATE SEQUENCE acl_sid_sequence START WITH 1 INCREMENT BY 1 NOMAXVALUE",
		
		"CREATE OR REPLACE TRIGGER acl_sid_id_trigger " +
		"    BEFORE INSERT ON acl_sid " +
		"    FOR EACH ROW " +
		"BEGIN " +
		"    SELECT acl_sid_sequence.nextval INTO :new.id FROM dual; " +
		"END;",

		"CREATE TABLE acl_class ( " +
		"    id NUMBER(38) NOT NULL PRIMARY KEY, " +
		"    class NVARCHAR2(100) NOT NULL, " +
		"    CONSTRAINT uk_acl_class UNIQUE (class) " +
		")",
		
		"CREATE SEQUENCE acl_class_sequence START WITH 1 INCREMENT BY 1 NOMAXVALUE", 
		
		"CREATE OR REPLACE TRIGGER acl_class_id_trigger " +
		"    BEFORE INSERT ON acl_class " +
		"    FOR EACH ROW " +
		"BEGIN " +
		"    SELECT acl_class_sequence.nextval INTO :new.id FROM dual; " +
		"END;",

		"CREATE TABLE acl_object_identity ( " +
		"    id NUMBER(38) NOT NULL PRIMARY KEY, " +
		"    object_id_class NUMBER(38) NOT NULL, " +
		"    object_id_identity NUMBER(38) NOT NULL, " +
		"    parent_object NUMBER(38), " +
		"    owner_sid NUMBER(38), " +
		"    entries_inheriting NUMBER(1) NOT NULL CHECK (entries_inheriting in (0, 1)), " +
		"    CONSTRAINT uk_acl_object_identity UNIQUE (object_id_class, object_id_identity), " +
		"    CONSTRAINT fk_acl_object_identity_parent FOREIGN KEY (parent_object) REFERENCES acl_object_identity (id), " +
		"    CONSTRAINT fk_acl_object_identity_class FOREIGN KEY (object_id_class) REFERENCES acl_class (id), " +
		"    CONSTRAINT fk_acl_object_identity_owner FOREIGN KEY (owner_sid) REFERENCES acl_sid (id) " +
		")",
		
		"CREATE SEQUENCE acl_object_identity_sequence START WITH 1 INCREMENT BY 1 NOMAXVALUE;",
		
		"CREATE OR REPLACE TRIGGER acl_object_identity_id_trigger " +
		"    BEFORE INSERT ON acl_object_identity " +
		"    FOR EACH ROW " +
		"BEGIN " +
		"    SELECT acl_object_identity_sequence.nextval INTO :new.id FROM dual; " +
		"END;",

		"CREATE TABLE acl_entry ( " +
		"    id NUMBER(38) NOT NULL PRIMARY KEY, " +
		"    acl_object_identity NUMBER(38) NOT NULL, " +
		"    ace_order INTEGER NOT NULL, " +
		"    sid NUMBER(38) NOT NULL, " +
		"    mask INTEGER NOT NULL, " +
		"    granting NUMBER(1) NOT NULL CHECK (granting in (0, 1)), " +
		"    audit_success NUMBER(1) NOT NULL CHECK (audit_success in (0, 1)), " +
		"    audit_failure NUMBER(1) NOT NULL CHECK (audit_failure in (0, 1)), " +
		"    CONSTRAINT unique_acl_entry UNIQUE (acl_object_identity, ace_order), " +
		"    CONSTRAINT fk_acl_entry_object FOREIGN KEY (acl_object_identity) REFERENCES acl_object_identity (id), " +
		"    CONSTRAINT fk_acl_entry_acl FOREIGN KEY (sid) REFERENCES acl_sid (id) " +
		")",
		
		"CREATE SEQUENCE acl_entry_sequence START WITH 1 INCREMENT BY 1 NOMAXVALUE;",
		
		"CREATE OR REPLACE TRIGGER acl_entry_id_trigger " +
		"    BEFORE INSERT ON acl_entry " +
		"    FOR EACH ROW " +
		"BEGIN " +
		"    SELECT acl_entry_sequence.nextval INTO :new.id FROM dual; " +
		"END;"	
	};
	
	public OracleCreate() {
		
	}

	public boolean haveEntityManager(){
		return entityManager != null ? true : false;
	}
	
	/**
	 * Add database objects
	 * 
	 * @throws DatabaseException
	 */
	public void createDatabase() throws DatabaseException {
		
		// sequences
		entityManager.createNativeQuery(SQL_CREATE_SEQUENCE_FS_PRUNE_ID).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_SEQUENCE_FS_NODE_ID).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_SEQUENCE_FS_LINK_ID).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_SEQUENCE_FS_TREE_ID).executeUpdate();		
		entityManager.createNativeQuery(SQL_CREATE_SEQUENCE_FS_FILE_ID).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_SEQUENCE_FS_STORE_ID).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_SEQUENCE_FS_CMS_SITE_ID).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_SEQUENCE_FS_USER_ID).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_SEQUENCE_FS_USER_GROUP_ID).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_SEQUENCE_FS_USER_ROLE_ID).executeUpdate();
		
		// core tables
		entityManager.createNativeQuery(SQL_CREATE_TABLE_FS_PRUNE).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_TABLE_FS_NODE).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_TABLE_FS_CLOSURE).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_TABLE_FS_TREE).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_TABLE_FS_USER).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_TABLE_FS_USER_GROUP).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_TABLE_FS_USER_ROLE).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_TABLE_FS_USER_GROUP_LINK).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_TABLE_FS_USER_ROLE_LINK).executeUpdate();
		
		// test tables
		entityManager.createNativeQuery(SQL_CREATE_TABLE_FS_TEST_NODE).executeUpdate();
		
		// file 2 tables
		entityManager.createNativeQuery(SQL_CREATE_TABLE_FS_PATH_RESOURCE).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_TABLE_FS_FILE_META_RESOURCE).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_TABLE_FS_FILE_RESOURCE).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_TABLE_FS_DIRECTORY_RESOURCE).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_TABLE_FS_RESOURCE_STORE).executeUpdate();	
		
		// cms tables
		entityManager.createNativeQuery(SQL_CREATE_TABLE_FS_CMS_SITE).executeUpdate();
		
		// constraints
		entityManager.createNativeQuery(SQL_CREATE_CONSTRAINT_FK_FS_FILE_META_RESOURCE).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_CONSTRAINT_FK_FS_DIRECTORY_RESOURCE).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_CONSTRAINT_FK_FS_PATH_RESOURCE).executeUpdate();
		
		// indexes
		entityManager.createNativeQuery(SQL_CREATE_INDEX_FS_PARENT_DEPTH_CHILD).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_INDEX_FS_CHILD_PARENT_DEPTH).executeUpdate();
		
		// create spring security acl tables
		for(String aclQuery : SPRING_SECURITY_ACL_CREATE){
			entityManager.createNativeQuery(aclQuery).executeUpdate();
		}
		
		// insert data
		insertDefaultData();		
		
	}
	
	/**
	 * Drop database objects
	 * 
	 * @throws DatabaseException
	 */
	public void dropDatabase() throws DatabaseException {

		// constraints
		entityManager.createNativeQuery(SQL_DROP_CONSTRAINT_FK_FS_FILE_META_RESOURCE).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_CONSTRAINT_FK_FS_DIRECTORY_RESOURCE).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_CONSTRAINT_FK_FS_PATH_RESOURCE).executeUpdate();		
		
		// indexes
		entityManager.createNativeQuery(SQL_DROP_INDEX_FS_PARENT_DEPTH_CHILD).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_INDEX_FS_CHILD_PARENT_DEPTH).executeUpdate();		
		
		// cms tables
		entityManager.createNativeQuery(SQL_DROP_TABLE_FS_CMS_SITE).executeUpdate();		
		
		// test tables
		entityManager.createNativeQuery(SQL_DROP_TABLE_FS_TEST_NODE).executeUpdate();
		
		// file 2 tables	
		entityManager.createNativeQuery(SQL_DROP_TABLE_FS_PATH_RESOURCE).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_TABLE_FS_FILE_META_RESOURCE).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_TABLE_FS_FILE_RESOURCE).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_TABLE_FS_DIRECTORY_RESOURCE).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_TABLE_FS_RESOURCE_STORE).executeUpdate();		
		
		// core tables
		entityManager.createNativeQuery(SQL_DROP_TABLE_FS_PRUNE).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_TABLE_FS_NODE).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_TABLE_FS_CLOSURE).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_TABLE_FS_TREE).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_TABLE_FS_USER_GROUP_LINK).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_TABLE_FS_USER_ROLE_LINK).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_TABLE_FS_USER_GROUP).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_TABLE_FS_USER_ROLE).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_TABLE_FS_USER).executeUpdate();
		
		// sequences
		entityManager.createNativeQuery(SQL_DROP_SEQUENCE_FS_PRUNE_ID).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_SEQUENCE_FS_NODE_ID).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_SEQUENCE_FS_LINK_ID).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_SEQUENCE_FS_TREE_ID).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_SEQUENCE_FS_FILE_ID).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_SEQUENCE_FS_STORE_ID).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_SEQUENCE_FS_CMS_SITE_ID).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_SEQUENCE_FS_USER_ID).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_SEQUENCE_FS_USER_GROUP_ID).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_SEQUENCE_FS_USER_ROLE_ID).executeUpdate();
		
		// drop spring security acl tables
		for(String aclQuery : SPRING_SECURITY_ACL_DROP){
			entityManager.createNativeQuery(aclQuery).executeUpdate();
		}
		
	}
	
	/**
	 * Reset database objects
	 * 
	 * @throws DatabaseException
	 */
	public void resetDatabase() throws DatabaseException {
		
		// drop
		dropDatabase();
		
		// create
		createDatabase();
		
	}
	
	/**
	 * Insert default data
	 * 
	 * @throws DatabaseException
	 */
	private void insertDefaultData() throws DatabaseException {
		
		Arrays.stream(DEFAULT_DATA).forEach( (query) -> {
			
			entityManager.createNativeQuery(query).executeUpdate();
			
		});
		
	}

}
