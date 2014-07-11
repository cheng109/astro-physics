package ImageDownload; 
import java.io.File;
import java.sql.ResultSet;
import java.sql.Statement;

public class WebCrawl {
    public Crawler crawler; 
    public int maxurls; 
    public String root; 
    public int jobID; 
    public WebCrawl(int jobID, String confName) {
	this.jobID = jobID;
	crawler = new Crawler(jobID, confName); 
    }
	
    public void startCrawl() {
	crawler.NextURLIDScanned = 0; 
	try {
	    root = crawler.config.urlRoot[jobID]; 
	    File f=new File(crawler.config.fullPath[jobID]); 
	    if(!f.isDirectory()) {
		f.mkdir(); 
	    }
	    maxurls = crawler.config.crawlerMAXURL;
	    crawler.createDB();
	    crawler.insertURLInDB(root); 
	    crawler.ImageurlID = 0; 
	    crawler.urlID=1;
	    crawler.fetchURL(root);	
	    crawler.fetchImageURL(root); 
	}catch(Exception e) {
	    e.printStackTrace();
	}
    }
	
    public void crawl()  {
	while(crawler.NextURLIDScanned<crawler.NextURLID) {	
	    try {
		Statement statFetchURL = crawler.connection.createStatement();
		String query = "select url from URLS where (urlid ='"+crawler.NextURLIDScanned+"' )" ;
		statFetchURL.executeQuery( query );
		ResultSet rs = statFetchURL.getResultSet ();
			
		rs.next(); 
		String url1 = rs.getString("url"); 
		url1 = url1.replace('\"','\0');
		if(crawler.urlID<maxurls) {
		    crawler.fetchURL(url1);
		    System.out.println(crawler.NextURLIDScanned+ "/" + maxurls); 
		    System.out.println("[urlID]=" +crawler.urlID); 
		}	
		crawler.fetchImageURL(url1); 
	    }
	    catch( Exception e) {
		e.printStackTrace();
	    }
	    crawler.NextURLIDScanned++; 
	}
	System.out.println("nexturlid = "+crawler.NextURLID);     		

    }	
}
