package org.lenzi.fstore.setup.db.postgresql;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reset PostgreSQL database objects by deleting and recreating tables, indexes, sequence, etc.
 * 
 * @author sal
 */
@Repository
@Transactional
public class PostgreSQLCreate {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7175619343965011323L;

	private String SCHEMA = "TEST.";
	
	@PersistenceContext
	protected EntityManager entityManager;	
	
	/***********************************************************************************
	 * 
	 * core tree tables
	 * 
	 */
	private String SQL_DROP_TABLE_FS_NODE =
		"drop table " + SCHEMA + "FS_NODE";
	private String SQL_CREATE_TABLE_FS_NODE =
		"create table " + SCHEMA + "FS_NODE ( " +
		"	NODE_ID NUMERIC(15,0) NOT NULL, " +
		"	PARENT_NODE_ID NUMERIC(15,0) NOT NULL, " +
		//"   NODE_TYPE CHARACTER VARYING(100) NOT NULL, " +
		"	NAME CHARACTER VARYING(250) NOT NULL, " +
		"	CREATION_DATE TIMESTAMP NOT NULL, " +
		"	UPDATED_DATE TIMESTAMP NOT NULL, " +
		"	PRIMARY KEY (NODE_ID) " +
		")";
	private String SQL_DROP_TABLE_FS_CLOSURE =
		"drop table " + SCHEMA + "FS_CLOSURE";	
	private String SQL_CREATE_TABLE_FS_CLOSURE =
		"create table " + SCHEMA + "FS_CLOSURE ( " +
		"	LINK_ID NUMERIC(15,0) NOT NULL, " +
		"	PARENT_NODE_ID NUMERIC(15,0) NOT NULL, " +
		"	CHILD_NODE_ID NUMERIC(15,0) NOT NULL, " +
		"	DEPTH NUMERIC(5,0) NOT NULL, " +
		"	PRIMARY KEY (LINK_ID) " +
		")";
	// prune table is used in delete operations (e.g., deleteNode(nodeId) method)
	private String SQL_DROP_TABLE_FS_PRUNE =
		"drop table " + SCHEMA + "FS_PRUNE";	
	private String SQL_CREATE_TABLE_FS_PRUNE =
		"create table " + SCHEMA + "FS_PRUNE ( " +
		"	PRUNE_ID NUMERIC(15,0) NOT NULL, " +
		"	NODE_ID NUMERIC(15,0) NOT NULL, " +
		"	PRIMARY KEY (PRUNE_ID,NODE_ID) " +
		")";
	private String SQL_DROP_TABLE_FS_TREE =
		"drop table " + SCHEMA + "FS_TREE";		
	private String SQL_CREATE_TABLE_FS_TREE =
		"create table " + SCHEMA + "FS_TREE ( " +
		"	TREE_ID NUMERIC(15,0) NOT NULL, " +
		"	ROOT_NODE_ID NUMERIC(15,0) NOT NULL, " +
		"	NAME CHARACTER VARYING(250) NOT NULL, " +
		"	DESCRIPTION CHARACTER VARYING(2000), " +
		"	CREATION_DATE TIMESTAMP NOT NULL, " +
		"	UPDATED_DATE TIMESTAMP NOT NULL, " +
		"	PRIMARY KEY (TREE_ID) " +
		")";
	
	/***********************************************************************************
	 * 
	 * test table
	 * 
	 */
	private String SQL_DROP_TABLE_FS_TEST_NODE =
		"drop table " + SCHEMA + "FS_TEST_NODE";
	private String SQL_CREATE_TABLE_FS_TEST_NODE =
		"create table " + SCHEMA + "FS_TEST_NODE ( " +
		"	NODE_ID NUMERIC(15,0) NOT NULL, " +
		"	TEST_VALUE CHARACTER VARYING(250), " +
		"	PRIMARY KEY (NODE_ID) " +
		")";
	
