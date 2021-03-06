package com.mckoi.database;
import com.mckoi.database.global.*;

import java.security.MessageDigest;

/*
adds functions to be used in sql queries:

	md5_hash(string)
	sha1_hash(string)
	el(name,content,attrs)
	a(name,value)
	ahref(url,title,target,onclick)
	opt(value,title)
	btn(value,onclick)

i.e.
	select md5_hash(concat(field1,field2,'foo')) as md5,* from bar;

select
	el(
		'a'
		,displayname
		,concat(
			a('href',concat('http://foo.bar/',id))
			,a('target','_blank')
		)
	)
,*
from tbl_file;

select ahref(concat('http://foo.bar/',id),displayname,'_blank',null) from tbl_file;

select opt(id,id) from tbl_file;

/!\\ escaping not considered atm

db.conf must contain
	function_factories=com.mckoi.database.CustomFunctionFactory

(must be in classpath when database is started)
*/

//tb/1608

//=============================================================================
//=============================================================================
public class CustomFunctionFactory extends FunctionFactory
{
//========================================================================
	public void init()
	{
		addFunction(MD5HashFunction.function_name, MD5HashFunction.class);
		addFunction(SHA1HashFunction.function_name, SHA1HashFunction.class);
		addFunction(XMLElementFunction.function_name, XMLElementFunction.class);
		addFunction(XMLAttributeFunction.function_name, XMLAttributeFunction.class);
		addFunction(AHREFFunction.function_name, AHREFFunction.class);
		addFunction(OptionFunction.function_name, OptionFunction.class);
		addFunction(ButtonFunction.function_name, ButtonFunction.class);
	}

//========================================================================
//========================================================================
	private static class MD5HashFunction extends AbstractFunction
	{
		MessageDigest md_md5=null;
		final static String function_name="md5_hash";
//========================================================================
		public String toMD5HashString(String s)
		{
			byte[] encodedHashB=md_md5.digest( s.getBytes() );
			return String.format("%032X", new java.math.BigInteger(1, encodedHashB));
		}

//========================================================================
		public MD5HashFunction(Expression[] params)
		{
			super(function_name, params);

			if (parameterCount() != 1)
			{
				throw new RuntimeException("function '"+function_name+"' must have one argument.");
			}

			try
			{
				md_md5=MessageDigest.getInstance("MD5");
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new RuntimeException("Could not initialize MessageDigest MD5.");
			}
		}

//========================================================================
		public TObject evaluate(GroupResolver group, VariableResolver resolver,
			QueryContext context)
		{
			TObject ob0 = getParameter(0).evaluate(group, resolver, context);
			if (ob0.isNull() || ob0.getObject().toString().equals(""))
			{
				return null;
			}
			return new TObject(ob0.getTType(),
				toMD5HashString(ob0.getObject().toString())
			);
		}

//========================================================================
		public TType returnTType()
		{
			return TType.STRING_TYPE;
		}
	}//end MD5HashFunction

//========================================================================
//========================================================================
	private static class SHA1HashFunction extends AbstractFunction
	{
		MessageDigest md_sha1=null;
		final static String function_name="sha1_hash";
//========================================================================
		public String toSHA1HashString(String s)
		{
			byte[] encodedHashB=md_sha1.digest( s.getBytes() );
			return String.format("%032X", new java.math.BigInteger(1, encodedHashB));
		}
//========================================================================
		public SHA1HashFunction(Expression[] params)
		{
			super(function_name, params);
			if (parameterCount() != 1)
			{
				throw new RuntimeException("function '"+function_name+"' must have one argument.");
			}

			try
			{
				md_sha1=MessageDigest.getInstance("SHA-1");
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new RuntimeException("Could not initialize MessageDigest SHA-1.");
			}
		}

//========================================================================
		public TObject evaluate(GroupResolver group, VariableResolver resolver,
			QueryContext context)
		{
			TObject ob0 = getParameter(0).evaluate(group, resolver, context);
			if (ob0.isNull() || ob0.getObject().toString().equals(""))
			{
				return null;
			}
			return new TObject(ob0.getTType(),
				toSHA1HashString(ob0.getObject().toString())
			);
		}

//========================================================================
		public TType returnTType()
		{
			return TType.STRING_TYPE;
		}
	}//end SHA1HashFunction

//========================================================================
//========================================================================
	private static class XMLElementFunction extends AbstractFunction
	{
		final static String function_name="el";
//========================================================================
		public XMLElementFunction(Expression[] params)
		{
			super(function_name, params);
			if (parameterCount() != 3)
			{
				throw new RuntimeException("function '"+function_name+"' must have 3 arguments: element name, content, attrs");
			}
		}

//========================================================================
		public TObject evaluate(GroupResolver group, VariableResolver resolver,
			QueryContext context)
		{
			TObject ob0 = getParameter(0).evaluate(group, resolver, context);
			TObject ob1 = getParameter(1).evaluate(group, resolver, context);
			TObject ob2 = getParameter(2).evaluate(group, resolver, context);

			if (ob0.isNull() || ob0.getObject().toString().equals(""))
			{
				return null;
			}
			String attrs="";
			if(ob2.isNull() || ob2.getObject().toString().equals(""))
			{
			}
			else
			{
				attrs=" "+ob2.getObject().toString();
			}

			String content="";
			if(ob1.isNull() || ob1.getObject().toString().equals(""))
			{
			}
			else
			{
				content=ob1.getObject().toString();
			}
			return new TObject(
				ob0.getTType(),
				"<"+ob0.getObject().toString()+attrs+">"
				+content
				+"</"+ob0.getObject().toString()+">"
			);
		}

//========================================================================
		public TType returnTType()
		{
			return TType.STRING_TYPE;
		}
	}//end XMLElementFunction

//========================================================================
//========================================================================
	private static class XMLAttributeFunction extends AbstractFunction
	{
		final static String function_name="a";
//========================================================================
		public XMLAttributeFunction(Expression[] params)
		{
			super(function_name, params);
			if (parameterCount() != 2)
			{
				throw new RuntimeException("function '"+function_name+"' must have 2 arguments: attribute name, value");
			}
		}

//========================================================================
		public TObject evaluate(GroupResolver group, VariableResolver resolver,
			QueryContext context)
		{
			TObject ob0 = getParameter(0).evaluate(group, resolver, context);
			TObject ob1 = getParameter(1).evaluate(group, resolver, context);

			if (ob0.isNull() || ob0.getObject().toString().equals(""))
			{
				return null;
			}
			String value="";
			if(ob1.isNull() || ob1.getObject().toString().equals(""))
			{
			}
			else
			{
				value=ob1.getObject().toString();
			}
			return new TObject(
				ob0.getTType(),
				ob0.getObject().toString()+"=\""+value+"\" "
			);
		}

//========================================================================
		public TType returnTType()
		{
			return TType.STRING_TYPE;
		}
	}//end XMLAttributeFunction

//========================================================================
//========================================================================
	private static class AHREFFunction extends AbstractFunction
	{
		final static String function_name="ahref";
//========================================================================
		public AHREFFunction(Expression[] params)
		{
			super(function_name, params);
			if (parameterCount() != 4)
			{
				throw new RuntimeException("function '"+function_name+"' must have 4 arguments: url, title, target, onclick");
			}
		}

//========================================================================
		public TObject evaluate(GroupResolver group, VariableResolver resolver,
			QueryContext context)
		{
			TObject ob0 = getParameter(0).evaluate(group, resolver, context);
			TObject ob1 = getParameter(1).evaluate(group, resolver, context);
			TObject ob2 = getParameter(2).evaluate(group, resolver, context);
			TObject ob3 = getParameter(3).evaluate(group, resolver, context);

			if (ob0.isNull() || ob0.getObject().toString().equals(""))
			{
				return null;
			}
			String title="";
			if(ob1.isNull() || ob1.getObject().toString().equals(""))
			{
			}
			else
			{
				title=ob1.getObject().toString();
			}
			String target="";
			if(ob2.isNull() || ob2.getObject().toString().equals(""))
			{
			}
			else
			{
				target=" target=\""+ob2.getObject().toString()+"\"";
			}
			String onclick="";
			if(ob3.isNull() || ob3.getObject().toString().equals(""))
			{
			}
			else
			{
				onclick=" onclick=\""+ob3.getObject().toString()+"\"";
			}
			return new TObject(
				ob0.getTType(),
				"<a href=\""+ob0.getObject().toString()+"\""+target+onclick+">"
				+title+"</a>"
			);
		}

//========================================================================
		public TType returnTType()
		{
			return TType.STRING_TYPE;
		}
	}//end AHREFFunction

//========================================================================
//========================================================================
	private static class OptionFunction extends AbstractFunction
	{
		final static String function_name="opt";
//========================================================================
		public OptionFunction(Expression[] params)
		{
			super(function_name, params);
			if (parameterCount() != 2)
			{
				throw new RuntimeException("function '"+function_name+"' must have 2 arguments: value, title");
			}
		}

//========================================================================
		public TObject evaluate(GroupResolver group, VariableResolver resolver,
			QueryContext context)
		{
			TObject ob0 = getParameter(0).evaluate(group, resolver, context);
			TObject ob1 = getParameter(1).evaluate(group, resolver, context);

			if (ob0.isNull() || ob0.getObject().toString().equals(""))
			{
				return null;
			}
			String title="";
			if(ob1.isNull() || ob1.getObject().toString().equals(""))
			{
			}
			else
			{
				title=ob1.getObject().toString();
			}
			return new TObject(
				ob0.getTType(),
				"<option value=\""+ob0.getObject().toString()+"\">"
				+title+"</option>"
			);
		}

//========================================================================
		public TType returnTType()
		{
			return TType.STRING_TYPE;
		}
	}//end OptionFunction

//========================================================================
//========================================================================
	private static class ButtonFunction extends AbstractFunction
	{
		final static String function_name="btn";
//========================================================================
		public ButtonFunction(Expression[] params)
		{
			super(function_name, params);
			if (parameterCount() != 2)
			{
				throw new RuntimeException("function '"+function_name+"' must have 2 arguments: html button attributes: value, onclick");
			}
		}

//========================================================================
		public TObject evaluate(GroupResolver group, VariableResolver resolver,
			QueryContext context)
		{
			TObject ob0 = getParameter(0).evaluate(group, resolver, context);
			TObject ob1 = getParameter(1).evaluate(group, resolver, context);

			if (ob0.isNull() || ob0.getObject().toString().equals(""))
			{
				return null;
			}
			String title="";
			if(ob1.isNull() || ob1.getObject().toString().equals(""))
			{
			}
			else
			{
				title=ob1.getObject().toString();
			}

			//<input type="button" value="foo" onclick="submitForm($id(\'sql_query_form\'));"/>
			return new TObject(
				ob0.getTType(),
				"<input type=\"button\" value=\""+ob0.getObject().toString()+"\" onclick=\""+ob1.getObject().toString()+"\" />"
			);
		}

//========================================================================
		public TType returnTType()
		{
			return TType.STRING_TYPE;
		}
	}//end ButtonFunction

}//end class CustomFunctionFactory
//EOF
