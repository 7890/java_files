package util.formatter;

import java.io.OutputStreamWriter;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

//tb/1608

//=============================================================================
//=============================================================================
public class PlainRSFormatter extends CommonRSFormatter
{
	public boolean enclose_xml=true;
	public String delimiter="";
//=============================================================================
	public void formatRSImpl(ResultSet rs) throws Exception
	{
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		if(enclose_xml)
		{
			writeOut("<result>\n");
		}

		//omit header row

		//print rows, concatenated
		while (rs.next())
		{
			for(int i=1;i<=columnCount;i++)
			{
				writeOut(rs.getString(i)+delimiter);
			}
			writeOut("\n");
		}
		if(enclose_xml)
		{
			writeOut("</result>\n");
		}
	}
}//end class PlainRSFormatter
//EOF
