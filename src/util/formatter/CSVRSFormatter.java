package util.formatter;

import java.io.OutputStreamWriter;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

//tb/1608

//=============================================================================
//=============================================================================
public class CSVRSFormatter extends CommonRSFormatter
{
	boolean with_header=true;

//=============================================================================
	public CSVRSFormatter()
	{
	}

//=============================================================================
	public CSVRSFormatter(boolean complete_html)
	{
		this.with_header=with_header;
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

		//print csv header
		if(with_header)
		{
			for(int i=1;i<=columnCount;i++)
			{
				writeOut(rsmd.getColumnName(i)+";");
			}
			writeOut("\n");
		}

		int rows_so_far=0;

		//print rows
		while ((record_count==-1 || rows_so_far<record_count) && rs.next())
		{
			for(int i=1;i<=columnCount;i++)
			{
				writeOut(rs.getString(i)+";");
			}
			writeOut("\n");
			rows_so_far++;
		}
	}
}//end class RSFormatter
//EOF
