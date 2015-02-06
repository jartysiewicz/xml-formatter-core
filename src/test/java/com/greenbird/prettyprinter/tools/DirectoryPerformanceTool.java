package com.greenbird.prettyprinter.tools;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.greenbird.prettyprinter.benchmark.AaltoStaxPrettyPrinter;
import com.greenbird.prettyprinter.benchmark.DOM4JPrettyPrinter;
import com.greenbird.prettyprinter.benchmark.TransformPrettyPrinter;
import com.greenbird.prettyprinter.benchmark.W3CDOMPrettyPrinter;
import com.greenbird.prettyprinter.benchmark.W3CDOMPrettyPrinterWithXPathFilter;
import com.greenbird.prettyprinter.benchmark.XercesSAXPrettyPrinter;
import com.greenbird.prettyprinter.benchmark.utils.MapNamespaceContext;
import com.greenbird.prettyprinter.benchmark.utils.XPathFilter;
import com.greenbird.prettyprinter.benchmark.utils.XPathFilterFactory;
import com.greenbird.prettyprinter.utils.FileDirectoryCache;
import com.greenbird.prettyprinter.utils.FileDirectoryValue;
import com.greenbird.xml.prettyprinter.PrettyPrinter;
import com.greenbird.xml.prettyprinter.plain.AbstractPrettyPrinter;
import com.greenbird.xml.prettyprinter.plain.PlainPrettyPrinter;
import com.greenbird.xml.prettyprinter.plain.PlainPrettyPrinterForCDataAndComments;
import com.greenbird.xml.prettyprinter.plain.PlainPrettyPrinterForTextNodesWithXML;
import com.greenbird.xml.prettyprinter.plain.PlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength;
import com.greenbird.xml.prettyprinter.plain.PlainPrettyPrinterWithMaxNodeLength;
import com.greenbird.xml.prettyprinter.plain.RobustPlainPrettyPrinter;
import com.greenbird.xml.prettyprinter.plain.filter.AbstractMultiFilterPrettyPrinter.FilterType;
import com.greenbird.xml.prettyprinter.plain.filter.MultiFilterPlainPrettyPrinter;
import com.greenbird.xml.prettyprinter.plain.filter.SingleFilterPlainPrettyPrinter;
import com.greenbird.xml.prettyprinter.plain.ws.PlainIndentedPrettyPrinter;

public class DirectoryPerformanceTool {
	
