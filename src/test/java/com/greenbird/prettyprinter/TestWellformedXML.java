package com.greenbird.prettyprinter;

import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.greenbird.prettyprinter.utils.PrettyPrinterConstructor;
import com.greenbird.prettyprinter.utils.PrettyPrinterFactoryItem;
import com.greenbird.xml.prettyprinter.PrettyPrinter;
import com.greenbird.xml.prettyprinter.PrettyPrinterFactory;
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
 * Test pretty-printers against various well-formed XML. <br/><br/>
 * 
 * Due to the many, many different test files, implementations and corresponding configurations, this
 * test class relies on utility classes which automatically constructs pretty-printers from a set of parameters.<br/><br/> 
 * 
 * The resulting pretty-printers are then matched against a compiled set of input-output directory pairs - each with specific 
 * parameters. Upon match, pretty-printers must perfectly recreate output files from input files. Upon mismatch, pretty-printers
 * must produce well-formed output from input.<br/>
 * 
 * @author thomas
 *
 */

public class TestWellformedXML extends AbstractNodeTest {

	protected static Logger logger = Logger.getLogger(TestWellformedXML.class.getName());

	private static final String basicInput = "all/basic/";
	private static final String basicOutput = "all/basic/indented/";

	private static final String cdataTextInput = "cdata/text/";
	private static final String cdataTextOutput = "cdata/text/indented/";
	private static final String cdataTextOutputMaxLength = "cdata/text/indentedMaxLength/";
	
	private static final String cdataTextFilterAnonOutput = "cdata/text/anon/";
	private static final String cdataTextFilterAnonOutputMaxLength = "cdata/text/anonMaxLength/";
	private static final String cdataTextFilterPruneOutput = "cdata/text/prune/";
	private static final String cdataTextFilterPruneOutputMaxLength = "cdata/text/pruneMaxLength/";

	private static final String cdataXMLInput = "cdata/xml/";
	private static final String cdataXMLIndentedOutput = "cdata/xml/indentedCData/";
	private static final String cdataXMLOutput = "cdata/xml/indented/";

	private static final String commentsTextInput = "comments/text/";
	private static final String commentsTextOutput = "comments/text/indented/";

	private static final String commentsXMLInput = "comments/xml/";
	private static final String commentsXMLIndentedOutput = "comments/xml/indentedComment/";
	private static final String commentsXMLOutput = "comments/xml/indented/";
	
	private static final String elementInput = "elements/noNamespace";
	private static final String elementOutput = "elements/noNamespace/indented/";

	private static final String elementNamespaceInput = "elements/noNamespace";
	private static final String elementNamespaceOutput = "elements/noNamespace/indented/";

	private static final String processingInstructionsInput = "processingInstructions/";
	private static final String processingInstructionsOutput = "processingInstructions/indented/";

	private static final String realSoapInput = "all/real/soap";
	private static final String realSoapOutput = "all/real/soap/indented/";

	private static final String textInput = "text/text/";
	private static final String textOutput = "text/text/indented/";

	private static final String textInputStartEndNode = "text/textBetweenStartEndNode/";
	private static final String textOutputStartEndNode = "text/textBetweenStartEndNode/indented/";
	private static final String textOutputMaxLength = "text/textBetweenStartEndNode/indentedMaxLength/";

	private static final String textXMLInput1 = "text/xml/escapedExceptGreaterThan/";
	private static final String textXMLOutput1 = "text/xml/escapedExceptGreaterThan/indented/";

	private static final String textXMLInput2 = "text/xml/escapedNormal/";
	private static final String textXMLOutput2 = "text/xml/escapedNormal/indented/";

	private static final String textXMLInput3 = "text/xmlBetweenStartEndNode/escapedExceptGreaterThan/";
	private static final String textXMLOutput3 = "text/xmlBetweenStartEndNode/escapedExceptGreaterThan/indented/";

	private static final String textXMLInput4 = "text/xmlBetweenStartEndNode/escapedNormal/";
	private static final String textXMLOutput4 = "text/xmlBetweenStartEndNode/escapedNormal/indented/";
	private static final String textXMLOutput4Prune = "text/xmlBetweenStartEndNode/escapedNormal/prune/";
	private static final String textXMLOutput4Anon = "text/xmlBetweenStartEndNode/escapedNormal/anon/";
	
