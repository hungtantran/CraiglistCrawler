package script;

import java.util.List;

import commonlib.Globals;
import commonlib.HTMLCompressor;
import dbconnection.DAOFactory;
import dbconnection.RawHTML;
import dbconnection.RawHTMLDAO;
import dbconnection.RawHTMLDAOJDBC;

public class CompressRawHtml {
    public static void main(String[] args) throws ClassNotFoundException {
        int lowerBound = 0;
        final int maxNumResult = 200;

        DAOFactory daoFactory = null;
        daoFactory = DAOFactory.getInstance(Globals.username, Globals.password, Globals.server + Globals.database);

        if (daoFactory == null) {
            return;
        }

        while (true) {
            try {
                final RawHTMLDAO rawHTMLDAO = new RawHTMLDAOJDBC(daoFactory);
                final List<RawHTML> htmls = rawHTMLDAO.get(lowerBound, maxNumResult);

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
	                    
	                    rawHTMLDAO.update(rawHTML);
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
