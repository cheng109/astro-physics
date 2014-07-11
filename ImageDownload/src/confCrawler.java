package ImageDownload; 

import java.io.*;
import java.util.Properties;


public class confCrawler 
{
    private static final int MAXTHREAD = 20;
    public String fileName; 

    public String jdbcUSERNAME; 
    public String jdbcPASSWORD;
    public int crawlerMAXURL; 
    public String parentPATH; 
    public String parentDBURL; 

    public String downLoadListName; 
    public String[] dbName = new String[MAXTHREAD]; 
    public String[] urlRoot  = new String[MAXTHREAD];
    public String[] childPath  = new String[MAXTHREAD]; 
    public String[] fullPath  = new String[MAXTHREAD];
    public String[] nameDB = new String[MAXTHREAD]; 
    public String[] jdbcURL = new String[MAXTHREAD];  
    public Properties props;
    
    public int numThreads = 0; 

    confCrawler(String fileName) {
	this.fileName = fileName;
	try {
	    this.readProperties();
	    this.readList(this.downLoadListName);  
	} catch(Exception e) {
	}
    }

    public void readProperties() throws IOException {
	try {
	    props = new Properties();
	    FileInputStream in = new FileInputStream(fileName); 
	    props.load(in);
	    this.parentDBURL=props.getProperty("jdbc.url");
	    this.jdbcUSERNAME=props.getProperty("jdbc.username");
	    this.jdbcPASSWORD=props.getProperty("jdbc.password");
	    this.parentPATH=props.getProperty("download.path");
	    this.crawlerMAXURL=Integer.parseInt(props.getProperty("crawler.maxurls")); 
	    this.downLoadListName=props.getProperty("download.list");
	    in.close();
	} catch (Exception e) {
	    e.printStackTrace(); 
	}
    }

    public void readList(String nameListFile) {
	try {
	    BufferedReader buff = new BufferedReader(new FileReader(nameListFile)); 
	    String line; 
	    while((line = buff.readLine())!=null) {

		if(line.charAt(0)!='#') {
		    String[] array = line.split("\\s+"); 

		    this.nameDB[numThreads] = array[0];
		    this.childPath[numThreads] = array[1];
		    this.urlRoot[numThreads] = "http://"+array[2];
		    
		    this.fullPath[numThreads] = this.concatePATH(this.parentPATH, this.childPath[numThreads]); 
		    this.jdbcURL[numThreads] = this.concatePATH(this.parentDBURL, this.nameDB[numThreads]); 
		    this.numThreads ++; 
		}
	    }

	    buff.close();
	} catch (Exception e) {
	}
	
    }


    public String concatePATH ( String parent, String child ) {
	if(parent.charAt(parent.length()-1)!='/') {
	    return parent + "/" + child;
	} else {
	    return parent + child;
	}
    }

    



}

   	






