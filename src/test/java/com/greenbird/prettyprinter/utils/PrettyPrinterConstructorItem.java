package com.greenbird.prettyprinter.utils;

import java.util.Arrays;

public class PrettyPrinterConstructorItem {
	
	private String name;
	private Object[] values;

	public PrettyPrinterConstructorItem(String name, Object[] values) {
		this.name = name;
		this.values = values;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object[] getValues() {
		return values;
	}

	public void setValues(Object[] values) {
		this.values = values;
	}

	public int getCount() {
		return values.length;
	}

	public Object getValue(int i) {
		return values[i];
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + Arrays.hashCode(values);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PrettyPrinterConstructorItem other = (PrettyPrinterConstructorItem) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (!Arrays.equals(values, other.values))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ParameterItem [name=" + name + ", values=" + Arrays.toString(values) + "]";
	}
	
	
}