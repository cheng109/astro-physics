
package ImageDownload;

public class multipleMain {
    public static void main (String[] args) {

	String confName = "database.properties"; 
	confCrawler config = new confCrawler(confName); 	    
	int numThreads = config.numThreads; 
	int ind = 0; 
	for(ind=0; ind < numThreads; ind++ ) {
	    CrawlThread thread = new CrawlThread (ind, confName);
	    thread.jobID = ind; 
	    thread.start();

	}
    }
}
