package dbconnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import commonlib.Globals;

public class StrainDAOJDBC implements StrainDAO {
    private final String SQL_SELECT_ALL = "SELECT * FROM strains";
    private final String SQL_INSERT = "INSERT INTO strains"
            + "(name, description, reviews, photo, type)"
            + " values (?, ?, ?, ?, ?)";

    private final DAOFactory daoFactory;

    public StrainDAOJDBC(DAOFactory daoFactory) throws SQLException {
        this.daoFactory = daoFactory;
    }

    private Strain constructStrainObject(ResultSet resultSet) throws SQLException {
        final Strain strain = new Strain();
        
        strain.setId(resultSet.getInt("id"));
        if (resultSet.wasNull()) {
        	strain.setId(null);
        }
        
        strain.setName(resultSet.getString("name"));
        if (resultSet.wasNull()) {
        	strain.setName(null);
        }
        
        strain.setDescription(resultSet.getString("description"));
        if (resultSet.wasNull()) {
        	strain.setDescription(null);
        }
        
        strain.setReviews(resultSet.getString("reviews"));
        if (resultSet.wasNull()) {
        	strain.setReviews(null);
        }
        
        strain.setType(resultSet.getInt("type"));
        if (resultSet.wasNull()) {
        	strain.setType(null);
        }
        
        return strain;
    }
    
    @Override
    public List<Strain> get() throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.daoFactory.getConnection();
            preparedStatement = DAOUtil.prepareStatement(connection, this.SQL_SELECT_ALL, false);
            resultSet = preparedStatement.executeQuery();

            final List<Strain> strains = new ArrayList<Strain>();
            while (resultSet.next()) {
                final Strain strain = this.constructStrainObject(resultSet);
                strains.add(strain);
            }

            return strains;
        } catch (final SQLException e) {
            Globals.crawlerLogManager.writeLog("Get strains fails");
            Globals.crawlerLogManager.writeLog(e.getMessage());

            return null;
        } finally {
            DAOUtil.close(connection, preparedStatement, resultSet);
        }
    }

    @Override
    public boolean create(Strain strain) throws SQLException {
        if (!strain.isValid()) {
            return false;
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.daoFactory.getConnection();

            final Object[] values = {
                    strain.getName(),
                    strain.getDescription(),
                    strain.getReviews(),
                    strain.getPhoto(),
                    strain.getType()};

            preparedStatement = DAOUtil.prepareStatement(connection, this.SQL_INSERT, false, values);

            Globals.crawlerLogManager.writeLog(preparedStatement.toString());

            preparedStatement.executeUpdate();

            return true;
        } catch (final SQLException e) {
            Globals.crawlerLogManager.writeLog("Insert into table strains fails");
            Globals.crawlerLogManager.writeLog(e.getMessage());

            return false;
        } finally {
            DAOUtil.close(connection, preparedStatement, resultSet);
        }
    }
}