	/***********************************************************************************
	 * 
	 * File 1 tables
	 * 
	 */
	/* old
	private String SQL_DROP_TABLE_FS_DIRECTORY =
		"drop table " + SCHEMA + "FS_DIRECTORY";	
	private String SQL_CREATE_TABLE_FS_DIRECTORY =
		"create table " + SCHEMA + "FS_DIRECTORY ( " + 
		"	NODE_ID NUMERIC(15,0) NOT NULL, " + 
		"	DIR_NAME CHARACTER VARYING(250) NOT NULL, " +
		"	RELATIVE_DIR_PATH CHARACTER VARYING(250) NOT NULL, " +
		"	PRIMARY KEY (NODE_ID) " + 
		")";
	private String SQL_DROP_TABLE_FS_DIR_FILE_LINK =
		"drop table " + SCHEMA + "FS_DIR_FILE_LINK";
	private String SQL_CREATE_TABLE_FS_DIR_FILE_LINK =
		"create table " + SCHEMA + "FS_DIR_FILE_LINK ( " + 
		"	NODE_ID NUMERIC(15,0) NOT NULL, " + 
		"	FILE_ID NUMERIC(15,0) NOT NULL, " + 
		"	PRIMARY KEY(NODE_ID,FILE_ID) " + 
		")";
	private String SQL_DROP_TABLE_FS_FILE_ENTRY =
		"drop table " + SCHEMA + "FS_FILE_ENTRY";	
	private String SQL_CREATE_TABLE_FS_FILE_ENTRY =
		"create table " + SCHEMA + "FS_FILE_ENTRY ( " +  
		"	FILE_ID NUMERIC(15,0) NOT NULL, " +
		"	FILE_SIZE NUMERIC(15,0) NOT NULL, " +
		"	FILE_NAME CHARACTER VARYING(250) NOT NULL, " +  
		"	PRIMARY KEY (FILE_ID) " +  
		")";	
	private String SQL_DROP_TABLE_FS_FILE =
		"drop table " + SCHEMA + "FS_FILE";	
	private String SQL_CREATE_TABLE_FS_FILE =
		"create table " + SCHEMA + "FS_FILE ( " +  
		"	FILE_ID NUMERIC(15,0) NOT NULL, " + 
		"	FILE_DATA OID NOT NULL, " + 
		"	PRIMARY KEY (FILE_ID) " + 
		")";
	private String SQL_DROP_TABLE_FS_FILE_STORE =
		"drop table " + SCHEMA + "FS_FILE_STORE";	
	private String SQL_CREATE_TABLE_FS_FILE_STORE =
		"create table " + SCHEMA + "FS_FILE_STORE ( " +  
		"	STORE_ID NUMERIC(15,0) NOT NULL, " + 
		"	STORE_NAME CHARACTER VARYING(250) NOT NULL, " + 
		"	STORE_DESCRIPTION CHARACTER VARYING(4000) NOT NULL, " + 
		"	STORE_PATH CHARACTER VARYING(2000) NOT NULL, " + 
		"	NODE_ID NUMERIC(15,0) NOT NULL, " + 
		"	CREATION_DATE TIMESTAMP NOT NULL,  " + 
		"	UPDATED_DATE TIMESTAMP NOT NULL,  " + 
		"	PRIMARY KEY (STORE_ID)  " + 
		")";
	*/
	
