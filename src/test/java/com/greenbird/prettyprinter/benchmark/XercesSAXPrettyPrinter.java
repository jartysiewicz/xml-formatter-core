package com.greenbird.prettyprinter.benchmark;

import java.io.CharArrayReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;

import org.xml.sax.InputSource;

import com.fasterxml.aalto.stax.OutputFactoryImpl;
import com.greenbird.prettyprinter.benchmark.utils.ContentHandlerToXMLStreamWriter;
import com.greenbird.prettyprinter.benchmark.utils.IndentingXMLStreamWriter;
import com.greenbird.prettyprinter.benchmark.utils.StringBuilderWriter;
import com.greenbird.xml.prettyprinter.plain.AbstractPrettyPrinter;

/**
 * 
 * This class is used to define the lower limit of any XML parse + writer approach for pretty printing.
 * 
 * @author thomas
 *
 */

public class XercesSAXPrettyPrinter extends AbstractPrettyPrinter {

	private static final String ACCESS_EXTERNAL_DTD = "http://javax.xml.XMLConstants/property/accessExternalDTD";
	private static final String ACCESS_EXTERNAL_SCHEMA = "http://javax.xml.XMLConstants/property/accessExternalSchema";

	private SAXParserFactory factory = SAXParserFactory.newInstance();
	private XMLOutputFactory outputFactory = OutputFactoryImpl.newInstance();

	
	public XercesSAXPrettyPrinter() {
		super(true);
		factory.setNamespaceAware(false);
		factory.setValidating(false);
		try {
			factory.setFeature( "http://apache.org/xml/features/nonvalidating/load-external-dtd" , false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public boolean process(char[] chars, int offset, int length, StringBuilder output) {
		try {
	        InputSource source = new InputSource(new CharArrayReader(chars, offset, length));

	        ContentHandlerToXMLStreamWriter contentHandlerToXMLStreamWriter = new ContentHandlerToXMLStreamWriter(new IndentingXMLStreamWriter(outputFactory.createXMLStreamWriter(new StringBuilderWriter(output)), "\t"));
	        
	        SAXParser saxParser = factory.newSAXParser();
	        
	 	   try {
			   saxParser.setProperty(ACCESS_EXTERNAL_DTD, Boolean.FALSE.toString());
		   } catch(Exception e) {
			   //ignore
		   }
		   try {
			   saxParser.setProperty(ACCESS_EXTERNAL_SCHEMA, Boolean.FALSE.toString());
		   } catch(Exception e) {
			   //ignore
		   }

        	saxParser.parse(source, contentHandlerToXMLStreamWriter);
		} catch(Exception e) {
			return false;
		}

		return true;

	}
	
}

