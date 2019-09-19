




import java.io.*;

import java.net.InetAddress;
import java.net.NetworkInterface;
import org.apache.commons.io.*;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
//import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public  class Backup {
	private final static Logger logger = LoggerFactory.getLogger(Backup.class);
	private static Options OPTIONS = new Options();
	 private static CommandLine commandLine;
	 private  static String HELP_STRING = null;
	 private  static String source= "c:\\";
	 private  static String type= "DOC,DOCX,XLS,XLSX,PPT,PPTX";
	 private  static String dest="d:\\backup";
	
	 private  static String exclude="c:\\$";

	 private static String SERVER="localhost";
	 private static int PORT= 21;
	 private static String USERNAME="upload";
	 private static String PASSWORD="abc123Mm";
	 private static String LOCAL_CHARSET = "GBK";

	 private static int mode=1;
	 private static FTPClient ftp;


public static boolean isExcludeFile(String filename,String excludelist) {
	//String newexcludelist=excludelist+",~$";
	String[] aaa=excludelist.split(",");
	
	int count=0;
	for(int i=0;i<aaa.length;i++)
		if (filename.startsWith(aaa[i])) 
			count++;
	if (count>0 )
		return true;
	else
		return false;
}
	


public static boolean needBackup(String fileName,String backupfiletype) {
	boolean myflag=false;
	if(fileName.lastIndexOf(".")>0){
		 String fileType=fileName.substring(fileName.lastIndexOf(".")+1,fileName.length());
		// String fileShortName=fileName.substring(fileName.lastIndexOf("\\")+1,fileName.length());
		 String[] aa = backupfiletype.split(",");
		 
		 for (int i = 0 ; i <aa.length ; i++ ) {
			 if(fileType.toUpperCase().equals(aa[i].toUpperCase())) {
				 myflag=true;
				 return myflag;
			 }
		 }
	}
		
	return myflag;
	
	

}
	//String[] OfficeType=new String[]{"DOC","DOCX","XLS","XLSX","PPT","PPTX","TXT"};
public static void traverseFolder2(String path) {
        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (null == files || files.length == 0) {
                //System.out.println("Directory is empty!");
                return;
            }
            else {
                for (File file2 : files) {
                		if (file2.isDirectory()) {
                    		traverseFolder2(file2.getAbsolutePath());
                    	}
                    	 else {//is file
                    		 	String fileName=file2.getAbsolutePath();
                    		 	//logger.info(fileName);
                    		 	//logger.info(exclude);
                    		 	if(!isExcludeFile(fileName,exclude)) {  
                    		 		
                    		 		if (needBackup(fileName,type)) { 
                    		 			//need byackup 
                    				 switch(mode) {
                    				 case 1://local backup;
                    					 localBackup(fileName,dest);
                    					 break;
                    					 
                    				 case 2://ftp backup
                    				
                    					 
                    					 ftpBackup(fileName,SERVER,PORT,USERNAME,PASSWORD);
                    					 break;
                    				
                    				 default:
                    					 break;
                    				 }
                    		 		}
                    		 	}
                    	 }
                }
            }
        }
}

public static void localBackup(String myfile,String mydest) {
	try {
	File source=new File(myfile);
	String fileShortName=myfile.substring(myfile.lastIndexOf("\\")+1,myfile.length());
	File dest=new File(mydest+"\\"+fileShortName);
	FileUtils.copyFile(source, dest);
	System.out.println(source+"   copied to " +dest);
	logger.info(source+"   copied "+dest);
	}
	catch(Exception e){
		//System.out.println(e.getMessage());
		logger.error("error localBackup");
		
	}
	
		
}
public  static String getLocalMac(){
       String abc=null;
		try {
			InetAddress ia = InetAddress.getLocalHost();
			byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
      
			StringBuffer sb = new StringBuffer("");
			for(int i=0; i<mac.length; i++) {
				if(i!=0) {
					sb.append("");
				}
             int temp = mac[i]&0xff;
             String str = Integer.toHexString(temp);
          
             if(str.length()==1) {
                 sb.append("0"+str);
                 }else {
                 sb.append(str);
             }
			}
         logger.debug("local MAC address:"+sb.toString().toUpperCase());
         		abc=sb.toString().toUpperCase();
         		return abc;
		}catch(Exception e) {
		//	System.out.println(e.getMessage());
			logger.error("can't get mac  address");
			return null;
		}
}




