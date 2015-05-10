package script;

import java.util.List;

import commonlib.Globals;

import dbconnection.DAOFactory;
import dbconnection.PostingLocation;
import dbconnection.PostingLocationDAO;
import dbconnection.PostingLocationDAOJDBC;
import dbconnection.PostingType;
import dbconnection.PostingTypeDAO;
import dbconnection.PostingTypeDAOJDBC;
import dbconnection.Strain;
import dbconnection.StrainDAO;
import dbconnection.StrainDAOJDBC;

public class TagPostingBody {
	public static void main(String[] args) throws Exception {
		DAOFactory daoFactory = null;
        daoFactory = DAOFactory.getInstance(Globals.username, Globals.password, Globals.server + Globals.database);
        
        final PostingLocationDAO postingLocationDAO = new PostingLocationDAOJDBC(daoFactory);
        final PostingTypeDAO postingTypeDAO = new PostingTypeDAOJDBC(daoFactory);
        final StrainDAO strainDAO = new StrainDAOJDBC(daoFactory);
        
        List<PostingLocation> postingLocations = postingLocationDAO.get();
        List<Strain> strains = strainDAO.get();

        for (PostingLocation postingLocation : postingLocations) {
        	String postingBody = postingLocation.getPosting_body();
        	if (postingBody == null) {
        		continue;
        	}
        	
        	postingBody = postingBody.toLowerCase();

        	for (Strain strain : strains) {
        		String strainName = strain.getName().toLowerCase();
        		
        		if (postingBody.indexOf(strainName) != -1) {
        			PostingType postingType = new PostingType();
        			postingType.setPostingLocationId(postingLocation.getLocation_fk());
        			postingType.setStrainId(strain.getId());
        			
        			postingTypeDAO.create(postingType);
        		}
        	}
        }
	}
}
