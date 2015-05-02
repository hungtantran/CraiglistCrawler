package parser;

import org.jsoup.nodes.Document;

import dbconnection.PostingLocation;

public interface IPostingLocationParser {
	void SetHTML(String domain, String html) throws Exception;

	void SetDoc(String domain, Document doc);
	
	public PostingLocation Parse();

	public String ParsePostingBody();

	public String ParsePostingTitle();
	
	public String ParseEmail() throws Exception;
}
