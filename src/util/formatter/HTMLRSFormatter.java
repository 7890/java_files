package util.formatter;

import java.io.OutputStreamWriter;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

//tb/1608

//=============================================================================
//=============================================================================
public class HTMLRSFormatter extends CommonRSFormatter
{
	boolean complete_html=true;

//=============================================================================
	public HTMLRSFormatter()
	{
	}

//=============================================================================
	public HTMLRSFormatter(boolean complete_html)
	{
		this.complete_html=complete_html;
	}

//=============================================================================
	public void formatRSImpl(ResultSet rs) throws Exception
	{
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();

		if(complete_html)
		{
			writeOut("<!DOCTYPE html>\n");
			writeOut("<html lang=\"en\">\n");
			writeOut("<head>\n");
			writeOut("<meta charset=\"UTF-8\" />\n");
			writeOut("<tite>Output</title>\n");
			writeOut("</head>\n");
		}

		writeOut("<table>\n");
		//table header
		writeOut("<tr>\n");
		for (int i=1; i<=columnCount;i++)
		{
			writeOut("<th>" + rsmd.getColumnLabel(i) + "</th>\n");
		}
		writeOut("</tr>\n");
		 //row data
		while (rs.next())
		{
			writeOut("<tr>\n");
			for (int i=1;i <=columnCount;i++)
			{
				writeOut("<td>"+rs.getString(i)+"</td>\n");
			}
			writeOut("</tr>\n");
		}
		writeOut("</table>\n");

		if(complete_html)
		{
			writeOut("</body>\n");
			writeOut("</html>\n");
		}
	}//end formatRSImpl()
}//end class RSFormatter
//EOF