	private static final String textFilterInput = "text/textBetweenStartEndNode/";
	private static final String textFilterAnonOutput = "text/textBetweenStartEndNode/anon/";
	private static final String textFilterPruneOutput = "text/textBetweenStartEndNode/prune/";
	private static final String textFilterAnonAnyOutput = "text/textBetweenStartEndNode/anonAny/"; // for //achild
	private static final String textFilterPruneAnyOutput = "text/textBetweenStartEndNode/pruneAny/"; // for //achild
	private static final String textFilterAnonWildcardOutput = "text/textBetweenStartEndNode/anonWildcard/"; // for /*/*
	private static final String textFilterPruneWildcardOutput = "text/textBetweenStartEndNode/pruneWildcard/"; // for /*/*

	private static final String textFilterAnonOutputMaxLength = "text/textBetweenStartEndNode/anonMaxLength/";
	private static final String textFilterPruneOutputMaxLength = "text/textBetweenStartEndNode/pruneMaxLength/";

	private static final String declarationInput = "declaration/";
	private static final String declarationOutputKeepDeclaration = "declaration/keep/";
	private static final String declarationOutputRemoveDeclaration = "declaration/remove/";

	private static final String attributeInput = "attributes/";
	private static final String attributeOutput = "attributes/indented/";
	private static final String attributeFilterAnonOutput = "attributes/anon/";
	private static final String attributeFilterAnonWildcardOutput = "attributes/anonWildcard/";

	private static final String dtdInput = "dtd/";
	private static final String dtdOutput = "dtd/indented/";

	static final PrettyPrinterFactory defaultFactory = new PrettyPrinterFactory();
	
	private static List<PrettyPrinterFactoryItem> factoryItems = new ArrayList<PrettyPrinterFactoryItem>();
			
