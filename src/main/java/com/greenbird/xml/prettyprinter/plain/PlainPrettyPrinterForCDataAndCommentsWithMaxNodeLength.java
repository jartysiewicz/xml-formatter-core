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


/**
 * 
 * XML Pretty Printer - produces reformatted XML. 
 * <br>
 * Reformat of XML within comments and CDATA is supported. 
 * <br>
 * Max text and CDATA node size is supported.
 * 
 * @author Thomas Rorvik Skjolberg
 *
 */

public class PlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength extends AbstractPrettyPrinter {

	/** pretty-print cdata */
	private final boolean cdata;
	/** pretty-print comments */
	private final boolean comment;

	public PlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength(boolean cdata, boolean comment, boolean declaration, int maxTextNodeLength, int maxCDATANodeLength, char indentationCharacter, int indentationMultiplier) {
		super(declaration, maxTextNodeLength, maxCDATANodeLength, indentationCharacter, indentationMultiplier);

		this.cdata = cdata;
		this.comment = comment;
	}
	
	public boolean process(final char[] chars, int offset, int length, final StringBuilder buffer) {
		
		/**
		 *
		 * Implementation note: cdata + comments characters handled on end element, not in uniform way,
		 * so keep track of character type.
		 */

		int bufferLength = buffer.length();
		
		final int maxTextNodeLength = this.maxTextNodeLength; // watch overflow when using arithmetic
		final int maxCDATANodeLength = this.maxCDATANodeLength; // watch overflow when using arithmetic

		CharactersType characterType = CharactersType.NONE;
		
		char[][] indentations = this.indentations;

		// use length as the end index
		length += offset;

		int sourceStart = offset;

		int level = 0;
	
		Type type = Type.NEITHER;
		
		try {
			while(offset < length) {
				if(chars[offset] == '<') {
					switch(chars[offset + 1]) {
						case '/' : {
							level--;
		
							if(type != Type.INCREMENT) {
								// 2 or more endish elements
								// flush bytes
								if(sourceStart < offset) {
									buffer.append(chars, sourceStart, offset - sourceStart);
									sourceStart = offset;
								}
		
								buffer.append(indentations[level]);
							} else {
								// characters: text, or cdata, or comment node
								
								type = Type.DECREMENT;
								
								if(characterType == CharactersType.NONE) {
									if(offset - sourceStart > maxTextNodeLength) {
										buffer.append(chars, sourceStart, maxTextNodeLength);
										buffer.append("...[TRUNCATED BY ");
										buffer.append(offset - sourceStart - maxTextNodeLength);
										buffer.append("]");
										
										sourceStart = offset; // skip to <
									} else {
										// not long enough to truncate
									}
								} else if(characterType == CharactersType.CDATA) {
									if(offset - sourceStart - 3 > maxCDATANodeLength) { // 3 - watch overflow
										// already have flushed <![CDATA[
										buffer.append(chars, sourceStart, maxCDATANodeLength);
										buffer.append("...[TRUNCATED BY ");
										buffer.append(offset - sourceStart - 3 - maxCDATANodeLength);
										buffer.append("]");
										
										sourceStart = offset - 3; // i.e. first ] in ]]>
									} else {
										// not long enough to truncate
									}
								} else {
									// do not truncate
								}
							}
							
							offset = scanBeyondEndElement(chars, offset, length);
							
							// complete end element
							buffer.append(chars, sourceStart, offset - sourceStart);
							sourceStart = offset;
							
							characterType = CharactersType.NONE;

							continue;
						} 
						case '!' : {
							// skip cdata and comments so that we maintain correct level count
							
							if(chars[offset + 2] == '-') {							
								if(sourceStart < offset) {
									buffer.append(chars, sourceStart, offset - sourceStart);
									sourceStart = offset;
								}
	
								buffer.append(indentations[level]);
								
								if(!comment) {
									offset = scanBeyondComment(chars, offset, length);

									// complete comment
									buffer.append(chars, sourceStart, offset - sourceStart);
									sourceStart = offset;
	
									type = Type.DECREMENT;

									characterType = CharactersType.NONE;

									continue;
								} else {
									offset += 4; // skip !-- in <!--
									
									characterType = CharactersType.COMMENT;
								}
								
								// complete comment
								buffer.append(chars, sourceStart, offset - sourceStart);
								sourceStart = offset;
								
								type = Type.DECREMENT;
								
								continue;
							} else if(chars[offset + 2] == '[') {
								
								if(offset + 12 >= length) {
									return false;
								}

								offset += 9;
								if(!cdata) {
									// flush previous data + <![CDATA[
									buffer.append(chars, sourceStart, offset - sourceStart);
									sourceStart = offset;
									
									offset = scanBeyondCDataEnd(chars, offset + 3, length); // skip ]]>

									if(offset - 3 - sourceStart > maxCDATANodeLength) {
										buffer.append(chars, sourceStart, maxCDATANodeLength);
										buffer.append("...[TRUNCATED BY ");
										buffer.append(offset - 3 - sourceStart - maxCDATANodeLength);
										buffer.append("]");
										
										sourceStart = offset - 3; // keep ]]>
									}
									characterType = CharactersType.NONE;
								} else {
									characterType = CharactersType.CDATA;
								}
								// complete cdata
								buffer.append(chars, sourceStart, offset - sourceStart);
								sourceStart = offset;
								
								continue;
							} else {
								// assume entity declaration
								// look for >
								
								offset = scanBeyondDTDEnd(chars, offset, length);
								type = Type.DECREMENT;
								
								// complete entity declaration
								buffer.append(chars, sourceStart, offset - sourceStart);
								sourceStart = offset;

								continue;
							}
						}
						case '?' : {
							// processing instruction
							// indentate as start elements
	
							if(sourceStart < offset) {
								buffer.append(chars, sourceStart, offset - sourceStart);
								sourceStart = offset;
							}
	
							offset = scanBeyondProcessingInstruction(chars, offset, length);
							
							// <?xml version="1.0"?>
							if(level == 0 && !declaration && isXMLDeclaration(chars, sourceStart, length)) {
								// skip the whole XML declaration
								sourceStart = offset;
							} else {
								buffer.append(indentations[level]);
	
								// complete processing instruction
								buffer.append(chars, sourceStart, offset - sourceStart);
								sourceStart = offset;
								
								type = Type.DECREMENT;
							}	
							
							characterType = CharactersType.NONE;

							continue;
						} 
						
						default : {
							// start element
							// flush bytes
							if(sourceStart < offset) {
								buffer.append(chars, sourceStart, offset - sourceStart);
								sourceStart = offset;
							}
		
							if(level >= indentations.length) {
								indentations = ensureCharCapacity(level + 8);
							}
		
							buffer.append(indentations[level]);
		
							// scan to end of start element
							offset = scanBeyondStartElement(chars, offset, length); 
							
							// see if empty start element
							if(chars[offset - 2] == '/') {
								// empty element
								type = Type.DECREMENT;
								
								// do not increment level
							} else {
								type = Type.INCREMENT;

								level++;
							}
							
							// complete start tag
							buffer.append(chars, sourceStart, offset - sourceStart);
							sourceStart = offset;
	
							characterType = CharactersType.NONE;
							
							continue;
						}
					}
				}
	
				offset++;
			}

			if(level != 0) {
				buffer.setLength(bufferLength);
				
				return false;
			}

			if(sourceStart < length) {
				buffer.append(chars, sourceStart, length - sourceStart);
			}
			

		} catch(Exception e) {
			buffer.setLength(bufferLength);
			
			return false;
		}
		return true;
	}

	public boolean isPrettyPrintCData() {
		return cdata;
	}
	
	public boolean isPrettyPrintComments() {
		return comment;
	}

	@Override
	public String toString() {
		return "PlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength [cdata=" + cdata + ", comment=" + comment + ", declaration=" + declaration + ", maxTextNodeLength=" + getMaxTextNodeLength()
				+ ", maxCDATANodeLength=" + getMaxCDATANodeLength() + "]";
	}

	
}
