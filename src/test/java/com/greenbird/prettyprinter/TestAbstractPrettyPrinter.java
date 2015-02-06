package com.greenbird.prettyprinter;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;

import com.greenbird.xml.prettyprinter.plain.AbstractPrettyPrinter;
import com.greenbird.xml.prettyprinter.plain.PlainPrettyPrinter;
import com.greenbird.xml.prettyprinter.plain.PlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength;
import com.greenbird.xml.prettyprinter.plain.filter.AbstractMultiFilterPrettyPrinter.FilterType;
import com.greenbird.xml.prettyprinter.plain.filter.MultiFilterPlainPrettyPrinter;
import com.greenbird.xml.prettyprinter.plain.filter.SingleFilterPlainPrettyPrinterWithMaxNodeLength;

/**
 * Target various aspects of abstract pretty printer classes.
 * 
 * @author thomas
 *
 */

public class TestAbstractPrettyPrinter {
 
	@Test
	public void testIndentCapacity() {
		char[][] ensureCharCapacity = new PlainPrettyPrinter(true).ensureCharCapacity(AbstractPrettyPrinter.defaultIndentationDepth + 1);
		
		Assert.assertEquals(AbstractPrettyPrinter.defaultIndentationDepth + 1, ensureCharCapacity.length);
	}
	
	@Test 
	public void testReader1() throws IOException {
		String xml = "<xml/>";
		
		PlainPrettyPrinter prettyPrinter = new PlainPrettyPrinter(true);
		
		prettyPrinter.process(xml, new StringBuilder());

		prettyPrinter.process(new StringReader(xml), xml.length(), new StringBuilder());
	}

	@Test 
	public void testReaderExceptionHandlingShortContent() throws IOException {
		String xml = "<xml/>";
		
		PlainPrettyPrinter prettyPrinter = new PlainPrettyPrinter(true);

		try {
			prettyPrinter.process(new StringReader(xml), 10, new StringBuilder());
			
			Assert.fail();
		} catch(Exception e) {
			
		}
	}

	@Test 
	public void testReaderExceptionHandlingMarkNotSupported() throws IOException {
		PlainPrettyPrinter prettyPrinter = new PlainPrettyPrinter(true);

		try {
			prettyPrinter.process(new Reader() {
				
				@Override
				public int read(char[] cbuf, int off, int len) throws IOException {
					throw new IllegalArgumentException();
				}
				
				@Override
				public void close() throws IOException {
					throw new IllegalArgumentException();
				}
				
				@Override
				public boolean markSupported() {
					return false;
				}
			}, 10, new StringBuilder());
			
			Assert.fail();
		} catch(Exception e) {
			
		}
	}

	@Test
	public void testMaxLengthConstructor1() {
		try {
			new PlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength(false, false, false, 1024, -2, '\t', 1);
			
			Assert.fail();
		} catch(Exception e) {
			
		}
	}

	@Test
	public void testMaxLengthConstructor2() {
		try {
			new PlainPrettyPrinterForCDataAndCommentsWithMaxNodeLength(false, false, false, -2, 1024, '\t', 1);
			
			Assert.fail();
		} catch(Exception e) {
			
		}
	}

	@Test
	public void testIncorrectXPath1() {
		try {
			new SingleFilterPlainPrettyPrinterWithMaxNodeLength(false, "/aparent/@achild", FilterType.PRUNE, -1, 26, '\t', 1);
			
			Assert.fail();
		} catch(Exception e) {
			
		}
	}

	@Test
	public void testIncorrectXPath2() {
		try {
			new MultiFilterPlainPrettyPrinter(false, null, new String[]{"/aparent/@achild"}, '\t', 1);
			
			Assert.fail();
		} catch(Exception e) {
			
		}
	}

	@Test
	public void testSingleCharAttribute() {
		MultiFilterPlainPrettyPrinter prettyPrinter = new MultiFilterPlainPrettyPrinter(false, new String[]{"/aparent/@a"}, null, '\t', 1);
			
		StringBuilder output = new StringBuilder();
		
		Assert.assertTrue(prettyPrinter.process("<aparent a='b' b='c'/>", output));
		
		Assert.assertEquals("<aparent a='[*****]' b='c'/>", output.toString().substring(1));
	}

	@Test
	public void testNegativeIndentation() {
		try {
			PlainPrettyPrinter prettyPrinter = new PlainPrettyPrinter(true, '\t', -1);
			
			Assert.fail();
		} catch(Exception e) {
			
		}
	}

}
