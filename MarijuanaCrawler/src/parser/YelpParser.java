package parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import commonlib.Globals;

import dbconnection.LocalBusiness;

public class YelpParser implements ILocalBusinessParser {
	private String html = null;
	private Document doc = null;
	private LocalBusiness localBusiness = null;
	
	public YelpParser() {
	}
	
	@Override
	public void SetHTML(String html) throws Exception {
		this.html = html;
		this.doc = Jsoup.parse(this.html);
		this.localBusiness = new LocalBusiness();
	}

	private void parseAddress() {
		// <span itemprop="streetAddress">1602 Dexter Ave N</span>
		Elements streetAddressElems = this.doc.select("span[itemprop=streetAddress]");

		if (streetAddressElems.size() != 1) {
			return;
		}
		
		String streetAddress = streetAddressElems.get(0).text();
		
		// <span itemprop="addressLocality">Seattle</span>
		Elements addressLocalityElems = this.doc.select("span[itemprop=addressLocality]");

		if (addressLocalityElems.size() != 1) {
			return;
		}
		
		String addressLocality = addressLocalityElems.get(0).text();
		
		// <span itemprop="addressRegion">WA</span>
		Elements addressRegionElems = this.doc.select("span[itemprop=addressRegion]");

		if (addressRegionElems.size() != 1) {
			return;
		}
		
		String addressRegion = addressRegionElems.get(0).text();
		
		// <span itemprop="postalCode">98109</span>
		Elements postalCodeElems = this.doc.select("span[itemprop=postalCode]");

		if (postalCodeElems.size() != 1) {
			return;
		}
		
		String postalCode = postalCodeElems.get(0).text();
		
		String address = streetAddress + ", " + addressLocality + ", " + addressRegion + ", " + postalCode;
		
		Globals.crawlerLogManager.writeLog("Address = " + address);
		this.localBusiness.setAddress(address);
	}
	
	private void parsePhoneNumber() {
		// <span class="biz-phone" itemprop="telephone">
		Elements telephoneElems = this.doc.select("span[itemprop=telephone]");

		if (telephoneElems.size() != 1) {
			return;
		}
		
		String telephone = telephoneElems.get(0).text();
		
		Globals.crawlerLogManager.writeLog("Telephone = " + telephone);
		this.localBusiness.setPhone_number(telephone);
	}
	
	private void parseRating() {
		// <meta itemprop="ratingValue" content="5.0">
		Elements ratingElems = this.doc.select("meta[itemprop=ratingValue]");

		if (ratingElems.size() < 1) {
			this.localBusiness.setRating(-1);
			return;
		}
		
		String ratingStr = ratingElems.get(0).attr("content");
		Double rating = Double.parseDouble(ratingStr);
		int roundedRating = (int)Math.ceil(rating);
		
		Globals.crawlerLogManager.writeLog("Rating = " + roundedRating);
		this.localBusiness.setRating(roundedRating);
	}
	
	private void parseLatLng() {
		// {&#34;latitude&#34;: 47.2523803710938, &#34;longitude&#34;: -122.43920135498}
		// Parse latitude
		String latitudeMatchString = "latitude&quot;: ";
		int indexLat = this.html.indexOf(latitudeMatchString);
		if (indexLat == -1) {
			return;
		}
		
		String latitudeStr = this.html.substring(indexLat + latitudeMatchString.length());
		indexLat = latitudeStr.indexOf(",");
		if (indexLat == -1) {
			return;
		}
		
		// Only accept certain significant figures
		if (indexLat > 15) {
			indexLat = 15;
		}
		
		latitudeStr = latitudeStr.substring(0, indexLat);
		
		try {
			Double.parseDouble(latitudeStr);
			Globals.crawlerLogManager.writeLog("Latitude = " + latitudeStr);
			this.localBusiness.setLatitude(latitudeStr);
		} catch (Exception e) {
			return;
		}
		
		// Parse longitude
		String longitudeMatchString = "longitude&quot;: ";
		int indexLng = this.html.indexOf(longitudeMatchString);
		if (indexLng == -1) {
			return;
		}
		
		String longitudeStr = this.html.substring(indexLng + longitudeMatchString.length());
		indexLng = longitudeStr.indexOf("}");
		if (indexLng == -1) {
			return;
		}
		
		// Only accept certain significant figures
		if (indexLng > 15) {
			indexLng = 15;
		}
		
		longitudeStr = longitudeStr.substring(0, indexLng);
		
		try {
			Double.parseDouble(longitudeStr);
			Globals.crawlerLogManager.writeLog("Longitude = " + longitudeStr);
			this.localBusiness.setLongitude(longitudeStr);
		} catch (Exception e) {
			return;
		}
	}
	
	private void parsePostingBody() {
		Globals.crawlerLogManager.writeLog("PostingBody = <not implemented>");
	}
	
	private void parseTitle() {
		// <h1 class="biz-page-title embossed-text-white" itemprop="name">
		//    Tacoma Cross
		// </h1>
		Elements titleElems = this.doc.select("h1");

		if (titleElems.size() < 1) {
			return;
		}
		
		String title = titleElems.get(0).text().trim();
		if (title.isEmpty()) {
			return;
		}
		
		Globals.crawlerLogManager.writeLog("Title = " + title);
		this.localBusiness.setTitle(title);
	}
	
	@Override
	public LocalBusiness Parse() {
		parseAddress();
		parsePhoneNumber();
		parseRating();
		parseLatLng();
		parsePostingBody();
		parseTitle();

		return this.localBusiness;
	}
	
	public static void main(String[] args) {
		// YelpParser parser = new YelpParser();
		// parser.SetHTML("");
		// parser.Parse();
	}
}
