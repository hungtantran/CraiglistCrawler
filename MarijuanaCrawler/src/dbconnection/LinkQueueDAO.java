package dbconnection;

import java.sql.SQLException;
import java.util.List;

public interface LinkQueueDAO {
	public List<LinkQueue> get(int domainId) throws SQLException;
	
	public int create(LinkQueue linkQueue) throws SQLException;
}
