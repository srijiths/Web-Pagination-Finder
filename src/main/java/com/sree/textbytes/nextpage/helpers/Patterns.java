package com.sree.textbytes.nextpage.helpers;

import java.util.regex.Pattern;
/**
 * 
 * @author Java-Readability
 *
 */
public class Patterns {
	public static Pattern PAGE_NUMBER_LIKE = compilePattern("((_|-)?p[a-z]*|(_|-))[0-9]{1,2}$");
	public static Pattern NEXT_LINK = compilePattern("(story-paging|next|weiter|continue|>([^\\|]|$)|»([^\\|]|$))");
	public static Pattern PAGINATION = compilePattern("pag(e|ing|inat)");
	public static Pattern NEGATIVE = compilePattern("(combx|comment|com-|contact|foot|footer|footnote|masthead|media|meta|outbrain|promo|related|scroll|shoutbox|sidebar|sponsor|shopping|tags|tool|widget)");
    public static Pattern PREV_LINK = compilePattern("(prev|earl|old|new|<|«)");
    public static Pattern POSITIVE = compilePattern("(article|body|content|entry|hentry|main|page|pagination|post|text|blog|story)");
    public static Pattern PAGE_AND_NUMBER = compilePattern("p(a|g|ag)?(e|ing|ination)?(=|/)[0-9]{1,2}");
    public static Pattern PAGE_OR_PAGING = compilePattern("(page|paging)");
    public static Pattern EXTRANEOUS = compilePattern("print|archive|comment|discuss|e[\\-]?mail|share|reply|all|login|sign|single");
    public static Pattern DIGIT = Pattern.compile("\\d");
    public static Pattern FIRST_OR_LAST = compilePattern("(first|last)");
    
    private static Pattern compilePattern(String patternString) {
        return Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
        
    }

    public static boolean exists(Pattern pattern, String string) {
        return pattern.matcher(string).find();
    }
    
    public static boolean match(Pattern pattern, String string) {
        return pattern.matcher(string).matches();
    }

}
