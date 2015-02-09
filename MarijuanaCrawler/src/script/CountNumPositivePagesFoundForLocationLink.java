package script;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import commonlib.Globals;

import dbconnection.DAOFactory;
import dbconnection.DAOUtil;
import dbconnection.LocationLink;
import dbconnection.LocationLinkDAO;
import dbconnection.LocationLinkDAOJDBC;

public class CountNumPositivePagesFoundForLocationLink {
    public static final String SQL_SELECT_SUM = "SELECT COUNT(*) AS SUM FROM rawhtml WHERE (predict1 = 1 OR positive = 1) AND url LIKE ?";
    public static final String SQL_UPDATE_LOCATION_LINK = "UPDATE location_link SET num_positive_pages_found = ? WHERE id = ?";
            
    public static void main(String[] args) {
        DAOFactory daoFactory = null;
        try {
            daoFactory = DAOFactory.getInstance(Globals.username, Globals.password, Globals.server + Globals.database);
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        if (daoFactory == null) {
            return;
        }
        
        try {
            LocationLinkDAO locationLinkDAO = new LocationLinkDAOJDBC(daoFactory);
            List<LocationLink> locationLinks = locationLinkDAO.get();
            
            for (LocationLink locationLink : locationLinks) {
                Connection connection = null;
                PreparedStatement preparedStatement = null;

                connection = daoFactory.getConnection();
                
                Object[] values = { locationLink.getLink() + "%" };
                
                preparedStatement = DAOUtil.prepareStatement(connection,
                        CountNumPositivePagesFoundForLocationLink.SQL_SELECT_SUM, false, values);
                System.out.println(preparedStatement.toString());
                ResultSet resultSet = preparedStatement.executeQuery();
                
                if (resultSet.next()) {
                    Object[] updateValues = {
                        resultSet.getInt("SUM"),
                        locationLink.getId()
                    };
                    preparedStatement = DAOUtil.prepareStatement(
                        connection,
                        CountNumPositivePagesFoundForLocationLink.SQL_UPDATE_LOCATION_LINK,
                        false,
                        updateValues);
                    System.out.println(preparedStatement.toString());
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
