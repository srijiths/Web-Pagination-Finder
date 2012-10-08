package com.sree.textbytes.nextpage;

/**
 * 
 * Page link info 
 *
 */

public class PageLinkInfo {
   private double score;
   private String linkText;
   private String href;
   public PageLinkInfo(double score, String linkText, String href) {
       this.score = score;
       this.linkText = linkText;
       this.href = href;
   }
   public void setScore(double score) {
       this.score = score;
   }
   public void incrementScore(double incr) {
       score = score + incr;
   }
   public void setLinkText(String linkText) {
       this.linkText = linkText;
   }
   public double getScore() {
       return score;
   }
   public String getLinkText() {
       return linkText;
   }
   public String getHref() {
       return href;
   }
   public String toString() {
       return "PageLinkInfo [score=" + score + ", linkText=" + linkText + ", href=" + href + "]";
   }
}