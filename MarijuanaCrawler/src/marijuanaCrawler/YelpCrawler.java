package marijuanaCrawler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.jsoup.nodes.Document;

import commonlib.Globals;
import commonlib.Globals.Domain;
import commonlib.HTMLCompressor;
import commonlib.Helper;
import commonlib.Location;
import commonlib.LocationLinkComparator;
import commonlib.NetworkingFunctions;

import dbconnection.DAOFactory;
import dbconnection.LinkCrawled;
import dbconnection.LinkCrawledDAO;
import dbconnection.LinkCrawledDAOJDBC;
import dbconnection.LocalBusiness;
import dbconnection.LocalBusinessDAO;
import dbconnection.LocalBusinessDAOJDBC;
import dbconnection.LocationLink;
import dbconnection.LocationLinkDAO;
import dbconnection.LocationLinkDAOJDBC;
import dbconnection.RawHTML;
import dbconnection.RawHTMLDAO;
import dbconnection.RawHTMLDAOJDBC;

public class YelpCrawler implements IWebsiteCrawler {
//  private static final int maxTimeCrawlInSec = 10;  // 10 sec
//  private static final int maxTimeCrawlInSec = 60;  // 1 min
//  private static final int maxTimeCrawlInSec = 3600;  // 1 hours
	private static final int maxTimeCrawlInSec = 7200;  // 2 hours
//  private static final int maxTimeCrawlInSec = 14400; // 4 hours
//  private static final int maxTimeCrawlInSec = 21600; // 6 hours
//  private static final int maxTimeCrawlInSec = 43200; // 12 hours

	private Map<String, Location> linkToLocationMap = null;
	private Set<LocationLink> locationLinkLists = null;
	private Set<String> urlsCrawled = null;
	private Queue<LocationLink> urlsQueue = null;
	private final int numRetriesDownloadLink = 2;
	
	private RawHTMLDAO rawHTMLDAO = null;
	private LinkCrawledDAO linkCrawledDAO = null;
	private LocationLinkDAO locationLinkDAO = null;
	private LocalBusinessDAO localBusinessDAO = null;
	
	private long startTimeInSec = 0;

	private final String[] searchTerms = { "marijuana" };

