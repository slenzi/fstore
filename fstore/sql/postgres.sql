/**
 * PostgreSQL scripts
 */

ALTER TABLE TEST.FS_FILE_RESOURCE DROP CONSTRAINT FK_FS_FILE_RESOURCE;
ALTER TABLE TEST.FS_FILE_META_RESOURCE DROP CONSTRAINT FK_FS_FILE_META_RESOURCE;
ALTER TABLE TEST.FS_DIRECTORY_RESOURCE DROP CONSTRAINT FK_FS_DIRECTORY_RESOURCE;
ALTER TABLE TEST.FS_PATH_RESOURCE DROP CONSTRAINT FK_FS_PATH_RESOURCE;

drop sequence TEST.FS_NODE_ID_SEQUENCE;
drop sequence TEST.FS_LINK_ID_SEQUENCE;	
drop sequence TEST.FS_PRUNE_ID_SEQUENCE;
drop sequence TEST.FS_TREE_ID_SEQUENCE;

drop sequence TEST.FS_FILE_ID_SEQUENCE;
drop sequence TEST.FS_STORE_ID_SEQUENCE;

drop index TEST.fs_parent_depth_child_idx;
drop index TEST.fs_child_parent_depth_idx;	

drop table TEST.FS_NODE;
drop table TEST.FS_CLOSURE;	
drop table TEST.FS_PRUNE;
drop table TEST.FS_TREE;

drop table TEST.FS_TEST_NODE;

drop table TEST.FS_FILE;
drop table TEST.FS_FILE_ENTRY;
drop table TEST.FS_DIR_FILE_LINK;
drop table TEST.FS_DIRECTORY;
drop table TEST.FS_FILE_STORE;

drop table TEST.FS_FILE_RESOURCE;
drop table TEST.FS_FILE_META_RESOURCE;
drop table TEST.FS_DIRECTORY_RESOURCE;
drop table TEST.FS_PATH_RESOURCE;
drop table TEST.FS_RESOURCE_STORE;

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

/**
 * test table
 */
create table TEST.FS_TEST_NODE ( 
	NODE_ID NUMERIC(15,0) NOT NULL, 
	TEST_VALUE CHARACTER VARYING(250), 
	PRIMARY KEY (NODE_ID) 
);

/**
 * File 1 tables
 */
create table TEST.FS_DIRECTORY ( 
	NODE_ID NUMERIC(15,0) NOT NULL, 
	DIR_NAME CHARACTER VARYING(250),
	RELATIVE_DIR_PATH CHARACTER VARYING(250),
	PRIMARY KEY (NODE_ID) 
);
create table TEST.FS_DIR_FILE_LINK (
	NODE_ID NUMERIC(15,0) NOT NULL,
	FILE_ID NUMERIC(15,0) NOT NULL,
	PRIMARY KEY(NODE_ID,FILE_ID)
);
create table TEST.FS_FILE_ENTRY ( 
	FILE_ID NUMERIC(15,0) NOT NULL,
	FILE_SIZE NUMERIC(15,0) NOT NULL,
	FILE_NAME CHARACTER VARYING(250),
	PRIMARY KEY (FILE_ID) 
);
create table TEST.FS_FILE ( 
	FILE_ID NUMERIC(15,0) NOT NULL,
	FILE_DATA OID NOT NULL,
	PRIMARY KEY (FILE_ID) 
);
create table TEST.FS_FILE_STORE ( 
	STORE_ID NUMERIC(15,0) NOT NULL,
	STORE_NAME CHARACTER VARYING(250) NOT NULL,
	STORE_DESCRIPTION CHARACTER VARYING(4000) NOT NULL,
	STORE_PATH CHARACTER VARYING(2000) NOT NULL,
	NODE_ID NUMERIC(15,0) NOT NULL,
	CREATION_DATE TIMESTAMP NOT NULL, 
	UPDATED_DATE TIMESTAMP NOT NULL, 	
	PRIMARY KEY (STORE_ID) 
);

/**
 * File 2 tables
 */
create table TEST.FS_PATH_RESOURCE ( 
	NODE_ID NUMERIC(15,0) NOT NULL, 
	NAME CHARACTER VARYING(250)) NOT NULL,
	PATH_TYPE CHARACTER VARYING(250) NOT NULL,
	RELATIVE_PATH CHARACTER VARYING(250) NOT NULL,
	PRIMARY KEY (NODE_ID) 
);
create table TEST.FS_FILE_META_RESOURCE ( 
	NODE_ID NUMERIC(15,0) NOT NULL, 
	FILE_SIZE NUMERIC(15,0) NOT NULL,
	MIME_TYPE CHARACTER VARYING(100),
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
	CREATION_DATE TIMESTAMP NOT NULL, 
	UPDATED_DATE TIMESTAMP NOT NULL, 	
	PRIMARY KEY (STORE_ID) 
);

/**
 * File 2 Constraints
 */
ALTER TABLE TEST.FS_FILE_RESOURCE ADD CONSTRAINT FK_FS_FILE_RESOURCE
  FOREIGN KEY (NODE_ID)
  REFERENCES TEST.FS_FILE_META_RESOURCE(NODE_ID);
  
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