package dbconnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import commonlib.Globals;

public class RawHTMLDAOJDBC implements RawHTMLDAO {
	private DAOFactory daoFactory;
	private String database = null;

	public RawHTMLDAOJDBC(DAOFactory daoFactory, String database) {
		this.daoFactory = daoFactory;
		this.database = database;
	}

	private RawHTML constructRawHTMLObject(ResultSet resultSet)
			throws SQLException {
		RawHTML rawHTML = new RawHTML();

		rawHTML.setId(resultSet.getInt("id"));
		if (resultSet.wasNull()) rawHTML.setId(null);
		
		rawHTML.setUrl(resultSet.getString("url"));
		if (resultSet.wasNull()) rawHTML.setUrl(null);
		
		rawHTML.setHtml(resultSet.getString("html"));
		if (resultSet.wasNull()) rawHTML.setHtml(null);
		
		rawHTML.setPositive(resultSet.getShort("positive"));
		if (resultSet.wasNull()) rawHTML.setPositive(null);
		
		rawHTML.setPredict1(resultSet.getShort("predict1"));
		if (resultSet.wasNull()) rawHTML.setPredict1(null);
		
		rawHTML.setPredict2(resultSet.getShort("predict2"));
		if (resultSet.wasNull()) rawHTML.setPredict2(null);
		
		rawHTML.setCountry(resultSet.getString("country"));
		if (resultSet.wasNull()) rawHTML.setCountry(null);
		
		rawHTML.setState(resultSet.getString("state"));
		if (resultSet.wasNull()) rawHTML.setState(null);
		
		rawHTML.setCity(resultSet.getString("city"));
		if (resultSet.wasNull()) rawHTML.setCity(null);
		
		rawHTML.setAlt_quantities(resultSet.getString("alt_quantities"));
		if (resultSet.wasNull()) rawHTML.setAlt_quantities(null);
		
		rawHTML.setAlt_prices(resultSet.getString("alt_prices"));
		if (resultSet.wasNull()) rawHTML.setAlt_prices(null);
		
		return rawHTML;
	}

	@Override
	public RawHTML find(int id) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			connection = this.daoFactory.getConnection();
			Statement st = connection.createStatement();
			st.executeQuery("USE " + this.database);

			String query = "SELECT * FROM RawHTML WHERE id = " + id;

			resultSet = st.executeQuery(query);

			RawHTML rawHTML = null;

			if (resultSet.next()) {
				rawHTML = this.constructRawHTMLObject(resultSet);
			}

