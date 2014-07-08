package ImageDownload;

class CrawlThread implements Runnable {
    Thread t;
    String task = new String(); 
    WebCrawl WebCrawler; 
    CrawlThread(String task) {
	this.task=task; 
	WebCrawler = new WebCrawl(task); 
	t = new Thread(this, "crawling"); 
	t.start(); 
    }
    public void run() {
	WebCrawler.startCrawl();
	WebCrawler.crawl(); 
    }
	
    public static void main (String[] args) {
	final CrawlThread thread = new CrawlThread("task1"); 
    }
		
}
