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

import java.io.IOException;
import java.io.Reader;

/**
 * Interface for pretty-printing operations on XML as text.
 * 
 * @author Thomas Rorvik Skjolberg (thomas.skjoelberg@greenbird.com)
 * 
 */

public interface PrettyPrinter {

	/**
	 * Pretty print XML characters to an output StringBuilder.
	 * 
	 * @param chars characters containing XML to be pretty printed
	 * @param output the buffer to which indented XML is appended
	 * @return true if pretty printing was successful. If false, the output buffer is unaffected.
	 */

	boolean process(String chars, StringBuilder output);

	/**
	 * Pretty print XML characters to an output StringBuilder.
	 * 
	 * @param chars characters containing XML to be pretty printed
	 * @param offset the offset within the chars where the XML starts
	 * @param length the length of the XML within the chars
	 * @param output the buffer to which indented XML is appended
	 * @return true if pretty printing was successful. If false, the output buffer is unaffected.
	 */

	boolean process(char[] chars, int offset, int length, StringBuilder output);

	/**
	 * Pretty print XML characters to an output StringBuilder.
	 * 
	 * @param reader reader containing XML characters to be pretty printed
	 * @param length the number of characters within the reader
	 * @param output the buffer to which indented XML is appended
	 * @throws IOException from reader
	 * @return true if pretty printing was successful. If false, the output buffer is unaffected.
	 */

	boolean process(Reader reader, int length, StringBuilder output) throws IOException;
}
