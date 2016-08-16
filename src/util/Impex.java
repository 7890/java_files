/**
 * com.mckoi.tools.JDBCQueryTool  18 Aug 2000
 *
 * Mckoi SQL Database ( <A  HREF="http://www.mckoi.com/database">http://www.mckoi.com/database</A> )
 * Copyright (C) 2003 Guillemot Design Ltd
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Change Log:
 *
 *
 * Mike Calder-Smith March 2003.  Original import/export functions as implemented in JDBCQueryTool
 *
 *  export tablename filename
 *  import tablename filename
 *
 */
//package com.mckoi.tools;
//https://mckoi.com/database/maillist/msg03477.html

package util;

import com.mckoi.jfccontrols.ResultSetTableModel;
import com.mckoi.jfccontrols.Query;
import com.mckoi.util.CommandLine;
import java.sql.*;
import java.awt.*;
import java.util.*;
import java.io.*;

/**
 * JDBC Import/Export via McKoi
 *
 * @author Mike Calder
 *
 */

public class Impex extends Object {

  /**
   * The ResultSetTableModel for the table model that contains our result set.
   */
  private static ResultSetTableModel table_model;


  /**
  Current query text
  */
  static String qtext;

  /**
   * The JDBC Connection we have established to the server.
   */
  private static Connection connection;
  
  /**
  Constructor
  */
  public Impex() {
  }

  /**
   * Application start point.
   */
  public static void main(String[] args) {
    CommandLine cl = new CommandLine(args);

    String driver = cl.switchArgument("-jdbc", "com.mckoi.JDBCDriver");
    String url = cl.switchArgument("-url", ":jdbc:mckoi:");
    String username = cl.switchArgument("-u");
    String password = cl.switchArgument("-p");
    String cmdfilename = cl.switchArgument("-f", "impex.cmd");

    if ((cl.containsSwitch("-h")) || (cl.containsSwitch("-?"))) {

      printSyntax();
      helpSyntax();

    } else if (username == null) {
      System.err.println("Please provide a username");
      System.err.println();
      printSyntax();

    } else if (password == null) {
      System.err.println("Please provide a password");
      System.err.println();
      printSyntax();
    } else {
      try {
        System.err.println("Using JDBC Driver: " + driver);

        // Register the driver.
        Class.forName(driver).newInstance();

        // Make a connection to the server.
        connection = DriverManager.getConnection(url, username, password);

        if (connection != null) {
	  System.err.println("Connection established to: " + url);
	} else {
	  System.err.println("No connection to: " + url + " - severe error.");
	  return;
	}

        Impex impex = new Impex();

	// Read command file

        BufferedReader fin;
        int i;
        int l;
        int end;
        String s;
        String s1;
        String s2;

        try {
          fin = new BufferedReader(new InputStreamReader(new FileInputStream(cmdfilename)));
          while (true) {
            try {
              s = new String( fin.readLine() );
              if (s == null) break;

	      // Various types of comment indicator
              if (s.startsWith("//")) continue;
              if (s.startsWith("/*")) continue;
              if (s.startsWith("#")) continue;

	      qtext = s.trim();
	      if (qtext.length() == 0) continue;

	      impex.performcommand();

            } catch (Exception e) {
              break;
            }
          }
          fin.close();
        } catch (IOException ioex) {
          System.err.println( "Unable to open Import/Export command file " + cmdfilename );
        }
	
	// Finished

        connection.close();

      } catch (ClassNotFoundException e) {System.err.println("JDBC Driver not found.");}
        catch (Exception e) {e.printStackTrace();}
    }
  }

  /**
   * Prints the syntax to System.out.
   */
  private static void printSyntax() {
    System.out.println("Impex [-f command_file_name] [-h -? syntax] [-jdbc JDBC_Driver_Class] [-url JDBC_URL] -u username -p password");
  }

