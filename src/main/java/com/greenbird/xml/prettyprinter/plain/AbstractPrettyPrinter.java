/***************************************************************************
 * Copyright 2014 greenbird Integration Technology, http://www.greenbird.com/
 *
 * This file is part of the 'xml-formatter' project available at
 * http://greenbird.github.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.greenbird.xml.prettyprinter.plain;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;

import com.greenbird.xml.prettyprinter.PrettyPrinter;

public abstract class AbstractPrettyPrinter implements PrettyPrinter {

	public enum Type {
		/** public for testing */
		INCREMENT(), DECREMENT(), NEITHER();
		
		private Type() {
		}
	}

	/** enum for flows where characters at end element are not handled in the same way for cdata, comments and text nodes */
	public enum CharactersType {
		/** public for testing */
		NONE(), CDATA(), COMMENT();
		
		private CharactersType() {
		}
	}

	protected static final char[][] tabIndentations;
	protected static final char[][][] spaceIndentations;

	public static final char defaultIndentationCharacter = '\t';
	public static final int defaultIndentationMultiplier = 1;
	public static final int defaultIndentationDepth = 64;

	static {
		tabIndentations = getIndentations(defaultIndentationDepth, defaultIndentationCharacter, defaultIndentationMultiplier);
		
		spaceIndentations = new char[4][][];
		for(int i = 0; i < spaceIndentations.length; i++) {
			spaceIndentations[i] = getIndentations(defaultIndentationDepth, ' ', i + 1);
		}

		// reduce number of objects somewhat
		char[][] commonSpaceIndentations = new char[defaultIndentationDepth * spaceIndentations.length + 1][];
		for(int i = 0; i < spaceIndentations.length; i++) {
			for(int k = 0; k < spaceIndentations[i].length; k++) {
				if(commonSpaceIndentations[spaceIndentations[i][k].length] != null) {
					if(spaceIndentations[i][k].length != commonSpaceIndentations[spaceIndentations[i][k].length].length) {
						throw new IllegalArgumentException();
					}
					spaceIndentations[i][k] = commonSpaceIndentations[spaceIndentations[i][k].length];
				} else {
					commonSpaceIndentations[spaceIndentations[i][k].length] = spaceIndentations[i][k];
				}
			}
			
		}
		
	}
			
	private static char[][] getIndentations(int size, char character, int multiplier) {
		
		final char[][] indentations = new char[size][];
		for(int i = 0; i < indentations.length; i++) {
			indentations[i] = new char[(i * multiplier) + 1];

			indentations[i][0] = '\n';

			for(int k = 1; k < indentations[i].length; k++) {
				indentations[i][k] = character;
			}
		}
		
		return indentations;
	}

	
	protected final boolean declaration;
	protected final int maxTextNodeLength; // not always in use, if so set to max int
	protected final int maxCDATANodeLength;  // not always in use, if so set to max int
	
	protected final char indentationCharacter;
	protected final int indentationMultiplier;
	protected volatile char[][] indentations;
	
	public AbstractPrettyPrinter(boolean declaration) {
		this(declaration, Integer.MAX_VALUE, Integer.MAX_VALUE, defaultIndentationCharacter, defaultIndentationMultiplier);
	}

	public AbstractPrettyPrinter(boolean declaration, char indentationCharacter, int indentationMultiplier) {
		this(declaration, Integer.MAX_VALUE, Integer.MAX_VALUE, indentationCharacter, indentationMultiplier);
	}

	public AbstractPrettyPrinter(boolean declaration, int maxTextNodeLength, int maxCDATANodeLength, char indentationCharacter, int indentationMultiplier) {
		this(indentationCharacter, indentationMultiplier, declaration, maxTextNodeLength, maxCDATANodeLength);
	}

	public AbstractPrettyPrinter(char indentationCharacter, int indentationMultiplier, boolean declaration, int maxTextNodeLength, int maxCDATANodeLength) {
		this.indentationCharacter = indentationCharacter;
		this.indentationMultiplier = indentationMultiplier;
		
		if(indentationCharacter == '\t' && indentationMultiplier == 1) {
			this.indentations = tabIndentations;
		} else if(indentationCharacter == ' ' && indentationMultiplier <= 4 && indentationMultiplier >= 1) {
			this.indentations = spaceIndentations[indentationMultiplier - 1];
		} else {
			this.indentations = getIndentations(defaultIndentationDepth, indentationCharacter, indentationMultiplier);
		}
		
		this.declaration = declaration;
		
		if(maxTextNodeLength < -1) {
			throw new IllegalArgumentException();
		}
		if(maxCDATANodeLength < -1) {
			throw new IllegalArgumentException();
		}
		if(maxCDATANodeLength == -1 && maxTextNodeLength == -1) {
			throw new IllegalArgumentException("No max node length");
		}
		
		if(maxTextNodeLength == -1) {
			this.maxTextNodeLength = Integer.MAX_VALUE;
		} else {
			this.maxTextNodeLength = maxTextNodeLength;
		}
		if(maxCDATANodeLength == -1) {
			this.maxCDATANodeLength = Integer.MAX_VALUE;
		} else {
			this.maxCDATANodeLength = maxCDATANodeLength;
		}
	}
	
	/**
	 * Return array of indentations
	 * 
	 * @param size depth of XML tree
	 * @return array of indentation character arrays
	 */
	
	public char[][] ensureCharCapacity(int size) {

		final char[][] currentIdentactions = indentations;
		if(size < currentIdentactions.length) {
			return currentIdentactions;
		}

		final char[][] nextIndentations = new char[size][];
		
		// reuse existing values
		System.arraycopy(currentIdentactions, 0, nextIndentations, 0, currentIdentactions.length);

		for(int i = currentIdentactions.length; i < nextIndentations.length; i++) {
			nextIndentations[i] = new char[(i * indentationMultiplier) + 1];

			nextIndentations[i][0] = '\n';

			for(int k = 1; k < nextIndentations[i].length; k++) {
				nextIndentations[i][k] = indentationCharacter;
			}
		}
		
		// save in field
		this.indentations = nextIndentations;
		
		return nextIndentations;
	}

	
	public boolean isXmlDeclaration() {
		return declaration;
	}
	
	public int getMaxCDATANodeLength() {
		if(maxCDATANodeLength == Integer.MAX_VALUE) {
			return -1;
		}
		return maxCDATANodeLength;
	}
	
	public int getMaxTextNodeLength() {
		if(maxTextNodeLength == Integer.MAX_VALUE) {
			return -1;
		}
		return maxTextNodeLength;
	}

	public boolean process(String xmlString, StringBuilder output) {
		
		char[] chars = xmlString.toCharArray();
		
		return process(chars, 0, chars.length, output);
	}
	
	public boolean process(Reader reader, int length, StringBuilder output) throws IOException {
		if(!reader.markSupported()) {
			throw new IllegalArgumentException("Reader of class " + reader.getClass().getName() + " does not support mark/reset");
		}
		reader.mark(length);
		
		char[] chars = new char[length];

		int offset = 0;
		int read;
		do {
			read = reader.read(chars, offset, length - offset);
			if(read == -1) {
				throw new EOFException("Expected reader with " + length + " characters");
			}

			offset += read;
		} while(offset < length);

		boolean success = process(chars, 0, chars.length, output);
		
		reader.reset();
		
		return success;
	}


	protected boolean isIndentationWhitespace(char c) {
		return c == ' ' || c == '\t' || c == '\n' || c == '\r';
	}
	
	/**
	 * Scan from end element start to end element end, plus one.
	 * 
	 * @param chars XML data
	 * @param offset start offset within XML data
	 * @param limit end offset within XML data
	 * 
	 * @return offset one character past the end element
	 */
	

	protected static int scanBeyondEndElement(final char[] chars, int offset, int limit) {
		// scan to end of end tag + 1
		offset += 3; // an end element must be at least 4 chars.
		
		return scanElementEnd(chars, offset, limit) + 1;
	}

	protected static int scanElementEnd(final char[] chars, int offset, int limit) {
		while(offset < limit) {
			if(chars[offset] == '>') {
				return offset;
			}
			offset++;
		}
		throw new ArrayIndexOutOfBoundsException("Unable to find end of end element");
	}
	
	/**
	 * 
	 * Scan from start element start to start element end, plus one.
	 * 
	 * @param chars XML data
	 * @param offset start offset within XML data
	 * @param limit end offset within XML data
	 * 
	 * @return offset  one character past the start tag
	 */

	protected static int scanBeyondStartElement(final char[] chars, int offset, int limit) {
		// scan to end of start tag + 1
		offset += 2; // an start element must be at least 3 chars.
		
		return scanStartElementEnd(chars, offset, limit) + 1;
	}

	/**
	 * 
	 * Scan start element end, plus one.
	 * 
	 * @param chars XML data
	 * @param offset start offset within XML data
	 * @param limit end offset within XML data
	 * 
	 * @return offset one character past the start tag
	 */

	protected static int scanBeyondStartElementEnd(final char[] chars, int offset, int limit) {
		return scanStartElementEnd(chars, offset, limit) + 1;
	}

	protected static int scanStartElementEnd(final char[] chars, int offset, int limit) {
		while(offset < limit) {
			if(chars[offset] == '>') {
				return offset;
			}
			offset++;
		}
		throw new ArrayIndexOutOfBoundsException("Unable to find end of start element");
	}

	protected static int scanBeyondDTDEnd(final char[] chars, int offset, int limit) {
		return scanDTDEnd(chars, offset, limit) + 1;
	}

	protected static int scanDTDEnd(final char[] chars, int offset, int limit) {
		
		// assume DTD are nested structures
		// simplified scan loop
		int level = 1;
		
		do {
			offset++;
			if(chars[offset] == '<') {
				level++;
			} else if(chars[offset] == '>') {
				level--;
			}
			
		} while(level > 0);

		return offset;
	}

	/**
	 * Scan from processing instruction start to processing instruction end, plus one.
	 * 
	 * @param chars XML data
	 * @param offset start offset within XML data
	 * @param limit end offset within XML data
	 * 
	 * @return offset one character past the processing instruction end
	 */
	
	protected static int scanBeyondProcessingInstruction(final char[] chars, int offset, int limit) {
		offset += 3; // // a processing must be at least 4 chars. <? >
		
		return scanProcessingInstructionEnd(chars, offset, limit) + 1;
	}

	protected static int scanProcessingInstructionEnd(final char[] chars, int offset, int limit) {
		while(offset < limit) {
			if(chars[offset] == '>' && chars[offset - 1] == '?') {
				return offset;
			}
			offset++;
		}
		throw new ArrayIndexOutOfBoundsException("Unable to find end of processing instruction");
	}
	
	/**
	 * Scan from CDATA start to CDATA end, plus one.
	 * 
	 * @param chars XML data
	 * @param offset start offset within XML data
	 * @param limit end offset within XML data
	 * 
	 * @return offset one character past the CDATA end
	 */

	protected static int scanBeyondCData(final char[] chars, int offset, int limit) {
		offset += 11; // // a CDATA node must be at least 12 chars. <![CDATA[]]>
		
		return scanCDataEnd(chars, offset, limit) + 1;
	}

	/**
	 * 
	 * Scan one past CDATA end
	 * 
	 * @param chars XML data
	 * @param offset start offset within XML data
	 * @param limit end offset within XML data
	 * 
	 * @return offset one character past the CDATA end
	 * 
	 */
	
	protected static int scanBeyondCDataEnd(final char[] chars, int offset, int limit) {
		return scanCDataEnd(chars, offset, limit) + 1;
	}
	
	protected static int scanCDataEnd(final char[] chars, int offset, int limit) {
		while(offset < limit) {
			if(chars[offset] == '>' && chars[offset - 1] == ']' && chars[offset - 2] == ']') {
				return offset;
			}
			offset++;
		}
		throw new ArrayIndexOutOfBoundsException("Unable to find end of CDATA");
	}

	protected static int scanCommentEnd(final char[] chars, int offset, int limit) {
		while(offset < limit) {
			if(chars[offset] == '>' && chars[offset - 1] == '-' && chars[offset - 2] == '-') {
				return offset;
			}
			offset++;
		}
		throw new ArrayIndexOutOfBoundsException("Unable to find end of comment");
	}

	/**
	 * Scan from comment start to comment end, plus one.
	 * 
	 * @param chars XML data
	 * @param offset start offset within XML data
	 * @param limit end offset within XML data
	 * 
	 * @return offset one character past the comment end
	 */
	
	protected static int scanBeyondComment(final char[] chars, int offset, int limit) {
		// scan to end of comment + 1
		offset += 6; // a comment must be at least 7 chars. <!-- -->
		
		return scanCommentEnd(chars, offset, limit) + 1;
	}
	
	protected static int scanBeyondCommentEnd(final char[] chars, int offset, int limit) {
		// scan to end of comment + 1
		return scanCommentEnd(chars, offset, limit) + 1;
	}

	protected static int skipSubtree(final char[] chars, int offset, int limit) {
		
		int level = 0;
		
		while(offset < limit) {
			
			if(chars[offset] == '<') {
				switch(chars[offset + 1]) {
					case '/' : {  // end tag
						level--;
	
						if(level < 0) {
							return offset;
						}
						
						offset = scanBeyondEndElement(chars, offset, limit);
						
						continue;
					}
					case '!': {
						// complete cdata and comments so nodes
						
						if(chars[offset + 2] == '-') {
							// look for -->
							offset = scanBeyondComment(chars, offset, limit);
							
							continue;
						} else if(chars[offset + 2] == '[') {
							// look for ]]>
							offset = scanBeyondCData(chars, offset, limit);
							
							continue;
						} else {
							// do nothing
						}
						break;
					}
					case '?' : {
						// processing instruction
						offset = scanBeyondProcessingInstruction(chars, offset, limit);
						
						continue;
					} 
					default : {
						// start element
						// flush bytes
						level++;
	
						// scan to end of start element to see if empty element
						offset += 2; // skip <a in <a>
						while(offset < limit) {
							if(chars[offset] == '>') {
								if(chars[offset - 1] == '/') {
									// empty element
									level--;
								}
								
								offset++;
								
								break;
							}
							offset++;
						}
						
						continue;
					}
				}	
			}
			
			offset++;
		}
		
		return offset;

	}

	protected static boolean isEscapedXML(final char[] chars, int offset, int sourceStart) {
		// An escaped minimum xml length is <x/> in which encoded size is 4 + x + / + 4 = 10
		// however the end tag is not strictly needed in escaped form, so it is 4 + x + / + > = 7

		return offset - sourceStart >= 7 
				// check for start and end tag (escaped)
				&& chars[sourceStart] == '&' // start escape start
				&& chars[sourceStart+3] == ';' // start escape end
				&& chars[sourceStart + 1] == 'l' 
				&& chars[sourceStart+2] == 't' // start lt

				&& (
						chars[offset - 1] == '>' // unescaped end

						|| (	// or escaped end
								chars[offset - 4] == '&' // end escape start
								&& chars[offset - 1] == ';' // end escape end
								&& chars[offset - 2] == 't' 
								&& chars[offset - 3] == 'g' // end gt
								)
						);
		
		// assume method is inlined 
	}

	protected boolean isXMLDeclaration(final char[] chars, int sourceStart, int sourceEnd) {
		return sourceStart < sourceEnd - 6 && chars[sourceStart + 2] == 'x' && chars[sourceStart + 3] == 'm' && chars[sourceStart + 4] == 'l' && Character.isWhitespace(chars[sourceStart + 5]);
		// assume method is inlined 
	}

	protected boolean appendEntity(int entityLength, char[] chars, int entityIndex, StringBuilder buffer) {
		
		switch(entityLength) {
		case 2: {
			
			if(chars[entityIndex] == 'g' && chars[entityIndex+1] == 't') {
				buffer.append('>');
				
				return true;
			} else if(chars[entityIndex] == 'l' && chars[entityIndex+1] == 't') {
				buffer.append('<');
				
				return true;
			}

			break;
		}
		
		case 3: {
			if(chars[entityIndex] == 'a' && chars[entityIndex+1] == 'm' && chars[entityIndex+2] == 'p') {
				buffer.append('&');
				
				return true;
			}
			break;
		}
		case 4: {
			if(chars[entityIndex] == 'a' && chars[entityIndex+1] == 'p' && chars[entityIndex+2] == 'o' && chars[entityIndex+3] == 's') {
				buffer.append('\'');
				
				return true;
			} else if(chars[entityIndex] == 'q' && chars[entityIndex+1] == 'u' && chars[entityIndex+2] == 'o' && chars[entityIndex+3] == 't') {
				buffer.append('"');
				
				return true;
			}
			break;
		}
		}
		return false;
	}

}
