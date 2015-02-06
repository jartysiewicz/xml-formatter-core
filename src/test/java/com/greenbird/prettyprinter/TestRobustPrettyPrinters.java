package com.greenbird.prettyprinter;

import java.io.File;
import java.io.FileFilter;

import org.junit.Test;

import com.greenbird.xml.prettyprinter.PrettyPrinter;
import com.greenbird.xml.prettyprinter.plain.PlainPrettyPrinterForTextNodesWithXML;
import com.greenbird.xml.prettyprinter.plain.PlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength;
import com.greenbird.xml.prettyprinter.plain.RobustPlainPrettyPrinter;
import com.greenbird.xml.prettyprinter.plain.filter.MultiFilterPlainPrettyPrinterForTextNodesWithXML;
import com.greenbird.xml.prettyprinter.plain.filter.MultiFilterPlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength;
import com.greenbird.xml.prettyprinter.plain.filter.MultiFilterRobustPlainPrettyPrinter;
import com.greenbird.xml.prettyprinter.plain.ws.RobustPlainIndentedPrettyPrinter;


public class TestRobustPrettyPrinters extends AbstractNodeTest {
 
	private static final String robustInput = "robust/";
	private static final String robustOutput = "robust/indented/";
	
	private static final String robustAlmostXMLOutput = "robust/almostXML/indented/";
	private static final String robustAlmostXML= "robust/almostXML/";
	
	private static final FileFilter textFilter = new FileFilter() {
		
		@Override
		public boolean accept(File pathname) {
			return pathname.getName().endsWith(".xml") && pathname.getName().contains("Text");
		}
	};

	@Test
	public void testRobustAll() throws Exception {
		
		RobustPlainPrettyPrinter prettyPrinter = new RobustPlainPrettyPrinter(true, true, true, true, true, true, false, '\t', 1);
		
		process(robustInput, robustOutput, prettyPrinter, false, 1, false);
	}

	@Test
	public void testRobustText() throws Exception {
		
		RobustPlainPrettyPrinter prettyPrinter = new RobustPlainPrettyPrinter(true, false, true, true, true, true, false, '\t', 1);
		
		process(robustInput, prettyPrinter, false, textFilter);
	}

	@Test
	public void testRobustWhitespaceAll() throws Exception {
		
		RobustPlainIndentedPrettyPrinter prettyPrinter = new RobustPlainIndentedPrettyPrinter(true, true, true, true, true, true, false, '\t', 1);
		
		process(robustInput, robustOutput, prettyPrinter, false, 1, false);
	}

	@Test
	public void testRobustWhitespaceText() throws Exception {
		
		RobustPlainIndentedPrettyPrinter prettyPrinter = new RobustPlainIndentedPrettyPrinter(true, false, true, true, true, true, false, '\t', 1);
		
		process(robustInput, prettyPrinter, false, textFilter);
	}

	@Test
	public void testRobustFilterAll() throws Exception {
		
		MultiFilterRobustPlainPrettyPrinter prettyPrinter = new MultiFilterRobustPlainPrettyPrinter(true, true, true, true, true, true, false, new String[]{"/a/b"}, null, '\t', 1);
		
		process(robustInput, robustOutput, prettyPrinter, false, 1, false);
	}

	@Test
	public void testRobustFilterText() throws Exception {
		
		MultiFilterRobustPlainPrettyPrinter prettyPrinter = new MultiFilterRobustPlainPrettyPrinter(true, false, true, true, true, true, false, new String[]{"/a/b"}, null, '\t', 1);
		
		process(robustInput, prettyPrinter, false, textFilter);
	}

	@Test
	public void testRobustText1Accept() throws Exception {
		
		PlainPrettyPrinterForTextNodesWithXML prettyPrinter = new PlainPrettyPrinterForTextNodesWithXML(false, false, true, false, '\t', 1);
		
		process(robustInput, robustOutput, prettyPrinter, false, 1, false, textFilter);
	}

	@Test
	public void testRobustText1Disregard() throws Exception {
		
		PlainPrettyPrinterForTextNodesWithXML prettyPrinter = new PlainPrettyPrinterForTextNodesWithXML(false, false, false, false, '\t', 1);
		
		process(robustInput, prettyPrinter, false, textFilter);
	}

	@Test
	public void testRobustText2Accept() throws Exception {
		
		PlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength prettyPrinter = new PlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength(false, false, true, false, 1024, 1024, '\t', 1);
		
		process(robustInput, robustOutput, prettyPrinter, false, 1, false, textFilter);
	}
	
	@Test
	public void testRobustText2Disregard() throws Exception {
		
		PlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength prettyPrinter = new PlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength(false, false, false, false, 1024, 1024, '\t', 1);
		
		process(robustInput, prettyPrinter, false, textFilter);
	}
	
	@Test
	public void testRobustText3Accept() throws Exception {
		
		MultiFilterPlainPrettyPrinterForTextNodesWithXML prettyPrinter = new MultiFilterPlainPrettyPrinterForTextNodesWithXML(false, false, true, false, new String[]{"/a/b"}, new String[]{"/c/d"}, '\t', 1);
		
		process(robustInput, robustOutput, prettyPrinter, false, 1, false, textFilter);
	}

	@Test
	public void testRobustText3Disregard() throws Exception {
		
		MultiFilterPlainPrettyPrinterForTextNodesWithXML prettyPrinter = new MultiFilterPlainPrettyPrinterForTextNodesWithXML(false, false, false, false, new String[]{"/a/b"}, new String[]{"/c/d"}, '\t', 1);
		
		process(robustInput, prettyPrinter, false, textFilter);

	}

	@Test
	public void testRobustText4Accept() throws Exception {
		
		MultiFilterPlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength prettyPrinter = new MultiFilterPlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength(false, false, true, false, 1024, 1024, new String[]{"/a/b"}, new String[]{"/c/d"}, '\t', 1);
		
		process(robustInput, robustOutput, prettyPrinter, false, 1, false, textFilter);
	}

	@Test
	public void testRobustText4Disregard() throws Exception {
		
		MultiFilterPlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength prettyPrinter = new MultiFilterPlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength(false, false, false, false, 1024, 1024, new String[]{"/a/b"}, new String[]{"/c/d"}, '\t', 1);
		
		process(robustInput, prettyPrinter, false, textFilter);

	}

	@Test
	public void testAlmostXML() throws Exception {
		// check that various border cases are not detected as text node XML and thus do not fail
		PrettyPrinter prettyPrinter = new RobustPlainPrettyPrinter(true, false, true, true, true, true, false, '\t', 1);
		
		process(robustAlmostXML, robustAlmostXMLOutput, prettyPrinter, false, 1, false);
	}


}
