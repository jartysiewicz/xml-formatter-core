package com.greenbird.prettyprinter.tools;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLFilterImpl;
       
public class WhitespaceRemoverTool {
       
	private static final String ACCESS_EXTERNAL_DTD = "http://javax.xml.XMLConstants/property/accessExternalDTD";
	private static final String ACCESS_EXTERNAL_SCHEMA = "http://javax.xml.XMLConstants/property/accessExternalSchema";
	
	private static final EntityResolver noEntityResolver = new EntityResolver() {
        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            return new InputSource(new StringReader(""));
        }
	};
	
	private static class DataFilter extends XMLFilterImpl {
		
		private StringBuffer buffer = new StringBuffer();
				
		 public DataFilter(XMLReader xmlReader) {
	            super(xmlReader);
	        }
		 
		public void startElement(String namespaceURI, String localName, String qName, Attributes attribs)
				throws SAXException {           
			buffer.setLength(0);
			
			super.startElement(namespaceURI, localName, qName, attribs);
		}

		public void characters(char[] ch, int start, int length) throws SAXException {
			buffer.append(ch, start, length);
		}

		public void endElement(String namespaceURI, String localName, String qName) throws SAXException {           
			if(buffer.length() > 0) {
				String trimmed = buffer.toString().trim();
				if(trimmed.length() > 0) {
					char[] charArray = trimmed.toCharArray();
					super.characters(charArray, 0, charArray.length);
				}
				buffer.setLength(0);
			}
			super.endElement(namespaceURI, localName, qName);
		}
	}
	
	private static class XMLFilterBridge  extends DefaultHandler
    {
        private XMLFilterImpl _filter;

        public XMLFilterBridge(XMLFilterImpl myFilter)
        {
            _filter = myFilter;
        }

        @Override
        public void characters(char[] ch, int start, int length)
        throws SAXException
        {
            _filter.characters(ch, start, length);
        }

        // override all other methods of DefaultHandler
        // ...
    }

    public static String clean(String xml) throws Exception {
 	   StringWriter writer = new StringWriter();
 	   
 	   StringReader reader = new StringReader(xml);
 	   
 	   return clean(reader, writer);
    }

    public static String clean(Reader reader, Writer writer) throws Exception {

	   SAXParserFactory factory = SAXParserFactory.newInstance();
	   factory.setValidating(false);
	   factory.setNamespaceAware(false);
	   factory.setFeature("http://xml.org/sax/features/namespaces", false);
	   factory.setFeature("http://xml.org/sax/features/validation", false);
	   factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
	   factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

	   Transformer xform = TransformerFactory.newInstance().newTransformer();
	   
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
	   
       XMLReader xmlReader = saxParser.getXMLReader();
       xmlReader.setEntityResolver(noEntityResolver);
       
	   XMLReader xmlFilterReader = new DataFilter(xmlReader);
	   
	   try {
		   xmlFilterReader.setProperty(ACCESS_EXTERNAL_DTD, Boolean.FALSE.toString());
	   } catch(Exception e) {
		   //ignore
	   }
	   try {
		   xmlFilterReader.setProperty(ACCESS_EXTERNAL_SCHEMA, Boolean.FALSE.toString());
	   } catch(Exception e) {
		   //ignore
	   }
	   xmlFilterReader.setEntityResolver(noEntityResolver);

       xform.transform(new SAXSource(xmlFilterReader, new InputSource(reader)), new StreamResult(writer));
       
       return writer.toString();
    
    }
       
	public static void main (String[] args){

		if(args == null || (args.length != 2)) {
			System.out.println("Strip whitespace from XML files in a directory.");
			System.out.println();
			System.out.println("Usage: input-directory output-directory");
			System.out.println();
			System.exit(1);
		}

		try {
			File directory = new File(args[0]);
			File outputDirectory = new File(args[1]);

			File[] listFiles = directory.listFiles(new FileFilter() {
				
				@Override
				public boolean accept(File file) {
					return file.getName().toLowerCase().endsWith(".xml");
				}
			});
			for(File file : listFiles) {
				if(!file.isFile()) continue;

				FileInputStream in = new FileInputStream(file);
				FileOutputStream out = new FileOutputStream(new File(outputDirectory, file.getName()));
				
				InputStreamReader reader = new InputStreamReader(in);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				
				clean(reader, writer);

				reader.close();
				writer.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 

	}
}