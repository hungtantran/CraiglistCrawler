package dbconnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import commonlib.Globals;
import commonlib.Globals.Type;

public class InitializeDB {
	private Connection con = null;
	private String username = null;
	private String password = null;
	private String server = null;
	private String database = null;

	public InitializeDB(String username, String password, String server,
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

	// Create all the tables
	public void createDB() {
		this.createDomainTable();
		this.createTypeTable();
		this.createTopicTable();
		this.createLinkTable();
	}

	// Initialize the tables with initial data
	public void initializeDB() {
		this.initializeDomainTable();
		this.initializeTypeTable();
		this.initializeTopicTable();
	}

	// Create domain_table
	private void createDomainTable() {
		try {
			Statement st = this.con.createStatement();
			st.executeQuery("USE " + this.database);
			st.executeUpdate("CREATE TABLE domain_table ("
					+ "id int unsigned AUTO_INCREMENT not null, "
					+ "domain char(255) not null, " + "PRIMARY KEY(id), "
					+ "UNIQUE (id), " + "UNIQUE (domain))");
		} catch (SQLException e) {
			Globals.crawlerLogManager.writeLog("CREATE TABLE domain_table fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());
		}
	}

	// Create type_table
	private void createTypeTable() {
		try {
			Statement st = this.con.createStatement();
			st.executeQuery("USE " + this.database);
			st.executeUpdate("CREATE TABLE type_table ("
					+ "id int unsigned AUTO_INCREMENT not null, "
					+ "type char(255) not null, " + "PRIMARY KEY(id), "
					+ "UNIQUE (id), " + "UNIQUE (type))");
		} catch (SQLException e) {
			Globals.crawlerLogManager.writeLog("CREATE TABLE type_table fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());
		}
	}

	// Create topic_table
	private void createTopicTable() {
		try {
			Statement st = this.con.createStatement();
			st.executeQuery("USE " + this.database);
			st.executeUpdate("CREATE TABLE topic_table ("
					+ "id int unsigned AUTO_INCREMENT not null, "
					+ "type_table_id int unsigned not null, "
					+ "topic char(255) not null, " + "PRIMARY KEY(id), "
					+ "FOREIGN KEY (type_table_id) REFERENCES type_table(id), "
					+ "UNIQUE (id), " + "UNIQUE (topic))");
		} catch (SQLException e) {
			Globals.crawlerLogManager.writeLog("CREATE TABLE topic_table fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());
		}
	}

	// Create link_queue_table and link_crawled_table
	private void createLinkTable() {
		try {
			Statement st = this.con.createStatement();
			st.executeQuery("USE " + this.database);

			st.executeUpdate("CREATE TABLE link_queue_table ("
					+ "id int unsigned AUTO_INCREMENT not null, "
					+ "link char(255) not null, "
					+ "domain_table_id_1 int unsigned not null, "
					+ "priority int unsigned, "
					+ "persistent int unsigned, "
					+ "time_crawled char(128) not null, "
					+ "date_crawled char(128) not null, "
					+ "PRIMARY KEY(id), "
					+ "UNIQUE (id), "
					+ "UNIQUE (link), "
					+ "FOREIGN KEY (domain_table_id_1) REFERENCES domain_table(id))");

			st.executeUpdate("CREATE TABLE link_crawled_table ("
					+ "id int unsigned AUTO_INCREMENT not null, "
					+ "link char(255) not null, "
					+ "priority int unsigned, "
					+ "domain_table_id_1 int unsigned not null, "
					+ "time_crawled char(128) not null, "
					+ "date_crawled char(128) not null, "
					+ "PRIMARY KEY(id), "
					+ "UNIQUE (id), "
					+ "UNIQUE (link), "
					+ "FOREIGN KEY (domain_table_id_1) REFERENCES domain_table(id))");
		} catch (SQLException e) {
			Globals.crawlerLogManager.writeLog("CREATE TABLE link_queue_table or link_crawled_table fails");
			Globals.crawlerLogManager.writeLog(e.getMessage());
		}
	}

	// Insert all the domains into the type tables
	private void initializeDomainTable() {
		// TODO try catch less generic
		try {
			Statement st = this.con.createStatement();
			st.executeQuery("USE " + this.database);
		} catch (SQLException e) {
			Globals.crawlerLogManager.writeLog("Fail to initialize domain table");
			Globals.crawlerLogManager.writeLog(e.getMessage());
		}

		for (Map.Entry<Globals.Domain, String> entry : Globals.domainNameMap
				.entrySet()) {
			Globals.Domain type = entry.getKey();
			String domainName = entry.getValue().trim();
			try {
				PreparedStatement stmt = null;
				stmt = this.con
						.prepareStatement("INSERT INTO domain_table (id, domain) values (?, ?)");
				stmt.setInt(1, type.value);
				stmt.setString(2, domainName);
				stmt.executeUpdate();
			} catch (SQLException e) {
				Globals.crawlerLogManager.writeLog("Fail to insert domain '" + domainName
						+ "' into domain_table");
				Globals.crawlerLogManager.writeLog(e.getMessage());
			}
		}
	}

	// Insert all the types into the type tables
	private void initializeTypeTable() {
		try {
			Statement st = this.con.createStatement();
			st.executeQuery("USE " + this.database);
		} catch (SQLException e) {
			Globals.crawlerLogManager.writeLog("Fail to initialize type table");
			Globals.crawlerLogManager.writeLog(e.getMessage());
		}

		for (Map.Entry<Type, String> entry : Globals.typeNameMap.entrySet()) {
			Type type = entry.getKey();
			String typeName = entry.getValue().trim();

			try {
				PreparedStatement stmt = null;
				stmt = this.con
						.prepareStatement("INSERT INTO type_table (id, type) values (?, ?)");
				stmt.setInt(1, type.value);
				stmt.setString(2, typeName);
				stmt.executeUpdate();
			} catch (SQLException e) {
				Globals.crawlerLogManager.writeLog("Fail to insert type '" + type.value
						+ "' into type_table");
			}
		}
	}

	// Insert all the types into the type tables
	private void initializeTopicTable() {
		try {
			Statement st = this.con.createStatement();
			st.executeQuery("USE " + this.database);
		} catch (SQLException e) {
			Globals.crawlerLogManager.writeLog("Fail to initialize topic table");
			Globals.crawlerLogManager.writeLog(e.getMessage());
		}

		// Iteratate through each type to get the list of topics of that type
		for (Map.Entry<Type, String[]> entry : Globals.typeTopicMap.entrySet()) {
			Type type = entry.getKey();
			String[] topics = entry.getValue();

			// Iteratate through each topic in the list of topics
			for (int i = 0; i < topics.length; i++) {
				String topic = topics[i].trim();
				try {
					PreparedStatement stmt = null;
					stmt = this.con
							.prepareStatement("INSERT INTO topic_table (id, type_table_id, topic) values (?, ?, ?)");
					stmt.setInt(1, i + 1);
					stmt.setInt(2, type.value);
					stmt.setString(3, topic);
					stmt.executeUpdate();
				} catch (SQLException e) {
					Globals.crawlerLogManager.writeLog("Fail to insert topic '" + topic
							+ "' into topic_table");
				}
			}
		}
	}

	public static void main(String[] args) {
		InitializeDB con = new InitializeDB(Globals.username, Globals.password,
				Globals.server, Globals.database);
		con.createDB();
		con.initializeDB();
	}
}
