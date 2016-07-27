package hooks;
import util.*;
import interfaces.*;

import java.io.*;
import java.sql.*;

//hook to add file matched by ForAllFilesDo to mckoi database

//tb/1607

//========================================================================
//========================================================================
public class AddFileToDb implements ProcessFileHook
{
	//this file is loaded if found in current directory
	private String propertiesFileUri="AddFileToDb.properties";

	//===configurable parameters (here: default values)
	public String db_connection_url = "jdbc:mckoi://localhost/";
//	public String db_connection_url = "jdbc:mckoi:local://./db.conf"

	public String db_username = "admin";
	public String db_password = "admin";
	//===end configurable parameters

	private Connection db_connection;

	private PreparedStatement pstmt_insert_file;

//========================================================================
	public AddFileToDb() throws Exception
	{
		if(!LProps.load(propertiesFileUri,this))
		{
			System.err.println("/!\\ could not load properties");
		}

		connectDb();
		prepareStatements();
	}//end constructor

//=============================================================================
	private void prepareStatements() throws Exception
	{
		pstmt_insert_file = db_connection.prepareStatement(
			"INSERT INTO tbl_file "
			+"(id,filename,displayname,uri,mimetype,length,md5sum,lastmodified,canwrite,canexecute) "
			+"VALUES (NEXTVAL('seq_tbl_file_id'),?,?,?,?,?,?,?,?,?);"
		);
	}

//=============================================================================
	private void pstm_insert_file_(
		String name
		,String displayName
		,String uri
		,String mimeType
		,long length
		,String md5sum
		,long lastModified
		,boolean canWrite
		,boolean canExecute
	) throws Exception
	{
		pstmt_insert_file.setString(1,name);
		pstmt_insert_file.setString(2,displayName);
		pstmt_insert_file.setString(3,uri);
		pstmt_insert_file.setString(4,mimeType);
		pstmt_insert_file.setLong(5,length);
		pstmt_insert_file.setString(6,md5sum);
		pstmt_insert_file.setLong(7,lastModified);
		pstmt_insert_file.setBoolean(8,canWrite);
		pstmt_insert_file.setBoolean(9,canExecute);
	}

//========================================================================
	public String processFile(File _f, String displayName, String mimeType, String md5sum, boolean use_relative_uris) throws Exception
	{
		String _path="";
		if(use_relative_uris)
		{
			_path=_f.getPath();
		}
		else
		{
			_path=_f.getAbsolutePath();
		}

		pstm_insert_file_(
			_f.getName()
			,displayName
			,_path
			,mimeType
			,_f.length()
			,md5sum
			,_f.lastModified()
			,_f.canWrite()
			,_f.canExecute()
		);

		System.err.println("executing query...");

		ResultSet rs = pstmt_insert_file.executeQuery();
		///=db_connection.createStatement().executeQuery(sql_statement);

		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		while (rs.next())
		{
			for(int i=1;i<=columnCount;i++)
			{
				System.out.print(rs.getString(i)+" ");
			}
			System.out.println("");
		}
		return ""; ///
	}//end processFile()

//=============================================================================
	private void connectDb() throws Exception
	{
		//register the Mckoi JDBC driver
		Class.forName("com.mckoi.JDBCDriver").newInstance();
		System.err.println("connecting to database...");
		db_connection = DriverManager.getConnection(db_connection_url, db_username, db_password);
	}

//========================================================================
	public void close() throws Exception
	{
		if(db_connection!=null)
		{
			db_connection.close();
		}
	}
}//end class AddFileToDb
//EOF
