package naiveBayes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Set;
import java.util.SortedSet;

import commonlib.Helper;

public class FeaturesCalculator {
	private SortedSet<String> dic = null;
	
	public FeaturesCalculator(Dictionary dic) {
		this.dic = dic.GetWordList();
	}
	
	public int[] calculateFeatures(String content) {
		if (this.dic == null || content == null)
			return null;
		
		int[] featureVector = new int[dic.size()];
		
		Set<String> wordsSet = Helper.expandContentToWords(content);
		if (wordsSet == null)
			return featureVector;
		
		int index = 0;
		for (String word : this.dic) {
			//word = word.replaceAll("[^a-zA-Z0-9]","");
			
			if (wordsSet.contains(word)) {
				featureVector[index] = 1;
				// System.out.println(word + " " + 1);
			} else {
				featureVector[index] = 0;
				// System.out.println(word + " " + 0);
			}
			
			index++;
		}
		
		if (index != dic.size()) {
			return null;
		}
		
		return featureVector;
	}
	
	public static void main (String[] args) {
		Dictionary dic = new Dictionary();
		dic.GenerateDictionary();
		FeaturesCalculator cal = new FeaturesCalculator(dic);
		
		try {
			StringBuffer fileData = new StringBuffer();
	        BufferedReader reader = new BufferedReader(new FileReader("positive.html"));
	        char[] buf = new char[1024];
	        int numRead=0;
	        while((numRead=reader.read(buf)) != -1){
	            String readData = String.valueOf(buf, 0, numRead);
	            fileData.append(readData);
	        }
	        reader.close();
	        
	        String htmlContent = fileData.toString();
	        
	        String postingBodyTag = "<section id=\"postingbody\">";
			String htmlBody = htmlContent.substring(htmlContent.indexOf(postingBodyTag)+postingBodyTag.length());
			htmlBody.substring(0, htmlBody.indexOf("</section>"));
			
			int[] featureVector = cal.calculateFeatures(htmlBody);
			
			if (featureVector != null) {
				for (int i = 0; i < featureVector.length; i++) {
					System.out.println(featureVector[i]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
}
