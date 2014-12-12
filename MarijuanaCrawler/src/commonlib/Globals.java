package commonlib;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import naiveBayes.Dictionary;
import naiveBayes.FeaturesCalculator;
import naiveBayes.ModelTrainer;
import naiveBayes.NaiveBayesClassifier;

public class Globals {
	public static final LogManager crawlerLogManager = new LogManager("crawlerLog", "crawlerLog");
	public static final boolean DEBUG = true;
	public static int DEFAULTLOWERBOUNDWAITTIMESEC = 30;
	public static int DEFAULTUPPERBOUNDWAITTIMESEC = 60;
	public static String pathSeparator = File.separator;
	
	public static String username = "cedro";
	public static String password = "password";
	public static String server = "pow-db.clfpwrv3fbfn.us-west-2.rds.amazonaws.com:4200";
	public static String database = "powdb";
	
	// public static String username = "root";
	// public static String password = "";
	// public static String server = "localhost";
	// public static String database = "weedpricelink";
	
	public static final String[] fileExtenstions = { "jpg", "xml", "gif",
			"pdf", "png", "jpeg" };
	
	public static final String[] stopWords = { "weed out" };
	
	public static final String[] importantWords = { "donation", "1/2", "half", "1/4", "quarter", "qtr", "qt", "1/8", "eight", "1oz", "1 oz", "marijuana", "cannabis", "bho", "cookie", "afghan", "ganja", "wax" };
	
	public static class Location {
		public String country = null;
		public String state = null;
		public String city = null;
		
		public Location() {
		}
		
		public Location(String country, String state, String city) {
			this.country = country;
			this.state = state;
			this.city = city;
		}
	}
	
	// Type of links
	public static enum Type {
		LISTING(1);

		public final int value;

		private Type(int value) {
			this.value = value;
		}
	};

	// Map between the type and its name as string
	public static Map<Type, String> typeNameMap;
	static {
		Map<Type, String> tempMap = new HashMap<Type, String>();
		tempMap.put(Type.LISTING, "LISTING");
		typeNameMap = Collections.unmodifiableMap(tempMap);
	}

	// Domain of links
	public static enum Domain {
		CRAIGLIST(1);

		public final int value;

		private Domain(int value) {
			this.value = value;
		}
	};

	// Map between the domain and its name as string
	public static Map<Domain, String> domainNameMap;
	static {
		Map<Domain, String> tempMap = new HashMap<Domain, String>();
		tempMap.put(Domain.CRAIGLIST, "CRAIGLIST");
		domainNameMap = Collections.unmodifiableMap(tempMap);
	}

	// Map between the type and keywords associated with the type
	public static Map<Type, String[]> typeTopicMap;
	public static final String[] LISTINGTOPICS = { "Marijuana" };

	static {
		Map<Type, String[]> tempMap = new HashMap<Type, String[]>();
		tempMap.put(Type.LISTING, LISTINGTOPICS);
		typeTopicMap = Collections.unmodifiableMap(tempMap);
	}
	
	public static NaiveBayesClassifier classifier = null;
	static {
		Dictionary dic = new Dictionary();
		dic.GenerateDictionary();
		FeaturesCalculator cal = new FeaturesCalculator(dic);
		ModelTrainer model = new ModelTrainer(dic);
		model.ReadModelFromFile("model.txt");
		Globals.classifier = new NaiveBayesClassifier(model, cal, dic);
	}
}
