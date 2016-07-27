--COMMENT
DROP SEQUENCE seq_tbl_file_id;
DROP TABLE IF EXISTS tbl_file;
--DELETE FROM tbl_file;
CREATE SEQUENCE seq_tbl_file_id INCREMENT 1 START 0;
--CREATE TABLE IF NOT EXISTS tbl_file;
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
