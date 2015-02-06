package com.greenbird.prettyprinter.utils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.greenbird.xml.prettyprinter.PrettyPrinter;
import com.greenbird.xml.prettyprinter.PrettyPrinterFactory;

public class PrettyPrinterFactoryItem {
	
	/**  */
	private String[] excludes = new String[]{"anonymizeFilterList", "pruneFilterList"};
	
	private String input;
	private String output;
	private PrettyPrinterFactory factory;
	private PrettyPrinterFactory defaultFactory;
	
	private PropertyDescriptor[] factoryProperties;

	public PrettyPrinterFactoryItem(PrettyPrinterFactory factory, String input, String output, PrettyPrinterFactory defaultPrettyPrinterFactory) throws IntrospectionException {
		this.factory = factory;
		this.input = input;
		this.output = output;
		this.defaultFactory = defaultPrettyPrinterFactory;
		
		BeanInfo factoryBeanInfo = Introspector.getBeanInfo(factory.getClass());  
		PropertyDescriptor[] factoryProperties = factoryBeanInfo.getPropertyDescriptors();
		
		List<PropertyDescriptor> activeFactoryProperties = new ArrayList<PropertyDescriptor>();
		properties:
		for(PropertyDescriptor factoryProperty : factoryProperties) {
			for(String exclude : excludes) {
				if(factoryProperty.getName().equals(exclude)) {
					continue properties;
				}
			}
			activeFactoryProperties.add(factoryProperty);
		}
		this.factoryProperties = activeFactoryProperties.toArray(new PropertyDescriptor[activeFactoryProperties.size()]);
	}

	public String getInput() {
		return input;
	}

	public String getOutput() {
		return output;
	}

	public boolean matches(PrettyPrinter prettyPrinter) throws Exception {
		 BeanInfo prettyPrinterBeanInfo = Introspector.getBeanInfo(prettyPrinter.getClass());  
		 PropertyDescriptor[] prettyPrinterProperties = prettyPrinterBeanInfo.getPropertyDescriptors();  
		 
		 // verify integrity of approach, i.e. no incorrectly named methods in pretty printers
		 check: 
		 for (PropertyDescriptor prettyPrinterPD : prettyPrinterProperties) {
			 if(prettyPrinterPD.getDisplayName().equals("class")) {
				 continue;
			 }
			 for (PropertyDescriptor factoryPD : factoryProperties) {
				 
				 if(prettyPrinterPD.getDisplayName().equals(factoryPD.getDisplayName())) {
					 continue check;
				 }
			 }
			 
			 throw new IllegalArgumentException("Unknown descriptor " + prettyPrinterPD.getDisplayName());
		 }
		 
		 for (PropertyDescriptor factoryPD : factoryProperties) {
			 if(factoryPD.getDisplayName().equals("class")) {
				 continue;
			 }

			 PropertyDescriptor corresponding = null;
			 for (PropertyDescriptor prettyPrinterPD : prettyPrinterProperties) {
				 if(factoryPD.getDisplayName().equals(prettyPrinterPD.getDisplayName())) {
					 corresponding = prettyPrinterPD;
					 
					 break;
				 }
			 }

			 Object firstValue;
			 Object secondValue;

			 if(corresponding != null) {
				 // resolve to same value
				 
				 firstValue = corresponding.getReadMethod().invoke(prettyPrinter);
				 secondValue = factoryPD.getReadMethod().invoke(factory);
			 } else {
				 // resolve to default value
				 
				 firstValue = factoryPD.getReadMethod().invoke(defaultFactory);
				 secondValue = factoryPD.getReadMethod().invoke(factory);

			 }
			 
			 if(firstValue == secondValue) {
				 continue;
			 }
			 
			 if(firstValue == null && secondValue != null) {
				 if(secondValue instanceof String[]) {
					 String[] v = (String[])secondValue;
					 
					 if(v.length > 0) {
						 return false;
					 }
				 } else {
					 return false;
				 }
			 } else if(firstValue != null && secondValue == null) {
				 if(firstValue instanceof String[]) {
					 String[] v = (String[])firstValue;
					 
					 if(v.length > 0) {
						 return false;
					 }
				 } else {
					 return false;
				 }
			 } else {
				 if(secondValue instanceof String[]) {
					 String[] dv = (String[])firstValue;
					 String[] v = (String[])secondValue;
					 if(!Arrays.equals(dv, v)) {
						 return false;
					 }
				 } else if(!firstValue.equals(secondValue)) {
					 return false;
				 }
			 }
		 }
		 
        return true;
	}

	public PrettyPrinterFactory getFactory() {
		return factory;
	}
}