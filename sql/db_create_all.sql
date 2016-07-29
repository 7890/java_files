--COMMENT
DROP SEQUENCE seq_tbl_file_id;
CREATE SEQUENCE seq_tbl_file_id INCREMENT 1 START 0;

DROP TABLE IF EXISTS tbl_file;
--DELETE FROM tbl_file;
--CREATE TABLE IF NOT EXISTS tbl_file;

--=============================================================================
CREATE TABLE tbl_file
(
	id INTEGER 	NOT NULL UNIQUE,
	filename 	STRING(256) NOT NULL,
	displayname 	STRING(256) NOT NULL,
	uri 		STRING(1024) NOT NULL,
	mimetype 	STRING(256),
	length 		INTEGER NOT NULL,
	md5sum 		STRING(32),
	lastmodified 	BIGINT NOT NULL,
	canwrite 	BOOLEAN NOT NULL,
	canexecute 	BOOLEAN NOT NULL
);

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
