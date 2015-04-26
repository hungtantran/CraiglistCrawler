package parser;

import dbconnection.LocalBusiness;

public interface ILocalBusinessParser {
	public void SetHTML(String html) throws Exception;
	
	public LocalBusiness Parse();
}
