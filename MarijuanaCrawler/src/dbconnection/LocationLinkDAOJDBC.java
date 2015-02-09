package dbconnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import commonlib.Globals;

public class LocationLinkDAOJDBC implements LocationLinkDAO {
	private final String SQL_SELECT_ALL = "SELECT * FROM location_link";
	private final String SQL_INSERT = "INSERT INTO location_link (link, country, state, city) values (?, ?, ?, ?)";

	private final DAOFactory daoFactory;

	public LocationLinkDAOJDBC(DAOFactory daoFactory) throws SQLException {
		this.daoFactory = daoFactory;
	}

	private LocationLink constructLocationLinkObject(ResultSet resultSet) throws SQLException {
		final LocationLink locationLink = new LocationLink();

		locationLink.setId(resultSet.getInt("id"));
		if (resultSet.wasNull()) {
			locationLink.setId(null);
		}

		locationLink.setLink(resultSet.getString("link"));
		if (resultSet.wasNull()) {
			locationLink.setLink(null);
		}

		locationLink.setCountry(resultSet.getString("country"));
		if (resultSet.wasNull()) {
			locationLink.setCountry(null);
		}

		locationLink.setState(resultSet.getString("state"));
		if (resultSet.wasNull()) {
			locationLink.setState(null);
		}

		locationLink.setCity(resultSet.getString("city"));
		if (resultSet.wasNull()) {
			locationLink.setCity(null);
		}

		return locationLink;
	}

	@Override
	public List<LocationLink> get() throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			connection = this.daoFactory.getConnection();
			preparedStatement = DAOUtil.prepareStatement(connection,
					this.SQL_SELECT_ALL, false);
			resultSet = preparedStatement.executeQuery();

			final List<LocationLink> locationLinks = new ArrayList<LocationLink>();
			while (resultSet.next()) {
				final LocationLink locationLink = this
						.constructLocationLinkObject(resultSet);
				locationLinks.add(locationLink);
			}

			return locationLinks;
		} catch (final SQLException e) {
			Globals.crawlerLogManager.writeLog("Get location_link fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());

			return null;
		} finally {
			DAOUtil.close(connection, preparedStatement, resultSet);
		}
	}

	@Override
	public int create(LocationLink locationLink) throws SQLException {
		if (!locationLink.isValid()) {
			return -1;
		}

		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			connection = this.daoFactory.getConnection();

			final Object[] values = { locationLink.getLink(),
					locationLink.getCountry(), locationLink.getState(),
					locationLink.getCity() };

			preparedStatement = DAOUtil.prepareStatement(connection,
					this.SQL_INSERT, true, values);

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
			Globals.crawlerLogManager
					.writeLog("Insert into table location_link fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());

			return -1;
		} finally {
			DAOUtil.close(connection, preparedStatement, resultSet);
		}
	}

}
