package script;

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

public class IdentifyLocationScript {
	public static void identifyLocationLinkCrawl() throws Exception {
		LocationLinkDAO locationLinkDAO = new LocationLinkDAOJDBC(DAOFactory.getInstance(
		        Globals.username, Globals.password, Globals.server + Globals.database));

		Map<String, Location> linkToLocationMap = new HashMap<String, Location>();

		List<LocationLink> locationLinks = locationLinkDAO.get();
		for (LocationLink locationLink : locationLinks) {
		    Integer id = locationLink.getId();
			String link = locationLink.getLink();
			String country = locationLink.getCity();
			String state = locationLink.getState();
			String city = locationLink.getCity();
			Location location = new Location(id, country, state, city);

			linkToLocationMap.put(link, location);
		}

		LinkCrawledDAO linkCrawledDAO = new LinkCrawledDAOJDBC(
				DAOFactory.getInstance(Globals.username, Globals.password, Globals.server + Globals.database));

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
		} catch (Exception e) {
			return;
		}
	}
}
