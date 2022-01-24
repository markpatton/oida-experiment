package edu.jhu.library.oida.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.jupiter.api.Test;

import edu.jhu.library.oid.core.Iiif3Serializer;
import edu.jhu.library.oid.core.InsysLoader;
import edu.jhu.library.oid.core.OidaDocument;
import edu.jhu.library.oid.core.OidaLoader;
import edu.jhu.library.oid.core.OidaPage;
import edu.jhu.library.oid.core.OidaUris;

public class Iiif3SerializerTest {
	private static String TEST_INSYS_DIR = "src/test/resources/insys/";
	private static String TEST_DOC_DIR = "src/test/resources/insys/f/g/h/g/fghg0233/";


	@Test
	public void testSerialization() throws IOException {
		OidaLoader loader = new InsysLoader(Paths.get(TEST_INSYS_DIR));
		
		OidaDocument doc = loader.loadDocument(Paths.get(TEST_DOC_DIR));
		OidaUris uris = new OidaUris();
		
		Iiif3Serializer serializer = new Iiif3Serializer(uris, loader);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ByteArrayOutputStream anno_bos = new ByteArrayOutputStream();
		
		serializer.serialize(doc, bos, (d, p) -> anno_bos);
		String json = bos.toString(StandardCharsets.UTF_8);

		JsonObject result = Json.createReader(new StringReader(json)).readObject();
			
		assertNotNull(result);
		
		assertEquals(result.getString("id"), uris.getIiifManifestUri(doc));
		assertEquals(result.getString("type"), "Manifest");
		
		String anno_json = anno_bos.toString(StandardCharsets.UTF_8);
		JsonObject anno_result = Json.createReader(new StringReader(anno_json)).readObject();

		assertEquals(anno_result.getString("id"), uris.getIiifTextAnnotationPageUri(doc, new OidaPage("", 0, 0, 0, "", null)));
		assertEquals(anno_result.getString("type"), "AnnotationPage");
	}

}
