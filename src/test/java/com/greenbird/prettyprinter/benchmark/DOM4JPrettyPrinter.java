package com.greenbird.prettyprinter.benchmark;

import java.io.CharArrayReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.greenbird.prettyprinter.benchmark.utils.StringBuilderWriter;
import com.greenbird.xml.prettyprinter.plain.AbstractPrettyPrinter;

public class DOM4JPrettyPrinter extends AbstractPrettyPrinter {

	private final OutputFormat format = OutputFormat.createPrettyPrint();
	
    private final SAXParserFactory factory = SAXParserFactory.newInstance();
	private final XMLWriter writer;
	
    public DOM4JPrettyPrinter() throws Exception {
    	super(true);
    	
		factory.setNamespaceAware(false);
		factory.setValidating(false);
		try {
			factory.setFeature( "http://apache.org/xml/features/nonvalidating/load-external-dtd" , false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
        writer = new XMLWriter(format);
	}
    
	@Override
	public boolean process(char[] chars, int offset, int length, StringBuilder output) {
	    try {
	        StringBuilderWriter sw = new StringBuilderWriter(output);
	        
	        InputSource source = new InputSource(new CharArrayReader(chars, offset, length));

	        SAXParser saxParser = factory.newSAXParser();
	        
	        XMLReader xmlReader = saxParser.getXMLReader();
	        xmlReader.setContentHandler(writer);

	        writer.setWriter(sw);
	        
	        saxParser.getXMLReader().parse(source);
	        
	        writer.flush();
	    } catch (Exception e) {
	    	return false;
	    }
	    return true;
	}

}