	/***********************************************************************************
	 * 
	 * File 2 tables
	 * 
	 */
	private String SQL_DROP_TABLE_FS_PATH_RESOURCE =
		"drop table " + SCHEMA + "FS_PATH_RESOURCE";	
	private String SQL_CREATE_TABLE_FS_PATH_RESOURCE =
		"create table " + SCHEMA + "FS_PATH_RESOURCE ( " +  
		"	NODE_ID NUMERIC(15,0) NOT NULL, " + 
		"	STORE_ID NUMERIC(15,0) NOT NULL, " + 
		"	NAME CHARACTER VARYING(250) NOT NULL, " +
		"	PATH_TYPE CHARACTER VARYING(250) NOT NULL, " +
		"	RELATIVE_PATH CHARACTER VARYING(250) NOT NULL, " +
		"	PRIMARY KEY (NODE_ID) " + 
		")";
	private String SQL_DROP_TABLE_FS_FILE_META_RESOURCE =
		"drop table " + SCHEMA + "FS_FILE_META_RESOURCE";	
	private String SQL_CREATE_TABLE_FS_FILE_META_RESOURCE =
		"create table " + SCHEMA + "FS_FILE_META_RESOURCE ( " +  
		"	NODE_ID NUMERIC(15,0) NOT NULL, " + 
		"	FILE_SIZE NUMERIC(15,0) NOT NULL, " +
		"   MIME_TYPE CHARACTER VARYING(100), " +
		"	PRIMARY KEY (NODE_ID) " + 
		")";
	private String SQL_DROP_TABLE_FS_FILE_RESOURCE =
		"drop table " + SCHEMA + "FS_FILE_RESOURCE";	
	private String SQL_CREATE_TABLE_FS_FILE_RESOURCE =
		"create table " + SCHEMA + "FS_FILE_RESOURCE ( " +  
		"	NODE_ID NUMERIC(15,0) NOT NULL, " + 
		"	FILE_DATA OID NOT NULL, " + 
		"	PRIMARY KEY (NODE_ID) " + 
		")";	
	private String SQL_DROP_TABLE_FS_DIRECTORY_RESOURCE =
		"drop table " + SCHEMA + "FS_DIRECTORY_RESOURCE";	
	private String SQL_CREATE_TABLE_FS_DIRECTORY_RESOURCE =
		"create table " + SCHEMA + "FS_DIRECTORY_RESOURCE ( " +  
		"	NODE_ID NUMERIC(15,0) NOT NULL, " + 
		"	PRIMARY KEY (NODE_ID) " + 
		")";
	private String SQL_DROP_TABLE_FS_RESOURCE_STORE =
		"drop table " + SCHEMA + "FS_RESOURCE_STORE";	
	private String SQL_CREATE_TABLE_FS_RESOURCE_STORE =
		"create table " + SCHEMA + "FS_RESOURCE_STORE ( " +  
		"	STORE_ID NUMERIC(15,0) NOT NULL, " + 
		"	STORE_NAME CHARACTER VARYING(250) NOT NULL, " + 
		"	STORE_DESCRIPTION CHARACTER VARYING(4000) NOT NULL, " + 
		"	STORE_PATH CHARACTER VARYING(2000) NOT NULL, " + 
		"	NODE_ID NUMERIC(15,0) NOT NULL, " + 
		"	CREATION_DATE TIMESTAMP NOT NULL,  " + 
		"	UPDATED_DATE TIMESTAMP NOT NULL,  " + 
		"	PRIMARY KEY (STORE_ID)  " + 
		")";
	
	/***********************************************************************************
	 * 
	 * CMS tables
	 * 
	 */
	private String SQL_DROP_TABLE_FS_CMS_SITE =
		"drop table " + SCHEMA + "FS_CMS_SITE";	
	private String SQL_CREATE_TABLE_FS_CMS_SITE =
		"create table " + SCHEMA + "FS_CMS_SITE ( " +  
		"	SITE_ID NUMERIC(15,0) NOT NULL, " + 
		"	SITE_NAME CHARACTER VARYING(250) NOT NULL, " + 
		"	SITE_DESCRIPTION CHARACTER VARYING(4000) NOT NULL, " + 
		"	OFFLINE_STORE_ID NUMERIC(15,0) NOT NULL, " +
		"	ONLINE_STORE_ID NUMERIC(15,0) NOT NULL, " +
		"	CREATION_DATE TIMESTAMP NOT NULL,  " + 
		"	UPDATED_DATE TIMESTAMP NOT NULL,  " + 
		"	PRIMARY KEY (SITE_ID)  " + 
		")";
	
