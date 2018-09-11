import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jdom.Element;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class RssFeedValidateTool {

	public static void main(String[] args) {
		RssFeedValidateTool rssFeedValidator = new RssFeedValidateTool();
		System.out.println("Please enter the target RSS Feed URL :");
		if(args!=null && args[0].length()>0) {
			String rssFeedUrl = args[0];
			System.out.println("Feed validation inprogress...");
			//rssFeedValidator.validateXML(rssFeedUrl) &&
			if(rssFeedValidator.validateImageURL(rssFeedUrl)) {
				System.out.println("Feed validated successfully !!");
			}else {
				System.out.println("Feed validaiton failed");
			}
		}else {
			System.out.println("Please specify the RSS Feed URL");
		}
	}

	@SuppressWarnings("unchecked")
	private boolean validateImageURL(String rssFeedUrl) {
		try {
			URL feedUrl;
			int imgTtlCnt = 0;
			int imgPassCnt = 0;
			int imgFailCnt = 0;
			feedUrl = new URL(rssFeedUrl);
			SyndFeedInput input = new SyndFeedInput();
			SyndFeed feed = input.build(new XmlReader(feedUrl));

			for (SyndEntry entry : (List<SyndEntry>) feed.getEntries()) {
				List<Element> foreignElementList = (List<Element>) entry.getForeignMarkup();
				for (Element element : foreignElementList) {
					if (element.getAttributeValue("name") != null
							&& element.getAttributeValue("name").startsWith("article-image")) {
						for (Element childElem : (List<Element>) element.getChildren()) {
							if (childElem != null && childElem.getContentSize() > 0
									&& !childElem.getText().equals("image")) {
								String url = childElem.getContent(0).getValue();
								URL obj = new URL(url);
								HttpURLConnection con = (HttpURLConnection) obj.openConnection();
								con.setRequestMethod("GET");
								int responseCode = con.getResponseCode();
								imgTtlCnt++;
								if(responseCode == 404) {
									System.out.println(entry.getTitle() + " content url invalid:: " + url);
									imgFailCnt++;
								}else {
									imgPassCnt++;
								}
							}
						}
					}
				}
			}
			System.out.println("***** Total number of image URL scanned :" +imgTtlCnt);
			System.out.println("***** Image URL failed scan :" +imgFailCnt);
			System.out.println("***** Image URL passed scan :" +imgPassCnt);
		} catch (IllegalArgumentException | FeedException | IOException e) {
			System.out.println("Error occurred while validating image URL: "+ e.getMessage());
			return false;
		}
		return true;
	}

	private boolean validateXML(String rssFeedUrl) {
		try {
			InputStreamReader isr = new InputStreamReader(new URL(rssFeedUrl).openStream(), "UTF-8");
			BufferedReader in = new BufferedReader(isr);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			String inputLine;
			String wholeDocument = "";
			while ((inputLine = in.readLine()) != null) {
				// builder.parse(inputLine);
				Document doc = builder.parse(new InputSource(new StringReader(inputLine)));
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			System.out.println("Error occurred while validating XML: "+ e.getMessage());
			return false;
		}
		return true;
	}
}
