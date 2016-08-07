package hooks;
import util.*;
import interfaces.*;

import java.io.*;
import java.sql.*;

import java.util.ArrayList;

import org.apache.commons.cli.*;

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

	private PreparedStatement ps_next_file_id;
	private PreparedStatement ps_insert_file;
	private PreparedStatement ps_insert_file_link;
	private PreparedStatement ps_insert_basket;
	private PreparedStatement ps_insert_basket_file_link;
	private PreparedStatement ps_find_basket_id_by_link;

	private Options options;
	private Option help;
	private Option displayName;
	private Option addToBasket;
	private Option createBasket;
	private Option createDownloadLink;

	private CommandLineParser parser;
	private HelpFormatter formatter;
	private CommandLine cmd;

	private int basket_id;

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
	private int initCLIOptions(String[] args)
	{
		options=new Options();

		help=new Option("h","help",false,"display help");
		help.setRequired(false);
		options.addOption(help);

		displayName=new Option("d","display",true,"set displayname (single file)");
		displayName.setRequired(false);
		options.addOption(displayName);

		addToBasket=new Option("b","basket",true,"add files to existing basket with given link");
		addToBasket.setRequired(false);
		options.addOption(addToBasket);

		createBasket=new Option("n","new",true,"create new basket with given name");
		createBasket.setRequired(false);
		options.addOption(createBasket);

		createBasket=new Option("N","baket-link",true,"use given link for basket creation (only along with -n)");
		createBasket.setRequired(false);
		options.addOption(createBasket);

		createDownloadLink=new Option("l","link",false,"create random download link per file");
		createDownloadLink.setRequired(false);
		options.addOption(createDownloadLink);

		createDownloadLink=new Option("L","link",true,"use given link for download link cration");
		createDownloadLink.setRequired(false);
		options.addOption(createDownloadLink);

		parser = new DefaultParser();
		formatter = new HelpFormatter();

		try
		{
			cmd=parser.parse(options,args);
		}
		catch(Exception e)
		{
			formatter.printHelp("ForAllFilesDo (hook options) [file or directory]\nAddFileToDb Hook Help", options);
			System.exit(1);
		}

		if(cmd.hasOption("h"))
		{
			formatter.printHelp("ForAllFilesDo (hook options) [file or directory]\nAddFileToDb Hook Help:", options);
			System.exit(0);
		}

		int remaining_args_start_index=0;
		Option[] opts=cmd.getOptions();
		for(int i=0;i<opts.length;i++)
		{
			System.err.println(opts[i]);
			//option key
			remaining_args_start_index++;
			//option params
			int count=opts[i].getArgs();
			if(count>0)
			{
				//getArgs() returns -1 (!) if no args needed (NOT 0 as expected)
				remaining_args_start_index+=opts[i].getArgs();
			}
		}
//		System.err.println("remaining args start index: "+remaining_args_start_index);
		return remaining_args_start_index;

	}//end initCLIOptions()

//=============================================================================
	public void setArgs(String[] args) throws Exception
	{
		initCLIOptions(args);

		if(cmd.hasOption("b") && cmd.hasOption("n"))
		{
			throw new Exception("options -b and -n can't be used combined.");
		}

		if(cmd.hasOption("N") && !cmd.hasOption("n"))
		{
			throw new Exception("option -N can only be used along -n");
		}

		if(cmd.hasOption("L") && cmd.hasOption("l"))
		{
			throw new Exception("options -l and -L can't be used combined.");
		}

		String link="";
		if(cmd.hasOption("n"))
		{
			System.err.println("creating new basket with name '"+cmd.getOptionValue("n")+"'");

			if(cmd.hasOption("N"))
			{
				link=cmd.getOptionValue("N");
			}
			else
			{
				///dummy, create link
				link="foo"+System.currentTimeMillis();
			}

			//create basket
			ps_insert_basket_(cmd.getOptionValue("n"),link);
		}
		else if(cmd.hasOption("b"))
		{
			link=cmd.getOptionValue("b");
			System.err.println("adding files to basket link '"+link+"'");
		}

		if(cmd.hasOption("b") || cmd.hasOption("n"))
		{
			//remember id
			basket_id=ps_find_basket_id_by_link_(link);
			System.err.println("found basket id: "+basket_id);
		}
	}//end setArgs()

