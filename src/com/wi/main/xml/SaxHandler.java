package com.wi.main.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SaxHandler extends DefaultHandler {
	private HashMap<String, String> map = null;
	private List<HashMap<String, String>> list = null;
	/**
	 * 正在解析的元素的标签
	 */
	private String currentTag = null;
	/**
	 * 正在解析的元素的值
	 */
	private String currentValue = null;
	private String nodeName = null;
	
	public List<HashMap<String, String>> getList(){
		return list;
	}

	public SaxHandler(String nodeName) {
		this.nodeName = nodeName;
	}

	@Override
	public void startDocument() throws SAXException {
		// TODO 当读到一个开始标签的时候，会触发这个方法
		list = new ArrayList<HashMap<String,String>>();
	}

	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		// TODO 当遇到文档的开头的时候，调用这个方法
		if(name.equals(nodeName)){
			map = new HashMap<String, String>();
		}
		if(attributes != null && map != null){
			for(int i = 0; i < attributes.getLength();i++){
				map.put(attributes.getQName(i), attributes.getValue(i));
			}
		}
		currentTag = name;
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		// TODO 这个方法用来处理在XML文件中读到的内容
		if(currentTag != null && map != null){
			currentValue = new String(ch, start, length);
			if(currentValue != null && !currentValue.trim().equals("") && !currentValue.trim().equals("\n")){
				map.put(currentTag, currentValue);
			}
		}
		currentTag=null;
		currentValue=null;
	}

	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		// TODO 在遇到结束标签的时候，调用这个方法
		if(name.equals(nodeName)){
			list.add(map);
			map = null;
		}
		super.endElement(uri, localName, name);
	}
	

}