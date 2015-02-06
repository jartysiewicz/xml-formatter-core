package com.greenbird.prettyprinter;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import com.greenbird.xml.prettyprinter.PrettyPrinter;
import com.greenbird.xml.prettyprinter.PrettyPrinterBuilder;
import com.greenbird.xml.prettyprinter.PrettyPrinterFactory;
import com.greenbird.xml.prettyprinter.plain.PlainPrettyPrinter;
import com.greenbird.xml.prettyprinter.plain.PlainPrettyPrinterForCDataAndComments;
import com.greenbird.xml.prettyprinter.plain.PlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength;
import com.greenbird.xml.prettyprinter.plain.PlainPrettyPrinterForTextNodesWithXML;
import com.greenbird.xml.prettyprinter.plain.PlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength;
import com.greenbird.xml.prettyprinter.plain.PlainPrettyPrinterWithMaxNodeLength;
import com.greenbird.xml.prettyprinter.plain.RobustPlainPrettyPrinter;
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

public class TestPrettyPrinterFactory extends AbstractNodeTest {
 
	@Test
	public void testInstanceReuse() throws Exception {
		PrettyPrinterFactory factory = PrettyPrinterFactory.newInstance();
		
		PrettyPrinter first = factory.newPrettyPrinter();
		Assert.assertTrue(first == factory.newPrettyPrinter());

		factory.setIgnoreWhitespace(true);
		Assert.assertFalse(first == factory.newPrettyPrinter());
	}
	
	@Test
	public void testPlainPrettyPrinter() throws Exception {
		PrettyPrinterFactory factory = PrettyPrinterFactory.newInstance();
		
		PrettyPrinter newPrettyPrinter = factory.newPrettyPrinter();
		
		assertEquals(PlainPrettyPrinter.class, newPrettyPrinter.getClass());
	}

	@Test
	public void testPlainPrettyPrinterForCDataAndComments() {
		PrettyPrinterFactory factory = PrettyPrinterFactory.newInstance();
		
		factory.setPrettyPrintCData(true);
		assertEquals(PlainPrettyPrinterForCDataAndComments.class, factory.newPrettyPrinter().getClass());
		factory.setPrettyPrintCData(false);

		factory.setPrettyPrintComments(true);
		assertEquals(PlainPrettyPrinterForCDataAndComments.class, factory.newPrettyPrinter().getClass());
	}

	@Test
	public void testPlainPrettyPrinterForCDataAndCommentsWithMaxLength() {
		PrettyPrinterFactory factory = PrettyPrinterFactory.newInstance();
		factory.setPrettyPrintCData(true);
		
		factory.setMaxCDATANodeLength(1024);
		assertEquals(PlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength.class, factory.newPrettyPrinter().getClass());
		
		factory.setMaxCDATANodeLength(-1);
		
		factory.setMaxTextNodeLength(1024);
		assertEquals(PlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength.class, factory.newPrettyPrinter().getClass());
	}

	@Test
	public void testPlainPrettyPrinterForTextNodesWithXML1() {
		PrettyPrinterFactory factory = PrettyPrinterFactory.newInstance();
		factory.setPrettyPrintTextNodes(true);
		
		PrettyPrinter newPrettyPrinter = factory.newPrettyPrinter();
		
		assertEquals(PlainPrettyPrinterForTextNodesWithXML.class, newPrettyPrinter.getClass());

	}

	@Test
	public void testPlainPrettyPrinterForTextNodesWithXML2() {
		PrettyPrinterFactory factory = PrettyPrinterFactory.newInstance();
		factory.setPrettyPrintTextNodes(true);
		factory.setRobustness(true);
		PrettyPrinter newPrettyPrinter = factory.newPrettyPrinter();
		
		assertEquals(PlainPrettyPrinterForTextNodesWithXML.class, newPrettyPrinter.getClass());

	}

	@Test
	public void testPlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength1() {
		PrettyPrinterFactory factory = PrettyPrinterFactory.newInstance();
		factory.setPrettyPrintTextNodes(true);
		factory.setMaxCDATANodeLength(1024);
		
		assertEquals(PlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength.class, factory.newPrettyPrinter().getClass());
		factory.setMaxCDATANodeLength(-1);
		
		factory.setMaxTextNodeLength(1024);
		assertEquals(PlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength.class, factory.newPrettyPrinter().getClass());

	}
	
