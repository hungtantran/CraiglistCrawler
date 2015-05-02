package dbconnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import commonlib.Globals;
import commonlib.Helper;

public class PostingLocationDAOJDBC implements PostingLocationDAO {
    private final String SQL_SELECT_ALL = "SELECT * FROM posting_location";
    private final String SQL_SELECT_BY_ID = "SELECT * FROM posting_location WHERE location_fk = ?";
    private final String SQL_SELECT_BY_ACTIVE = "SELECT * FROM posting_location WHERE active = ? ORDER BY datePosted DESC, timePosted DESC";
    private final String SQL_INSERT = "INSERT INTO posting_location"
            + "(state, city, latitude, longitude, location_fk, location_link_fk, datePosted, timePosted, posting_body, title, url, active, email)"
            + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private final String SQL_ACTIVATE = "UPDATE posting_location SET active = 1 WHERE url = ?";
    private final String SQL_DEACTIVATE = "UPDATE posting_location SET active = 0 WHERE url LIKE ? AND datePosted < ?";
    private final String SQL_UPDATE = "UPDATE posting_location SET email = ?, active = ? WHERE location_fk = ?";

    private final DAOFactory daoFactory;

    public PostingLocationDAOJDBC(DAOFactory daoFactory) throws SQLException {
        this.daoFactory = daoFactory;
    }

    private PostingLocation constructPostingLocationObject(ResultSet resultSet) throws SQLException {
        final PostingLocation location = new PostingLocation();
        
        location.setState(resultSet.getString("state"));
        if (resultSet.wasNull()) {
            location.setState(null);
        }
        
        location.setCity(resultSet.getString("city"));
        if (resultSet.wasNull()) {
            location.setCity(null);
        }
        
        location.setLatitude(resultSet.getString("latitude"));
        if (resultSet.wasNull()) {
            location.setLatitude(null);
        }
        
        location.setLongitude(resultSet.getString("longitude"));
        if (resultSet.wasNull()) {
            location.setLongitude(null);
        }
        
        location.setLocation_fk(resultSet.getInt("location_fk"));
        if (resultSet.wasNull()) {
            location.setLocation_fk(null);
        }
        
        location.setLocation_link_fk(resultSet.getInt("location_link_fk"));
        if (resultSet.wasNull()) {
            location.setLocation_link_fk(null);
        }
        
        location.setDatePosted(resultSet.getString("datePosted"));
        if (resultSet.wasNull()) {
            location.setDatePosted(null);
        }
        
        location.setTimePosted(resultSet.getString("timePosted"));
        if (resultSet.wasNull()) {
            location.setTimePosted(null);
        }
        
        location.setPosting_body(resultSet.getString("posting_body"));
        if (resultSet.wasNull()) {
            location.setPosting_body(null);
        }
        
        location.setTitle(resultSet.getString("title"));
        if (resultSet.wasNull()) {
            location.setTitle(null);
        }

        location.setAlt_quantities(resultSet.getString("alt_quantities"));
		if (resultSet.wasNull()) {
			location.setAlt_quantities(null);
		}

		location.setAlt_prices(resultSet.getString("alt_prices"));
		if (resultSet.wasNull()) {
			location.setAlt_prices(null);
		}
		
		location.setUrl(resultSet.getString("url"));
		if (resultSet.wasNull()) {
			location.setUrl(null);
		}
		
		location.setActive(resultSet.getInt("active"));
		if (resultSet.wasNull()) {
			location.setActive(null);
		}
		
		location.setEmail(resultSet.getString("email"));
		if (resultSet.wasNull()) {
			location.setEmail(null);
		}
        
        return location;
    }
    
    @Override
    public List<PostingLocation> get() throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.daoFactory.getConnection();
            preparedStatement = DAOUtil.prepareStatement(connection, this.SQL_SELECT_ALL, false);
            resultSet = preparedStatement.executeQuery();

            final List<PostingLocation> locations = new ArrayList<PostingLocation>();
            while (resultSet.next()) {
                final PostingLocation location = this.constructPostingLocationObject(resultSet);
                locations.add(location);
            }

