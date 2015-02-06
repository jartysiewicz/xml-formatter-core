package com.greenbird.prettyprinter;

import java.util.List;

import org.junit.Test;

import com.greenbird.prettyprinter.utils.PrettyPrinterConstructor;
import com.greenbird.xml.prettyprinter.PrettyPrinter;
import com.greenbird.xml.prettyprinter.plain.AbstractPrettyPrinter;
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
 * Verify / expose certain cases where an ordinary XML parser would have refused parsing, but pretty-printing still occurs.
 * 
 * @author thomas
 *
 */

public class TestInvalidXML extends AbstractNodeTest {
 
	private static final String input = "invalid/";
	private static final String output = "invalid/indented/";

	private static final String robustInput = "robust/";
	private static final String robustOutput = "robust/indented/";

	@Test
	public void testPlainPrettyPrinter() throws Exception {
		run(PlainPrettyPrinter.class);
	}

	@Test
	public void testPlainPrettyPrinterWithMaxNodeLength() throws Exception {
		run(PlainPrettyPrinterWithMaxNodeLength.class);
	}
	
	@Test
	public void testPlainPrettyPrinterForCDataAndComments() throws Exception {
		run(PlainPrettyPrinterForCDataAndComments.class);
	}
	
	@Test
	public void testPlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength() throws Exception {
		run(PlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength.class);
	}
	
	@Test
	public void testPlainPrettyPrinterForTextNodesWithXML() throws Exception {
		run(PlainPrettyPrinterForTextNodesWithXML.class);
	}
	
	@Test
	public void testPlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength() throws Exception {
		run(PlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength.class);
	}

	@Test
	public void testRobustPlainPrettyPrinter() throws Exception {
		run(RobustPlainPrettyPrinter.class);
	}

	@Test
	public void testPlainIndentedPrettyPrinter() throws Exception {
		run(PlainIndentedPrettyPrinter.class);
	}

	@Test
	public void testFilterSinglePlainPrettyPrinter() throws Exception {
		process(input, new SingleFilterPlainPrettyPrinter(false, "/aparent/achild", FilterType.ANON, '\t', 1), true);
		process(input, new SingleFilterPlainPrettyPrinter(false, "/aparent/achild", FilterType.PRUNE, '\t', 1), true);
	}

	@Test
	public void testFilterSinglePlainPrettyPrinterWithMaxLength() throws Exception {
		process(input, new SingleFilterPlainPrettyPrinterWithMaxNodeLength(false, "/aparent/achild", FilterType.ANON, 26, 26, '\t', 1), true);
		process(input, new SingleFilterPlainPrettyPrinterWithMaxNodeLength(false, "/aparent/achild", FilterType.PRUNE, 26, 26, '\t', 1), true);
	}

	@Test
	public void testFilterMultiPlainPrettyPrinterForCDataAndComments() throws Exception {
		run(MultiFilterPlainPrettyPrinterForCDataAndComments.class);
	}

	@Test
	public void testFilterMultiPlainPrettyPrinterForCDataAndCommentsWithMaxLength() throws Exception {
		run(MultiFilterPlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength.class);
	}

	@Test
	public void testFilterMultiPlainPrettyPrinter() throws Exception {
		run(MultiFilterPlainPrettyPrinter.class);
	}

	@Test
	public void testFilterMultiPlainPrettyPrinterWithMaxNodeLength() throws Exception {
		run(MultiFilterPlainPrettyPrinterWithMaxNodeLength.class);
	}

	@Test
	public void testFilterMultiPlainPrettyPrinterForTextNodesWithXML() throws Exception {
		run(MultiFilterPlainPrettyPrinterForTextNodesWithXML.class);
	}

	@Test
	public void testFilterMultiPlainPrettyPrinterForTextNodesWithXMLAndMaxLength() throws Exception {
		run(MultiFilterPlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength.class);
	}

	@Test
	public void testMultiFilterRobustPlainIndentedPrettyPrinter() throws Exception {
		run(MultiFilterRobustPlainPrettyPrinter.class);
	}

	@Test
	public void testRobustPlainIndentedPrettyPrinter() throws Exception {
		run(RobustPlainIndentedPrettyPrinter.class);
	}

	private void run(Class cls) throws Exception {
		List<PrettyPrinter> construct = PrettyPrinterConstructor.getConstructor().construct(cls);
		for(PrettyPrinter pp : construct) {
			process(input, output, pp, false, AbstractPrettyPrinter.defaultIndentationMultiplier, false);
		}
	}


}
