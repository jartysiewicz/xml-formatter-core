package com.greenbird.prettyprinter.benchmark.utils;

import java.util.List;

import javax.xml.xpath.XPathException;

import org.w3c.dom.Document;

public class XPathFilterFilterChain implements XPathFilter {

	private XPathFilter[] filters;

	public XPathFilterFilterChain(XPathFilter[] filters) {
		this.filters = filters;
	}

	public XPathFilterFilterChain(List<XPathFilter> filters) {
		this(filters.toArray(new XPathFilter[filters.size()]));
	}

	@Override
	public void filter(Document document) throws XPathException {
		for(XPathFilter filter : filters) {
			filter.filter(document);
		}
		
	}
}