	/***********************************************************************************
	 * 
	 * Constraints
	 * 
	 */
	/*
	private String SQL_DROP_CONSTRAINT_FK_FS_FILE_RESOURCE =
		"ALTER TABLE " + SCHEMA + "FS_FILE_RESOURCE DROP CONSTRAINT FK_FS_FILE_RESOURCE";		
	private String SQL_CREATE_CONSTRAINT_FK_FS_FILE_RESOURCE =
		"ALTER TABLE " + SCHEMA + "FS_FILE_RESOURCE ADD CONSTRAINT FK_FS_FILE_RESOURCE " +  
		"  FOREIGN KEY (NODE_ID) " +  
		"  REFERENCES " + SCHEMA + "FS_FILE_META_RESOURCE(NODE_ID)";
	*/
	private String SQL_DROP_CONSTRAINT_FK_FS_FILE_META_RESOURCE =
		"ALTER TABLE " + SCHEMA + "FS_FILE_META_RESOURCE DROP CONSTRAINT FK_FS_FILE_META_RESOURCE";		
	private String SQL_CREATE_CONSTRAINT_FK_FS_FILE_META_RESOURCE =
		"ALTER TABLE " + SCHEMA + "FS_FILE_META_RESOURCE ADD CONSTRAINT FK_FS_FILE_META_RESOURCE " +  
		"  FOREIGN KEY (NODE_ID) " +  
		"  REFERENCES " + SCHEMA + "FS_PATH_RESOURCE(NODE_ID)";
	private String SQL_DROP_CONSTRAINT_FK_FS_DIRECTORY_RESOURCE =
		"ALTER TABLE " + SCHEMA + "FS_DIRECTORY_RESOURCE DROP CONSTRAINT FK_FS_DIRECTORY_RESOURCE";		
	private String SQL_CREATE_CONSTRAINT_FK_FS_DIRECTORY_RESOURCE =
		"ALTER TABLE " + SCHEMA + "FS_DIRECTORY_RESOURCE ADD CONSTRAINT FK_FS_DIRECTORY_RESOURCE " +  
		"  FOREIGN KEY (NODE_ID) " +  
		"  REFERENCES " + SCHEMA + "FS_PATH_RESOURCE(NODE_ID)";
	private String SQL_DROP_CONSTRAINT_FK_FS_PATH_RESOURCE =
		"ALTER TABLE " + SCHEMA + "FS_PATH_RESOURCE DROP CONSTRAINT FK_FS_PATH_RESOURCE";		
	private String SQL_CREATE_CONSTRAINT_FK_FS_PATH_RESOURCE =
		"ALTER TABLE " + SCHEMA + "FS_PATH_RESOURCE ADD CONSTRAINT FK_FS_PATH_RESOURCE " +  
		"  FOREIGN KEY (NODE_ID) " +  
		"  REFERENCES " + SCHEMA + "FS_NODE(NODE_ID)";	
	
	/***********************************************************************************
	 * 
	 * Indexes
	 * 
	 */
	private String SQL_DROP_INDEX_FS_PARENT_DEPTH_CHILD =
		"drop index " + SCHEMA + "fs_parent_depth_child_idx";
	private String SQL_CREATE_INDEX_FS_PARENT_DEPTH_CHILD =
		"create unique index fs_parent_depth_child_idx on " + SCHEMA + "fs_closure(parent_node_id,depth,child_node_id)";
	private String SQL_DROP_INDEX_FS_CHILD_PARENT_DEPTH =
		"drop index " + SCHEMA + "fs_child_parent_depth_idx";	
	private String SQL_CREATE_INDEX_FS_CHILD_PARENT_DEPTH =
		"create unique index fs_child_parent_depth_idx on " + SCHEMA + "fs_closure(child_node_id,parent_node_id,depth)";	
	
