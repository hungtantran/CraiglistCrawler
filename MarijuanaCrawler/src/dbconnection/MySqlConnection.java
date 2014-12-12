package dbconnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import commonlib.Globals;
import commonlib.Helper;

public class MySqlConnection {
	private Connection con = null;
	private String username = null;
	private String password = null;
	private String server = null;
	private String database = null;

	public MySqlConnection(String username, String password, String server,
			String database) {
		this.username = username;
		this.password = password;
		this.server = server;
		this.database = database;

		// Set up sql connection
		try {
			Class.forName("com.mysql.jdbc.Driver");
			this.con = DriverManager.getConnection("jdbc:mysql://"
					+ this.server, this.username, this.password);
		} catch (ClassNotFoundException e) {
			Globals.crawlerLogManager.writeLog("Driver not found");
		} catch (SQLException e) {
			Globals.crawlerLogManager.writeLog(e.getMessage());
		}
	}

	// Populate information of type
	@SuppressWarnings("unused")
	private void getTypeInfo() {
		try {
			Statement st = this.con.createStatement();
			st.executeQuery("USE " + this.database);
			ResultSet resultSet = st.executeQuery("SELECT * FROM type_table");

			// Iterate through the result set to populate the information
			while (resultSet.next()) {
			}
		} catch (SQLException e) {
			Globals.crawlerLogManager
					.writeLog("Get type_table information fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());
		}
	}

	// Get link from the queue given the domainId
	public ResultSet getLinkQueue(int domainId) {
		try {
			Statement st = this.con.createStatement();
			st.executeQuery("USE " + this.database);

			String query = "SELECT link FROM link_queue_table WHERE domain_table_id_1 = "
					+ domainId;

			return st.executeQuery(query);
		} catch (SQLException e) {
			Globals.crawlerLogManager.writeLog("Get link_queue_table fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());
		}

		return null;
	}

	// Get link from the crawled set given the domainId
	public ResultSet getLinkCrawled(int domainId) {
		try {
			Statement st = this.con.createStatement();
			st.executeQuery("USE " + this.database);

			String query = "SELECT * FROM link_crawled_table WHERE domain_table_id_1 = "
					+ domainId;

			return st.executeQuery(query);
		} catch (SQLException e) {
			Globals.crawlerLogManager.writeLog("Get link_crawled_table fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());
		}

		return null;
	}

	public void UpdateLinkCrawl(String link, int priority, String country,
			String state, String city) {
		try {
			Statement st = this.con.createStatement();
			st.executeQuery("USE " + this.database);

			// Insert into watch_price_stat_table
			String prepareStmt = "UPDATE link_crawled_table SET priority = ?, country = ?, state = ?, city = ? WHERE link = ?";

			PreparedStatement stmt = this.con.prepareStatement(prepareStmt);
			stmt.setInt(1, priority);
			stmt.setString(2, country);
			stmt.setString(3, state);
			stmt.setString(4, city);
			stmt.setString(5, link);

			if (Globals.DEBUG)
				Globals.crawlerLogManager.writeLog(stmt.toString());
			stmt.executeUpdate();
		} catch (SQLException e) {
			Globals.crawlerLogManager
					.writeLog("Update link_crawled_table fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());
		}
	}

	// Insert link into link_queue_table
	public boolean insertIntoLinkQueueTable(String link, int domainId,
			Integer priority, Integer persistent, String timeCrawled,
			String dateCrawled) {
		if (link == null)
			return false;

		// If the time crawled is not specified, use the current time
		if (timeCrawled == null || dateCrawled == null) {
			timeCrawled = Helper.getCurrentTime();
			dateCrawled = Helper.getCurrentDate();
		}

		try {
			Statement st = this.con.createStatement();
			st.executeQuery("USE " + this.database);

			// Insert into watch_price_stat_table
			String prepareStmt = "INSERT INTO link_queue_table (" + "link, "
					+ "domain_table_id_1, " + "priority, " + "persistent, "
					+ "time_crawled, "
					+ "date_crawled) values (?, ?, ?, ?, ?, ?)";

			PreparedStatement stmt = this.con.prepareStatement(prepareStmt);
			stmt.setString(1, link);
			stmt.setInt(2, domainId);

			if (priority != null)
				stmt.setInt(3, priority);
			else
				stmt.setNull(3, java.sql.Types.INTEGER);

			if (persistent != null)
				stmt.setInt(4, persistent);
			else
				stmt.setNull(4, java.sql.Types.INTEGER);

			stmt.setString(5, timeCrawled);
			stmt.setString(6, dateCrawled);

			if (Globals.DEBUG)
				Globals.crawlerLogManager.writeLog(stmt.toString());
			stmt.executeUpdate();
		} catch (SQLException e) {
			Globals.crawlerLogManager
					.writeLog("Insert into link_queue_table fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());
			return false;
		}

		return true;
	}

	public int insertIntoLinkCrawledTable(String link, int domainId,
			Integer priority, String timeCrawled, String dateCrawled,
			String country, String state, String city) {
		if (link == null)
			return -1;

		// If the time crawled is not specified, use the current time
		if (timeCrawled == null || dateCrawled == null) {
			timeCrawled = Helper.getCurrentTime();
			dateCrawled = Helper.getCurrentDate();
		}

		try {
			Statement st = this.con.createStatement();
			st.executeQuery("USE " + this.database);

			// Insert into watch_price_stat_table
			String prepareStmt = "INSERT INTO link_crawled_table (link, domain_table_id_1, priority, time_crawled, date_crawled, country, state, city) values (?, ?, ?, ?, ?, ?, ?, ?)";

			PreparedStatement stmt = this.con.prepareStatement(prepareStmt,
					Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, link);
			stmt.setInt(2, domainId);

			if (priority != null)
				stmt.setInt(3, priority);
			else
				stmt.setNull(3, java.sql.Types.INTEGER);

			stmt.setString(4, timeCrawled);
			stmt.setString(5, dateCrawled);

			// Country
			if (country != null)
				stmt.setString(6, country);
			else
				stmt.setNull(6, java.sql.Types.CHAR);

			// State
			if (country != null && state != null)
				stmt.setString(7, state);
			else
				stmt.setNull(7, java.sql.Types.CHAR);

			// City
			if (country != null && state != null && city != null)
				stmt.setString(8, city);
			else
				stmt.setNull(8, java.sql.Types.CHAR);

			if (Globals.DEBUG)
				Globals.crawlerLogManager.writeLog(stmt.toString());
			stmt.executeUpdate();

			// Return the generated key (id)
			ResultSet generatedKeys = stmt.getGeneratedKeys();
			if (generatedKeys.next()) {
				return generatedKeys.getInt(1);
			}
		} catch (SQLException e) {
			Globals.crawlerLogManager
					.writeLog("Insert into link_crawled_table fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());
			return -1;
		}

		return -1;
	}

	public boolean insertIntoRawHTMLTable(int id, String link,
			String htmlContent, Integer positivePage, Integer predict1,
			Integer predict2, String country, String state, String city) {
		if (link == null || htmlContent == null)
			return false;

		try {
			Statement st = this.con.createStatement();
			st.executeQuery("USE " + this.database);

			// Insert into watch_price_stat_table
			String prepareStmt = "INSERT INTO RawHTML (id, url, html, positive, predict1, predict2, country, state, city) values (?, ?, ?, ?, ?, ?, ?, ?, ?)";

			PreparedStatement stmt = this.con.prepareStatement(prepareStmt);
			stmt.setInt(1, id);
			stmt.setString(2, link);
			stmt.setString(3, htmlContent);

			if (positivePage != null) {
				stmt.setInt(4, positivePage);
			} else {
				stmt.setNull(4, java.sql.Types.INTEGER);
			}

			if (predict1 != null) {
				stmt.setInt(5, predict1);
			} else {
				stmt.setNull(5, java.sql.Types.INTEGER);
			}

			if (predict2 != null) {
				stmt.setInt(6, predict2);
			} else {
				stmt.setNull(6, java.sql.Types.INTEGER);
			}

			stmt.setString(7, country);
			stmt.setString(8, state);
			stmt.setString(9, city);

			if (Globals.DEBUG)
				Globals.crawlerLogManager.writeLog("INSERT INTO RawHTML ("
						+ link + ", Content Length = " + htmlContent.length());
			stmt.executeUpdate();
		} catch (SQLException e) {
			Globals.crawlerLogManager
					.writeLog("Insert into table RawHTML fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());
			return false;
		}

		return true;
	}

	public ResultSet GetRawHTML(int lowerBound, int maxNumResult) {
		try {
			Statement st = this.con.createStatement();
			st.executeQuery("USE " + this.database);

			String query = "SELECT * FROM RawHTML";

			if (lowerBound > 0 || maxNumResult > 0)
				query += " LIMIT " + lowerBound + "," + maxNumResult;

			return st.executeQuery(query);
		} catch (SQLException e) {
			Globals.crawlerLogManager.writeLog("Get RawHTML fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());
		}

		return null;
	}

	public void UpdateRawHTML(String link, String htmlContent,
			Integer positivePage, Integer predict1, Integer predict2,
			String country, String state, String city) {
		try {
			Statement st = this.con.createStatement();
			st.executeQuery("USE " + this.database);

			// Insert into watch_price_stat_table
			String prepareStmt = "UPDATE RawHTML SET html = ?, positive = ?, predict1 = ?, predict2 = ?, country = ?, state = ?, city = ? WHERE url = ?";

			PreparedStatement stmt = this.con.prepareStatement(prepareStmt);
			stmt.setString(1, htmlContent);

			if (positivePage != null) {
				stmt.setInt(2, positivePage);
			} else {
				stmt.setNull(2, java.sql.Types.INTEGER);
			}

			if (predict1 != null) {
				stmt.setInt(3, predict1);
			} else {
				stmt.setNull(3, java.sql.Types.INTEGER);
			}

			if (predict2 != null) {
				stmt.setInt(4, predict2);
			} else {
				stmt.setNull(4, java.sql.Types.INTEGER);
			}

			stmt.setString(5, country);
			stmt.setString(6, state);
			stmt.setString(7, city);

			stmt.setString(8, link);

			if (Globals.DEBUG)
				Globals.crawlerLogManager.writeLog("Update RawHTML (" + link
						+ ", Content Length = " + htmlContent.length());
			stmt.executeUpdate();
		} catch (SQLException e) {
			Globals.crawlerLogManager.writeLog("Update RawHTML fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());
		}
	}

	public boolean insertIntoLocationLink(String link, String country,
			String state, String city) {
		if (link == null || country == null || state == null || city == null)
			return false;

		try {
			Statement st = this.con.createStatement();
			st.executeQuery("USE " + this.database);

			// Insert into watch_price_stat_table
			String prepareStmt = "INSERT INTO location_link (link, country, state, city) values (?, ?, ?, ?)";

			PreparedStatement stmt = this.con.prepareStatement(prepareStmt);
			stmt.setString(1, link);
			stmt.setString(2, country);
			stmt.setString(3, state);
			stmt.setString(4, city);

			Globals.crawlerLogManager.writeLog(stmt.toString());

			stmt.executeUpdate();
		} catch (SQLException e) {
			Globals.crawlerLogManager
					.writeLog("Insert into table location_link fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());
			return false;
		}

		return true;
	}

	public Map<String, Globals.Location> GetLocationLink() {
		try {
			Statement st = this.con.createStatement();
			st.executeQuery("USE " + this.database);

			String query = "SELECT * FROM location_link";

			Map<String, Globals.Location> linkToLocationMap = new HashMap<String, Globals.Location>();

			ResultSet resultSet = st.executeQuery(query);
			while (resultSet.next()) {
				String link = resultSet.getString("link");
				String country = resultSet.getString("country");
				String state = resultSet.getString("state");
				String city = resultSet.getString("city");
				Globals.Location location = new Globals.Location(country,
						state, city);

				linkToLocationMap.put(link, location);
			}

			return linkToLocationMap;
		} catch (SQLException e) {
			Globals.crawlerLogManager.writeLog("Get location_link fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());
		}

		return null;
	}

	public static void main(String[] args) {
	}
}
