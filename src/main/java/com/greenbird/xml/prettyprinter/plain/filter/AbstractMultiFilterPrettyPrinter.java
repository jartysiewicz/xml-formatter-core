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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public abstract class AbstractMultiFilterPrettyPrinter extends AbstractFilterPrettyPrinter {
	
	public static enum FilterType {
		/** public for testing */
		ANON, PRUNE;
	}
	
	protected static class AbsolutePathFilter {
		
		protected final char[][] paths;
		protected final FilterType filterType;
		
		public AbsolutePathFilter(char[][] paths, FilterType filterType) {
			this.paths = paths;
			this.filterType = filterType;
		}
		
		protected int getLength() {
			return paths.length;
		}
		
		protected FilterType getFilterType() {
			return filterType;
		}
		
	}
	
	protected static class AnyPathFilter {
		
		protected final char[] path;
		protected final FilterType filterType;
		
		public AnyPathFilter(char[] path, FilterType filterType) {
			this.path = path;
			this.filterType = filterType;
		}

		protected FilterType getFilterType() {
			return filterType;
		}
	}

	private static final Comparator<AbsolutePathFilter> comparator = new Comparator<AbsolutePathFilter>() {

		@Override
		public int compare(AbsolutePathFilter o1, AbsolutePathFilter o2) {
			return Integer.compare(o1.getLength(), o2.getLength());
		}
	};
	
	/** absolute path expressions */
	protected final AbsolutePathFilter[] elementFilters;
	protected final AbsolutePathFilter[] attributeFilters;

	/** any path expression - //element */
	protected final AnyPathFilter[] anyElementFilters;
	
	protected final int[] elementFilterStart;
	protected final int[] elementFilterEnd;
	
	protected final int[] attributeFilterStart;
	protected final int[] attributeFilterEnd;

	public AbstractMultiFilterPrettyPrinter(boolean declaration, String[] anonymizes, String[] prunes, char indentationCharacter, int indentationMultiplier) {
		this(declaration, Integer.MAX_VALUE, Integer.MAX_VALUE, anonymizes, prunes, indentationCharacter, indentationMultiplier);
	}

	public AbstractMultiFilterPrettyPrinter(boolean declaration, int maxTextNodeLength, int maxCDATANodeLength, String[] anonymizes, String[] prunes, char indentationCharacter, int indentationMultiplier) {
		super(declaration, maxTextNodeLength, maxCDATANodeLength, anonymizes != null ? anonymizes : EMPTY, prunes != null ? prunes : EMPTY, indentationCharacter, indentationMultiplier);
		
		List<AbsolutePathFilter> attributes = new ArrayList<AbsolutePathFilter>();
		List<AbsolutePathFilter> elements = new ArrayList<AbsolutePathFilter>();

		List<AnyPathFilter> any = new ArrayList<AnyPathFilter>(); // prunes take precedence of anonymizes

		if(prunes != null) {
			for(int i = 0; i < prunes.length; i++) {
				String prune = prunes[i];
				if(prune.startsWith(ANY_PREFIX)) {
					any.add(new AnyPathFilter(prune.substring(2).toCharArray(), FilterType.PRUNE));
				} else {
					elements.add(new AbsolutePathFilter(parse(prune), FilterType.PRUNE));
				}
			}
		}

		if(anonymizes != null) {
			for(int i = 0; i < anonymizes.length; i++) {
				String anonymize = anonymizes[i];
				if(anonymize.startsWith(ANY_PREFIX)) {
					any.add(new AnyPathFilter(anonymize.substring(2).toCharArray(), FilterType.ANON));
				} else {
					char[][] elementPath = parse(anonymize);
					if(elementPath[elementPath.length - 1][0] == '@') {
						// remove at
						char[] name = new char[elementPath[elementPath.length - 1].length - 1];
						System.arraycopy(elementPath[elementPath.length - 1], 1, name, 0, name.length);
						elementPath[elementPath.length - 1] = name;
	
						attributes.add(new AbsolutePathFilter(elementPath, FilterType.ANON));
					} else {
						elements.add(new AbsolutePathFilter(elementPath, FilterType.ANON));
					}
				}
			}
		}
		
		if(!any.isEmpty()) {
			anyElementFilters = any.toArray(new AnyPathFilter[any.size()]);
		} else {
			anyElementFilters = null;
		}
		
		if(!elements.isEmpty()) {
			Collections.sort(elements, comparator);
			
			int maxElementPaths = Integer.MIN_VALUE;
			for(AbsolutePathFilter elementPath : elements) {
				if(elementPath.getLength() > maxElementPaths) {
					maxElementPaths = elementPath.getLength();
				}
			}
			
			elementFilterStart = new int[maxElementPaths + 1];
			elementFilterEnd = new int[maxElementPaths + 1];
			
			// count
			for(int i = 0; i < elements.size(); i++) {
				if(elementFilterEnd[elements.get(i).getLength()] == 0) { // first filter for this index
					elementFilterStart[elements.get(i).getLength()] = i;
				}
				elementFilterEnd[elements.get(i).getLength()]++;
			}

			// add start to count for end
			for(int i = 0; i < elementFilterEnd.length; i++) {
				elementFilterEnd[i] += elementFilterStart[i];
			}

			elementFilters = elements.toArray(new AbsolutePathFilter[elements.size()]);
		} else {
			elementFilterStart = new int[]{};
			elementFilterEnd = new int[]{};
			elementFilters = new AbsolutePathFilter[]{};
		}

		if(!attributes.isEmpty()) {
			Collections.sort(attributes, comparator);

			int maxAttributePaths = Integer.MIN_VALUE;
			for(AbsolutePathFilter elementPath : attributes) {
				if(elementPath.getLength() > maxAttributePaths) {
					maxAttributePaths = elementPath.getLength();
				}
			}
	
			attributeFilterStart = new int[maxAttributePaths];
			attributeFilterEnd = new int[maxAttributePaths];
			
			// count
			for(int i = 0; i < attributes.size(); i++) {
				
				int elementPaths = attributes.get(i).getLength() - 1; // last is attribute
				if(attributeFilterEnd[elementPaths] == 0) { // first filter for this index
					attributeFilterStart[elementPaths] = i;
				}
				attributeFilterEnd[elementPaths]++;
			}
			
			// add start to count for end
			for(int i = 0; i < attributeFilterEnd.length; i++) {
				attributeFilterEnd[i] += attributeFilterStart[i];
			}
			
			attributeFilters = attributes.toArray(new AbsolutePathFilter[attributes.size()]);
		} else {
			attributeFilterStart = new int[]{};
			attributeFilterEnd = new int[]{};
			attributeFilters = new AbsolutePathFilter[]{};
		}

	}

	public boolean isXmlDeclaration() {
		return declaration;
	}

	protected void constrainAttributeMatches(int[] matches, int level) {
		for(int i = attributeFilterStart[level]; i < matches.length; i++) {
			if(matches[i] > level) {
				matches[i] = level;
			}
		}
	}
	
	protected boolean constrainMatches(int[] matches, int level) {
		boolean anon;
		anon = false;
		for(int i = elementFilterStart[level]; i < matches.length; i++) {
			if(matches[i] > level) {
				matches[i] = level;
			}
			
			if(matches[i] == elementFilters[i].getLength()) {
				if(elementFilters[i].getFilterType() == FilterType.ANON) {
					anon = true;
				}
			}
		}
		return anon;
	}

	protected int filterAttributes(final char[] chars, int offset, int length, final StringBuilder buffer, int sourceStart, int level, final int[] attributeMatches) {
		// match again any higher filter
		boolean attributeElementMatch = false;
		for(int i = attributeFilterStart[level]; i < attributeMatches.length; i++) {
			
			if(attributeMatches[i] == level - 1) {
				if(attributeFilters[i].paths[attributeMatches[i]].length == 1 && attributeFilters[i].paths[attributeMatches[i]][0] == '*') {
					// pass through
				} else {
					if(attributeFilters[i].paths[attributeMatches[i]].length != offset - sourceStart - 1) {
						continue;
					}
					for(int k = 0; k < offset - sourceStart - 1; k++) {
						if(attributeFilters[i].paths[attributeMatches[i]][k] != chars[sourceStart + 1 + k]) {
							continue;
						}
					}
				}
				attributeMatches[i]++;

				if(i < attributeFilterEnd[level]) {
					attributeElementMatch = true;
				}
			}
		}
		
		if(attributeElementMatch) {
			int attributeSourceStart = sourceStart;
			// all elements matches, but attribute must match too

			while(offset < length && chars[offset] != '>') {

				// some attributes use ', others " as delimiters

				if(isIndentationWhitespace(chars[offset])) {

					// skip across whitespace (accept some whitespace)
					do {
						offset++;
					} while(isIndentationWhitespace(chars[offset]));

					if(chars[offset] == '/') {
						offset++;
						
						continue;
					} else if(chars[offset] == '>') {
						break;
					}
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

					buffer.append(chars, attributeSourceStart, offset - attributeSourceStart);
					attributeSourceStart = offset;

					offset = scanToEndOfAttributeValue(chars, offset, length);

					// check attribute name, length
					filterAttribute:
						for(int i = attributeFilterStart[level]; i < attributeFilterEnd[level]; i++) {
							if(attributeMatches[i] == level) {
								
								// check if wildcard
								if(attributeFilters[i].paths[level].length == 1 && attributeFilters[i].paths[level][0] == '*') {
									// pass through
								} else {
									if(attributeFilters[i].paths[level].length == attributeSourceStart - attributeNameStart) {
										for(int k = 0; k < attributeFilters[i].paths[level].length; k++) {
											if(attributeFilters[i].paths[level][k] != chars[attributeNameStart + k]) {
												continue filterAttribute;
											}
										}
									} else {
										continue filterAttribute;
									}
								}
								
								buffer.append(chars, attributeSourceStart, 2); // =" or ='
								buffer.append(FILTER_ANONYMIZE_MESSAGE);
								attributeSourceStart = offset;

								break filterAttribute;

							}
						}

					// flush remainer
					if(attributeSourceStart < offset) {
						buffer.append(chars, attributeSourceStart, offset - attributeSourceStart);
						attributeSourceStart = offset;
					}
				}	
				offset++;
			}
			offset++;
			
			// complete start tag
			if(attributeSourceStart < offset) {
				buffer.append(chars, attributeSourceStart, offset - attributeSourceStart);
			}

		} else {
			offset = scanBeyondStartElementEnd(chars, offset, length);
			
			// complete start tag
			buffer.append(chars, sourceStart, offset - sourceStart);
		}
		return offset;
	}
	
	protected boolean matchElements(final char[] chars, int offset, int sourceStart, int level, final int[] elementMatches) {
		boolean match = false;
		
		matching:
		for(int i = elementFilterStart[level]; i < elementMatches.length; i++) {
			if(elementMatches[i] == level - 1) {
				
				if(elementMatches[i] >= elementFilters[i].paths.length) {
					// this filter is at the maximum
					continue;
				}

				if(elementFilters[i].paths[elementMatches[i]].length == 1 && elementFilters[i].paths[elementMatches[i]][0] == '*') {
					// pass through
				} else {
					if(elementFilters[i].paths[elementMatches[i]].length != offset - sourceStart - 1) {
						continue;
					}
					for(int k = 0; k < offset - sourceStart - 1; k++) {
						if(elementFilters[i].paths[elementMatches[i]][k] != chars[sourceStart + 1 + k]) {
							continue matching;
						}
					}
				}
				
				elementMatches[i]++;
				
				if(i < elementFilterEnd[level]) {
					match = true;
				}

			}
		}
		return match;
			
	}

	/**
	 * Note that the order or the filters establishes precedence (prune over anon).
	 * 
	 * @param chars XML characters
	 * @param offset XML characters end position
	 * @param sourceStart XML characters start position
	 * @return the matching filter type, or null if none
	 */
	
	protected FilterType matchAnyElements(final char[] chars, int offset, int sourceStart) {
		anyFilters:
		for(int i = 0; i < anyElementFilters.length; i++) {
			if(anyElementFilters[i].path.length != offset - sourceStart - 1) {
				continue;
			}
			for(int k = 0; k < anyElementFilters[i].path.length; k++) {
				if(anyElementFilters[i].path[k] != chars[sourceStart + 1 + k]) {
					continue anyFilters;
				}
			}
			
			return anyElementFilters[i].getFilterType();
		}
		return null;
			
	}

}