public static void ftpBackup(String myfile,String myftpserver,int myport,String myuser,String mypassword){
	
	String mydir=getLocalMac();
//	logger.info("my mac address:"+mydir);
	String fileShortName=myfile.substring(myfile.lastIndexOf("\\")+1,myfile.length());
	
	try {
 	if(!ftp.isConnected()||!ftp.isAvailable()) {
 		 	ftp = new FTPClient();
 	 	    ftp.connect(myftpserver,myport);
 	 	  ftp.login(myuser, mypassword);
 	 	 // mydir=getLocalMac();
	 	    ftp.makeDirectory(mydir);
	 	    ftp.changeWorkingDirectory(mydir);
				ftp.enterLocalPassiveMode();
				ftp.setRemoteVerificationEnabled(false);
				ftp.setControlEncoding("UTF-8");
				ftp.setFileType(FTP.BINARY_FILE_TYPE);
 		}
 		
 				
 		//System.out.println("now current path:"+ftp.printWorkingDirectory());
 		
 	
 		//logger.info(myfile);
 		FileInputStream input = new FileInputStream(myfile);
 		
 		if( ftp.storeFile(new String(fileShortName.getBytes(LOCAL_CHARSET),"ISO-8859-1"), input)) {
        			System.out.println( myfile+ " uploaded...") ;
        			logger.info(myfile+ " uploaded...");
        		
        }
         input.close();
 		
 	 	
 	
	}catch(IOException e) {
		//System.out.println(e.getMessage());
		logger.error("error in ftpbackup");
		
	}
}
	
public static void initFtp(String ftpserver,int port,String username,String password) {
		try
        {
           // int reply;
            ftp=new FTPClient();
            ftp.connect(ftpserver,port);
            
        }
    
		catch (Exception e)
        {
           // e.printStackTrace();
			logger.error("in initftp ,error init");
            System.exit(1);
        }
		
		try
        {
            if (!ftp.login(username, password))
            {
                ftp.logout();
                logger.error("in initftp,usesrname or password is not correct");     
               // System.err.println("usesrname or password is not correctr.");
                
                System.exit(1);
            }
            else {
            	
            	String mydir=getLocalMac();
            	logger.info("mac address:"+mydir);
     	 	    
     	 	    if(mydir!=null) {
     	 	    	//System.out.println(mydir);
     	 	    	if(ftp.makeDirectory(mydir)) 
     	 	    		System.out.println("create dir successful!");
     	 	    	if(ftp.changeWorkingDirectory(mydir))
     	 	    		logger.info("ftp change dir successful!");
					ftp.enterLocalPassiveMode();
					ftp.setRemoteVerificationEnabled(false);
					ftp.setControlEncoding("UTF-8");
					ftp.setFileType(FTP.BINARY_FILE_TYPE);
     	 	    }
     	 	    else
     	 	    	logger.error("in initftp ,error get mac");
         	
            }
        }catch(Exception e) {
        	logger.error("in initftp");
        	}
		
          
  }
