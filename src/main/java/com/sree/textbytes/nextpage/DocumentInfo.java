package com.sree.textbytes.nextpage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.select.Elements;


/**
 * Document info class which holds all the information
 * 
 * @author sree
 *
 */
public class DocumentInfo {
	/**
	 * Input url passed in
	 */
	private String url;

	/**
	 * All the parsed pages , in case if there any next page links
	 */
	private Set<String> parsedPages = new HashSet<String>();
	
	/**
	 * holds the original unmodified HTML retrieved from the URL
	 */
	private String rawHtml;

	/**
	 * Link elements containing all the links from a document. Used for Next page find
	 */
	private Elements linkElements;
	
	/**
	 * List of next pages found
	 */
	List<String> nextPageLinks = new ArrayList<String>();
	
	/**
	 * Its true of the document has next pages.
	 */
	private boolean isMultiPage = false;
	
	/**
	 * Set the next pages 
	 * 
	 * @param nextPageLink
	 */
	public void setNextPageLinks(String nextPageLink) {
		nextPageLinks.add(nextPageLink);
	}
	
	/**
	 * Get the next pages
	 * 
	 * @return
	 */
	
	public List<String> getNextPageLinks() {
		return nextPageLinks;
	}
	
	/**
	 * Set the link elements , which is used fo next page finding
	 * 
	 * @param linkElements
	 */
	public void setLinkElements(Elements linkElements) {
		this.linkElements = linkElements;
	}
	
	/**
	 * Get the link elements  
	 * 
	 * @return
	 */
	public Elements getLinkElements() {
		return linkElements;
	}
	
	public void setMultiPageStatus(boolean status) {
		this.isMultiPage = status;
	}
	
	public boolean getMultiPageStatus() {
		return isMultiPage;
	}
	
	public void setUrl(String url) {
		this.url = url; 
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setParsedPages(String parsedPage) {
		this.parsedPages.add(parsedPage);
	}
	
	public Set<String> getParsedPages() {
		return parsedPages;
	}

	public String getRawHtml() {
		return rawHtml;
	}

	public void setRawHtml(String rawHtml) {
		this.rawHtml = rawHtml;
	}



}
