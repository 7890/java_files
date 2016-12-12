import interfaces.*;
import util.*;
import util.formatter.*;

import java.io.*;
import java.sql.*;

import java.lang.reflect.Constructor;

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
	public static String jdbc_impl_class="com.mckoi.JDBCDriver";

	public static String db_connection_url = "jdbc:mckoi://localhost/";
//	public String db_connection_url = "jdbc:mckoi:local://./db.conf"

	public static String db_username = "admin";
	public static String db_password = "admin";

	public static String formatter_class="util.formatter.CSVRSFormatter";

	public static boolean continue_on_error=false;
	//===end configurable parameters

	private static Connection db_connection;

	private static String errorSepBegin="--ERR begin-------------";
	private static String errorSepEnd=  "--ERR end---------------";

/*
	private static CSVRSFormatter csv = new CSVRSFormatter();
	private static HTMLRSFormatter html = new HTMLRSFormatter(false);
	private static HTMLStyledRSFormatter html_styled = new HTMLStyledRSFormatter(true);
	...
	formatter class now read from properties
*/
	private static RSFormatter rsf=null;

	private static OutputStreamWriter osw = new OutputStreamWriter(new BufferedOutputStream(System.out));

//=============================================================================
	public static void main(String[] args) throws Exception
	{
		if(!LProps.load(propertiesFileUri,new ExecSQL())) ///works because static. ExecSQL.class doesn't
		{
			System.err.println("/!\\ could not load properties");
		}
		connectDb();

		Class<?> c = Class.forName(formatter_class);
		Constructor<?> cons = c.getConstructor();
		rsf=((RSFormatter)cons.newInstance());
/*
		System.err.println("unrequested mini how-to:");
		System.err.println("lines starting with '--', '//' or '#' are ignored (comments).");
		System.err.println("statements must end with ';'.");
		System.err.println("interactive session can be closed with 'ctrl+d'.");
		System.err.println("reading SQL statements from stdin now:");
*/
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
				e.printStackTrace();

				if(continue_on_error)
				{
					System.err.println("reading next statement from stdin now:");
				}
				else
				{
					close();
					System.err.println("ExecSQL finished with errors.");
					System.exit(1);
				}
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
		osw.close();
	}

//=============================================================================
	private static void processStdIn() throws Exception
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		StringBuffer sb=new StringBuffer();
		String line="";
		while((line=reader.readLine()) != null)
		{
			//remove leading/trailing whitespace
			line.trim();
			//somehow trim() won't remove tabs (?)
			//poor way to remove leading tabs in order to check if its a comment (indented comment)
			try{while(line.substring(0,1).equals("\t")){
				line=line.substring(1,line.length());
			}}catch(Exception e){}

			//final trimmed lined
//			System.err.println("["+line+"]");

			//test if comment
			if(line.startsWith("--") || line.startsWith("//") || line.startsWith("#"))
			{
				continue;
			}

			//not a comment, adding to stringbuffer
			sb.append(line+"\n");

			//assume end of one sql statement. execute stringbuffer's sql, create new one for next
			if(line.endsWith(";"))
			{
				//System.err.println("==================");
				//send to db
				execQuery(sb.toString());
				//clear for next statement
				sb=new StringBuffer();
			}
		}
		//System.err.println(sb);
		reader.close();
	}//end processStdIn()

//=============================================================================
	private static void connectDb() throws Exception
	{
		//Register the Mckoi JDBC Driver
		Class.forName(jdbc_impl_class).newInstance();
		System.err.println("connecting to database...");
		db_connection = DriverManager.getConnection(db_connection_url, db_username, db_password);
	}//end connectDb()

//=============================================================================
	private static void execQuery(String sql_statement) throws Exception
	{
		System.err.println("executing query...");
//		System.err.println(sql_statement);
//		ResultSet rs=db_connection.createStatement().executeQuery(sql_statement);
		//JDBC implementations seem to behave differently.
		//some accept manipulative statements to be executed via executeQuery(),
		//others require executeUpdate().
		//using execute() and afterwards testing if there is a resultset seems
		//to work for all cases

		Statement stm=db_connection.createStatement();
		boolean is_rs=stm.execute(sql_statement);
		if(is_rs)
		{
			ResultSet rs=stm.getResultSet();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			//csv.formatRS(rs);
			//csv.formatRS(rs,osw);
			rsf.formatRS(rs,osw);
			osw.flush();
			//don't close osw here
			//close result set
			rs.close();
		}//end if execute() returned a ResultSet
		else
		{
			System.out.println("result;");
			System.out.println(stm.getUpdateCount()+";");
		}
		System.err.println("done.");
	}
}//end class ExecSQL
//EOF
