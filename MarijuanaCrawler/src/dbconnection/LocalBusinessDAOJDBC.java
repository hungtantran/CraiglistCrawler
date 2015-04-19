package dbconnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import commonlib.Globals;

public class LocalBusinessDAOJDBC implements LocalBusinessDAO {
    private final String SQL_SELECT_ALL = "SELECT * FROM local_business";
    private final String SQL_SELECT_BY_ID = "SELECT * FROM local_business WHERE rawhtml_fk = ?";
    private final String SQL_INSERT = "INSERT INTO local_business"
            + "(state, city, address, phone_number, rating, latitude, longitude, rawhtml_fk, location_link_fk, datePosted, timePosted, posting_body, title, duplicatePostId)"
            + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final DAOFactory daoFactory;

    public LocalBusinessDAOJDBC(DAOFactory daoFactory) throws SQLException {
        this.daoFactory = daoFactory;
    }

    private LocalBusiness constructLocalBusinessObject(ResultSet resultSet) throws SQLException {
        final LocalBusiness business = new LocalBusiness();
        
        business.setState(resultSet.getString("state"));
        if (resultSet.wasNull()) {
            business.setState(null);
        }
        
        business.setCity(resultSet.getString("city"));
        if (resultSet.wasNull()) {
            business.setCity(null);
        }
        
        business.setAddress(resultSet.getString("address"));
        if (resultSet.wasNull()) {
            business.setAddress(null);
        }
        
        business.setPhone_number(resultSet.getString("phone_number"));
        if (resultSet.wasNull()) {
            business.setPhone_number(null);
        }
        
        business.setRating(resultSet.getInt("rating"));
        if (resultSet.wasNull()) {
            business.setRating(null);
        }
        
        business.setLatitude(resultSet.getString("latitude"));
        if (resultSet.wasNull()) {
            business.setLatitude(null);
        }
        
        business.setLongitude(resultSet.getString("longitude"));
        if (resultSet.wasNull()) {
            business.setLongitude(null);
        }
        
        business.setRawhtml_fk(resultSet.getInt("rawhtml_fk"));
        if (resultSet.wasNull()) {
            business.setRawhtml_fk(null);
        }
        
        business.setLocation_link_fk(resultSet.getInt("location_link_fk"));
        if (resultSet.wasNull()) {
            business.setLocation_link_fk(null);
        }
        
        business.setDatePosted(resultSet.getString("datePosted"));
        if (resultSet.wasNull()) {
            business.setDatePosted(null);
        }
        
        business.setTimePosted(resultSet.getString("timePosted"));
        if (resultSet.wasNull()) {
            business.setTimePosted(null);
        }
        
        business.setPosting_body(resultSet.getString("posting_body"));
        if (resultSet.wasNull()) {
            business.setPosting_body(null);
        }
        
        business.setTitle(resultSet.getString("title"));
        if (resultSet.wasNull()) {
            business.setTitle(null);
        }
        
        business.setDuplicatePostId(resultSet.getInt("duplicatePostId"));
        if (resultSet.wasNull()) {
            business.setDuplicatePostId(null);
        }
        
        return business;
    }
    
    @Override
    public List<LocalBusiness> get() throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.daoFactory.getConnection();
            preparedStatement = DAOUtil.prepareStatement(connection, this.SQL_SELECT_ALL, false);
            resultSet = preparedStatement.executeQuery();

            final List<LocalBusiness> businesses = new ArrayList<LocalBusiness>();
            while (resultSet.next()) {
                final LocalBusiness business = this.constructLocalBusinessObject(resultSet);
                businesses.add(business);
            }

            return businesses;
        } catch (final SQLException e) {
            Globals.crawlerLogManager.writeLog("Get local_business fails");
            Globals.crawlerLogManager.writeLog(e.getMessage());

            return null;
        } finally {
            DAOUtil.close(connection, preparedStatement, resultSet);
        }
    }

    @Override
    public LocalBusiness get(int locationId) throws Exception {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.daoFactory.getConnection();
            
            final Object[] values = { locationId };
            
            preparedStatement = DAOUtil.prepareStatement(connection, this.SQL_SELECT_BY_ID, false, values);
            resultSet = preparedStatement.executeQuery();

            LocalBusiness business = null;
            if (resultSet.next()) {
            	business = new LocalBusiness();
            	business = this.constructLocalBusinessObject(resultSet);
            }
            
            if (resultSet.next()) {
                Globals.crawlerLogManager.writeLog("There are two local businesses with the same id " + locationId);
                throw new Exception("There are two local businesses with the same id");
            }

            return business;
        } catch (final SQLException e) {
            Globals.crawlerLogManager.writeLog("Get local_business fails");
            Globals.crawlerLogManager.writeLog(e.getMessage());

            return null;
        } finally {
            DAOUtil.close(connection, preparedStatement, resultSet);
        }
    }

    @Override
    public boolean create(LocalBusiness business) throws SQLException {
        if (!business.isValid()) {
            return false;
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.daoFactory.getConnection();

            final Object[] values = {
            		business.getState(),
            		business.getCity(),
            		business.getAddress(),
            		business.getPhone_number(),
            		business.getRating(),
            		business.getLatitude(),
            		business.getLongitude(),
            		business.getRawhtml_fk(),
            		business.getLocation_link_fk(),
            		business.getDatePosted(),
            		business.getTimePosted(),
                    business.getPosting_body(),
                    business.getTitle(),
                    business.getDuplicatePostId()};

            preparedStatement = DAOUtil.prepareStatement(connection, this.SQL_INSERT, false, values);

            Globals.crawlerLogManager.writeLog(preparedStatement.toString());

            preparedStatement.executeUpdate();

            return true;
        } catch (final SQLException e) {
            Globals.crawlerLogManager.writeLog("Insert into table local_business fails");
            Globals.crawlerLogManager.writeLog(e.getMessage());

            return false;
        } finally {
            DAOUtil.close(connection, preparedStatement, resultSet);
        }
    }
}
