package com.m4c.profileutil.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;

public class Utils {
	public static String readerToString(Reader input) throws IOException {
		StringWriter writer = new StringWriter();
		IOUtils.copy(input, writer);
		input.close();
		
		return writer.toString();
	}

	public static String streamToString(InputStream input) {
		StringWriter writer = new StringWriter();
		try {
			IOUtils.copy(input, writer);
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		
		return writer.toString();
	}
}
