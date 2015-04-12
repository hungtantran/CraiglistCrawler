package script;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import commonlib.Globals;

import dbconnection.DAOFactory;
import dbconnection.DAOUtil;

public class MarkDuplicatePostingLocation {
	public static void main(String[] args) throws ClassNotFoundException, SQLException, UnsupportedEncodingException, NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		Map<String, Integer> hashBodyToId = new HashMap<String, Integer>();
		Map<String, Integer> hashTitleToId = new HashMap<String, Integer>();
		
        DAOFactory daoFactory = null;
        daoFactory = DAOFactory.getInstance(Globals.username, Globals.password, Globals.server + Globals.database);
        
        Connection connection = daoFactory.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        // String sqlQuery = "SELECT posting_body, title, location_fk FROM posting_location WHERE posting_body IS NOT NULL and title IS NOT NULL and duplicatePostId IS NULL";
        String sqlQuery = "SELECT title, posting_body, location_fk FROM posting_location WHERE posting_body IS NOT NULL and title IS NOT NULL and duplicatePostId IS NULL";
        preparedStatement = DAOUtil.prepareStatement(connection, sqlQuery, false);
        resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            int id = resultSet.getInt("location_fk");
            String postingBody = resultSet.getString("posting_body").trim();
            String title = resultSet.getString("title").trim();
        	
            // Hash the html content
    		// byte[] bytesOfMessage = postingBody.getBytes("UTF-8");
            byte[] bytesOfTitle = title.getBytes("UTF-8");
    		byte[] titleDigest = md.digest(bytesOfTitle);
    		String titleMsg = new String(titleDigest);
    		
    		byte[] bytesOfBody = postingBody.getBytes("UTF-8");
    		byte[] bodyDigest = md.digest(bytesOfBody);
    		String bodyMsg = new String(bodyDigest);
    		
    		Integer originalIdForTitle = null;
    		Integer originalIdForBody = null;

    		if (hashTitleToId.containsKey(titleMsg)) {
    			originalIdForTitle = hashTitleToId.get(titleMsg);
    			// System.out.println("here1   " + originalIdForTitle);
			} else {
				hashTitleToId.put(titleMsg, id);
			}
    		
    		if (hashBodyToId.containsKey(bodyMsg)) {
    			originalIdForBody = hashBodyToId.get(bodyMsg);
    			// System.out.println("here2   " + originalIdForBody);
			} else {
				hashBodyToId.put(bodyMsg, id);
			}
    		
    		if (originalIdForTitle != null && originalIdForBody != null && originalIdForTitle.equals(originalIdForBody)) {
    			System.out.println("Article " + id + " has the same html with article " + originalIdForTitle);
	
	            String update = "UPDATE posting_location SET duplicatePostId = " + originalIdForTitle + " WHERE location_fk = " + id;
	            System.out.println(update);
	            
	            preparedStatement = DAOUtil.prepareStatement(connection, update, false);
	            preparedStatement.executeUpdate();
    		} else if (originalIdForBody != null) {
    			System.out.println("Posting body " + originalIdForBody + " has body match with " + id + " but title doesn't");
    			String update = "UPDATE posting_location SET duplicatePostId = " + originalIdForBody + " WHERE location_fk = " + id;
	            System.out.println(update);
	            
	            preparedStatement = DAOUtil.prepareStatement(connection, update, false);
	            preparedStatement.executeUpdate();
    		} else if (originalIdForTitle != null) {
    			System.out.println("Posting body " + originalIdForTitle + " has title match with " + id + " but body doesn't");
    			String update = "UPDATE posting_location SET duplicatePostId = " + originalIdForTitle + " WHERE location_fk = " + id;
	            System.out.println(update);
	            
	            preparedStatement = DAOUtil.prepareStatement(connection, update, false);
	            preparedStatement.executeUpdate();
    		}
        }
    }
}