	static {
		PrettyPrinterFactory factory = new PrettyPrinterFactory();
		
		factory.setXmlDeclaration(false);

		add(factory, basicInput, basicOutput);
		add(factory, cdataTextInput, cdataTextOutput);
		add(factory, cdataXMLInput, cdataXMLOutput);
		add(factory, commentsTextInput, commentsTextOutput);
		add(factory, commentsXMLInput, commentsXMLOutput);
		add(factory, elementInput, elementOutput);
		add(factory, elementNamespaceInput, elementNamespaceOutput);
		add(factory, processingInstructionsInput, processingInstructionsOutput);
		add(factory, realSoapInput, realSoapOutput);
		add(factory, textInput, textOutput);
		add(factory, textInputStartEndNode, textOutputStartEndNode);
		add(factory, declarationInput, declarationOutputRemoveDeclaration);
		add(factory, attributeInput, attributeOutput);
		add(factory, dtdInput, dtdOutput);
		
		factory = new PrettyPrinterFactory();
		factory.setXmlDeclaration(true);
		
		add(factory, declarationInput, declarationOutputKeepDeclaration);

		factory = new PrettyPrinterFactory();
		factory.setXmlDeclaration(false);
		factory.setMaxCDATANodeLength(26);

		add(factory, basicInput, basicOutput);
		add(factory, cdataTextInput, cdataTextOutputMaxLength);
		add(factory, cdataXMLInput, cdataXMLOutput);
		add(factory, commentsTextInput, commentsTextOutput);
		add(factory, commentsXMLInput, commentsXMLOutput);
		add(factory, elementInput, elementOutput);
		add(factory, elementNamespaceInput, elementNamespaceOutput);
		add(factory, processingInstructionsInput, processingInstructionsOutput);
		add(factory, realSoapInput, realSoapOutput);
		add(factory, textInput, textOutput);
		add(factory, textInputStartEndNode, textOutputStartEndNode);
		
		factory = new PrettyPrinterFactory();
		factory.setXmlDeclaration(false);
		factory.setPrettyPrintCData(true);
		
		add(factory, basicInput, basicOutput);
		add(factory, cdataTextInput, cdataTextOutput);
		add(factory, cdataXMLInput, cdataXMLIndentedOutput);
		add(factory, commentsTextInput, commentsTextOutput);
		add(factory, commentsXMLInput, commentsXMLOutput);
		add(factory, elementInput, elementOutput);
		add(factory, elementNamespaceInput, elementNamespaceOutput);
		add(factory, processingInstructionsInput, processingInstructionsOutput);
		add(factory, realSoapInput, realSoapOutput);
		add(factory, textInput, textOutput);
		add(factory, textInputStartEndNode, textOutputStartEndNode);

		factory = new PrettyPrinterFactory();
		factory.setXmlDeclaration(false);
		factory.setPrettyPrintComments(true);
		
		add(factory, basicInput, basicOutput);
		add(factory, cdataTextInput, cdataTextOutput);
		add(factory, cdataXMLInput, cdataXMLOutput);
		add(factory, commentsTextInput, commentsTextOutput);
		add(factory, commentsXMLInput, commentsXMLIndentedOutput);
		add(factory, elementInput, elementOutput);
		add(factory, elementNamespaceInput, elementNamespaceOutput);
		add(factory, processingInstructionsInput, processingInstructionsOutput);
		add(factory, realSoapInput, realSoapOutput);
		add(factory, textInput, textOutput);
		add(factory, textInputStartEndNode, textOutputStartEndNode);
		
		factory = new PrettyPrinterFactory();
		factory.setXmlDeclaration(false);
		factory.setMaxTextNodeLength(26);
		
		add(factory, basicInput, basicOutput);
		add(factory, cdataTextInput, cdataTextOutput);
		add(factory, cdataXMLInput, cdataXMLOutput);
		add(factory, commentsTextInput, commentsTextOutput);
		add(factory, commentsXMLInput, commentsXMLOutput);
		add(factory, elementInput, elementOutput);
		add(factory, elementNamespaceInput, elementNamespaceOutput);
		add(factory, processingInstructionsInput, processingInstructionsOutput);
		add(factory, realSoapInput, realSoapOutput);
		add(factory, textInput, textOutput);
		add(factory, textInputStartEndNode, textOutputMaxLength);

		factory = new PrettyPrinterFactory();
		factory.setXmlDeclaration(false);
		factory.setPrettyPrintTextNodes(true);

		add(factory, basicInput, basicOutput);
		add(factory, cdataTextInput, cdataTextOutput);
		add(factory, cdataXMLInput, cdataXMLOutput);
		add(factory, commentsTextInput, commentsTextOutput);
		add(factory, commentsXMLInput, commentsXMLOutput);
		add(factory, elementInput, elementOutput);
		add(factory, elementNamespaceInput, elementNamespaceOutput);
		add(factory, processingInstructionsInput, processingInstructionsOutput);
		add(factory, realSoapInput, realSoapOutput);
		add(factory, textInput, textOutput);
		add(factory, textInputStartEndNode, textOutputStartEndNode);

		add(factory, textXMLInput1, textXMLOutput1);
		add(factory, textXMLInput2, textXMLOutput2);
		add(factory, textXMLInput3, textXMLOutput3);
		add(factory, textXMLInput4, textXMLOutput4);
		
		factory = new PrettyPrinterFactory();
		factory.setXmlDeclaration(false);
		factory.setIgnoreWhitespace(true);
		// so this does not cover whitespace which is not from pretty-printing (all over the place).

		// not including text outside start-end tags
		add(factory, basicInput, basicOutput);
		add(factory, cdataTextInput, cdataTextOutput);
		add(factory, cdataXMLInput, cdataXMLOutput);
		add(factory, commentsTextInput, commentsTextOutput);
		add(factory, commentsXMLInput, commentsXMLOutput);
		add(factory, elementInput, elementOutput);
		add(factory, elementNamespaceInput, elementNamespaceOutput);
		add(factory, processingInstructionsInput, processingInstructionsOutput);
		add(factory, realSoapInput, realSoapOutput);
		add(factory, textInputStartEndNode, textOutputStartEndNode);

		// output is also input
		add(factory, basicOutput, basicOutput);
		add(factory, cdataTextOutput, cdataTextOutput);
		add(factory, cdataXMLOutput, cdataXMLOutput);
		add(factory, commentsTextOutput, commentsTextOutput);
		add(factory, commentsXMLOutput, commentsXMLOutput);
		add(factory, elementOutput, elementOutput);
		add(factory, elementNamespaceInput, elementNamespaceOutput);
		add(factory, processingInstructionsOutput, processingInstructionsOutput);
		add(factory, realSoapOutput, realSoapOutput);
		add(factory, textOutputStartEndNode, textOutputStartEndNode);		
		
		factory = new PrettyPrinterFactory();
		factory.setXmlDeclaration(false);
		factory.setAnonymizeFilters(new String[]{"/aparent/achild"});
		
		// not including text outside start-end tags
		add(factory, cdataTextInput, cdataTextFilterAnonOutput);
		add(factory, cdataXMLInput, cdataXMLOutput);
		add(factory, commentsTextInput, commentsTextOutput);
		add(factory, commentsXMLInput, commentsXMLOutput);
		add(factory, processingInstructionsInput, processingInstructionsOutput);
		add(factory, textFilterInput, textFilterAnonOutput);

		factory = new PrettyPrinterFactory();
		factory.setXmlDeclaration(false);
		factory.setAnonymizeFilters(new String[]{"/aparent/achild", "/parent/nosuchchild"});
		
		// not including text outside start-end tags
		add(factory, cdataTextInput, cdataTextFilterAnonOutput);
		add(factory, cdataXMLInput, cdataXMLOutput);
		add(factory, commentsTextInput, commentsTextOutput);
		add(factory, commentsXMLInput, commentsXMLOutput);
		add(factory, processingInstructionsInput, processingInstructionsOutput);
		add(factory, textFilterInput, textFilterAnonOutput);

		factory = new PrettyPrinterFactory();
		factory.setXmlDeclaration(false);
		factory.setAnonymizeFilters(new String[]{"/aparent/achild"});
		factory.setMaxTextNodeLength(26);

		// not including text outside start-end tags
		add(factory, textFilterInput, textFilterAnonOutputMaxLength);
		
		factory = new PrettyPrinterFactory();
		factory.setXmlDeclaration(false);
		factory.setAnonymizeFilters(new String[]{"/aparent/achild"});
		factory.setMaxCDATANodeLength(26);
		add(factory, cdataTextInput, cdataTextFilterAnonOutputMaxLength);

		factory = new PrettyPrinterFactory();
		factory.setXmlDeclaration(false);
		factory.setPruneFilters(new String[]{"/aparent/achild", "/parent/nosuchchild"});
		factory.setMaxCDATANodeLength(26);
		add(factory, cdataTextInput, cdataTextFilterPruneOutputMaxLength);

		factory = new PrettyPrinterFactory();
		factory.setXmlDeclaration(false);
		factory.setPruneFilters(new String[]{"/aparent/achild"});
		
		// not including text outside start-end tags
		add(factory, cdataXMLInput, cdataXMLOutput);
		add(factory, cdataTextInput, cdataTextFilterPruneOutput);
		add(factory, commentsTextInput, commentsTextOutput);
		add(factory, commentsXMLInput, commentsXMLOutput);
		add(factory, processingInstructionsInput, processingInstructionsOutput);
		add(factory, textFilterInput, textFilterPruneOutput);

		factory = new PrettyPrinterFactory();
		factory.setXmlDeclaration(false);
		factory.setPruneFilters(new String[]{"/aparent/achild", "/parent/nosuchchild"});
		
		// not including text outside start-end tags
		add(factory, cdataXMLInput, cdataXMLOutput);
		add(factory, cdataTextInput, cdataTextFilterPruneOutput);
		add(factory, commentsTextInput, commentsTextOutput);
		add(factory, commentsXMLInput, commentsXMLOutput);
		add(factory, processingInstructionsInput, processingInstructionsOutput);
		add(factory, textFilterInput, textFilterPruneOutput);

		factory = new PrettyPrinterFactory();
		factory.setXmlDeclaration(false);
		factory.setPruneFilters(new String[]{"/aparent/achild", "/parent/nosuchchild"});
		factory.setMaxTextNodeLength(26);
		
		// not including text outside start-end tags
		add(factory, textFilterInput, textFilterPruneOutputMaxLength);
		
		factory = new PrettyPrinterFactory();
		factory.setXmlDeclaration(false);
		factory.setPrettyPrintTextNodes(true);
		factory.setPruneFilters(new String[]{"/parent/child/x/m", "/parent/nosuchchild"});
		
		add(factory, textXMLInput4, textXMLOutput4Prune);

		factory = new PrettyPrinterFactory();
		factory.setXmlDeclaration(false);
		factory.setPrettyPrintTextNodes(true);
		factory.setAnonymizeFilters(new String[]{"/parent/child/x/m/l", "/parent/nosuchchild"});
		
		add(factory, textXMLInput4, textXMLOutput4Anon);
		
		factory = new PrettyPrinterFactory();
		factory.setXmlDeclaration(false);
		factory.setAnonymizeFilters(new String[]{"/aparent/achild/@attr"});
		
		add(factory, attributeInput, attributeFilterAnonOutput);

		factory = new PrettyPrinterFactory();
		factory.setXmlDeclaration(false);
		factory.setAnonymizeFilters(new String[]{"/aparent/achild/@*"});
		
		add(factory, attributeInput, attributeFilterAnonWildcardOutput);
		
		factory = new PrettyPrinterFactory();
		factory.setXmlDeclaration(false);
		factory.setPruneFilters(new String[]{"/*/*"});
		
		add(factory, textFilterInput, textFilterPruneWildcardOutput);

		factory = new PrettyPrinterFactory();
		factory.setXmlDeclaration(false);
		factory.setAnonymizeFilters(new String[]{"/*/*"});
		
		add(factory, textFilterInput, textFilterAnonWildcardOutput);
		
		factory = new PrettyPrinterFactory();
		factory.setXmlDeclaration(false);
		factory.setAnonymizeFilters(new String[]{"//achild"});

		add(factory, textFilterInput, textFilterAnonAnyOutput);
		
		factory = new PrettyPrinterFactory();
		factory.setXmlDeclaration(false);
		factory.setPruneFilters(new String[]{"//achild"});

		add(factory, textFilterInput, textFilterPruneAnyOutput);
		
	}
	
