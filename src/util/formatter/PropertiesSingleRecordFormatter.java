package util.formatter;

import java.io.OutputStreamWriter;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import java.util.*;

import java.lang.StringBuilder;

//tb/1612

//format the first record found in the resultset as key=value pairs (~ Java properties)
//k1=v1
//k2=v2
//etc.

//to be used with java_files, java_http sql query handler, ..

//=============================================================================
//=============================================================================
public class PropertiesSingleRecordFormatter extends CommonRSFormatter
{
//=============================================================================
	public void formatRSImpl(ResultSet rs) throws Exception
	{
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();

		//only process first record
		if(rs.next())
		{
			for(int i=1;i<=columnCount;i++)
			{
				writeOut(nativeToAscii(rsmd.getColumnLabel(i))+"=");

				//int coltype=rsmd.getColumnType(i);

				String val=rs.getString(i);
				if(val==null)
				{
					val="null";
				}
				writeOut(nativeToAscii(val)+"\n");
			}
		}
	}

	//https://docs.oracle.com/cd/E23095_01/Platform.93/ATGProgGuide/html/s1816convertingpropertiesfilestoescap01.html
	//~mimic what native2ascii does
//=============================================================================
	public String nativeToAscii(String input)
	{
		StringBuilder b = new StringBuilder();
		for (char c : input.toCharArray())
		{
			if (c >= 128)
			{
				b.append("\\u").append(String.format("%04X", (int) c));
			}
			else
			{
				b.append(c);
			}
		}
		return b.toString();
	}
}//end class RSFormatter
//EOF
