/*ECMA JavaScript file - comments *cannot* be like '//' - the use of ';' is mandatory*/
function c() {};
/*callback: for every directory, return true or false*/
c.filterD=function(path)
{
	J.println('testing '+path);
	var pat=new RegExp ('.*/[.]git');
	if(pat.test(path)){J.println("D filtered");return false;}
	return true;
};

/*callback: for every file, return true or false*/
c.filter=function(filename,displayname,uri,mimetype,length,md5sum,lastmodified,canwrite,canexecute)
{
	J.println(uri);
	var pat=new RegExp('.*/[.]git/.*');
	if(pat.test(uri)){return false;}
	return true;
};
/*callback: before all files and directories*/
c.header=function(dirname){};
/*callback: after all files and directories*/
c.footer=function(dirname){};