	public YelpCrawler() throws Exception {
		if (!this.readConfig()) {
			throw new Exception("Read config file fails");
		}

		final DAOFactory daoFactory = DAOFactory.getInstance(Globals.username, Globals.password, Globals.server + Globals.database);

		this.rawHTMLDAO = new RawHTMLDAOJDBC(daoFactory);

		this.linkCrawledDAO = new LinkCrawledDAOJDBC(daoFactory);

		this.locationLinkDAO = new LocationLinkDAOJDBC(daoFactory);
		
		this.localBusinessDAO = new LocalBusinessDAOJDBC(daoFactory);

		// Get start urls (location links for now)
		this.linkToLocationMap = new HashMap<String, Location>();
		this.locationLinkLists = new HashSet<LocationLink>();
		
		final List<LocationLink> locationLinks = this.locationLinkDAO.get();
		for (final LocationLink locationLink : locationLinks) {
		    final Integer id = locationLink.getId();
			final String link = locationLink.getLink();
			final String country = locationLink.getCity();
			final String state = locationLink.getState();
			final String city = locationLink.getCity();
			
			final Location location = new Location(id, country, state, city);

			this.linkToLocationMap.put(link, location);
			this.locationLinkLists.add(locationLink);
		}

		final List<LinkCrawled> linksCrawled = this.linkCrawledDAO.get(Domain.CRAIGLIST.value);

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

	private boolean processOneEntryLink(String entryLink, Location loc) throws SQLException, IOException {
		final Document htmlDoc = NetworkingFunctions.downloadHtmlContentToDoc(entryLink, this.numRetriesDownloadLink);
		
		if (htmlDoc == null) {
			Globals.crawlerLogManager.writeLog("Fail to download link " + entryLink);
			return false;
		}
		
		final String htmlContent = HTMLCompressor.compressHtmlContent(htmlDoc.outerHtml());

		final LinkCrawled linkCrawled = new LinkCrawled();
		linkCrawled.setLink(entryLink);
		linkCrawled.setDomainTableId1(Domain.YELP.value);
		linkCrawled.setPriority(1);
		linkCrawled.setTimeCrawled(null);
		linkCrawled.setDateCrawled(null);
		linkCrawled.setLocation(loc);

		int id = -1;

		try {
			id = this.linkCrawledDAO.create(linkCrawled);
		} catch (final SQLException e) {
			Globals.crawlerLogManager.writeLog("Insert content of link " + entryLink + " into RawHTML table fails");
			throw e;
		}

		if (id < 0) {
			Globals.crawlerLogManager.writeLog("Fail to insert link " + entryLink + " into table link_crawled_table");
			return false;
		}

		final Short positivePage = null;
		final Short predict1 = null;
		final Short predict2 = null;
		String currentDate = Helper.getCurrentDate();
		String currentTime = Helper.getCurrentTime();

		final RawHTML rawHTML = new RawHTML();
		rawHTML.setId(id);
		rawHTML.setUrl(entryLink);
		rawHTML.setHtml(htmlContent);
		rawHTML.setPositive(positivePage);
		rawHTML.setPredict1(predict1);
		rawHTML.setPredict2(predict2);
		rawHTML.setDateCrawled(currentDate);
		rawHTML.setTimeCrawled(currentTime);
		
		try {
			int rawHTMLId = this.rawHTMLDAO.create(rawHTML);
			
		    Globals.crawlerLogManager.writeLog("Insert content of link " + entryLink + " into RawHTML table succeeds with id = " + rawHTMLId);
		    
		    LocalBusiness business = new LocalBusiness();
		    business.setState(loc.state);
		    business.setCity(loc.city);
		    business.setAddress(null);
		    business.setPhone_number(null);
		    business.setRating(null);
		    business.setRawhtml_fk(rawHTMLId);
		    business.setLocation_link_fk(loc.id);
		    business.setDatePosted(currentDate);
		    business.setTimePosted(currentTime);
		    business.setDuplicatePostId(null);
		    business.setPosting_body(null);
		    business.setTitle(null);
		    
		    if (!this.localBusinessDAO.create(business)) {
		        Globals.crawlerLogManager.writeLog("Fails to insert location for " + entryLink + " into local_business table");
                return false;
		    }
		} catch (final SQLException e) {
			Globals.crawlerLogManager.writeLog("Insert content of link " + entryLink + " into RawHTML table fails");
			throw e;
		}

		return true;
	}

	private boolean processOneLocationLink(Location location) throws Exception {
		if (location == null) {
			Globals.crawlerLogManager.writeLog("Unexpected: null location");
			return false;
		}

		final IEntryLinkCrawler crawler = new YelpEntryLinkCrawl(location);

		// Process each search term at a time
		for (final String term : this.searchTerms) {
			crawler.setQueryTerm(term);

			if (!crawler.startUp()) {
				Globals.crawlerLogManager.writeLog("Fail to start up from location " + location.toString());
				continue;
			}

			// Iterate through every link in a given location link like
			// http://seattle.craigslist.org
			while (true) {
				// Add new links into the queue table
				final String nextEntryLink = crawler.getNextEntryLink();
				if (nextEntryLink == null) {
					Helper.waitSec(Globals.DEFAULTLOWERBOUNDWAITTIMESEC, Globals.DEFAULTUPPERBOUNDWAITTIMESEC);
					break;
				}

				if (this.urlsCrawled.contains(nextEntryLink)) {
					continue;
				}

				// this.urlsQueue.add(entryLink);
				this.urlsCrawled.add(nextEntryLink);

				// Classify the link and add relevant information into the database
				final boolean processLinkSuccess = this.processOneEntryLink(nextEntryLink, location);
				if (!processLinkSuccess) {
					continue;
				}

				Helper.waitSec(Globals.DEFAULTLOWERBOUNDWAITTIMESEC, Globals.DEFAULTUPPERBOUNDWAITTIMESEC);
				
				// Reach maximum timeslot, stop
				long currentTimeInSec = System.currentTimeMillis()/1000;
				if (currentTimeInSec - this.startTimeInSec > YelpCrawler.maxTimeCrawlInSec) {
				    break;
				}
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
			Globals.crawlerLogManager.writeLog("There is no location links to start from");
			return false;
		}
		
        this.startTimeInSec = System.currentTimeMillis()/1000;
		
		while (true) {
			// If the queue is empty, restock the links again
			if (this.urlsQueue == null || this.urlsQueue.isEmpty()) {
				// If this is the first crawl time, initilize the queue.
				// Otherwise, wait for 10 to 15 minutes before the next crawl.
				if (this.urlsQueue == null) {
					this.urlsQueue = new PriorityQueue<LocationLink>(100, new LocationLinkComparator());
				} else {
					Globals.crawlerLogManager.writeLog("Finish crawling once, wait before recrawl again");
					Helper.waitSec(30 * 60, 60 * 60);
					Globals.crawlerLogManager.writeLog("Finish waiting, restart crawling again");
				}

				for (final LocationLink locationLink : this.locationLinkLists) {
					this.urlsQueue.add(locationLink);
				}
			}

			final String locationUrl = this.urlsQueue.remove().getLink();
			final Location curLocation = this.linkToLocationMap.get(locationUrl);
			
			if (curLocation == null) {
				throw new Exception("Unexpected error: location link " + locationUrl + " is not present in the map");
			}

			final boolean processLocLinkSuccess = this.processOneLocationLink(curLocation);
			
			// Reach maximum timeslot, stop
            long currentTimeInSec = System.currentTimeMillis()/1000;
            if (currentTimeInSec - this.startTimeInSec > YelpCrawler.maxTimeCrawlInSec) {
                Globals.crawlerLogManager.writeLog(
                		"Crawler reaches maximum time allowable. Start time = " + this.startTimeInSec + ". End time = " + currentTimeInSec);
                break;
            }
			
			if (!processLocLinkSuccess) {
				Globals.crawlerLogManager.writeLog("Process location " + curLocation.toString() + " fails");
				continue;
			}
		}
		
        return true;
	}
}
