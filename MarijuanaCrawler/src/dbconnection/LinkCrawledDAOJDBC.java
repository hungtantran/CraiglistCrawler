package dbconnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import commonlib.Globals;
import commonlib.Helper;

public class LinkCrawledDAOJDBC implements LinkCrawledDAO {
	private final String SQL_SELECT_BY_DOMAINID = "SELECT * FROM link_crawled_table WHERE domain_table_id_1 = ?";
	private final String SQL_INSERT = "INSERT INTO link_crawled_table (link, domain_table_id_1, priority, time_crawled, date_crawled, country, state, city) values (?, ?, ?, ?, ?, ?, ?, ?)";
	private final String SQL_UPDATE = "UPDATE link_crawled_table SET link = ?, priority = ?, country = ?, state = ?, city = ? WHERE id = ?";
	
	private DAOFactory daoFactory;

	public LinkCrawledDAOJDBC(DAOFactory daoFactory) throws SQLException {
		this.daoFactory = daoFactory;
	}

	private LinkCrawled constructLinkCrawledObject(ResultSet resultSet)
			throws SQLException {
		LinkCrawled linkCrawled = new LinkCrawled();
		
		linkCrawled.setId(resultSet.getInt("id"));
		if (resultSet.wasNull()) linkCrawled.setId(null);
		
		linkCrawled.setLink(resultSet.getString("link"));
		if (resultSet.wasNull()) linkCrawled.setLink(null);
		
		linkCrawled.setPriority(resultSet.getInt("priority"));
		if (resultSet.wasNull()) linkCrawled.setPriority(null);
		
		linkCrawled.setDomainTableId1(resultSet.getInt("domain_table_id_1"));
		if (resultSet.wasNull()) linkCrawled.setDomainTableId1(null);
		
		linkCrawled.setTimeCrawled(resultSet.getString("time_crawled"));
		if (resultSet.wasNull()) linkCrawled.setTimeCrawled(null);
		
		linkCrawled.setDateCrawled(resultSet.getString("date_crawled"));
		if (resultSet.wasNull()) linkCrawled.setDateCrawled(null);
		
		linkCrawled.setCountry(resultSet.getString("country"));
		if (resultSet.wasNull()) linkCrawled.setCountry(null);
		
		linkCrawled.setState(resultSet.getString("state"));
		if (resultSet.wasNull()) linkCrawled.setState(null);
		
		linkCrawled.setCity(resultSet.getString("city"));
		if (resultSet.wasNull()) linkCrawled.setCity(null);
		
		return linkCrawled;
	}

	@Override
	public List<LinkCrawled> get(int domainId) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			connection = this.daoFactory.getConnection();
			preparedStatement = DAOUtil.prepareStatement(connection, SQL_SELECT_BY_DOMAINID, false, domainId);
			resultSet = preparedStatement.executeQuery();
			
			List<LinkCrawled> linksCrawled = new ArrayList<LinkCrawled>();
			while (resultSet.next()) {
				LinkCrawled linkCrawled = this.constructLinkCrawledObject(resultSet);
				linksCrawled.add(linkCrawled);
			}

			return linksCrawled;
		} catch (SQLException e) {
			Globals.crawlerLogManager.writeLog("Get link_crawled_table fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());
			
			return null;
		} finally {
			DAOUtil.close(connection, preparedStatement, resultSet);
		}
	}

	@Override
	public int create(LinkCrawled linkCrawled) throws SQLException {
		if (linkCrawled.getLink() == null) {
			return -1;
		}

		// If the time crawled is not specified, use the current time
		if (linkCrawled.getTimeCrawled() == null || linkCrawled.getDateCrawled() == null) {
			linkCrawled.setTimeCrawled(Helper.getCurrentTime());
			linkCrawled.setDateCrawled(Helper.getCurrentDate());
		}
		
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			connection = this.daoFactory.getConnection();
			
			Object[] values = {
				linkCrawled.getLink(),
				linkCrawled.getDomainTableId1(),
				linkCrawled.getPriority(),
				linkCrawled.getTimeCrawled(),
				linkCrawled.getDateCrawled(),
				linkCrawled.getCountry(),
				linkCrawled.getState(),
				linkCrawled.getCity()
			};
			
			preparedStatement = DAOUtil.prepareStatement(connection, SQL_INSERT, true, values);

			if (Globals.DEBUG)
				Globals.crawlerLogManager.writeLog(preparedStatement.toString());
			
			preparedStatement.executeUpdate();

			// Get the generated key (id)
			resultSet = preparedStatement.getGeneratedKeys();
			int generatedKey = -1;
			
			if (resultSet.next()) {
				generatedKey = resultSet.getInt(1);
			}
			
			return generatedKey;
		} catch (SQLException e) {
			Globals.crawlerLogManager
					.writeLog("Insert into link_crawled_table fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());
			
			return -1;
		} finally {
			DAOUtil.close(connection, preparedStatement, resultSet);
		}
	}
	
	@Override
	public boolean update(LinkCrawled linkCrawled) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			connection = this.daoFactory.getConnection();
			
			Object[] values = {
				linkCrawled.getLink(),
				linkCrawled.getPriority(),
				linkCrawled.getCountry(),
				linkCrawled.getState(),
				linkCrawled.getCity(),
				linkCrawled.getId()
			};
			
			preparedStatement = DAOUtil.prepareStatement(connection, SQL_UPDATE, false, values);
			
			if (Globals.DEBUG)
				Globals.crawlerLogManager.writeLog(preparedStatement.toString());
			
			preparedStatement.executeUpdate();
			
			return true;
		} catch (SQLException e) {
			Globals.crawlerLogManager
					.writeLog("Update link_crawled_table fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());
			
			return false;
		} finally {
			DAOUtil.close(connection, preparedStatement, resultSet);
		}
	}
}
