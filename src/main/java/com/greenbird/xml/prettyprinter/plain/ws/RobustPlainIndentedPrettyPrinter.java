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

package com.greenbird.xml.prettyprinter.plain.ws;


/**
 * 
 * Pretty-prints XML in text, comment and cdata nodes. 
 * Optionally does not fail if there is invalid XML or if XML is detected incorrectly.
 * XML in text and CDATA nodes is detected if the text starts with '&lt;' and ends with '&gt;' and size is &gt;= 4.
 * 
 * @author Thomas Rorvik Skjolberg
 *
 */

public class RobustPlainIndentedPrettyPrinter extends AbstractPlainIgnoreWhitespacePrettyPrinter {

	private final boolean text;
	private final boolean ignoreInvalidTextNodeXML;
	
	private final boolean cdata;
	private final boolean ignoreInvalidCDataNodeXML;
	
	private final boolean comment;
	private final boolean ignoreInvalidCommentNodeXML;
	
	public RobustPlainIndentedPrettyPrinter(boolean text, boolean ignoreInvalidTextNodeXML, boolean cdata, boolean ignoreInvalidCDataNodeXML, boolean comment, boolean ignoreInvalidCommentNodeXML, boolean declaration, char indentationCharacter, int indentationMultiplier) {
		super(declaration, indentationCharacter, indentationMultiplier);
		this.text = text;
		this.ignoreInvalidTextNodeXML = ignoreInvalidTextNodeXML;
		this.cdata = cdata;
		this.ignoreInvalidCDataNodeXML = ignoreInvalidCDataNodeXML;
		this.comment = comment;
		this.ignoreInvalidCommentNodeXML = ignoreInvalidCommentNodeXML;
	}

	public boolean process(char[] chars, int offset, int length, StringBuilder buffer) {
		return process(chars, offset, length, buffer, 0);
	}