  /**
   * Prints the command file syntax to System.out.
   */
  private static void helpSyntax() {
    System.out.println("\n\nCommand file:");
    System.out.println("Lines beginning with '#', '//', or '/*' are treated as comments.");
    System.out.println("Blank lines are ignored.");
    System.out.println("Remaining lines must be of the form:");
    System.out.println("    export tablename filename");
    System.out.println("    import tablename filename");
    System.out.println("where 'tablename' is the name of the table (including schema if appropriate), and");
    System.out.println("'filename' is the name of the external data file to be imported from or exported to.");
  }

  /**
  */
  private void performcommand() {
    int i;
    String err;

    try {

      String qtest = qtext.trim().toLowerCase();

      if (qtest.startsWith("export")) {

        // " export table filename "

        StringTokenizer st = new StringTokenizer(qtext);
	String tok = st.nextToken();

	String tablename;
	String schema;
	String filename;
	String sqlstring;

	if (st.hasMoreTokens()) {
	  tablename = st.nextToken();

	  System.err.println("Tablename >" + tablename +"<");

	  String[] ns = tablename.split("\\.", 2);

	  System.err.println("ns length >" + ns.length +"<");

	  if (ns.length > 1) {
	    tablename = ns[1];
	    schema = ns[0];
	  } else {
	    schema = "APP";	// default schema
	  }
	  filename = "default.out";
	  if (st.hasMoreTokens()) {
	    filename = st.nextToken();
	  }
          System.err.println("Exporting " + schema + "." + tablename + " to " + filename );

	  try {
	    PrintWriter exporter = new PrintWriter( new BufferedWriter( new FileWriter(filename) ) );
	    sqlstring = "Select * from " + schema + "." + tablename;
            ResultSetTableModel ntm = new ResultSetTableModel();
            Query qry = new Query( sqlstring );
            ResultSet rset = executeQuery(qry);
            ntm.updateResultSet(rset);

	    // System.err.println( "Export sql :" + sqlstring );
	    System.err.println( "Result set rows = " + ntm.getRowCount() );

	    csvprint( ntm, exporter );
	    exporter.flush();
	    exporter.close();

	  } catch(Exception e) {
	    err = "Can't export table " + tablename + " to file " + filename + " Exception " + e;
	    System.err.println( err );
	    e.printStackTrace();
	  }
	}

	return;

      } else if (qtest.startsWith("import")) {

        // "import table filename "

        String linesep = System.getProperty("line.separator");
        StringTokenizer st = new StringTokenizer(qtext);
	String tok = st.nextToken();

	String tablename;
	String schema = "";
	String filename;
	String temp;
	String sqlstring = "";
	Query qry;
	ResultSet rs;
	BufferedReader br;
	PreparedStatement pstmt;
	int count;
	int rowcount;
	int rcode;

	if (st.hasMoreTokens()) {
	  tablename = st.nextToken();
	  String[] ns = tablename.split("\\.", 2);
	  if (ns.length > 1) {
	    tablename = ns[1];
	    schema = ns[0];
	  }

	  if (st.hasMoreTokens()) {
	    filename = st.nextToken();
	    try {

	      br = new BufferedReader( new FileReader( new File(filename) ) );
	      temp = br.readLine();
	    } catch(Exception e) {
	      err = "File " + filename + " could not be read, exception " + e;
	      //e.printStackTrace();
	      System.err.println( err );
	      System.err.println( err + "Can't Import this file");
              return;
	    }

	    try {
	      if (temp != null) temp = temp.trim();
              executeQuery(new Query("SET AUTO COMMIT OFF"));

	      // get the table metadata
	      String insSql1 = "";
	      String insSql2 = "";
	      Vector columns = new Vector();
	      Vector ctypes = new Vector();
	      int[] columnType;
	      int csize;
	      try {
	        DatabaseMetaData dbmd = connection.getMetaData();

		// Egregious hack; if schema not supplied, assumes tablename unique.
		if (schema.equals("")) {
	          rs = dbmd.getColumns( null, null, tablename, "%" );
		} else {
	          rs = dbmd.getColumns( null, schema, tablename, "%" );
		}

		String columnname;
		int ctype;
                if (rs != null) {
		  i = 0;
                  while (rs.next()) {
		    schema = rs.getString(2);
		    columnname = rs.getString(4);
		    ctype = rs.getInt(5);
		    columns.add( columnname );
		    ctypes.add( new Integer(ctype) );
		    // System.err.println("Metadata Column " + columnname + " type " + ctype );
		    i++;
		  }
		  if (i == 0) {
	            System.err.println( "No columns in table " + tablename + "Can't Import this file");
	            return;
		  }
		} else {
	          System.err.println("Null Result set from table " + tablename);
		  return;
		}
		csize = columns.size();
		columnType = new int[csize];

		insSql1 = "Insert into " + schema + "." + tablename + " (" + "\""+(String)columns.elementAt(0)+"\"";
		insSql2 = " ) VALUES( ?";
		String keyval;
		columnType[0] = ((Integer)ctypes.elementAt(0)).intValue();

		for (i=1;i<csize;i++) {
		  keyval = (String)columns.elementAt(i);
		  columnType[i] = ((Integer)ctypes.elementAt(i)).intValue();
		  insSql1 = insSql1 + ", " + "\""+keyval+"\"";
		  insSql2 = insSql2 + ", ?";
		}
		insSql1 = insSql1 + insSql2 + ")";

	      } catch(Exception me) {
	        err = "Can't get metadata for table " + tablename + " Exception " + me;
		System.err.println("SQL so far: " + insSql1 + insSql2);
		//me.printStackTrace();
	        System.err.println( err + "Can't Import this file");
	        return;
	      }

              System.err.println(insSql1);

	      // Build a prepared statement with the right number of ?s
	      pstmt = connection.prepareStatement( insSql1 );


	      System.err.println("Inserting data from " + filename + " to table " + schema + "." + tablename );
	      // System.err.println("SQL is " + insSql1 );
	      count = 0;
	      rowcount = 0;

	      while (temp != null) {
	        if (temp.length() == 0) {
		  temp = br.readLine();
		  if (temp != null) temp = temp.trim();

		  System.err.println("Record rowcount " + rowcount );
		  continue;
		}
	        temp = temp.replaceAll(  "\\\\u000A", "\n" );
		temp = temp.replaceAll(  "\\\\u0008", "\b" );
		temp = temp.replaceAll(  "\\\\u0009", "\t" );
		temp = temp.replaceAll(  "\\\\u000D", "\r" );
		temp = temp.replaceAll(  "\\\\u000C", "\f" );
		temp = temp.replaceAll(  "\\\\u0027", "\'" );
		temp = temp.replaceAll(  "\\\\u0022", "\"" );
		temp = temp.replaceAll(  "\\\\u005C", "\\\\" );

		// sqlstring is used by exception reporting
	        sqlstring = "insert into " + schema + "." + tablename + " VALUES(" + temp + ")";

		count++;

		// Set each field by type; even more horrid hack - only uses setString() !
		int ic = 0;
		try {
		  for (i=0;i<csize;i++) {
		    ic = i+1;
		    switch( columnType[i] ) {
		      case java.sql.Types.BIGINT:
		      case java.sql.Types.BINARY:
		      case java.sql.Types.BLOB:
		      case java.sql.Types.BOOLEAN:
		      case java.sql.Types.CHAR:
		      case java.sql.Types.CLOB:
		      case java.sql.Types.DATE:
		      case java.sql.Types.DECIMAL:
		      case java.sql.Types.DOUBLE:
		      case java.sql.Types.FLOAT:
		      case java.sql.Types.INTEGER:
		      case java.sql.Types.LONGVARBINARY:
		      case java.sql.Types.LONGVARCHAR:
		      case java.sql.Types.NUMERIC:
		      case java.sql.Types.REAL:
		      case java.sql.Types.SMALLINT:
		      case java.sql.Types.TIME:
		      case java.sql.Types.TIMESTAMP:
		      case java.sql.Types.TINYINT:
		      case java.sql.Types.VARBINARY:
		      case java.sql.Types.VARCHAR:
		        if (temp.toLowerCase().equals("null")) {
		          pstmt.setString(ic, null);
			} else {
		          pstmt.setString(ic, getCommaDelim( temp, ic ) );
			  //System.err.println("Field " + ic + " Type " + columnType[i] +" Value " + getCommaDelim( temp, ic ) + "\n" );
			}
		        break;
		      case java.sql.Types.ARRAY:
		      case java.sql.Types.BIT:
		      case java.sql.Types.DATALINK:
		      case java.sql.Types.DISTINCT:
		      case java.sql.Types.JAVA_OBJECT:
		      case java.sql.Types.REF:
		      case java.sql.Types.STRUCT:
	                err = "Column " + ic + " in table " + tablename + " type " + columnType[i] +
			             " - Sorry, I don't know how to handle this type. Send the coder a nastygram. ";
		        System.err.println(err );
	                System.err.println("Can't Import this file");
	                return;
		      case java.sql.Types.OTHER:
		      default:
	                err = "Column " + ic + " in table " + tablename + " Value " + temp +
			             " Undefined type " + columnType[i];
		        System.err.println(err );
	                System.err.println( "Can't Import this file");
	                return;
		    }
		  }
		} catch(Exception me) {
	          err = "Column " + ic + " in table " + tablename + " Value " + temp + " Exception " + me;
		  System.err.println(err );
		  //me.printStackTrace();
	          System.err.println("Can't Import this file");
	          return;
		}

		rcode = pstmt.executeUpdate();

		if (rcode == 1) {
		  rowcount++;
		  // System.err.println("Imported record " + count );
		} else {
		  err = "Failed to insert record " + count + "Warning was " + pstmt.getWarnings();
		  System.err.println( err );
	          System.err.println("Can't Import this file");
                  executeQuery(new Query("ROLLBACK"));
		  System.err.println("Rolled back from import. ");
	          return;
		}
		temp = br.readLine();
		if (temp != null) temp = temp.trim();

	      }

              executeQuery(new Query("COMMIT"));
	      System.err.println("Imported " + rowcount + " records. " );
	      System.err.println("Import Complete. ");

	    } catch(Exception e) {
	      err = "Can't import table " + tablename + " from file " + filename + " Exception " + e;
	      //e.printStackTrace();
	      System.err.println( err );
	      System.err.println("Sqlstring was " + sqlstring );
              e.printStackTrace();
	      try {
                executeQuery(new Query("ROLLBACK"));
		System.err.println("Rolled back from import. ");
	      } catch(Exception e2) {
		//e2.printStackTrace();	      }
	        System.err.println( err + "Can't Import this file");
	      }
	    }
	  }
	}

	return;

      } else  {
	System.err.println("Unrecognised command: " + qtext);
      }

    } catch (Exception e) {
      System.err.println( "Exceptio e " + e);
      e.printStackTrace();
    }
  }

