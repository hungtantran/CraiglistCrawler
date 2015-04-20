package script;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import commonlib.Globals;

import dbconnection.DAOFactory;
import dbconnection.DAOUtil;

public class PopulateLocationFkForLocationLink {
	public final static String SQL_UPDATE = "UPDATE location_link "
	        + "SET locationFk1 = ?, locationFk2 = ?, locationFk3 = ? WHERE id = ?";
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
        DAOFactory daoFactory = null;
        daoFactory = DAOFactory.getInstance(Globals.username, Globals.password, Globals.server + Globals.database);
        
        Connection connection = daoFactory.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        String sqlQuery = "SELECT id, state, city FROM location";
        preparedStatement = DAOUtil.prepareStatement(connection, sqlQuery, false);
        resultSet = preparedStatement.executeQuery();

        List<Integer> locationIds = new ArrayList<Integer>();
        List<String> states = new ArrayList<String>();
        List<String> cities = new ArrayList<String>();
        while (resultSet.next()) {
        	locationIds.add(resultSet.getInt("id"));
        	states.add(resultSet.getString("state").toLowerCase());
        	cities.add(resultSet.getString("city").toLowerCase());
        }
        
        sqlQuery = "SELECT id, state, city FROM location_link WHERE locationFk1 IS NULL";
        preparedStatement = DAOUtil.prepareStatement(connection, sqlQuery, false);
        resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
        	Integer[] locationFk = new Integer[3];
        	locationFk[0] = null;
        	locationFk[1] = null;
        	locationFk[2] = null;
        	int found = 0;

        	int id = resultSet.getInt("id");
            String state = resultSet.getString("state").toLowerCase();
            String city = resultSet.getString("city").toLowerCase();
            
            for (int i = 0; i < states.size(); ++i) {
            	if (state.equals(states.get(i))) {
            		if (city.contains(cities.get(i))) {
            			System.out.println(state + " " + city + " " + cities.get(i));
            			locationFk[found] = locationIds.get(i);
            			++found;
            			if (found >= 3) {
            				break;
            			}
            		}
            	}
            }

			final Object[] values = {
				locationFk[0],
	        	locationFk[1],
	        	locationFk[2],
	        	id};

			preparedStatement = DAOUtil.prepareStatement(connection, PopulateLocationFkForLocationLink.SQL_UPDATE, true, values);

			Globals.crawlerLogManager.writeLog(preparedStatement.toString());

			preparedStatement.executeUpdate();
        }
    }
}
