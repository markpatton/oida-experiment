package edu.jhu.library.oida.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import edu.jhu.library.oid.core.InsysLoader;
import edu.jhu.library.oid.core.OidaDocument;
import edu.jhu.library.oid.core.OidaPage;
import edu.jhu.library.oid.core.OidaPageAnnotation;

public class InsysLoaderTest {
	private static String TEST_INSYS_DIR = "src/test/resources/insys/";
	private static String TEST_DOC_DIR = "src/test/resources/insys/f/g/h/g/fghg0233/";
	
	@Test
	public void testLoad() throws IOException {
		InsysLoader loader = new InsysLoader(Paths.get(TEST_INSYS_DIR));
		
		OidaDocument doc = loader.loadDocument(Paths.get(TEST_DOC_DIR));
		
		assertEquals("fghg0233", doc.getId());
		assertEquals("f/g/h/g/fghg0233", doc.getPath());

		OidaPage[] pages = loader.streamPages(doc).toArray(OidaPage[]::new);
		
		assertEquals(1, pages.length);
		
		OidaPage page = pages[0];
				
		assertEquals(1275, page.getWidth());
		assertEquals(1651, page.getHeight());
		assertEquals(0, page.getIndex());
		assertEquals("fghg0233-1.jpg", page.getImageName());
		
		assertTrue(page.getText().contains("inconsequential"));
		assertEquals(31, page.getWordAnnotations().size());
		
		OidaPageAnnotation a = page.getWordAnnotations().get(0);
		
		assertEquals("test.txt", a.getText());
		assertEquals(90, Math.round(a.getX() * page.getWidth()));
		assertEquals(122, Math.round(a.getY() * page.getHeight()));
		assertEquals(100, Math.round(a.getWidth() * page.getWidth()));
		assertEquals(13, Math.round(a.getHeight() * page.getHeight()));
	}
}
