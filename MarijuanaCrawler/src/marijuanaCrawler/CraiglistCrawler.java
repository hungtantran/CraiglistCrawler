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
	private int numRetriesDownloadLink = 2;

	private RawHTMLDAO rawHTMLDAO = null;
	private LinkCrawledDAO linkCrawledDAO = null;
	private LocationLinkDAO locationLinkDAO = null;
	
	private final String[] searchTerms = { "420 weed", "marijuana" };

	public CraiglistCrawler() throws Exception {
		if (!readConfig()) {
			throw new Exception("Read config file fails");
		}

		this.rawHTMLDAO = new RawHTMLDAOJDBC(DAOFactory.getInstance(
			Globals.username,
			Globals.password,
			Globals.server + Globals.database));

		this.linkCrawledDAO = new LinkCrawledDAOJDBC(DAOFactory.getInstance(
			Globals.username,
			Globals.password,
			Globals.server + Globals.database));

		this.locationLinkDAO = new LocationLinkDAOJDBC(DAOFactory.getInstance(
			Globals.username,
			Globals.password,
			Globals.server + Globals.database));

		// Get start urls (location links for now)
		this.linkToLocationMap = new HashMap<String, Location>();

		List<LocationLink> locationLinks = this.locationLinkDAO.get();
		for (LocationLink locationLink : locationLinks) {
			String link = locationLink.getLink();
			String country = locationLink.getCity();
			String state = locationLink.getState();
			String city = locationLink.getCity();
			Location location = new Location(country, state, city);

			this.linkToLocationMap.put(link, location);
		}

		List<LinkCrawled> linksCrawled = this.linkCrawledDAO
				.get(Domain.CRAIGLIST.value);
		
		this.urlsCrawled = new HashSet<String>();
		for (LinkCrawled linkCrawled : linksCrawled) {
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
	
	private boolean processOneEntryLink(String entryLink, Location loc) throws SQLException {
		String htmlContent = NetworkingFunctions.downloadHtmlContentToString(entryLink, this.numRetriesDownloadLink);
		if (htmlContent == null) {
			Globals.crawlerLogManager.writeLog("Fail to download link " + entryLink);
			return false;
		}

		LinkCrawled linkCrawled = new LinkCrawled();
		linkCrawled.setLink(entryLink);
		linkCrawled.setDomainTableId1(Domain.CRAIGLIST.value);
		linkCrawled.setPriority(1);
		linkCrawled.setTimeCrawled(null);
		linkCrawled.setDateCrawled(null);
		linkCrawled.setLocation(loc);

		int id = -1;

		try {
			id = this.linkCrawledDAO.create(linkCrawled);
		} catch (SQLException e) {
			Globals.crawlerLogManager.writeLog("Insert content of link " + entryLink + " into RawHTML table fails");
			throw e;
		}

		if (id < 0) {
			Globals.crawlerLogManager.writeLog("Fail to insert link " + entryLink + " into table link_crawled_table");
			return false;
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
		rawHTML.setLocation(loc);

		try {
			if (this.rawHTMLDAO.create(rawHTML) < 0) {
				Globals.crawlerLogManager.writeLog("Insert content of link " + entryLink + " into RawHTML table fails");
			}
		} catch (SQLException e) {
			Globals.crawlerLogManager.writeLog("Insert content of link " + entryLink + " into RawHTML table fails");
			throw e;
		}
		
		return true;
	}
	
	private boolean processOneLocationLink(String locationUrl) throws Exception {
		Location curLocation = this.linkToLocationMap.get(locationUrl);
		if (curLocation == null) {
			throw new Exception("Unexpected error: location link " + locationUrl + " is not poresent in the map");
		}

		EntryLinkCrawler crawler = new CraiglistEntryLinkCrawl(locationUrl);
		
		// Process each search term at a time
		for (String term : this.searchTerms) {
			crawler.setQueryTerm(term);
			
			if (!crawler.startUp()) {
				Globals.crawlerLogManager.writeLog("Fail to start up from link " + locationUrl);
				continue;
			}
			
			// Iterate through every link in a given location link like http://seattle.craigslist.org
			while (true) {
				// Add new links into the queue table
				String nextEntryLink = crawler.getNextEntryLink();
				if (nextEntryLink == null) {
					break;
				}
			
				if (this.urlsCrawled.contains(nextEntryLink)) {
					continue;
				}
				
				// this.urlsQueue.add(entryLink);
				this.urlsCrawled.add(nextEntryLink);
				
				// Classify the link and add relevant information into the database
				boolean processLinkSuccess = processOneEntryLink(nextEntryLink, curLocation);
				if (!processLinkSuccess) {
					continue;
				}
				
				Helper.waitSec(Globals.DEFAULTLOWERBOUNDWAITTIMESEC, Globals.DEFAULTUPPERBOUNDWAITTIMESEC);
			}
		}
		
		return true;
	}

	public boolean crawl() throws Exception {
		if (this.urlsCrawled == null) {
			throw new Exception("Unexpected error: url crawl set is null");
		}
		
		if (this.linkToLocationMap == null) {
			throw new Exception("Unexpected error: location link map is null");
		}
		
		if (this.linkToLocationMap.size() == 0) {
			Globals.crawlerLogManager.writeLog("There is no location links to start from");
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
					Globals.crawlerLogManager.writeLog("Finish crawling once, wait before recrawl again");
					Helper.waitSec(30 * 60, 60 * 60);
					Globals.crawlerLogManager.writeLog("Finish waiting, restart crawling again");
				}

				for (Map.Entry<String, Location> entry : this.linkToLocationMap.entrySet()) {
					this.urlsQueue.add(entry.getKey());
				}
			}

			String locationUrl = this.urlsQueue.remove();

			boolean processLocLinkSuccess = processOneLocationLink(locationUrl);
			
			if (!processLocLinkSuccess) {
				Globals.crawlerLogManager.writeLog("Process location link " + locationUrl + " fails");
				continue;
			}
		}
	}
}
