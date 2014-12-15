package dbconnection;

import java.sql.SQLException;
import java.util.List;

public interface LinkCrawledDAO {
	public List<LinkCrawled> get(int domainId) throws SQLException;
	
	public int create(LinkCrawled linkCrawled) throws SQLException;
	
	public boolean update(LinkCrawled linkCrawled) throws SQLException;
}
