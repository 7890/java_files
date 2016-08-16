#!/bin/sh

if [ $# -ne 3 ]
then
	echo "syntax error. need 4 parameters:" >&2
	echo "[table name] [file name] [number of processes]" >&2
	echo "example:" >&2
	echo "myfiles myfiles.dump 4" >&2
	exit 1
fi

TABLE="$1"
FILE="$2"
PROC="$3"

PARTS=`mktemp -d`

#split input file (one record per line)

LINES="`cat \"$FILE\" | wc -l`"
LINES_PER_PART=`echo "$LINES / $PROC" | bc`
echo "splitting to $LINES_PER_PART lines per file of total $LINES lines"
split "$FILE" --lines $LINES_PER_PART "$PARTS"/sql_import_part_

echo "tmpdir: $PARTS"
echo "using $PROC processes"

ls -1 "$PARTS"/sql_import_part_* | while read line
do
	CMD_FILE=`mktemp`

	echo -n "import " > "$CMD_FILE"
	echo -n "$TABLE " >>  "$CMD_FILE"
	echo "$line " >> "$CMD_FILE"

	echo "tmpfile: $CMD_FILE"
	cat "$CMD_FILE"

	echo "===================launching in background"
	java -cp .:archive/mckoidb.jar:_build util.Impex -f "$CMD_FILE" -url "jdbc:mckoi://localhost" -u "admin" -p "admin" &
	sleep 2
	rm -f "$CMD_FILE"
done

rm -rf "$PARTS"
