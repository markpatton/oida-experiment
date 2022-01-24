package edu.jhu.library.oida.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.junit.jupiter.api.Test;

import edu.jhu.library.oid.core.InsysLoader;
import edu.jhu.library.oid.core.OidaDocument;
import edu.jhu.library.oid.core.OidaLoader;
import edu.jhu.library.oid.core.OidaUris;
import edu.jhu.library.oid.core.RestheartSerializer;

public class RestheartSerializerTest {
	private static String TEST_INSYS_DIR = "src/test/resources/insys/";
	private static String TEST_DOC_DIR = "src/test/resources/insys/f/g/h/g/fghg0233/";
	
	@Test
	public void testSerialization() throws IOException {
		OidaLoader loader = new InsysLoader(Paths.get(TEST_INSYS_DIR));
		OidaDocument doc = loader.loadDocument(Paths.get(TEST_DOC_DIR));
		OidaUris uris = new OidaUris();
		
		RestheartSerializer serializer = new RestheartSerializer(uris, loader);
		
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		serializer.serialize(Stream.of(doc), bos);
		String json = bos.toString(StandardCharsets.UTF_8);

		JsonArray result_array = Json.createReader(new StringReader(json)).readArray();
			
		assertEquals(1, result_array.size());
		
		JsonObject result = result_array.getJsonObject(0);
		
		assertEquals(result.getString("_id"), "fghg0233");
	}

}
