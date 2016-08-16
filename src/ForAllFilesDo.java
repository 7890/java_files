import util.*;
import interfaces.*;
import hooks.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileTypeDetector;

import java.lang.reflect.Constructor;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.Bindings;
import javax.script.ScriptContext;

import java.security.MessageDigest;
import java.security.DigestInputStream;

//-for a given base directory, list all files in that directory, recurse into subdirectories
//-a JavaScript filter can optionally be used to match files by rules
//-the mimetype and md5sum of files can optionally be extracted and calculated
//-a Java hook can process all matching files
//-the order of built-in processors is: mimetype, md5sum (both are optional)
//-the place of the JS filter can be before mimetype, before md5sum or after md5sum
//-depending on where the JS filter is sorted in, it will receive mimetype and/or md5sum

//tb/1607

//=============================================================================
//=============================================================================
public class ForAllFilesDo
{
	//this file is loaded if found in current directory
	private String propertiesFileUri="ForAllFilesDo.properties";

	//===configurable parameters (here: default values)
	public boolean use_relative_uris=true;

	public boolean enable_mime_type_detection=true;

	public boolean enable_md5_calculation=true;

	public boolean enable_js_filter=false;
	public boolean filter_before_mime=false;
	public boolean filter_before_md5=false;
	public String js_filter_uri="./resources/filter.js";
	public String js_function_prefix="c";

	public boolean enable_non_js_output=true;

	public boolean enable_hook=true;
	public String hook_class_uri="hooks.AddFileToDb";
	//===end configurable parameters

	private static FileTypeDetector mime_detector;
	private int counter=0;

	private ScriptEngine js;
	private ScriptEngineManager js_factory;
	private API_FOR_JS api;

	private ProcessFileHook pfh;

	private MessageDigest md;

//=============================================================================
//=============================================================================
	public ForAllFilesDo(String[] args) throws Exception
	{
		if(args.length<1)
		{
			System.err.println("missing directory or file argument");
			System.exit(1);
			return;
		}

		if(!LProps.load(propertiesFileUri,this))
		{
			System.err.println("/!\\ could not load properties");
		}

		if(enable_mime_type_detection)
		{
			mime_detector=new TikaFileTypeDetector();
		}

		if(enable_md5_calculation)
		{
			md=MessageDigest.getInstance("MD5");
		}

		if(enable_js_filter)
		{
			api=new API_FOR_JS();

			js_factory=new ScriptEngineManager();
			js=js_factory.getEngineByName("JavaScript");

			loadScript(js_filter_uri);
		}

		if(enable_hook)
		{
			Class<?> c = Class.forName(hook_class_uri);
			Constructor<?> cons = c.getConstructor();
			pfh=(ProcessFileHook)cons.newInstance();

			String[] args_stripped=new String[args.length-1];//
			//public static void arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
			System.arraycopy(args, 0, args_stripped, 0, args.length-1);
			pfh.setArgs(args);
		}

		File f=new File(args[args.length-1]);
		if(!f.isDirectory())
		{
			//read displayname from stdin
			BufferedReader buffered_reader=new BufferedReader(new InputStreamReader(System.in));
			System.err.println("enter display name (enter to use original filename): ");

			String line;
			line = buffered_reader.readLine();
			if(line==null || line.equals(""))
			{
				//add single file, use original filename
				processFile(f);
			}
			else
			{
				//add single file with display name
				processFile(f,line);
			}
		}
		else
		{
			//start recursion
			recursiveListFilesStart(f);
		}

		if(enable_hook)
		{
			pfh.close();
		}
	}//end ForAllFilesDo constructor

//=============================================================================
	public static void main(String[] args) throws Exception
	{
		new ForAllFilesDo(args);
	}

//wrapper, entry point for recursive display files in directories
//=============================================================================
	public void recursiveListFilesStart(File rootDir) throws Exception
	{
		//header
		if(enable_js_filter)
		{
			js_eval(js_function_prefix+".header('"+rootDir.getName()+"');");
		}

		//process all files and directories in this directory
		recursiveListFiles(rootDir,0);

		//footer
		if(enable_js_filter)
		{
			js_eval(js_function_prefix+".footer('"+rootDir.getName()+"');");
		}
	}

//=============================================================================
	private boolean js_hook(File _f, String displayName, String mimeType, String md5sum, boolean use_relative_uris)
	{
		if(!enable_js_filter)
		{
			return true;
		}

		String _path="";
		if(use_relative_uris)
		{
			_path=_f.getPath();
		}
		else
		{
			_path=_f.getAbsolutePath();
		}

		e_("running JS filter...");
		try
		{
			return (Boolean)js_eval(js_function_prefix+".filter('"
				+_f.getName()
				+"','"+displayName
				+"','"+_path
				+"','"+mimeType
				+"',"+_f.length()
				+",'"+md5sum
				+"',"+_f.lastModified()
				+","+_f.canWrite()
				+","+_f.canExecute()
				+");");
		}
		catch(Exception e)
		{
			e.printStackTrace(); 
			e_(_path);
		}
		return false;
	}//end js_hook

//=============================================================================
	private boolean js_hook_d(File _f, boolean use_relative_uris)
	{
		if(!enable_js_filter)
		{
			return true;
		}

		String _path="";
		if(use_relative_uris)
		{
			_path=_f.getPath();
		}
		else
		{
			_path=_f.getAbsolutePath();
		}

		e_("running JS filter D...");
		try
		{
			return (Boolean)js_eval(js_function_prefix+".filterD('"+_path+"');");
		}
		catch(Exception e)
		{
			e.printStackTrace(); 
			e_(_path);
		}
		return false;
	}//end js_hook

