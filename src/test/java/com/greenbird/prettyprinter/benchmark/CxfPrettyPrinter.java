package com.greenbird.prettyprinter.benchmark;

import java.io.CharArrayReader;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.greenbird.prettyprinter.benchmark.utils.StringBuilderWriter;
import com.greenbird.xml.prettyprinter.plain.AbstractPrettyPrinter;

/**
 * Cxf-ish pretty printer
 * 
 * @author thomas
 *
 */

public class CxfPrettyPrinter extends AbstractPrettyPrinter {
	
	public CxfPrettyPrinter() {
		super(true);
	}

	private static final Map<ClassLoader, TransformerFactory> TRANSFORMER_FACTORIES = Collections.synchronizedMap(new WeakHashMap<ClassLoader, TransformerFactory>());

	private static TransformerFactory getTransformerFactory() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		if (loader == null) {
			loader = CxfPrettyPrinter.class.getClassLoader();
		}
		if (loader == null) {
			return TransformerFactory.newInstance();
		}
		TransformerFactory factory = TRANSFORMER_FACTORIES.get(loader);
		if (factory == null) {
			factory = TransformerFactory.newInstance();
			TRANSFORMER_FACTORIES.put(loader, factory);
		}
		return factory;
	}

	public static Transformer newTransformer(int indent) throws TransformerConfigurationException {
		if (indent > 0) {
			TransformerFactory f = TransformerFactory.newInstance();
			try {
				//sun way of setting indent
				f.setAttribute("indent-number", Integer.toString(indent));
			} catch (Throwable t) {
				//ignore
			}
			return f.newTransformer();
		}
		return getTransformerFactory().newTransformer();
	}

	@Override
	public boolean process(char[] chars, int offset, int length, StringBuilder builder) {
		
		try {
		    Transformer serializer = newTransformer(1);
		    // Setup indenting to "pretty print"
		    serializer.setOutputProperty(OutputKeys.INDENT, "yes");
		    serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "1");
		    
		    StringBuilderWriter writer = new StringBuilderWriter(builder);
		    serializer.transform(new StreamSource(new CharArrayReader(chars, offset, length)), new StreamResult(writer));
		} catch(Exception e) {
			return false;
		}
	    return true;
	}


}
