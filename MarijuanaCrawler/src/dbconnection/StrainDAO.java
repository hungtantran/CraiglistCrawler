package dbconnection;

import java.sql.SQLException;
import java.util.List;

public interface StrainDAO {
    public List<Strain> get() throws SQLException;
    
    public boolean create(Strain strain) throws SQLException;
}
