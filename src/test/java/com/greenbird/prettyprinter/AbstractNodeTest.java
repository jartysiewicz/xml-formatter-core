package com.greenbird.prettyprinter;

import static com.greenbird.prettyprinter.utils.XMLUtils.checkWellformed;
import static com.greenbird.prettyprinter.utils.XMLUtils.hasXMLDeclaration;
import static com.greenbird.prettyprinter.utils.XMLUtils.isIndented;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.greenbird.prettyprinter.utils.FileDirectoryValue;
import com.greenbird.prettyprinter.utils.FileUtils;
import com.greenbird.prettyprinter.utils.XMLUtils;
import com.greenbird.xml.prettyprinter.PrettyPrinter;

/**
 * Abstract test class.
 * 
 * @author thomas
 *
 */

public class AbstractNodeTest {

	protected static Logger logger = Logger.getLogger(AbstractNodeTest.class.getName());

	static {
		final InputStream inputStream = AbstractNodeTest.class.getResourceAsStream("/logging.properties");
		try
		{
		    LogManager.getLogManager().readConfiguration(inputStream);
		}
		catch (final IOException e)
		{
		    Logger.getAnonymousLogger().severe("Could not load default logging.properties file");
		    Logger.getAnonymousLogger().severe(e.getMessage());
		}
	}


	private FileFilter xmlFilterFilter = new FileFilter() {
		
		@Override
		public boolean accept(File pathname) {
			return pathname.getName().endsWith(".xml");
		}
	};

	protected void process(String[] inputs, String output, PrettyPrinter processor, int indentatinMultiplier, boolean declaration) throws IOException, Exception {
		for(String input : inputs) {
			process(input, output, processor, true, indentatinMultiplier, declaration);
		}
	}

	protected void process(String input, String output, PrettyPrinter processor, boolean wellformed, int indentatinMultiplier, boolean declaration) throws Exception, IOException {
		process(input, output, processor, wellformed, indentatinMultiplier, declaration, xmlFilterFilter);
	}

	protected void process(String input, String output, PrettyPrinter processor, boolean wellformed, int indentatinMultiplier, boolean declaration, FileFilter fileFilter) throws Exception, IOException {
		
		FileDirectoryValue inputValues = FileUtils.getValue(input, fileFilter);
		FileDirectoryValue outputValues = FileUtils.getValue(output, fileFilter);
		
		for(int i = 0; i < inputValues.size(); i++) {
			
			if(!inputValues.getFile(i).getName().equals(outputValues.getFile(i).getName())) {
				throw new IllegalArgumentException("Inconsistant input / output files");
			}
			
			String from = inputValues.getStringValue(i);
			if(wellformed) {
				checkWellformed(from);
			}
			String to = XMLUtils.process(processor, from);
			
			if(declaration) {
				assertTrue(hasXMLDeclaration(to));
			} else {
				assertFalse(hasXMLDeclaration(to));
			}
			if(wellformed) {
				checkWellformed(to);
				if(!isIndented(to, indentatinMultiplier)) {
					logger.severe("From: \n" + from);
					logger.severe(inputValues.getFile(i).toString() + " -> " + outputValues.getFile(i).toString() + " / " + processor);
					logger.severe("Actual:\n" + to);
					
					fail(inputValues.getFile(i).toString() + " / " + processor);
				}
			}
			
			String expected = outputValues.getStringValue(i);
			if(!new String(expected).equals(to)) {
				logger.severe(inputValues.getFile(i).toString() + " -> " + outputValues.getFile(i).toString() + " / " + processor);
				logger.severe("From: \n" + from);
				logger.severe("Expected:\n" + expected);
				logger.severe("Actual:\n" + to);
				logger.severe("(size " + expected.length() + " vs " + to.length() + ")");
				
				for(int k = 0; k < Math.min(expected.length(), to.length()); k++) {
					if(expected.charAt(k) != to.charAt(k)) {
						logger.severe("Diff at " + k + ": " + expected.charAt(k) + " vs + " + to.charAt(k));
						
						break;
					}
				}
				fail(inputValues.getFile(i).toString() + " / " + processor);
			}
		}
		
		logger.finer("Processed " +inputValues.size() + " files in " + input + " -> " + output + " for " + processor);
	}
	protected void process(String input, PrettyPrinter processor, boolean expected) throws Exception {
		process(input, processor, expected, xmlFilterFilter);
	}

	protected void process(String input, PrettyPrinter processor, boolean expected, FileFilter fileFilter) throws Exception {
		
		FileDirectoryValue inputValues = FileUtils.getValue(input, fileFilter);

		for(int i = 0; i < inputValues.size(); i++) {
			//System.out.println(sourceFiles[i]);
			String from = inputValues.getStringValue(i);
			
			if(expected != XMLUtils.attempt(processor, from)) {
				logger.severe(processor.toString() + " vs " + inputValues.getFile(i));
				logger.severe(from);
				logger.severe(XMLUtils.process(processor, from));
				throw new IllegalArgumentException(inputValues.getFile(i).toString());
			}
			
		}
		
		logger.finer("Processed " + inputValues.size() + " files in " + input + " for " + processor.getClass().getSimpleName() + " (no compare)");
		
	}

	
}
