package com.flatironschool.javacs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import org.jsoup.select.Elements;

public class WikiPhilosophy {
	
	final static WikiFetcher wf = new WikiFetcher();
	
	/**
	 * Tests a conjecture about Wikipedia and Philosophy.
	 * 
	 * https://en.wikipedia.org/wiki/Wikipedia:Getting_to_Philosophy
	 * 
	 * 1. Clicking on the first non-parenthesized, non-italicized link
     * 2. Ignoring external links, links to the current page, or red links
     * 3. Stopping when reaching "Philosophy", a page with no links or a page
     *    that does not exist, or when a loop occurs
	 * 
	 * @param args
	 * @throws IOException
	 */
		
	
	//if the url links to a non-existent page, the method will throw an io exception
	//perhaps, later I should try to catch the exception with a custom message
	public static void paraSearch(String url) throws IOException {
		
		List<String> validLinks = new ArrayList<>();
		boolean breakBoolean = false;
		boolean validLinkAvail = false;
		
		while(true) {
            	
		      System.out.println(url);	
			//this handles the fact that the link needs to be in the main text,
			//not in the sidebar or boxout
    			Elements paragraphs = wf.fetchWikipedia(url, "mw-content-text", "p");
			
			//if there is not a valid link in the first main-text paragraph,
			//search in the other paragraps as well 
			for(int i = 0; i < paragraphs.size(); i++) {
    				Element para = paragraphs.get(i);
		
				Iterable<Node> iter = new WikiNodeIterable(para);
    				for (Node node: iter) {
				
					String linkHref = node.attr("href");
					
					//if curLink makes you enter a loop, break out of all three for loops
					//(if the link is to the current page, it will be skipped over; will not cause the program to exit)
					if(!previousUseCheck(validLinks, linkHref)) {
						i = paragraphs.size();
						breakBoolean = true;
						break;
					}
				
					//valid link found
					if(externalLinkCheck(linkHref) && uppercaseCheck(node) &&
						italicsCheck(node) && redLinkCheck(node) &&
						parenthesesCheck(para, linkHref)) {
				 	
						url = "https://en.wikipedia.org";
						url += linkHref; 
						validLinkAvail = true;
						validLinks.add(linkHref);
														
						//break out of both loops
						i = paragraphs.size();
						break;
					}		
           	 		}
		    	}
		 
		 
			//exit - you entered a loop
			if(breakBoolean == true) { 
				System.out.println(url);
				System.out.print("Failure: Entered loop");
				break;
			}
			
    		 	 //exit - there is no valid link on page
    		  	if(validLinkAvail == false) {
    			  	System.out.print("Failure: No valid links on page");
    			  	break;
    		  	}
		  
		  	//reset the boolean variable
    		  	validLinkAvail = false;
			
			//exit - you reached philosophy
			if(url.equals("https://en.wikipedia.org/wiki/Philosophy")) {
				//success message
				System.out.println(url);
				System.out.print("Success: Philosophy reached");
				break;
			}
		}	
	}
	
	//For all of these check methods: TRUE means the link passed the check for validity
	
	public static boolean uppercaseCheck(Node node) {
		String nodeChild = node.childNode(0).toString();
		if(nodeChild.toLowerCase().equals(nodeChild)) { return true; }
		else { return false; }
	}
	
	public static boolean externalLinkCheck(String linkHref) {
		if(linkHref.contains("/wiki/")) { return true; }
		else { return false; }
	}
	
	public static boolean previousUseCheck(List<String> clickedLinks, String curLink) {
		for(int i = 0; i < clickedLinks.size(); i++) {
			if(curLink.equals(clickedLinks.get(i))) { return false; }
		}
		return true;
	}
	
	public static boolean italicsCheck(Node node) {
		if(node.outerHtml().contains("<i>") || node.outerHtml().contains("<em>")) { 
			return false; 
		} else { return true; }
	}
	
	//did not test this - may not work
	public static boolean redLinkCheck(Node node) {
		if(node.outerHtml().contains("=red") || node.outerHtml().contains("= red")) { return false; }
		else { return true; }
	}
	
	public static boolean parenthesesCheck(Element para, String linkHref) {
		String paraStr = para.toString();
		int linkIndex = paraStr.indexOf(linkHref);
		
		int leftFromIndex = 0;
		int rightFromIndex = 1;
		
		int leftParenIndex = -1;
		int rightParenIndex = -1;
		
		//if there are no parentheses, test passed
		if(paraStr.indexOf('(') == -1) { return true; }
		if(paraStr.indexOf(')') == -1) { return true; }
		
		//end loop when you have searched entire string for parentheses
		while(leftFromIndex < paraStr.length() && rightFromIndex < paraStr.length()) {
		
			//returns index of first instance of parentheses found
			leftParenIndex = paraStr.indexOf('(', leftFromIndex);
			rightParenIndex = paraStr.indexOf(')', rightFromIndex);
			
			//this should work for nested paretheses as well
			if(leftParenIndex != -1 && leftParenIndex < linkIndex && 
				rightParenIndex!= -1 && rightParenIndex > linkIndex) { 
					return false; 
			}
		
			leftFromIndex++;
			rightFromIndex++;
		}
		
		return true;	
	}
	
	
	public static void main(String[] args) throws IOException {
	  
		try {
	  		paraSearch("https://en.wikipedia.org/wiki/Java_(programming_language)");
		}
		catch(IOException e) {
			System.err.println("Failure: IOException - Invalid URL");
		}

	}
}
