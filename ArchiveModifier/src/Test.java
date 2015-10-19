import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;


public class Test {

	public static void main(String[] args) throws ZipException, IOException {
		
		File inputArchive = new File("/Users/benjholla/Desktop/test.zip");
		File outputArchive = new File("/Users/benjholla/Desktop/test-modified.zip");
		
		ArchiveModifier archive = new ArchiveModifier(inputArchive);
		
		archive.add("a/b/c/test.txt", new File("/Users/benjholla/Desktop/test2.txt"), true);
		
		archive.save(outputArchive);
	}

}
