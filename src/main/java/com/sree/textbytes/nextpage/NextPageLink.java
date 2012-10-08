package com.sree.textbytes.nextpage;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.sree.textbytes.nextpage.helpers.Patterns;

/**
 * Main class to identify the Next page links. Concept taken from Readability
 * 
 * @author Java-Readability
 * 
 * @modified author sree
 *
 */

public class NextPageLink {
	
	public static Logger logger = Logger.getLogger(NextPageLink.class.getName());
	
    private String findBaseUrl(String stringUrl) {
        try {
            URI base = findBaseURL(stringUrl);
            return base.toString();
        } catch (URISyntaxException e) {
            logger.debug("Failed to get base URI", e);
            return null;
        }
    }

    /***
     * FInd the base url 
     * 
     * @param stringUrl
     * @return
     * @throws URISyntaxException
     */
    private URI findBaseURL(String stringUrl) throws URISyntaxException {
        //Compensate for Windows path names. 
    	stringUrl = stringUrl.replace("\\", "/");
    	int qindex = stringUrl.indexOf("?");
        if (qindex != -1) {
            stringUrl = stringUrl.substring(0, qindex);
        }
        URI url = new URI(stringUrl);
        URI baseUrl = new URI(url.getScheme(), url.getAuthority(), url.getPath(), null, null);

        /**
         * toss the leading /
         */
        String path = baseUrl.getPath().substring(1);
        String[] pieces = path.split("/");
        List<String> urlSlashes = new ArrayList<String>();
        /**
         * Reverse
         */
        for (String piece : pieces) {
            urlSlashes.add(piece);
        }
        List<String> cleanedSegments = new ArrayList<String>();
        String possibleType = "";
        boolean del;

        for (int i = 0; i < urlSlashes.size(); i++) {
            String segment = urlSlashes.get(i);
            /**
             * Split off and save anything that looks like a file type.
             */
            if (segment.indexOf(".") != -1) {
                possibleType = segment.split("\\.")[1];
                /**
                 * If the type isn't alpha-only, it's probably not actually a file extension. 
                 */
                if (!possibleType.matches("[^a-zA-Z]")) {
                    segment = segment.split("\\.")[0];
                }
            }

            /**
             * EW-CMS specific segment replacement. Ugly. Example:
             * http://www.ew.com/ew/article/0,,20313460_20369436,00.html
             **/
            if (segment.indexOf(",00") != -1) {
                segment = segment.replaceFirst(",00", "");
            }

            /**
             * If our first or second segment has anything looking like a page number, remove it.
             */

            Matcher pnMatcher = Patterns.PAGE_NUMBER_LIKE.matcher(segment);
            if (pnMatcher.matches() && ((i == 1) || (i == 0))) {
                segment = pnMatcher.replaceAll("");
            }

            del = false;
            /**
             * If this is purely a number, and it's the first or second segment, it's probably a page number.
             * Remove it.
             */
            
            if (i < 2 && segment.matches("^\\d{1,2}$")) {
                del = true;
            }
            
            /**
             * If this is the first segment and it's just "index", remove it.
             */

            if (i == 0 && segment.toLowerCase() == "index")
                del = true;
            
            /**
             * If our first or second segment is smaller than 3 characters, and the first segment was purely
             * alphas, remove it.
             */

            if (i < 2 && segment.length() < 3 && !urlSlashes.get(0).matches("[a-z]"))
                del = true;

            /**
             * If it's not marked for deletion, push it to cleanedSegments.
             */
            if (!del) {
                cleanedSegments.add(segment);
            }
        }

        String cleanedPath = "";
        for (String s : cleanedSegments) {
            cleanedPath = cleanedPath + s;
            cleanedPath = cleanedPath + "/";
        }
        URI cleaned = new URI(url.getScheme(), url.getAuthority(), "/"
                                                                   + cleanedPath.substring(0, cleanedPath
                                                                       .length() - 1), null, null);
        return cleaned;
    }
    
    /**
     * Officially parsing URL's from HTML pages is a mug's game.
     * 
     * @param url
     * @return
     */

    private String getUrlHost(String url) {
        int hostStart = url.indexOf("//");
        if (hostStart == -1) {
            return "";
        }
        int hostEnd = url.indexOf("/", hostStart + 2);
        if (hostEnd == -1) {
            return url.substring(hostStart + 2);
        } else {
            return url.substring(hostStart + 2, hostEnd);
        }

    }

