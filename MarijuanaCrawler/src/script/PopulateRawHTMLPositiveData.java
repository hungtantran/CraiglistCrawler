package script;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import commonlib.Globals;
import dbconnection.DAOFactory;
import dbconnection.RawHTML;
import dbconnection.RawHTMLDAO;
import dbconnection.RawHTMLDAOJDBC;

public class PopulateRawHTMLPositiveData {
	public static void populateRawHTMLPositiveDataGivenPath(String path, short positive) {
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
				RawHTMLDAO rawHTMLDAOJDBC = new RawHTMLDAOJDBC(DAOFactory.getInstance(
						Globals.username, Globals.password, Globals.server + Globals.database));
				List<RawHTML> htmls = rawHTMLDAOJDBC.get(lowerBound,
						maxNumResult);
				if (htmls == null)
					break;

				int count = 0;
				// Iterate through the result set to populate the information
				for (RawHTML rawHTML : htmls) {
					count++;
					String link = rawHTML.getUrl();

					for (String fileName : fileNamesInPath) {
						if (link.contains(fileName)) {
							rawHTML.setPositive(positive);

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

	public static void main(String[] args) {
		String negativePath = "testingSet" + File.separator + "negative";
		PopulateRawHTMLPositiveData.populateRawHTMLPositiveDataGivenPath(
				negativePath, (short) 0);

		negativePath = "trainingSet" + File.separator + "negative";
		PopulateRawHTMLPositiveData.populateRawHTMLPositiveDataGivenPath(
				negativePath, (short) 0);

		String positivePath = "testingSet" + File.separator + "positive";
		PopulateRawHTMLPositiveData.populateRawHTMLPositiveDataGivenPath(
				positivePath, (short) 1);

		positivePath = "trainingSet" + File.separator + "positive";
		PopulateRawHTMLPositiveData.populateRawHTMLPositiveDataGivenPath(
				positivePath, (short) 1);
	}
}
