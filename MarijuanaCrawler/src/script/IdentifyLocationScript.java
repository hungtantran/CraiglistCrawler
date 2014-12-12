package script;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import commonlib.Globals;
import dbconnection.DAOFactory;
import dbconnection.MySqlConnection;
import dbconnection.RawHTML;
import dbconnection.RawHTMLDAOJDBC;

public class IdentifyLocationScript {
	public static void identifyLocationRawHTML() {
		MySqlConnection mysqlConnection = null;
		try {
			mysqlConnection = new MySqlConnection(Globals.username,
					Globals.password, Globals.server, Globals.database);
		} catch (Exception e) {
			Globals.crawlerLogManager.writeLog(e.getMessage());
			return;
		}

		Map<String, Globals.Location> linkToLocationMap = mysqlConnection
				.GetLocationLink();

		int lowerBound = 0;
		int maxNumResult = 200;
		int htmlCount = lowerBound;

		// Get 2000 articles at a time, until exhaust all the articles
		while (true) {
			try {
				RawHTMLDAOJDBC rawHTMLDAOJDBC = new RawHTMLDAOJDBC(DAOFactory.getInstance(
						Globals.username, Globals.password, Globals.server),
						Globals.database);
				List<RawHTML> htmls = rawHTMLDAOJDBC.get(lowerBound,
						maxNumResult);
				if (htmls == null)
					break;

				int count = 0;
				// Iterate through the result set to populate the information
				for (RawHTML rawHTML : htmls) {
					count++;
					htmlCount++;

					int id = rawHTML.getId();
					String link = rawHTML.getUrl();

					for (Map.Entry<String, Globals.Location> entry : linkToLocationMap
							.entrySet()) {
						String linkMap = entry.getKey();
						Globals.Location location = entry.getValue();

						if (link.contains(linkMap)) {
							if (Globals.DEBUG)
								System.out.println("(" + htmlCount
										+ ") Check HTML id " + id + ": "
										+ link);
							
							rawHTML.setCountry(location.country);
							rawHTML.setState(location.state);
							rawHTML.setCity(location.city);
							
							rawHTMLDAOJDBC.update(rawHTML);

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
			throws ClassNotFoundException {
		MySqlConnection mysqlConnection = new MySqlConnection(Globals.username,
				Globals.password, Globals.server, Globals.database);

		Map<String, Globals.Location> linkToLocationMap = mysqlConnection
				.GetLocationLink();

		ResultSet resultSet = mysqlConnection.getLinkCrawled(1);
		if (resultSet == null)
			return;

		try {
			// Iterate through the result set to populate the information
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				String link = resultSet.getString("link");
				int priority = resultSet.getInt("priority");

				for (Map.Entry<String, Globals.Location> entry : linkToLocationMap
						.entrySet()) {
					String linkMap = entry.getKey();
					Globals.Location location = entry.getValue();

					if (link.contains(linkMap)) {
						if (Globals.DEBUG)
							System.out.println("Check id " + id + ": " + link);

						mysqlConnection
								.UpdateLinkCrawl(link, priority,
										location.country, location.state,
										location.city);

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
		} catch (ClassNotFoundException e) {
			return;
		}
		
		IdentifyLocationScript.identifyLocationRawHTML();
	}
}
