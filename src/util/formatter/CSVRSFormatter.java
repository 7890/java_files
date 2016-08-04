package util.formatter;

import java.io.OutputStreamWriter;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

//tb/1608

//=============================================================================
//=============================================================================
public class CSVRSFormatter extends CommonRSFormatter
{
//=============================================================================
	public void formatRSImpl(ResultSet rs) throws Exception
	{
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();

		//print csv header
		for(int i=1;i<=columnCount;i++)
		{
			writeOut(rsmd.getColumnName(i)+";");
		}
		writeOut("\n");
		//print rows
		while (rs.next())
		{
			for(int i=1;i<=columnCount;i++)
			{
				writeOut(rs.getString(i)+";");
			}
			writeOut("\n");
		}
	}
}//end class RSFormatter
//EOF
