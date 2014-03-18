package monitor;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.m4c.monitor.action.GlueAction;

public class ProcessorTest {

	@Test
	public void test() throws IOException {
		String path = "D:\\opt\\logProcessor\\logs.zip";
		String dirPath = "D:\\Temp\\monitor829894736725683312";
		
//		Path dir = new UnzipAction().unzip(new File(path).toPath());
		
		File dir = new File(dirPath);
		new GlueAction().glueLogs(dir);
	}
}
