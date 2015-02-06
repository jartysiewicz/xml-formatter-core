# xml-formatter
Library for pretty-printing of XML for logging purposes. 

The library contains XML character stream processors which insert indentation whitespaces into the correct positions in a single step, rather than a two-step parse/serialize approach. Relying on use within existing XML stacks, a simplified view of the XML syntax is acceptable, and the corresponding reduction of complexity has resulted in drastically increased throughput.

Projects using this library will benefit from:

  * High-performance reformatting of XML
  * Advanced filtering options
    * Max text and/or CDATA node sizes
    * Reformatting of XML within text and/or CDATA nodes
    * Anonymize of element and/or attribute contents
    * Removal of subtrees

This project is part of the [greenbird] Open Source Java [projects].

Bugs, feature suggestions and help requests can be filed with the [issue-tracker].

[![Build Status][build-badge]][build-link]

## License
[Apache 2.0]

## Obtain
The project is based on [Maven] and is available on the central Maven repository.

Example dependency config:

```xml
<dependency>
    <groupId>com.greenbird.xml-formatter</groupId>
    <artifactId>xml-formatter-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

You can also [download] the jar directly if you need too.

Snapshot builds are available from the Sonatype OSS [snapshot repository].

# Usage
Preferred use is within existing XML stacks, which provide detailed XML document error handling, in addition to message validation and so on. 

### Initialization
Obtain a `PrettyPrinter` instance:

    // initialization
    PrettyPrinterFactory factory = PrettyPrinterFactory.newInstance();
    // configure factory
    factory.setXmlDeclaration(false);
    // ..
    
    // create pretty printer
    PrettyPrinter prettyPrinter = factory.newPrettyPrinter(); // thread-safe pretty-printer
    
    // Store prettyPrinter instance for later use..

### Pretty-printing XML

    // decode chars from bytes, reusing a big buffer, etc (out of scope)
    char[] chars = .. 
    int charsLength = ..;
    
    // get a string buffer (or reuse an existing)
    StringBuilder buffer = new StringBuilder(charsLength * 2);
    
    // perform local logging - to the buffer 
    buffer.append("LocalKeyA=");
    buffer.append(keyA);
    buffer.append(" GlobalKeyB=");
    buffer.append(keyB);
    
    // pretty print XML
    if(prettyPrinter.process(chars, 0, charsLength, buffer)) {
        // log as normal
        logger.debug(buffer.toString());
    } else {
        // something unexpected - log as warning
        buffer.append(" was unable to format XML");
        buffer.append('\n');
        buffer.append(chars); // unmodified XML
        
        logger.warn(buffer.toString());
    }
    
Output should be along the lines of

    DEBUG  2011-08-02 12:21:58,495 [tag or class] LocalKeyA=keyA GlobalKeyB=keyB
    <parent>
        <child>text</child>
    </parent>

or, for invalid XML:

    WARN  2011-08-02 12:21:58,495 [tag or class] LocalKeyA=keyA GlobalKeyB=keyB was unable to format XML
    <parent><child></parent>
    
## Details
Pretty-printer output can be configured using the `PrettyPrinterFactory` and `PrettyPrinterBuilder` classes.

### Max CDATA node sizes
Configuring

    factory.setMaxTextNodeLength(1024);
    factory.setMaxCDATANodeLength(1024);

yields output like (at a smaller max length)

    <parent>
	    <child><![CDATA[QUJDREVGR0hJSktMTU5PUFFSU1...[TRUNCATED BY 46]]]></child>
    </parent>

for CDATA and correspondingly for text nodes.
### Reformatting of XML within text and/or CDATA nodes
Configuring

    factory.setPrettyPrintCData(true);
    factory.setPrettyPrintTextNodes(true);
    
yields output like

    <parent>
	    <child><![CDATA[
    		<inner>
			    <xml>text</xml>
		    </inner>]]>
	    </child>
    </parent>

for CDATA and correspondingly for text nodes.

### Anonymizing attributes and/or elements
Configuring

    factory.setAnonymizeFilters(new String[]{"/parent/child"}); // multiple paths supported

results in 

    <parent>
	    <child>[*****]</child>
    </parent>

See below for supported XPath syntax.
### Removing subtrees
Configuring

    factory.setPruneFilters(new String[]{"/parent/child"}); // multiple paths supported

results in

    <parent>
    	<child>
		    <!-- [SUBTREE REMOVED] -->
	    </child>
    </parent>

See below for supported XPath syntax.
## Performance
The pretty-printers within this project are considerably faster than [regular](http://stackoverflow.com/questions/139076/how-to-pretty-print-xml-from-java) pretty-printing approaches. This is expected as parser/serializer features have been traded for performance.

For smaller documents (~1K), the regular pretty-printers are heavily affected by setup time, so throughput is approximately 10x times higher. For larger documents (~10K), throughput is approximately 2x-4x times higher. 

Memory use will be approximately two times the XML string size.

For optimal performance, configure your pretty-printer according to your XML Schema. 

## Don't optimize.. yet! 
Try the code on your own XML - the project contains a class which benchmarks a whole directory of XML files at once (recursively). 

	// arguments: iterations warmup-iterations directory includeSubdirectories clean-whitespace
	mvn test-compile exec:java -Dexec.mainClass="com.greenbird.prettyprinter.tools.DirectoryPerformanceTool" -Dexec.args="10000 1000 /path/to/XML true false" -Dexec.classpathScope="test"

The test runs a bunch of the regular pretty printers for comparison.

# But.. is it safe to use?
__Yes, for logging. It is not intended for processing XML which is passed up or down your processing pipe.__ Fully conformant down- or up-stream XML processors should provide XML document error handling, in addition to schema validation and so on. 

In case of invalid XML documents with minute errors, like illegal characters, illegal entities or a forgotten end quote on an attribute, your milage will vary depending on pretty-printer configuration. If you are not using filtering, the worst that can happen is that your document got some extra whitespace inserted in the wrong location.

Ideally, additionally configure down- or up-stream XML processors to log the raw XML upon processing fail. This is especially recommended when using filtering.
## Implementation limitations
The API naturally excludes charset encoding issues, like for XML version 1.0 vs XML version 1.1.

### Invalid XML detection
Many kinds of invalid XML will not be detected. However an unequal number of start and end elements will always be detected.

### Inner XML detection
Text nodes must start and end with the '<' and '>' characters in order to be treated as inner XML. This also applies to CDATA nodes if the robustness parameter is in use. See the code for more concrete details.

### XML attributes
Are not pretty-printed, i.e. kept as-is except when anonymizing.

### XPath expressions
A minor subset of the XPath syntax is supported. However multiple expressions can be used. Namespace prefixes in the XML are simply ignored, only local names at used to determine a match. Expressions are case-sensitive.

#### Anonymize 
Supported syntax:

	/my/xml/element
	/my/xml/@attribute

with support for wildcards; 

	/my/xml/*
	/my/xml/@*

or a simple any-level element search 

	//myElement

which cannot target attributes.

#### Prune
Supported syntax:

	/my/xml/element

with support for wildcards; 

	/my/xml/*

or a simple any-level element search 

	//myElement

# See also
You might also be interested in some related technologies.

### Log4J 2: Asynchronous loggers
[Asynchronous] logging will be considerably faster in heavily multi-threaded applications.

### Aalto XML parser
If you prefer using only full-featured XML processors, an [Aalto]-based StAX pretty-printer is your best option.

# History
- [1.0.0]: Initial release.


[1.0.0]:                https://github.com/greenbird/xml-formatter-core/issues?q=milestone%3Ar1.0.0+is%3Aclosed
[Aalto]:				https://github.com/FasterXML/aalto-xml
[Apache 2.0]:          	http://www.apache.org/licenses/LICENSE-2.0.html
[Asynchronous]: 		http://logging.apache.org/log4j/2.x/manual/async.html
[build-badge]:         	https://build.greenbird.com/job/xml-formatter-core/badge/icon
[build-link]:          	https://build.greenbird.com/job/xml-formatter-core/
[download]:            	http://search.maven.org/#search|ga|1|xml-formatter-core
[greenbird]:           	http://greenbird.com/
[issue-tracker]:       	https://github.com/greenbird/xml-formatter-core/issues
[Maven]:               	http://maven.apache.org/
[projects]:            	http://greenbird.github.io/
[snapshot repository]: 	https://oss.sonatype.org/content/repositories/snapshots/com/greenbird/xml-formatter/xml-formatter-core
