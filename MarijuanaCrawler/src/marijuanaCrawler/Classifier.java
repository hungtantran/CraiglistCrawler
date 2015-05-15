package marijuanaCrawler;

import commonlib.Globals;
import commonlib.Helper;

public class Classifier {
	public Classifier() {
	}

	public static Short classify(String htmlContent) {
		String htmlBody = Helper.getPostingBodyFromHtmlContent(htmlContent);

		if (htmlBody == null) {
			// TODO return error
			return 0;
		}

		htmlBody = Helper.pruneWords(htmlBody, Globals.stopWords);

		final int numImportantWordsOccur = Helper.numWordsOccur(htmlBody,Globals.importantWords);

		short predict1 = 1;

		if (numImportantWordsOccur < 3) {
			// TODO don't use Global classifier
			predict1 = Globals.classifier.ClassifyContent(htmlContent);
			System.out.println("Classify as " + predict1);
		}

		return predict1;
	}
}
