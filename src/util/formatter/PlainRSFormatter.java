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
	public PlainRSFormatter()
	{
	}

//=============================================================================
	public PlainRSFormatter(boolean enclose_xml)
	{
		this.enclose_xml=enclose_xml;
	}

//=============================================================================
	public void formatRSImpl(ResultSet rs) throws Exception
	{
		handleLimits(rs);
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();

		if(enclose_xml)
		{
			writeOut("<result>\n");
		}

		//omit header row

		int rows_so_far=0;

		//print rows, concatenated
		while ((record_count==-1 || rows_so_far<record_count) && rs.next())
		{
			for(int i=1;i<=columnCount;i++)
			{
				writeOut(rs.getString(i)+delimiter);
			}
			writeOut("\n");
			rows_so_far++;
		}

		if(enclose_xml)
		{
			writeOut("</result>\n");
		}
	}
}//end class PlainRSFormatter
//EOF