public static void main(String[] args) {
		initCliArgs(args);
		try{
		  mode = Integer.valueOf(commandLine.getOptionValue( "m" ));
		  if(mode==2) {
			//  InetAddress ia = InetAddress.getLocalHost();
		  	   dest=getLocalMac();
			  initFtp(SERVER,PORT,USERNAME,PASSWORD);
			  
			  }
		

            File dir = new File(dest);
            if (!dir.exists()||!dir.isDirectory())
            	dir.mkdir();
            	
			}
            catch(Exception x){
            	logger.error("in main ,error");
            }
       
		traverseFolder2(source);
	}
	
	 private static void initCliArgs(String[] args) {
		// TODO Auto-generated method stub
		CommandLineParser commandLineParser = new DefaultParser();
       // help
       OPTIONS.addOption("help","usage help");
       // source
       OPTIONS.addOption(Option.builder("s").hasArg(true).longOpt("source").type(String.class).desc("source directory").build());
    // file type 
       OPTIONS.addOption(Option.builder("t").hasArg(true).longOpt("type").type(String.class).desc("file type").build());
       OPTIONS.addOption(Option.builder("e").hasArg(true).longOpt("exclude").type(String.class).desc("exclude directory").build());
       // 
       // dest
       OPTIONS.addOption(Option.builder("d").hasArg(true).longOpt("dest").type(String.class).desc("destination directory").build());
       OPTIONS.addOption(Option.builder("m").hasArg(true).longOpt("mode").type(String.class).desc("backup mode").build());
       OPTIONS.addOption(Option.builder("f").hasArg(true).longOpt("ftpserver").type(String.class).desc("ftp server").build());
       OPTIONS.addOption(Option.builder("p").hasArg(true).longOpt("ftpport").type(Short.class).desc("ftp server").build());
       OPTIONS.addOption(Option.builder("u").hasArg(true).longOpt("ftpuser").type(String.class).desc("ftp user").build());
       OPTIONS.addOption(Option.builder("P").hasArg(true).longOpt("ftppass").type(String.class).desc("ftp password").build());
       try {
           commandLine = commandLineParser.parse(OPTIONS, args);
           
           if( commandLine.hasOption( "s" ) ) {
               // initialise the member variable
               source = commandLine.getOptionValue( "s" );
               //System.out.println(source);
               logger.info("backup source is:"+source);
           }
           if( commandLine.hasOption( "t" ) ) {
           //     initialise the member variable
               type = commandLine.getOptionValue( "t" );
			    logger.info("file type:"+type);
           }
           if( commandLine.hasOption( "d" ) ) {
               // initialise the member variable
               dest = commandLine.getOptionValue( "d" );
			    logger.info("destination:"+dest);
				
           }
           if( commandLine.hasOption( "e" ) ) {
               // initialise the member variable
               exclude = commandLine.getOptionValue( "e" );
			    logger.info("exclude dir:"+exclude);
           }
           if( commandLine.hasOption( "m" ) ) {
               // initialise the member variable
               mode = Integer.valueOf(commandLine.getOptionValue( "m" ));
			   logger.info("mode is:"+commandLine.getOptionValue( "m"));
           }
           if( commandLine.hasOption( "f" ) ) {
               // initialise the member variable
               SERVER = commandLine.getOptionValue( "f" );
			    logger.info("ftp server ip:"+SERVER);
           }
           if( commandLine.hasOption( "p" ) ) {
               // initialise the member variable
               PORT = Integer.valueOf(commandLine.getOptionValue( "p" ));
			    logger.info("ftp server port:"+PORT);
           }
           if( commandLine.hasOption( "u" ) ) {
               // initialise the member variable
               USERNAME = commandLine.getOptionValue( "u" );
			    logger.info("username:"+USERNAME);
				
           }
           if( commandLine.hasOption( "P" ) ) {
               // initialise the member variable
               PASSWORD = commandLine.getOptionValue( "P" );
			    logger.info("password:"+PASSWORD);
           }
       } catch (ParseException e) {
           //System.out.println(e.getMessage() + "\n" + getHelpString());
           logger.error("error in initcliargs");
           System.exit(0);
       }
	}
	 
	 private static String getHelpString() {
	        if (HELP_STRING == null) {
	            HelpFormatter helpFormatter = new HelpFormatter();

	            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	            PrintWriter printWriter = new PrintWriter(byteArrayOutputStream);
	            helpFormatter.printHelp(printWriter, HelpFormatter.DEFAULT_WIDTH, "java Backup [-m 1] [-s sourcedir] [-d destdir] [-t doc,docx,xls,xlsx,ppt,pptx]  [-e excludedir]  or java Backup [-m 2] [-s sourcedir] [-f ftpserver] [-p port] [-u user] [-p pass] [-t doc,docx,xls,xlsx,ppt,pptx]  [-e excludedir]", null,
	                    OPTIONS, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, null);
	            printWriter.flush();
	            HELP_STRING = new String(byteArrayOutputStream.toByteArray());
	            printWriter.close();
	        }
	        return HELP_STRING;
	    }
}