	@Test
	public void testPlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength2() {
		PrettyPrinterFactory factory = PrettyPrinterFactory.newInstance();
		factory.setPrettyPrintTextNodes(true);
		factory.setRobustness(true);
		factory.setMaxCDATANodeLength(1024);
		
		assertEquals(PlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength.class, factory.newPrettyPrinter().getClass());
		
		factory.setMaxCDATANodeLength(-1);
		factory.setMaxTextNodeLength(1024);
		
		assertEquals(PlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength.class, factory.newPrettyPrinter().getClass());
	}
	
	
	@Test
	public void testPlainPrettyPrinterWithMaxNodeLength() throws Exception {
		PrettyPrinterFactory factory = PrettyPrinterFactory.newInstance();
		factory.setMaxCDATANodeLength(1024);
		
		assertEquals(PlainPrettyPrinterWithMaxNodeLength.class, factory.newPrettyPrinter().getClass());
		factory.setMaxCDATANodeLength(-1);
		
		factory.setMaxTextNodeLength(1024);
		assertEquals(PlainPrettyPrinterWithMaxNodeLength.class, factory.newPrettyPrinter().getClass());
	}

	@Test
	public void testRobustPlainPrettyPrinter() {
		PrettyPrinterFactory factory = PrettyPrinterFactory.newInstance();
		factory.setPrettyPrintComments(true);
		factory.setRobustness(true);
		
		assertEquals(RobustPlainPrettyPrinter.class, factory.newPrettyPrinter().getClass());
		factory.setPrettyPrintComments(false);
		
		factory.setPrettyPrintCData(true);
		assertEquals(RobustPlainPrettyPrinter.class, factory.newPrettyPrinter().getClass());
	}

	@Test
	public void testRobustPlainPrettyPrinterAndMaxLength() {
		PrettyPrinterFactory factory = PrettyPrinterFactory.newInstance();
		factory.setPrettyPrintComments(true);
		factory.setMaxCDATANodeLength(1024);
		factory.setRobustness(true);
		
		assertEquals(RobustPlainPrettyPrinter.class, factory.newPrettyPrinter().getClass());
		factory.setMaxCDATANodeLength(-1);
		
		factory.setMaxTextNodeLength(1024);
		assertEquals(RobustPlainPrettyPrinter.class, factory.newPrettyPrinter().getClass());
	}
	

	@Test
	public void testSingleFilterPlainPrettyPrinter1() throws Exception {
		PrettyPrinterFactory factory = PrettyPrinterFactory.newInstance();
		factory.setAnonymizeFilters(new String[]{"/a/b"});
		
		assertEquals(SingleFilterPlainPrettyPrinter.class, factory.newPrettyPrinter().getClass());

		factory.setPruneFilters(new String[]{});
		assertEquals(SingleFilterPlainPrettyPrinter.class, factory.newPrettyPrinter().getClass());

	}

	@Test
	public void testSingleFilterPlainPrettyPrinter2() throws Exception {
		PrettyPrinterFactory factory = PrettyPrinterFactory.newInstance();
		factory.setPruneFilters(new String[]{"/a/b"});
		
		assertEquals(SingleFilterPlainPrettyPrinter.class, factory.newPrettyPrinter().getClass());
		
		factory.setAnonymizeFilters(new String[]{});
		assertEquals(SingleFilterPlainPrettyPrinter.class, factory.newPrettyPrinter().getClass());
	}

	@Test
	public void testMultiFilterPlainPrettyPrinter() throws Exception {
		PrettyPrinterFactory factory = PrettyPrinterFactory.newInstance();
		factory.setAnonymizeFilters(new String[]{"/a/b", "/c/d"});
		
		PrettyPrinter newPrettyPrinter = factory.newPrettyPrinter();
		
		assertEquals(MultiFilterPlainPrettyPrinter.class, newPrettyPrinter.getClass());
	}

