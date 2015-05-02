package marijuanaCrawler;

// General interface for all customer crawlers whose jobs is to query list of links
// to entry page that have the actual posting of interest.

public interface IEntryLinkCrawler {
	// Given a seed url, start up the entry crawler.
	// Return false if startup process fails. Return true otherwise.
	public boolean startUp();

	// Provide the crawler with a query term to find links.
	public boolean setQueryTerm(String queryTerm);

	// Return the next entry link.
	// Return null if there is no more entry link.
	public String getNextEntryLink() throws Exception;
}
