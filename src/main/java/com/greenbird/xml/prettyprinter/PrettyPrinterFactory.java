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

package com.greenbird.xml.prettyprinter;

import java.util.List;

import com.greenbird.xml.prettyprinter.plain.PlainPrettyPrinter;
import com.greenbird.xml.prettyprinter.plain.PlainPrettyPrinterForCDataAndComments;
import com.greenbird.xml.prettyprinter.plain.PlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength;
import com.greenbird.xml.prettyprinter.plain.PlainPrettyPrinterForTextNodesWithXML;
import com.greenbird.xml.prettyprinter.plain.PlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength;
import com.greenbird.xml.prettyprinter.plain.PlainPrettyPrinterWithMaxNodeLength;
import com.greenbird.xml.prettyprinter.plain.RobustPlainPrettyPrinter;
import com.greenbird.xml.prettyprinter.plain.filter.AbstractFilterPrettyPrinter;
import com.greenbird.xml.prettyprinter.plain.filter.AbstractMultiFilterPrettyPrinter.FilterType;
import com.greenbird.xml.prettyprinter.plain.filter.MultiFilterPlainPrettyPrinter;
import com.greenbird.xml.prettyprinter.plain.filter.MultiFilterPlainPrettyPrinterForCDataAndComments;
import com.greenbird.xml.prettyprinter.plain.filter.MultiFilterPlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength;
import com.greenbird.xml.prettyprinter.plain.filter.MultiFilterPlainPrettyPrinterForTextNodesWithXML;
import com.greenbird.xml.prettyprinter.plain.filter.MultiFilterPlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength;
import com.greenbird.xml.prettyprinter.plain.filter.MultiFilterPlainPrettyPrinterWithMaxNodeLength;
import com.greenbird.xml.prettyprinter.plain.filter.MultiFilterRobustPlainPrettyPrinter;
import com.greenbird.xml.prettyprinter.plain.filter.SingleFilterPlainPrettyPrinter;
import com.greenbird.xml.prettyprinter.plain.filter.SingleFilterPlainPrettyPrinterWithMaxNodeLength;
import com.greenbird.xml.prettyprinter.plain.ws.PlainIndentedPrettyPrinter;
import com.greenbird.xml.prettyprinter.plain.ws.RobustPlainIndentedPrettyPrinter;

public class PrettyPrinterFactory {
	
	private boolean robustness = false;
	
	private boolean ignoreWhitespace = false;

	private boolean prettyPrintCData = false;
	private boolean prettyPrintComments = false;
	
	private boolean prettyPrintTextNodes = false; // i.e. XML only

	private boolean xmlDeclaration = false;

	private int maxTextNodeLength = -1;
	private int maxCDATANodeLength = -1;
	
	private String[] anonymizeFilters;
	private String[] pruneFilters;
	
	protected char indentationCharacter = AbstractFilterPrettyPrinter.defaultIndentationCharacter;
	protected int indentationMultiplier = AbstractFilterPrettyPrinter.defaultIndentationMultiplier;

	/** cached instance */
	private PrettyPrinter prettyPrinter;

	/**
	 * Spawn a factory instance. Equivalent to using the default constructor.
	 * 
	 * @return newly created {@linkplain PrettyPrinterFactory}.
	 */
	
	public static PrettyPrinterFactory newInstance() {
		return new PrettyPrinterFactory();
	}

	/**
	 * Spawn a pretty printer. 
	 * 
	 * @return new, or previously created, thread-safe pretty printer
	 */
	
	public PrettyPrinter newPrettyPrinter() {
		if(prettyPrinter == null) {
			prettyPrinter = configurePrettyPrinter();
		}
		
		return prettyPrinter;
	}

