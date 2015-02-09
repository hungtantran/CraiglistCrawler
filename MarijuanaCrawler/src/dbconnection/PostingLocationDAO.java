package dbconnection;

import java.sql.SQLException;
import java.util.List;

public interface PostingLocationDAO {
    public List<PostingLocation> get() throws SQLException;
    
    public PostingLocation get(int locationId) throws Exception;
    
    public boolean create(PostingLocation location) throws SQLException;
}
