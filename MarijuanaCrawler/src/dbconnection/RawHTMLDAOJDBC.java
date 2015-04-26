package dbconnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import commonlib.Globals;

public class RawHTMLDAOJDBC implements RawHTMLDAO {
	private final String SQL_SELECT_BY_ID = "SELECT * FROM rawhtml WHERE id = ?";
	private final String SQL_SELECT_ALL = "SELECT * FROM rawhtml";
	private final String SQL_SELECT_WITH_LIMIT = "SELECT * FROM rawhtml LIMIT ?, ?";
	private final String SQL_INSERT = "INSERT INTO rawhtml"
	        + "(id, html, positive, predict1, predict2, dateCrawled, timeCrawled)"
	        + " values (?, ?, ?, ?, ?, ?, ?)";
	private final String SQL_UPDATE = "UPDATE rawhtml SET "
	        + "html = ?, positive = ?, predict1 = ?, predict2 = ?"
	        + " WHERE id = ?";

	private final DAOFactory daoFactory;

	public RawHTMLDAOJDBC(DAOFactory daoFactory) throws SQLException {
		this.daoFactory = daoFactory;
	}

	private RawHTML constructRawHTMLObject(ResultSet resultSet)
			throws SQLException {
		final RawHTML rawHTML = new RawHTML();

		rawHTML.setId(resultSet.getInt("id"));
		if (resultSet.wasNull()) {
			rawHTML.setId(null);
		}

		rawHTML.setHtml(resultSet.getString("html"));
		if (resultSet.wasNull()) {
			rawHTML.setHtml(null);
		}

		rawHTML.setPositive(resultSet.getShort("positive"));
		if (resultSet.wasNull()) {
			rawHTML.setPositive(null);
		}

		rawHTML.setPredict1(resultSet.getShort("predict1"));
		if (resultSet.wasNull()) {
			rawHTML.setPredict1(null);
		}

		rawHTML.setPredict2(resultSet.getShort("predict2"));
		if (resultSet.wasNull()) {
			rawHTML.setPredict2(null);
		}
        
        rawHTML.setDateCrawled(resultSet.getString("dateCrawled"));
        if (resultSet.wasNull()) {
            rawHTML.setDateCrawled(null);
        }
        
        rawHTML.setTimeCrawled(resultSet.getString("timeCrawled"));
        if (resultSet.wasNull()) {
            rawHTML.setTimeCrawled(null);
        }


		return rawHTML;
	}

	@Override
	public RawHTML get(int id) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			connection = this.daoFactory.getConnection();
			preparedStatement = DAOUtil.prepareStatement(connection,
					this.SQL_SELECT_BY_ID, false, id);
			resultSet = preparedStatement.executeQuery();

			RawHTML rawHTML = null;
			if (resultSet.next()) {
				rawHTML = this.constructRawHTMLObject(resultSet);
			}

			return rawHTML;
		} catch (final SQLException e) {
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
			preparedStatement = DAOUtil.prepareStatement(connection,
					this.SQL_SELECT_ALL, false);
			resultSet = preparedStatement.executeQuery();

			final ArrayList<RawHTML> htmls = new ArrayList<RawHTML>();
			while (resultSet.next()) {
				final RawHTML rawHTML = this.constructRawHTMLObject(resultSet);
				htmls.add(rawHTML);
			}

			return htmls;
		} catch (final SQLException e) {
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

			if (lowerBound > 0 || maxNumResult > 0) {
				preparedStatement = DAOUtil.prepareStatement(connection,
						this.SQL_SELECT_WITH_LIMIT, false, lowerBound,
						maxNumResult);
			} else {
				preparedStatement = DAOUtil.prepareStatement(connection,
						this.SQL_SELECT_ALL, false);
			}

			resultSet = preparedStatement.executeQuery();

			final ArrayList<RawHTML> htmls = new ArrayList<RawHTML>();
			while (resultSet.next()) {
				final RawHTML rawHTML = this.constructRawHTMLObject(resultSet);
				htmls.add(rawHTML);
			}

			return htmls;
		} catch (final SQLException e) {
			Globals.crawlerLogManager.writeLog("Get RawHTML fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());
		} finally {
			DAOUtil.close(connection, preparedStatement, resultSet);
		}

		return null;
	}

	@Override
	public int create(RawHTML rawHTML) throws SQLException {
		if (rawHTML == null || !rawHTML.isValid()) {
			return -1;
		}

		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			connection = this.daoFactory.getConnection();

			final Object[] values = {
		        rawHTML.getId(),
				rawHTML.getHtml(), rawHTML.getPositive(),
				rawHTML.getPredict1(), rawHTML.getPredict2(),
				rawHTML.getDateCrawled(), rawHTML.getTimeCrawled()};

			preparedStatement = DAOUtil.prepareStatement(connection,
					this.SQL_INSERT, true, values);

			if (Globals.DEBUG) {
				Globals.crawlerLogManager.writeLog("INSERT INTO RawHTML "
					+ rawHTML.getId() + ", Content Length = "
					+ rawHTML.getHtml().length());
			}

			preparedStatement.executeUpdate();

			return rawHTML.getId();
		} catch (final SQLException e) {
			Globals.crawlerLogManager.writeLog("Insert into table RawHTML fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());
			return -1;
		} finally {
			DAOUtil.close(connection, preparedStatement, resultSet);
		}
	}

	@Override
	public boolean update(RawHTML rawHTML) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		final ResultSet resultSet = null;

		try {
			connection = this.daoFactory.getConnection();
			
			final Object[] values = { rawHTML.getHtml(),
					rawHTML.getPositive(), rawHTML.getPredict1(),
					rawHTML.getPredict2(), rawHTML.getId()};

			preparedStatement = DAOUtil.prepareStatement(connection,
					this.SQL_UPDATE, false, values);

			if (Globals.DEBUG) {
				Globals.crawlerLogManager.writeLog("Update RawHTML ("
					+ rawHTML.getId() + ", Content Length = "
					+ rawHTML.getHtml().length());
			}

			preparedStatement.executeUpdate();

			return true;
		} catch (final SQLException e) {
			Globals.crawlerLogManager.writeLog("Update RawHTML fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());

			return false;
		} finally {
			DAOUtil.close(connection, preparedStatement, resultSet);
		}
	}

}
