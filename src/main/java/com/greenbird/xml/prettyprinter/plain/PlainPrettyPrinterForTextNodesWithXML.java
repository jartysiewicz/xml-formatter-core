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
 * XML within text nodes is also reformatted - if the text starts with '&lt;' and ends with '&gt;' and size is &gt;= 4.
 *
 * @author Thomas Rorvik Skjolberg
 *
 */

public class PlainPrettyPrinterForTextNodesWithXML extends AbstractPrettyPrinter {

	/** pretty-print cdata */
	private final boolean cdata;
	/** pretty-print comments */
	private final boolean comment;
	private final boolean ignoreInvalidTextNodeXML;

	public PlainPrettyPrinterForTextNodesWithXML(boolean cdata, boolean comment, boolean ignoreInvalidTextNodeXML, boolean declaration, char indentationCharacter, int indentationMultiplier) {
		super(declaration, indentationCharacter, indentationMultiplier);
		this.cdata = cdata;
		this.comment = comment;
		this.ignoreInvalidTextNodeXML = ignoreInvalidTextNodeXML;
	}

	public boolean process(final char[] chars, int offset, int length, final StringBuilder buffer) {
		return process(chars, offset, offset + length, buffer, 0);
	}

	public boolean process(final char[] chars, int offset, final int length, final StringBuilder buffer, final int levelOffset) {

		/**
		 * 
		 * Implementation note: Use stricter bounds checking
		 * Implementation note: cdata + comments characters handled on end element, not in uniform way,
		 * so keep track of character type.
		 */
		
		int bufferLength = buffer.length();

		char[][] indentations = this.indentations;

		CharactersType characterType = CharactersType.NONE;

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
							
							// characters: text, comment or cdata node
							
							if(characterType == CharactersType.NONE) {
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
							} else {
								// do not process cdata or comment
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

							if(!comment) {
								offset = scanBeyondComment(chars, offset, length);

								// complete comment
								buffer.append(chars, sourceStart, offset - sourceStart);
								sourceStart = offset;

								characterType = CharactersType.NONE;

								type = Type.DECREMENT;

								continue;
							}

							characterType = CharactersType.COMMENT;

							type = Type.DECREMENT;

						} else if(chars[offset + 2] == '[') {
							if(offset + 12 >= length) {
								return false;
							}
							if(!cdata) {
								// look for ]]>
								offset = scanBeyondCData(chars, offset, length); // skip ]]>
								
								characterType = CharactersType.NONE;
							} else {
								// counts as characters
								offset += 9;
								
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
		return "PlainPrettyPrinterForTextNodesWithXML [cdata=" + cdata + ", comment=" + comment + ", ignoreInvalidTextNodeXML=" + ignoreInvalidTextNodeXML + ", declaration=" + declaration + "]";
	}

	
}
