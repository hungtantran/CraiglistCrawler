package naiveBayes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;

import commonlib.Helper;

public class ModelTrainer {
	private String folderPath = "trainingSet";
	private FeaturesCalculator cal = null;
	private int sizeFeatureVector = 0;
	private int numPositiveEntries = 0;
	private int numNegativeEntries = 0;
	private int[] numFeatureOccurenceNegative = null;
	private int[] numFeatureOccurencePositive = null;
	
	public ModelTrainer() {
		Dictionary dic = new Dictionary();
		dic.GenerateDictionary();
		this.sizeFeatureVector = dic.GetSize();
		this.cal = new FeaturesCalculator(dic);
	}
	
	public ModelTrainer(Dictionary dic) {
		this.sizeFeatureVector = dic.GetSize();
		this.cal = new FeaturesCalculator(dic);
	}

	public ModelTrainer(String folderPath) {
		this();
		this.folderPath = folderPath;
	}
	
	public ModelTrainer(Dictionary dic, String folderPath) {
		this.sizeFeatureVector = dic.GetSize();
		this.cal = new FeaturesCalculator(dic);
		this.folderPath = folderPath;
	}
	
	public int getSizeFeatureVector() {
		return sizeFeatureVector;
	}

	public int getNumPositiveEntries() {
		return numPositiveEntries;
	}

	public int getNumNegativeEntries() {
		return numNegativeEntries;
	}

	public int[] getNumFeatureOccurenceNegative() {
		return numFeatureOccurenceNegative;
	}

	public int[] getNumFeatureOccurencePositive() {
		return numFeatureOccurencePositive;
	}
	
	@SuppressWarnings("resource")
	public boolean ReadModelFromFile(String filePath) {
		this.sizeFeatureVector = 0;
		this.numPositiveEntries = 0;
		this.numNegativeEntries = 0;
		this.numFeatureOccurenceNegative = null;
		this.numFeatureOccurencePositive = null;
		
		try {
			BufferedReader br = new BufferedReader(
					new FileReader(filePath));
			
			// Read num positive entries
			String line = br.readLine();
			if (line != null) {
				this.numPositiveEntries = Integer.parseInt(line);
			} else return false;
			
			// Read num negative entries
			line = br.readLine();
			if (line != null) {
				this.numNegativeEntries = Integer.parseInt(line);
			} else return false;
			
			// Read size of feature vector
			line = br.readLine();
			if (line != null) {
				this.sizeFeatureVector = Integer.parseInt(line);
			} else return false;
			
			this.numFeatureOccurencePositive = new int[this.sizeFeatureVector];
			this.numFeatureOccurenceNegative = new int[this.sizeFeatureVector];
			
			// Parse array of num feature occurence in positive set
			for (int i = 0; i < this.sizeFeatureVector; i++) {
				line = br.readLine();
				if (line == null) return false;
				this.numFeatureOccurencePositive[i] = Integer.parseInt(line);
			}
			
			// Parse array of num feature occurence in positive set
			for (int i = 0; i < this.sizeFeatureVector; i++) {
				line = br.readLine();
				if (line == null) return false;
				this.numFeatureOccurenceNegative[i] = Integer.parseInt(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public void Train() {
		this.numFeatureOccurenceNegative = new int[this.sizeFeatureVector];
		this.numFeatureOccurencePositive = new int[this.sizeFeatureVector];
		
		String negativePath = this.folderPath + File.separator
				+ "negative";
		File negativeFolder = new File(negativePath);
		
		if (negativeFolder.exists()) {
			for (final File fileEntry : negativeFolder.listFiles()) {
				if (fileEntry.isFile()) {
					System.out.println(fileEntry.getName());

					try {
						// Read in file content
						StringBuffer fileData = new StringBuffer();
						BufferedReader reader = new BufferedReader(
								new FileReader(negativePath + File.separator + fileEntry.getName()));
						char[] buf = new char[10000];
						int numRead = 0;
						while ((numRead = reader.read(buf)) != -1) {
							String readData = String.valueOf(buf, 0, numRead);
							fileData.append(readData);
						}
						reader.close();
						
						// Calculate the feature vector
						String htmlBody = Helper.getPostingBodyFromHtmlContent(fileData.toString());
						if (htmlBody == null)
							continue;
						
						int[] featureVector = this.cal.calculateFeatures(htmlBody);
						
						// Update model information
						if (featureVector != null) {
							this.numNegativeEntries++;
							for (int i = 0; i < featureVector.length; i++) {
								if (featureVector[i] == 1) {
									this.numFeatureOccurenceNegative[i]++;
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						return;
					}
				}
			}
		}
		
		String positivePath = this.folderPath + File.separator
				+ "positive";
		File positiveFolder = new File(positivePath);
		
		if (positiveFolder.exists()) {
			for (final File fileEntry : positiveFolder.listFiles()) {
				if (fileEntry.isFile()) {
					System.out.println(fileEntry.getName());

					try {
						// Read in file content
						StringBuffer fileData = new StringBuffer();
						BufferedReader reader = new BufferedReader(
								new FileReader(positivePath + File.separator + fileEntry.getName()));
						char[] buf = new char[10000];
						int numRead = 0;
						while ((numRead = reader.read(buf)) != -1) {
							String readData = String.valueOf(buf, 0, numRead);
							fileData.append(readData);
						}
						reader.close();
						
						// Calculate the feature vector
						String htmlBody = Helper.getPostingBodyFromHtmlContent(fileData.toString());
						if (htmlBody == null)
							continue;
							
						int[] featureVector = cal.calculateFeatures(htmlBody);
						
						// Update model information
						if (featureVector != null) {
							this.numPositiveEntries++;
							for (int i = 0; i < featureVector.length; i++) {
								if (featureVector[i] == 1) {
									this.numFeatureOccurencePositive[i]++;
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						return;
					}
				}
			}
		}
		
		PrintWriter writer;
		try {
			writer = new PrintWriter("model.txt", "UTF-8");
			writer.println(this.numPositiveEntries);
			writer.println(this.numNegativeEntries);
			writer.println(this.sizeFeatureVector);
			
			for (int i = 0; i < this.sizeFeatureVector; i++) {
				writer.println(this.numFeatureOccurencePositive[i]);
			}
			
			for (int i = 0; i < this.sizeFeatureVector; i++) {
				writer.println(this.numFeatureOccurenceNegative[i]);
			}
			
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		ModelTrainer trainer = new ModelTrainer();
		trainer.Train();
//		if (!trainer.ReadModelFromFile("model.txt")) {
//			System.out.println("Read model fails");
//		}
	}
}
