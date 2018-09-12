package rss.feed.validator;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.ArrayUtils;
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

import rss.feed.model.CountStat;

public class RssFeedValidateTool {
	
	static final ResourceBundle resourceBundle = ResourceBundle.getBundle("rssfeedvalidator");
	static final Logger logger = LogManager.getLogger(RssFeedValidateTool.class);

	public static void main(String[] args) {
		RssFeedValidateTool rssFeedValidator = new RssFeedValidateTool();
		if (args != null && args[0].length() > 0) {
			String rssFeedUrl = args[0];
			logger.debug("Feed validation inprogress...");
			if (rssFeedValidator.validateXML(rssFeedUrl) &&
					rssFeedValidator.validateXMLElements(rssFeedUrl) && 
					rssFeedValidator.validateImageURL(rssFeedUrl)) {
				logger.debug("Feed validated successfully !!");
			} else {
				logger.debug("Feed validaiton failed");
			}
		} else {
			logger.debug("Please specify the RSS Feed URL");
		}
	}

	/**
	 * Validates the URL of the image present in the RSS Feed xml
	 * @param rssFeedUrl
	 * @return boolean
	 */
	@SuppressWarnings("unchecked")
	private boolean validateImageURL(String rssFeedUrl) {
		try {
			URL feedUrl;
			int imgTtlCnt = 0;
			int imgFailCnt = 0;
			int imgPassCnt = 0;
			feedUrl = new URL(rssFeedUrl);
			SyndFeedInput input = new SyndFeedInput();
			SyndFeed feed = input.build(new XmlReader(feedUrl));

			for (SyndEntry entry : (List<SyndEntry>) feed.getEntries()) {
				List<Element> foreignElementList = (List<Element>) entry.getForeignMarkup();
				for (Element element : foreignElementList) {
					if (element.getAttributeValue("name") != null
							&& (element.getAttributeValue("name").startsWith("article-image") ||
									element.getAttributeValue("name").equals("regular-article-thumbnail-image") || 
									element.getAttributeValue("name").contains("gallery-image"))) {
						CountStat countStat = connectToURL(element,entry);
						imgTtlCnt = imgTtlCnt + countStat.getImgTtlCnt();
						imgFailCnt = imgFailCnt + countStat.getImgFailCnt();
						imgPassCnt = imgPassCnt + countStat.getImgPassCnt();
					}
				}
			}
			logger.debug("Total number of image URL scanned :" + imgTtlCnt);
			logger.debug("Image URL failed scan :" + imgFailCnt);
			logger.debug("Image URL passed scan :" + imgPassCnt);
		} catch (IllegalArgumentException | FeedException | IOException e) {
			logger.error("Error occurred while validating image URL: " + e.getMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * Makes HTTP connection to the image URL to check for the validity
	 * @param element
	 * @param entry
	 * @return CountStat
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private static CountStat connectToURL(Element element,SyndEntry entry) throws IOException{
		CountStat countStat = new CountStat();
		for (Element childElem : (List<Element>) element.getChildren()) {
			if (childElem != null && childElem.getContentSize() > 0
					&& !childElem.getText().equals("image")) {
				String url = childElem.getContent(0).getValue();								
				if(url!=null && !url.isEmpty()) {
					URL obj = new URL(url);
					HttpURLConnection con = (HttpURLConnection) obj.openConnection();
					con.setRequestMethod("GET");
					int responseCode = con.getResponseCode();
					int imgTtlCnt = countStat.getImgTtlCnt();
					imgTtlCnt ++;
					countStat.setImgTtlCnt(imgTtlCnt);
					if (responseCode == 404) {
						logger.debug(entry.getTitle() + " content url invalid:: " + url);
						int imgFailCnt = countStat.getImgFailCnt();
						imgFailCnt++;
						countStat.setImgFailCnt(imgFailCnt);
					} else {
						int imgPassCnt = countStat.getImgPassCnt();
						imgPassCnt++;
						countStat.setImgPassCnt(imgPassCnt);					
					}
				}
			}
		}
		return countStat;
	}

	/**
	 * Parses the XML and checks for the validity
	 * @param rssFeedUrl
	 * @return boolean
	 */
	private boolean validateXML(String rssFeedUrl) {
		try {

			Reader reader = new InputStreamReader(new URL(rssFeedUrl).openStream(), "UTF-8");
			InputSource xmlSource = new InputSource(reader);
			xmlSource.setEncoding("UTF-8");
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			docBuilder.parse(xmlSource);
			logger.debug("XML validated successfully !!");
		} catch (ParserConfigurationException | SAXException | IOException e) {
			logger.error("Error occurred while validating XML: " + e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Verifies whether the Feed contains valid WCM elements
	 * @param rssFeedUrl
	 * @return boolean
	 */
	@SuppressWarnings("unchecked")
	private boolean validateXMLElements(String rssFeedUrl) {
		String[] feedElmtArr = resourceBundle.getString("feed.element.entries").split(",");
		URL feedUrl;
		try {
			if(feedElmtArr!=null && feedElmtArr.length >0) {
				feedUrl = new URL(rssFeedUrl);
				SyndFeedInput input = new SyndFeedInput();
				SyndFeed feed = input.build(new XmlReader(feedUrl));
	
				for (SyndEntry entry : (List<SyndEntry>) feed.getEntries()) {
					List<Element> foreignElementList = (List<Element>) entry.getForeignMarkup();
					for(Element element : foreignElementList) {
						if(!ArrayUtils.contains(feedElmtArr, element.getQualifiedName())) {
							logger.debug("Element not found::" +element.getQualifiedName());
							return false;
						}
					}
				}
				logger.debug("XML Elements validated Successfully !!");
			}else{
				logger.debug("There are no XML elements to validate against..");
			}
			
		} catch (IllegalArgumentException | FeedException | IOException e) {
			logger.error("Error occurred while validating the XML elements" +e.getMessage());
			return false;
		}
		return true;
	}

}
