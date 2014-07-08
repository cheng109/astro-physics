package ImageDownload; 


import java.io.File;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;


public class WebCrawl {
	
	public Crawler crawler = new Crawler();
	public int maxurls; 
	public Properties props;  
	public String root; 
	public String task; 
	public WebCrawl(String task){
		this.task=task;
	}
	
	public void startCrawl() {
		crawler.NextURLIDScanned = 0; 
		String folder=new String(); 
		try {
			crawler.readProperties();
			String parentPath=crawler.props.getProperty("download.path"); 
		
			if(task=="task1") {
				root = crawler.props.getProperty("crawler.root1");
				folder = crawler.props.getProperty("folder1"); 
				crawler.jdbcURL = crawler.props.getProperty("jdbc.url1");
			}
			if(task=="task2") {
				root = crawler.props.getProperty("crawler.root2");
				folder = crawler.props.getProperty("folder2"); 
				crawler.jdbcURL = crawler.props.getProperty("jdbc.url2");
			}
			
			int pos = crawler.jdbcURL.lastIndexOf('/'); 
			crawler.nameDB = crawler.jdbcURL.substring(pos+1, crawler.jdbcURL.length()); 
			System.out.println(crawler.nameDB); 
			crawler.path = parentPath + folder+ "/"; 
			File f=new File(crawler.path); 
			if(!f.isDirectory()) {
				f.mkdir(); 
			}
			maxurls = Integer.parseInt(crawler.props.getProperty("crawler.maxurls")); 
			System.out.println(crawler.jdbcURL); 
			crawler.createDB();
			crawler.insertURLInDB(root); 
			crawler.ImageurlID = 0; 
			crawler.urlID=1;
			crawler.fetchURL(root);	
			crawler.fetchDescription(root, crawler.NextURLIDScanned); 

			crawler.fetchImageURL(root); 
		}
		catch( Exception e) {
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
				crawler.fetchDescription(url1, crawler.NextURLIDScanned); 
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
