import util.*;
import util.formatter.*;

import java.io.*;
import java.sql.*;

//execute SQL string from stdin
//statements must end with ;\n

//tb/1607

//=============================================================================
//=============================================================================
public class ExecSQL
{
	//this file is loaded if found in current directory
	private static String propertiesFileUri="ExecSQL.properties";

	//===configurable parameters (here: default values)
	public static String db_connection_url = "jdbc:mckoi://localhost/";
//	public String db_connection_url = "jdbc:mckoi:local://./db.conf"

	public static String db_username = "admin";
	public static String db_password = "admin";
	//===end configurable parameters

	private static Connection db_connection;

	private static String errorSepBegin="--ERR begin-------------";
	private static String errorSepEnd=  "--ERR end---------------";

//=============================================================================
	public static void main(String[] args) throws Exception
	{
		if(!LProps.load(propertiesFileUri,ExecSQL.class))
		{
			System.err.println("/!\\ could not load properties");
		}
		connectDb();
		System.err.println("unrequested mini how-to:");
		System.err.println("lines starting with '--' are ignored (comments).");
		System.err.println("statements must end with ';'.");
		System.err.println("interactive session can be closed with 'ctrl+d'.");
		System.err.println("reading SQL statements from stdin now:");

		//don't throw up errors upwards for bogus statements (i.e. for interactive stdin session)
		while(1==1)
		{
			try{
				processStdIn();
				break;
			}catch (Exception e)
			{
				System.err.println("/!\\ error (bogus SQL statement?)");
				System.err.println(errorSepBegin+"\n"+e.getMessage()+"\n"+errorSepEnd);
				System.err.println("reading next statement from stdin now:");
			}
		}

		close();
		System.err.println("ExecSQL finished.");
	}

//=============================================================================
	private static void close() throws Exception
	{
		System.err.println("closing connection to database.");
		db_connection.close();
	}

//=============================================================================
	private static void processStdIn() throws Exception
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		StringBuffer sb=new StringBuffer();
		String line="";
		while((line=reader.readLine()) != null)
		{
			sb.append(line+"\n");
			//assume comment
			if(line.startsWith("--"))
			{
				continue;
			}
			//assume end of one sql statement
			else if(line.endsWith(";"))
			{
				//System.err.println("==================");
				//send to db
				execQuery(sb.toString());
				//clear for next statement
				sb=new StringBuffer();
			}
		}
		//System.err.println(sb);
	}//end processStdIn()

//=============================================================================
	private static void connectDb() throws Exception
	{
		//Register the Mckoi JDBC Driver
		Class.forName("com.mckoi.JDBCDriver").newInstance();
		System.err.println("connecting to database...");
		db_connection = DriverManager.getConnection(db_connection_url, db_username, db_password);
	}//end connectDb()

//=============================================================================
	private static void execQuery(String sql_statement) throws Exception
	{
		System.err.println("executing query...");
		ResultSet rs=db_connection.createStatement().executeQuery(sql_statement);

		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();

//		System.out.println((new CSVRSFormatter()).formatRS(rs));
//		System.out.println((new HTMLRSFormatter(false)).formatRS(rs));
//		System.out.println((new HTMLStyledRSFormatter(true)).formatRS(rs));
		(new CSVRSFormatter()).formatRS(rs,new OutputStreamWriter(System.out));
//		(new HTMLStyledRSFormatter(true)).formatRS(rs,new OutputStreamWriter(System.out));
		System.err.println("done.");
	}
}//end class ExecSQL
//EOF
