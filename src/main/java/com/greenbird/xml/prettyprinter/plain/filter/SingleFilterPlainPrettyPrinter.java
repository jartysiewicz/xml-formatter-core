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

import com.greenbird.xml.prettyprinter.plain.filter.AbstractMultiFilterPrettyPrinter.FilterType;


/**
 * 
 * XML Pretty Printer. Produces conformant XML with the exception of an additional newline at the start of the file.
 * 
 * @author Thomas Rorvik Skjolberg
 *
 */

public class SingleFilterPlainPrettyPrinter extends AbstractFilterPrettyPrinter {

	private final char[][] paths;
	private final FilterType filterType;
	private final char[] attribute;
	
	public SingleFilterPlainPrettyPrinter(boolean declaration, String expression, FilterType type, char indentationCharacter, int indentationMultiplier) {
		super(declaration, type == FilterType.ANON ? new String[]{expression} : EMPTY, type == FilterType.PRUNE ? new String[]{expression} : EMPTY);
		
		char[][] paths = parse(expression);
		if(paths[paths.length - 1][0] == '@') {
			//remove @
			if(type == FilterType.PRUNE) {
				throw new IllegalArgumentException("Attribute match XPath for prune not supported");
			}
			attribute = new char[paths[paths.length - 1].length - 1];
			System.arraycopy(paths[paths.length - 1], 1, attribute, 0, attribute.length);
			
			char[][] elementPath = new char[paths.length - 1][];
			System.arraycopy(paths, 0, elementPath, 0, elementPath.length);
			
			this.paths = elementPath;
		} else {
			attribute = null;
			this.paths = paths;
		}
		this.filterType = type;
	}

