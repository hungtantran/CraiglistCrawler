package naiveBayes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import commonlib.Globals;
import commonlib.Helper;

import dbconnection.MySqlConnection;

public class Dictionary {
	private String filePath = "dictionary.txt";
	private SortedSet<String> dictionary = null;

	public Dictionary() {
		this.dictionary = new TreeSet<String>();
	}

	public Dictionary(String filePath) {
		this();
		this.filePath = filePath;
	}

	public boolean GenerateDictionary() {
		if (this.dictionary == null)
			return false;
		
		try {
			BufferedReader br = new BufferedReader(
					new FileReader(this.filePath));
			
			String line = null;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				line = line.toLowerCase();
				this.dictionary.add(line);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public boolean AddWord(String word) {
		if (this.dictionary == null)
			return false;
		
		this.dictionary.add(word);
		
		return true;
	}
	
	public SortedSet<String> GetWordList() {
		return this.dictionary;
	}
	
	public int GetSize() {
		if (this.dictionary == null)
			return 0;
		
		return this.dictionary.size();
	}
	
	public boolean WordExists(String word) {
		if (word == null)
			return false;
		
		if (this.dictionary == null)
			return false;
		
		return this.dictionary.contains(word);
	}
	
	public static void CreateDictionary() {
		int lowerBound = 0;
		int maxNumResult = 200;
		SortedSet<String> dictionary = new TreeSet<String>();
		Map<String, Integer> wordsToOccurence = new HashMap<String, Integer>();
		
		// Get 2000 articles at a time, until exhaust all the articles
		while (true) {
			MySqlConnection mysqlConnection = new MySqlConnection(
					Globals.username, Globals.password, Globals.server,
					Globals.database);

			ResultSet resultSet = mysqlConnection.GetRawHTML(lowerBound,
					maxNumResult);
			if (resultSet == null)
				break;

			try {
				int count = 0;
				// Iterate through the result set to populate the information
				while (resultSet.next()) {
					count++;
					int positive = resultSet.getInt("positive");
					if (positive != 1)
						continue;
					
					String htmlContent = resultSet.getString("html");
					
					String htmlBody = Helper.getPostingBodyFromHtmlContent(htmlContent);
					if (htmlBody == null) {
						continue;
					}
					
					Set<String> wordsSet = Helper.expandContentToWords(htmlBody);
					for (String word : wordsSet) {
						int occurence = 1;
						if (wordsToOccurence.containsKey(word)) {
							occurence = wordsToOccurence.get(word) + 1;
						}
						
						wordsToOccurence.put(word, occurence);
					}
				}

				if (count == 0)
					break;
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}

			lowerBound += maxNumResult;
		}
		
		for (Map.Entry<String, Integer> entry : wordsToOccurence.entrySet()) {
			String word = entry.getKey();
			int occurence = entry.getValue();
			
			if (occurence > 10) {
				dictionary.add(word);
			}
		}
		
		PrintWriter writer;
		try {
			writer = new PrintWriter("dictionary.txt", "UTF-8");
			
			for (String word : dictionary) {
				writer.println(word);
			}
			
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
//		Dictionary dic = new Dictionary();
//		if (dic.GenerateDictionary()) {
//			SortedSet<String> wordList = dic.GetWordList();
//			for (String word : wordList) {
//				System.out.println(word);
//			}
//			
//			System.out.println(wordList.size() + " words");
//		}
		
		Dictionary.CreateDictionary();
	}
}
