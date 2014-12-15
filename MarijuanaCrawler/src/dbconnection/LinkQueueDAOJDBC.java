package dbconnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import commonlib.Globals;
import commonlib.Helper;

public class LinkQueueDAOJDBC implements LinkQueueDAO {
	private final String SQL_SELECT_BY_DOMAINID = "SELECT * FROM link_queue_table WHERE domain_table_id_1 = ?";
	private final String SQL_INSERT = "INSERT INTO link_queue_table (link, domain_table_id_1, priority, persistent, time_crawled, date_crawled) values (?, ?, ?, ?, ?, ?)";
	
	private DAOFactory daoFactory;

	public LinkQueueDAOJDBC(DAOFactory daoFactory) throws SQLException {
		this.daoFactory = daoFactory;
	}

	private LinkQueue constructLinkQueueObject(ResultSet resultSet)
			throws SQLException {
		LinkQueue linkQueue = new LinkQueue();
		
		linkQueue.setId(resultSet.getInt("id"));
		if (resultSet.wasNull()) linkQueue.setId(null);
		
		linkQueue.setLink(resultSet.getString("link"));
		if (resultSet.wasNull()) linkQueue.setLink(null);
		
		linkQueue.setDomainTableId1(resultSet.getInt("domain_table_id_1"));
		if (resultSet.wasNull()) linkQueue.setDomainTableId1(null);
		
		linkQueue.setPriority(resultSet.getInt("priority"));
		if (resultSet.wasNull()) linkQueue.setPriority(null);
		
		linkQueue.setPersistent(resultSet.getInt("persistent"));
		if (resultSet.wasNull()) linkQueue.setPersistent(null);
		
		linkQueue.setTimeCrawled(resultSet.getString("time_crawled"));
		if (resultSet.wasNull()) linkQueue.setTimeCrawled(null);
		
		linkQueue.setDateCrawled(resultSet.getString("date_crawled"));
		if (resultSet.wasNull()) linkQueue.setDateCrawled(null);
		
		linkQueue.setCountry(resultSet.getString("country"));
		if (resultSet.wasNull()) linkQueue.setCountry(null);
		
		linkQueue.setState(resultSet.getString("state"));
		if (resultSet.wasNull()) linkQueue.setState(null);
		
		linkQueue.setCity(resultSet.getString("city"));
		if (resultSet.wasNull()) linkQueue.setCity(null);
		
		return linkQueue;
	}

	@Override
	public List<LinkQueue> get(int domainId) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			connection = this.daoFactory.getConnection();
			preparedStatement = DAOUtil.prepareStatement(connection, SQL_SELECT_BY_DOMAINID, false, domainId);
			resultSet = preparedStatement.executeQuery();
			
			List<LinkQueue> linksQueue = new ArrayList<LinkQueue>();
			while (resultSet.next()) {
				LinkQueue linkQueue = this.constructLinkQueueObject(resultSet);
				linksQueue.add(linkQueue);
			}

			return linksQueue;
		} catch (SQLException e) {
			Globals.crawlerLogManager.writeLog("Get link_queue_table fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());
			
			return null;
		} finally {
			DAOUtil.close(connection, preparedStatement, resultSet);
		}
	}

	@Override
	public int create(LinkQueue linkQueue) throws SQLException {
		if (linkQueue.getLink() == null) {
			return -1;
		}

		// If the time crawled is not specified, use the current time
		if (linkQueue.getTimeCrawled() == null || linkQueue.getDateCrawled() == null) {
			linkQueue.setTimeCrawled(Helper.getCurrentTime());
			linkQueue.setDateCrawled(Helper.getCurrentDate());
		}
		
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			connection = this.daoFactory.getConnection();
			
			Object[] values = {
				linkQueue.getLink(),
				linkQueue.getDomainTableId1(),
				linkQueue.getPriority(),
				linkQueue.getPersistent(),
				linkQueue.getTimeCrawled(),
				linkQueue.getDateCrawled(),
				linkQueue.getCountry(),
				linkQueue.getState(),
				linkQueue.getCity()
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
					.writeLog("Insert into link_queue_table fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());
			
			return -1;
		} finally {
			DAOUtil.close(connection, preparedStatement, resultSet);
		}
	}
}
