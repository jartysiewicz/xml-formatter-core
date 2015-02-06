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
 * XML Pretty Printer. Removes existing indentation (whitespace). Produces conformant XML with the exception of an additional newline at the start of the file.
 * 
 * @author Thomas Rorvik Skjolberg
 *
 */

public class PlainIndentedPrettyPrinter extends AbstractPlainIgnoreWhitespacePrettyPrinter {

	public PlainIndentedPrettyPrinter(boolean declaration, char indentationCharacter, int indentationMultiplier) {
		super(declaration, indentationCharacter, indentationMultiplier);
	}

	public boolean process(final char[] chars, int offset, int length, final StringBuilder buffer) {
		int bufferLength = buffer.length();
		
		char[][] indentations = this.indentations;

		// use length as the end index
		length += offset;

		int sourceStart = offset;

		Type type = Type.NEITHER;

		int level = 0;
		
		try {
			while(offset < length) {
	
				if(chars[offset] == '<') {
					switch(chars[offset + 1]) {
					case '/' : {
						level--;
	
						if(type != Type.INCREMENT) {
							// 2 or more endish elements
							// flush bytes is not whitespace

							for(int k = sourceStart; k < offset; k++) {
								if(!isIndentationWhitespace(chars[k])) {
									buffer.append(chars, sourceStart, offset - sourceStart);
									break;
								}
							}
							sourceStart = offset;
	
							buffer.append(indentations[level]);
						} else {
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
							
							offset = scanBeyondComment(chars, offset, length);
							type = Type.DECREMENT;
							
							// complete comment
						} else if(chars[offset + 2] == '[') {
							if(offset + 12 >= length) {
								return false;
							}

							// skip <![CDATA[]]>
							offset = scanBeyondCData(chars, offset, length);
							
							// complete cdata
						} else {
							// assume entity declaration
							// look for >
							
							offset = scanBeyondDTDEnd(chars, offset, length);
							type = Type.DECREMENT;
							
							// complete entity declaration
						}
						buffer.append(chars, sourceStart, offset - sourceStart);
						sourceStart = offset;
						
						continue;
					} 
					case '?' : {
						// processing instruction
						// indentate as start elements

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
							
							type = Type.DECREMENT;
							
							// complet processing instruction
							buffer.append(chars, sourceStart, offset - sourceStart);
							sourceStart = offset;
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

			if(level != 0) {
				buffer.setLength(bufferLength);
				
				return false;
			}

			if(sourceStart < length) {
				for(int k = sourceStart; k < length; k++) {
					if(!isIndentationWhitespace(chars[k])) {
						buffer.append(chars, sourceStart, length - sourceStart);
						break;
					}
				}
			}
			

		} catch(Exception e) {
			buffer.setLength(bufferLength);
			
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "PlainIndentedPrettyPrinter [declaration=" + declaration + "]";
	}

	
}
