package commonlib;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;

public class NetworkingFunctions {
    public static final String[] userAgents = {
        "Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6",
        "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0)",
        "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)",
        "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0 )",
        "Mozilla/4.0 (compatible; MSIE 5.5; Windows 98; Win 9x 4.90)",
        "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.0.1) Gecko/2008070208 Firefox/3.0.1",
        "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.14) Gecko/20080404 Firefox/2.0.0.14",
        "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.13 (KHTML, like Gecko) Chrome/0.2.149.29 Safari/525.13",
        "Mozilla/4.8 [en] (Windows NT 6.0; U)",
        "Mozilla/4.8 [en] (Windows NT 5.1; U)",
        "Opera/9.25 (Windows NT 6.0; U; en)",
        "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; en) Opera 8.0",
        "Opera/7.51 (Windows NT 5.1; U) [en]",
        "Opera/7.50 (Windows XP; U)",
        "Avant Browser/1.2.789rel1 (http://www.avantbrowser.com)",
        "Mozilla/5.0 (Windows; U; Win98; en-US; rv:1.4) Gecko Netscape/7.1 (ax)",
        "Mozilla/5.0 (Windows; U; Windows XP) Gecko MultiZilla/1.6.1.0a",
        "Opera/7.50 (Windows ME; U) [en]",
        "Mozilla/3.01Gold (Win95; I)",
        "Mozilla/2.02E (Win95; U)",
        "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/125.2 (KHTML, like Gecko) Safari/125.8",
        "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/125.2 (KHTML, like Gecko) Safari/85.8",
        "Mozilla/4.0 (compatible; MSIE 5.15; Mac_PowerPC)",
        "Mozilla/5.0 (Macintosh; U; PPC Mac OS X Mach-O; en-US; rv:1.7a) Gecko/20050614 Firefox/0.9.0+",
        "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-US) AppleWebKit/125.4 (KHTML, like Gecko, Safari) OmniWeb/v563.15",
        "Mozilla/5.0 (X11; U; Linux; i686; en-US; rv:1.6) Gecko Debian/1.6-7",
        "Mozilla/5.0 (X11; U; Linux; i686; en-US; rv:1.6) Gecko Epiphany/1.2.5",
        "Mozilla/5.0 (X11; U; Linux i586; en-US; rv:1.7.3) Gecko/20050924 Epiphany/1.4.4 (Ubuntu)",
        "Mozilla/5.0 (compatible; Konqueror/3.5; Linux) KHTML/3.5.10 (like Gecko) (Kubuntu)",
        "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.19) Gecko/20081216 Ubuntu/8.04 (hardy) Firefox/2.0.0.19",
        "Mozilla/5.0 (X11; U; Linux; i686; en-US; rv:1.6) Gecko Galeon/1.3.14",
        "Mozilla/5.0 (compatible; Konqueror/3.3; Linux 2.6.8-gentoo-r3; X11;",
        "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.6) Gecko/20050614 Firefox/0.8",
        "Opera/9.52 (X11; Linux i686; U; en)",
        "Mozilla/5.0 (X11; U; FreeBSD; i386; en-US; rv:1.7) Gecko",
        "Mozilla/4.77 [en] (X11; I; IRIX;64 6.5 IP30)",
        "Mozilla/4.8 [en] (X11; U; SunOS; 5.7 sun4u)",
        "Mozilla/3.0 (compatible; NetPositive/2.1.1; BeOS)"
    };
    
	// Download the html content into a private Document variable "doc"
	public static Document downloadHtmlContentToDoc(String url, int numRetries) throws IOException {
	    Random rand = new Random(); 
	    int ranIndex = rand.nextInt(NetworkingFunctions.userAgents.length); 
	    
		for (int i = 0; i < numRetries; i++) {
			try {
				Response response = Jsoup
					.connect(url)
					.userAgent(NetworkingFunctions.userAgents[ranIndex])
					.timeout(10000).followRedirects(true).execute();
				
				Globals.crawlerLogManager.writeLog("Download successfully link " + url + " after " + numRetries + " retries with user agent " + NetworkingFunctions.userAgents[ranIndex]);
				
				return response.parse();
			} catch (IOException e) {
				// Only print out fail on the last fail
				if (i == numRetries - 1) {
				    Globals.crawlerLogManager.writeLog("Fail to download link " + url + " after " + numRetries + " retries with user agent " + NetworkingFunctions.userAgents[ranIndex]);
					Globals.crawlerLogManager.writeLog(e.getMessage());
					throw e;
				}
			}
		}

		return null;
	}
	
	// Download the html content into a private Document variable "doc"
	public static String downloadHtmlContentToString(String url, int numRetries) {
		for (int i = 0; i < numRetries; i++) {
			try {
				Response response = Jsoup
						.connect(url)
						.userAgent(
								"Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
						.timeout(10000).followRedirects(true).execute();
				return response.body();
			} catch (IOException e) {
				// Only print out fail on the last fail
				if (i == numRetries - 1) 
					Globals.crawlerLogManager.writeLog(e.getMessage());
			}
		}

		return null;
	}

	// Send an http request given url and the parameters
	public static void sendHttpRequest(String requestUrl, String urlParameters) {
		URL dbUrl;
		HttpURLConnection connection = null;

		try {
			Globals.crawlerLogManager.writeLog("Request URL = " + requestUrl);
			dbUrl = new URL(requestUrl);
			connection = (HttpURLConnection) dbUrl.openConnection();
			connection.setRequestMethod("PUT");
			connection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");

			connection.setRequestProperty("Content-Length",
					"" + Integer.toString(urlParameters.getBytes().length));
			connection.setRequestProperty("Content-Language", "en-US");

			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			// Send request
			DataOutputStream wr = new DataOutputStream(
					connection.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();

			// Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuffer response = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
		} catch (Exception e) {
			// e.printStackTrace();
			Globals.crawlerLogManager.writeLog("Request failed");
		}
	}

	public static String excuteGet(String executedUrl) {
		HttpURLConnection connection = null;

		try {
			// Create connection
			URL url = new URL(executedUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection
					.setRequestProperty("Accept",
							"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			connection.setRequestProperty("Accept-Encoding",
					"gzip,deflate,sdch");
			connection.setRequestProperty("Accept-Language",
					"en-US,en;q=0.8,de;q=0.6,vi;q=0.4");
			connection.setRequestProperty("Cache-Control", "max-age=0");
			connection.setRequestProperty("Connection", "keep-alive");
			connection
					.setRequestProperty(
							"User-Agent",
							"Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36");
			connection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");

			// connection.setUseCaches (false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			// Send request
			DataOutputStream wr = new DataOutputStream(
					connection.getOutputStream());
			wr.flush();
			wr.close();

			// Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuffer response = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
			return response.toString();

		} catch (Exception e) {
			Globals.crawlerLogManager.writeLog(e.getMessage());
			return null;

		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
}

