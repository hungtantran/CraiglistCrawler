package dbconnection;

import java.sql.SQLException;
import java.util.List;

public interface LocationDBDAO {
	public List<LocationDB> get() throws SQLException;
	
	public int create(LocationDB loc) throws SQLException;
}
