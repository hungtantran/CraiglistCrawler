package marijuanaCrawler;

public interface EntryLinkCrawler {
	// Given a seed url, start up the entry crawler.
	// Return false if startup process fails. Return true otherwise.
	public boolean startUp();
	
	// Provide the crawler with a query term to find links.
	public boolean setQueryTerm(String queryTerm);
	
	// Return the next entry link.
	// Return null if there is no more entry link.
	public String getNextEntryLink();
}
