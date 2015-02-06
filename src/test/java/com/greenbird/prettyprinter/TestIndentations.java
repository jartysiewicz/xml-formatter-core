package com.greenbird.prettyprinter;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.junit.Assert;
import org.junit.Test;

import com.greenbird.prettyprinter.utils.XMLUtils;
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
 * Test declarations here so that we need not double up all the resource file originals.
 * 
 * @author thomas
 *
 */

public class TestIndentations extends AbstractNodeTest {
	
	private String deepXML;
	
	@Test 
	public void testSpaceIndentation() throws Exception {
		String xml = "<xml><a/></xml>";
		
		for(int i = 1; i < 10; i++) {
			PlainPrettyPrinter prettyPrinter = new PlainPrettyPrinter(true, ' ', i);
	
			StringBuilder builder = new StringBuilder();
			Assert.assertTrue(prettyPrinter.process(new StringReader(xml), xml.length(), builder));
			
			Assert.assertTrue(XMLUtils.isIndented(builder.toString(), i));
		}
	}
	
	public TestIndentations() throws XMLStreamException {
		XMLOutputFactory factory = XMLOutputFactory.newFactory();
		
		StringWriter stream = new StringWriter();
		
		XMLStreamWriter writer = factory.createXMLStreamWriter(stream);

		int count = AbstractPrettyPrinter.defaultIndentationDepth + 1;
		for(int i = 0; i < count; i++) {
			writer.writeStartElement("xml");
		}
		writer.writeCharacters("content");
		for(int i = 0; i < count; i++) {
			writer.writeEndElement();
		}
		writer.close();
		
		deepXML = stream.toString();
	}
	
	@Test
	public void testPlainPrettyPrinter() throws Exception {
		process(new PlainPrettyPrinter(true));
	}

	@Test
	public void testPlainPrettyPrinterForCDataAndComments() throws Exception {
		process(new PlainPrettyPrinterForCDataAndComments(true, true, true, '\t', 1));
	}

	@Test
	public void testPlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength() throws Exception {
		process(new PlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength(true, true, true, 1024, 1024, '\t', 1));
	}
	
	@Test
	public void testPlainPrettyPrinterForTextNodesWithXML() throws Exception {
		process(new PlainPrettyPrinterForTextNodesWithXML(true, true, true, true, '\t', 1));
	}
	
	@Test
	public void testPlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength() throws Exception {
		process(new PlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength(true, true, true, true, 1024, 1024, '\t', 1));
	}
	
	@Test
	public void testPlainPrettyPrinterWithMaxNodeLength() throws Exception {
		process(new PlainPrettyPrinterWithMaxNodeLength(true, 1024, 1024, '\t', 1));
	}
	
	@Test
	public void testRobustPlainPrettyPrinter() throws Exception {
		process(new RobustPlainPrettyPrinter(true, true, true, true, true, true, true, 1024, 1024, '\t', 1));
	}
	
	@Test
	public void testMultiFilterPlainPrettyPrinter() throws Exception {
		process(new MultiFilterPlainPrettyPrinter(true, new String[]{"/a/b"}, new String[]{"/a/b"}, '\t', 1));
	}

	@Test
	public void testMultiFilterPlainPrettyPrinterForCDataAndComments() throws Exception {
		process(new MultiFilterPlainPrettyPrinterForCDataAndComments(true, true, true, new String[]{"/a/b"}, new String[]{"/a/b"}, '\t', 1));
	}

	@Test
	public void testMultiFilterPlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength() throws Exception {
		process(new MultiFilterPlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength(true, true, true, 1024, 1024, new String[]{"/a/b"}, new String[]{"/a/b"}, '\t', 1));
	}

	@Test
	public void testMultiFilterPlainPrettyPrinterForTextNodesWithXML() throws Exception {
		process(new MultiFilterPlainPrettyPrinterForTextNodesWithXML(true, true, true, true, new String[]{"/a/b"}, new String[]{"/a/b"}, '\t', 1));
	}

	@Test
	public void testMultiFilterPlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength() throws Exception {
		process(new MultiFilterPlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength(true, true, true, true, 1024, 1024, new String[]{"/a/b"}, new String[]{"/a/b"}, '\t', 1));
	}

	@Test
	public void testMultiFilterPlainPrettyPrinterWithMaxNodeLength() throws Exception {
		process(new MultiFilterPlainPrettyPrinterWithMaxNodeLength(true, 1024, 1024, new String[]{"/a/b"}, new String[]{"/a/b"}, '\t', 1));
	}

	@Test
	public void testMultiFilterRobustPlainPrettyPrinter() throws Exception {
		process(new MultiFilterRobustPlainPrettyPrinter(true, true, true, true, true, true, true, 1024, 1024, new String[]{"/a/b"}, new String[]{"/a/b"}, '\t', 1));
	}

	@Test
	public void testSingleFilterPlainPrettyPrinter() throws Exception {
		process(new SingleFilterPlainPrettyPrinter(true, "/a/b", FilterType.PRUNE, '\t', 1));
	}
	
	@Test
	public void testSingleFilterPlainPrettyPrinterWithMaxNodeLength() throws Exception {
		process(new SingleFilterPlainPrettyPrinterWithMaxNodeLength(true, "/a/b", FilterType.PRUNE, 1024, 1024, '\t', 1));
	}
	
	@Test
	public void testPlainIndentedPrettyPrinter() throws Exception {
		process(new PlainIndentedPrettyPrinter(true, '\t', 1));
	}

	@Test
	public void testRobustPlainIndentedPrettyPrinter() throws Exception {
		process(new RobustPlainIndentedPrettyPrinter(true, true, true, true, true, true, true, '\t', 1));
	}

	private void process(PrettyPrinter prettyPrinter) throws IOException {
		StringBuilder writer = new StringBuilder();
		
		Assert.assertTrue(prettyPrinter.process(new StringReader(deepXML), deepXML.length(), writer));
	}	
	
}
