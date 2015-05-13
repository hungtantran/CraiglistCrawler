package script;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import commonlib.Globals;
import dbconnection.DAOFactory;
import dbconnection.DAOUtil;
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
        
        Connection connection = daoFactory.getConnection();
        String sqlQuery = "SELECT * FROM posting_types";
        PreparedStatement preparedStatement = DAOUtil.prepareStatement(connection, sqlQuery, false);
        ResultSet resultSet = preparedStatement.executeQuery();
        
        Map<Integer, Set<Integer>> postingIdToStrainId = new HashMap<Integer, Set<Integer>>();
        while (resultSet.next()) {
            int id = resultSet.getInt("posting_location_id");
            int strainId = resultSet.getInt("strain_id");

            if (postingIdToStrainId.containsKey(id)) {
                Set<Integer> strainIds = postingIdToStrainId.get(id);
                strainIds.add(strainId);
            } else {
                Set<Integer> strainIds = new HashSet<Integer>();
                strainIds.add(strainId);
                postingIdToStrainId.put(id, strainIds);
            }
        }

        for (Map.Entry<Integer, Set<Integer>> entry : postingIdToStrainId.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue());
        }
        
        List<PostingLocation> postingLocations = postingLocationDAO.get();
        List<Strain> strains = strainDAO.get();

        for (PostingLocation postingLocation : postingLocations) {
            System.out.println("Process posting id " + postingLocation.getLocation_fk());
        	String postingBody = postingLocation.getPosting_body();
        	if (postingBody == null) {
        		continue;
        	}
        	
        	postingBody = postingBody.toLowerCase();

        	for (Strain strain : strains) {
        		String strainName = strain.getName().toLowerCase();
        		String[] strainNameParts = strainName.split(" ");
        		
        		boolean found = false;
        		if (postingBody.indexOf(strainName) != -1) {
        		    found = true;
        		} else {
        		    found = true;
        		    for (String strainNamePart : strainNameParts) {
        		        if (postingBody.indexOf(strainNamePart) == -1) {
        		            found = false;
        		            break;
        		        }
        		    }
        		}
        		
        		if (found) {
        		    Integer postingId = postingLocation.getLocation_fk();
        		    System.out.println("Find strain " + strain.getId() + " from posting id " + postingId);
        		    
        		    if (!postingIdToStrainId.containsKey(postingId) || !postingIdToStrainId.get(postingId).contains(strain.getId())) {
                		PostingType postingType = new PostingType();
                        postingType.setPostingLocationId(postingId);
                        postingType.setStrainId(strain.getId());
                        
                        postingTypeDAO.create(postingType);
        		    } else {
        		        System.out.println("Already exists");
        		    }
        		}
        	}
        }
	}
}
