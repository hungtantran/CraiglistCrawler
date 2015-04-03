package marijuanaCrawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import commonlib.Globals;
import commonlib.NetworkingFunctions;

// TODO don't use Globals
public class CraiglistEntryLinkCrawl implements IEntryLinkCrawler {
	private final String searchPageSurfix = "/search/sss?query=";
	private final String sortParam = "&sort=rel&s=";

	private String searchTerm = null;

	private final int numRetryDownloadPage = 2;
	// A complete link = locationLink + searchPageSurfix + <searchterm> +
	// sortParam + <(pagenum+1)*100>

	private String domain = null;
	private String locationLink = null;
	private List<String> entryLinkList = null;
	private int curLinkListIndex = -1;
	private int curPage = 0;

	public CraiglistEntryLinkCrawl(String locationLink)
			throws MalformedURLException {
		if (locationLink == null) {
			throw new MalformedURLException();
		}

		this.locationLink = locationLink;

		final URL locationUrl = new URL(this.locationLink);
		this.domain = locationUrl.getHost();
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
			Globals.crawlerLogManager.writeLog("Found no link for page:"
					+ this.locationLink + ", page#:" + this.curPage
					+ ", with search term:" + this.searchTerm);
			return null;
		}

		for (final String link : curPageLink) {
			this.entryLinkList.add(link);
		}

		if (this.curLinkListIndex >= this.entryLinkList.size()) {
			Globals.crawlerLogManager.writeLog("Index " + this.curLinkListIndex
					+ " is unexpected");
			return null;
		}

		return this.entryLinkList.get(this.curLinkListIndex);
	}

	private boolean isValid() {
		return (this.domain != null && this.locationLink != null);
	}

	private String constructPageLink(String searchTerm, int pageNum) {
		if (searchTerm == null || pageNum <= 0) {
			return null;
		}

		final String pageLink = this.locationLink + this.searchPageSurfix
				+ searchTerm + this.sortParam + (pageNum - 1) * 100;

		return pageLink;
	}

	// Return the number of links found
	private List<String> parseEntryLinksOnePage(String searchTerm, int pageNum)
			throws IOException {
		final List<String> linksFound = new ArrayList<String>();

		if (searchTerm == null) {
			Globals.crawlerLogManager
					.writeLog("There are no search term to get entry links");
			return linksFound;
		}

		Globals.crawlerLogManager.writeLog("Attempt to get parse entry links from page:"
			+ this.locationLink + ", page#:" + pageNum
			+ ", with search term:" + searchTerm);

		final String pageLink = this
				.constructPageLink(this.searchTerm, pageNum);

		if (pageLink == null) {
			Globals.crawlerLogManager.writeLog("Fail to construct page:"
				+ this.locationLink + ", page#:" + pageNum
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

		final Elements contentElems = doc.select("div[class=content]");

		// There should be only one content div containing all the links
		if (contentElems.size() != 1) {
			return linksFound;
		}

		final Elements rowElems = contentElems.get(0).select("p[class=row]");
		final int numRows = rowElems.size();

		for (int i = 0; i < numRows; i++) {
			final Elements linkElems = rowElems.get(i).select("a[href]");

			// If there is no link on the row
			if (linkElems.size() == 0) {
				continue;
			}

			final String entryLink = linkElems.get(0).attr("href");

			final URL entryUrl = new URL(this.locationLink);
			final URL absoluteUrl = new URL(entryUrl, entryLink);
			final String absoluteLink = absoluteUrl.toString();

			// The link found is not from the same domain or from the same area
			if (absoluteLink.indexOf(this.domain) == -1) {
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
			Globals.crawlerLogManager.writeLog("Fail to start up Craiglist Entry Crawler");
			return false;
		}

		this.curPage = 0;

		this.entryLinkList = new ArrayList<String>();

		return true;
	}

	public static void main(String[] args) {
		CraiglistEntryLinkCrawl crawler = null;

		try {
			crawler = new CraiglistEntryLinkCrawl("http://seattle.craigslist.org");
			crawler.startUp();
			crawler.setQueryTerm("420 weed");

			while (true) {
				final String nextLink = crawler.getNextEntryLink();
				System.out.println(nextLink);
				if (nextLink == null) {
					break;
				}
			}
		} catch (final MalformedURLException e) {
			e.printStackTrace();
			System.out.println("Fail to create craiglist entry link crawl");
		} catch (final IOException e) {
		    e.printStackTrace();
            System.out.println("Fail to create craiglist entry link crawl");
		}
	}
}
