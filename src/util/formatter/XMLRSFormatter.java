package util.formatter;

import java.io.OutputStreamWriter;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import java.text.MessageFormat;

//tb/1608

/*
snippet from http://www.bennyn.de/programmierung/java/string-templates-mit-java-tausend-wege-fuhren-nach-rom.html

    String template = "My name is {0} {1}."
            + System.getProperty("line.separator", "\r\n")
            + "I am {2} years old.";
    Object[] values = new Object[]
    {
      "Benny", "Neugebauer", 24
    };
    String sentence = MessageFormat.format(template, values);
    System.out.println(sentence);
*/

//=============================================================================
//=============================================================================
public class XMLRSFormatter extends CommonRSFormatter
{
	boolean complete_xml=true;

//=============================================================================
	public XMLRSFormatter()
	{
	}

//=============================================================================
	public XMLRSFormatter(boolean complete_xml)
	{
		this.complete_xml=complete_xml;
	}

//=============================================================================
	public void formatRSImpl(ResultSet rs) throws Exception
	{
		handleLimits(rs);
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();

		if(complete_xml)
		{
			writeOut("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			writeOut("<query_result>\n");
		}

		//create record template from columns
		StringBuffer sb=new StringBuffer();
		sb.append("<record>");
		for (int i=1; i<=columnCount;i++)
		{
			sb.append(
				"<"+rsmd.getColumnLabel(i)+">" 
				+"{"+(i-1)+"}"
				+"</"+rsmd.getColumnLabel(i)+">" 
			);
		}
		sb.append("</record>\n");

		String record_template=sb.toString();
//		System.err.println("template: "+record_template);
		Object[] values = new Object[columnCount];

		int rows_so_far=0;

		//print rows
		while ((record_count==-1 || rows_so_far<record_count) && rs.next())
		{
			for (int i=1;i <=columnCount;i++)
			{
				values[i-1]=rs.getString(i);
			}

			///escaping !

			writeOut(MessageFormat.format(record_template, values));
			rows_so_far++;
		}

		if(complete_xml)
		{
			writeOut("</query_result>\n");
		}
	}//end formatRSImpl()
}//end class RSFormatter
//EOF
