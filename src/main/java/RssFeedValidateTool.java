import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class RssFeedValidateTool {
	
	final static Logger logger = LogManager.getLogger(RssFeedValidateTool.class);

	public static void main(String[] args) {
		RssFeedValidateTool rssFeedValidator = new RssFeedValidateTool();		
		if(args!=null && args[0].length()>0) {
			String rssFeedUrl = args[0];
			logger.debug("Feed validation inprogress...");
			if(rssFeedValidator.validateXML(rssFeedUrl) && rssFeedValidator.validateImageURL(rssFeedUrl)) {
				logger.debug("Feed validated successfully !!");
			}else {
				logger.debug("Feed validaiton failed");
			}
		}else {
			logger.debug("Please specify the RSS Feed URL");
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
									logger.debug(entry.getTitle() + " content url invalid:: " + url);
									imgFailCnt++;
								}else {
									imgPassCnt++;
								}
							}
						}
					}
				}
			}
			logger.debug("***** Total number of image URL scanned :" +imgTtlCnt);
			logger.debug("***** Image URL failed scan :" +imgFailCnt);
			logger.debug("***** Image URL passed scan :" +imgPassCnt);
		} catch (IllegalArgumentException | FeedException | IOException e) {
			logger.error("Error occurred while validating image URL: "+ e.getMessage());
			return false;
		}
		return true;
	}

	private boolean validateXML(String rssFeedUrl) {
		try {
			
            Reader reader = new InputStreamReader(new URL(rssFeedUrl).openStream(),"UTF-8");
            InputSource xmlSource = new InputSource(reader);
            xmlSource.setEncoding("UTF-8");                

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		    docBuilder.parse(xmlSource);
		   
		} catch (ParserConfigurationException | SAXException | IOException e) {
			logger.error("Error occurred while validating XML: "+ e.getMessage());
			return false;
		}
		return true;
	}
	
	
}
