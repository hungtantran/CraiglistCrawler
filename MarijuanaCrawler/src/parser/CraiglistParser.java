package parser;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import commonlib.Globals;
import commonlib.Helper;
import commonlib.NetworkingFunctions;

import dbconnection.PostingLocation;

public class CraiglistParser implements IPostingLocationParser {
	private static final int NUM_RETRIES_DOWNLOAD_REPLY_LINK = 2; 
	
	private String domain = null;
	private String html = null;
	private Document doc = null;
	private PostingLocation postingLocation = null;
	
	public CraiglistParser() {
	}
	
	@Override
	public void SetHTML(String domain, String html) throws Exception {
		this.domain = domain;
		this.html = html;
		this.doc = Jsoup.parse(this.html);
		this.postingLocation = new PostingLocation();
	}
	@Override
	public void SetDoc(String domain, Document doc) {
		this.domain = domain;
		this.html = doc.html();
		this.doc = doc;
		this.postingLocation = new PostingLocation();
	}
	
	@Override
	public PostingLocation Parse() {
		return this.postingLocation;
	}
	
	@Override
	public String ParsePostingBody() {
		Elements postingBodies = this.doc.select("section[id=postingBody]");
		if (postingBodies.size() != 1) {
	        return null;
		}
		
		String postingBody = Helper.cleanPostingBody(postingBodies.get(0).html());
		return postingBody;
	}
	
	@Override
	public String ParsePostingTitle() {
		Elements postingTitles = this.doc.select("h2[class=postingtitle]");
		if (postingTitles.size() != 1) {
	        return null;
	    }

		String postingTitle = Helper.cleanNonCharacterDigit(postingTitles.get(0).text());
		return postingTitle;
	}

	@Override
	public String ParseEmail() throws Exception {
		Elements replyLinks = this.doc.select("a[id=replylink]");
		if (replyLinks.size() != 1) {
			Globals.crawlerLogManager.writeLog("Found no contact");
	        return "NoEmail";
	    }
		
		String replyLink = this.domain + replyLinks.get(0).attr("href");
		Globals.crawlerLogManager.writeLog("Try to parse reply email for " + replyLink);
		Helper.waitSec(Globals.DEFAULTLOWERBOUNDWAITTIMESEC, Globals.DEFAULTUPPERBOUNDWAITTIMESEC);

		try {
			final Document replyDoc = NetworkingFunctions.downloadHtmlContentToDoc(replyLink, NUM_RETRIES_DOWNLOAD_REPLY_LINK);
			Elements emailElems = replyDoc.select("div[class=anonemail]");
			if (emailElems.size() != 1) {
				Globals.crawlerLogManager.writeLog("Found no email");
				return "NoEmail";
			}
			
			String email = emailElems.get(0).text();
			Globals.crawlerLogManager.writeLog("Found email " + email);
			return email;
		} catch (Exception e) {
			Globals.crawlerLogManager.writeLog(e.getMessage());
			
			if (e instanceof HttpStatusException) {
				int statusCode = ((HttpStatusException) e).getStatusCode();
				// If the page doesn't exists, email is "None"
				if (statusCode == 404) {
					Globals.crawlerLogManager.writeLog("Post expired");
					return "Expired";
				}
			}
			
			throw e;
		}
	}
}
