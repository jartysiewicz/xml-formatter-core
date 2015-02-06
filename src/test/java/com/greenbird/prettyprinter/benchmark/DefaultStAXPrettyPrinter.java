package com.greenbird.prettyprinter.benchmark;

import java.io.CharArrayReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.greenbird.prettyprinter.benchmark.utils.IndentingXMLStreamWriter;
import com.greenbird.prettyprinter.benchmark.utils.StringBuilderWriter;

public class DefaultStAXPrettyPrinter extends AbstractStAXPrettyPrinter {

	private XMLInputFactory inputFactory = XMLInputFactory.newInstance();
	private XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
	
	public DefaultStAXPrettyPrinter() {
		super(true);
	}
	
	@Override
	public boolean process(char[] chars, int offset, int length, StringBuilder output) {
		try {
			XMLStreamReader reader = inputFactory.createXMLStreamReader(new CharArrayReader(chars, offset, length));
			
			XMLStreamWriter writer = new IndentingXMLStreamWriter(outputFactory.createXMLStreamWriter(new StringBuilderWriter(output)), "\t");
			
			copy(reader, writer);
		} catch(Exception e) {
			e.printStackTrace();
			
			return false;
		}
		return true;
	}

}
