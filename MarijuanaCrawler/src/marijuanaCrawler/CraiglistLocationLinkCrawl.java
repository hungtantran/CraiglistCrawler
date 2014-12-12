package marijuanaCrawler;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import commonlib.Globals;
import commonlib.NetworkingFunctions;
import dbconnection.MySqlConnection;

public class CraiglistLocationLinkCrawl {
	private final int numRetryDownloadPage = 2;
	private final String startLink = "http://www.craigslist.org/about/sites";

	private Map<String, Globals.Location> linkToLocationMap = null;

	public CraiglistLocationLinkCrawl() {
		this.linkToLocationMap = new HashMap<String, Globals.Location>();
	}

	public Map<String, Globals.Location> getLinkToLocationMap() {
		return this.linkToLocationMap;
	}

	public boolean getLink() {
		Document doc = NetworkingFunctions.downloadHtmlContentToDoc(this.startLink,
				this.numRetryDownloadPage);

		if (doc == null)
			return false;

		Elements countryElems = doc.select("div[class=colmask]");

		// Return if the page has no country div
		if (countryElems.size() == 0) {
			Globals.crawlerLogManager.writeLog("No country element");
			return false;
		}

		// US elem is the first one
		Element usCountryElem = countryElems.get(0);

		Elements stateNameElems = usCountryElem.select("h4");
		Elements stateCitiesElems = usCountryElem.select("ul");

		Globals.crawlerLogManager.writeLog("Found " + stateNameElems.size() + " state names and "
				+ stateCitiesElems.size() + " state elems");

		if (stateNameElems.size() != stateCitiesElems.size())
			return false;

		if (stateNameElems.size() == 0)
			return false;

		int numStates = stateNameElems.size();

		// Iterate through each state listed in craiglist
		for (int i = 0; i < numStates; i++) {
			String stateName = stateNameElems.get(i).text().trim();
			Globals.crawlerLogManager.writeLog("State "+stateName + ":");

			Elements citiesElems = stateCitiesElems.get(i).select("li");
			int numCitiesInStates = citiesElems.size();
			
			Globals.crawlerLogManager.writeLog("Has "+numCitiesInStates+" cities/locations");
			// Iterate through each city of the current state
			for (int j = 0; j < numCitiesInStates; j++) {
				// TODO use city name when we have location based index
				String cityName = citiesElems.get(j).text().trim();
				String link = citiesElems.get(j).select("a").attr("href")
						.toString();
				Globals.crawlerLogManager.writeLog(cityName + ": " + link);
				
				Globals.Location loc =  new Globals.Location();
				loc.country = "US";
				loc.state = stateName;
				loc.city = cityName;
				
				this.linkToLocationMap.put(link, loc);
			}
		}

		return true;
	}

	public static void main(String[] args) {
		CraiglistLocationLinkCrawl crawler = new CraiglistLocationLinkCrawl();

		// Fail to get the craiglist location links
		if (!crawler.getLink())
			return;

		Map<String, Globals.Location> linkToLocationMap = crawler
				.getLinkToLocationMap();

		if (linkToLocationMap == null)
			return;
		
		MySqlConnection mysqlConnection;
		try {
			mysqlConnection = new MySqlConnection(
					Globals.username, Globals.password, Globals.server,
					Globals.database);
		} catch (ClassNotFoundException e) {
			Globals.crawlerLogManager.writeLog(e.getMessage());
			return;
		}
		
		for (Map.Entry<String, Globals.Location> entry : linkToLocationMap.entrySet()) {
			String link = entry.getKey();
			Globals.Location location = entry.getValue();
			
			if (!mysqlConnection.insertIntoLocationLink(link, location.country, location.state, location.city))
				System.out.println("Fail to insert");
		}
	}
}
