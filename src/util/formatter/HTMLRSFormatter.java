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
	public HTMLRSFormatter(int from_record_index, int record_count)
	{
		this.from_record_index=from_record_index;
		this.record_count=record_count;
	}

//=============================================================================
	public HTMLRSFormatter(boolean complete_html, int from_record_index, int record_count)
	{
		this.complete_html=complete_html;
		this.from_record_index=from_record_index;
		this.record_count=record_count;
	}

//=============================================================================
	public void formatRSImpl(ResultSet rs) throws Exception
	{
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		int total_records=totalRecords(rs);
		System.err.println("formatRSImpl: "+total_records+" record(s) in resultset. requested from: "+from_record_index+" count: "+record_count);

		if(from_record_index>=0 && from_record_index<total_records)
		{
			rs.absolute(from_record_index);
		}
		if(record_count<0 || from_record_index>=total_records) //return empty set
		{
			record_count=-1; //all
		}

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

		int rows_so_far=0;

		 //row data
		while ((record_count==-1 || rows_so_far<record_count) && rs.next())
		{
			writeOut("<tr>\n");
			for (int i=1;i <=columnCount;i++)
			{
				writeOut("<td>"+rs.getString(i)+"</td>\n");
			}
			writeOut("</tr>\n");
			rows_so_far++;
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
