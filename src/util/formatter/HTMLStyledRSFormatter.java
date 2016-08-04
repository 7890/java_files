package util.formatter;

import java.io.OutputStreamWriter;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

//tb/1608

//=============================================================================
//=============================================================================
public class HTMLStyledRSFormatter extends CommonRSFormatter
{
	public boolean complete_html=true;

//=============================================================================
	public HTMLStyledRSFormatter()
	{
	}

//=============================================================================
	public HTMLStyledRSFormatter(boolean complete_html)
	{
		this.complete_html=complete_html;
	}

//=============================================================================
	public void formatRSImpl(ResultSet rs) throws Exception
	{
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();

		StringBuffer out=new StringBuffer("");
		writeOut("<div class=\"table\">\n");

		if(complete_html)
		{
			writeOut("<!DOCTYPE html>\n");
			writeOut("<html lang=\"en\">\n");
			writeOut("<head>\n");
			writeOut("<meta charset=\"UTF-8\" />\n");
			writeOut("<tite>Output</title>\n");
			writeOut("<style>\n");
			writeOutFile("./resources/table_style.css");
			writeOut("</style>\n");
			writeOut("</head>\n");
		}

		//table header
		writeOut("<div class=\"row header\">\n");
		for (int i=1; i<=columnCount;i++)
		{
			writeOut("<div class=\"cell\">" + rsmd.getColumnLabel(i) + "</div>\n");
		}
		writeOut("</div>\n");
		 //row data
		while (rs.next())
		{
			writeOut("<div class=\"row\">\n");
			for (int i=1;i <=columnCount;i++)
			{
				writeOut("<div class=\"cell\">"+rs.getString(i)+"</div>\n");
			}
			writeOut("</div>\n");
		}
		writeOut("</div>\n");

		if(complete_html)
		{
			writeOut("</body>\n");
			writeOut("</html>\n");
		}
	}//end formatRS_
}//end class RSFormatter
//EOF