  /**
  Print CSV representation of ResultSet rows to a PrintWriter.
  <p>CSV Echo modification.
  @param ResultSetTableModel ResultSet data to print
  @param PrintWriter Output PrintWriter stream
  */
  private static void csvprint( ResultSetTableModel rsm, PrintWriter pw ) {
    int i, j, nrows, ncols;
    boolean isString;
    String temp;
    String linesep = System.getProperty("line.separator");

    nrows = rsm.getRowCount();
    ncols = rsm.getColumnCount();

    for (i=0;i<nrows;i++) {
      for (j=0;j<ncols;j++) {
	isString = false;
	try {
	  temp = (String)rsm.getValueAt(i,j);
	  isString = true;
	} catch(Exception e) {
	  temp = rsm.getValueAt(i,j).toString();
	}
	if(temp!=null)
	{
		temp = temp.replaceAll( "\\\\", "\\\\u005C" );
		temp = temp.replaceAll( linesep, "\\\\u000A" );
		temp = temp.replaceAll( "\b", "\\\\u0008" );
		temp = temp.replaceAll( "\t", "\\\\u0009" );
		temp = temp.replaceAll( "\r", "\\\\u000D" );
		temp = temp.replaceAll( "\f", "\\\\u000C" );
		temp = temp.replaceAll( "\'", "\\\\u0027" );
		temp = temp.replaceAll( "\"", "\\\\u0022" );
		if (isString) {
		  pw.write( "'" + temp + "'" );
		} else {
		  pw.write( temp );
		}
	}

	if ( j == (ncols-1)) {
	  pw.println( "" );
	} else {
	  pw.write( ", " );
	}
      }
    }
    pw.println( "" );
  }

