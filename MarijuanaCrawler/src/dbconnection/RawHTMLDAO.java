package dbconnection;

import java.sql.SQLException;
import java.util.List;

public interface RawHTMLDAO {
	public RawHTML find(int id) throws SQLException;
	
	public List<RawHTML> get() throws SQLException;
	
	public List<RawHTML> get(int lowerBound, int maxNumResult) throws SQLException;
	
	public boolean create(RawHTML rawHTML) throws SQLException;
	
	public boolean update(RawHTML rawHTML) throws SQLException;
}