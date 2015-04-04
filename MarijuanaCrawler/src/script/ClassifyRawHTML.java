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
        final int maxNumResult = 200;
        int htmlCount = lowerBound;
        final int useHeuristic = 0;

        DAOFactory daoFactory = null;
        try {
            daoFactory = DAOFactory.getInstance(Globals.username, Globals.password, Globals.server + Globals.database);
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        if (daoFactory == null) {
            return;
        }

        while (true) {
            try {
                final RawHTMLDAOJDBC rawHTMLDAOJDBC = new RawHTMLDAOJDBC(daoFactory);
                final List<RawHTML> htmls = rawHTMLDAOJDBC.get(lowerBound, maxNumResult);

                if (htmls == null) {
                    break;
                }

                int count = 0;
                // Iterate through the result set to populate the information
                for (final RawHTML rawHTML : htmls) {
                    count++;
                    htmlCount++;

                    final int id = rawHTML.getId();
                    final String link = rawHTML.getUrl();
                    final String htmlContent = rawHTML.getHtml();

                    if (Globals.DEBUG) {
                        System.out.println("(" + htmlCount + ") Check HTML id " + id + ": " + link);
                    }

                    // Classify the page
                    final Short predict1 = Classifier.classify(htmlContent);

                    rawHTML.setPredict1(predict1);
                    rawHTMLDAOJDBC.update(rawHTML);
                }

                if (count == 0) {
                    break;
                }
            } catch (final Exception e) {
                e.printStackTrace();
                break;
            }

            lowerBound += maxNumResult;
        }

        System.out.println("Use heuristic for " + useHeuristic + " times");
    }
}
