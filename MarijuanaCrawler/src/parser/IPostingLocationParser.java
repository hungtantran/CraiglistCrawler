package parser;

import dbconnection.PostingLocation;

public interface IPostingLocationParser {
	public void SetHTML(String html) throws Exception;
	
	public PostingLocation Parse();
}
