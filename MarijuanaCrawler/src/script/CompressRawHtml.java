package script;

import java.util.List;

import commonlib.Globals;
import commonlib.HTMLCompressor;

import dbconnection.DAOFactory;
import dbconnection.RawHTML;
import dbconnection.RawHTMLDAOJDBC;

public class CompressRawHtml {
    public static void main(String[] args) {
        int lowerBound = 0;
        final int maxNumResult = 200;

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
                    final String htmlContent = rawHTML.getHtml();
                    String compressHtml = HTMLCompressor.compressHtmlContent(htmlContent);
                    rawHTML.setHtml(compressHtml);
                    
                    if (compressHtml.length() < htmlContent.length()) {
	                    System.out.println(rawHTML.getId() + " Compress html length from " + htmlContent.length() + " donw to " + compressHtml.length());
	                    
	                    rawHTMLDAOJDBC.update(rawHTML);
                    }
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
    }
}
