package util.formatter;

import java.io.OutputStreamWriter;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import java.sql.Types;

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
			writeOut("<body>\n"); ////
		}

		writeOut("<div class=\"table\">\n");

		//table header
		writeOut("<div class=\"row header\">\n");

		if(numberRows)
		{
			writeOut("<div class=\"cell\">[#]</div>\n");
		}

		String prefix="";

		for (int i=1; i<=columnCount;i++)
		{
			if(numberCols)
			{
				prefix="[" + (i-1) + "] ";
			}
			writeOut("<div class=\"cell\">"+prefix + rsmd.getColumnLabel(i) + "</div>\n");
		}
		writeOut("</div>\n");

		int rows_so_far=0;

		String div_cell_class="";

		//print rows
		while ((record_count==-1 || rows_so_far<record_count) && rs.next())
		{
			writeOut("<div class=\"row\">\n");

			if(numberRows)
			{
				writeOut("<div class=\"cell\">"+ (from_record_index+rows_so_far) +"</div>\n");
			}

			for (int i=1;i <=columnCount;i++)
			{
				//align numbers to the right side
				div_cell_class="cell";
				int coltype=rsmd.getColumnType(i);
				if(	coltype==Types.BIGINT
					||coltype==Types.DECIMAL
					||coltype==Types.DOUBLE
					||coltype==Types.FLOAT
					||coltype==Types.INTEGER
					||coltype==Types.NUMERIC
					||coltype==Types.REAL
					||coltype==Types.SMALLINT
					||coltype==Types.TINYINT
				)
				{
					div_cell_class="cell cell-numeric";
				}
				writeOut("<div class=\""+div_cell_class+"\">"+rs.getString(i)+"</div>\n");
			}
			writeOut("</div>\n");
			rows_so_far++;
		}
		writeOut("</div>\n");

		if(complete_html)
		{
			///eval($id("div-eval-id").innerHTML);
			writeOut("<div id=\"div-eval-id\" style=\"visibility:hidden\">var TOTAL_RECORDS="+total_records+";</div>\n");

			writeOut("</body>\n");
			writeOut("</html>\n");
		}
	}//end formatRS_
}//end class RSFormatter
//EOF
