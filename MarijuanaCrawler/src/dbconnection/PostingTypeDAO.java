package dbconnection;

import java.sql.SQLException;
import java.util.List;

public interface PostingTypeDAO {
    public List<PostingType> get() throws SQLException;
    
    public boolean create(PostingType postingType) throws SQLException;
}
