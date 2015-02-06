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

import com.greenbird.xml.prettyprinter.plain.AbstractPrettyPrinter;

/**
 * 
 * 
 * @author Thomas Rorvik Skjolberg
 *
 */

public abstract class AbstractPlainIgnoreWhitespacePrettyPrinter extends AbstractPrettyPrinter {

	public AbstractPlainIgnoreWhitespacePrettyPrinter(boolean declaration, char indentationCharacter, int indentationMultiplier) {
		super(declaration, indentationCharacter, indentationMultiplier);
	}

	public boolean isIgnoreWhitespace() {
		return true;
	}

}
