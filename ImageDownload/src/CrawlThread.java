package ImageDownload;

class CrawlThread implements Runnable {
    private Thread t;
    int jobID;
    WebCrawl WebCrawler; 
    CrawlThread(int jobID, String confName) {
	this.jobID = jobID; 
	WebCrawler = new WebCrawl(this.jobID, confName); 
    }
    public void run() {
	WebCrawler.startCrawl();
	WebCrawler.crawl(); 
    }
		
    public void start() {
	t = new Thread(this, "crawling"); 
	t.start(); 
    }

}
