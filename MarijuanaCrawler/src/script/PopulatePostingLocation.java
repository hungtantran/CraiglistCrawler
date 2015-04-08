package script;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import commonlib.Globals;
import commonlib.Helper;

import dbconnection.DAOFactory;
import dbconnection.DAOUtil;

public class PopulatePostingLocation {
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
        DAOFactory daoFactory = null;
        daoFactory = DAOFactory.getInstance(Globals.username, Globals.password, Globals.server + Globals.database);
        
        Connection connection = daoFactory.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        String sqlQuery = "SELECT id, state, city FROM location_link";
        preparedStatement = DAOUtil.prepareStatement(connection, sqlQuery, false);
        resultSet = preparedStatement.executeQuery();

        List<Integer> locationIds = new ArrayList<Integer>();
        List<String> states = new ArrayList<String>();
        List<String> cities = new ArrayList<String>();
        while (resultSet.next()) {
        	locationIds.add(resultSet.getInt("id"));
        	states.add(resultSet.getString("state"));
        	cities.add(resultSet.getString("city"));
        }
        
        sqlQuery = "SELECT location_fk FROM posting_location WHERE state IS NULL AND location_fk IS NOT NULL AND title IS NULL";
        preparedStatement = DAOUtil.prepareStatement(connection, sqlQuery, false);
        resultSet = preparedStatement.executeQuery();

        List<Integer> ids = new ArrayList<Integer>();
        while (resultSet.next()) {
            ids.add(resultSet.getInt("location_fk"));
        }
        
        for (Integer id : ids) {
            sqlQuery = "SELECT html, state, city, dateCrawled, timeCrawled FROM rawhtml WHERE id = " + id;
            preparedStatement = DAOUtil.prepareStatement(connection, sqlQuery, false);
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                String html = resultSet.getString("html");
                String state = resultSet.getString("state");
                String city = resultSet.getString("city");
                String dateCrawled = resultSet.getString("dateCrawled");
                String timeCrawled = resultSet.getString("timeCrawled");
                
                if (city == null || state == null || html == null || dateCrawled == null || timeCrawled == null) {
                	continue;
                }
                
                Document doc = Jsoup.parse(html);
                
                // Extract posting title
                Elements postingTitles = doc.select("h2[class=postingtitle]");
                if (postingTitles.size() != 1) {
                	System.out.println("Can't parse posting title for id " + id);
                    continue;
                }
                String postingTitle = Helper.cleanNonCharacterDigit(postingTitles.get(0).text());
                System.out.println("Found title '" + postingTitle + "' for id " + id);
                
                // Extract posting body
                Elements postingBodies = doc.select("section[id=postingBody]");
        		if (postingBodies.size() != 1) {
        			System.out.println("Can't parse posting body for id " + id);
        		    continue;
        		}
        		String postingBody = postingBodies.get(0).html();
        		postingBody = postingBody.replace("'", "");
        		System.out.println("Found body length = '" + postingBody.length() + "' for id " + id);
        		
        		boolean foundLocation = false;
        		Integer locationId = -1;
        		for (int i = 0; i < locationIds.size(); ++i) {
        			if (cities.get(i).toLowerCase().equals(city.toLowerCase()) &&
        				states.get(i).toLowerCase().equals(state.toLowerCase())) {
        				locationId = locationIds.get(i);
        				foundLocation = true;
        			}
        		}
        		
        		if (!foundLocation) {
        			System.out.println("Can't find location for id " + id);
        		    continue;
        		} else {
        			System.out.println("Found location for id " + locationId);
        		}
                
                String update = "UPDATE posting_location SET state = '" + state + "', city = '" + city + "', location_link_fk = " + locationId + ", datePosted = '" + dateCrawled + "',"
                		+ " timePosted = '" + timeCrawled + "', posting_Body = '" + postingBody +"', title = '" + postingTitle + "' WHERE location_fk = " + id;
                System.out.println(update);
                preparedStatement = DAOUtil.prepareStatement(connection, update, false);
                preparedStatement.executeUpdate();
            }
        }
    }
}
