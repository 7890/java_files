package interfaces;

import java.io.File;

//=============================================================================
public interface ProcessFileHook
{
	public String processFile(File file, String originalFilename, String mimeType, String md5Sum, boolean use_relative_uris) throws Exception;
	public void close() throws Exception;
}
//EOF
