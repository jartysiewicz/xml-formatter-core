package com.greenbird.prettyprinter;

import org.junit.Assert;
import org.junit.Test;

import com.greenbird.xml.prettyprinter.PrettyPrinter;
import com.greenbird.xml.prettyprinter.PrettyPrinterBuilder;
import com.greenbird.xml.prettyprinter.plain.PlainPrettyPrinter;

public class TestPrettyPrinterBuilder extends AbstractNodeTest {

	@Test
	public void testBuilderNone() throws Exception {
		PrettyPrinter newPrettyPrinter = PrettyPrinterBuilder.newPrettyPrinter().build();
		
		Assert.assertEquals(newPrettyPrinter.getClass(), PlainPrettyPrinter.class);
	}
	
	@Test
	public void testBuilderAll() throws Exception {
		PrettyPrinter newPrettyPrinter = PrettyPrinterBuilder.newPrettyPrinter()
				.anonymize(new String[]{"/abcd/a"})
				.keepXMLDeclaration()
				.maxCDataNodeLength(1024)
				.maxNodeLength(1024)
				.maxTextNodeLength(1024)
				.prettyPrintCData()
				.prettyPrintComments()
				.prettyPrintTextNodes()
				.prune(new String[]{"/abcd/a"})
				.robust()
				.indentate('\t', 1)
				.build();
		
		Assert.assertNotNull(newPrettyPrinter);
	}
	
}
