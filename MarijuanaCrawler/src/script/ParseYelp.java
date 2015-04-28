package script;

import java.sql.SQLException;
import java.util.List;

import parser.YelpParser;

import commonlib.Globals;

import dbconnection.DAOFactory;
import dbconnection.LocalBusiness;
import dbconnection.LocalBusinessDAO;
import dbconnection.LocalBusinessDAOJDBC;
import dbconnection.RawHTML;
import dbconnection.RawHTMLDAO;
import dbconnection.RawHTMLDAOJDBC;

public class ParseYelp {
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
        DAOFactory daoFactory = null;
        daoFactory = DAOFactory.getInstance(Globals.username, Globals.password, Globals.server + Globals.database);

        if (daoFactory == null) {
            return;
        }
        
        final RawHTMLDAO rawHTMLDAO = new RawHTMLDAOJDBC(daoFactory);
        final LocalBusinessDAO localBusinessDAO = new LocalBusinessDAOJDBC(daoFactory);

    	YelpParser yelpParser = new YelpParser();
        
        List<LocalBusiness> localBusinesses = localBusinessDAO.get();
        
        for (LocalBusiness localBusiness : localBusinesses) {
        	// Skip already parsed entry
        	if (localBusiness.getRating() != null) {
        		continue;
        	}

        	int id = localBusiness.getRawhtml_fk();

        	RawHTML rawHTML = rawHTMLDAO.get(id);
        	
        	String html = rawHTML.getHtml();
        	
        	try {
        		yelpParser.SetHTML(html);
        	} catch (Exception e) {
        		System.out.println(e.getMessage());
        		continue;
        	}
        	
        	LocalBusiness newLocalBusiness = yelpParser.Parse();

        	localBusiness.setAddress(newLocalBusiness.getAddress());
        	localBusiness.setPhone_number(newLocalBusiness.getPhone_number());
        	localBusiness.setLatitude(newLocalBusiness.getLatitude());
        	localBusiness.setLongitude(newLocalBusiness.getLongitude());
        	localBusiness.setPosting_body(newLocalBusiness.getPosting_body());
        	localBusiness.setTitle(newLocalBusiness.getTitle());
        	localBusiness.setRating(newLocalBusiness.getRating());
        	
        	localBusinessDAO.updateLocalBusiness(localBusiness);
        }
    }
}