	@Test
	public void testMultiFilterPlainPrettyPrinterForCDataAndComments() {
		PrettyPrinterFactory factory = PrettyPrinterFactory.newInstance();
		factory.setPrettyPrintCData(true);
		factory.setAnonymizeFilters(new String[]{"/a/b"});
		
		assertEquals(MultiFilterPlainPrettyPrinterForCDataAndComments.class, factory.newPrettyPrinter().getClass());
		factory.setPrettyPrintCData(false);
		
		factory.setPrettyPrintComments(true);
		assertEquals(MultiFilterPlainPrettyPrinterForCDataAndComments.class, factory.newPrettyPrinter().getClass());
	}

	@Test
	public void testMultiFilterPlainPrettyPrinterForCDataAndCommentsWithMaxLength() {
		PrettyPrinterFactory factory = PrettyPrinterFactory.newInstance();
		factory.setMaxCDATANodeLength(1024);
		factory.setPrettyPrintCData(true);
		factory.setAnonymizeFilters(new String[]{"/a/b"});
		
		assertEquals(MultiFilterPlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength.class, factory.newPrettyPrinter().getClass());
		factory.setMaxCDATANodeLength(-1);
		
		factory.setMaxTextNodeLength(1024);
		assertEquals(MultiFilterPlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength.class, factory.newPrettyPrinter().getClass());
	}

	@Test
	public void testMultiFilterPlainPrettyPrinterForTextNodesWithXML1() {
		PrettyPrinterFactory factory = PrettyPrinterFactory.newInstance();
		factory.setPrettyPrintTextNodes(true);
		factory.setAnonymizeFilters(new String[]{"/a/b"});
		
		PrettyPrinter newPrettyPrinter = factory.newPrettyPrinter();
		
		assertEquals(MultiFilterPlainPrettyPrinterForTextNodesWithXML.class, newPrettyPrinter.getClass());

	}
	
	@Test
	public void testMultiFilterPlainPrettyPrinterForTextNodesWithXML2() {
		PrettyPrinterFactory factory = PrettyPrinterFactory.newInstance();
		factory.setPrettyPrintTextNodes(true);
		factory.setRobustness(true);
		factory.setAnonymizeFilters(new String[]{"/a/b"});
		
		PrettyPrinter newPrettyPrinter = factory.newPrettyPrinter();
		
		assertEquals(MultiFilterPlainPrettyPrinterForTextNodesWithXML.class, newPrettyPrinter.getClass());
	}


	@Test
	public void testMultiFilterPlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength1() {
		PrettyPrinterFactory factory = PrettyPrinterFactory.newInstance();
		factory.setPrettyPrintTextNodes(true);
		factory.setMaxCDATANodeLength(1024);
		factory.setAnonymizeFilters(new String[]{"/a/b"});
		
		assertEquals(MultiFilterPlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength.class, factory.newPrettyPrinter().getClass());
		factory.setMaxCDATANodeLength(-1);
		
		factory.setMaxTextNodeLength(1024);
		assertEquals(MultiFilterPlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength.class, factory.newPrettyPrinter().getClass());
	}

	@Test
	public void testMultiFilterPlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength2() {
		PrettyPrinterFactory factory = PrettyPrinterFactory.newInstance();
		factory.setPrettyPrintTextNodes(true);
		factory.setRobustness(true);
		factory.setMaxCDATANodeLength(1024);
		factory.setAnonymizeFilters(new String[]{"/a/b"});
		
		assertEquals(MultiFilterPlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength.class, factory.newPrettyPrinter().getClass());
		factory.setMaxCDATANodeLength(-1);
		
		factory.setMaxTextNodeLength(1024);
		assertEquals(MultiFilterPlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength.class, factory.newPrettyPrinter().getClass());
	}

