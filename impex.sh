#!/bin/sh

if [ $# -ne 3 ]
then
	echo "syntax error. need 3 parameters:" >&2
	echo "[action] [table name] [file name]" >&2
	echo "actions: import, export" >&2
	echo "example:" >&2
	echo "export myfiles myfiles.dump" >&2
	exit 1
fi

ACTION="$1"
TABLE="$2"
FILE="$3"

CMD_FILE=`mktemp`

echo -n "$ACTION " > "$CMD_FILE"
echo -n "$TABLE " >>  "$CMD_FILE"
echo "$FILE " >> "$CMD_FILE"

echo "$CMD_FILE"
cat "$CMD_FILE"

java -cp .:archive/mckoidb.jar:_build util.Impex -f "$CMD_FILE" -url "jdbc:mckoi://localhost" -u "admin" -p "admin"

rm -f "$CMD_FILE"
