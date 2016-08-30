package util.formatter;
import interfaces.*;

import java.io.Writer;
import java.io.BufferedReader;
import java.io.FileReader;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

//tb/1608

//=============================================================================
//=============================================================================
public abstract class CommonRSFormatter implements RSFormatter
{
	public int target=0; //0: StringBuffer/String 1: OutputStreamWriter
	public StringBuffer out;
	public Writer os;

        public int from_record_index=0;
        public int record_count=-1; //no limit

//=============================================================================
	public String formatRS(ResultSet rs) throws Exception
	{
		target=0;
		out=new StringBuffer();
		formatRSImpl(rs);
		return out.toString();
	}

//=============================================================================
	public void formatRS(ResultSet rs, Writer os) throws Exception
	{
		target=1;
		this.os=os;
		formatRSImpl(rs);
		os.flush();
//		os.close();
	}

//=============================================================================
	public int totalRecords(ResultSet rs) throws Exception
	{
		int size=0;
		rs.last();
		size = rs.getRow();
		rs.beforeFirst();
		return size;
	}

//=============================================================================
	public void writeOut(String s) throws Exception
	{
		if(target==0)
		{
			out.append(s);
		}
		else if(target==1)
		{
			os.write(s);
		}
	}

//=============================================================================
	public void writeOutFile(String file) throws Exception
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String sLine="";
		while (sLine!=null)
		{
			sLine=reader.readLine();
			if (sLine!=null)
			{
				writeOut(sLine+"\n");
			}
		}
		reader.close();
	}

//=============================================================================
	public void handleLimits(ResultSet rs) throws Exception
	{
		int total_records=totalRecords(rs);

		System.err.println("formatRSImpl: "+total_records+" record(s) in resultset. requested from: "+from_record_index+" count: "+record_count);

		//if from_record_index within reasonable range, seek to index
		if(from_record_index>=0 && from_record_index<total_records)
		{
			rs.absolute(from_record_index);
		}
		//record_count < 0 means no limit on count
		if(record_count<0)
		{
			record_count=-1; //all
		}
		//condition that can only result in an empty set
		if(from_record_index>=total_records)
		{
			from_record_index=0;
			record_count=0;
		}
	}

//=============================================================================
	public abstract void formatRSImpl(ResultSet rs) throws Exception;
}//end class RSFormatter
//EOF