			return rawHTML;
		} catch (SQLException e) {
			Globals.crawlerLogManager.writeLog("Get RawHTML fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());
		} finally {
			DAOUtil.close(connection, preparedStatement, resultSet);
		}

		return null;
	}

	@Override
	public List<RawHTML> get() throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			connection = this.daoFactory.getConnection();
			Statement st = connection.createStatement();
			st.executeQuery("USE " + this.database);

			String query = "SELECT * FROM RawHTML";

			resultSet = st.executeQuery(query);

			ArrayList<RawHTML> htmls = new ArrayList<RawHTML>();

			while (resultSet.next()) {
				RawHTML rawHTML = this.constructRawHTMLObject(resultSet);

				htmls.add(rawHTML);
			}

			return htmls;
		} catch (SQLException e) {
			Globals.crawlerLogManager.writeLog("Get RawHTML fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());
		} finally {
			DAOUtil.close(connection, preparedStatement, resultSet);
		}

		return null;
	}

	@Override
	public List<RawHTML> get(int lowerBound, int maxNumResult)
			throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			connection = this.daoFactory.getConnection();
			Statement st = connection.createStatement();
			st.executeQuery("USE " + this.database);

			String query = "SELECT * FROM RawHTML";

			if (lowerBound > 0 || maxNumResult > 0)
				query += " LIMIT " + lowerBound + "," + maxNumResult;

			resultSet = st.executeQuery(query);

			ArrayList<RawHTML> htmls = new ArrayList<RawHTML>();

			while (resultSet.next()) {
				RawHTML rawHTML = this.constructRawHTMLObject(resultSet);

				htmls.add(rawHTML);
			}

			return htmls;
		} catch (SQLException e) {
			Globals.crawlerLogManager.writeLog("Get RawHTML fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());
		} finally {
			DAOUtil.close(connection, preparedStatement, resultSet);
		}

		return null;
	}

	@Override
	public boolean create(RawHTML rawHTML) throws SQLException {
		if (rawHTML == null || !rawHTML.isValid()) {
			return false;
		}

		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			connection = this.daoFactory.getConnection();
			Statement st = connection.createStatement();
			st.executeQuery("USE " + this.database);

			// Insert into watch_price_stat_table
			String prepareStmt = "INSERT INTO RawHTML (id, url, html, positive, predict1, predict2, country, state, city) values (?, ?, ?, ?, ?, ?, ?, ?, ?)";

			PreparedStatement stmt = connection.prepareStatement(prepareStmt);
			stmt.setInt(1, rawHTML.getId());
			stmt.setString(2, rawHTML.getUrl());
			stmt.setString(3, rawHTML.getHtml());

			if (rawHTML.getPositive() != null) {
				stmt.setInt(4, rawHTML.getPositive());
			} else {
				stmt.setNull(4, java.sql.Types.INTEGER);
			}

			if (rawHTML.getPredict1() != null) {
				stmt.setInt(5, rawHTML.getPredict1());
			} else {
				stmt.setNull(5, java.sql.Types.INTEGER);
			}

			if (rawHTML.getPredict2() != null) {
				stmt.setInt(6, rawHTML.getPredict2());
			} else {
				stmt.setNull(6, java.sql.Types.INTEGER);
			}

			if (rawHTML.getCountry() != null) {
				stmt.setString(7, rawHTML.getCountry());
			} else {
				stmt.setNull(7, java.sql.Types.VARCHAR);
			}

			if (rawHTML.getState() != null) {
				stmt.setString(8, rawHTML.getState());
			} else {
				stmt.setNull(8, java.sql.Types.VARCHAR);
			}

			if (rawHTML.getCity() != null) {
				stmt.setString(9, rawHTML.getCity());
			} else {
				stmt.setNull(9, java.sql.Types.VARCHAR);
			}

			if (Globals.DEBUG)
				Globals.crawlerLogManager.writeLog("INSERT INTO RawHTML ("
						+ rawHTML.getUrl() + ", Content Length = "
						+ rawHTML.getHtml().length());
			stmt.executeUpdate();
		} catch (SQLException e) {
			Globals.crawlerLogManager
					.writeLog("Insert into table RawHTML fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());
			return false;
		} finally {
			DAOUtil.close(connection, preparedStatement, resultSet);
		}

		return true;
	}

	@Override
	public boolean update(RawHTML rawHTML) throws SQLException {
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet resultSet = null;

		try {
			connection = this.daoFactory.getConnection();
			Statement st = connection.createStatement();
			st.executeQuery("USE " + this.database);

			// Insert into watch_price_stat_table
			String prepareStmt = "UPDATE RawHTML SET html = ?, positive = ?, predict1 = ?, predict2 = ?, country = ?, state = ?, city = ? WHERE url = ?";

			stmt = connection.prepareStatement(prepareStmt);

			stmt.setString(1, rawHTML.getHtml());

			if (rawHTML.getPositive() != null) {
				stmt.setInt(2, rawHTML.getPositive());
			} else {
				stmt.setNull(2, java.sql.Types.INTEGER);
			}

			if (rawHTML.getPredict1() != null) {
				stmt.setInt(3, rawHTML.getPredict1());
			} else {
				stmt.setNull(3, java.sql.Types.INTEGER);
			}

			if (rawHTML.getPredict2() != null) {
				stmt.setInt(4, rawHTML.getPredict2());
			} else {
				stmt.setNull(4, java.sql.Types.INTEGER);
			}

			if (rawHTML.getCountry() != null) {
				stmt.setString(5, rawHTML.getCountry());
			} else {
				stmt.setNull(5, java.sql.Types.VARCHAR);
			}

			if (rawHTML.getState() != null) {
				stmt.setString(6, rawHTML.getState());
			} else {
				stmt.setNull(6, java.sql.Types.VARCHAR);
			}

			if (rawHTML.getCity() != null) {
				stmt.setString(7, rawHTML.getCity());
			} else {
				stmt.setNull(7, java.sql.Types.VARCHAR);
			}

			stmt.setString(8, rawHTML.getUrl());

			if (Globals.DEBUG)
				Globals.crawlerLogManager.writeLog("Update RawHTML ("
						+ rawHTML.getUrl() + ", Content Length = "
						+ rawHTML.getHtml().length());
			stmt.executeUpdate();
		} catch (SQLException e) {
			Globals.crawlerLogManager.writeLog("Update RawHTML fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());
		} finally {
			DAOUtil.close(connection, stmt, resultSet);
		}

		return false;
	}

}
