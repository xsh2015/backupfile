




import java.io.*;

import java.net.InetAddress;
import java.net.NetworkInterface;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
//import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public  class Restore {
	private final static Logger logger = LoggerFactory.getLogger(Restore.class);
	private static Options OPTIONS = new Options();
	 private static CommandLine commandLine;
	 private  static String HELP_STRING = null;
	 private  static String destdir="e:\\backup";
	 private static String SERVER="localhost";
	 private static int PORT= 21;
	 private static String USERNAME="upload";
	 private static String PASSWORD="abc123Mm";
	 private static String LOCAL_CHARSET = "GBK";
	 private static FTPClient ftp;

	 
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

public static void ftpRestore(String myftpserver,int myport,String myuser,String mypassword,String destdir){
	
	String mydir=getLocalMac();
	OutputStream os=null;
	File mylocaldir=new File(destdir);
	if(!mylocaldir.exists())
		mylocaldir.mkdir();
	else{
		if (mylocaldir.isFile())
			mylocaldir.mkdir();
	}		
	
	try {
	
 	
 	if(!ftp.isConnected()||!ftp.isAvailable()) {
 		
 		 
 		 	ftp = new FTPClient();
 	 	   
 	 	    ftp.connect(myftpserver,myport);
 	 	  ftp.login(myuser, mypassword);
 	 	  mydir=getLocalMac();
	 	  
	 	    ftp.changeWorkingDirectory(mydir);
			 ftp.enterLocalPassiveMode();
			ftp.setRemoteVerificationEnabled(false);
		
 	 	    
 	}
 		
 				
 		//System.out.println("now current path:"+ftp.printWorkingDirectory());
 		ftp.setControlEncoding("UTF-8");
 		ftp.setFileType(FTP.BINARY_FILE_TYPE);
 		FTPFile[] ftpFiles=ftp.listFiles();
 		logger.info("total files: "+String.valueOf(ftpFiles.length));
 		int count=0;
 		for(int i=0;i<ftpFiles.length;i++){
 			FTPFile file=ftpFiles[i];
 		//for(FTPFile file:ftpFiles){
 			//logger.info(file.getName());
 			File localFile=new File(destdir+"\\"+file.getName());
 			
 			os=new FileOutputStream(localFile);
 			ftp.retrieveFile(new String(file.getName().getBytes(LOCAL_CHARSET),"ISO-8859-1"),os);
 			
 			logger.info(String.valueOf(count++)+" " +file.getName()+" downloaded...");
 			os.close();
 			
 		}
 		
	}catch(IOException e) {
		
		logger.error("ftp download error");
		
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
            	logger.debug("successful connect ftp server");
            	String mydir=getLocalMac();
                
     	 	    
     	 	   // if(mydir!=null) {
     	 	    	//	System.out.println("error create dir successful!");
     	 	    	if(ftp.changeWorkingDirectory(mydir))
     	 	    		
     	 	    		logger.info("ftp change "+mydir+" successful!");
     	 	    	else
     	 	    	//System.out.println("error get mac ");
     	 	    		logger.error("ftp change "+mydir+" error!");
						
					ftp.enterLocalPassiveMode();
					ftp.setRemoteVerificationEnabled(false);
         	
            }
        }catch(Exception e) {
        	logger.error("in initftp");
        	}
		
          
  }
public static void main(String[] args) {
		initCliArgs(args);
	
		initFtp(SERVER,PORT,USERNAME,PASSWORD);
			  
	    ftpRestore(SERVER,PORT,USERNAME,PASSWORD,destdir);
}
		
	
	
	 private static void initCliArgs(String[] args) {
		// TODO Auto-generated method stub
		CommandLineParser commandLineParser = new DefaultParser();
       // help
       OPTIONS.addOption("help","usage help");
       OPTIONS.addOption(Option.builder("d").hasArg(true).longOpt("dest").type(String.class).desc("destination directory").build());
       OPTIONS.addOption(Option.builder("f").hasArg(true).longOpt("ftpserver").type(String.class).desc("ftp server").build());
       OPTIONS.addOption(Option.builder("p").hasArg(true).longOpt("ftpport").type(Short.class).desc("ftp server").build());
       OPTIONS.addOption(Option.builder("u").hasArg(true).longOpt("ftpuser").type(String.class).desc("ftp user").build());
       OPTIONS.addOption(Option.builder("P").hasArg(true).longOpt("ftppass").type(String.class).desc("ftp password").build());
       try {
           commandLine = commandLineParser.parse(OPTIONS, args);
           
         if( commandLine.hasOption( "d" ) ) {
               destdir = commandLine.getOptionValue( "d" );
			    logger.info("destination:"+destdir);
				
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
	            helpFormatter.printHelp(printWriter, HelpFormatter.DEFAULT_WIDTH, "java Restore  [-d destdir] [-f ftpserver] [-p port] [-u user] [-P pass]", null,
	                    OPTIONS, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, null);
	            printWriter.flush();
	            HELP_STRING = new String(byteArrayOutputStream.toByteArray());
	            printWriter.close();
	        }
	        return HELP_STRING;
	    }
}



