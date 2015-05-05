package script;

import java.util.List;

import parser.CraiglistParser;
import parser.IPostingLocationParser;

import commonlib.Globals;

import dbconnection.DAOFactory;
import dbconnection.PostingLocation;
import dbconnection.PostingLocationDAO;
import dbconnection.PostingLocationDAOJDBC;
import dbconnection.RawHTML;
import dbconnection.RawHTMLDAO;
import dbconnection.RawHTMLDAOJDBC;

public class PopulateEmailPostingLocation {
	public static void main(String[] args) throws Exception {
		DAOFactory daoFactory = null;
	    daoFactory = DAOFactory.getInstance(Globals.username, Globals.password, Globals.server + Globals.database);
	    
	    if (daoFactory == null) {
	        return;
	    }
	    
	    final String match = "craigslist.org";
	    
	    final PostingLocationDAO postingLocationDAO = new PostingLocationDAOJDBC(daoFactory);
	    final RawHTMLDAO rawHTMLDAO = new RawHTMLDAOJDBC(DAOFactory.getInstance(Globals.username, Globals.password, Globals.server + Globals.database));
	    
		IPostingLocationParser craiglistParser = new CraiglistParser();

	    List<PostingLocation> postingLocations = postingLocationDAO.getActive(1);
	    
	    for (PostingLocation postingLocation : postingLocations) {
	    	// Skip already parsed entry
	    	if (postingLocation.getEmail() != null) {
	    		continue;
	    	}
	
	    	int id = postingLocation.getLocation_fk();
	    	String link = postingLocation.getUrl();
	    	int index = link.indexOf(match);
	    	
	    	if (index == -1) {
	    		continue;
	    	}
	    	
	    	String domain = link.substring(0, index + match.length());
	    	
	    	RawHTML rawHTML = rawHTMLDAO.get(id);
	    	
	    	String html = rawHTML.getHtml();
	    	
	    	try {
	    		craiglistParser.SetHTML(domain, html);
	    	} catch (Exception e) {
	    		System.out.println(e.getMessage());
	    		continue;
	    	}
	    	
	    	try {
		    	String email = craiglistParser.ParseEmail();
		    	postingLocation.setEmail(email);
		    	
		    	// If email is "Expired", the post is already disappear 404
		    	if (email != null && email.equals("Expired")) {
		    		postingLocation.setActive(0);
		    	}
		    	
		    	postingLocationDAO.update(postingLocation);
	    	} catch (Exception e) {
	    		Globals.crawlerLogManager.writeLog(e.getMessage());
	    		return;
	    	}
	    }
	}
}
