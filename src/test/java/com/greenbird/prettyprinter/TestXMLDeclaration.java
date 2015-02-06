package com.greenbird.prettyprinter;

import org.junit.Test;

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
 * Test declarations here so that we need not double up all the resource file originals.
 * 
 * @author thomas
 *
 */

public class TestXMLDeclaration extends AbstractNodeTest {
	
	private static final String declarationInput = "declaration/";
	private static final String declarationOutputKeepDeclaration = "declaration/keep/";
	private static final String declarationOutputRemoveDeclaration = "declaration/remove/";

	@Test
	public void testPlainPrettyPrinter() throws Exception {
		process(declarationInput, declarationOutputKeepDeclaration, new PlainPrettyPrinter(true), true, 1, true);
		process(declarationInput, declarationOutputRemoveDeclaration, new PlainPrettyPrinter(false), true, 1, false);
	}

	@Test
	public void testPlainPrettyPrinterForCDataAndComments() throws Exception {
		process(declarationInput, declarationOutputKeepDeclaration, new PlainPrettyPrinterForCDataAndComments(true, true, true, '\t', 1), true, 1, true);
		process(declarationInput, declarationOutputRemoveDeclaration, new PlainPrettyPrinterForCDataAndComments(true, true, false, '\t', 1), true, 1, false);
	}

	@Test
	public void testPlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength() throws Exception {
		process(declarationInput, declarationOutputKeepDeclaration, new PlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength(true, true, true, 1024, 1024, '\t', 1), true, 1, true);
		process(declarationInput, declarationOutputRemoveDeclaration, new PlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength(true, true, false, 1024, 1024, '\t', 1), true, 1, false);
	}
	
	@Test
	public void testPlainPrettyPrinterForTextNodesWithXML() throws Exception {
		process(declarationInput, declarationOutputKeepDeclaration, new PlainPrettyPrinterForTextNodesWithXML(true, true, true, true, '\t', 1), true, 1, true);
		process(declarationInput, declarationOutputRemoveDeclaration, new PlainPrettyPrinterForTextNodesWithXML(true, true, true, false, '\t', 1), true, 1, false);
	}
	
	@Test
	public void testPlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength() throws Exception {
		process(declarationInput, declarationOutputKeepDeclaration, new PlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength(true, true, true, true, 1024, 1024, '\t', 1), true, 1, true);
		process(declarationInput, declarationOutputRemoveDeclaration, new PlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength(true, true, true, false, 1024, 1024, '\t', 1), true, 1, false);
	}
	
	@Test
	public void testPlainPrettyPrinterWithMaxNodeLength() throws Exception {
		process(declarationInput, declarationOutputKeepDeclaration, new PlainPrettyPrinterWithMaxNodeLength(true, 1024, 1024, '\t', 1), true, 1, true);
		process(declarationInput, declarationOutputRemoveDeclaration, new PlainPrettyPrinterWithMaxNodeLength(false, 1024, 1024, '\t', 1), true, 1, false);
	}
	
	@Test
	public void testRobustPlainPrettyPrinter() throws Exception {
		process(declarationInput, declarationOutputKeepDeclaration, new RobustPlainPrettyPrinter(true, true, true, true, true, true, true, 1024, 1024, '\t', 1), true, 1, true);
		process(declarationInput, declarationOutputRemoveDeclaration, new RobustPlainPrettyPrinter(true, true, true, true, true, true, false, 1024, 1024, '\t', 1), true, 1, false);
	}
	
	@Test
	public void testMultiFilterPlainPrettyPrinter() throws Exception {
		process(declarationInput, declarationOutputKeepDeclaration, new MultiFilterPlainPrettyPrinter(true, new String[]{"/a/b"}, new String[]{"/a/b"}, '\t', 1), true, 1, true);
		process(declarationInput, declarationOutputRemoveDeclaration, new MultiFilterPlainPrettyPrinter(false, new String[]{"/a/b"}, new String[]{"/a/b"}, '\t', 1), true, 1, false);
	}

	@Test
	public void testMultiFilterPlainPrettyPrinterForCDataAndComments() throws Exception {
		process(declarationInput, declarationOutputKeepDeclaration, new MultiFilterPlainPrettyPrinterForCDataAndComments(true, true, true, new String[]{"/a/b"}, new String[]{"/a/b"}, '\t', 1), true, 1, true);
		process(declarationInput, declarationOutputRemoveDeclaration, new MultiFilterPlainPrettyPrinterForCDataAndComments(true, true, false, new String[]{"/a/b"}, new String[]{"/a/b"}, '\t', 1), true, 1, false);
	}

