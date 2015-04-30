package parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import dbconnection.PostingLocation;

public class CraiglistParser implements IPostingLocationParser {
	private String html = null;
	private Document doc = null;
	private PostingLocation postingLocation = null;
	
	public CraiglistParser() {
	}
	
	@Override
	public void SetHTML(String html) throws Exception {
		this.html = html;
		this.doc = Jsoup.parse(this.html);
		this.postingLocation = new PostingLocation();
	}

	@Override
	public PostingLocation Parse() {
		return this.postingLocation;
	}
	
	public static void main(String[] args) {
	}
}