	public boolean process(final char[] chars, int offset, int length, final StringBuilder buffer) {
		/**
		 *
		 * Implementation note: cdata + comments characters handled locally
		 *  
		 */
		int matches = 0;
		
		final char[][] elementPaths = this.paths;
		final FilterType filterType = this.filterType;
		
		final int bufferLength = buffer.length();
		
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
						case '/' : {  // end tag
							if(type != Type.INCREMENT) {
								level--;
								// 2 or more endish elements
								// flush bytes
								if(sourceStart < offset) {
									buffer.append(chars, sourceStart, offset - sourceStart);
									sourceStart = offset;
								}
								
								buffer.append(indentations[level]);
							} else {
								type = Type.DECREMENT;
								// characters: text node
								if(filterType == FilterType.ANON && matches >= elementPaths.length && level == elementPaths.length) {
									// hide empty value? no
									if(sourceStart < offset) {
										buffer.append(FILTER_ANONYMIZE_MESSAGE);
										
										sourceStart = offset;
									}
								}
								level--;
							}
							if(matches >= level) {
								matches = level;
							}
		
							offset = scanBeyondEndElement(chars, offset, length);
							
							// complete end element
							buffer.append(chars, sourceStart, offset - sourceStart);
							sourceStart = offset;
							
							continue;
						}
						case '!': {
							// complete cdata and comments so nodes
							
							if(chars[offset + 2] == '-') {
								// look for -->
								
								if(sourceStart < offset) {
									buffer.append(chars, sourceStart, offset - sourceStart);
									sourceStart = offset;
								}
	
								buffer.append(indentations[level]);
								
								offset = scanBeyondComment(chars, offset, length);
								
								// complete comment
								buffer.append(chars, sourceStart, offset - sourceStart);
								sourceStart = offset;

								type = Type.DECREMENT;
	
								continue;
							} else if(chars[offset + 2] == '[') {
								if(offset + 12 >= length) {
									return false;
								}

								// skip <![CDATA[ ]]>
								offset += 9;
								
								buffer.append(chars, sourceStart, offset - sourceStart);
								sourceStart = offset;
								
								offset = scanBeyondCDataEnd(chars, offset + 3, length);

								if(filterType == FilterType.ANON && matches >= elementPaths.length && level == elementPaths.length) {
									if(offset - sourceStart - 3 > 0) {
										buffer.append(FILTER_ANONYMIZE_MESSAGE);
										
										sourceStart = offset - 3; // keep ]]>
									}
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
							
							match:
							if(matches == level && level < elementPaths.length) {
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
								
								if(elementPaths[matches].length == 1 && elementPaths[matches][0] == '*') {
									matches++;
								} else if(elementPaths[matches].length == offset - sourceStart - 1) {
									for(int i = 0; i < offset - sourceStart - 1; i++) {
										if(elementPaths[matches][i] != chars[sourceStart + 1 + i]) {
											break match;
										}
									}
									matches++;
								} else {
									break match;
								}
							
								// complete local name
								buffer.append(chars, sourceStart, offset - sourceStart);
								sourceStart = offset;

								if(attribute != null && matches == elementPaths.length) {
									// find start of first attribute
									while(offset < length && chars[offset] != '>') {
										// some attributes use ', others " as delimiters
										
										if(isIndentationWhitespace(chars[offset])) {
											
											// skip across whitespace (accept some whitespace)
											do {
												offset++;
											} while(isIndentationWhitespace(chars[offset]));
											
											if(chars[offset] != '/' && chars[offset] != '>') {
												// start of attribute?
												int attributeNameStart = offset;
												do {
													offset++;
	
													if(chars[offset] == ':') {
														// ignore namespaces
														offset++;
														attributeNameStart = offset;
													}
	
												} while(chars[offset] != '='); // && !isIndentationWhitespace(chars[offset]));
												
												buffer.append(chars, sourceStart, offset - sourceStart);
												sourceStart = offset;
	
												offset = scanToEndOfAttributeValue(chars, offset, length);
	
												// check attribute name, length
													
												if(attribute.length == 1 && attribute[0] == '*') {
													buffer.append(chars, sourceStart, 2); // =" or ='
													buffer.append(FILTER_ANONYMIZE_MESSAGE);
													sourceStart = offset;
												} else {
													attributeMatch:
		
														if(attribute.length == sourceStart - attributeNameStart) {
															for(int i = 0; i < attribute.length; i++) {
																if(attribute[i] != chars[attributeNameStart + i]) {
																	break attributeMatch;
																}
															}
															buffer.append(chars, sourceStart, 2); // =" or ='
															buffer.append(FILTER_ANONYMIZE_MESSAGE);
															sourceStart = offset;
														}
												}
												
												// flush remainer
												if(sourceStart < offset) {
													buffer.append(chars, sourceStart, offset - sourceStart);
													sourceStart = offset;
												}
											}
										} else {
											offset++;
										}
									}

								}
							}
							
							offset = scanBeyondStartElementEnd(chars, offset, length);

							// complete start tag
							if(sourceStart < offset) {
								buffer.append(chars, sourceStart, offset - sourceStart);
								sourceStart = offset;
							}
							if(chars[offset - 2] == '/') {
								// empty element
								
								if(matches >= level) {
									matches = level;
								}
								
								type = Type.DECREMENT;
								// do not increment level
							} else if(filterType == FilterType.PRUNE && matches >= elementPaths.length) {
								offset = skipSubtree(chars, offset, length);
								
								level++;

								if(level >= indentations.length) {
									indentations = ensureCharCapacity(level + 8);
								}
			
								buffer.append(indentations[level]);

								buffer.append(FILTER_PRUNE_MESSAGE);
								
								type = Type.DECREMENT;
							} else {
								type = Type.INCREMENT;

								level++;
							}
							
							sourceStart = offset;
	
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

	@Override
	public String toString() {
		return "SingleFilterPlainPrettyPrinter [anonymizes=" + Arrays.toString(anonymizes) + ", prunes=" + Arrays.toString(prunes) + ", declaration=" + declaration + "]";
	}

	
}
