package script;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import marijuanaCrawler.Classifier;
import commonlib.Globals;
import commonlib.Helper;
import dbconnection.DAOFactory;
import dbconnection.PostingLocation;
import dbconnection.PostingLocationDAO;
import dbconnection.PostingLocationDAOJDBC;
import dbconnection.RawHTML;
import dbconnection.RawHTMLDAOJDBC;

public class ClassifyRawHTML {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        int lowerBound = 0;
        final int maxNumResult = 200;
        int htmlCount = lowerBound;
        final int useHeuristic = 0;

        DAOFactory daoFactory = DAOFactory.getInstance(Globals.username, Globals.password, Globals.server + Globals.database);
        final PostingLocationDAO postingLocationDAO = new PostingLocationDAOJDBC(daoFactory);
        List<PostingLocation> postingLocations = postingLocationDAO.get();

        Set<Integer> postingLocationIds = new HashSet<Integer>();
        for (PostingLocation postingLocation : postingLocations) {
            postingLocationIds.add(postingLocation.getLocation_fk());
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
                    
                    if (postingLocationIds.contains(rawHTML.getId()) || rawHTML.getHtml().indexOf("craigslist.org") == -1) {
                        System.out.println("Id " + rawHTML.getId() + " already exists");
                        continue;
                    }

                    final int id = rawHTML.getId();
                    final String htmlContent = rawHTML.getHtml();

                    if (Globals.DEBUG) {
                        System.out.println("(" + htmlCount + ") Check HTML id " + id);
                    }

                    // Classify the page
                    final Short predict1 = Classifier.classify(htmlContent);
                    
                    if (predict1 == 1) {
                        System.out.println("Found a new one!!! Predict = " + predict1 + " for id = " + id + "\n\n\n\n");
                        rawHTML.setPredict1(predict1);
                        rawHTMLDAOJDBC.update(rawHTML);
                        
                        PostingLocation location = new PostingLocation();
                        location.setLocation_fk(rawHTML.getId());
                        location.setDatePosted(rawHTML.getDateCrawled());
                        location.setTimePosted(rawHTML.getTimeCrawled());
                        
                        String pastDate = Helper.getPastDate(-14);
                        if (rawHTML.getDateCrawled() == null || rawHTML.getDateCrawled().compareTo(pastDate) < 0) {
                            location.setActive(0);
                        } else {
                            location.setActive(1);
                        }
                        location.setEmail(null);
                        
                        if (!postingLocationDAO.create(location)) {
                            System.out.println("Fails to insert id " + rawHTML.getId() + " into posting_location table");
                            continue;
                        }
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

        System.out.println("Use heuristic for " + useHeuristic + " times");
    }
}