	@Test
	public void testSingleFilterPlainPrettyPrinterWithMaxNodeLengthAnon() throws Exception {
		PrettyPrinterFactory factory = PrettyPrinterFactory.newInstance();
		factory.setMaxCDATANodeLength(1024);
		factory.setAnonymizeFilters(new String[]{"/a/b"});
		
		assertEquals(SingleFilterPlainPrettyPrinterWithMaxNodeLength.class, factory.newPrettyPrinter().getClass());
		
		factory.setPruneFilters(new String[]{});
		assertEquals(SingleFilterPlainPrettyPrinterWithMaxNodeLength.class, factory.newPrettyPrinter().getClass());
		
		factory.setMaxCDATANodeLength(-1);
		
		factory.setMaxTextNodeLength(1024);
		assertEquals(SingleFilterPlainPrettyPrinterWithMaxNodeLength.class, factory.newPrettyPrinter().getClass());
	}

	@Test
	public void testSingleFilterPlainPrettyPrinterWithMaxNodeLengthPrune() throws Exception {
		PrettyPrinterFactory factory = PrettyPrinterFactory.newInstance();
		factory.setMaxCDATANodeLength(1024);
		factory.setPruneFilters(new String[]{"/a/b"});
		
		assertEquals(SingleFilterPlainPrettyPrinterWithMaxNodeLength.class, factory.newPrettyPrinter().getClass());
		
		factory.setAnonymizeFilters(new String[]{});
		assertEquals(SingleFilterPlainPrettyPrinterWithMaxNodeLength.class, factory.newPrettyPrinter().getClass());

		factory.setMaxCDATANodeLength(-1);
		
		factory.setMaxTextNodeLength(1024);
		assertEquals(SingleFilterPlainPrettyPrinterWithMaxNodeLength.class, factory.newPrettyPrinter().getClass());
	}

	@Test
	public void testMultiFilterPlainPrettyPrinterWithMaxNodeLength() throws Exception {
		PrettyPrinterFactory factory = PrettyPrinterFactory.newInstance();
		factory.setMaxCDATANodeLength(1024);
		factory.setAnonymizeFilters(new String[]{"/a/b", "/c/d"});
		
		assertEquals(MultiFilterPlainPrettyPrinterWithMaxNodeLength.class, factory.newPrettyPrinter().getClass());
		factory.setMaxCDATANodeLength(-1);
		
		factory.setMaxTextNodeLength(1024);
		assertEquals(MultiFilterPlainPrettyPrinterWithMaxNodeLength.class, factory.newPrettyPrinter().getClass());
	}

	@Test
	public void testMultiFilterRobustPlainPrettyPrinter() {
		PrettyPrinterFactory factory = PrettyPrinterFactory.newInstance();
		factory.setPrettyPrintCData(true);
		factory.setRobustness(true);
		factory.setAnonymizeFilters(new String[]{"/a/b"});
		
		assertEquals(MultiFilterRobustPlainPrettyPrinter.class, factory.newPrettyPrinter().getClass());
		factory.setPrettyPrintCData(false);
		
		factory.setPrettyPrintComments(true);
		assertEquals(MultiFilterRobustPlainPrettyPrinter.class, factory.newPrettyPrinter().getClass());
	}

	@Test
	public void testMultiFilterRobustPlainPrettyPrinterWithMaxLength() {
		PrettyPrinterFactory factory = PrettyPrinterFactory.newInstance();
		factory.setPrettyPrintCData(true);
		factory.setMaxCDATANodeLength(1024);
		factory.setRobustness(true);
		factory.setAnonymizeFilters(new String[]{"/a/b"});
		
		assertEquals(MultiFilterRobustPlainPrettyPrinter.class, factory.newPrettyPrinter().getClass());
		factory.setMaxCDATANodeLength(-1);
		
		factory.setMaxTextNodeLength(1024);
		assertEquals(MultiFilterRobustPlainPrettyPrinter.class, factory.newPrettyPrinter().getClass());
	}

	@Test
	public void testPlainPrettyPrinterWS() throws Exception {
		PrettyPrinterFactory factory = PrettyPrinterFactory.newInstance();
		factory.setIgnoreWhitespace(true);
		
		PrettyPrinter newPrettyPrinter = factory.newPrettyPrinter();
		
		assertEquals(PlainIndentedPrettyPrinter.class, newPrettyPrinter.getClass());
	}

