#!/bin/sh
#java -cp _build -jar archive/mckoidb.jar -c db.conf
java -cp .:_build:archive/mckoidb.jar com.mckoi.runtime.McKoiDBMain -conf db.conf
