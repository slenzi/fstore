package org.lenzi.fstore.setup.db.postgresql;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.lenzi.fstore.repository.exception.DatabaseException;
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
	
	private String SQL_DROP_TABLE_FS_NODE =
			"drop table " + SCHEMA + "FS_NODE";
		private String SQL_CREATE_TABLE_FS_NODE =
			"create table " + SCHEMA + "FS_NODE ( " +
			"	NODE_ID NUMERIC(15,0) NOT NULL, " +
			"	PARENT_NODE_ID NUMERIC(15,0) NOT NULL, " +
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
		
		private String SQL_DROP_INDEX_FS_PARENT_DEPTH_CHILD =
			"drop index " + SCHEMA + "fs_parent_depth_child_idx";
		private String SQL_CREATE_INDEX_FS_PARENT_DEPTH_CHILD =
			"create unique index fs_parent_depth_child_idx on " + SCHEMA + "fs_closure(parent_node_id,depth,child_node_id)";
		
		private String SQL_DROP_INDEX_FS_CHILD_PARENT_DEPTH =
			"drop index " + SCHEMA + "fs_child_parent_depth_idx";	
		private String SQL_CREATE_INDEX_FS_CHILD_PARENT_DEPTH =
			"create unique index fs_child_parent_depth_idx on " + SCHEMA + "fs_closure(child_node_id,parent_node_id,depth)";	
		
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
		
		// add
		entityManager.createNativeQuery(SQL_CREATE_SEQUENCE_FS_PRUNE_ID).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_SEQUENCE_FS_NODE_ID).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_SEQUENCE_FS_LINK_ID).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_SEQUENCE_FS_TREE_ID).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_TABLE_FS_PRUNE).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_TABLE_FS_NODE).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_TABLE_FS_CLOSURE).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_TABLE_FS_TREE).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_INDEX_FS_PARENT_DEPTH_CHILD).executeUpdate();
		entityManager.createNativeQuery(SQL_CREATE_INDEX_FS_CHILD_PARENT_DEPTH).executeUpdate();		
		
	}
	
	/**
	 * Drop database objects
	 * 
	 * @throws DatabaseException
	 */
	public void dropDatabase() throws DatabaseException {
		
		// drop
		entityManager.createNativeQuery(SQL_DROP_INDEX_FS_PARENT_DEPTH_CHILD).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_INDEX_FS_CHILD_PARENT_DEPTH).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_SEQUENCE_FS_PRUNE_ID).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_SEQUENCE_FS_NODE_ID).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_SEQUENCE_FS_LINK_ID).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_SEQUENCE_FS_TREE_ID).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_TABLE_FS_PRUNE).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_TABLE_FS_NODE).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_TABLE_FS_CLOSURE).executeUpdate();
		entityManager.createNativeQuery(SQL_DROP_TABLE_FS_TREE).executeUpdate();		
		
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