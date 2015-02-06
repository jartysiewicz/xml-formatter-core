package com.greenbird.prettyprinter.utils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

import com.greenbird.xml.prettyprinter.PrettyPrinter;
import com.greenbird.xml.prettyprinter.plain.AbstractPrettyPrinter;

/**
 * 
 * Utility class for generating pretty-printers.
 * 
 * @author thomas
 *
 */

public class PrettyPrinterConstructor {

	protected static Logger logger = Logger.getLogger(PrettyPrinterConstructor.class.getName());

	private static PrettyPrinterConstructor constructor;
	
	public static PrettyPrinterConstructor getConstructor() {
		return constructor;
	}
	
	static {
		constructor = new PrettyPrinterConstructor();
		
		Object[] trueFalse = new Object[]{Boolean.TRUE, Boolean.FALSE};

		// boolean text, boolean ignoreInvalidTextNodeXML, boolean cdata, boolean ignoreInvalidCDataNodeXML, boolean comment, boolean ignoreInvalidCommentNodeXML, boolean declaration, int maxTextNodeLength, int maxCDATANodeLength
		constructor.addItem("text", trueFalse);
		constructor.addItem("ignoreInvalidTextNodeXML", trueFalse);
		constructor.addItem("cdata", trueFalse);
		constructor.addItem("ignoreInvalidCDataNodeXML", trueFalse);
		constructor.addItem("comment", trueFalse);
		constructor.addItem("ignoreInvalidCommentNodeXML", trueFalse);
		
		constructor.addItem("declaration", trueFalse);
		constructor.addItem("indentationCharacter", new Object[]{new Character(AbstractPrettyPrinter.defaultIndentationCharacter)});
		constructor.addItem("indentationMultiplier", new Object[]{new Integer(AbstractPrettyPrinter.defaultIndentationMultiplier)});
		
		constructor.addItem("maxTextNodeLength", new Object[]{-1, Integer.MAX_VALUE, 26});
		constructor.addItem("maxCDATANodeLength", new Object[]{-1, Integer.MAX_VALUE, 26});

		constructor.addItem("anonymizes", new Object[]{null, new String[]{"/aparent/achild"}, new String[]{"/aparent/achild", "/parent/nosuchchild"}, new String[]{"/parent/child/x/m/l", "/parent/nosuchchild"}, new String[]{"/aparent/achild/@attr"}, new String[]{"/aparent/achild/@*"}, new String[]{"//achild"}});
		constructor.addItem("prunes", new Object[]{null, new String[]{"/aparent/achild"}, new String[]{"/aparent/achild", "/parent/nosuchchild"}, new String[]{"/parent/child/x/m/l", "/parent/nosuchchild"}, new String[]{"/*/*"}, new String[]{"//achild"}});
	}
	
	/**
	 * Returns a list containing one parameter name for each argument accepted
	 * by the given constructor. If the class was compiled with debugging
	 * symbols, the parameter names will match those provided in the Java source
	 * code. Otherwise, a generic "arg" parameter name is generated ("arg0" for
	 * the first argument, "arg1" for the second...).
	 * 
	 * This method relies on the constructor's class loader to locate the
	 * bytecode resource that defined its class.
	 * 
	 * @param constructor
	 * @return 
	 * @throws IOException
	 */
	
