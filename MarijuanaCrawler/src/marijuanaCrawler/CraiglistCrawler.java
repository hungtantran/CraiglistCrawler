package marijuanaCrawler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import commonlib.Globals;
import commonlib.NetworkingFunctions;
import commonlib.Globals.Domain;
import commonlib.Helper;
import dbconnection.MySqlConnection;

public class CraiglistCrawler {
	protected Map<String, Globals.Location> linkToLocationMap = null;
	protected Set<String> urlsCrawled = null;
	protected PriorityQueue<String> urlsQueue = null;
	protected MySqlConnection mysqlConnection = null;
	protected int numRetriesDownloadLink = 2;

	public CraiglistCrawler() {
		if (!readConfig())
			return;

		this.urlsCrawled = new HashSet<String>();
		this.mysqlConnection = new MySqlConnection(Globals.username,
				Globals.password, Globals.server, Globals.database);

		// Get start urls (location links for now)
		if (!this.getStartLinks()) {
			return;
		}

		try {
			ResultSet crawled = this.mysqlConnection
					.getLinkCrawled(Domain.CRAIGLIST.value);
			while (crawled.next()) {
				this.urlsCrawled.add(crawled.getString("link"));
			}

			if (Globals.DEBUG)
				Globals.crawlerLogManager.writeLog("Urls in Crawled Set : "
						+ this.urlsCrawled.size());
		} catch (SQLException e) {
			Globals.crawlerLogManager.writeLog(e.getMessage());
		}
	}

	private boolean readConfig() {
		// TODO read in the config paramaters from a file.
		return true;
	}

	private boolean getStartLinks() {
		this.linkToLocationMap = this.mysqlConnection.GetLocationLink();

		return (this.linkToLocationMap != null);
	}

	public boolean crawl() {
		if (this.urlsCrawled == null || this.mysqlConnection == null
				|| this.linkToLocationMap == null)
			return false;

		while (true) {
			// If the queue is empty, restock the links again
			if (this.urlsQueue == null || this.urlsQueue.isEmpty()) {
				if (this.linkToLocationMap.size() == 0)
					break;

				// If this is the first crawl time, initilize the queue.
				// Otherwise, wait for 10 to 15 minutes before the next crawl.
				if (this.urlsQueue == null) {
					this.urlsQueue = new PriorityQueue<String>(100, null);
				} else {
					Helper.waitSec(30 * 60, 60 * 60);
				}

				for (Map.Entry<String, Globals.Location> entry : this.linkToLocationMap
						.entrySet()) {
					this.urlsQueue.add(entry.getKey());
				}
			}

			String url = this.urlsQueue.remove();
			// this.urlsCrawled.add(url);
			// this.mysqlConnection.insertIntoLinkCrawledTable(url,
			// Domain.CRAIGLIST.value, 1, null, null);

			Globals.Location curLocation = this.linkToLocationMap.get(url);
			if (curLocation == null)
				continue;

			CraiglistEntryLinkCrawl crawler = new CraiglistEntryLinkCrawl(url);
			if (!crawler.parseEntryLinks())
				continue;

			// Add new links into the queue table
			Set<String> entryLinks = crawler.getEntryLinks();
			for (String entryLink : entryLinks) {
				if (!this.urlsCrawled.contains(entryLink)) {
					// this.urlsQueue.add(entryLink);
					// Add html into the RawHTML table
					this.urlsCrawled.add(entryLink);
					String htmlContent = NetworkingFunctions
							.downloadHtmlContentToString(entryLink,
									this.numRetriesDownloadLink);
					if (htmlContent == null)
						continue;

					int id = this.mysqlConnection.insertIntoLinkCrawledTable(
							entryLink, Domain.CRAIGLIST.value, 1, null, null,
							curLocation.country, curLocation.state,
							curLocation.city);
					if (id < 0) {
						Globals.crawlerLogManager
								.writeLog("Fail to insert link " + entryLink
										+ " into table link_crawled_table");
						continue;
					}

					Integer positivePage = null;
					int predict1 = Classifier.classify(htmlContent);
					Integer predict2 = null;
					
					if (!this.mysqlConnection.insertIntoRawHTMLTable(id,
							entryLink, htmlContent, positivePage, predict1,
							predict2, curLocation.country, curLocation.state,
							curLocation.city)) {
						Globals.crawlerLogManager
								.writeLog("Insert content of link " + entryLink
										+ " into RawHTML table fails");
					}

					// this.mysqlConnection.insertIntoLinkQueueTable(entryLink,
					// Domain.CRAIGLIST.value, 1, 0, null, null);
					Helper.waitSec(Globals.DEFAULTLOWERBOUNDWAITTIMESEC, Globals.DEFAULTUPPERBOUNDWAITTIMESEC);
				}
			}
		}

		return true;
	}

	public static void main(String[] args) {
		CraiglistCrawler crawler = new CraiglistCrawler();

		if (!crawler.crawl())
			Globals.crawlerLogManager.writeLog("Fail to crawl");
	}
}
