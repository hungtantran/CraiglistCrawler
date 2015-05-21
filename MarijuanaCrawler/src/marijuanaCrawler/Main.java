package marijuanaCrawler;

import commonlib.Globals;

public class Main {
	public static void crawlCraigList() {
		IWebsiteCrawler crawler = null;

		try {
			crawler = new CraiglistCrawler();

			if (!crawler.crawl()) {
				Globals.crawlerLogManager.writeLog("Fail to crawl craiglist");
			}
		} catch (Exception e) {
			Globals.crawlerLogManager.writeLog("Fail to create CraiglistCrawler object and crawl " + e.getMessage());
			e.printStackTrace();
			return;
		}
	}
	
	public static void crawlYelp() {
		IWebsiteCrawler crawler = null;

		try {
			crawler = new YelpCrawler();

			if (!crawler.crawl()) {
				Globals.crawlerLogManager.writeLog("Fail to crawl yelp");
			}
		} catch (Exception e) {
			Globals.crawlerLogManager.writeLog("Fail to create YelpCrawl object and crawl " + e.getMessage());
			e.printStackTrace();
			return;
		}
	}
	
	public static void crawlLeafly() {
		IWebsiteCrawler crawler = null;

		try {
			crawler = new LeaflyCrawler();

			if (!crawler.crawl()) {
				Globals.crawlerLogManager.writeLog("Fail to crawl leafly");
			}
		} catch (Exception e) {
			Globals.crawlerLogManager.writeLog("Fail to create LeaflyCrawler object and crawl " + e.getMessage());
			e.printStackTrace();
			return;
		}
	}

	public static void main(String[] args) {
		crawlCraigList();
		// crawlYelp();
		// crawlLeafly();
	}
}
