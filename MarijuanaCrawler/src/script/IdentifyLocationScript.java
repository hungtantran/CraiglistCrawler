package script;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import commonlib.Globals;
import commonlib.Location;
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

public class IdentifyLocationScript {
	public static void identifyLocationRawHTML() throws ClassNotFoundException,
			SQLException {
		LocationLinkDAO locationLinkDAO = new LocationLinkDAOJDBC(
				DAOFactory.getInstance(Globals.username, Globals.password,
						Globals.server + Globals.database));

		Map<String, Location> linkToLocationMap = new HashMap<String, Location>();

		List<LocationLink> locationLinks = locationLinkDAO.get();
		for (LocationLink locationLink : locationLinks) {
			String link = locationLink.getLink();
			String country = locationLink.getCity();
			String state = locationLink.getState();
			String city = locationLink.getCity();
			Location location = new Location(country, state, city);

			linkToLocationMap.put(link, location);
		}

		int lowerBound = 0;
		int maxNumResult = 200;
		int htmlCount = lowerBound;

		RawHTMLDAO rawHTMLDAO = new RawHTMLDAOJDBC(DAOFactory.getInstance(
				Globals.username, Globals.password, Globals.server
						+ Globals.database));

		// Get 2000 articles at a time, until exhaust all the articles
		while (true) {
			try {
				List<RawHTML> htmls = rawHTMLDAO.get(lowerBound, maxNumResult);
				if (htmls == null)
					break;

				int count = 0;
				// Iterate through the result set to populate the information
				for (RawHTML rawHTML : htmls) {
					count++;
					htmlCount++;

					int id = rawHTML.getId();
					String link = rawHTML.getUrl();

					for (Map.Entry<String, Location> entry : linkToLocationMap.entrySet()) {
						String linkMap = entry.getKey();
						Location location = entry.getValue();

						if (link.contains(linkMap)) {
							if (Globals.DEBUG)
								System.out
										.println("(" + htmlCount
												+ ") Check HTML id " + id
												+ ": " + link);

							rawHTML.setCountry(location.country);
							rawHTML.setState(location.state);
							rawHTML.setCity(location.city);

							rawHTMLDAO.update(rawHTML);

							break;
						}
					}
				}

				if (count == 0)
					break;
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}

			lowerBound += maxNumResult;
		}
	}

	public static void identifyLocationLinkCrawl()
			throws ClassNotFoundException, SQLException {
		LocationLinkDAO locationLinkDAO = new LocationLinkDAOJDBC(
				DAOFactory.getInstance(Globals.username, Globals.password,
						Globals.server + Globals.database));

		Map<String, Location> linkToLocationMap = new HashMap<String, Location>();

		List<LocationLink> locationLinks = locationLinkDAO.get();
		for (LocationLink locationLink : locationLinks) {
			String link = locationLink.getLink();
			String country = locationLink.getCity();
			String state = locationLink.getState();
			String city = locationLink.getCity();
			Location location = new Location(country, state,
					city);

			linkToLocationMap.put(link, location);
		}

		LinkCrawledDAO linkCrawledDAO = new LinkCrawledDAOJDBC(
				DAOFactory.getInstance(Globals.username, Globals.password,
						Globals.server + Globals.database));

		List<LinkCrawled> linksCrawled = linkCrawledDAO
				.get(Globals.Domain.CRAIGLIST.value);

		try {
			// Iterate through the result set to populate the information
			for (LinkCrawled linkCrawled : linksCrawled) {
				Integer id = linkCrawled.getId();
				String link = linkCrawled.getLink();
				for (Map.Entry<String, Location> entry : linkToLocationMap.entrySet()) {
					String linkMap = entry.getKey();
					Location location = entry.getValue();

					if (link.contains(linkMap)) {
						if (Globals.DEBUG)
							System.out.println("Check id " + id + ": " + link);

						linkCrawled.setCountry(location.country);
						linkCrawled.setState(location.state);
						linkCrawled.setCity(location.city);

						linkCrawledDAO.update(linkCrawled);

						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			IdentifyLocationScript.identifyLocationLinkCrawl();

			IdentifyLocationScript.identifyLocationRawHTML();
		} catch (Exception e) {
			return;
		}
	}
}
