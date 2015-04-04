package script;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import commonlib.Globals;
import commonlib.Helper;
import dbconnection.DAOFactory;
import dbconnection.DAOUtil;

public class ExtractPostingTtile {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        DAOFactory daoFactory = null;
        daoFactory = DAOFactory.getInstance(Globals.username, Globals.password, Globals.server + Globals.database);
        
        Connection connection = daoFactory.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        String sqlQuery = "SELECT location_fk FROM posting_location WHERE title IS NULL";
        preparedStatement = DAOUtil.prepareStatement(connection, sqlQuery, false);
        resultSet = preparedStatement.executeQuery();

        List<Integer> ids = new ArrayList<Integer>();
        while (resultSet.next()) {
            ids.add(resultSet.getInt("location_fk"));
        }
        
        for (Integer id : ids) {
            sqlQuery = "SELECT html FROM rawhtml WHERE id = " + id;
            preparedStatement = DAOUtil.prepareStatement(connection, sqlQuery, false);
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                String html = resultSet.getString("html");
                Document doc = Jsoup.parse(html);
                
                Elements postingTitles = doc.select("h2[class=postingtitle]");
                if (postingTitles.size() != 1) {
                    Globals.crawlerLogManager.writeLog("Can't parse posting title for id " + id);
                    continue;
                }
                String postingTitle = Helper.cleanNonCharacterDigit(postingTitles.get(0).text());
                System.out.println("Found title '" + postingTitle + "' for id " + id);
                
                String update = "UPDATE posting_location SET title = '" + postingTitle + "' WHERE location_fk = " + id;
                System.out.println(update);
                preparedStatement = DAOUtil.prepareStatement(connection, update, false);
                preparedStatement.executeUpdate();
            }
        }
    }
}