	public static List<String> getParameterNames(Constructor<?> constructor) throws IOException {
	    Class<?> declaringClass = constructor.getDeclaringClass();
	    ClassLoader declaringClassLoader = declaringClass.getClassLoader();

	    Type declaringType = Type.getType(declaringClass);
	    String constructorDescriptor = Type.getConstructorDescriptor(constructor);
	    String url = declaringType.getInternalName() + ".class";

	    InputStream classFileInputStream = declaringClassLoader.getResourceAsStream(url);
	    if (classFileInputStream == null) {
	        throw new IllegalArgumentException("The constructor's class loader cannot find the bytecode that defined the constructor's class (URL: " + url + ")");
	    }

	    ClassNode classNode;
	    try {
	        classNode = new ClassNode();
	        ClassReader classReader = new ClassReader(classFileInputStream);
	        classReader.accept(classNode, 0);
	    } finally {
	        classFileInputStream.close();
	    }

	    @SuppressWarnings("unchecked")
	    List<MethodNode> methods = classNode.methods;
	    for (MethodNode method : methods) {
	        if (method.name.equals("<init>") && method.desc.equals(constructorDescriptor)) {
	            Type[] argumentTypes = Type.getArgumentTypes(method.desc);
	            List<String> parameterNames = new ArrayList<String>(argumentTypes.length);

	            @SuppressWarnings("unchecked")
	            List<LocalVariableNode> localVariables = method.localVariables;
	            for (int i = 0; i < argumentTypes.length; i++) {
	                // The first local variable actually represents the "this" object
	                parameterNames.add(localVariables.get(i + 1).name);
	            }

	            return parameterNames;
	        }
	    }

	    return null;
	}

	private List<PrettyPrinterConstructorItem> items = new ArrayList<PrettyPrinterConstructorItem>();
	
	private boolean printConstructionMisses = false;
	private boolean printConstructionHits = false;

	public void addItem(String name, Object[] values) {
		this.items.add(new PrettyPrinterConstructorItem(name, values));
	}
	
	public List<PrettyPrinter> construct(Class cls) throws Exception {
		List<PrettyPrinter> prettyPrinters = new ArrayList<PrettyPrinter>();
		
		Map<String, PrettyPrinterConstructorItem> all = new HashMap<String, PrettyPrinterConstructorItem>();

		for(PrettyPrinterConstructorItem item : items) {
			all.put(item.getName(), item);
		}
		
		for(Constructor constructor : cls.getConstructors()) {
			
			List<String> parameterNames = getParameterNames(constructor);

			List<PrettyPrinterConstructorItem> names = new ArrayList<PrettyPrinterConstructorItem>();

			for(String parameterName : parameterNames) {
				PrettyPrinterConstructorItem name = all.get(parameterName);
				if(name == null) {
					throw new IllegalArgumentException("Unknown variable name " + parameterName + " for " + cls + " constructor " + constructor);
				}
				names.add(name);
			}

			// variable-length for loop
			
			int[] indexes = new int[names.size()];
			
			construction:
			do {
				StringBuffer buffer = new StringBuffer();
				buffer.append(cls.getName() + "(");
				Object[] parameters = new Object[indexes.length];
				for(int i = 0; i < indexes.length; i++) {
					parameters[i] = names.get(i).getValue(indexes[i]);
					
					buffer.append(names.get(i).getName());
					buffer.append("=");
					Object value = names.get(i).getValue(indexes[i]);
					if(value instanceof String[]) {
						buffer.append(Arrays.toString((String[])value));
					} else {
						buffer.append(value);
					}
					buffer.append(", ");
				}
				buffer.setLength(buffer.length() - 2);
				
				buffer.append(")");
				
				try {
					
					PrettyPrinter prettyPrinter = (PrettyPrinter) constructor.newInstance(parameters);
					
					prettyPrinters.add(prettyPrinter);

					// TODO add test here which checks that paramters have been properly set
					
					if(printConstructionHits) {
						System.out.println(buffer);
					}
				} catch(Exception e) {
					// ignore
					if(e.getCause() instanceof IllegalArgumentException) {
						if(printConstructionMisses) {
							logger.finer(buffer + " ignored");
						}
					} else {
						logger.severe("Problem constructing " + buffer);
						throw new RuntimeException("Problem constructing " + buffer, e);
					}
				}

				for(int k = indexes.length - 1; k >= 0; k--) {
					if(indexes[k] + 1 < names.get(k).getCount()) {
						indexes[k]++;
						
						for(int i = k + 1; i < indexes.length; i++) {
							indexes[i] = 0;
						}
						
						continue construction;
					} else {
						// continue to next parameter
					}
				}
				
				break;
			} while(true);
		}
		
		return prettyPrinters;
	}
}