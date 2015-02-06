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

package com.greenbird.xml.prettyprinter.plain.filter;

import java.util.Arrays;


/**
 * 
 * Pretty-prints XML in text, comment and cdata nodes. 
 * Optionally does not fail if there is invalid XML or if XML is detected incorrectly.
 * XML in text and CDATA nodes is detected if the text starts with '&lt;' and ends with '&gt;' and size is &gt;= 4.
 * 
 * @author Thomas Rorvik Skjolberg
 *
 */

public class MultiFilterRobustPlainPrettyPrinter extends AbstractMultiFilterPrettyPrinter {
	
	private final boolean text;
	private final boolean ignoreInvalidTextNodeXML;
	
	private final boolean cdata;
	private final boolean ignoreInvalidCDataNodeXML;
	
	private final boolean comment;
	private final boolean ignoreInvalidCommentNodeXML;
	
	public MultiFilterRobustPlainPrettyPrinter(boolean text, boolean ignoreInvalidTextNodeXML, boolean cdata, boolean ignoreInvalidCDataNodeXML, boolean comment, boolean ignoreInvalidCommentNodeXML, boolean declaration, int maxTextNodeLength, int maxCDATANodeLength, String[] anonymizes, String[] prunes, char indentationCharacter, int indentationMultiplier) {
		super(declaration, maxTextNodeLength, maxCDATANodeLength, anonymizes, prunes, indentationCharacter, indentationMultiplier);
		
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

	public MultiFilterRobustPlainPrettyPrinter(boolean text, boolean ignoreInvalidTextNodeXML, boolean cdata, boolean ignoreInvalidCDataNodeXML, boolean comment, boolean ignoreInvalidCommentNodeXML, boolean declaration, String[] anonymizes, String[] prunes, char indentationCharacter, int indentationMultiplier) {
		this(text, ignoreInvalidTextNodeXML, cdata, ignoreInvalidCDataNodeXML, comment, ignoreInvalidCommentNodeXML, declaration, Integer.MAX_VALUE, Integer.MAX_VALUE, anonymizes, prunes, indentationCharacter, indentationMultiplier);
	}

	public boolean process(final char[] chars, int offset, int length, final StringBuilder buffer) {
		return process(chars, offset, offset + length, buffer, 0, new int[elementFilters.length], new int[attributeFilters.length]);
	}
	
	public boolean process(final char[] chars, int offset, final int length, final StringBuilder buffer, final int levelOffset, final int[] elementMatches, final int[] attributeMatches) {
		
		
		/**
		 * 
		 * Implementation note: Use stricter bounds checking
		 * 
		 */

		int bufferLength = buffer.length();
		
		char[][] indentations = this.indentations;

		CharactersType characterType = CharactersType.NONE;

		boolean anon = false;

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
							// text, comment or cdata
							// max length for text and cdata

							if(sourceStart < offset) {
								if(characterType == CharactersType.NONE) {
									if(anon) {
										buffer.append(FILTER_ANONYMIZE_MESSAGE);

										sourceStart = offset;
									} else {
										xml:
										if(isEscapedXML(chars, offset, sourceStart)) {

											int markLength = buffer.length();
											int markSourceStart = sourceStart;
											
											// initially detected entity
											buffer.append('<');
											sourceStart += 4;
											
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
											if(!process(innerChars, 0, innerChars.length, buffer, level + 1, elementMatches, attributeMatches)) {
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
											buffer.append(FILTER_TRUNCATE_MESSAGE);
											buffer.append(offset - sourceStart - maxTextNodeLength);
											buffer.append("]");
											
											sourceStart = offset; // skip to <
										}
									}
								} else if(characterType == CharactersType.CDATA) {
									if(anon) {
										buffer.append(FILTER_ANONYMIZE_MESSAGE);

										sourceStart = offset - 3; // keep ]]>
									} else {
										if(offset - sourceStart - 3 > maxCDATANodeLength) { // 3 - watch overflow
											// already have flushed <![CDATA[
											buffer.append(chars, sourceStart, maxCDATANodeLength);
											buffer.append(FILTER_TRUNCATE_MESSAGE);
											buffer.append(offset - sourceStart - 3 - maxCDATANodeLength);
											buffer.append("]");
											
											sourceStart = offset - 3; // keep ]]>
										} else {
											// not long enough to truncate
										}
									}
								} else {
									// keep all for comments
								}
							}
							type = Type.DECREMENT;							
						}
	
						// constrain matches
						if(level < elementFilterStart.length) {
							anon = constrainMatches(elementMatches, level);
						} else {
							anon = false;
						}
						if(level < attributeFilterStart.length) {
							constrainAttributeMatches(attributeMatches, level);
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
								if(!process(chars, sourceStart, offset - 3, buffer, level, elementMatches, attributeMatches)) {
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
							
							characterType = CharactersType.NONE;

							continue;
						} else if(chars[offset + 2] == '[') {
							
							if(offset + 12 >= length) {
								return false;
							}

							// append previous data + <![CDATA[
							offset += 9;
							buffer.append(chars, sourceStart, offset - sourceStart);
							sourceStart = offset;

							if(!cdata || ignoreInvalidCDataNodeXML || anon) {
								// look for ]]>
								while(offset < length - 3) {
									if(chars[offset + 2] == '>' && chars[offset] == ']' && chars[offset + 1] == ']') {
										break;
									}
									offset++;
								}
							}
							
							if(sourceStart < offset) {
								if(anon) {
									buffer.append(FILTER_ANONYMIZE_MESSAGE);
									
									buffer.append(chars, offset, 3);
									
									offset += 3;
									sourceStart = offset;
								} else {
									if(ignoreInvalidCDataNodeXML) {
										if(chars[sourceStart] == '<' && chars[offset - 1] == '>' && (offset - 1 - sourceStart) >= 4) {
											// pretty-print the cdata chars without failing the current XML if not well-formed
											if(process(chars, sourceStart, offset, buffer, level, elementMatches, attributeMatches)) {
												// cdata pretty printing success
												// append ]]>
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
										buffer.append(FILTER_TRUNCATE_MESSAGE);
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
								}
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
	
						level++;
						
						boolean prune = false;
						anon = false;
						
						// scan to end of local name
						offset++; // skip <
						while(offset < length) {
							if(chars[offset] == ':') {
								// ignore namespace
								buffer.append(chars, sourceStart, offset - sourceStart);
								sourceStart = offset;
							} else if(isEndOfLocalName(chars[offset])) {
								break;
							}
							offset++;
						}

						if(level < elementFilterStart.length) {
							// match again any higher filter
							if(matchElements(chars, offset, sourceStart, level, elementMatches)) {
								for(int i = elementFilterStart[level]; i < elementFilterEnd[level]; i++) {
									if(elementMatches[i] == level) {
										// matched
										if(elementFilters[i].filterType == FilterType.ANON) {
											anon = true;
										} else if(elementFilters[i].filterType == FilterType.PRUNE) {
											prune = true;
										}
									}
								}
							}
						}
						
						if(anyElementFilters != null) {
							FilterType filterType = matchAnyElements(chars, offset, sourceStart);
							if(filterType == FilterType.ANON) {
								anon = true;
							} else if(filterType == FilterType.PRUNE) {
								prune = true;
							}
						}

						if(level < attributeFilterStart.length) {
							offset = filterAttributes(chars, offset, length, buffer, sourceStart, level, attributeMatches);
						} else {
							offset = scanBeyondStartElementEnd(chars, offset, length);
							
							// complete start tag
							buffer.append(chars, sourceStart, offset - sourceStart);
						}

						sourceStart = offset;

						if(chars[offset - 2] == '/') {
							// empty element
							type = Type.DECREMENT;

							level--;
							
							// constrain matches
							if(level < elementFilterStart.length) {
								anon = constrainMatches(elementMatches, level);
							} else {
								anon = false;
							}
							if(level < attributeFilterStart.length) {
								constrainAttributeMatches(attributeMatches, level);
							}
						} else if(prune) {
							offset = skipSubtree(chars, offset, length);

							if(level >= indentations.length) {
								indentations = ensureCharCapacity(level + 8);
							}
		
							buffer.append(indentations[level]);

							buffer.append(FILTER_PRUNE_MESSAGE);
							
							type = Type.DECREMENT;
						} else {
							type = Type.INCREMENT;
						}
						
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
		return "MultiFilterRobustPlainPrettyPrinter [text=" + text + ", ignoreInvalidTextNodeXML=" + ignoreInvalidTextNodeXML + ", cdata=" + cdata + ", ignoreInvalidCDataNodeXML="
				+ ignoreInvalidCDataNodeXML + ", comment=" + comment + ", ignoreInvalidCommentNodeXML=" + ignoreInvalidCommentNodeXML + ", elementFilters=" + Arrays.toString(elementFilters)
				+ ", attributeFilters=" + Arrays.toString(attributeFilters) + ", elementFilterStart=" + Arrays.toString(elementFilterStart) + ", elementFilterLength="
				+ Arrays.toString(elementFilterEnd) + ", attributeFilterStart=" + Arrays.toString(attributeFilterStart) + ", attributeFilterLength=" + Arrays.toString(attributeFilterEnd)
				+ ", anonymizes=" + Arrays.toString(anonymizes) + ", prunes=" + Arrays.toString(prunes) + ", declaration=" + declaration + ", maxTextNodeLength=" + maxTextNodeLength
				+ ", maxCDATANodeLength=" + maxCDATANodeLength + "]";
	}

	
}