  /**
  Get a value from a comma delimited string.
  <p>CSV Echo modification.
  <p>No, we couldn't just use a StringTokeniser.  A comma delimited string
  is tokens which may be strings or numbers separated by commas. Strings
  are enclosed between single quote characters; if a single quote
  character is required in a string it appears as two single quote
  characters. There may or may not be whitespace after the comma between
  the tokens.
  @param String Source comma delimited string
  @param int Number of field required (starting at 1)
  @return String String representation of the field value
  */
  static public String getCommaDelim( String source, int reqno ) {
    String res;
    String copy;
    boolean stringfield;
    boolean infield;
    boolean escaped;
    char c;
    int i;
    int l;
    int n;
    int quotecount;
    int start;
    int end;

    copy = new String( source );
    copy = copy.trim();
    l = copy.length();

    if (l == 0) return new String("");

    start = -1;
    end = -1;
    stringfield = false;
    escaped = false;
    quotecount = 0;
    infield = true;
    if (copy.charAt(0) == '\'') {
      stringfield = true;
    }
    if (copy.charAt(0) == '\\') {
      if (copy.charAt(1) == '\'') {
        stringfield = true;
        escaped = true;
      }
    }
    if (reqno == 1) {
      if (stringfield) {
        if (escaped) {
          start = 2;
        } else {
          start = 1;
        }
      } else {
        start = 0;
      }
    }

    for (i=0, n=1; i<l; i++) {
      c = copy.charAt(i);
      if ( infield ) {
        // We're looking for the end of the field
        if (stringfield) {
          if (c == '\\') {
            c = copy.charAt(++i);
            // Ignore non-quote escapes
            if (c != '\'') continue;
          }
          if (c == '\'') quotecount++;
          if (c == ',') {
            if ( (quotecount % 2) == 0 ) {
              infield = false;
              if ( n == reqno ) {
                end = i - 1;
                break;
              }
            }
          }
        } else {
          if (c == ',') {
            infield = false;
            if ( n == reqno ) {
              end = i;
              break;
            }
          }
        }
      } else {
        // We're looking for the start of a new field
        if (Character.isWhitespace( c )) continue;
        infield = true;
        if (c == '\'') {
          stringfield = true;
          quotecount = 1;
        } else if (c == '\\') {
          c = copy.charAt(++i);
          if (c == '\'') {
            stringfield = true;
            escaped = true;
            quotecount = 1;
          }
        } else {
          stringfield = false;
        }
        n++;
        if ( n == reqno ) {
          if (stringfield) {
            start = i + 1;
          } else {
            start = i;
          }
        }
      }
    }
    if (start != -1) {
      if (end == -1) {
        end = l;
      }
      if (stringfield && (end == l)) end--;
      if (stringfield && escaped) {
        if (copy.charAt(end-1) == '\\') end--;
      }
      res = copy.substring( start, end );
    } else {
      res = new String("");
    }

    return res;
  }
  
  
  /**
  Outrageous hack to replace QueryAgent executeQuery.
  */
  public ResultSet executeQuery(Query query) {
    ResultSet rs = null;
    try {
      // Set the new statement to be executed
      PreparedStatement statement =
                             connection.prepareStatement(query.getString());
      for (int i = 0; i < query.parameterCount(); ++i) {
        statement.setObject(i + 1, query.getParameter(i));
      }

      rs = statement.executeQuery();

    } catch(Exception e) {
      System.err.println("Failure Exception " + e);
      e.printStackTrace();
    }
    return rs;

  }


}//end class Impex
//EOF