    /**
     * Find the next page link by applying some heuristics
     * 
     * @param body
     * @param article
     * @return
     */
    public String findNextPageLink(Elements linkElements,DocumentInfo documentInfo) {
        Map<String, PageLinkInfo> possiblePages = new HashMap<String, PageLinkInfo>();
        Elements allLinks = linkElements;
        String articleBaseUrl = findBaseUrl(documentInfo.getUrl());
        logger.debug("Base Url : "+articleBaseUrl);
        String baseHost = getUrlHost(articleBaseUrl);

        /**
         * Loop through all links, looking for hints that they may be next-page links. Things like having
         * "page" in their textContent, className or id, or being a child of a node with a page-y className or
         * id. Also possible: levenshtein distance? longest common subsequence? After we do that, assign each
         * page a score, and
         **/
        for (Element link : allLinks) {
            String linkHref = link.attr("abs:href").replaceAll("#.*$", "").replaceAll("/$", "");
            
            /**
             * If we've already seen this page, ignore it
             */
            if ("".equals(linkHref) || linkHref.equals(articleBaseUrl) || linkHref.equals(documentInfo.getUrl()) 
            		|| documentInfo.getParsedPages().contains(linkHref)) {
            	logger.debug("parsed pages. continue"+ linkHref );
                continue;
            }
            
            if(isParsedPageSubString(linkHref, documentInfo.getParsedPages())) {
            	logger.debug("Substring matches in parsed pages : "+linkHref);
            	continue;
            }
            
            String linkHost = getUrlHost(linkHref);

            /**
             * If it's on a different domain, skip it.
             */
            if (!linkHost.equals(baseHost)) {
                continue;
            }

            String linkText = link.text(); // like innerText

            /**
             * If the linkText looks like it's not the next page, skip it.
             */
            if (Patterns.EXTRANEOUS.matcher(linkText).matches() || linkText.length() > 25) {
                continue;
            }

            /**
             * If the leftovers of the URL after removing the base URL don't contain any digits, it's
             * certainly not a next page link.
             */
            String linkHrefLeftover = linkHref.replaceFirst(articleBaseUrl, "");
            if (!Patterns.exists(Patterns.DIGIT, linkHrefLeftover)) {
                continue;
            }

            PageLinkInfo linkObj = possiblePages.get(linkHref);
            if (linkObj == null) {
                linkObj = new PageLinkInfo(0.0, linkText, linkHref);
                possiblePages.put(linkHref, linkObj);
            } else {
                String newLinkText = linkObj.getLinkText() + " | " + linkText;
                linkObj.setLinkText(newLinkText);
            }

            /**
             * If the articleBaseUrl isn't part of this URL, penalize this link. It could still be the link,
             * but the odds are lower. Example:
             * http://www.actionscript.org/resources/articles/745/1/JavaScript
             * -and-VBScript-Injection-in-ActionScript-3/Page1.html
             **/
            if (linkHref.indexOf(articleBaseUrl) != 0) {
                linkObj.incrementScore(-25);
            }

            String linkData = linkText + " " + link.className() + " " + link.id();
            if (Patterns.exists(Patterns.NEXT_LINK, linkData)) {
                linkObj.incrementScore(50);
            }
            if (Patterns.exists(Patterns.PAGINATION, linkData)) {
                linkObj.incrementScore(25);
            }
            if (Patterns.exists(Patterns.FIRST_OR_LAST, linkData)) {
            	/** -65 is enough to negate any bonuses gotten from a > or » in the text,
                 * If we already matched on "next", last is probably fine. If we didn't, then it's bad.
                 * Penalize.
                 */
                if (!Patterns.exists(Patterns.NEXT_LINK, linkObj.getLinkText())) {
                    linkObj.incrementScore(-65);
                }
            }

            if (Patterns.exists(Patterns.NEGATIVE, linkData)
                || Patterns.exists(Patterns.EXTRANEOUS, linkData)) {
                linkObj.incrementScore(-50);
            }
            if (Patterns.exists(Patterns.PREV_LINK, linkData)) {
                linkObj.incrementScore(-200);
            }

            /** If a parentNode contains page or paging or paginat */
            Element parentNode = link.parent();
            boolean positiveNodeMatch = false;
            boolean negativeNodeMatch = false;
            while (parentNode != null) {
                String parentNodeClassAndId = parentNode.className() + " " + parentNode.id();
                if (!positiveNodeMatch && Patterns.match(Patterns.PAGINATION, parentNodeClassAndId)) {
                    positiveNodeMatch = true;
                    linkObj.incrementScore(25);
                }
                if (!negativeNodeMatch && Patterns.match(Patterns.NEGATIVE, parentNodeClassAndId)) {
                    /**
                     * If this is just something like "footer", give it a negative. If it's something like
                     * "body-and-footer", leave it be.
                     */
                    if (!Patterns.exists(Patterns.POSITIVE, parentNodeClassAndId)) {
                        linkObj.incrementScore(-25);
                        negativeNodeMatch = true;
                    }
                }
                parentNode = parentNode.parent();
            }

            /**
             * If the URL looks like it has paging in it, add to the score. Things like /page/2/, /pagenum/2,
             * ?p=3, ?page=11, ?pagination=34
             **/
            if (Patterns.exists(Patterns.PAGE_AND_NUMBER, linkHref)
                || Patterns.exists(Patterns.PAGE_OR_PAGING, linkHref)) {
                linkObj.incrementScore(+25);
            }

            /** If the URL contains negative values, give a slight decrease. */
            if (Patterns.exists(Patterns.EXTRANEOUS, linkHref)) {
                linkObj.incrementScore(-15);
            }

            /**
             * Minor punishment to anything that doesn't match our current URL. NOTE: I'm finding this to
             * cause more harm than good where something is exactly 50 points. Dan, can you show me a
             * counterexample where this is necessary? if (linkHref.indexOf(window.location.href) !== 0) {
             * linkObj.score -= 1; }
             **/

            /**
             * If the link text can be parsed as a number, give it a minor bonus, with a slight bias towards
             * lower numbered pages. This is so that pages that might not have 'next' in their text can still
             * get scored, and sorted properly by score.
             **/
            boolean linkNumeric = false;
            int linkTextAsNumber = 0;

            try {
                linkTextAsNumber = Integer.parseInt(linkText);
                linkNumeric = true;
            } catch (NumberFormatException e) {
            }

            if (linkNumeric) {
                /** Punish 1 since we're either already there, or it's probably before what we want anyways.*/
                if (linkTextAsNumber == 1) {
                    linkObj.incrementScore(-10);
                } else {
                    linkObj.incrementScore(Math.max(0, 10 - linkTextAsNumber));
                }
            }
        }

        /**
         * Loop through all of our possible pages from above and find our top candidate for the next page URL.
         * Require at least a score of 50, which is a relatively high confidence that this page is the next
         * link.
         **/
        PageLinkInfo topPage = null;
        for (Map.Entry<String, PageLinkInfo> pageEntry : possiblePages.entrySet()) {
            if (pageEntry.getValue().getScore() >= 50
                && (topPage == null || topPage.getScore() < pageEntry.getValue().getScore())) {
                topPage = pageEntry.getValue();
            }
        }

        if (topPage != null) {
            String nextHref = topPage.getHref().replaceFirst("/$", "");
            if(isAdLink(nextHref)){
            	logger.debug("Next Page looks like Ad link , ignoring : "+nextHref);
            	return null;
            }else {
                logger.debug("Next page = " + nextHref);
                documentInfo.setParsedPages(nextHref);
                return nextHref;
            }
        } else {
            return null;
        }
    }

