#!/bin/bash

FULLPATH="`pwd`/$0"
DIR=`dirname "$FULLPATH"`

src="$DIR"/src
build="$DIR"/_build
archive="$DIR"/archive
doc="$DIR"/doc
javadoc="$DIR"/javadoc

MCKOI="$archive/mckoidb.jar"
TIKA="$archive/tika-server-1.13.jar"
CLI_SRC="$archive/commons-cli-1.3.1-src.tar.gz"

db="$DIR"/db
USER="admin"
PW="admin"
DB_CONF="$DIR"/db.conf
#overrides db.conf
DB_DATA_PATH="$db"/data
DB_SQL_PATH="$DIR"/sql

#linux / osx different mktemp call
#TMPFILE=`mktemp 2>/dev/null || mktemp -t /tmp`
#NOW=`date +"%s"`

jsource=1.6
jtarget=1.6

JAVAC="javac -source $jsource -target $jtarget -nowarn"
#JAVA="java -Xms500M -Xmx1000M"
JAVA="java -Xmx1000M"

#========================================================================
checkAvail()
{
	which "$1" >/dev/null 2>&1
	ret=$?
	if [ $ret -ne 0 ]
	then
		echo "tool \"$1\" not found. please install"
		exit 1
	fi
}

#========================================================================
compile()
{
	echo "building..."
	echo "==========="

	cp "$CLI_SRC" "$build"
	cd "$build"
	tar xf *.tar.gz

	#don't build tests (uses junit)
	rm -rf commons-cli-1.3.1-src/src/test

	shopt -s globstar

	$JAVAC -classpath "$DIR" -sourcepath "$build"/commons-cli-1.3.1-src -d "$build" \
		"$build"/commons-cli-1.3.1-src/**/*.java

	$JAVAC -classpath "$DIR":"$build":"$MCKOI":"$TIKA" -sourcepath "$src" -d "$build" \
		"$src"/*.java "$src"/util/*.java "$src"/interfaces/*.java "$src"/hooks/*.java
}

#========================================================================
create_mckoi_javadoc()
{
	mkdir -p "$javadoc"
	rm -rf "$javadoc"/*
	cp "$archive"/mckoi1.0.6.zip "$javadoc"
	cd "$javadoc"
	unzip mckoi1.0.6.zip
	mv mckoi1.0.6/src.zip .
	unzip src.zip
	shopt -s globstar
	javadoc src/**/*.java
#	rm -rf src
	rm -rf mckoi1.0.6
	rm -f *.zip
}

#========================================================================
init_db()
{
	mkdir -p "$db"
	echo db path: "$DB_DATA_PATH"

	#check if data directory exists
	if [ -d "$DB_DATA_PATH" ]
	then
		echo "db exists (won't create new)"
		echo "start database server now (./start_server.sh) in another terminal now and hit enter"
		read a
		return
	fi

	echo "initializing database..."
	#create database in sub directory "data" (see db.conf)
	$JAVA -jar "$MCKOI" -conf "$DB_CONF" -dbpath "$DB_DATA_PATH" -create "$USER" "$PW"

	echo "new database created."
	echo "start database server now (./start_server.sh) in another terminal now and hit enter"
	read a

	echo "creating tables, sequences, views in database"
	echo "==="
	cat "$DB_SQL_PATH"/db_create_all.sql
	echo "==="
	cat "$DB_SQL_PATH"/db_create_all.sql | $JAVA -cp "$DIR":"$build":"$MCKOI" ExecSQL
	echo "db init done."
}

#========================================================================
create_run_scripts()
{
	echo "#!/bin/sh" > "$DIR"/ForAllFilesDo.sh
	echo $JAVA -classpath "$DIR":"$build":"$MCKOI":"$TIKA" \\ >> "$DIR"/ForAllFilesDo.sh
	echo '	ForAllFilesDo $@' >> "$DIR"/ForAllFilesDo.sh
	chmod +x "$DIR"/ForAllFilesDo.sh
#	$JAVA -classpath "$DIR":"$build":"$MCKOI":"$TIKA" ForAllFilesDo .

#	java -verbose:class ... >/tmp/out.txt 2>&1
#	cat /tmp/out.txt | grep "\[Loaded" | grep "\.jar" | rev | cut -d"/" -f1 | rev | sort | uniq | cut -d"]" -f1 >/tmp/jars.txt

	echo "#!/bin/sh" > "$DIR"/ExecSQL.sh
	echo $JAVA -classpath "$DIR":"$build":"$MCKOI":"$TIKA" \\ >> "$DIR"/ExecSQL.sh
	echo '  ExecSQL $@' >> "$DIR"/ExecSQL.sh
	chmod +x "$DIR"/ExecSQL.sh
#	echo "test querying database"
#	echo "echo \"select count(*) from tbl_file;\"" \| $JAVA -classpath "$DIR":"$build":"$MCKOI":"$TIKA" ExecSQL
#	echo "select count(*) from tbl_file;" | $JAVA -classpath "$DIR":"$build":"$MCKOI":"$TIKA" ExecSQL

	echo "see generated scripts ForAllFilesDo.sh, ExecSQL.sh"
	echo "query tbl_file manually (./start_gui.sh)"
}

for tool in java javac jar javadoc unzip; \
	do checkAvail "$tool"; done

mkdir -p "$build"
rm -rf "$build"/*

#create_mckoi_javadoc
compile
init_db
create_run_scripts
