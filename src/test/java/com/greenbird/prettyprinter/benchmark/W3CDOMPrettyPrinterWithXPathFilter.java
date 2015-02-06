package com.greenbird.prettyprinter.benchmark;

import java.io.CharArrayReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import com.greenbird.prettyprinter.benchmark.utils.XPathFilter;
import com.greenbird.xml.prettyprinter.plain.AbstractPrettyPrinter;

public class W3CDOMPrettyPrinterWithXPathFilter extends AbstractPrettyPrinter {

	private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	private final DocumentBuilder documentBuilder;
	private final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
	private final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
	private final LSSerializer writer = impl.createLSSerializer();
	
	private final XPathFilter filter;
	
	public W3CDOMPrettyPrinterWithXPathFilter(XPathFilter filter) throws Exception {
    	super(true);
    	this.filter = filter;
    	
		documentBuilderFactory.setNamespaceAware(true);
		documentBuilderFactory.setValidating(false);
		try {
			documentBuilderFactory.setFeature( "http://apache.org/xml/features/nonvalidating/load-external-dtd" , false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		documentBuilder = documentBuilderFactory.newDocumentBuilder();
		
		writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE); // Set this to true if the output needs to be beautified.
		writer.getDomConfig().setParameter("xml-declaration", Boolean.FALSE); // Set this to true if the declaration is needed to be outputted.
	}
	
	@Override
	public boolean process(char[] chars, int offset, int length, StringBuilder output) {
	    try {
			final InputSource src = new InputSource(new CharArrayReader(chars, offset, length));
			
			Document document = documentBuilder.parse(src);

			filter.filter(document);
			
			output.append(writer.writeToString(document.getDocumentElement()));
	    } catch (Exception e) {
	    	return false;
	    }
	    return true;
	}
	

}
