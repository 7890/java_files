--COMMENT
DROP SEQUENCE seq_tbl_file_id;
CREATE SEQUENCE seq_tbl_file_id INCREMENT 1 START 0;

DROP SEQUENCE seq_tbl_file_link_id;
CREATE SEQUENCE seq_tbl_file_link_id INCREMENT 1 START 0;

DROP SEQUENCE seq_tbl_basket_id;
CREATE SEQUENCE seq_tbl_basket_id INCREMENT 1 START 0;

DROP SEQUENCE seq_tbl_basket_file_link_id;
CREATE SEQUENCE seq_tbl_basket_file_link_id INCREMENT 1 START 0;

DROP TABLE IF EXISTS tbl_file;
DROP TABLE IF EXISTS tbl_file_link;
DROP TABLE IF EXISTS tbl_basket;
DROP TABLE IF EXISTS tbl_basket_file_link;
--DELETE FROM tbl_file;
--CREATE TABLE IF NOT EXISTS tbl_file;

--=============================================================================
CREATE TABLE tbl_file
(
	id 		INTEGER	NOT NULL,
	filename 	STRING(256) NOT NULL,
	displayname 	STRING(256) NOT NULL,
	uri 		STRING(1024) NOT NULL,
	mimetype 	STRING(256),
	length 		INTEGER NOT NULL,
	md5sum 		STRING(32),
	lastmodified 	BIGINT NOT NULL,
	canwrite 	BOOLEAN NOT NULL,
	canexecute 	BOOLEAN NOT NULL,
	UNIQUE (id)
);

--=============================================================================
CREATE TABLE tbl_file_link
(
	id 		INTEGER NOT NULL,
	id_file		INTEGER NOT NULL,
	link		STRING(256) NOT NULL,
	UNIQUE (id),
	UNIQUE (link)
);

--INSERT INTO tbl_file_link (id,id_file,link) VALUES (0,0,'foo');
--INSERT INTO tbl_file_link (id,id_file,link) VALUES (1,1,'bar');

--=============================================================================
CREATE TABLE tbl_basket
(
	id 		INTEGER	NOT NULL,
	name 		STRING(256) NOT NULL,
	link		STRING(256) NOT NULL,
	UNIQUE (id),
	UNIQUE (link)
);
--INSERT INTO tbl_basket (id,name,link) VALUES (1,'my basket','foo');
--INSERT INTO tbl_basket (id,name,link) VALUES (2,'my other basket','bar');

--=============================================================================
CREATE TABLE tbl_basket_file_link
(
	id 		INTEGER	NOT NULL,
	id_basket	INTEGER NOT NULL,
	id_file		INTEGER NOT NULL,
	UNIQUE(id)
);
--INSERT INTO tbl_basket_file_link (id,id_basket,id_file) VALUES (1,1,1);
--INSERT INTO tbl_basket_file_link (id,id_basket,id_file) VALUES (2,1,2);
--INSERT INTO tbl_basket_file_link (id,id_basket,id_file) VALUES (3,2,3);
--INSERT INTO tbl_basket_file_link (id,id_basket,id_file) VALUES (4,2,4);

--=============================================================================
DROP VIEW v_basket_files;
CREATE VIEW v_basket_files AS
	SELECT
		a.*
		,b.id AS id_file
		,b.filename
		,b.displayname
		,b.uri
		,b.mimetype
		,b.length
		,b.md5sum
		,b.lastmodified
		,b.canwrite
		,b.canexecute
	FROM
		tbl_basket AS a
		,tbl_file AS b
		,tbl_basket_file_link AS c
	WHERE
		a.id=c.id_basket
	AND
		b.id=c.id_file;

--=============================================================================
DROP VIEW v_file_link_simple;
CREATE VIEW v_file_link_simple AS
	SELECT
		a.id
		,a.link
		,b.id AS id_file
		,b.displayname
		,b.uri
		,b.mimetype
		,b.length
	FROM
		tbl_file_link AS a
		,tbl_file AS b
	WHERE
		a.id=b.id;

--=============================================================================
DROP VIEW v_file_1;
CREATE VIEW v_file_1 AS
	SELECT
		*
		,1 AS cnt
	FROM
		tbl_file;

--=============================================================================
DROP VIEW v_mimetype_distribution;
CREATE VIEW v_mimetype_distribution AS
	SELECT
		mimetype
		,sum(cnt) as total
	FROM
		v_file_1
	GROUP BY
		mimetype;
