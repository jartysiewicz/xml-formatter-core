package com.greenbird.prettyprinter.utils;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import com.greenbird.xml.prettyprinter.PrettyPrinter;

/**
 * For testing only
 * 
 * @author thomas
 *
 */

public class XMLUtils {
	
	private static XMLInputFactory inputFactory = XMLInputFactory.newInstance();
	
	private static Set<String> wellformedSet = new HashSet<String>();

	public static String process(PrettyPrinter processor, String xml) {
		return process(processor, xml.toCharArray());
	}

	public static String process(PrettyPrinter processor, char[] chars) {
		StringBuilder buffer = new StringBuilder();
		
		if(!processor.process(chars, 0, chars.length, buffer)) {
			throw new RuntimeException("Unable to process " + processor.getClass().getName());
		}
		return buffer.toString().trim();
	}

	public static boolean attempt(PrettyPrinter processor, String xml) {
		char[] chars = xml.toCharArray();
		
		StringBuilder buffer = new StringBuilder();
		
		return processor.process(chars, 0, chars.length, buffer);
	}

	public static void checkWellformed(String out) throws Exception {

		synchronized (wellformedSet) {
			if(wellformedSet.contains(out)) {
				return;
			}
		}
		
		XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(out));
		
		do {
			reader.next();
		} while(reader.hasNext());
		
		synchronized (wellformedSet) {
			wellformedSet.add(out);
		}
	}
	
	/**
	 * 
	 * Checks indentation (over a single line - multipline text nodes is not supported)
	 * 
	 * @param out
	 * @param indentSize
	 * @return
	 * @throws Exception
	 */
	
	public static boolean isIndented(String out, int indentSize) throws Exception {
		BufferedReader reader = new BufferedReader(new StringReader(out));
		
		boolean indentated = false;
		
		int level = 0;
		int line = 0;
		
		String string = reader.readLine();
		while(string != null) {
			int newLevel = 0;
			while(newLevel < string.length()){
				if(!Character.isWhitespace(string.charAt(newLevel))) {
					break;
				}
				newLevel++;
			}
			if((newLevel % indentSize) != 0) {
				throw new IllegalArgumentException("Unexpected " + newLevel + " whitespace chars at line " + line);
			}
			if(Math.abs(level - newLevel) > indentSize) {
				throw new IllegalArgumentException("Unexpected jump from " + level + " to " + newLevel + " whitespace chars at line " + line + " for indenting with " + indentSize + " chars");
			}
			level = newLevel;
			
			string = reader.readLine();
			line++;
			
			if(level > 0) {
				indentated = true;
			}
		}
		
		if(!indentated) {
			// see if a simple xml piece
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			XMLStreamReader parser = inputFactory.createXMLStreamReader(new StringReader(out));

			int elementMaxLevel = -1;
			int elementLevel = 0;
			do {
				int event = parser.next();
				if(event == XMLStreamConstants.START_ELEMENT) {
					elementLevel++;
					
					if(elementMaxLevel < elementLevel) {
						elementMaxLevel = elementLevel;
					}
				} else if(event == XMLStreamConstants.END_ELEMENT) {
					elementLevel--;
				}
			} while(parser.hasNext());
			
			if(elementMaxLevel > 1) { // should be indentated
				return false;
			}
			return true;
		}
		
		return indentated;
	}
	
	public static boolean hasXMLDeclaration(String out) {
		return out.startsWith("<?xml ");
	}
	

}
