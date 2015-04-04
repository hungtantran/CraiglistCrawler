package commonlib;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helper {
	public static String[] splitString(String string, String delimiter) {
		if (string == null || delimiter == null)
			return null;

		ArrayList<String> splitString = new ArrayList<String>();
		int prevPos = 0;
		for (int i = 0; i < string.length() - delimiter.length() + 1; i++) {
			String subStringAtCurPos = string.substring(i,
					i + delimiter.length());
			if (subStringAtCurPos.equals(delimiter)) {
				String token = string.substring(prevPos, i);
				if (token.length() > 0)
					splitString.add(token);
				i += delimiter.length();
				prevPos = i;
			}
		}

		String token = string.substring(prevPos, string.length());
		if (token.length() > 0)
			splitString.add(token);

		String[] wordsArray = new String[splitString.size()];
		for (int i = 0; i < splitString.size(); i++)
			wordsArray[i] = splitString.get(i);

		return wordsArray;
	}

	// Given a string and list of delimiters. Split the string into a set of
	// words with delimiter contained in the list
	public static Set<String> splitString(String string, String[] delimiters) {
		if (string == null || delimiters == null)
			return null;

		Set<String> wordsSet = new HashSet<String>();
		wordsSet.add(string);
		for (String delimiter : delimiters) {
			Set<String> tempSet = new HashSet<String>();

			for (String word : wordsSet) {
				String[] newWords = Helper.splitString(word, delimiter);
				for (String newWord : newWords)
					tempSet.add(newWord);
			}

			wordsSet = tempSet;
		}

		return wordsSet;
	}

	// Return the current date, e.g: 2014-05-23
	@SuppressWarnings("deprecation")
	public static String getCurrentDate() {
		Date currentDate = new Date();

		StringBuilder dateString = new StringBuilder();
		dateString.append(1900 + currentDate.getYear());
		dateString.append("-");
		dateString.append(currentDate.getMonth() + 1);
		dateString.append("-");
		dateString.append(currentDate.getDate());

		return dateString.toString();
	}

	// Return the current time 22:11:30
	@SuppressWarnings("deprecation")
	public static String getCurrentTime() {
		Date currentDate = new Date();

		StringBuilder timeString = new StringBuilder();
		timeString.append(currentDate.getHours());
		timeString.append(":");
		timeString.append(currentDate.getMinutes());
		timeString.append(":");
		timeString.append(currentDate.getSeconds());

		return timeString.toString();
	}

	// Make the current thread wait for a random amount of time between
	// lowerBound and upperBound number of seconds
	public static void waitSec(int lowerBound, int upperBound) {
		try {
			int waitTime = lowerBound * 1000 + (int) (Math.random() * ((upperBound * 1000 - lowerBound * 1000) + 1));
			Globals.crawlerLogManager.writeLog("Wait for " + waitTime);
			Thread.currentThread();
			Thread.sleep(waitTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// Read content of a file and return the string content
	public static String readFileContent(String path) throws IOException {
		if (path == null)
			return null;

		// Read in file content
		StringBuffer fileData = new StringBuffer();
		BufferedReader reader = new BufferedReader(new FileReader(path));

		char[] buf = new char[10000];

		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
		}

		reader.close();

		return fileData.toString();
	}

	public static Set<String> expandContentToWords(String content) {
		if (content == null)
			return null;

		content = content.toLowerCase();
		String[] words = content.split("<|>|\\s+|\\.|;|,");

		Set<String> wordsSet = new HashSet<String>();

		for (String word : words) {
			word = word.replaceAll("[^a-zA-Z0-9]", " ");

			String[] word2s = word.split("<|>|\\s+|\\.|;|,");
			for (String word2 : word2s) {
				word2 = word2.trim();

				if (word2.length() < 2 || word2.length() > 20)
					continue;

				if (Helper.isStringMixNumberAndCharacter(word2))
					continue;

				try {
					double d = Double.parseDouble(word2);
					if (d != 420)
						continue;
				} catch (NumberFormatException nfe) {
					// TODO do something
				}

				wordsSet.add(word2);
			}
		}

		return wordsSet;
	}

	public static boolean isStringMixNumberAndCharacter(String str) {
		if (str == null || str.isEmpty())
			return false;

		boolean hasLetter = false;
		boolean hasNumber = false;

		for (int i = 0; i < str.length(); i++) {
			char curChar = str.charAt(i);

			if (Character.isAlphabetic(curChar))
				hasLetter = true;

			if (Character.isDigit(curChar))
				hasNumber = true;
		}

		if (hasLetter && hasNumber)
			return true;

		return false;
	}

	public static boolean isNumeric(String str) {
		try {
			Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public static String getPostingBodyFromHtmlContent(String htmlContent) {
		if (htmlContent == null)
			return null;
		
		String openTag = "<section id=\"postingbody\">";
		
		String htmlBody = htmlContent.toLowerCase();
		
		int openTagIndex = htmlBody.indexOf(openTag);
		if (openTagIndex == -1) {
			return null;
		}
		
		htmlBody = htmlBody.substring(openTagIndex + openTag.length());
		
		String closeTag = "</section>";
		int closeTagIndex = htmlBody.indexOf(closeTag);
		
		if (closeTagIndex == -1) {
			return null;
		}
		
		htmlBody = htmlBody.substring(0, closeTagIndex);
		
		String keywords = "keyword";
		int keywordIndex = htmlBody.indexOf(keywords);
		
		if (keywordIndex != -1) {
			htmlBody = htmlBody.substring(0, keywordIndex);
		}
		
		ArrayList<Integer> commaPositions = new ArrayList<Integer>();
		for (int i = 0; i < htmlBody.length(); i++) {
			if (htmlBody.charAt(i) == ',') {
				commaPositions.add(i);
			}
		}
		
		for (int i = 0; i < commaPositions.size()-5; i++) {
			int pos1 = commaPositions.get(i);
			int pos2 = commaPositions.get(i+1);
			int pos3 = commaPositions.get(i+2);
			int pos4 = commaPositions.get(i+3);
			int pos5 = commaPositions.get(i+4);
			if (pos2 - pos1 < 20 && pos3 - pos2 < 20 && pos4 - pos3 < 20 && pos5- pos4 < 20) {
				htmlBody = htmlBody.substring(0, pos1);
				break;
			}
		}

		return htmlBody;
	}
	
	public static String pruneWords(String content, String[] words) {
		if (content == null)
			return null;
		
		for (String word : words) {
			content = content.replace(word, "");
		}
		
		return content;
	}
	
	public static int numWordsOccur(String content, String[] words) {
		if (content == null || words.length == 0)
			return 0;
		
		int numWordsOccur = 0;
		for (String word : words) {
			if (content.indexOf(word) != -1)
				numWordsOccur++;
		}
		
		return numWordsOccur;
	}

    public static String cleanNonCharacterDigit(String rawString) {
        String cleanUpString = "";

        String regex = "([a-zA-Z0-9$ ])+";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(rawString);
        
        while (m.find()) {
            cleanUpString += m.group(0);
        }
        
        cleanUpString = cleanUpString.trim();
        
        return cleanUpString;
    }
}
