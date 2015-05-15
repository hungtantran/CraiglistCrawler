package naiveBayes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import commonlib.Globals;
import commonlib.Helper;

import dbconnection.DAOFactory;
import dbconnection.LocationLink;
import dbconnection.LocationLinkDAO;
import dbconnection.LocationLinkDAOJDBC;

public class NaiveBayesClassifier {
	private double probPositive = 0;
	private double probNegative = 0;
	private int featureVectorSize = 0;
	private double[] probFeatureGivenPositive = null;
	private double[] probFeatureGivenNegative = null;
	private FeaturesCalculator cal = null;
	private Map<String, Integer> locationLinkToNumPositive = null;
	private Integer avgNumPositive = null;
	
	public NaiveBayesClassifier(ModelTrainer model, FeaturesCalculator cal, Dictionary dic) {
	    try {
    	    final DAOFactory daoFactory = DAOFactory.getInstance(Globals.username, Globals.password, Globals.server + Globals.database);
            LocationLinkDAO locationLinkDAO = new LocationLinkDAOJDBC(daoFactory);
            List<LocationLink> locationLinks = locationLinkDAO.get();
            
            this.locationLinkToNumPositive = new HashMap<String, Integer>();
            int total = 0;
            int numLinkWithPositive = 0;
            for (LocationLink locationLink : locationLinks) {
                int numPositivePage = locationLink.getNumPositivePagesFound();

                total += numPositivePage;                
                if (numPositivePage > 0) {
                    numLinkWithPositive++;
                }

                this.locationLinkToNumPositive.put(locationLink.getLink(), numPositivePage);
            }
            
            this.avgNumPositive = Math.round(total / numLinkWithPositive);
            Globals.crawlerLogManager.writeLog("Average num positive = " + this.avgNumPositive);
	    } catch (Exception e) {
	        e.printStackTrace();
	        Globals.crawlerLogManager.writeLog(e.getMessage());
	    }
	        
		if (model == null) {
			return;
		}

		int numNegativeEntries = model.getNumNegativeEntries();
		int numPositiveEntries = model.getNumPositiveEntries();
		int[] numFeatureOccurencePositive = model.getNumFeatureOccurencePositive();
		int[] numFeatureOccurenceNegative = model.getNumFeatureOccurenceNegative();

		this.cal = cal;
		this.featureVectorSize = model.getSizeFeatureVector();
		this.probPositive = (double) numPositiveEntries / (numPositiveEntries + numNegativeEntries);
		this.probNegative = 1 - this.probPositive;

		Globals.crawlerLogManager.writeLog(this.featureVectorSize + " " + this.probPositive + " " + this.probNegative);

		// Use Laplace smoothing
		this.probFeatureGivenNegative = new double[this.featureVectorSize];
		for (int i = 0; i < this.featureVectorSize; i++) {
			this.probFeatureGivenNegative[i] = (double) (numFeatureOccurenceNegative[i] + 1) / (numNegativeEntries + 2);
			// System.out.println(this.probFeatureGivenNegative[i]);
		}

		this.probFeatureGivenPositive = new double[this.featureVectorSize];
		for (int i = 0; i < this.featureVectorSize; i++) {
			this.probFeatureGivenPositive[i] = (double) (numFeatureOccurencePositive[i] + 1) / (numPositiveEntries + 2);
			// System.out.println(this.probFeatureGivenPositive[i]);
		}
	}

	public short ClassifyContent(String html) {
		if (html == null) {
			return -1;
		}
		
		String content = Helper.getPostingBodyFromHtmlContent(html);

        if (content == null) {
            return -1;
        }

        content = Helper.pruneWords(content, Globals.stopWords);

		int[] featureVector = cal.calculateFeatures(content);

		double numerator = this.probPositive;
		double denominator1 = this.probPositive;
		double denominator2 = this.probNegative;
		
		for (int i = 0; i < this.featureVectorSize; i++) {
			if (featureVector[i] == 1) {
				numerator *= this.probFeatureGivenPositive[i];
				denominator1 *= this.probFeatureGivenPositive[i];
				denominator2 *= this.probFeatureGivenNegative[i];
			} else {
				numerator *= (1 - this.probFeatureGivenPositive[i]);
				denominator1 *= (1 - this.probFeatureGivenPositive[i]);
				denominator2 *= (1 - this.probFeatureGivenNegative[i]);
			}
		}

		double probContentPositive = numerator / (denominator1 + denominator2);
		System.out.println("Prob = "+probContentPositive + " deno1 = "+denominator1+" deno2 = "+denominator2+" num = "+numerator);
		if (probContentPositive < 0.5) {
		    for (Map.Entry<String, Integer> entry : locationLinkToNumPositive.entrySet()) {
		        if (html.indexOf(entry.getKey()) != -1) {
		            if (entry.getValue() > this.avgNumPositive) {
		                double extraProb = 0.35;
		                extraProb = Math.min(extraProb, (double)((double)(entry.getValue()-this.avgNumPositive)/entry.getValue()));
		                System.out.println("Extra Prob = " + extraProb +" probContentPositive = " + probContentPositive + " link = " + entry.getKey() + "\n");
		                if (extraProb + probContentPositive >= 0.5) {
		                    Globals.crawlerLogManager.writeLog("Categorize as positive with extra prob of " + extraProb);
		                    return 1;
		                }
		            }
		            break;
		        }
		    }
			return 0;
		}

		return 1;
	}

	public ArrayList<Integer> classifyFolder(String path) {
		if (path == null) {
			return null;
		}

		File folder = new File(path);

		if (!folder.exists()) {
			return null;
		}
		
		ArrayList<Integer> results = new ArrayList<Integer>();
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isFile()) {
				String filePath = path + File.separator + fileEntry.getName();
				
				try {
					String fileContent = Helper.readFileContent(filePath);
					
					// Classify the content
					int result = this.ClassifyContent(fileContent);
					System.out.println(result);
					results.add(result);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return results;
	}

	public static void main(String[] args) {
		String path = "testingSet" + File.separator + "negative";
		List<Integer> resultArray = Globals.classifier.classifyFolder(path);
		for (Integer result : resultArray) {
			System.out.println(result);
		}
		// Globals.classifier.ClassifyContent("<section id=\"postingbody\"> Cheap indoor cannabis. Decent quality, text only please <br> <br> Alien og<br> <br> $30 1/8<br> $50 Q<br> $80 half<br> $150 Oz <br> Donations<br> <br> Prescription needed<br> <br> <br> Cannabis, weed,marijuana, blunts,prop215,sativa,indica,santa cruz,california, maryjane, health, 420, medicine </section>");
	}
}