            return locations;
        } catch (final SQLException e) {
            Globals.crawlerLogManager.writeLog("Get posting_location fails");
            Globals.crawlerLogManager.writeLog(e.getMessage());

            return null;
        } finally {
            DAOUtil.close(connection, preparedStatement, resultSet);
        }
    }

    @Override
    public PostingLocation get(int locationId) throws Exception {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.daoFactory.getConnection();
            
            final Object[] values = { locationId };
            
            preparedStatement = DAOUtil.prepareStatement(connection, this.SQL_SELECT_BY_ID, false, values);
            resultSet = preparedStatement.executeQuery();

            PostingLocation locations = null;
            if (resultSet.next()) {
                locations = new PostingLocation();
                locations = this.constructPostingLocationObject(resultSet);
            }
            
            if (resultSet.next()) {
                Globals.crawlerLogManager.writeLog("There are two locations with the same id " + locationId);
                throw new Exception("There are two locations with the same id");
            }

            return locations;
        } catch (final SQLException e) {
            Globals.crawlerLogManager.writeLog("Get posting_location fails");
            Globals.crawlerLogManager.writeLog(e.getMessage());

            return null;
        } finally {
            DAOUtil.close(connection, preparedStatement, resultSet);
        }
    }

	@Override
	public List<PostingLocation> getActive(int active) throws Exception {
		Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.daoFactory.getConnection();
            
            final Object[] values = { active };
            
            preparedStatement = DAOUtil.prepareStatement(connection, this.SQL_SELECT_BY_ACTIVE, false, values);
            resultSet = preparedStatement.executeQuery();

            final List<PostingLocation> locations = new ArrayList<PostingLocation>();
            while (resultSet.next()) {
                final PostingLocation location = this.constructPostingLocationObject(resultSet);
                locations.add(location);
            }

            return locations;
        } catch (final SQLException e) {
            Globals.crawlerLogManager.writeLog("Get posting_location where active = " + active +" fails");
            Globals.crawlerLogManager.writeLog(e.getMessage());

            return null;
        } finally {
            DAOUtil.close(connection, preparedStatement, resultSet);
        }
	}

    @Override
    public boolean create(PostingLocation location) throws SQLException {
        if (!location.isValid()) {
            return false;
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.daoFactory.getConnection();

            final Object[] values = {
                    location.getState(),
                    location.getCity(),
                    location.getLatitude(),
                    location.getLongitude(),
                    location.getLocation_fk(),
                    location.getLocation_link_fk(),
                    location.getDatePosted(),
                    location.getTimePosted(),
                    location.getPosting_body(),
                    location.getTitle(),
                    location.getUrl(),
                    location.getActive(),
                    location.getEmail()};

            preparedStatement = DAOUtil.prepareStatement(connection, this.SQL_INSERT, false, values);

            Globals.crawlerLogManager.writeLog(preparedStatement.toString());

            preparedStatement.executeUpdate();

            return true;
        } catch (final SQLException e) {
            Globals.crawlerLogManager.writeLog("Insert into table posting_location fails");
            Globals.crawlerLogManager.writeLog(e.getMessage());

            return false;
        } finally {
            DAOUtil.close(connection, preparedStatement, resultSet);
        }
    }

	@Override
	public boolean activate(String url) throws SQLException {
		if (url == null) {
            return false;
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.daoFactory.getConnection();

            final Object[] values = { url };

            preparedStatement = DAOUtil.prepareStatement(connection, this.SQL_ACTIVATE, false, values);

            Globals.crawlerLogManager.writeLog(preparedStatement.toString());

            preparedStatement.executeUpdate();

            return true;
        } catch (final SQLException e) {
            Globals.crawlerLogManager.writeLog("Fails to activate link " + url + " in posting_location");
            Globals.crawlerLogManager.writeLog(e.getMessage());

            return false;
        } finally {
            DAOUtil.close(connection, preparedStatement, resultSet);
        }
	}

	@Override
	public boolean deActivate(String locationUrl, int maxLinkAge) throws SQLException {
		if (locationUrl == null) {
            return false;
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.daoFactory.getConnection();
            
            String matchLocationLinkString = locationUrl + "%";
            String pastDate = Helper.getPastDate(maxLinkAge);
            final Object[] values = { matchLocationLinkString, pastDate };

            preparedStatement = DAOUtil.prepareStatement(connection, this.SQL_DEACTIVATE, false, values);

            Globals.crawlerLogManager.writeLog(preparedStatement.toString());

            preparedStatement.executeUpdate();

            return true;
        } catch (final SQLException e) {
            Globals.crawlerLogManager.writeLog("Fails to deactivate link " + locationUrl + " in posting_location");
            Globals.crawlerLogManager.writeLog(e.getMessage());

            return false;
        } finally {
            DAOUtil.close(connection, preparedStatement, resultSet);
        }
	}

	@Override
	public boolean update(PostingLocation postingLocation) throws SQLException {
		if (postingLocation == null) {
            return false;
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.daoFactory.getConnection();

            final Object[] values = { postingLocation.getEmail(), postingLocation.getActive(), postingLocation.getLocation_fk() };

            preparedStatement = DAOUtil.prepareStatement(connection, this.SQL_UPDATE, false, values);

            Globals.crawlerLogManager.writeLog(preparedStatement.toString());

            preparedStatement.executeUpdate();

            return true;
        } catch (final SQLException e) {
            Globals.crawlerLogManager.writeLog("Fails to update email " + postingLocation.getEmail() + " in posting_location for id " + postingLocation.getLocation_fk());
            Globals.crawlerLogManager.writeLog(e.getMessage());

            return false;
        } finally {
            DAOUtil.close(connection, preparedStatement, resultSet);
        }
	}
}
