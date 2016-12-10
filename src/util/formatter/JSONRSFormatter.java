package util.formatter;

import java.io.OutputStreamWriter;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import java.util.*;

//tb/1612

//format all records found in resultset in a JSON text string
//
// [
//   {"k1":v1, "k2":"v2"}
//   ,{"k1": .....}
//   ...
// ]

//to be used with java_files, java_http sql query handler, ..

//=============================================================================
//=============================================================================
public class JSONRSFormatter extends CommonRSFormatter
{
//=============================================================================
	public void formatRSImpl(ResultSet rs) throws Exception
	{
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();

		int record_index=0;

		writeOut("[\n");

		while (rs.next())
		{
			if(record_index>0)
			{
				writeOut(",");
			}
			writeOut("{ ");

			for(int i=1;i<=columnCount;i++)
			{
				int coltype=rsmd.getColumnType(i);

				writeOut("\""+escapeStringForJSON(rsmd.getColumnName(i))+"\": ");

				String val=rs.getString(i);
				if(val==null)
				{
					val="null";
				}

				if(coltype==java.sql.Types.BIGINT
					|| coltype==java.sql.Types.DECIMAL
					|| coltype==java.sql.Types.DOUBLE
					|| coltype==java.sql.Types.FLOAT
					|| coltype==java.sql.Types.INTEGER
					|| coltype==java.sql.Types.NUMERIC
					|| coltype==java.sql.Types.REAL
					|| coltype==java.sql.Types.SMALLINT
					/*|| coltype==java.sql.Types.TIMESTAMP*/
					|| coltype==java.sql.Types.TINYINT
				)
				{
					writeOut(val);
				}
				else
				{
					writeOut("\""+escapeStringForJSON(val)+"\"");
				}

				if(i<columnCount)
				{
					writeOut(", ");
				}
			}//end for all columns
			writeOut(" }\n");
			record_index++;
		}//end while(rs.next())
		writeOut("]\n");
	}//end formatRSImpl()

//=============================================================================
	public String escapeStringForJSON(String in)
	{
		//lame, maybe this doesn't work for every case
		//escapes " and \
		//String in = "a\"b  a\"b  a\\b  a\\b a\\\\b"
		return (in.replace("\\","\\\\").replace("\"","\\\""));
	}
}//end class JSONRSFormatter
//EOF
