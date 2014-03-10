package edu.m4c.fcheck;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import dbhelper.db.Column;
import dbhelper.db.Database;
import dbhelper.db.Table;

public class FragmentChecker {
	private Database db;
	
	private Map<String, Set<Long>> keys = new HashMap<>();
	protected Table table;

	private CheckingResult result;

	public FragmentChecker(Database db) {
		this.db = db;
	}

	public CheckingResult check(String name) throws IOException {
		keys.clear();
		
		this.result = new CheckingResult();
		DataStream ds = new DataStream(name);
		
		processXml(ds);
		ds.close();
		
		return result;
	}

	private void processXml(InputStream is) {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			
			DefaultHandler handler = new DefaultHandler() {
				@Override
				public void startElement(String uri, String localName,
						String qName, Attributes attributes)
						throws SAXException {
					
					if (qName.equals("row")) {
						processRow(attributes);
					} else 
					if (qName.equals("TABLE")) {
						table = db.getTable(attributes.getValue(0));
					} else {
						if (qName != "EXPORT") {
							throw new IllegalStateException();
						}
					}
				}
			};
			
			saxParser.parse(new InputSource(isr), handler);
			
		} catch (ParserConfigurationException | SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void processRow(Attributes attributes) {
		for (int i = 0; i < attributes.getLength(); ++i) {
			String columnName = attributes.getLocalName(i);
			String value = attributes.getValue(i);
			
			if (columnName.equalsIgnoreCase(table.getPk())) {
				if (!keys.containsKey(table.getName())) {
					keys.put(table.getName(), new HashSet<Long>());
				}
				
				if (keys.get(table.getName()).contains(Long.valueOf(value))) {
					result.addRepeatedRow();
				}
				
				keys.get(table.getName()).add(Long.valueOf(value));
			} else {
				Column referenced = table.getColumn(columnName).getReferenced();
				if (referenced != null && !value.equals("SPB_NULL_VALUE")) {
					String refTableName = referenced.getTable().getName();
					
					if (!keys.get(refTableName).contains(Long.valueOf(value))) {
						result.addMessage(table.getPk() + "(PK): " + attributes.getValue(table.getPk()) + "; " + 
							columnName + "(FK): " + value + " -> ?");
					}
				}
			}
		}
	}

	private class DataStream extends InputStream {
		private ZipInputStream zip;
		
		public DataStream(String name) throws IOException {
			zip = new ZipInputStream(new FileInputStream(name));
			ZipEntry entry;
			
			while ((entry = zip.getNextEntry()) != null) {
				if (entry.getName().endsWith(".xml")) {
					return;
				}
			}
			
			throw new IllegalStateException();
		}

		@Override
		public int read() throws IOException {
			return zip.read();
		}
		
		@Override
		public void close() throws IOException {
			zip.close();
		}
	}
	
	public class CheckingResult {
		private List<String> messages = new LinkedList<>();
		private int repeated;
		
		public List<String> getMessages() {
			return messages;
		}
		
		public int getRepeated() {
			return repeated;
		}
		
		public void addMessage(String message) {
			messages.add(message);
		}
		
		public void addRepeatedRow() {
			this.repeated++;
		}
	}
}