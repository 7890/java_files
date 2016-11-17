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

	public boolean numberCols=false;
	public boolean numberRows=false;

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
		handleLimits(rs);
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();

		StringBuffer out=new StringBuffer("");

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

		writeOut("<div class=\"table\">\n");

		//table header
		writeOut("<div class=\"row header\">\n");
		if(numberRows)
		{
			writeOut("<div class=\"cell\">#</div>\n");
		}

		for (int i=1; i<=columnCount;i++)
		{
			if(numberCols)
			{
				writeOut("<div class=\"cell\">[" + i + "]" + rsmd.getColumnLabel(i) + "</div>\n");
			}
			else
			{
				writeOut("<div class=\"cell\">" + rsmd.getColumnLabel(i) + "</div>\n");
			}
		}
		writeOut("</div>\n");

		int rows_so_far=0;

		//print rows
		while ((record_count==-1 || rows_so_far<record_count) && rs.next())
		{
			writeOut("<div class=\"row\">\n");
			if(numberRows)
			{
				writeOut("<div class=\"cell\">#</div>\n");
			}
			for (int i=1;i <=columnCount;i++)
			{
				writeOut("<div class=\"cell\">"+rs.getString(i)+"</div>\n");
			}
			writeOut("</div>\n");
			rows_so_far++;
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