	public static void main(String[] args) throws Exception {

		if(args == null || (args.length != 5)) {
			System.out.println("Usage: iterations warmup-iterations directory recursive clean-whitespace");
			System.out.println();
			System.out.println("Example: 10000 1000 saml/ true true");
			System.exit(1);
		}

		int iterations = Integer.parseInt(args[0]);
		int warmupIterations = Integer.parseInt(args[1]);
		
		boolean recursive = Boolean.parseBoolean(args[3]);
		boolean cleanWhitespace = Boolean.parseBoolean(args[4]);

		String file = args[2];
		
		List<FileDirectoryValue> directories = new FileDirectoryCache().getValue(new File(file), new FileFilter() {
			
			@Override
			public boolean accept(File file) {
				return file.getName().toLowerCase().endsWith(".xml");
			}
		}, recursive);

		if(cleanWhitespace) {
			for(FileDirectoryValue directory : directories) {
				for(int i = 0; i < directory.size(); i++) {
					try {
						directory.setValue(i, WhitespaceRemoverTool.clean(new String(directory.getValue(i))));
					} catch(Exception e) {
						System.out.println(new String(directory.getValue(i)));
						throw new RuntimeException("Unable to remove whitespace for source " + directory.getFile(i), e);
					}
				}
			}
		}
		
		int fileCount = 0;
		for(FileDirectoryValue directory : directories) {
			fileCount += directory.size();
		}
		
		System.out.println();
		System.out.println("Scanned " + directories.size() + " directories and found " + fileCount + " XML files");
		
		List<PrettyPrinter> prettyPrinters = new ArrayList<PrettyPrinter>();

		int maxNodeSize = 25;
		
		prettyPrinters.add(new XercesSAXPrettyPrinter());
		prettyPrinters.add(new PlainIndentedPrettyPrinter(false, AbstractPrettyPrinter.defaultIndentationCharacter, AbstractPrettyPrinter.defaultIndentationMultiplier));
		prettyPrinters.add(new RobustPlainPrettyPrinter(true,  true,  true,  true,  true,  true,  true, AbstractPrettyPrinter.defaultIndentationCharacter, AbstractPrettyPrinter.defaultIndentationMultiplier));
		prettyPrinters.add(new PlainPrettyPrinter(false));
		prettyPrinters.add(new PlainPrettyPrinterWithMaxNodeLength(false, maxNodeSize, maxNodeSize, AbstractPrettyPrinter.defaultIndentationCharacter, AbstractPrettyPrinter.defaultIndentationMultiplier));
		prettyPrinters.add(new PlainIndentedPrettyPrinter(false, AbstractPrettyPrinter.defaultIndentationCharacter, AbstractPrettyPrinter.defaultIndentationMultiplier));
		prettyPrinters.add(new PlainPrettyPrinterForCDataAndComments(true, true, false, AbstractPrettyPrinter.defaultIndentationCharacter, AbstractPrettyPrinter.defaultIndentationMultiplier));
		prettyPrinters.add(new PlainPrettyPrinterForTextNodesWithXML(true, true, false, false, AbstractPrettyPrinter.defaultIndentationCharacter, AbstractPrettyPrinter.defaultIndentationMultiplier));
		prettyPrinters.add(new PlainPrettyPrinterForTextNodesWithXMLAndMaxNodeLength(true, true, false, false, maxNodeSize, maxNodeSize, AbstractPrettyPrinter.defaultIndentationCharacter, AbstractPrettyPrinter.defaultIndentationMultiplier));
		prettyPrinters.add(new RobustPlainPrettyPrinter(true, false, true, false, true, false, false, AbstractPrettyPrinter.defaultIndentationCharacter, AbstractPrettyPrinter.defaultIndentationMultiplier));
		prettyPrinters.add(new RobustPlainPrettyPrinter(true, false, true, false, true, false, false, maxNodeSize, maxNodeSize, AbstractPrettyPrinter.defaultIndentationCharacter, AbstractPrettyPrinter.defaultIndentationMultiplier));
		
		prettyPrinters.add(new TransformPrettyPrinter());
		prettyPrinters.add(new AaltoStaxPrettyPrinter());
		prettyPrinters.add(new W3CDOMPrettyPrinter());
		prettyPrinters.add(new DOM4JPrettyPrinter());
		
		XPathFilterFactory factory = new XPathFilterFactory();
		Map<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("env", "http://schemas.xmlsoap.org/soap/envelope/");
		namespaces.put("wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
		
		String[] anon = new String[]{"/env:Envelope/env:Header/wsse:Security/wsse:UsernameToken/wsse:Password"};
		
		MapNamespaceContext context = new MapNamespaceContext(namespaces);
		XPathFilter filter = factory.getFilter(context, null, anon);
		
		prettyPrinters.add(new W3CDOMPrettyPrinterWithXPathFilter(filter));
		
		String path = "/Envelope/Header/Security/UsernameToken/Password";
		
		prettyPrinters.add(new SingleFilterPlainPrettyPrinter(false, path, FilterType.ANON, AbstractPrettyPrinter.defaultIndentationCharacter, AbstractPrettyPrinter.defaultIndentationMultiplier));
		prettyPrinters.add(new MultiFilterPlainPrettyPrinter(false, new String[]{path}, null, AbstractPrettyPrinter.defaultIndentationCharacter, AbstractPrettyPrinter.defaultIndentationMultiplier));
		
		benchmark(prettyPrinters, iterations, warmupIterations, directories);
		
	}

	public static void benchmark(List<PrettyPrinter> prettyPrinters, int iterations, int warmupIterations, List<FileDirectoryValue> directories) {
		System.out.println("Benchmark using " + prettyPrinters.size() + " pretty-printers");

		System.out.println("Warmup start - " + warmupIterations + " iterations..");

		StringBuilder builder = new StringBuilder();

		// warmup
		long sizeSum = 0;
		for(FileDirectoryValue directory : directories) {
			
			for(int i = 0; i < directory.size(); i++) {
				char[] xmlChars = directory.getValue(i);
			
				for(PrettyPrinter prettyPrinter : prettyPrinters) {
					for(int k = 0; k < warmupIterations; k++) {
						if(prettyPrinter.process(xmlChars, 0, xmlChars.length, builder)) {
							sizeSum += builder.length();
						} else {
							System.out.println(new String(xmlChars));
							throw new RuntimeException("Unable to pretty-print using " + prettyPrinter + " for source " + directory.getFile(i));
						}
						
						// reset builder for next iteration
						builder.setLength(0);
					}
				}
			}
		}
		System.out.println("Warmup completed");

		long benchmarkStart = System.currentTimeMillis();
		
		System.out.println("Benchmark start - " + iterations + " iterations..");

		long[] durations = new long[prettyPrinters.size()];
		String[] bandwidths = new String[prettyPrinters.size()];
		
		for(int l = 0; l < prettyPrinters.size(); l++) {
			PrettyPrinter prettyPrinter = prettyPrinters.get(l);
			
			System.gc();

			long byteCount = 0;

			long time = System.currentTimeMillis();
			
			for(FileDirectoryValue directory : directories) {

				for(int k = 0; k < directory.size(); k++) {
					char[] xmlChars = directory.getValue(k);

					for(int i = 0; i < iterations; i++) {
						if(prettyPrinter.process(xmlChars, 0, xmlChars.length, builder)) {
							byteCount += builder.length();
						} else {
							throw new RuntimeException();
						}
						builder.setLength(0);
					}
				}
			}
			
			durations[l] = System.currentTimeMillis() - time;
			bandwidths[l] = getBandwidth(byteCount, durations[l]);
		}
		
		System.out.println("Benchmark completed in " + ((System.currentTimeMillis() - benchmarkStart) / 1000) + " seconds");
		System.out.println();
		System.out.println("Results (name, time, rate):");
		StringBuffer stringBuffer = new StringBuffer();

		for(int l = 0; l < prettyPrinters.size(); l++) {
			PrettyPrinter prettyPrinter = prettyPrinters.get(l);
			stringBuffer.append(prettyPrinter.getClass().getSimpleName());
			stringBuffer.append(",");
			stringBuffer.append(Long.toString(durations[l]));
			stringBuffer.append(",");
			stringBuffer.append(bandwidths[l]);
			stringBuffer.append(",");
			
			stringBuffer.append("\n");
		}
		
		System.out.println(stringBuffer);
	}

	public static String getBandwidth(long bytes, long duration) {
		return (bytes / duration) / 1024 + "MB/s";
	}	
	

}
 