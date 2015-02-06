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
 * Reformat of XML within text nodes is also supported - if the text starts with '&lt;' and ends with '&gt;' and size is &gt;= 4.
 * <br>
 * Max text and CDATA node size is supported.
 * <br>
 *
 * @author Thomas Rorvik Skjolberg
 *
 */

public class RobustPlainPrettyPrinter extends AbstractPrettyPrinter {

	private final boolean text;
	private final boolean ignoreInvalidTextNodeXML;
	
	/** pretty-print cdata */
	private final boolean cdata;
	private final boolean ignoreInvalidCDataNodeXML;
	
	/** pretty-print comments */
	private final boolean comment;
	private final boolean ignoreInvalidCommentNodeXML;
	
	public RobustPlainPrettyPrinter(boolean text, boolean ignoreInvalidTextNodeXML, boolean cdata, boolean ignoreInvalidCDataNodeXML, boolean comment, boolean ignoreInvalidCommentNodeXML, boolean declaration, int maxTextNodeLength, int maxCDATANodeLength, char indentationCharacter, int indentationMultiplier) {
		super(declaration, maxTextNodeLength, maxCDATANodeLength, indentationCharacter, indentationMultiplier);
		
		if(ignoreInvalidTextNodeXML && !text) {
			throw new IllegalArgumentException("Cannot ignore invalid text node XML without targeting text nodes");
		}
		if(ignoreInvalidCDataNodeXML && !cdata) {
			throw new IllegalArgumentException("Cannot ignore invalid CDATA node XML without targeting CDATA nodes");
		}
		if(ignoreInvalidCommentNodeXML && !comment) {
			throw new IllegalArgumentException("Cannot ignore invalid comment node XML without targeting comment nodes");
		}
		
		this.text = text;
		this.ignoreInvalidTextNodeXML = ignoreInvalidTextNodeXML;
		this.cdata = cdata;
		this.ignoreInvalidCDataNodeXML = ignoreInvalidCDataNodeXML;
		this.comment = comment;
		this.ignoreInvalidCommentNodeXML = ignoreInvalidCommentNodeXML;
	}

	public RobustPlainPrettyPrinter(boolean text, boolean ignoreInvalidTextNodeXML, boolean cdata, boolean ignoreInvalidCDataNodeXML, boolean comment, boolean ignoreInvalidCommentNodeXML, boolean declaration, char indentationCharacter, int indentationMultiplier) {
		this(text, ignoreInvalidTextNodeXML, cdata, ignoreInvalidCDataNodeXML, comment, ignoreInvalidCommentNodeXML, declaration, Integer.MAX_VALUE, Integer.MAX_VALUE, indentationCharacter, indentationMultiplier);
	}

	public boolean process(final char[] chars, int offset, int length, final StringBuilder buffer) {
		return process(chars, offset, offset + length, buffer, 0);
	}

