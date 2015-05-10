package dbconnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import commonlib.Globals;

public class PostingTypeDAOJDBC implements PostingTypeDAO {
    private final String SQL_SELECT_ALL = "SELECT * FROM posting_types";
    private final String SQL_INSERT = "INSERT INTO posting_types (posting_location_id, strain_id) values (?, ?)";

    private final DAOFactory daoFactory;

    public PostingTypeDAOJDBC(DAOFactory daoFactory) throws SQLException {
        this.daoFactory = daoFactory;
    }

    private PostingType constructPostingTypeObject(ResultSet resultSet) throws SQLException {
        final PostingType postingType = new PostingType();
        
        postingType.setId(resultSet.getInt("id"));
        if (resultSet.wasNull()) {
        	postingType.setId(null);
        }
        
        postingType.setPostingLocationId(resultSet.getInt("posting_location_id"));
        if (resultSet.wasNull()) {
        	postingType.setPostingLocationId(null);
        }
        
        postingType.setStrainId(resultSet.getInt("strain_id"));
        if (resultSet.wasNull()) {
        	postingType.setStrainId(null);
        }        
        return postingType;
    }
    
    @Override
    public List<PostingType> get() throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.daoFactory.getConnection();
            preparedStatement = DAOUtil.prepareStatement(connection, this.SQL_SELECT_ALL, false);
            resultSet = preparedStatement.executeQuery();

            final List<PostingType> postingTypes = new ArrayList<PostingType>();
            while (resultSet.next()) {
                final PostingType postingType = this.constructPostingTypeObject(resultSet);
                postingTypes.add(postingType);
            }

            return postingTypes;
        } catch (final SQLException e) {
            Globals.crawlerLogManager.writeLog("Get posting_types fails");
            Globals.crawlerLogManager.writeLog(e.getMessage());

            return null;
        } finally {
            DAOUtil.close(connection, preparedStatement, resultSet);
        }
    }

    @Override
    public boolean create(PostingType postingType) throws SQLException {
        if (!postingType.isValid()) {
            return false;
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.daoFactory.getConnection();

            final Object[] values = {
        		postingType.getPostingLocationId(),
        		postingType.getStrainId()};

            preparedStatement = DAOUtil.prepareStatement(connection, this.SQL_INSERT, false, values);

            Globals.crawlerLogManager.writeLog(preparedStatement.toString());

            preparedStatement.executeUpdate();

            return true;
        } catch (final SQLException e) {
            Globals.crawlerLogManager.writeLog("Insert into table posting_types fails");
            Globals.crawlerLogManager.writeLog(e.getMessage());

            return false;
        } finally {
            DAOUtil.close(connection, preparedStatement, resultSet);
        }
    }
}
