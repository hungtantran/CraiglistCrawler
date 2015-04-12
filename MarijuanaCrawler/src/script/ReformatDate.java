package script;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import commonlib.Globals;
import commonlib.Helper;

import dbconnection.DAOFactory;
import dbconnection.DAOUtil;

public class ReformatDate {
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
        DAOFactory daoFactory = null;
        daoFactory = DAOFactory.getInstance(Globals.username, Globals.password, Globals.server + Globals.database);
        
        Connection connection = daoFactory.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        String sqlQuery = "SELECT datePosted, timePosted, location_fk FROM posting_location WHERE datePosted IS NOT NULL AND timePosted IS NOT NULL AND"
        		+ "(CHAR_LENGTH(datePosted) < 10 OR CHAR_LENGTH(timePosted) < 8)";
        preparedStatement = DAOUtil.prepareStatement(connection, sqlQuery, false);
        resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            int id = resultSet.getInt("location_fk");
            String datePosted = resultSet.getString("datePosted");
            String[] dateParts = datePosted.split("-");
            String newDatePosted = Helper.AddZeros(dateParts[0], 4) + "-" + Helper.AddZeros(dateParts[1], 2) + "-" + Helper.AddZeros(dateParts[2], 2);
            
            String timePosted = resultSet.getString("timePosted");
            String[] timeParts = timePosted.split(":");
            String newTimePosted = Helper.AddZeros(timeParts[0], 2) + ":" + Helper.AddZeros(timeParts[1], 2) + ":" + Helper.AddZeros(timeParts[2], 2);
        	
            String update = "UPDATE posting_location SET datePosted = '" + newDatePosted + "'," + " timePosted = '" + newTimePosted + "' WHERE location_fk = " + id;
            System.out.println(update);
            
            preparedStatement = DAOUtil.prepareStatement(connection, update, false);
            preparedStatement.executeUpdate();
        }
    }
}
