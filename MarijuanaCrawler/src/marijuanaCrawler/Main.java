package marijuanaCrawler;

import commonlib.Globals;

public class Main {
	public static void main(String[] args) {
		IWebsiteCrawler crawler = null;

		try {
			crawler = new CraiglistCrawler();

			if (!crawler.crawl()) {
				Globals.crawlerLogManager.writeLog("Fail to crawl");
			}
		} catch (Exception e) {
			Globals.crawlerLogManager.writeLog("Fail to create CraiglistCrawler object");
			e.printStackTrace();
		}
	}
}
