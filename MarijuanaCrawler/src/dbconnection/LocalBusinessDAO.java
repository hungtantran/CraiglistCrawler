package dbconnection;

import java.sql.SQLException;
import java.util.List;

public interface LocalBusinessDAO {
    public List<LocalBusiness> get() throws SQLException;
    
    public LocalBusiness get(int locationId) throws Exception;
    
    public boolean create(LocalBusiness business) throws SQLException;
}
