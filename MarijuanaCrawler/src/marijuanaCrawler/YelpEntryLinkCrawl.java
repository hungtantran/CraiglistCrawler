package marijuanaCrawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import commonlib.Globals;
import commonlib.Helper;
import commonlib.NetworkingFunctions;

import dbconnection.LocationDB;

public class YelpEntryLinkCrawl implements IEntryLinkCrawler {
	// A complete search link is like this http://www.yelp.com/search?find_loc=los%20angeles%2C+california&ns=1&find_desc=marijuana&start=100
	private static final String domain = "http://www.yelp.com/";
	private static final String searchPageSurfix = "search?find_loc=%s&ns=1&find_desc=%s&start=%d";

	private String searchTerm = null;

	private final int numRetryDownloadPage = 2;

	private LocationDB location = null;
	private List<String> entryLinkList = null;
	private int curLinkListIndex = -1;
	private int curPage = 0;

	public YelpEntryLinkCrawl(LocationDB location) throws Exception {
		if (location == null) {
			throw new Exception("Invalid location");
		}

		this.location = location;
	}

	@Override
	public boolean setQueryTerm(String queryTerm) {
		if (queryTerm == null) {
			return false;
		}

		this.searchTerm = queryTerm;

		return true;
	}

	@Override
	public String getNextEntryLink() throws IOException {
		this.curLinkListIndex++;

		if (this.curLinkListIndex < this.entryLinkList.size()) {
			return this.entryLinkList.get(this.curLinkListIndex);
		}

		// Run out of link on the current page, try the next page
		this.curPage++;

		List<String> curPageLink = null;
		try {
			curPageLink = this.parseEntryLinksOnePage(this.searchTerm, this.curPage);
		} catch (final MalformedURLException e) {
			e.printStackTrace();
			return null;
		}

		if (curPageLink == null || curPageLink.size() == 0) {
			Globals.crawlerLogManager.writeLog("Found no link for location:"
				+ this.location.toString() + ", page#:" + this.curPage
				+ ", with search term:" + this.searchTerm);
			return null;
		}

		for (final String link : curPageLink) {
			this.entryLinkList.add(link);
		}

		if (this.curLinkListIndex >= this.entryLinkList.size()) {
			Globals.crawlerLogManager.writeLog("Index " + this.curLinkListIndex + " is unexpected");
			return null;
		}

		return this.entryLinkList.get(this.curLinkListIndex);
	}

	private boolean isValid() {
		return (this.location != null);
	}

	private String constructPageLink(String searchTerm, int pageNum) {
		if (searchTerm == null || pageNum <= 0) {
			return null;
		}
		
		// A complete search link is like this http://www.yelp.com/search?find_loc=los%20angeles%2C+california&ns=1&find_desc=marijuana&start=100
		final String locationQueryString = this.location.getCity().replaceAll(" ", "%20") + "%2C+" + this.location.getState();
		final String termQueryString = this.searchTerm.replaceAll(" ", "%20");
		final String pageLink = String.format(YelpEntryLinkCrawl.domain + YelpEntryLinkCrawl.searchPageSurfix, locationQueryString, termQueryString, (this.curPage-1)*10);

		return pageLink;
	}

	// Return the number of links found
	private List<String> parseEntryLinksOnePage(String searchTerm, int pageNum) throws IOException {
		final List<String> linksFound = new ArrayList<String>();

		if (searchTerm == null) {
			Globals.crawlerLogManager.writeLog("There are no search term to get entry links");
			return linksFound;
		}

		Globals.crawlerLogManager.writeLog("Attempt to get parse entry links from location:"
			+ this.location.toString() + ", page#:" + pageNum
			+ ", with search term:" + searchTerm);

		final String pageLink = this.constructPageLink(this.searchTerm, pageNum);

		if (pageLink == null) {
			Globals.crawlerLogManager.writeLog("Fail to construct page:"
				+ this.location.toString() + ", page#:" + pageNum
				+ ", with search term:" + searchTerm);
			return linksFound;
		}

		Globals.crawlerLogManager.writeLog("Search page: " + pageLink);
		final Document doc = NetworkingFunctions.downloadHtmlContentToDoc(pageLink, this.numRetryDownloadPage);

		// If fail to download the page, return found no links
		if (doc == null) {
			Globals.crawlerLogManager.writeLog("Fail to download html of page " + pageLink);
			return linksFound;
		}

		final Elements contentElems = doc.select("ul[class=ylist ylist-bordered search-results]");

		// There should be only one content div containing all the links
		if (contentElems.size() != 1) {
			return linksFound;
		}

		final Elements rowElems = contentElems.get(0).select("li");
		final int numRows = rowElems.size();

		for (int i = 0; i < numRows; i++) {
			final Elements linkElems = rowElems.get(i).select("a[href]");

			// If there is no link on the row
			if (linkElems.size() == 0) {
				continue;
			}

			final String entryLink = linkElems.get(0).attr("href");

			final URL entryUrl = new URL(YelpEntryLinkCrawl.domain);
			final URL absoluteUrl = new URL(entryUrl, entryLink);
			String absoluteLink = absoluteUrl.toString();
			absoluteLink = Helper.stripParamUrl(absoluteLink);

			// The link found is not from the same domain or from the same area
			if (absoluteLink.indexOf(YelpEntryLinkCrawl.domain) == -1) {
				continue;
			}

			linksFound.add(absoluteLink);
		}

		return linksFound;
	}

	// Implements EntryLinkCrawler interface startUp function
	@Override
	public boolean startUp() {
		if (!this.isValid()) {
			Globals.crawlerLogManager.writeLog("Fail to start up Yelp Entry Crawler");
			return false;
		}

		this.curPage = 0;

		this.entryLinkList = new ArrayList<String>();

		return true;
	}

	public static void main(String[] args) {
		YelpEntryLinkCrawl crawler = null;

		try {
			LocationDB loc = new LocationDB();
			loc.setCountry("US");
			loc.setState("California");
			loc.setCity("los angeles");

			crawler = new YelpEntryLinkCrawl(loc);
			crawler.startUp();
			crawler.setQueryTerm("marijuana");

			while (true) {
				final String nextLink = crawler.getNextEntryLink();
				System.out.println(nextLink);
				if (nextLink == null) {
					break;
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
			System.out.println("Fail to create yelp entry link crawl");
		}
	}
}
