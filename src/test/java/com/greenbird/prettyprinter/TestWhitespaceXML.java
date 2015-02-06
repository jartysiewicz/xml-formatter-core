package com.greenbird.prettyprinter;

import org.junit.Test;

import com.greenbird.xml.prettyprinter.plain.ws.PlainIndentedPrettyPrinter;
import com.greenbird.xml.prettyprinter.plain.ws.RobustPlainIndentedPrettyPrinter;

/** test whitespace which is not result of pretty-printing */

public class TestWhitespaceXML extends AbstractNodeTest {

	private static final String cdataInput = "cdata/whitespace";
	private static final String cdataOutput = "cdata/whitespace/indented";

	private static final String commentsInput = "comments/whitespace";
	private static final String commentsOutput = "comments/whitespace/indented";
	
	private static final String processingInstructionsInput = "processingInstructions/whitespace";
	private static final String processingInstructionsOutput = "processingInstructions/whitespace/indented";
	
	@Test
	public void testCData() throws Exception {
		process(cdataInput, cdataOutput, new PlainIndentedPrettyPrinter(true, '\t', 1), true, 1, false);
		
		process(cdataInput, cdataOutput, new RobustPlainIndentedPrettyPrinter(true, true, true, true, true, true, true, '\t', 1), true, 1, false);
	}

	@Test
	public void testComments() throws Exception {
		process(commentsInput, commentsOutput, new PlainIndentedPrettyPrinter(true, '\t', 1), true, 1, false);
		
		process(commentsInput, commentsOutput, new RobustPlainIndentedPrettyPrinter(true, true, true, true, true, true, true, '\t', 1), true, 1, false);
	}

	@Test
	public void testProcessingInstructions() throws Exception {
		process(processingInstructionsInput, processingInstructionsOutput, new PlainIndentedPrettyPrinter(true, '\t', 1), true, 1, false);
		
		process(processingInstructionsInput, processingInstructionsOutput, new RobustPlainIndentedPrettyPrinter(true, true, true, true, true, true, true, '\t', 1), true, 1, false);
	}
}