	@Test
	public void testMultiFilterPlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength() throws Exception {
		process(declarationInput, declarationOutputKeepDeclaration, new MultiFilterPlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength(true, true, true, 1024, 1024, new String[]{"/a/b"}, new String[]{"/a/b"}, '\t', 1), true, 1, true);
		process(declarationInput, declarationOutputRemoveDeclaration, new MultiFilterPlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength(true, true, false, 1024, 1024, new String[]{"/a/b"}, new String[]{"/a/b"}, '\t', 1), true, 1, false);
	}

	@Test
	public void testMultiFilterPlainPrettyPrinterForTextNodesWithXML() throws Exception {
		process(declarationInput, declarationOutputKeepDeclaration, new MultiFilterPlainPrettyPrinterForTextNodesWithXML(true, true, true, true, new String[]{"/a/b"}, new String[]{"/a/b"}, '\t', 1), true, 1, true);
		process(declarationInput, declarationOutputRemoveDeclaration, new MultiFilterPlainPrettyPrinterForTextNodesWithXML(true, true, true, false, new String[]{"/a/b"}, new String[]{"/a/b"}, '\t', 1), true, 1, false);
	}

	@Test
	public void testMultiFilterPlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength() throws Exception {
		process(declarationInput, declarationOutputKeepDeclaration, new MultiFilterPlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength(true, true, true, true, 1024, 1024, new String[]{"/a/b"}, new String[]{"/a/b"}, '\t', 1), true, 1, true);
		process(declarationInput, declarationOutputRemoveDeclaration, new MultiFilterPlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength(true, true, true, false, 1024, 1024, new String[]{"/a/b"}, new String[]{"/a/b"}, '\t', 1), true, 1, false);
	}

	@Test
	public void testMultiFilterPlainPrettyPrinterWithMaxNodeLength() throws Exception {
		process(declarationInput, declarationOutputKeepDeclaration, new MultiFilterPlainPrettyPrinterWithMaxNodeLength(true, 1024, 1024, new String[]{"/a/b"}, new String[]{"/a/b"}, '\t', 1), true, 1, true);
		process(declarationInput, declarationOutputRemoveDeclaration, new MultiFilterPlainPrettyPrinterWithMaxNodeLength(false, 1024, 1024, new String[]{"/a/b"}, new String[]{"/a/b"}, '\t', 1), true, 1, false);
	}

	@Test
	public void testMultiFilterRobustPlainPrettyPrinter() throws Exception {
		process(declarationInput, declarationOutputKeepDeclaration, new MultiFilterRobustPlainPrettyPrinter(true, true, true, true, true, true, true, 1024, 1024, new String[]{"/a/b"}, new String[]{"/a/b"}, '\t', 1), true, 1, true);
		process(declarationInput, declarationOutputRemoveDeclaration, new MultiFilterRobustPlainPrettyPrinter(true, true, true, true, true, true, false, 1024, 1024, new String[]{"/a/b"}, new String[]{"/a/b"}, '\t', 1), true, 1, false);
	}

	@Test
	public void testSingleFilterPlainPrettyPrinter() throws Exception {
		process(declarationInput, declarationOutputKeepDeclaration, new SingleFilterPlainPrettyPrinter(true, "/a/b", FilterType.PRUNE, '\t', 1), true, 1, true);
		process(declarationInput, declarationOutputRemoveDeclaration, new SingleFilterPlainPrettyPrinter(false, "/a/b", FilterType.ANON, '\t', 1), true, 1, false);
	}
	
	@Test
	public void testSingleFilterPlainPrettyPrinterWithMaxNodeLength() throws Exception {
		process(declarationInput, declarationOutputKeepDeclaration, new SingleFilterPlainPrettyPrinterWithMaxNodeLength(true, "/a/b", FilterType.PRUNE, 1024, 1024, '\t', 1), true, 1, true);
		process(declarationInput, declarationOutputRemoveDeclaration, new SingleFilterPlainPrettyPrinterWithMaxNodeLength(false, "/a/b", FilterType.ANON, 1024, 1024, '\t', 1), true, 1, false);
	}
	
	@Test
	public void testPlainIndentedPrettyPrinter() throws Exception {
		process(declarationInput, declarationOutputKeepDeclaration, new PlainIndentedPrettyPrinter(true, '\t', 1), true, 1, true);
		process(declarationInput, declarationOutputRemoveDeclaration, new PlainIndentedPrettyPrinter(false, '\t', 1), true, 1, false);
	}

	@Test
	public void testRobustPlainIndentedPrettyPrinter() throws Exception {
		process(declarationInput, declarationOutputKeepDeclaration, new RobustPlainIndentedPrettyPrinter(true, true, true, true, true, true, true, '\t', 1), true, 1, true);
		process(declarationInput, declarationOutputRemoveDeclaration, new RobustPlainIndentedPrettyPrinter(true, true, true, true, true, true, false, '\t', 1), true, 1, false);
	}

}
