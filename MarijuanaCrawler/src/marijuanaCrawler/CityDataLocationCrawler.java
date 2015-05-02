package marijuanaCrawler;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import commonlib.Globals;
import commonlib.NetworkingFunctions;

import dbconnection.DAOFactory;
import dbconnection.LocationDB;
import dbconnection.LocationDBDAO;
import dbconnection.LocationDBDAOJDBC;

public class CityDataLocationCrawler {
	private final int numRetryDownloadPage = 2;
	private final String startLink = "http://www.city-data.com/";

	private Set<LocationDB> locationSet = null;
	private LocationDBDAO locationDBDAO = null;

	public CityDataLocationCrawler() throws ClassNotFoundException, SQLException {
		this.locationSet = new HashSet<LocationDB>();
		this.locationDBDAO = new LocationDBDAOJDBC(DAOFactory.getInstance(Globals.username, Globals.password, Globals.server + Globals.database));
	}

	public Set<LocationDB> getLocationSet() {
		return this.locationSet;
	}
	
	public Set<String> getCityFromLink(String link) throws Exception {
		Document doc = NetworkingFunctions.downloadHtmlContentToDoc(link, this.numRetryDownloadPage);
		
		Set<String> cities = new HashSet<String>();

		Elements bodyElems = doc.select("table[class=tabBlue tblsort tblsticky]");
		if (bodyElems.size() != 1) {
			return cities;
		}
		
		Elements cityElems = bodyElems.get(0).select("a");
		for (int i = 0; i < cityElems.size(); ++i) {
			String cityName = cityElems.get(i).text();
			
			int index = cityName.indexOf(',');
			
			if (index != -1) {
				cityName = cityName.substring(0, index);
			}
			
			cityName = cityName.trim();
			
			cities.add(cityName);
		}
		
		return cities;
	}
	
	public boolean getLink() throws Exception {
		Document doc = NetworkingFunctions.downloadHtmlContentToDoc(this.startLink, this.numRetryDownloadPage);

		if (doc == null) {
			return false;
		}

		Elements countryElems = doc.select("div[id=home1]");

		// Return if the page has no country div
		if (countryElems.size() == 0) {
			Globals.crawlerLogManager.writeLog("No country element");
			return false;
		}

		// US elem is the first one
		Element usCountryElem = countryElems.get(0);

		Elements stateElems = usCountryElem.select("ul[class=tab-list tab-list-long]");

		Globals.crawlerLogManager.writeLog("Found " + stateElems.size() + " state names and " + stateElems.size() + " state elems");

		int numStateElems = stateElems.size();

		// Iterate through each state listed in craiglist
		for (int i = 0; i < numStateElems; ++i) {
			Elements stateNameElems = stateElems.get(i).select("li");
			
			for (int j = 0; j < stateNameElems.size(); ++j) {
				Element stateElem = stateNameElems.get(j).select("a").get(0);
				
				String stateName = stateElem.text();
				String stateLink = stateElem.attr("href");
				
				Globals.crawlerLogManager.writeLog("State "+ stateName + ": " + stateLink);
				
				Set<String> cities = this.getCityFromLink(stateLink);
				for (String city : cities) {
					Globals.crawlerLogManager.writeLog(city);
					
					LocationDB locDB = new LocationDB();
					locDB.setCountry("US");
					locDB.setState(stateName);
					locDB.setCity(city);
					
					this.locationSet.add(locDB);
					
					this.locationDBDAO.create(locDB);
				}
			}
		}

		return true;
	}

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		CityDataLocationCrawler crawler = new CityDataLocationCrawler();

		// Fail to get the craiglist location links
		try {
            if (!crawler.getLink()) {
            	return;
            }
        } catch (Exception e) {
            Globals.crawlerLogManager.writeLog("Throw Exception " + e.getMessage());
            return;
        }
	}
}
