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
		os.close();
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
	public abstract void formatRSImpl(ResultSet rs) throws Exception;
}//end class RSFormatter
//EOF