	public boolean process(char[] chars, int offset, int length, StringBuilder buffer, int levelOffset) {
		int bufferLength = buffer.length();
		
		char[][] indentations = this.indentations;

		// use length as the end index
		length += offset;

		int sourceStart = offset;

		int level = levelOffset;
	
		Type type = Type.NEITHER;
		
		try {
			while(offset < length - 3) {
	
				if(chars[offset] == '<') {
					switch(chars[offset + 1]) {
					case '/' : {
						level--;
	
						if(type != Type.INCREMENT) {
							// 2 or more endish elements
							// flush bytes is not whitespace
							if(sourceStart < offset) {
								for(int k = sourceStart; k < offset; k++) {
									if(!isIndentationWhitespace(chars[k])) {
										buffer.append(chars, sourceStart, offset - sourceStart);
										break;
									}
								}
								sourceStart = offset;
							}
							
							buffer.append(indentations[level]);
						} else {
							
							
							if(text && sourceStart < offset) {
								// An escaped minimum xml length is <x/> in which encoded size is 4 + x + / + 4 = 10
								// however the end tag is not strictly need in escaped form, so it is 4 + x + / + > = 7
								xml:
								if(isEscapedXML(chars, offset, sourceStart)) {
									int markLength = buffer.length();
									int markSourceStart = sourceStart;
									for(int k = sourceStart; k < offset; k++) {
										if(chars[k] == '&') {
											k++;
											int l = k;
											while(l < offset) {
												if(chars[l] == ';') {

													if(chars[k] == '#') {
														// numeric entity
														buffer.append(chars, sourceStart, l - sourceStart + 1);

														sourceStart = l + 1;
													} else {

														buffer.append(chars, sourceStart, k - sourceStart - 1);
														
														if(appendEntity(l - k, chars, k, buffer)) {
															sourceStart = l + 1;
														} else {
															// unknown entity? 
															
															if(ignoreInvalidTextNodeXML) {
																// reset
																buffer.setLength(markLength);
																sourceStart = markSourceStart;
																
																break xml;
															} else {
																// report back negative result
																buffer.setLength(bufferLength);
																
																return false;
															}
														}
													}
													
													k = l; // increments +1 in for loop
													
													break;
												}
												l++;
											}
											
										}
									}
								
									if(sourceStart < offset) {
										buffer.append(chars, sourceStart, offset - sourceStart);
										sourceStart = offset;
									}

									// recursive call - expensive
									// rip chars from buffer
									char[] innerChars = new char[buffer.length() - markLength];
									buffer.getChars(markLength, buffer.length(), innerChars, 0);
									
									// set buffer back to the length we had before unescaping
									buffer.setLength(markLength);
									if(!process(innerChars, 0, innerChars.length, buffer, level + 1)) {
										// whoops just append the escaped text as chars
										if(ignoreInvalidTextNodeXML) {
											buffer.append(chars, markSourceStart, offset - markSourceStart);
										} else {
											// report back negative result
											buffer.setLength(bufferLength);
											
											return false;
										}
									} else {
										// so we have now written the payload as at least one element
										buffer.append(indentations[level]);
									}
								} else {
									// count as plain text, not XML
									if(sourceStart < offset) {
										buffer.append(chars, sourceStart, offset - sourceStart);
										sourceStart = offset;
									}
								}
								
							}
							type = Type.DECREMENT;
						}
	
						// scan to end of end tag
						offset = scanBeyondEndElement(chars, offset, length);
						
						// complete tag
						buffer.append(chars, sourceStart, offset - sourceStart);
						sourceStart = offset;
						
						continue;
					} 
					case '!' : { 
						// skip cdata and comments so that we maintain correct level count

						if(sourceStart < offset) {
							for(int k = sourceStart; k < offset; k++) {
								if(!isIndentationWhitespace(chars[k])) {
									buffer.append(chars, sourceStart, offset - sourceStart);
									break;
								}
							}
							sourceStart = offset;
						}
						
						if(chars[offset + 2] == '-') {
							// look for -->

							buffer.append(indentations[level]);

							// append <!--
							buffer.append(chars, sourceStart, 4);
							sourceStart += 4;

							offset = scanCommentEnd(chars, offset + 5, length);

							if(!comment) {
								offset++;
								
								// complete comment
								buffer.append(chars, sourceStart, offset - sourceStart);
							} else {
								// pretty-print the comment chars without failing the current XML if not well-formed
								if(!process(chars, sourceStart, offset - sourceStart - 2, buffer, level)) {
									// append chars + -->
									if(ignoreInvalidCommentNodeXML) {
										offset++;

										buffer.append(chars, sourceStart, offset - sourceStart);
									} else {
										// report back negative result
										buffer.setLength(bufferLength);
										
										return false;
									}
								} else {
									// append -->
									buffer.append(chars, offset - 2, 3);
									
									offset++;
								}
								
							}
							sourceStart = offset;
							
							type = Type.DECREMENT;							
							
							continue;
						} else if(chars[offset + 2] == '[') {
							if(offset + 12 >= length) {
								return false;
							}
							
							// append <![CDATA[
							offset += 9;
							buffer.append(chars, sourceStart, offset - sourceStart);
							sourceStart = offset;

							// skip to last char of ]]>
							offset = scanCDataEnd(chars, offset, length);
							
							// flush chars if no cdata pretty print or if not is XML
							if(!cdata || !(chars[sourceStart] == '<' && chars[offset - 3] == '>' && (offset - sourceStart) >= 7)) { // <x/> + ]]> is minimum
								// complete cdata
								offset++;
								
								buffer.append(chars, sourceStart, offset - sourceStart);
							} else {
								// pretty-print the cdata chars without failing the current XML if not well-formed
								if(!process(chars, sourceStart, offset - sourceStart - 2, buffer, level)) {
									// append chars + ]]>
									if(ignoreInvalidCDataNodeXML) {
										offset++;
										
										buffer.append(chars, sourceStart, offset - sourceStart);
									} else {
										// report back negative result
										buffer.setLength(bufferLength);
										
										return false;
									}
								} else {
									// append ]]>
									buffer.append(chars, offset - 2, 3);
									
									offset++;
									
									type = Type.DECREMENT;
								}
								
							}
							sourceStart = offset;
						} else {
							// assume entity declaration
							// look for >
							
							offset = scanBeyondDTDEnd(chars, offset, length);
							type = Type.DECREMENT;
							
							// complete entity declaration
							buffer.append(chars, sourceStart, offset - sourceStart);
							sourceStart = offset;

						}
						continue;
					} 
					case '?' : {
						// processing instruction
						// indentate as empy elements

						if(sourceStart < offset) {
							for(int k = sourceStart; k < offset; k++) {
								if(!isIndentationWhitespace(chars[k])) {
									buffer.append(chars, sourceStart, offset - sourceStart);
									break;
								}
							}
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
						continue;
					} 
					default : {
						// start element
						// flush bytes
						if(sourceStart < offset) {
							for(int k = sourceStart; k < offset; k++) {
								if(!isIndentationWhitespace(chars[k])) {
									buffer.append(chars, sourceStart, offset - sourceStart);
									break;
								}
							}
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

						continue;
					}
					}
				}
	
				offset++;
			}

			if(level != levelOffset) {
				buffer.setLength(bufferLength);
				
				return false;
			}

			if(sourceStart < length) {
				buffer.append(chars, sourceStart, length - sourceStart);
			}
			

		} catch(Exception e) {
			return false;
		}
		return true;
	}

	public boolean isPrettyPrintTextNodes() {
		return text;
	}
	
	public boolean isPrettyPrintCData() {
		return cdata;
	}
	
	public boolean isPrettyPrintComments() {
		return comment;
	}

	@Override
	public String toString() {
		return "RobustPlainIndentedPrettyPrinter [text=" + text + ", ignoreInvalidTextNodeXML=" + ignoreInvalidTextNodeXML + ", cdata=" + cdata + ", ignoreInvalidCDataNodeXML="
				+ ignoreInvalidCDataNodeXML + ", comment=" + comment + ", ignoreInvalidCommentNodeXML=" + ignoreInvalidCommentNodeXML + ", declaration=" + declaration + ", maxTextNodeLength="
				+ maxTextNodeLength + ", maxCDATANodeLength=" + maxCDATANodeLength + "]";
	}

	
}
