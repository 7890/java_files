#!/bin/sh
#java -cp archive/mckoidb.jar com.mckoi.tools.JDBCScriptTool -url "jdbc:mckoi:local://./db.conf" -u "admin" -p "admin"
java -cp archive/mckoidb.jar com.mckoi.tools.JDBCScriptTool -url "jdbc:mckoi://localhost" -u "admin" -p "admin"