	//wrapper, display and original filename are the same
//=============================================================================
	public void processFile(File _f) throws Exception
	{
		processFile(_f,_f.getName());
	}
//=============================================================================
	public void processFile(File _f, String displayName) throws Exception
	{
		String contentType=null;
		String md5Sum=null;

		//default true
		boolean is_match=true;

		if(filter_before_mime)
		{
			is_match=js_hook(_f,displayName,contentType,md5Sum,use_relative_uris);
		}

		if(enable_mime_type_detection && is_match)
		{
			e_("detecting MIME type...");
			contentType=mime_detector.probeContentType(Paths.get(_f.getAbsolutePath()));//nio path
//			e_("MIME type: "+contentType);
			if(contentType==null)
			{
//				e_("MIME type is unknown. setting to 'application/octet-stream'");
				contentType="application/octet-stream";
			}
		}

		if(filter_before_md5 && !filter_before_mime)
		{
			is_match=js_hook(_f,displayName,contentType,md5Sum,use_relative_uris);
		}

		if(enable_md5_calculation && is_match)
		{
		e_("calculating MD5 sum...");
			try
			{
				md5Sum=calcMD5(_f);
//				e_("MD5 sum: "+md5Sum);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		if(!filter_before_md5 && !filter_before_mime)
		{
			is_match=js_hook(_f,displayName,contentType,md5Sum,use_relative_uris);
		}

		if(enable_hook && is_match)
		{
			e_("running hook...");
			pfh.processFile(_f,displayName,contentType,md5Sum,use_relative_uris);
		}

		if(is_match && enable_non_js_output)
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

			e_(_path);
		}
		e_("file done (match: "+is_match+").");

		counter++;
	}//end processFile

//=============================================================================
	public void recursiveListFiles(File _f, int level) throws Exception
	{
		if(_f.isFile())
		{
			processFile(_f);
		}//end if is file
		else
		{
			//only progress in dir if d filter allows
			if( js_hook_d(_f,use_relative_uris) )
			{
				//is a directory
				File files[] = _f.listFiles();

				level++;
				if(files!=null && files.length>0)
				{
					for(File dirOrFile: files)
					{
						recursiveListFiles(dirOrFile,level);
					}
				}
				level--;
			}
		}
	}//end recursiveListFiles()

//http://stackoverflow.com/questions/304268/getting-a-files-md5-checksum-in-java
//=============================================================================
	public String calcMD5(File _f) throws Exception
	{
		byte[] buffer = new byte[64000]; ///
		if(md==null)
		{
			md = MessageDigest.getInstance("MD5");
		}//otherwise use existing
		DigestInputStream dis = new DigestInputStream(new FileInputStream(_f), md);

		try
		{
			while (dis.read(buffer) != -1);
		}
		finally
		{
			dis.close();
		}

		byte[] bytes = md.digest();
		String returnVal="";

		for (int i=0; i < bytes.length; i++)
		{
			returnVal += Integer.toString( ( bytes[i] & 0xff ) + 0x100, 16).substring( 1 );
		}

		return returnVal;
	}

//=============================================================================
	private void loadScript(String sJsUri) throws Exception
	{
		if (js==null)
		{
			throw new Exception("ERR: error loading scripts: "+sJsUri);
		}
		try 
		{
			Bindings js_bindings=js.createBindings();
			js_bindings.put("J", api);
			js.setBindings(js_bindings, ScriptContext.ENGINE_SCOPE);
			js_eval(getAsString(sJsUri));
		}
		catch(Exception e)
		{
			e_("error loading js: "+e.getMessage());
			throw new Exception(e);
		}
	}//end loadScripts()

	//evaluate javascript
//=============================================================================
	private Object js_eval(Object o)
	{
		if (js==null)
		{	
			return null;
		}
		try 
		{
			return js.eval(o.toString());
		}
		catch(Exception e)
		{
			p_("ERR: error evaluating js: "+e.getMessage());
		}

		return null;
	}

//=============================================================================
	public static void e_(Object o)
	{
		System.err.println(""+o);
	}

//=============================================================================
	public static void p_(Object o)
	{
		System.out.println(""+o);
	}

	///not the best way to do
//=============================================================================
	private static String getAsString(String sFile)
	{
		try 
		{
			BufferedReader reader = new BufferedReader(new FileReader(sFile));
			StringBuffer content = new StringBuffer();
			String sLine="";

			while (sLine!=null)
			{
				sLine=reader.readLine();
				if (sLine!=null)
				{
					content.append(sLine);
				}
			}
			reader.close();
			return content.toString();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

//=============================================================================
	public static String indent(int count)
	{
		String out="";
		for(int i=0;i<count;i++)
		{
			out+=" ";
		}
		return out;
	}

//inner
//make visible java functions to javascript over wrapper / API class
//=============================================================================
//=============================================================================
	public class API_FOR_JS
	{
		public void println(String s)
		{
			System.out.println(s);
		}
		public void errorln(String s)
		{
			System.err.println(s);
		}
		public void print(String s)
		{
			System.out.print(s);
		}
		public void error(String s)
		{
			System.err.print(s);
		}
	}
}//end class ForAllFilesDo
//EOF