//=============================================================================
	private void prepareStatements() throws Exception
	{
		ps_next_file_id = db_connection.prepareStatement(
			"SELECT NEXTVAL('seq_tbl_file_id');"
		);

		ps_insert_file = db_connection.prepareStatement(
			"INSERT INTO tbl_file "
			+"(id,filename,displayname,uri,mimetype,length,md5sum,lastmodified,canwrite,canexecute) "
			+"VALUES (?,?,?,?,?,?,?,?,?,?);"
		);

		ps_insert_file_link = db_connection.prepareStatement(
			"INSERT INTO tbl_file_link "
			+"(id,id_file,link) "
			+"VALUES (NEXTVAL('seq_tbl_file_link_id'),?,?);"
		);

		ps_insert_basket = db_connection.prepareStatement(
			"INSERT INTO tbl_basket "
			+"(id,name,link) "
			+"VALUES (NEXTVAL('seq_tbl_basket_id'),?,?);"
		);

		ps_insert_basket_file_link = db_connection.prepareStatement(
			"INSERT INTO tbl_basket_file_link "
			+"(id,id_basket,id_file) "
			+"VALUES (NEXTVAL('seq_tbl_basket_file_link_id'),?,?);"
		);

		ps_find_basket_id_by_link = db_connection.prepareStatement(
			"SELECT id FROM tbl_basket "
			+"WHERE link = ?;"
		);
	}

//=============================================================================
	private int ps_next_file_id_() throws Exception
	{
		System.err.println("executing query...");
		ResultSet rs = ps_next_file_id.executeQuery();
		if(rs.next()){return rs.getInt(1);}else{throw new Exception("could not get next file id");}
	}

//=============================================================================
	private void ps_insert_file_(
		int id
		,String name
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
		ps_insert_file.clearParameters();
		ps_insert_file.setInt(1,id);
		ps_insert_file.setString(2,name);
		ps_insert_file.setString(3,displayName);
		ps_insert_file.setString(4,uri);
		ps_insert_file.setString(5,mimeType);
		ps_insert_file.setLong(6,length);
		ps_insert_file.setString(7,md5sum);
		ps_insert_file.setLong(8,lastModified);
		ps_insert_file.setBoolean(9,canWrite);
		ps_insert_file.setBoolean(10,canExecute);

		System.err.println("executing query...");

		ResultSet rs = ps_insert_file.executeQuery();
	}

//=============================================================================
	private void ps_insert_file_link_(
		int id_file
		,String link
	) throws Exception
	{
		ps_insert_file_link.clearParameters();
		ps_insert_file_link.setInt(1,id_file);
		ps_insert_file_link.setString(2,link);

		System.err.println("executing query...");

		ResultSet rs = ps_insert_file_link.executeQuery();
	}

//=============================================================================
	private void ps_insert_basket_(
		String name
		,String link
	) throws Exception
	{
		ps_insert_basket.clearParameters();
		ps_insert_basket.setString(1,name);
		ps_insert_basket.setString(2,link);

		System.err.println("executing query...");

		ResultSet rs = ps_insert_basket.executeQuery();
	}

//=============================================================================
	private void ps_insert_basket_file_link_(
		int id_basket
		,int id_file
	) throws Exception
	{
		ps_insert_basket_file_link.clearParameters();
		ps_insert_basket_file_link.setInt(1,id_basket);
		ps_insert_basket_file_link.setInt(2,id_file);

		System.err.println("executing query...");

		ResultSet rs = ps_insert_basket_file_link.executeQuery();
	}

//=============================================================================
	private int ps_find_basket_id_by_link_(
		String link
	) throws Exception
	{
		ps_find_basket_id_by_link.clearParameters();
		ps_find_basket_id_by_link.setString(1,link);

		System.err.println("executing query...");

		ResultSet rs = ps_find_basket_id_by_link.executeQuery();
		if(rs.next()){return rs.getInt(1);}else{throw new Exception("no basket found with that link");}
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

		//create id from sequence
		int file_id=ps_next_file_id_();

		ps_insert_file_(
			file_id
			,_f.getName()
			,displayName
			,_path
			,mimeType
			,_f.length()
			,md5sum
			,_f.lastModified()
			,_f.canWrite()
			,_f.canExecute()
		);

		if(cmd.hasOption("l"))
		{
			///dummy, create link
			String link="foo"+System.currentTimeMillis();
			ps_insert_file_link_(file_id,link);
		}
		else if(cmd.hasOption("L"))
		{
			ps_insert_file_link_(file_id,cmd.getOptionValue("L"));
		}

		if(cmd.hasOption("b") || cmd.hasOption("n"))
		{
			ps_insert_basket_file_link_(basket_id,file_id);
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
