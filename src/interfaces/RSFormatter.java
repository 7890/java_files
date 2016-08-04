package interfaces;

import java.io.Writer;
import java.sql.ResultSet;

//=============================================================================
public interface RSFormatter
{
	public String formatRS(ResultSet rs) throws Exception;
	public void formatRS(ResultSet rs, Writer os) throws Exception;
}
//EOF
