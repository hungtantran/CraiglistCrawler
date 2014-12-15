package dbconnection;

import java.sql.SQLException;
import java.util.List;

public interface LocationLinkDAO {
	public List<LocationLink> get() throws SQLException;
	
	public int create(LocationLink locationLink) throws SQLException;
}
