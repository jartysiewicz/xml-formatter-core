package com.greenbird.prettyprinter;

import java.util.List;

import org.junit.Test;

import com.greenbird.prettyprinter.utils.PrettyPrinterConstructor;
import com.greenbird.xml.prettyprinter.PrettyPrinter;
import com.greenbird.xml.prettyprinter.plain.PlainPrettyPrinter;
import com.greenbird.xml.prettyprinter.plain.PlainPrettyPrinterForCDataAndComments;
import com.greenbird.xml.prettyprinter.plain.PlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength;
import com.greenbird.xml.prettyprinter.plain.PlainPrettyPrinterForTextNodesWithXML;
import com.greenbird.xml.prettyprinter.plain.PlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength;
import com.greenbird.xml.prettyprinter.plain.PlainPrettyPrinterWithMaxNodeLength;
import com.greenbird.xml.prettyprinter.plain.RobustPlainPrettyPrinter;
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

/**
 * 
 * Test some XML documents which result in no pretty-printing
 * 
 * @author thomas
 *
 */

public class TestFailXML extends AbstractNodeTest {
 
	private static final String input1 = "fail/";
	
	/** for non-recursive pretty-printers */
	private static final String input2 = "fail/trash/";

	/** for non-comment-pretty-printers */
	private static final String input3 = "fail/comments/";

	/** for attribute-aware pretty-printers*/
	private static final String input4 = "fail/attributes/";

	/** for attribute-aware pretty-printers with inner xml*/
	private static final String input5 = "fail/textXML/";

	@Test
	public void testPlainPrettyPrinter() throws Exception {
		run(PlainPrettyPrinter.class, input1);
		
		run(PlainPrettyPrinter.class, input2);
		
		run(PlainPrettyPrinter.class, input3);
	}

	@Test
	public void testPlainPrettyPrinterWithMaxNodeLength() throws Exception {
		run(PlainPrettyPrinterWithMaxNodeLength.class, input1);
		
		run(PlainPrettyPrinterWithMaxNodeLength.class, input2);
		
		run(PlainPrettyPrinterWithMaxNodeLength.class, input3);
	}
	
	@Test
	public void testPlainPrettyPrinterForCDataAndComments() throws Exception {
		run(PlainPrettyPrinterForCDataAndComments.class, input1);
		
		run(PlainPrettyPrinterForCDataAndComments.class, input2);
	}
	
	@Test
	public void testPlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength() throws Exception {
		run(PlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength.class, input1);
		
		run(PlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength.class, input2);
	}
	
	@Test
	public void testPlainPrettyPrinterForTextNodesWithXML() throws Exception {
		run(PlainPrettyPrinterForTextNodesWithXML.class, input1);
		
		process(input5, new PlainPrettyPrinterForTextNodesWithXML(true, true, false, false, '\t', 1), false);
	}
	
	@Test
	public void testPlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength() throws Exception {
		run(PlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength.class, input1);
		
		process(input5, new PlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength(true, true, false, false, Integer.MAX_VALUE, Integer.MAX_VALUE, '\t', 1), false);
	}

	@Test
	public void testRobustPlainPrettyPrinter() throws Exception {
		run(RobustPlainPrettyPrinter.class, input1);
	}

	@Test
	public void testPlainIndentedPrettyPrinter() throws Exception {
		run(PlainIndentedPrettyPrinter.class, input1);
		
		run(PlainIndentedPrettyPrinter.class, input2);
		
		run(PlainIndentedPrettyPrinter.class, input3);
	}

	@Test
	public void testFilterSinglePlainPrettyPrinter() throws Exception {
		process(input1, new SingleFilterPlainPrettyPrinter(false, "/aparent/achild", FilterType.ANON, '\t', 1), false);
		process(input1, new SingleFilterPlainPrettyPrinter(false, "/aparent/achild", FilterType.PRUNE, '\t', 1), false);
		
		process(input2, new SingleFilterPlainPrettyPrinter(false, "/aparent/achild", FilterType.ANON, '\t', 1), false);
		process(input2, new SingleFilterPlainPrettyPrinter(false, "/aparent/achild", FilterType.PRUNE, '\t', 1), false);
		
		process(input3, new SingleFilterPlainPrettyPrinter(false, "/aparent/achild", FilterType.ANON, '\t', 1), false);
		process(input3, new SingleFilterPlainPrettyPrinter(false, "/aparent/achild", FilterType.PRUNE, '\t', 1), false);
	}