	private PrettyPrinter configurePrettyPrinter() {
		if((anonymizeFilters != null && anonymizeFilters.length > 0) || (pruneFilters != null && pruneFilters.length > 0)) {
			return configurePrettyPrinterWithFilters();
		}
		
		if(robustness && !prettyPrintCData && !prettyPrintComments && !prettyPrintTextNodes) {
			throw new IllegalArgumentException("The robustness parameter applies only to CDATA, text and comment pretty printing");
		}
		
		if(ignoreWhitespace) {
			if(maxCDATANodeLength != -1 || maxTextNodeLength != -1) {
				throw new IllegalArgumentException("Max CDATA or text node length not supported while removing existing indentation");
			}

			if(prettyPrintComments|| prettyPrintCData || prettyPrintTextNodes) {
				return new RobustPlainIndentedPrettyPrinter(prettyPrintTextNodes, robustness, prettyPrintCData, robustness, prettyPrintComments, robustness, xmlDeclaration, indentationCharacter, indentationMultiplier);
			}
			
			return new PlainIndentedPrettyPrinter(xmlDeclaration, indentationCharacter, indentationMultiplier);
		}

		if(robustness) {
			if(!prettyPrintCData && !prettyPrintComments) {
				// pretty print xml in text nodes
				if(maxCDATANodeLength != -1 || maxTextNodeLength != -1) {
					return new PlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength(false, false, true, xmlDeclaration, maxTextNodeLength, maxCDATANodeLength, indentationCharacter, indentationMultiplier);
				}
				return new PlainPrettyPrinterForTextNodesWithXML(false, false, true, xmlDeclaration, indentationCharacter, indentationMultiplier);
			}

			if(maxCDATANodeLength != -1 || maxTextNodeLength != -1) {
				return new RobustPlainPrettyPrinter(prettyPrintTextNodes, prettyPrintTextNodes, prettyPrintCData, prettyPrintCData, prettyPrintComments, prettyPrintComments, xmlDeclaration, maxTextNodeLength, maxCDATANodeLength, indentationCharacter, indentationMultiplier);
			} else {
				return new RobustPlainPrettyPrinter(prettyPrintTextNodes, prettyPrintTextNodes, prettyPrintCData, prettyPrintCData, prettyPrintComments, prettyPrintComments, xmlDeclaration, indentationCharacter, indentationMultiplier);
			}
		}

		if(prettyPrintTextNodes) {
			if(maxCDATANodeLength != -1 || maxTextNodeLength != -1) {
				return new PlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength(prettyPrintCData, prettyPrintComments, false, xmlDeclaration, maxTextNodeLength, maxCDATANodeLength, indentationCharacter, indentationMultiplier);
			}
			return new PlainPrettyPrinterForTextNodesWithXML(prettyPrintCData, prettyPrintComments, false, xmlDeclaration, indentationCharacter, indentationMultiplier);
		}

		if(prettyPrintCData || prettyPrintComments) {
			if(maxCDATANodeLength != -1 || maxTextNodeLength != -1) {
				return new PlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength(prettyPrintCData, prettyPrintComments, xmlDeclaration, maxTextNodeLength, maxCDATANodeLength, indentationCharacter, indentationMultiplier);
			}
			return new PlainPrettyPrinterForCDataAndComments(prettyPrintCData, prettyPrintComments, xmlDeclaration, indentationCharacter, indentationMultiplier);
		}

		if(maxCDATANodeLength != -1 || maxTextNodeLength != -1) {
			return new PlainPrettyPrinterWithMaxNodeLength(xmlDeclaration, maxTextNodeLength, maxCDATANodeLength, indentationCharacter, indentationMultiplier);
		}
		return new PlainPrettyPrinter(xmlDeclaration, indentationCharacter, indentationMultiplier);
	}
	
