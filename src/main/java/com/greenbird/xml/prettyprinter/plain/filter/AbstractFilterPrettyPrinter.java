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

import com.greenbird.xml.prettyprinter.plain.AbstractPrettyPrinter;

public abstract class AbstractFilterPrettyPrinter extends AbstractPrettyPrinter {

	public static final String FILTER_PRUNE_MESSAGE = "<!-- [SUBTREE REMOVED] -->";
	public static final String FILTER_ANONYMIZE_MESSAGE = "[*****]";
	public static final String FILTER_TRUNCATE_MESSAGE = "...[TRUNCATED BY ";

	/** 
	 * group 1 starting with slash and containing no special chars except star (*). 
	 * optional group 2 starting with slash and containing no special chars except star (*) and at (@), must be last. 
	 */ 
	protected final static String pruneSyntaxAbsolutePath = "^(\\/[^@\\/\\[\\]\\(\\)\\.\\:\\|]+)+(\\/[^@\\/\\[\\]\\(\\)\\.\\:\\|]+)?$"; // slash + non-special chars @/[]().:|
	protected final static String syntaxAnyPath = "^(\\/\\/[^@\\/\\[\\]\\(\\)\\.\\:\\|\\*]+)$"; // 2x slash + non-special chars @/[]().:|*
	protected final static String anonymizeSyntax = "^(\\/[^@\\/\\[\\]\\(\\)\\.\\:\\|]+)+(\\/[^\\/\\[\\]\\(\\)\\.\\:\\|]+)?$"; // slash + non-special chars @/[]().:|

	protected final static String[] EMPTY = new String[]{};
	public final static String ANY_PREFIX = "//";
	
	/** strictly not needed, but necessary for testing */
	protected final String[] anonymizes;
	protected final String[] prunes;
	
	public AbstractFilterPrettyPrinter(boolean declaration, int maxTextNodeLength, int maxCDATANodeLength, String[] anonymizes, String[] prunes, char indentationCharacter, int indentationMultiplier) {
		super(declaration, maxTextNodeLength, maxCDATANodeLength, indentationCharacter, indentationMultiplier);
		
		validate(anonymizes, prunes);

		this.anonymizes = anonymizes;
		this.prunes = prunes;
	}

	public AbstractFilterPrettyPrinter(boolean declaration, String[] anonymizes, String[] prunes) {
		this(declaration, Integer.MAX_VALUE, Integer.MAX_VALUE, anonymizes, prunes, defaultIndentationCharacter, defaultIndentationMultiplier);
	}

	protected static void validate(String[] anonymizes, String[] prunes) {
		/*
		if((anonymizes == null || anonymizes.length == 0) && (prunes == null || prunes.length == 0)) {
			throw new IllegalArgumentException();
		}
		*/

		validateAnonymizeExpressions(anonymizes);
		validatePruneExpressions(prunes);
	}

	public static void validateAnonymizeExpressions(String[] expressions) {
		for(String expression : expressions) {
			validateAnonymizeExpression(expression);
		}
	}

	public static void validateAnonymizeExpression(String expression) {
		if(!expression.matches(anonymizeSyntax) && !expression.matches(syntaxAnyPath)) {
			throw new IllegalArgumentException("Illegal expression '" + expression + "'. Expected expression on the form /a/b/c or /a/b/@c with wildcards or //a without wildcards");
		}
	}
	
	public static void validatePruneExpressions(String[] expressions) {
		for(String expression : expressions) {
			validatePruneExpression(expression);
		}
	}

	public static void validatePruneExpression(String expression) {
		if(!expression.matches(pruneSyntaxAbsolutePath) && !expression.matches(syntaxAnyPath) ) {
			throw new IllegalArgumentException("Illegal expression '" + expression + "'. Expected expression on the form /a/b/c with wildcards or //a without wildcards");
		}
	}
	
	protected static char[][] parse(String expression) {
		String[] split = expression.split("/");
		char[][] elementPath = new char[split.length - 1][];
		for(int k = 0; k < elementPath.length; k++) {
			elementPath[k] = split[k + 1].toCharArray();
		}
		return elementPath;
	}

	public String[] getAnonymizeFilters() {
		return anonymizes;
	}

	public String[] getPruneFilters() {
		return prunes;
	}

	protected int scanToEndOfAttributeValue(final char[] chars, int offset, int offsetBounds) {
		// scan to end of attribute value
		if(chars[offset + 1] == '"') {
			offset += 2; // skip =" or ='
			while(chars[offset] != '"') {
				offset++;
			}
		} else {
			offset += 2; // skip =" or ='
			while(chars[offset] != '\'') {
				offset++;
			}
		}
		
		// bounds check is really not necessary as long as all recursive calls rip chars into own array first
		// check bounds for inner-xml cases
		if(offset >= offsetBounds) {
			throw new ArrayIndexOutOfBoundsException();
		}
		
		return offset;
	}

	protected boolean isEndOfLocalName(char c) {
		return c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '>' || c == '/';
	}

}