	@Test
	public void testSingleFilterPlainPrettyPrinterWithMaxNodeLength() throws Exception {
		process(input1, new SingleFilterPlainPrettyPrinterWithMaxNodeLength(false, "/aparent/achild", FilterType.ANON, 1024, 1024, '\t', 1), false);
		process(input1, new SingleFilterPlainPrettyPrinterWithMaxNodeLength(false, "/aparent/achild", FilterType.PRUNE, 1024, 1024, '\t', 1), false);
		
		process(input2, new SingleFilterPlainPrettyPrinterWithMaxNodeLength(false, "/aparent/achild", FilterType.ANON, 1024, 1024, '\t', 1), false);
		process(input2, new SingleFilterPlainPrettyPrinterWithMaxNodeLength(false, "/aparent/achild", FilterType.PRUNE, 1024, 1024, '\t', 1), false);
		
		process(input3, new SingleFilterPlainPrettyPrinterWithMaxNodeLength(false, "/aparent/achild", FilterType.ANON, 1024, 1024, '\t', 1), false);
		process(input3, new SingleFilterPlainPrettyPrinterWithMaxNodeLength(false, "/aparent/achild", FilterType.PRUNE, 1024, 1024, '\t', 1), false);
	}

	@Test
	public void testFilterMultiPlainPrettyPrinter() throws Exception {
		run(MultiFilterPlainPrettyPrinter.class, input1);
		
		run(MultiFilterPlainPrettyPrinter.class, input2);
		
		run(MultiFilterPlainPrettyPrinter.class, input3);

		process(input4, new MultiFilterPlainPrettyPrinter(false, new String[]{"/aparent/achild/@attr"}, null, '\t', 1), false);
		process(input4, new MultiFilterPlainPrettyPrinter(false, new String[]{"/aparent/achild/@a"}, null, '\t', 1), false);
	}

	@Test
	public void testFilterMultiPlainPrettyPrinterForCDataAndComments() throws Exception {
		run(MultiFilterPlainPrettyPrinterForCDataAndComments.class, input1);
		
		run(MultiFilterPlainPrettyPrinterForCDataAndComments.class, input2);
	}

	@Test
	public void testFilterMultiPlainPrettyPrinterForCDataAndCommentsWithMaxLength() throws Exception {
		run(MultiFilterPlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength.class, input1);
		
		run(MultiFilterPlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength.class, input2);
	}

	@Test
	public void testFilterMultiPlainPrettyPrinterWithMaxNodeLength() throws Exception {
		run(MultiFilterPlainPrettyPrinterWithMaxNodeLength.class, input1);
		
		// TODO
		//run(MultiFilterPlainPrettyPrinterWithMaxNodeLength.class, input2);
		
		run(MultiFilterPlainPrettyPrinterWithMaxNodeLength.class, input3);
	}

	@Test
	public void testFilterMultiPlainPrettyPrinterForTextNodesWithXML() throws Exception {
		run(MultiFilterPlainPrettyPrinterForTextNodesWithXML.class, input1);
		
		process(input5, new MultiFilterPlainPrettyPrinterForTextNodesWithXML(true, true, false, false, new String[]{"/aparent/achild/@a"}, null, '\t', 1), false);

	}

	@Test
	public void testFilterMultiPlainPrettyPrinterForTextNodesWithXMLAndMaxLength() throws Exception {
		run(MultiFilterPlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength.class, input1);
		
		process(input5, new MultiFilterPlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength(true, true, false, false, Integer.MAX_VALUE, Integer.MAX_VALUE, new String[]{"/aparent/achild/@a"}, null, '\t', 1), false);
	}

	@Test
	public void testMultiFilterRobustPlainPrettyPrinter() throws Exception {
		run(MultiFilterRobustPlainPrettyPrinter.class, input1);
	}

	@Test
	public void testRobustPlainIndentedPrettyPrinter() throws Exception {
		run(RobustPlainIndentedPrettyPrinter.class, input1);
	}

	private void run(Class cls, String input) throws Exception {
		List<PrettyPrinter> construct = PrettyPrinterConstructor.getConstructor().construct(cls);
		for(PrettyPrinter pp : construct) {
			process(input, pp, false);
		}
	}
}