	private static void add(PrettyPrinterFactory factory, String input, String output) {
		try {
			add(new PrettyPrinterFactoryItem(factory, input, output, defaultFactory));
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void add(PrettyPrinterFactoryItem factoryItem) {
		factoryItems.add(factoryItem);
	}
	
	public int evaluate(PrettyPrinter prettyPrinter, int minimum) throws Exception {
		int count = evaluate(prettyPrinter, true);
		if(count < minimum) {
			throw new IllegalArgumentException("Found " + count + " < minimum " + minimum + " for " + prettyPrinter);
		}
		
		return count;
	}

	public int evaluate(PrettyPrinter prettyPrinter) throws Exception {
		return evaluate(prettyPrinter, 0);
	}
	
	public int evaluate(PrettyPrinter prettyPrinter, boolean wellformed) throws Exception {
		int count = 0;
		for(PrettyPrinterFactoryItem factoryItem : factoryItems) {
			if(factoryItem.matches(prettyPrinter)) {
				AbstractPrettyPrinter impl = (AbstractPrettyPrinter)prettyPrinter;
				
				process(factoryItem.getInput(), factoryItem.getOutput(), prettyPrinter, wellformed, AbstractPrettyPrinter.defaultIndentationMultiplier, impl.isXmlDeclaration());
				
				count++;
			} else {
				// process and see that is as expected
				process(factoryItem.getInput(), prettyPrinter, wellformed);
			}
		}
		return count;
	}
	
	@BeforeClass
	public static void printTestRunNotification() {
		logger.info("This might take a while..");
	}
	
	@Before
	public void resetCharCapacity() {
		
	}
	
	@Test
	public void testConstructor1() throws Exception {
		
		PrettyPrinterConstructor prettyPrinterConstructor = new PrettyPrinterConstructor();
		
		// boolean cdata, boolean comment, boolean declaration
		prettyPrinterConstructor.addItem("cdata", new Object[]{Boolean.TRUE, Boolean.FALSE});
		prettyPrinterConstructor.addItem("comment", new Object[]{Boolean.TRUE, Boolean.FALSE});
		prettyPrinterConstructor.addItem("declaration", new Object[]{Boolean.TRUE, Boolean.FALSE});
		prettyPrinterConstructor.addItem("indentationCharacter", new Object[]{new Character(AbstractPrettyPrinter.defaultIndentationCharacter)});
		prettyPrinterConstructor.addItem("indentationMultiplier", new Object[]{new Integer(AbstractPrettyPrinter.defaultIndentationMultiplier)});

		List<PrettyPrinter> construct = prettyPrinterConstructor.construct(PlainPrettyPrinterForCDataAndComments.class);
		
		Assert.assertEquals(2 * 2 * 2, construct.size());
	}
	
	@Test
	public void testConstructor2() throws Exception {
		
		PrettyPrinterConstructor prettyPrinterConstructor = new PrettyPrinterConstructor();
		
		// boolean cdata, boolean comment, boolean declaration
		prettyPrinterConstructor.addItem("declaration", new Object[]{Boolean.TRUE, Boolean.FALSE});
		prettyPrinterConstructor.addItem("maxTextNodeLength", new Object[]{-1, Integer.MAX_VALUE, 26});
		prettyPrinterConstructor.addItem("maxCDATANodeLength", new Object[]{-1, Integer.MAX_VALUE, 26});
		prettyPrinterConstructor.addItem("indentationCharacter", new Object[]{new Character(AbstractPrettyPrinter.defaultIndentationCharacter)});
		prettyPrinterConstructor.addItem("indentationMultiplier", new Object[]{new Integer(AbstractPrettyPrinter.defaultIndentationMultiplier)});
		
		List<PrettyPrinter> construct = prettyPrinterConstructor.construct(PlainPrettyPrinterWithMaxNodeLength.class);
		
		Assert.assertEquals(2 * 3 * 3 - 2, construct.size());
	}
	
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
		evaluate(new SingleFilterPlainPrettyPrinter(false, "/aparent/achild", FilterType.ANON, '\t', 1));
		evaluate(new SingleFilterPlainPrettyPrinter(false, "/aparent/achild", FilterType.PRUNE, '\t', 1));
		evaluate(new SingleFilterPlainPrettyPrinter(false, "/aparent/achild/@attr", FilterType.ANON, '\t', 1));
		evaluate(new SingleFilterPlainPrettyPrinter(false, "/aparent/achild/@*", FilterType.ANON, '\t', 1), 1);

		evaluate(new SingleFilterPlainPrettyPrinter(false, "/*/*", FilterType.PRUNE, '\t', 1), 1);

		new SingleFilterPlainPrettyPrinter(false, "/aparent/achild/@attr", FilterType.ANON, '\t', 1).toString();
	}

	@Test
	public void testFilterSinglePlainPrettyPrinterWithMaxLength() throws Exception {
		evaluate(new SingleFilterPlainPrettyPrinterWithMaxNodeLength(false, "/aparent/achild", FilterType.ANON, -1, 26, '\t', 1));
		evaluate(new SingleFilterPlainPrettyPrinterWithMaxNodeLength(false, "/aparent/achild", FilterType.ANON, 26, -1, '\t', 1));

		evaluate(new SingleFilterPlainPrettyPrinterWithMaxNodeLength(false, "/aparent/achild/@attr", FilterType.ANON, -1, 26, '\t', 1));
		evaluate(new SingleFilterPlainPrettyPrinterWithMaxNodeLength(false, "/aparent/achild/@attr", FilterType.ANON, 26, -1, '\t', 1));
		evaluate(new SingleFilterPlainPrettyPrinterWithMaxNodeLength(false, "/aparent/achild/@*", FilterType.ANON, Integer.MAX_VALUE, Integer.MAX_VALUE, '\t', 1), 1);

		evaluate(new SingleFilterPlainPrettyPrinterWithMaxNodeLength(false, "/aparent/achild", FilterType.PRUNE, -1, 26, '\t', 1));
		evaluate(new SingleFilterPlainPrettyPrinterWithMaxNodeLength(false, "/aparent/achild", FilterType.PRUNE, 26, -1, '\t', 1));

		evaluate(new SingleFilterPlainPrettyPrinterWithMaxNodeLength(false, "/*/*", FilterType.PRUNE, Integer.MAX_VALUE, Integer.MAX_VALUE, '\t', 1), 1);

		new SingleFilterPlainPrettyPrinterWithMaxNodeLength(false, "/aparent/achild", FilterType.ANON, -1, 26, '\t', 1).toString();
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
	public void testMultiFilterRobustPlainPrettyPrinter() throws Exception {
		run(MultiFilterRobustPlainPrettyPrinter.class);
	}

	@Test
	public void testRobustPlainIndentedPrettyPrinter() throws Exception {
		run(RobustPlainIndentedPrettyPrinter.class);
	}

	private void run(Class cls) throws Exception {
		List<PrettyPrinter> construct = PrettyPrinterConstructor.getConstructor().construct(cls);
		Assert.assertFalse(construct.isEmpty());
		construct.get(0).toString(); // for code coverage
		
		int count = 0;
		for(PrettyPrinter pp : construct) {
			count += evaluate(pp);
		}
		
		Assert.assertTrue(count > 0);
	}


}
