package marijuanaCrawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import commonlib.Helper;
import commonlib.Globals;
import commonlib.NetworkingFunctions;

public class CraiglistEntryParser {
	private final double gramsInOunce = 28.3495;
	private final int numRetryDownloadPage = 2;
	private final String[] breakMarks = { "<br>", "<br/>", "</br>", "<br />",
			"</ br>" };
	private final double minPrice = 10;
	private final double minGramsQuantity = 1;
	private final double maxOuncesQuantity = 10;
	private Map<Double, Double> quantityToPriceMap = new HashMap<Double, Double>();

	protected String link = null;
	protected Document doc = null;
	protected String content = null;
	protected String timeCreated = null;
	protected String dateCreated = null;

	public CraiglistEntryParser(String link) {
		if (link == null)
			return;

		this.link = link;
		downloadHtmlContent(this.link, this.numRetryDownloadPage);
	}

	public CraiglistEntryParser(Document doc) {
		if (doc == null)
			return;

		this.link = null;
		this.doc = doc;
		this.content = this.doc.outerHtml();
	}

	protected void downloadHtmlContent(String url, int numRetryDownloadPage) {
		this.doc = NetworkingFunctions.downloadHtmlContentToDoc(url,
				numRetryDownloadPage);

		if (this.doc != null) {
			Globals.crawlerLogManager.writeLog("Success downloading the html");
			this.content = this.doc.outerHtml();
		} else {
			Globals.crawlerLogManager.writeLog("Failure downloading the html");
			this.content = null;
		}
	}

	public boolean isValid() {
		return (this.doc != null && this.content != null);
	}

	public String getContent() {
		return this.content;
	}

	public boolean parseDoc() {
		if (!this.isValid())
			return false;

		Globals.crawlerLogManager.writeLog("Start Parsing Weed Price");
		this.parseWeedPrice();

		return true;
	}

	// Find the next instance of appearance of a subword in a strin
	private int findSubWord(String str, String subWord) {
		if (subWord == null || str == null)
			return -1;

		String string = new String(str);
		while (true) {
			int index = string.indexOf(subWord);
			if (index == -1)
				break;

			char charAfterSubWord = string.charAt(index + subWord.length());
			if (Character.isLetter(charAfterSubWord)) {
				string = string.substring(index + subWord.length());
			} else {
				return index;
			}
		}

		return -1;
	}

	private double findQuantity(String str, int index, int direction,
			Integer[] position) {
		if (str == null || index < 0 || index >= str.length() || direction == 0)
			return -1;

		direction = direction / Math.abs(direction);
		Map<String, Double> stringToQuantityMap = new HashMap<String, Double>();
		stringToQuantityMap.put("quarter", 0.25);
		stringToQuantityMap.put("qt", 0.25);
		stringToQuantityMap.put("half", 0.5);
		stringToQuantityMap.put("eighth", 0.125);
		stringToQuantityMap.put("eight", 0.125);
		stringToQuantityMap.put("an ounce", 1.0);

		double quantity = 0;

		boolean startQuantity = false;
		String quantityString = "";
		while (true) {
			if (index >= str.length() || index < 0)
				break;

			char curChar = str.charAt(index);
			// Handle quantity in string like quarter, half, eighth, etc...
			if (Character.isLetter(curChar)) {
				for (Map.Entry<String, Double> entry : stringToQuantityMap
						.entrySet()) {
					if (index >= entry.getKey().length() - 1) {
						String compareString = str.substring(
								index - (entry.getKey().length() - 1),
								index + 1).toLowerCase();
						
						if (compareString.equals(entry.getKey())) {
							position[0] = index - (entry.getKey().length() - 1);
							return entry.getValue();
						}
					}
				}
				break;
			}

			if (startQuantity
					&& (!Character.isDigit(curChar) && curChar != ','
							&& curChar != '.' && curChar != '/' && curChar != '\\'))
				break;

			// Start the quantity string when finding the first digit
			if (Character.isDigit(curChar)) {
				startQuantity = true;
			}

			// Concat the current character belonging to the quantityString to
			// the quantityString
			if (startQuantity
					&& (Character.isDigit(curChar) || curChar == ','
							|| curChar == '.' || curChar == '/' || curChar == '\\')) {
				if (position[0] == -1
						|| (position[0] > -1 && position[0] > index))
					position[0] = index;

				if (direction < 0) {
					quantityString = "" + curChar + quantityString;
				} else if (direction > 0) {
					quantityString += curChar;
				}
			}

			index += direction;
		}

		// If not found quantity string
		if (!startQuantity)
			return quantity;

		// If found quantity string
		if (quantityString.indexOf('/') == -1
				&& quantityString.indexOf('\\') == -1) {
			try {
				quantity = Double.parseDouble(quantityString);
			} catch (Exception e) {
				// TODO catch exception
			}
		} else if (quantityString.indexOf('/') != -1) {
			try {
				String firstPart = quantityString.substring(0,
						quantityString.indexOf('/'));
				String secondPart = quantityString.substring(quantityString
						.indexOf('/') + 1);
				int numerator = Integer.parseInt(firstPart);
				int denominator = Integer.parseInt(secondPart);
				quantity = (double) numerator / (double) denominator;
			} catch (Exception e) {
				// TODO catch exception
			}
		} else if (quantityString.indexOf('\\') != -1) {
			try {
				String firstPart = quantityString.substring(0,
						quantityString.indexOf('\\'));
				String secondPart = quantityString.substring(quantityString
						.indexOf('\\') + 1);
				int numerator = Integer.parseInt(firstPart);
				int denominator = Integer.parseInt(secondPart);
				quantity = (double) numerator / (double) denominator;
			} catch (Exception e) {
				// TODO catch exception
			}
		}

		return quantity;
	}

