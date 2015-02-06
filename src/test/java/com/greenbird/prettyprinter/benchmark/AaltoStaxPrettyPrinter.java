package com.greenbird.prettyprinter.benchmark;

import java.io.CharArrayReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.codehaus.stax2.XMLStreamWriter2;

import com.fasterxml.aalto.stax.InputFactoryImpl;
import com.fasterxml.aalto.stax.OutputFactoryImpl;
import com.greenbird.prettyprinter.benchmark.utils.IndentingXMLStreamWriter2;
import com.greenbird.prettyprinter.benchmark.utils.StringBuilderWriter;

public class AaltoStaxPrettyPrinter extends AbstractStAXPrettyPrinter {

	public AaltoStaxPrettyPrinter() {
		super(true);
	}

	private XMLInputFactory inputFactory = InputFactoryImpl.newInstance();
	private XMLOutputFactory outputFactory = OutputFactoryImpl.newInstance();

	@Override
	public boolean process(char[] chars, int offset, int length, StringBuilder output) {
		
		try {
			XMLStreamReader reader = inputFactory.createXMLStreamReader(new CharArrayReader(chars, offset, length));
		
			XMLStreamWriter writer = new IndentingXMLStreamWriter2((XMLStreamWriter2) outputFactory.createXMLStreamWriter(new StringBuilderWriter(output)), "\t");
			
			copy(reader, writer);
		} catch(Exception e) {
			return false;
		}
		return true;
	}
}
