package marijuanaCrawler;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import commonlib.Globals;
import commonlib.NetworkingFunctions;

import dbconnection.DAOFactory;
import dbconnection.Strain;
import dbconnection.StrainDAO;
import dbconnection.StrainDAOJDBC;

public class LeaflyCrawler implements IWebsiteCrawler {
	private static final Integer NUM_RETRIES_DOWNLOAD = 2;

	private static final Map<Integer, String> typeToLinkMap;
    static {
        Map<Integer, String> tmp = new HashMap<Integer, String>();
        tmp.put(1, "https://www.leafly.com/explore/category-sativa/sort-popular");
        tmp.put(2, "https://www.leafly.com/explore/category-indica/sort-popular");
        tmp.put(3, null);
        tmp.put(4, "https://www.leafly.com/explore/category-hybrid/sort-popular");
        typeToLinkMap = Collections.unmodifiableMap(tmp);
    }
    
    private StrainDAO strainDAO = null;
	
	public LeaflyCrawler() throws ClassNotFoundException, SQLException {
		final DAOFactory daoFactory = DAOFactory.getInstance(Globals.username, Globals.password, Globals.server + Globals.database);
		this.strainDAO = new StrainDAOJDBC(daoFactory);
	}

	@Override
	public boolean crawl() throws Exception {
		for (Map.Entry<Integer, String> entry : typeToLinkMap.entrySet()) {
			final Integer type = entry.getKey();
			final String link = entry.getValue();
			
			if (link == null || link.isEmpty()) {
				continue;
			}
			
			Document doc = NetworkingFunctions.downloadHtmlContentToDoc(link, LeaflyCrawler.NUM_RETRIES_DOWNLOAD);
			if (doc == null) {
				continue;
			}
			
			Elements nameElems = doc.select("div[class=strain-tile__footer]");
			for (Element nameElem : nameElems) {
				Strain strain = new Strain();

				String name = nameElem.text();
				strain.setName(name);
				strain.setType(type);
				
				this.strainDAO.create(strain);
			}
		}

		return true;
	}
}
