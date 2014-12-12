package script;

import java.sql.ResultSet;
import java.util.Map;

import commonlib.Globals;
import dbconnection.MySqlConnection;

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
				mysqlConnection = new MySqlConnection(Globals.username,
						Globals.password, Globals.server, Globals.database);

				ResultSet resultSet = mysqlConnection.GetRawHTML(lowerBound,
						maxNumResult);
				if (resultSet == null)
					break;

				int count = 0;
				// Iterate through the result set to populate the information
				while (resultSet.next()) {
					count++;
					htmlCount++;

					int id = resultSet.getInt("id");
					String link = resultSet.getString("url");
					String htmlContent = resultSet.getString("html");

					Integer positivePage = resultSet.getInt("positive");
					if (resultSet.wasNull())
						positivePage = null;

					Integer predict1 = resultSet.getInt("predict1");
					if (resultSet.wasNull())
						predict1 = null;

					Integer predict2 = resultSet.getInt("predict2");
					if (resultSet.wasNull())
						predict2 = null;

					for (Map.Entry<String, Globals.Location> entry : linkToLocationMap
							.entrySet()) {
						String linkMap = entry.getKey();
						Globals.Location location = entry.getValue();

						if (link.contains(linkMap)) {
							if (Globals.DEBUG)
								System.out.println("(" + htmlCount
										+ ") Check HTML id " + id + ": "
										+ link);

							mysqlConnection.UpdateRawHTML(link, htmlContent,
									positivePage, predict1, predict2,
									location.country, location.state,
									location.city);

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
