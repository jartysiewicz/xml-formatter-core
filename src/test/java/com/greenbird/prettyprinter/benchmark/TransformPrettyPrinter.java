package com.greenbird.prettyprinter.benchmark;

import java.io.CharArrayReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;

import com.greenbird.prettyprinter.benchmark.utils.StringBuilderWriter;
import com.greenbird.xml.prettyprinter.plain.AbstractPrettyPrinter;

public class TransformPrettyPrinter extends AbstractPrettyPrinter {

	private Transformer transformer;
	private SAXParserFactory factory = SAXParserFactory.newInstance();
	
	public TransformPrettyPrinter() throws Exception {
		super(true);

		factory.setValidating(false);
		factory.setNamespaceAware(false);
		try {
			factory.setFeature( "http://apache.org/xml/features/nonvalidating/load-external-dtd" , false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		TransformerFactory transFactory = TransformerFactory.newInstance();
		
        transFactory.setAttribute("indent-number", new Integer(1));
        transformer = transFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	}
	
	@Override
	public boolean process(char[] chars, int offset, int length, StringBuilder output) {
	    try {
	        InputSource source = new InputSource(new CharArrayReader(chars, offset, length));

	        SAXParser saxParser = factory.newSAXParser();

        	transformer.transform(new SAXSource(saxParser.getXMLReader(), source), new StreamResult(new StringBuilderWriter(output)));
	    } catch (Exception e) {
	    	return false;
	    }
	    return true;
	}

}
