package com.mckoi.database;
import com.mckoi.database.global.*;

import java.security.MessageDigest;

/*
adds two functions to be used in sql queries:

	md5_hash(string)
	sha1_hash(string)

i.e.
	select md5_hash(concat(field1,field2,'foo')) as md5,* from bar;

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
			TObject ob = getParameter(0).evaluate(group, resolver, context);
			if (ob.isNull())
			{
				return ob;
			}
			return new TObject(ob.getTType(),
				toMD5HashString(ob.getObject().toString())
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
			TObject ob = getParameter(0).evaluate(group, resolver, context);
			if (ob.isNull())
			{
				return ob;
			}
			return new TObject(ob.getTType(),
				toSHA1HashString(ob.getObject().toString())
			);
		}

//========================================================================
		public TType returnTType()
		{
			return TType.STRING_TYPE;
		}
	}//end SHA1HashFunction
}//end class CustomFunctionFactory
//EOF
