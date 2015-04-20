package dbconnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import commonlib.Globals;

public class LocationDBDAOJDBC implements LocationDBDAO {
	private final String SQL_SELECT_ALL = "SELECT * FROM location";
	private final String SQL_INSERT = "INSERT INTO location"
	        + " (country, state, city, latitude, longitude, nelatitude, nelongitude, swlatitude, swlongitude)"
	        + " values (?, ?, ?, ?, ?, ?, ?, ?, ?)";

	private final DAOFactory daoFactory;

	public LocationDBDAOJDBC(DAOFactory daoFactory) throws SQLException {
		this.daoFactory = daoFactory;
	}

	private LocationDB constructLocationDBObject(ResultSet resultSet) throws SQLException {
		final LocationDB loc = new LocationDB();

		loc.setId(resultSet.getInt("id"));
		if (resultSet.wasNull()) {
			loc.setId(null);
		}

		loc.setCountry(resultSet.getString("country"));
		if (resultSet.wasNull()) {
			loc.setCountry(null);
		}

		loc.setState(resultSet.getString("state"));
		if (resultSet.wasNull()) {
			loc.setState(null);
		}

		loc.setCity(resultSet.getString("city"));
		if (resultSet.wasNull()) {
			loc.setCity(null);
		}
        
        loc.setLatitude(resultSet.getString("latitude"));
        if (resultSet.wasNull()) {
        	loc.setLatitude(null);
        }
        
        loc.setLongitude(resultSet.getString("longitude"));
        if (resultSet.wasNull()) {
        	loc.setLongitude(null);
        }
        
        loc.setNelatitude(resultSet.getString("nelatitude"));
        if (resultSet.wasNull()) {
        	loc.setNelatitude(null);
        }
        
        loc.setNelongitude(resultSet.getString("nelongitude"));
        if (resultSet.wasNull()) {
        	loc.setNelongitude(null);
        }
        
        loc.setSwlatitude(resultSet.getString("swlatitude"));
        if (resultSet.wasNull()) {
        	loc.setSwlatitude(null);
        }
        
        loc.setSwlongitude(resultSet.getString("swlongitude"));
        if (resultSet.wasNull()) {
        	loc.setSwlongitude(null);
        }

		return loc;
	}

	@Override
	public List<LocationDB> get() throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			connection = this.daoFactory.getConnection();
			preparedStatement = DAOUtil.prepareStatement(connection, this.SQL_SELECT_ALL, false);
			resultSet = preparedStatement.executeQuery();

			final List<LocationDB> locDBs = new ArrayList<LocationDB>();
			while (resultSet.next()) {
				final LocationDB loc = this.constructLocationDBObject(resultSet);
				locDBs.add(loc);
			}

			return locDBs;
		} catch (final SQLException e) {
			Globals.crawlerLogManager.writeLog("Get location fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());

			return null;
		} finally {
			DAOUtil.close(connection, preparedStatement, resultSet);
		}
	}

	@Override
	public int create(LocationDB loc) throws SQLException {
		if (!loc.isValid()) {
			return -1;
		}

		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			connection = this.daoFactory.getConnection();

			final Object[] values = {
					loc.getCountry(),
					loc.getState(),
					loc.getCity(),
					loc.getLatitude(),
					loc.getLongitude(),
					loc.getNelatitude(),
					loc.getNelongitude(),
					loc.getSwlatitude(),
					loc.getSwlongitude()};

			preparedStatement = DAOUtil.prepareStatement(connection, this.SQL_INSERT, true, values);

			Globals.crawlerLogManager.writeLog(preparedStatement.toString());

			preparedStatement.executeUpdate();

			// Get the generated key (id)
			resultSet = preparedStatement.getGeneratedKeys();
			int generatedKey = -1;

			if (resultSet.next()) {
				generatedKey = resultSet.getInt(1);
			}

			return generatedKey;
		} catch (final SQLException e) {
			Globals.crawlerLogManager.writeLog("Insert into table location fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());

			return -1;
		} finally {
			DAOUtil.close(connection, preparedStatement, resultSet);
		}
	}

}