	// Parse out ounces quantities out of a line
	private Double[] getOunces(String line) {
		if (line == null)
			return null;

		ArrayList<Double> ounces = new ArrayList<Double>();
		/*
		 * String[] ounceSymbols = { "ounce", "ounces", "oz" };
		 * 
		 * for (String sym : ounceSymbols) { String str = new String(line); //
		 * Find all the current occurence of the symbol in the line while (true)
		 * { int index = this.findSubWord(str, sym);
		 * 
		 * if (index != -1) { double quantityBefore = this.findQuantity(str,
		 * index-1, -1); double quantityAfter = this.findQuantity(str, index +
		 * sym.length(), 1);
		 * 
		 * // Found quantity if (quantityBefore > 0 || quantityAfter > 0) { if
		 * (quantityBefore > 0 && quantityAfter > 0) {
		 * ounces.add(quantityBefore); } else if (quantityBefore > 0) {
		 * ounces.add(quantityBefore); } else if (quantityAfter > 0) {
		 * ounces.add(quantityAfter); }
		 * 
		 * System.out.println("Found ounce quantity "+ quantityBefore+" "+
		 * quantityAfter); }
		 * 
		 * str = str.substring(index + sym.length()); } else break; } }
		 */

		// Ounce sometimes doesn't have any symbol goes with it so let's get all
		// the quantity

		int index = -1;
		Set<Integer> positions = new HashSet<Integer>();
		while (true) {
			index++;
			if (index >= line.length())
				break;

			char curChar = line.charAt(index);
			if (Character.isDigit(curChar) || curChar == '.' || curChar == ','
					|| curChar == '/' || curChar == '\\')
				continue;

			Integer[] positionBefore = new Integer[1];
			positionBefore[0] = -1;
			Integer[] positionAfter = new Integer[1];
			positionAfter[0] = -1;
			double quantityBefore = this.findQuantity(line, index - 1, -1,
					positionBefore);
			double quantityAfter = this.findQuantity(line, index, 1,
					positionAfter);

			// Found quantity
			if (!positions.contains(positionBefore[0]) && quantityBefore > 0
					&& quantityBefore < this.maxOuncesQuantity) {
				ounces.add(quantityBefore);
				positions.add(positionBefore[0]);
			} else if (!positions.contains(positionAfter[0])
					&& quantityAfter > 0
					&& quantityAfter < this.maxOuncesQuantity) {
				ounces.add(quantityAfter);
				positions.add(positionAfter[0]);
			}
		}

		Double[] ouncesArray = new Double[ounces.size()];
		ouncesArray = ounces.toArray(ouncesArray);
		return ouncesArray;
	}

