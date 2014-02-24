package com.sree.textbytes.nextpage;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.sree.textbytes.network.HtmlFetcher;
import com.sree.textbytes.nextpage.helpers.string;

/**
 * Class to find out multi page links and its html and store it as {@link MultiPageInfo}
 * @author sree
 *
 */

public class MultiPageLink {
	
	public DocumentInfo documentInfo = new DocumentInfo();
	
	public Logger logger = Logger.getLogger(MultiPageLink.class.getName());
	
	NextPageLink nextPageLink = new NextPageLink();
	
	public DocumentInfo checkMultiplePages(String url,String html) throws Exception {
		if(!string.isNullOrEmpty(url)) {
			documentInfo.setUrl(url);
			documentInfo.setParsedPages(normalizeTrailingSlash(url));
			logger.debug("URL is not empty : "+documentInfo.getUrl());
		}else 
			throw new RuntimeException();
		String nextPage = null;
		
		documentInfo.setRawHtml(html);
		documentInfo.setLinkElements(getLinkElements(html, url));
		
		nextPage = nextPageLink.findNextPageLink(documentInfo.getLinkElements(), documentInfo);
		
		if(!string.isNullOrEmpty(nextPage)) {
			documentInfo.setMultiPageStatus(true);
			documentInfo.setParsedPages(nextPage);
			checkNextPageLinks(nextPage);
		}

		return documentInfo;
	}
	
	/**
	 * recursive call to next pages until next page is null
	 * 
	 * @param initialLink
	 */
	
	private void checkNextPageLinks(String initialLink) {
		String html = null;
		try {
			html = processLinks(initialLink);
			Elements linkElements = getLinkElements(html, initialLink);
			
			String nextPage = nextPageLink.findNextPageLink(linkElements, documentInfo);
			
			if(!string.isNullOrEmpty(nextPage)) {
				documentInfo.setParsedPages(nextPage);
				checkNextPageLinks(nextPage);
			}else 
				return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Get the link elements from the document for next page search
	 * 
	 * @param html
	 * @param url
	 * @return
	 */
	
	private Elements getLinkElements(String html,String url) {
		Document nekoDocument = parseHtml(html, url);
		Elements linkElements = nekoDocument.body().getElementsByTag("a");

		return linkElements;
	}
	
	/**
	 * Procss the next page link, get the html source and create a DOM Document and store it
	 * @param url
	 * @return
	 * @throws Exception
	 */
	private String processLinks(String url) throws Exception {
		HtmlFetcher htmlFetcher = new HtmlFetcher();
		String html = htmlFetcher.getHtml(url,0);
		
		try {
			documentInfo.setNextPageLinks(url);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
		
		return html;
	}
	
	/**
	 * parse the next page html pages
	 * 
	 * @param rawHtml
	 * @param url
	 * @return
	 */
	private Document parseHtml(String rawHtml,String url) {
		Document document = null;
		try {
			document = Jsoup.parse(rawHtml, url);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return document;
	}
	
    private String normalizeTrailingSlash(String url) {
        return url.replaceAll("/$", "");
    }

}
