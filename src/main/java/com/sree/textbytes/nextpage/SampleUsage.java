package com.sree.textbytes.nextpage;

import java.util.List;

import com.sree.textbytes.network.HtmlFetcher;

public class SampleUsage {

	public static void main(String[] args) throws Exception {
		HtmlFetcher htmlFetcher = new HtmlFetcher();
        String html = htmlFetcher.getHtml("http://www.washingtontimes.com/news/2012/aug/29/american-scene-cdc-says-west-nile-cases-rise-40-in/?page=1&utm_medium=RSS&utm_source=RSS_Feed", 100000);
        
        MultiPageLink multiPageLink = new MultiPageLink();
        DocumentInfo documentInfo = new DocumentInfo();
        documentInfo = multiPageLink.checkMultiplePages("http://www.washingtontimes.com/news/2012/aug/29/american-scene-cdc-says-west-nile-cases-rise-40-in/?page=1&utm_medium=RSS&utm_source=RSS_Feed", html);
        if(documentInfo.getMultiPageStatus()) {
        	List<String> nextPages = documentInfo.getNextPageLinks();
        	if(nextPages.size() > 0) {
        		for(String nextPage : nextPages) {
        			System.out.println("Next Page Link : "+nextPage);
        		}
        	}
        }

	}

}