	// Parse out gram quantities out of a line
	private Double[] getGrams(String line) {
		if (line == null)
			return null;

		ArrayList<Double> grams = new ArrayList<Double>();
		String[] gramsSymbols = { "gram", "grams", "gr", "g" };
		Set<Integer> positions = new HashSet<Integer>();

		for (String sym : gramsSymbols) {
			String str = new String(line);
			// Find all the current occurence of the symbol in the line
			while (true) {
				int index = this.findSubWord(str, sym);
				if (index != -1) {
					Integer[] positionBefore = new Integer[1];
					positionBefore[0] = -1;
					Integer[] positionAfter = new Integer[1];
					positionAfter[0] = -1;
					double quantityBefore = this.findQuantity(str, index - 1,
							-1, positionBefore);
					double quantityAfter = this.findQuantity(str,
							index + sym.length(), 1, positionAfter);

					// Found quantity (gram needs to be > 1, to avoid confuse
					// with ounces)
					if (!positions.contains(positionBefore[0])
							&& quantityBefore > this.minGramsQuantity) {
						positions.add(positionBefore[0]);
						grams.add(quantityBefore);
					} else if (!positions.contains(positionAfter[0])
							&& quantityAfter > this.minGramsQuantity) {
						positions.add(positionAfter[0]);
						grams.add(quantityAfter);
					}

					str = str.substring(index + sym.length());
				} else
					break;
			}
		}

		Double[] gramsArray = new Double[grams.size()];
		gramsArray = grams.toArray(gramsArray);
		return gramsArray;
	}

	// Parse out ounces quantities out of a line
	private Double[] getPrices(String line) {
		if (line == null)
			return null;

		ArrayList<Double> prices = new ArrayList<Double>();
		int index = -1;
		Set<Integer> positions = new HashSet<Integer>();

		while (true) {
			index++;
			if (index >= line.length())
				break;

			char curChar = line.charAt(index);
			if (Character.isDigit(curChar) || curChar == '.' || curChar == ','
					|| curChar == '/' || curChar == '\\')
				continue;

			Integer[] positionBefore = new Integer[1];
			positionBefore[0] = -1;
			Integer[] positionAfter = new Integer[1];
			positionAfter[0] = -1;
			double quantityBefore = this.findQuantity(line, index - 1, -1,
					positionBefore);
			double quantityAfter = this.findQuantity(line, index, 1,
					positionAfter);

			// Found quantity
			if (!positions.contains(positionBefore[0])
					&& quantityBefore > this.minPrice) {
				prices.add(quantityBefore);
				positions.add(positionBefore[0]);
			} else if (!positions.contains(positionAfter[0])
					&& quantityAfter > this.minPrice) {
				prices.add(quantityAfter);
				positions.add(positionAfter[0]);
			}
		}

		Double[] pricesArray = new Double[prices.size()];
		pricesArray = prices.toArray(pricesArray);
		return pricesArray;
	}

	// Parse the price of the weed
	private boolean parseWeedPrice() {
		Elements postingBodyElems = doc.select("section[id=postingbody]");

		// Return if the page has no posting body
		if (postingBodyElems.size() != 1) {
			Globals.crawlerLogManager.writeLog("No body element");
			return false;
		}

		String postingBody = postingBodyElems.get(0).toString();
		Set<String> bodyLines = Helper
				.splitString(postingBody, this.breakMarks);

		for (String line : bodyLines) {
			if (line.trim().length() == 0)
				continue;

			Globals.crawlerLogManager.writeLog("Line = " + line);
			Double[] ounces = this.getOunces(line);
			Double[] grams = this.getGrams(line);
			Double[] prices = this.getPrices(line);

			for (Double ounce : ounces)
				Globals.crawlerLogManager.writeLog("Ounces = " + ounce);

			for (Double gram : grams)
				Globals.crawlerLogManager.writeLog("Grams = " + gram);

			for (Double price : prices)
				Globals.crawlerLogManager.writeLog("Prices = " + price);

			// If there is no price
			if (prices.length == 0)
				continue;

			// If there is no quantity information on the line
			if (ounces.length == 0 && grams.length == 0)
				continue;

			// If the number of quantities in gram and ounces are different
			if (ounces.length > 0 && grams.length > 0
					&& ounces.length != grams.length)
				continue;

			// TODO add gram in here
			// Prices and quantities do not match
			if (ounces.length != prices.length)
				continue;

			for (int i = 0; i < ounces.length; i++)
				this.quantityToPriceMap.put(ounces[i], prices[i]);
		}

		return true;

	}

	public static void main(String[] args) {
		// CraiglistEntryParser parser = new CraiglistEntryParser(
		// "http://seattle.craigslist.org/skc/for/4666121116.html");
		CraiglistEntryParser parser = new CraiglistEntryParser(
				"http://seattle.craigslist.org/skc/fod/4665565176.html");
		// CraiglistEntryParser parser = new CraiglistEntryParser(
		// "http://seattle.craigslist.org/kit/for/4667035854.html");
		if (parser.parseDoc()) {

		}
	}
}