	private PrettyPrinter configurePrettyPrinterWithFilters() {
		if(ignoreWhitespace) {
			throw new IllegalArgumentException("No ignore of whitespace with filtering");
		}

		if(robustness && !prettyPrintCData && !prettyPrintComments && !prettyPrintTextNodes) {
			throw new IllegalArgumentException("The robustness parameter applies only to CDATA, text and comment pretty printing");
		}
				
		if(robustness) {
			if(!prettyPrintCData && !prettyPrintComments) {
				// pretty print text nodes
				if(maxCDATANodeLength != -1 || maxTextNodeLength != -1) {
					return new MultiFilterPlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength(false, false, true, xmlDeclaration, maxTextNodeLength, maxCDATANodeLength, anonymizeFilters, pruneFilters, indentationCharacter, indentationMultiplier);
				}
				return new MultiFilterPlainPrettyPrinterForTextNodesWithXML(false, false, true, xmlDeclaration, anonymizeFilters, pruneFilters, indentationCharacter, indentationMultiplier);
			}
			
			if(maxCDATANodeLength != -1 || maxTextNodeLength != -1) {
				return new MultiFilterRobustPlainPrettyPrinter(prettyPrintTextNodes, prettyPrintTextNodes, prettyPrintCData, prettyPrintCData, prettyPrintComments, prettyPrintComments, xmlDeclaration, maxTextNodeLength, maxCDATANodeLength, anonymizeFilters, pruneFilters, indentationCharacter, indentationMultiplier);
			} else {
				return new MultiFilterRobustPlainPrettyPrinter(prettyPrintTextNodes, prettyPrintTextNodes, prettyPrintCData, prettyPrintCData, prettyPrintComments, prettyPrintComments, xmlDeclaration, anonymizeFilters, pruneFilters, indentationCharacter, indentationMultiplier);
			}
		}

		if(prettyPrintTextNodes) {
			if(maxCDATANodeLength != -1 || maxTextNodeLength != -1) {
				return new MultiFilterPlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength(prettyPrintCData, prettyPrintComments, robustness, xmlDeclaration, maxTextNodeLength, maxCDATANodeLength, anonymizeFilters, pruneFilters, indentationCharacter, indentationMultiplier);
			}
			return new MultiFilterPlainPrettyPrinterForTextNodesWithXML(prettyPrintCData, prettyPrintComments, robustness, xmlDeclaration, anonymizeFilters, pruneFilters, indentationCharacter, indentationMultiplier);
		}

		if(prettyPrintCData || prettyPrintComments) {
			if(maxCDATANodeLength != -1 || maxTextNodeLength != -1) {
				return new MultiFilterPlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength(prettyPrintCData, prettyPrintComments, xmlDeclaration, maxTextNodeLength, maxCDATANodeLength, anonymizeFilters, pruneFilters, indentationCharacter, indentationMultiplier);
			}
			return new MultiFilterPlainPrettyPrinterForCDataAndComments(prettyPrintCData, prettyPrintComments, xmlDeclaration, anonymizeFilters, pruneFilters, indentationCharacter, indentationMultiplier);
		}

		if(maxCDATANodeLength != -1 || maxTextNodeLength != -1) {
			
			// check for single prune/anon filter
			if((anonymizeFilters == null || anonymizeFilters.length == 0) && pruneFilters.length == 1 && !pruneFilters[0].startsWith(AbstractFilterPrettyPrinter.ANY_PREFIX)) {
				return new SingleFilterPlainPrettyPrinterWithMaxNodeLength(xmlDeclaration, pruneFilters[0], FilterType.PRUNE, maxTextNodeLength, maxCDATANodeLength, indentationCharacter, indentationMultiplier);
			} else if((pruneFilters == null || pruneFilters.length == 0)  && anonymizeFilters.length == 1 && !anonymizeFilters[0].startsWith(AbstractFilterPrettyPrinter.ANY_PREFIX)) {
				return new SingleFilterPlainPrettyPrinterWithMaxNodeLength(xmlDeclaration, anonymizeFilters[0], FilterType.ANON, maxTextNodeLength, maxCDATANodeLength, indentationCharacter, indentationMultiplier);
			}
			
			return new MultiFilterPlainPrettyPrinterWithMaxNodeLength(xmlDeclaration, maxTextNodeLength, maxCDATANodeLength, anonymizeFilters, pruneFilters, indentationCharacter, indentationMultiplier);
		}
		
		// check for single prune/anon filter
		if((anonymizeFilters == null || anonymizeFilters.length == 0) && pruneFilters.length == 1 && !pruneFilters[0].startsWith(AbstractFilterPrettyPrinter.ANY_PREFIX)) {
			return new SingleFilterPlainPrettyPrinter(xmlDeclaration, pruneFilters[0], FilterType.PRUNE, indentationCharacter, indentationMultiplier);
		} else if((pruneFilters == null || pruneFilters.length == 0)  && anonymizeFilters.length == 1 && !anonymizeFilters[0].startsWith(AbstractFilterPrettyPrinter.ANY_PREFIX)) {
			return new SingleFilterPlainPrettyPrinter(xmlDeclaration, anonymizeFilters[0], FilterType.ANON, indentationCharacter, indentationMultiplier);
		}

		return new MultiFilterPlainPrettyPrinter(xmlDeclaration, anonymizeFilters, pruneFilters, indentationCharacter, indentationMultiplier);

	}

	private void clearPrettyPrinter() {
		prettyPrinter = null;
	}

	public boolean isIgnoreWhitespace() {
		return ignoreWhitespace;
	}

	/**
	 * Remove any 'ignorable' whitespace already present in the input XML
	 * 
	 * @param ignore true if whitespace is present
	 */
	
	
	public void setIgnoreWhitespace(boolean ignore) {
		clearPrettyPrinter();
		this.ignoreWhitespace = ignore;
	}

	public boolean isPrettyPrintCData() {
		return prettyPrintCData;
	}

	/**
	 * Format XML within CDATA nodes
	 * 
	 * @param prettyPrintCData true if enabled
	 */
	
	public void setPrettyPrintCData(boolean prettyPrintCData) {
		clearPrettyPrinter();
		this.prettyPrintCData = prettyPrintCData;
	}

