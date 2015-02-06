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
 * Pretty-prints XML in text nodes between start and end elements. 
 * XML in text nodes is detected if the text starts with '&lt;' and ends with '&gt;' and size is &gt;= 4.
 *
 * @author Thomas Rorvik Skjolberg
 *
 */

public class MultiFilterPlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength extends AbstractMultiFilterPrettyPrinter {

	private final boolean cdata;
	private final boolean comment;
	private final boolean ignoreInvalidTextNodeXML;

	public MultiFilterPlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength(boolean cdata, boolean comment, boolean ignoreInvalidTextNodeXML, boolean declaration, int maxTextNodeLength, int maxCDATANodeLength, String[] anonymizes, String[] prunes, char indentationCharacter, int indentationMultiplier) {
		super(declaration, maxTextNodeLength, maxCDATANodeLength, anonymizes, prunes, indentationCharacter, indentationMultiplier);
		this.cdata = cdata;
		this.comment = comment;
		this.ignoreInvalidTextNodeXML = ignoreInvalidTextNodeXML;
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

						continue;
					}
					case '!' : {
						// complete cdata and comments so nodes
						
						if(chars[offset + 2] == '-') {
							// look for -->

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
							}

							characterType = CharactersType.COMMENT;

							type = Type.DECREMENT;

						} else if(chars[offset + 2] == '[') {
							if(offset + 12 >= length) {
								return false;
							}

							offset += 9; // skip <![CDATA[
							
							// flush <![CDATA[
							buffer.append(chars, sourceStart, offset - sourceStart);
							sourceStart = offset;

							if(!cdata) {
								offset = scanBeyondCDataEnd(chars, offset + 3, length); // skip ]]>

								if(offset - 3 - sourceStart > 0) {
									if(anon) {
										buffer.append(FILTER_ANONYMIZE_MESSAGE);
										
										sourceStart = offset - 3; // keep ]]>
									} else if(offset - sourceStart - 3 > maxCDATANodeLength) { // 3 - watch overflow
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

								// complete cdata
								buffer.append(chars, sourceStart, offset - sourceStart);
								sourceStart = offset;

								characterType = CharactersType.NONE;
							} else {
								// counts as characters or xml
								// handle on end tag
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
						break;	
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

	public boolean isPrettyPrintCData() {
		return cdata;
	}
	
	public boolean isPrettyPrintComments() {
		return comment;
	}

	public boolean isPrettyPrintTextNodes() {
		return true;
	}

	@Override
	public String toString() {
		return "MultiFilterPlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength [cdata=" + cdata + ", comment=" + comment + ", ignoreInvalidTextNodeXML=" + ignoreInvalidTextNodeXML + ", anonymizes="
				+ Arrays.toString(anonymizes) + ", prunes=" + Arrays.toString(prunes) + ", declaration=" + declaration + ", maxTextNodeLength=" + getMaxTextNodeLength() + ", maxCDATANodeLength="
				+ getMaxCDATANodeLength() + "]";
	}

}
