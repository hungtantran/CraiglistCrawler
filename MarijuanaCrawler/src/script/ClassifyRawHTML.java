package script;

import java.sql.ResultSet;

import marijuanaCrawler.Classifier;

import commonlib.Globals;

import dbconnection.MySqlConnection;

public class ClassifyRawHTML {
	public static void main(String[] args) {
		int lowerBound = 0;
		int maxNumResult = 200;
		int htmlCount = lowerBound;
		int useHeuristic = 0;

		// Get 2000 articles at a time, until exhaust all the articles
		while (true) {
			try {
				MySqlConnection mysqlConnection = new MySqlConnection(
						Globals.username, Globals.password, Globals.server,
						Globals.database);

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

					Integer predict2 = resultSet.getInt("predict2");
					if (resultSet.wasNull())
						predict2 = null;

					String country = resultSet.getString("country");
					String state = resultSet.getString("state");
					String city = resultSet.getString("city");

					if (Globals.DEBUG)
						System.out.println("(" + htmlCount + ") Check HTML id "
								+ id + ": " + link);

					// Classify the page
					int predict1 = Classifier.classify(htmlContent);

					mysqlConnection.UpdateRawHTML(link, htmlContent,
							positivePage, predict1, predict2, country, state,
							city);
				}

				if (count == 0)
					break;
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}

			lowerBound += maxNumResult;
		}

		System.out.println("Use heuristic for " + useHeuristic + " times");
	}
}