	@Test
	public void testRobustPlainPrettyPrinterWS() {
		PrettyPrinterFactory factory = PrettyPrinterFactory.newInstance();
		factory.setPrettyPrintCData(true);
		factory.setRobustness(true);
		factory.setIgnoreWhitespace(true);
		
		assertEquals(RobustPlainIndentedPrettyPrinter.class, factory.newPrettyPrinter().getClass());
		factory.setPrettyPrintCData(false);
		
		factory.setPrettyPrintComments(true);
		assertEquals(RobustPlainIndentedPrettyPrinter.class, factory.newPrettyPrinter().getClass());
		factory.setPrettyPrintComments(false);
		
		factory.setPrettyPrintTextNodes(true);
		assertEquals(RobustPlainIndentedPrettyPrinter.class, factory.newPrettyPrinter().getClass());
	}
	
	@Test
	public void testEmptyPruneFilters() throws Exception {
		PrettyPrinterFactory factory = PrettyPrinterFactory.newInstance();
		
		factory.setPruneFilters(new String[]{});
		PrettyPrinter newPrettyPrinter = factory.newPrettyPrinter();
		
		assertEquals(PlainPrettyPrinter.class, newPrettyPrinter.getClass());
		
		factory.setPruneFilters(null);
		Assert.assertNull(factory.getPruneFilters());

		factory.setAnonymizeFilterList(null);
		Assert.assertNull(factory.getPruneFilters());

	}

	@Test
	public void testEmptyAnonFilters() throws Exception {
		PrettyPrinterFactory factory = PrettyPrinterFactory.newInstance();
		
		factory.setAnonymizeFilters(new String[]{});
		PrettyPrinter newPrettyPrinter = factory.newPrettyPrinter();
		
		assertEquals(PlainPrettyPrinter.class, newPrettyPrinter.getClass());

		factory.setAnonymizeFilters(null);
		Assert.assertNull(factory.getAnonymizeFilters());

		factory.setAnonymizeFilterList(null);
		Assert.assertNull(factory.getAnonymizeFilters());
	}

	@Test
	public void testInvalidXPath1() {
		try {
			PrettyPrinterBuilder.newPrettyPrinter().anonymize(new String[]{"/abcd[a='b']/*"}).build();

			Assert.fail();
		} catch(Exception e) {
			// pass
		}
	}

	@Test
	public void testInvalidXPath2() {
		try {
			PrettyPrinterBuilder.newPrettyPrinter().anonymize(new String[]{"/@abcd"}).build();

			Assert.fail();
		} catch(Exception e) {
			// pass
		}
	}

	@Test
	public void testInvalidXPath3() {
		try {
			PrettyPrinterBuilder.newPrettyPrinter().anonymize(new String[]{"/a|/b"}).build();

			Assert.fail();
		} catch(Exception e) {
			// pass
		}
	}

	@Test
	public void testXPath() {
		PrettyPrinterBuilder.newPrettyPrinter().anonymize(new String[]{"/abcd"}).build();
		PrettyPrinterBuilder.newPrettyPrinter().anonymize(new String[]{"/abcd/abcdef"}).build();
		PrettyPrinterBuilder.newPrettyPrinter().anonymize(new String[]{"/abcd/@e"}).build();
	}

	@Test
	public void testUnsupported1() {
		try {
			PrettyPrinterBuilder.newPrettyPrinter().ignoreWhitespace().maxNodeLength(1024).build();

			Assert.fail();
		} catch(Exception e) {
			// pass
		}
	}

	@Test
	public void testInvalidConfiguration2() {
		try {
			PrettyPrinterBuilder.newPrettyPrinter().robust().build();

			Assert.fail();
		} catch(Exception e) {
			// pass
		}
	}

	@Test
	public void testUnsupported2() {
		try {
			PrettyPrinterBuilder.newPrettyPrinter().robust().anonymize(new String[]{"/a"}).build();

			Assert.fail();
		} catch(Exception e) {
			// pass
		}
	}

	@Test
	public void testUnsupported3() {
		try {
			PrettyPrinterBuilder.newPrettyPrinter().ignoreWhitespace().anonymize(new String[]{"/a"}).build();

			Assert.fail();
		} catch(Exception e) {
			// pass
		}
	}

}
