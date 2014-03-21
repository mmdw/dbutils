package monitor;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.m4c.monitor.action.Util;

public class ProcessorTest {

	@Test
	public void test() throws IOException {
		String dirPath = "D:\\Temp\\aaaa";

		Util.testContent(new File("C:\\Users\\user\\Desktop\\testDir\\logs.rar"));
		
	}
}
