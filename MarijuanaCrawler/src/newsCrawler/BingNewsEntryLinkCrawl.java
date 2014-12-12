package newsCrawler;

import java.util.HashMap;
import java.util.Map;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import commonlib.Globals;
import commonlib.NetworkingFunctions;

public class BingNewsEntryLinkCrawl {
	private final int numRetryDownloadPage = 2;
	private final String formatStringLink = "http://www.bing.com/news/search?q=%s&qft=sortbydate%3d\"1\"&first=%s&FORM=PENR";
	private final String[] searchTerms = { "marijuana" };

	public BingNewsEntryLinkCrawl() {
	}

	public Map<String, String> getLink(int pageNum) {
		if (pageNum < 1)
			return null;

		Map<String, String> linkToTitleMap = new HashMap<String,String>();
		int firstEntryOrder = (pageNum - 1) * 10 + 1;

		for (String searchTerm : searchTerms) {
			String startLink = "http://www.bing.com/news/search?q=" + searchTerm + "&qft=sortbydate%3d\"1\"&first=" + firstEntryOrder + "&FORM=YFNR";
			System.out.println("Start link = "+startLink);
			
			Document doc = NetworkingFunctions.downloadHtmlContentToDoc(
					startLink, this.numRetryDownloadPage);

			if (doc == null)
				return null;

			Elements newsElems = doc.select("div[class=sn_r]");

			// Return if the page has no country div
			if (newsElems.size() == 0) {
				Globals.crawlerLogManager.writeLog("No news left on page "
						+ pageNum + " with search term " + searchTerm);
				continue;
			}
			
			for (int i = 0; i < newsElems.size(); i++) {
				Element newsElem = newsElems.get(i);
				Elements linkElems = newsElem.select("a");
				
				if (linkElems.size() > 1) {
					Element linkElem = linkElems.get(0);
					String title = linkElem.text();
					String link = linkElem.attr("href");
					linkToTitleMap.put(link, title);
				}
			}
		}
		return linkToTitleMap;
	}

	public static void main(String[] args) {
		BingNewsEntryLinkCrawl crawler = new BingNewsEntryLinkCrawl();

		Map<String, String> linkToTitleMap = crawler.getLink(2);
		
		if (linkToTitleMap != null) {
			for (Map.Entry<String, String> entry : linkToTitleMap.entrySet()) {
				System.out.println(entry.getValue()+" : "+entry.getValue());
			}
		}
	}
}
