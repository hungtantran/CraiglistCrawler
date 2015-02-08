package marijuanaCrawler;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import commonlib.Globals;
import commonlib.Globals.Domain;
import commonlib.Helper;
import commonlib.Location;
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

public class CraiglistCrawler implements WebsiteCrawler {
	private Map<String, Location> linkToLocationMap = null;
	private Set<String> urlsCrawled = null;
	private Queue<String> urlsQueue = null;
	private final int numRetriesDownloadLink = 2;

	private RawHTMLDAO rawHTMLDAO = null;
	private LinkCrawledDAO linkCrawledDAO = null;
	private LocationLinkDAO locationLinkDAO = null;

	private final String[] searchTerms = { "420 weed", "marijuana" };

	public CraiglistCrawler() throws Exception {
		if (!this.readConfig()) {
			throw new Exception("Read config file fails");
		}

		final DAOFactory daoFactory = DAOFactory.getInstance(Globals.username,
				Globals.password, Globals.server + Globals.database);

		this.rawHTMLDAO = new RawHTMLDAOJDBC(daoFactory);

		this.linkCrawledDAO = new LinkCrawledDAOJDBC(daoFactory);

		this.locationLinkDAO = new LocationLinkDAOJDBC(daoFactory);

		// Get start urls (location links for now)
		this.linkToLocationMap = new HashMap<String, Location>();

		final List<LocationLink> locationLinks = this.locationLinkDAO.get();
		for (final LocationLink locationLink : locationLinks) {
			final String link = locationLink.getLink();
			final String country = locationLink.getCity();
			final String state = locationLink.getState();
			final String city = locationLink.getCity();
			final Location location = new Location(country, state, city);

			this.linkToLocationMap.put(link, location);
		}

		final List<LinkCrawled> linksCrawled = this.linkCrawledDAO
		        .get(Domain.CRAIGLIST.value);

		this.urlsCrawled = new HashSet<String>();
		for (final LinkCrawled linkCrawled : linksCrawled) {
			this.urlsCrawled.add(linkCrawled.getLink());
		}

		if (Globals.DEBUG) {
			Globals.crawlerLogManager.writeLog("Urls in Crawled Set : " + this.urlsCrawled.size());
		}
	}

	private boolean readConfig() {
		// TODO read in the config paramaters from a file.
		return true;
	}

	private boolean processOneEntryLink(String entryLink, Location loc)
			throws SQLException {
		final String htmlContent = NetworkingFunctions
				.downloadHtmlContentToString(entryLink,
						this.numRetriesDownloadLink);
		if (htmlContent == null) {
			Globals.crawlerLogManager.writeLog("Fail to download link " + entryLink);
			return false;
		}

		final LinkCrawled linkCrawled = new LinkCrawled();
		linkCrawled.setLink(entryLink);
		linkCrawled.setDomainTableId1(Domain.CRAIGLIST.value);
		linkCrawled.setPriority(1);
		linkCrawled.setTimeCrawled(null);
		linkCrawled.setDateCrawled(null);
		linkCrawled.setLocation(loc);

		int id = -1;

		try {
			id = this.linkCrawledDAO.create(linkCrawled);
		} catch (final SQLException e) {
			Globals.crawlerLogManager.writeLog("Insert content of link "
					+ entryLink + " into RawHTML table fails");
			throw e;
		}

		if (id < 0) {
			Globals.crawlerLogManager.writeLog("Fail to insert link "
					+ entryLink + " into table link_crawled_table");
			return false;
		}

		final Short positivePage = null;
		final Short predict1 = Classifier.classify(htmlContent);
		final Short predict2 = null;

		final RawHTML rawHTML = new RawHTML();
		rawHTML.setId(id);
		rawHTML.setUrl(entryLink);
		rawHTML.setHtml(htmlContent);
		rawHTML.setPositive(positivePage);
		rawHTML.setPredict1(predict1);
		rawHTML.setPredict2(predict2);
		rawHTML.setLocation(loc);

		try {
			if (this.rawHTMLDAO.create(rawHTML) < 0) {
				Globals.crawlerLogManager.writeLog("Insert content of link "
						+ entryLink + " into RawHTML table fails");
			}
		} catch (final SQLException e) {
			Globals.crawlerLogManager.writeLog("Insert content of link "
					+ entryLink + " into RawHTML table fails");
			throw e;
		}

		return true;
	}

	private boolean processOneLocationLink(String locationUrl) throws Exception {
		final Location curLocation = this.linkToLocationMap.get(locationUrl);
		if (curLocation == null) {
			throw new Exception("Unexpected error: location link "
					+ locationUrl + " is not poresent in the map");
		}

		final EntryLinkCrawler crawler = new CraiglistEntryLinkCrawl(
				locationUrl);

		// Process each search term at a time
		for (final String term : this.searchTerms) {
			crawler.setQueryTerm(term);

			if (!crawler.startUp()) {
				Globals.crawlerLogManager
						.writeLog("Fail to start up from link " + locationUrl);
				continue;
			}

			// Iterate through every link in a given location link like
			// http://seattle.craigslist.org
			while (true) {
				// Add new links into the queue table
				final String nextEntryLink = crawler.getNextEntryLink();
				if (nextEntryLink == null) {
					Helper.waitSec(2, 5);
					break;
				}

				if (this.urlsCrawled.contains(nextEntryLink)) {
					continue;
				}

				// this.urlsQueue.add(entryLink);
				this.urlsCrawled.add(nextEntryLink);

				// Classify the link and add relevant information into the
				// database
				final boolean processLinkSuccess = this.processOneEntryLink(
						nextEntryLink, curLocation);
				if (!processLinkSuccess) {
					continue;
				}

				Helper.waitSec(Globals.DEFAULTLOWERBOUNDWAITTIMESEC,
						Globals.DEFAULTUPPERBOUNDWAITTIMESEC);
			}
		}

		return true;
	}

	@Override
	public boolean crawl() throws Exception {
		if (this.urlsCrawled == null) {
			throw new Exception("Unexpected error: url crawl set is null");
		}

		if (this.linkToLocationMap == null) {
			throw new Exception("Unexpected error: location link map is null");
		}

		if (this.linkToLocationMap.size() == 0) {
			Globals.crawlerLogManager
					.writeLog("There is no location links to start from");
			return false;
		}

		while (true) {
			// If the queue is empty, restock the links again
			if (this.urlsQueue == null || this.urlsQueue.isEmpty()) {
				// If this is the first crawl time, initilize the queue.
				// Otherwise, wait for 10 to 15 minutes before the next crawl.
				if (this.urlsQueue == null) {
					this.urlsQueue = new PriorityQueue<String>(100, null);
				} else {
					Globals.crawlerLogManager
							.writeLog("Finish crawling once, wait before recrawl again");
					Helper.waitSec(30 * 60, 60 * 60);
					Globals.crawlerLogManager
							.writeLog("Finish waiting, restart crawling again");
				}

				for (final Map.Entry<String, Location> entry : this.linkToLocationMap
						.entrySet()) {
					this.urlsQueue.add(entry.getKey());
				}
			}

			final String locationUrl = this.urlsQueue.remove();

			final boolean processLocLinkSuccess = this
					.processOneLocationLink(locationUrl);

			if (!processLocLinkSuccess) {
				Globals.crawlerLogManager.writeLog("Process location link "
						+ locationUrl + " fails");
				continue;
			}
		}
	}
}
