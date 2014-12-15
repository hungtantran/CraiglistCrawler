package script;

import java.util.List;

import marijuanaCrawler.Classifier;

import commonlib.Globals;

import dbconnection.DAOFactory;
import dbconnection.RawHTML;
import dbconnection.RawHTMLDAOJDBC;

public class ClassifyRawHTML {
	public static void main(String[] args) {
		int lowerBound = 0;
		int maxNumResult = 200;
		int htmlCount = lowerBound;
		int useHeuristic = 0;

		// Get 2000 articles at a time, until exhaust all the articles
		while (true) {
			try {
				RawHTMLDAOJDBC rawHTMLDAOJDBC = new RawHTMLDAOJDBC(DAOFactory.getInstance(
						Globals.username, Globals.password, Globals.server + Globals.database));
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
					String htmlContent = rawHTML.getHtml();

					if (Globals.DEBUG)
						System.out.println("(" + htmlCount + ") Check HTML id "
								+ id + ": " + link);

					// Classify the page
					Short predict1 = Classifier.classify(htmlContent);
					
					rawHTML.setPredict1(predict1);
					rawHTMLDAOJDBC.update(rawHTML);
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
