package marijuanaCrawler;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import commonlib.Globals;
import commonlib.Globals.Domain;
import commonlib.Helper;
import commonlib.NetworkingFunctions;

import dbconnection.DAOFactory;
import dbconnection.LinkCrawled;
import dbconnection.LinkCrawledDAO;
import dbconnection.LinkCrawledDAOJDBC;
import dbconnection.LocationLink;
import dbconnection.LocationLinkDAO;
import dbconnection.LocationLinkDAOJDBC;
import dbconnection.RawHTML;
import dbconnection.RawHTMLDAO;
import dbconnection.RawHTMLDAOJDBC;

public class CraiglistCrawler {
	protected Map<String, Globals.Location> linkToLocationMap = null;
	protected Set<String> urlsCrawled = null;
	protected PriorityQueue<String> urlsQueue = null;
	protected int numRetriesDownloadLink = 2;

	protected RawHTMLDAO rawHTMLDAO = null;
	protected LinkCrawledDAO linkCrawledDAO = null;
	protected LocationLinkDAO locationLinkDAO = null;

	public CraiglistCrawler() throws SQLException, ClassNotFoundException {
		if (!readConfig())
			return;

		this.urlsCrawled = new HashSet<String>();

		this.rawHTMLDAO = new RawHTMLDAOJDBC(DAOFactory.getInstance(
				Globals.username, Globals.password, Globals.server
						+ Globals.database));

		this.linkCrawledDAO = new LinkCrawledDAOJDBC(DAOFactory.getInstance(
				Globals.username, Globals.password, Globals.server
						+ Globals.database));

		this.locationLinkDAO = new LocationLinkDAOJDBC(DAOFactory.getInstance(
				Globals.username, Globals.password, Globals.server
						+ Globals.database));

		// Get start urls (location links for now)
		this.linkToLocationMap = new HashMap<String, Globals.Location>();

		List<LocationLink> locationLinks = this.locationLinkDAO.get();
		for (LocationLink locationLink : locationLinks) {
			String link = locationLink.getLink();
			String country = locationLink.getCity();
			String state = locationLink.getState();
			String city = locationLink.getCity();
			Globals.Location location = new Globals.Location(country, state,
					city);

			this.linkToLocationMap.put(link, location);
		}

		List<LinkCrawled> linksCrawled = this.linkCrawledDAO
				.get(Domain.CRAIGLIST.value);
		for (LinkCrawled linkCrawled : linksCrawled) {
			this.urlsCrawled.add(linkCrawled.getLink());
		}

		if (Globals.DEBUG)
			Globals.crawlerLogManager.writeLog("Urls in Crawled Set : "
					+ this.urlsCrawled.size());
	}

	private boolean readConfig() {
		// TODO read in the config paramaters from a file.
		return true;
	}

	public boolean crawl() {
		if (this.urlsCrawled == null || this.linkToLocationMap == null)
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
					this.urlsCrawled.add(entryLink);
					// TODO add urlsCrawled to database
					String htmlContent = NetworkingFunctions
							.downloadHtmlContentToString(entryLink,
									this.numRetriesDownloadLink);
					if (htmlContent == null)
						continue;

					LinkCrawled linkCrawled = new LinkCrawled();
					linkCrawled.setLink(entryLink);
					linkCrawled.setDomainTableId1(Domain.CRAIGLIST.value);
					linkCrawled.setPriority(1);
					linkCrawled.setTimeCrawled(null);
					linkCrawled.setDateCrawled(null);
					linkCrawled.setCountry(curLocation.country);
					linkCrawled.setState(curLocation.state);
					linkCrawled.setCity(curLocation.city);

					int id = -1;

					try {
						id = this.linkCrawledDAO.create(linkCrawled);

						if (id < 0) {
							Globals.crawlerLogManager
									.writeLog("Insert content of link "
											+ entryLink
											+ " into RawHTML table fails");
							continue;
						}
					} catch (SQLException e) {
						e.printStackTrace();
						Globals.crawlerLogManager
								.writeLog("Insert content of link " + entryLink
										+ " into RawHTML table fails");
						continue;
					}

					if (id < 0) {
						Globals.crawlerLogManager
								.writeLog("Fail to insert link " + entryLink
										+ " into table link_crawled_table");
						continue;
					}

					Short positivePage = null;
					Short predict1 = Classifier.classify(htmlContent);
					Short predict2 = null;

					RawHTML rawHTML = new RawHTML();
					rawHTML.setId(id);
					rawHTML.setUrl(entryLink);
					rawHTML.setHtml(htmlContent);
					rawHTML.setPositive(positivePage);
					rawHTML.setPredict1(predict1);
					rawHTML.setPredict2(predict2);
					rawHTML.setCountry(curLocation.country);
					rawHTML.setState(curLocation.state);
					rawHTML.setCity(curLocation.city);

					try {
						if (this.rawHTMLDAO.create(rawHTML) < 0) {
							Globals.crawlerLogManager
									.writeLog("Insert content of link "
											+ entryLink
											+ " into RawHTML table fails");
						}
					} catch (SQLException e) {
						Globals.crawlerLogManager
								.writeLog("Insert content of link " + entryLink
										+ " into RawHTML table fails");
						continue;
					}

					// this.mysqlConnection.insertIntoLinkQueueTable(entryLink,
					// Domain.CRAIGLIST.value, 1, 0, null, null);
					Helper.waitSec(Globals.DEFAULTLOWERBOUNDWAITTIMESEC,
							Globals.DEFAULTUPPERBOUNDWAITTIMESEC);
				}
			}
		}

		return true;
	}

	public static void main(String[] args) {
		CraiglistCrawler crawler = null;

		try {
			crawler = new CraiglistCrawler();

			if (!crawler.crawl()) {
				Globals.crawlerLogManager.writeLog("Fail to crawl");
			}
		} catch (Exception e) {
			Globals.crawlerLogManager
					.writeLog("Fail to create CraiglistCrawler object");
			e.printStackTrace();
		}
	}
}