	/**
	 * get the word count of a sub string in a string
	 * 
	 * @author sree
	 * 
	 * @param string
	 * @param subString
	 * @return
	 */
	private int getWordCount(String string, String subString) {
		int count = 0;
		int index = string.indexOf(subString);
		if (index >= 0) {
			count++;
			count += getWordCount(string.substring(index + subString.length()),
					subString);
		}
		return count;
	}

	/**
	 * check whether its an Ad image / not
	 * 
	 * @author sree
	 * 
	 * @param imgUrl
	 * @return
	 */
	private boolean isAdLink(String nextPageUrl) {
		return getWordCount(nextPageUrl, "ad") >= 2;
	}
	
	/**
	 * Perform a sub string match to avoid identifying part of parsed pages as next page
	 * 
	 * http://www.washingtontimes.com/news/2012/aug/29/american-scene-cdc-says-west-nile-cases-rise-40-in/?page=3
	 * http://www.washingtontimes.com/news/2012/aug/29/american-scene-cdc-says-west-nile-cases-rise-40-in/?page=3&utm_medium=RSS&utm_source=RSS_Feed
	 * 
	 * @author sree
	 * 
	 * @param url
	 * @param parsedPages
	 * @return
	 */
	private boolean isParsedPageSubString(String url,Set<String> parsedPages) {
		boolean status = false;
        Set<String> parsedPagesSet = new HashSet<String>();
        parsedPagesSet = parsedPages; 
        Iterator pagesIterator = parsedPagesSet.iterator();
        while(pagesIterator.hasNext()) {
        	String parsedPage = pagesIterator.next().toString();
        	if(parsedPage.length() > url.length()) {
        		String subString = parsedPage.substring(0, url.length());
        		logger.debug("Substring : "+subString + " Parsed page : "+parsedPage);
        		if(subString.equals(url)) {
        			status = true;
        			break;
        		}
        	}
        }
        
        return status;

	}

}