	/***********************************************************************************
	 * 
	 * Sequences
	 * 
	 */
	private String SQL_DROP_SEQUENCE_FS_NODE_ID =
		"drop sequence " + SCHEMA + "FS_NODE_ID_SEQUENCE";
	private String SQL_CREATE_SEQUENCE_FS_NODE_ID =
		"CREATE SEQUENCE " + SCHEMA + "FS_NODE_ID_SEQUENCE " + 
		"INCREMENT BY 1 " +
		"START WITH 1 " +
		"CACHE 10  " +
		"NO CYCLE";	
	private String SQL_DROP_SEQUENCE_FS_LINK_ID =
		"drop sequence " + SCHEMA + "FS_LINK_ID_SEQUENCE";	
	private String SQL_CREATE_SEQUENCE_FS_LINK_ID =
		"CREATE SEQUENCE " + SCHEMA + "FS_LINK_ID_SEQUENCE " + 
		"INCREMENT BY 1 " +
		"START WITH 1 " +
		"CACHE 10  " +
		"NO CYCLE";	
	// used in our prune table
	private String SQL_DROP_SEQUENCE_FS_PRUNE_ID =
		"drop sequence " + SCHEMA + "FS_PRUNE_ID_SEQUENCE";	
	private String SQL_CREATE_SEQUENCE_FS_PRUNE_ID =
		"CREATE SEQUENCE " + SCHEMA + "FS_PRUNE_ID_SEQUENCE " + 
		"INCREMENT BY 1 " +
		"START WITH 1 " +
		"CACHE 10  " +
		"NO CYCLE";	
	private String SQL_DROP_SEQUENCE_FS_TREE_ID =
		"drop sequence " + SCHEMA + "FS_TREE_ID_SEQUENCE";	
	private String SQL_CREATE_SEQUENCE_FS_TREE_ID =
		"CREATE SEQUENCE " + SCHEMA + "FS_TREE_ID_SEQUENCE " + 
		"INCREMENT BY 1 " +
		"START WITH 1 " +
		"CACHE 10  " +
		"NO CYCLE";
	private String SQL_DROP_SEQUENCE_FS_FILE_ID =
		"drop sequence " + SCHEMA + "FS_FILE_ID_SEQUENCE";	
	private String SQL_CREATE_SEQUENCE_FS_FILE_ID =
		"CREATE SEQUENCE " + SCHEMA + "FS_FILE_ID_SEQUENCE " + 
		"INCREMENT BY 1 " +
		"START WITH 1 " +
		"CACHE 10  " +
		"NO CYCLE";
	private String SQL_DROP_SEQUENCE_FS_STORE_ID =
		"drop sequence " + SCHEMA + "FS_STORE_ID_SEQUENCE";	
	private String SQL_CREATE_SEQUENCE_FS_STORE_ID =
		"CREATE SEQUENCE " + SCHEMA + "FS_STORE_ID_SEQUENCE " + 
		"INCREMENT BY 1 " +
		"START WITH 1 " +
		"CACHE 10  " +
		"NO CYCLE";	
	private String SQL_DROP_SEQUENCE_FS_CMS_SITE_ID =
		"drop sequence " + SCHEMA + "FS_CMS_SITE_ID_SEQUENCE";	
	private String SQL_CREATE_SEQUENCE_FS_CMS_SITE_ID =
		"CREATE SEQUENCE " + SCHEMA + "FS_CMS_SITE_ID_SEQUENCE " + 
		"INCREMENT BY 1 " +
		"START WITH 1 " +
		"CACHE 10  " +
		"NO CYCLE";
	
	
	public PostgreSQLCreate() {
		
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
		
		// core tables
		entityManager.createNativeQuery(SQL_CREATE_TABLE_FS_PRUNE).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_TABLE_FS_NODE).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_TABLE_FS_CLOSURE).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_TABLE_FS_TREE).executeUpdate();
		
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
		
		// sequences
		entityManager.createNativeQuery(SQL_DROP_SEQUENCE_FS_PRUNE_ID).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_SEQUENCE_FS_NODE_ID).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_SEQUENCE_FS_LINK_ID).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_SEQUENCE_FS_TREE_ID).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_SEQUENCE_FS_FILE_ID).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_SEQUENCE_FS_STORE_ID).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_SEQUENCE_FS_CMS_SITE_ID).executeUpdate();
		
	}
	
	/**
	 * Reset database objects
	 * 
	 * @throws DatabaseException
	 */
	public void resetDatabase() throws DatabaseException {
		
		// drop
		dropDatabase();
		
		// add
		createDatabase();		
		
	}

}