	public boolean isPrettyPrintComments() {
		return prettyPrintComments;
	}

	/**
	 * Format XML within comments
	 * 
	 * @param prettyPrintComments true if enabled
	 */
	
	public void setPrettyPrintComments(boolean prettyPrintComments) {
		clearPrettyPrinter();
		this.prettyPrintComments = prettyPrintComments;
	}

	public boolean isPrettyPrintTextNodes() {
		return prettyPrintTextNodes;
	}
	
	/**
	 * Format XML within text
	 * 
	 * @param prettyPrintTextNodes true if enabled
	 */ 

	public void setPrettyPrintTextNodes(boolean prettyPrintTextNodes) {
		clearPrettyPrinter();
		this.prettyPrintTextNodes = prettyPrintTextNodes;
	}
	
	/**
	 * Keep any XML declarations
	 * 
	 * @param xmlDeclaration true if enabled
	 */
	
	
	public void setXmlDeclaration(boolean xmlDeclaration) {
		clearPrettyPrinter();
		this.xmlDeclaration = xmlDeclaration;
	}

	public boolean isRobustness() {
		return robustness;
	}

	/**
	 * 
	 * Ignore any well-formed issues when pretty-printing XML within text, CDATA or comment nodes
	 * 
	 * @param robustness true if enabled
	 */
	
	
	public void setRobustness(boolean robustness) {
		clearPrettyPrinter();
		this.robustness = robustness;
	}

	/**
	 * 
	 * Set maximum text node length (if not pretty-printed)
	 * 
	 * Note that truncation of nodes below 25 chars will not reduce node size.
	 * 
	 * @param maxTextNodeLength max text node length
	 */

	
	public void setMaxTextNodeLength(int maxTextNodeLength) {
		clearPrettyPrinter();
		this.maxTextNodeLength = maxTextNodeLength;
	}
	
	public int getMaxTextNodeLength() {
		return maxTextNodeLength;
	}
	
	/**
	 * Set maximum CDATA node length (if not pretty-printed)
	 * 
	 * Note that truncation of nodes below 25 chars will not reduce node size.
	 * 
	 * @param maxCDATANodeLength max CDATA node length
	 */
	
	public void setMaxCDATANodeLength(int maxCDATANodeLength) {
		clearPrettyPrinter();
		this.maxCDATANodeLength = maxCDATANodeLength;
	}
	
	public int getMaxCDATANodeLength() {
		return maxCDATANodeLength;
	}
	
	public boolean isXmlDeclaration() {
		return xmlDeclaration;
	}
	
	/**
	 * Set prune expressions
	 * 
	 * @param filters array of prune expressions
	 */
	
	public void setPruneFilters(String[] filters) {
		clearPrettyPrinter();
		
		if(filters != null) {
			AbstractFilterPrettyPrinter.validateAnonymizeExpressions(filters);
		}
		
		this.pruneFilters = filters;
	}
	
	/**
	 * Set prune expressions
	 * 
	 * @param filters list of prune expressions
	 */
	
	public void setPruneFilterList(List<String> filters) {
		if(filters != null) {
			setPruneFilters(filters.toArray(new String[filters.size()]));
		} else {
			setPruneFilters(null);
		}
	}
	
	public String[] getPruneFilters() {
		return pruneFilters;
	}
	
	/**
	 * Set anonymize filters
	 * 
	 * @param filters array of anonymize filters
	 */
	
	public void setAnonymizeFilters(String[] filters) {
		clearPrettyPrinter();
		
		if(filters != null) {
			AbstractFilterPrettyPrinter.validateAnonymizeExpressions(filters);
		}
		
		this.anonymizeFilters = filters;
	}
	
	/**
	 * 
	 * Set anonymize filters
	 * 
	 * @param filters list of anonymize filters
	 */
	
	public void setAnonymizeFilterList(List<String> filters) {
		if(filters != null) {
			setAnonymizeFilters(filters.toArray(new String[filters.size()]));
		} else {
			setAnonymizeFilters(null);
		}
	}
	
	public String[] getAnonymizeFilters() {
		return anonymizeFilters;
	}
	
	public void setIndentationMultiplier(int indentationMultiplier) {
		this.indentationMultiplier = indentationMultiplier;
	}
	
	public int getIndentationMultiplier() {
		return indentationMultiplier;
	}

	public void setIndentationCharacter(char indentationCharacter) {
		this.indentationCharacter = indentationCharacter;
	}
	
	public char getIndentationCharacter() {
		return indentationCharacter;
	}
}
