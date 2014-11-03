package marijuanaCrawler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import commonlib.Globals;
import commonlib.NetworkingFunctions;

public class CraiglistEntryLinkCrawl {
	private final String searchPageSurfix = "/search/sss?query=";
	private final String sortParam = "&sort=rel&s=";
	private final String[] searchTerms = { "420+weed", "420+marijuana",
			"marijuana", "weed" };
	private final int numRetryDownloadPage = 2;
	// A complete link = locationLink + searchPageSurfix + <searchterm> +
	// sortParam + <(pagenum+1)*100>

	private String domain = null;
	private String locationLink = null;
	private Set<String> entryLinkSet = null;

	public CraiglistEntryLinkCrawl(String locationLink) {
		if (locationLink != null) {
			this.locationLink = locationLink;
			try {
				URL locationUrl = new URL(this.locationLink);
				this.domain = locationUrl.getHost();
			} catch (MalformedURLException e) {
				return;
			}
			this.entryLinkSet = new HashSet<String>();
		}
	}

	public Set<String> getEntryLinks() {
		return this.entryLinkSet;
	}

	public boolean isValid() {
		return (this.entryLinkSet != null && this.domain != null && this.locationLink != null);
	}

	private String constructPageLink(String searchTerm, int pageNum) {
		if (searchTerm == null || pageNum <= 0)
			return null;

		String pageLink = this.locationLink + this.searchPageSurfix
				+ searchTerm + this.sortParam + (pageNum - 1) * 100;

		return pageLink;
	}

	// Return the number of links found
	public int parseEntryLinksOnePage(int pageNum) {
		int totalLinksFound = 0;

		for (String searchTerm : this.searchTerms) {
			String pageLink = this.constructPageLink(searchTerm, pageNum);

			if (pageLink == null)
				continue;

			Globals.crawlerLogManager.writeLog("Search page: " + pageLink);
			Document doc = NetworkingFunctions.downloadHtmlContentToDoc(
					pageLink, numRetryDownloadPage);

			// If fail to download the page, return found no links
			if (doc == null)
				continue;

			Elements contentElems = doc.select("div[class=content]");

			// There should be only one content div containing all the links
			if (contentElems.size() == 1) {
				Elements rowElems = contentElems.get(0).select("p[class=row]");
				int numRows = rowElems.size();

				for (int i = 0; i < numRows; i++) {
					Elements linkElems = rowElems.get(i).select("a[href]");

					// If there is no link on the row
					if (linkElems.size() == 0)
						continue;

					String entryLink = linkElems.get(0).attr("href");

					try {
						URL entryUrl = new URL(this.locationLink);
						URL absoluteUrl = new URL(entryUrl, entryLink);
						String absoluteLink = absoluteUrl.toString();

						// The link found is not from the same domain or from
						// the
						// same area
						if (absoluteLink.indexOf(this.domain) == -1)
							continue;

						Globals.crawlerLogManager.writeLog("Found entry link: "
								+ absoluteLink);
						totalLinksFound++;

						this.entryLinkSet.add(absoluteLink);
					} catch (Exception e) {
						// TODO
					}
				}
			}
		}

		return totalLinksFound;
	}

	public boolean parseEntryLinks() {
		if (!this.isValid())
			return false;

		int curPage = 1;

		// Keep iterate through all the page until there is no more links on the
		// page
		while (true) {
			int numLinksInPage = this.parseEntryLinksOnePage(curPage);

			if (numLinksInPage == 0)
				break;

			curPage++;
		}

		Globals.crawlerLogManager.writeLog("Found total of "
				+ this.entryLinkSet.size());

		return true;
	}

	public static void main(String[] args) {
		CraiglistEntryLinkCrawl crawler = new CraiglistEntryLinkCrawl(
				"http://seattle.craigslist.org");

		if (crawler.parseEntryLinks()) {

		}
	}
}
