
/*
 * Postgres database setup
 */

drop sequence FS_NODE_ID_SEQUENCE;
drop sequence FS_LINK_ID_SEQUENCE;
drop sequence FS_PRUNE_ID_SEQUENCE;
drop sequence FS_TREE_ID_SEQUENCE;

drop index fs_parent_depth_child_idx;
drop index fs_child_parent_depth_idx;

drop table FS_NODE;
drop table FS_CLOSURE;
drop table FS_PRUNE;
drop table FS_TREE;

/*
 * Master list of all nodes.
 *
 * NODE_ID - Unique ID for the node
 * PARENT_NODE_ID - Parent node ID for this node.
 * NAME - Node name (e.g. path or dir name).
 * CREATION_DATE - Date node was originally created.
 * UPDATED_DATE - Date node was updated (renamed, moved, children added)
 */
create table TEST.FS_NODE (
	NODE_ID NUMERIC(15,0) NOT NULL,
	PARENT_NODE_ID NUMERIC(15,0) NOT NULL,
	NAME CHARACTER VARYING(250) NOT NULL,
	CREATION_DATE TIMESTAMP NOT NULL,
	UPDATED_DATE TIMESTAMP NOT NULL,
	PRIMARY KEY (NODE_ID)
);

/*
 * Maintains parent-child relationship for all nodes.
 *
 * LINK_ID - Unique id for the link entry
 * PARENT_NODE_ID - Parent node ID
 * CHILD_NODE_ID - Child node ID
 * DEPTH - Depth / distance the child is from the parent in the tree structure.
 */
create table TEST.FS_CLOSURE (
	LINK_ID NUMERIC(15,0) NOT NULL,
	PARENT_NODE_ID NUMERIC(15,0) NOT NULL,
	CHILD_NODE_ID NUMERIC(15,0) NOT NULL,
	DEPTH NUMERIC(5,0) NOT NULL,
	PRIMARY KEY (LINK_ID)
);

/*
 * Table used in delete operations
 * 
 * PRUNE_ID - Unique ID for the prune operation
 * NODE_ID - Node to prune
 */
create table TEST.FS_PRUNE (
	PRUNE_ID NUMERIC(15,0) NOT NULL,
	NODE_ID NUMERIC(15,0) NOT NULL,
	PRIMARY KEY (PRUNE_ID,NODE_ID)
);

/*
 * List of all trees, linked to their root node in FS_NODE
 */
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
 * Indexes
 */
create unique index fs_parent_depth_child_idx on test.fs_closure(parent_node_id, depth, child_node_id);
create unique index fs_child_parent_depth_idx on test.fs_closure(child_node_id, parent_node_id, depth);

/**
 * Sequence for node IDs
 */
CREATE SEQUENCE TEST.FS_NODE_ID_SEQUENCE 
	MINVALUE 1 
	INCREMENT BY 1 
	START WITH 1
	CACHE 10
	NO CYCLE;
	
/**
 * Sequence for link IDs, used in closure table.
 */
CREATE SEQUENCE TEST.FS_LINK_ID_SEQUENCE 
	MINVALUE 1 
	INCREMENT BY 1 
	START WITH 1
	CACHE 10
	NO CYCLE;

/**
 * Sequence for prune_id in the fs_prune table.
 */
CREATE SEQUENCE TEST.FS_PRUNE_ID_SEQUENCE
	MINVALUE 1 
	INCREMENT BY 1 
	START WITH 1
	CACHE 10
	NO CYCLE;
	
/**
 * Sequence for tree IDs
 */
CREATE SEQUENCE TEST.FS_TREE_ID_SEQUENCE 
	MINVALUE 1 
	INCREMENT BY 1 
	START WITH 1
	CACHE 10
	NO CYCLE;