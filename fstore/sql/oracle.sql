/**
 * Oracle scripts
 * 
 * Get length of lob:
 * select dbms_lob.getlength(f.file_data) from fs_cms_file f
 */

drop sequence FS_NODE_ID_SEQUENCE;
drop sequence FS_LINK_ID_SEQUENCE;
drop sequence FS_PRUNE_ID_SEQUENCE;
drop sequence FS_TREE_ID_SEQUENCE;

drop sequence FS_FILE_ID_SEQUENCE;
drop sequence FS_STORE_ID_SEQUENCE;

drop index fs_parent_depth_child_idx;
drop index fs_child_parent_depth_idx;

drop table FS_NODE;
drop table FS_CLOSURE;
drop table FS_PRUNE;
drop table FS_TREE;

drop table FS_TEST_NODE;

drop table FS_FILE;
drop table FS_FILE_ENTRY;
drop table FS_DIRECTORY;
drop table FS_DIR_FILE_LINK;
drop table FS_FILE_STORE;

drop table FS_PATH_RESOURCE;
drop table FS_FILE_META_RESOURCE;
drop table FS_FILE_RESOURCE;
drop table FS_DIRECTORY_RESOURCE;
drop table FS_RESOURCE_STORE;

/**
 * core tree tables
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
 * test table
 */
create table FS_TEST_NODE ( 
	NODE_ID NUMBER(15,0) NOT NULL, 
	TEST_VALUE VARCHAR2(250), 
	PRIMARY KEY (NODE_ID) 
);

/**
 * File 1 tables
 */
create table FS_DIRECTORY ( 
	NODE_ID NUMBER(15,0) NOT NULL, 
	DIR_NAME VARCHAR2(250) NOT NULL,
	RELATIVE_DIR_PATH VARCHAR2(250) NOT NULL,
	PRIMARY KEY (NODE_ID) 
);
create table FS_DIR_FILE_LINK (
	NODE_ID NUMBER(15,0) NOT NULL,
	FILE_ID NUMBER(15,0) NOT NULL,
	PRIMARY KEY(NODE_ID,FILE_ID)
);
create table FS_FILE_ENTRY ( 
	FILE_ID NUMBER(15,0) NOT NULL,
	FILE_SIZE NUMBER(15,0) NOT NULL,
	FILE_NAME VARCHAR2(250) NOT NULL, 
	PRIMARY KEY (FILE_ID) 
);
create table FS_FILE ( 
	FILE_ID NUMBER(15,0) NOT NULL,
	FILE_DATA BLOB NOT NULL,
	PRIMARY KEY (FILE_ID) 
);
create table FS_FILE_STORE ( 
	STORE_ID NUMBER(15,0) NOT NULL,
	STORE_NAME VARCHAR2(250) NOT NULL,
	STORE_DESCRIPTION VARCHAR2(4000) NOT NULL,
	STORE_PATH VARCHAR2(2000) NOT NULL,
	NODE_ID NUMBER(15,0) NOT NULL,
	CREATION_DATE date NOT NULL, 
	UPDATED_DATE date NOT NULL, 	
	PRIMARY KEY (STORE_ID) 
);

/**
 * File 2 tables
 */
create table FS_PATH_RESOURCE ( 
	NODE_ID NUMBER(15,0) NOT NULL, 
	NAME VARCHAR2(250) NOT NULL,
	PATH_TYPE VARCHAR2(250) NOT NULL,
	RELATIVE_PATH VARCHAR2(250) NOT NULL,
	PRIMARY KEY (NODE_ID) 
);
create table FS_FILE_META_RESOURCE ( 
	NODE_ID NUMBER(15,0) NOT NULL, 
	FILE_SIZE NUMBER(15,0) NOT NULL,
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
	CREATION_DATE date NOT NULL, 
	UPDATED_DATE date NOT NULL, 	
	PRIMARY KEY (STORE_ID) 
);

create unique index fs_parent_depth_child_idx on fs_closure(parent_node_id,depth,child_node_id);
create unique index fs_child_parent_depth_idx on fs_closure(child_node_id,parent_node_id,depth);	

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