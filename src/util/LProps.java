package util;

//http://docs.oracle.com/javase/tutorial/reflect/member/fieldTypes.html
//tb/160306
import java.util.Properties;
import java.util.Enumeration;
import java.util.TreeSet;
import java.util.Collections;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.Vector;

//========================================================================
//========================================================================
public class LProps
{
	private final static boolean PROCESS_INSTRUCTIONS=true;

	private final static String PROPERTIES_READ_ENCODING="UTF-8";
	private final static String PROPERTIES_STORE_ENCODING="UTF-8";

	public LProps(){}

//========================================================================
	public static String dumpObject(Object configurable_object)
	{
		try
		{
			Class<?> c = configurable_object.getClass();
			Field[] fields = c.getFields();

			StringBuffer sb=new StringBuffer();
			String nl = System.getProperty("line.separator");

			for (Field field : fields)
			{
				Class ctype=field.getType();
				if(ctype==int.class || ctype==Integer.class
					|| ctype==long.class || ctype==Long.class
					|| ctype==float.class || ctype==Float.class
					|| ctype==double.class || ctype==Double.class
					|| ctype==String.class
					|| ctype==char.class
					|| ctype==boolean.class || ctype==boolean.class
				)
				{
					sb.append(field.getName()+"="+field.get(configurable_object)+nl);
				}

				else if(ctype==java.util.Vector.class)
				{
					sb.append(field.getName()+"=");
					Vector v=(Vector)field.get(configurable_object);
					for(int i=0;i<v.size();i++)
					{
						sb.append(v.get(i));
						if(i<v.size()-1)
						{
							sb.append(",");
						}
					}
				}
			}
			return sb.toString();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return "";
	}

//========================================================================
	public static boolean store(String configfile_uri, String all)
	{
		Properties props=new Properties();
		if(props==null){return false;}
		try
		{
			props.load(new StringReader(all));
			getOrderedProperties(props).store(
				new OutputStreamWriter(
					new FileOutputStream(
						new File(configfile_uri)
					)
					,PROPERTIES_STORE_ENCODING)
				,null);
			return true;
		}
		catch(Exception e){e.printStackTrace();}
		return false;
	}

//========================================================================
	public static boolean store(String configfile_uri, Object configurable_object)
	{
		String all=dumpObject(configurable_object);
		if(all.equals(""))
		{
			return false;
		}

		return store(configfile_uri, all);
	}

//========================================================================
	public static boolean load(String configfile_uri, Object configurable_object)
	{
		return load(configfile_uri, configurable_object, PROCESS_INSTRUCTIONS);
	}

//========================================================================
	public static boolean load(String configfile_uri, Object configurable_object, boolean process_lprops_instructions)
	{
		try
		{
			Properties props=checkLoadFile(configfile_uri);
			if(props==null)
			{
				return false;
			}

			//test if LProps processing instruction is available
			if(process_lprops_instructions && props.getProperty("lprops_pre_load_dump")!=null)
			{
				System.err.println("LProps pre load dump: "+props.getProperty("lprops_pre_load_dump"));
				System.err.println(LProps.dumpObject(configurable_object));
			}

			Class<?> c = configurable_object.getClass();
			Field[] fields = c.getFields();
			for(int i=0; i<fields.length;i++)
			{
//				System.err.println("field "+i+" : "+fields[i]);
				Class ctype=fields[i].getType();
				String fname=fields[i].getName();
				if(props.getProperty(fname)!=null)
				{
//					System.err.println("found matching member variable property in file");
					if(ctype==int.class || ctype==Integer.class)
					{
//						System.err.println("found int");
						try{fields[i].setInt(configurable_object, Integer.parseInt(props.getProperty(fname)));}
						catch(Exception e){System.err.println(""+e);}
					}
					if(ctype==long.class || ctype==Long.class)
					{
//						System.err.println("found long");
						try{fields[i].setLong(configurable_object, Long.parseLong(props.getProperty(fname)));}
						catch(Exception e){System.err.println(""+e);}
					}
					else if(ctype==float.class || ctype==Float.class)
					{
//						System.err.println("found float");
						try{fields[i].setFloat(configurable_object, Float.parseFloat(props.getProperty(fname)));}
						catch(Exception e){System.err.println(""+e);}
					}
					else if(ctype==double.class || ctype==Double.class)
					{
//						System.err.println("found double");
						try{fields[i].setDouble(configurable_object, Double.parseDouble(props.getProperty(fname)));}
						catch(Exception e){System.err.println(""+e);}
					}
					else if(ctype==String.class)
					{
//						System.err.println("found string");
						try{fields[i].set(configurable_object, props.getProperty(fname));}
						catch(Exception e){System.err.println(""+e);}
					}
					else if(ctype==char.class)
					{
//						System.err.println("found char");
						try{fields[i].setChar(configurable_object, (props.getProperty(fname).charAt(0)));}
						catch(Exception e){System.err.println(""+e);}
					}
					else if(ctype==boolean.class || ctype==boolean.class)
					{
//						System.err.println("found boolean");
						try{fields[i].setBoolean(configurable_object, Boolean.parseBoolean(props.getProperty(fname)));}
						catch(Exception e){System.err.println(""+e);}
					}
					else if(ctype==Vector.class)
					{
//						System.err.println("found vector");
						try
						{
							String tokens[]=props.getProperty(fname).split(",");
							Vector v = (Vector)fields[i].get(configurable_object);
							for(int k=0;k<tokens.length;k++)
							{
								v.add(tokens[k].trim());
							}
						}
						catch(Exception e){System.err.println(""+e);}
					}
					///else if byte,short,long,char,double
				}//end if found property
			}//end for all fields

			//test if LProps processing instruction is available
			if(process_lprops_instructions && props.getProperty("lprops_post_load_dump")!=null)
			{
				System.err.println("LProps post load dump: "+props.getProperty("lprops_post_load_dump"));
				System.err.println(LProps.dumpObject(configurable_object));
			}
			return true;
		}//end try
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}//end load()

//========================================================================
	public static Properties checkLoadFile(String configfile_uri)
	{
		Properties props=new Properties();
		InputStream is=null;
		try
		{
			File f=new File(configfile_uri);
			if(!f.exists() || !f.canRead())
			{
				return null;
			}
			is=new FileInputStream(f);
			if(is==null)
			{
				return null;
			}
			props.load(new InputStreamReader(is,PROPERTIES_READ_ENCODING));
			if(props==null)
			{
				return null;
			}
			is.close(); ////
			return props;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

//========================================================================
	public static Properties getOrderedProperties(Properties props)
	{
		//http://stackoverflow.com/questions/17011108/how-can-i-write-java-properties-in-a-defined-order
		Properties tmp=new Properties()
		{
			public synchronized Enumeration<Object> keys()
			{
				return Collections.enumeration(new TreeSet<Object>(super.keySet()));
			}
		};
		tmp.putAll(props);
		return tmp;
	}

//========================================================================
	public static Object get(String configfile_uri, String key)
	{
		Properties props=checkLoadFile(configfile_uri);
		if(props==null){return null;}
		try
		{
			return props.getProperty(key);	
		}
		catch(Exception e){e.printStackTrace();}
		return null;
	}

//========================================================================
	public static boolean set(String configfile_uri, String key, Object val)
	{
		Properties props=checkLoadFile(configfile_uri);
		if(props==null){return false;}
		try
		{
			props.setProperty(key, ""+val);
			///OVERWRITE ORIGINAL FILE. LOOSING ALL COMMENTS. KEYS IN ALPHABETIC ORDER.
			getOrderedProperties(props).store(
				new OutputStreamWriter(
					new FileOutputStream(
						new File(configfile_uri)
					)
					,PROPERTIES_STORE_ENCODING)
				,null);
//			System.out.println(key+"="+get(configfile_uri,key));
//			print(configfile_uri);
			return true;
		}
		catch(Exception e){e.printStackTrace();}
		return false;
	}

//========================================================================
	public static boolean print(String configfile_uri)
	{
		Properties props=checkLoadFile(configfile_uri);
		if(props==null){return false;}
		try
		{
			getOrderedProperties(props).store(System.out, null);
			return true;
		}
		catch(Exception e){e.printStackTrace();}
		return false;
	}

}//end class LProps

//========================================================================
//========================================================================
class LPropsMan
{
	public Vector search_entries;

//========================================================================
	public LPropsMan()
	{
		search_entries=new Vector();
	}//end constructor

//========================================================================
	public void add(String uri)
	{
		search_entries.add(uri);
	}//end constructor

//========================================================================
	public void load(Object configurable_object)
	{
		if(configurable_object==null)
		{
			return;
		}
		for(int i=0;i<search_entries.size();i++)
		{
			System.err.println("loading '"+(String)search_entries.get(i)+"' ...");
			if(!LProps.load((String)search_entries.get(i), configurable_object))
			{
				System.err.println("/!\\ could not load '"+(String)search_entries.get(i)+"'");
			}
		}
	}

}//end class LPropsMan

//EOF