	public boolean process(char[] chars, int offset, final int length, StringBuilder buffer, int levelOffset) {
		
		/**
		 * 
		 * Implementation note: Use stricter bounds checking.
		 * Implementation note: cdata characters handled on end element, not in uniform way,
		 * so keep track of character type.
		 * 
		 */
		
		int bufferLength = buffer.length();
		
		CharactersType characterType = CharactersType.NONE;

		char[][] indentations = this.indentations;

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
							// flush bytes
							if(sourceStart < offset) {
								buffer.append(chars, sourceStart, offset - sourceStart);
								sourceStart = offset;
							}
	
							buffer.append(indentations[level]);
						} else {
							
							// characters: text or cdata node
							
							if(characterType == CharactersType.NONE) {
								xml:
								if(text && isEscapedXML(chars, offset, sourceStart)) {
									int markLength = buffer.length();
									int markSourceStart = sourceStart;
									
									// initially detected entity
									buffer.append('<');
									sourceStart += 4;  // skip &lt;
									
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
										if(ignoreInvalidTextNodeXML) {
											// reset
											buffer.setLength(markLength);
											sourceStart = markSourceStart;
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
								}
						
								// count as textual data, not XML
								if(offset - sourceStart > maxTextNodeLength) {
									buffer.append(chars, sourceStart, maxTextNodeLength);
									buffer.append("...[TRUNCATED BY ");
									buffer.append(offset - sourceStart - maxTextNodeLength);
									buffer.append("]");
									
									sourceStart = offset; // skip to <
								} else {
									// flush below
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
							type = Type.DECREMENT;
						}
	
						offset = scanBeyondEndElement(chars, offset, length);
						
						// complete tag
						buffer.append(chars, sourceStart, offset - sourceStart);
						sourceStart = offset;
						
						characterType = CharactersType.NONE;
						
						continue;
					} 
					case '!' : {
						// skip cdata and comments so that we maintain correct level count
						
						if(chars[offset + 2] == '-') {
							// look for -->
							
							if(sourceStart < offset) {
								buffer.append(chars, sourceStart, offset - sourceStart);
								sourceStart = offset;
							}

							buffer.append(indentations[level]);

							// append <!--
							buffer.append(chars, sourceStart, 4);
							sourceStart += 4;

							// scan to end
							offset = scanBeyondComment(chars, offset, length);
							
							if(!comment) {
								// complete comment
								buffer.append(chars, sourceStart, offset - sourceStart);
							} else {
								// pretty-print the comment chars without failing the current XML if not well-formed
								if(!process(chars, sourceStart, offset - 3, buffer, level)) {
									// append chars + -->
									if(ignoreInvalidCommentNodeXML) {
										buffer.append(chars, sourceStart, offset - sourceStart);
									} else {
										// report back negative result
										buffer.setLength(bufferLength);
										
										return false;
									}
								} else {
									// append -->
									buffer.append(chars, offset - 3, 3);
								}
								
							}
							sourceStart = offset;
							
							type = Type.DECREMENT;							
							
							characterType = CharactersType.COMMENT;
							
							continue;
						} else if(chars[offset + 2] == '[') {
							
							if(offset + 12 >= length) {
								return false;
							}

							// append previous data + <![CDATA[
							offset += 9;
							buffer.append(chars, sourceStart, offset - sourceStart);
							sourceStart = offset;

							if(!cdata || ignoreInvalidCDataNodeXML) {
								// look for ]]>
								while(offset < length - 3) {
									if(chars[offset + 2] == '>' && chars[offset] == ']' && chars[offset + 1] == ']') {
										break;
									}
									offset++;
								}
							}
							
							if(sourceStart < offset) {
								if(ignoreInvalidCDataNodeXML) {
									if(chars[sourceStart] == '<' && chars[offset - 1] == '>' && (offset - 1 - sourceStart) >= 4) {
										// pretty-print the cdata chars without failing the current XML if not well-formed
										if(process(chars, sourceStart, offset, buffer, level)) {
											// cdata pretty printing success
											sourceStart = offset;
	
											type = Type.DECREMENT;
										} else {
											// process as text
										}
									} else {
										// not XML, process as text
									}
								} else {
									// do not pretty-print cdata, or characters
								}
	
								// process as text text here
								if(offset - sourceStart > maxCDATANodeLength) {
									buffer.append(chars, sourceStart, maxCDATANodeLength);
									buffer.append("...[TRUNCATED BY ");
									buffer.append(offset - sourceStart - maxCDATANodeLength);
									buffer.append("]");
									
									// append ]]>
									buffer.append(chars, offset, 3);
								} else {
									// append textual cdata + ]]>
									buffer.append(chars, sourceStart, offset - sourceStart + 3);
								}
								offset += 3;
								sourceStart = offset;
								
								characterType = CharactersType.NONE;
							} else {
								// process as xml or text
								characterType = CharactersType.CDATA;
							}
								
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
						// process as empty elements

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

			if(level != levelOffset) {
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
		return "RobustPlainPrettyPrinter [text=" + text + ", ignoreInvalidTextNodeXML=" + ignoreInvalidTextNodeXML + ", cdata=" + cdata + ", ignoreInvalidCDataNodeXML=" + ignoreInvalidCDataNodeXML
				+ ", comment=" + comment + ", ignoreInvalidCommentNodeXML=" + ignoreInvalidCommentNodeXML + ", declaration=" + declaration + ", maxTextNodeLength=" + getMaxTextNodeLength()
				+ ", maxCDATANodeLength=" + getMaxCDATANodeLength() + "]";
	}

	
}
