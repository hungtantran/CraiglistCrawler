package script;

import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import commonlib.Globals;
import dbconnection.MySqlConnection;

public class PopulateRawHTMLPositiveData {
	public static void populateRawHTMLPositiveDataGivenPath(String path,
			int positive) {
		if (path == null)
			return;

		File folder = new File(path);

		if (!folder.exists())
			return;

		ArrayList<String> fileNamesInPath = new ArrayList<String>();
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isFile()) {
				fileNamesInPath.add(fileEntry.getName());
			}
		}

		int lowerBound = 0;
		int maxNumResult = 200;

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
					String link = resultSet.getString("url");
					String htmlContent = resultSet.getString("html");

					Integer predict1 = resultSet.getInt("predict1");
					if (resultSet.wasNull())
						predict1 = null;

					Integer predict2 = resultSet.getInt("predict2");
					if (resultSet.wasNull())
						predict2 = null;

					String country = resultSet.getString("country");
					String state = resultSet.getString("state");
					String city = resultSet.getString("city");

					for (String fileName : fileNamesInPath) {
						if (link.contains(fileName)) {
							Integer positivePage = positive;

							mysqlConnection.UpdateRawHTML(link, htmlContent,
									positivePage, predict1, predict2, country,
									state, city);

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

	public static void main(String[] args) {
		String negativePath = "testingSet" + File.separator + "negative";
		PopulateRawHTMLPositiveData.populateRawHTMLPositiveDataGivenPath(
				negativePath, 0);

		negativePath = "trainingSet" + File.separator + "negative";
		PopulateRawHTMLPositiveData.populateRawHTMLPositiveDataGivenPath(
				negativePath, 0);

		String positivePath = "testingSet" + File.separator + "positive";
		PopulateRawHTMLPositiveData.populateRawHTMLPositiveDataGivenPath(
				positivePath, 1);

		positivePath = "trainingSet" + File.separator + "positive";
		PopulateRawHTMLPositiveData.populateRawHTMLPositiveDataGivenPath(
				positivePath, 1);
	}
}
