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

public class PrettyPrinterBuilder {

	public static PrettyPrinterBuilder newPrettyPrinter() {
		return new PrettyPrinterBuilder();
	}
	
	private PrettyPrinterFactory factory = new PrettyPrinterFactory();
	
	public PrettyPrinter build() {
		return factory.newPrettyPrinter();
	}
	
	public PrettyPrinterBuilder robust() {
		factory.setRobustness(true);
		
		return this;
	}

	public PrettyPrinterBuilder ignoreWhitespace() {
		factory.setIgnoreWhitespace(true);
		
		return this;
	}

	public PrettyPrinterBuilder prettyPrintCData() {
		factory.setPrettyPrintCData(true);
		
		return this;
	}

	public PrettyPrinterBuilder prettyPrintComments() {
		factory.setPrettyPrintComments(true);
		
		return this;
	}

	public PrettyPrinterBuilder prettyPrintTextNodes() {
		factory.setPrettyPrintTextNodes(true);
		
		return this;
	}

	public PrettyPrinterBuilder keepXMLDeclaration() {
		factory.setXmlDeclaration(true);
		
		return this;
	}
	
	public PrettyPrinterBuilder maxCDataNodeLength(int length) {
		factory.setMaxCDATANodeLength(length);
		
		return this;
	}

	public PrettyPrinterBuilder maxTextNodeLength(int length) {
		factory.setMaxTextNodeLength(length);
		
		return this;
	}

	public PrettyPrinterBuilder maxNodeLength(int length) {
		factory.setMaxTextNodeLength(length);
		factory.setMaxCDATANodeLength(length);
		
		return this;
	}
	
	public PrettyPrinterBuilder prune(String[] filter) {
		factory.setPruneFilters(filter);
		
		return this;
	}
	
	public PrettyPrinterBuilder anonymize(String[] filter) {
		factory.setAnonymizeFilters(filter);
		
		return this;
	}

	public PrettyPrinterBuilder indentate(char indentationCharacter, int indentationMultiplier) {
		factory.setIndentationCharacter(indentationCharacter);
		factory.setIndentationMultiplier(indentationMultiplier);
		
		return this;
	}

}
