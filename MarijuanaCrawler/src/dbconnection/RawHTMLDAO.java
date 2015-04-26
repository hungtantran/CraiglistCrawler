package dbconnection;

import java.sql.SQLException;
import java.util.List;

public interface RawHTMLDAO {
	public RawHTML get(int id) throws SQLException;
	
	public List<RawHTML> get() throws SQLException;
	
	public List<RawHTML> get(int lowerBound, int maxNumResult) throws SQLException;
	
	public int create(RawHTML rawHTML) throws SQLException;
	
	public boolean update(RawHTML rawHTML) throws SQLException;
}
