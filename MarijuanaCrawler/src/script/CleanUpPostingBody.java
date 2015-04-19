package script;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import commonlib.Globals;
import commonlib.Helper;
import dbconnection.DAOFactory;
import dbconnection.DAOUtil;

public class CleanUpPostingBody {
	private final static String SQL_UPDATE = "UPDATE posting_location SET posting_body = ? WHERE location_fk = ?";
			
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
        DAOFactory daoFactory = null;
        daoFactory = DAOFactory.getInstance(Globals.username, Globals.password, Globals.server + Globals.database);
        
        Connection connection = daoFactory.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        String sqlQuery = "SELECT posting_body, location_fk FROM posting_location WHERE posting_body IS NOT NULL";
        preparedStatement = DAOUtil.prepareStatement(connection, sqlQuery, false);
        resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            int id = resultSet.getInt("location_fk");
            String postingBody = resultSet.getString("posting_body").trim();
            
            String cleanUpPostingBody = Helper.cleanPostingBody(postingBody);
            
            if (cleanUpPostingBody.length() != postingBody.length()) {
    			final Object[] values = { cleanUpPostingBody, id };

    			preparedStatement = DAOUtil.prepareStatement(connection, CleanUpPostingBody.SQL_UPDATE, false, values);
    			System.out.println(id + " " + preparedStatement.toString());
    			
    			preparedStatement.executeUpdate();
            }
        }
    }
}
