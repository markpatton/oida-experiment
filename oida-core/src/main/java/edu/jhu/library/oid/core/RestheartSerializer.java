package edu.jhu.library.oid.core;

import java.io.IOException;
import java.io.OutputStream;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.stream.JsonGenerator;

public class RestheartSerializer {
	private OidaUris uris;
	private OidaLoader loader;
	
	public RestheartSerializer(OidaUris uris, OidaLoader loader) {
		this.uris = uris;
		this.loader = loader;
	}
	
	public void serialize(Stream<OidaDocument> in, OutputStream out) throws IOException {
		try (JsonGenerator g = Json.createGenerator(out)) {
			g.writeStartArray();
			in.forEach(doc -> serialize_document(doc, g));
			g.writeEnd();
		}
	}

	private void serialize_document(OidaDocument doc, JsonGenerator g) {
		g.writeStartObject();
		g.write("_id", doc.getId());
		g.write("pdf", uris.getPdfUri(doc));
		g.write("iiif_manifest", uris.getIiifManifestUri(doc));
		
		// Mongo uses this to figure out language for text indexes
		g.write("language", "english");
		
		g.writeStartArray("pages");
		try {
			loader.streamPages(doc).forEach(p -> serialize_page(doc, p, g));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		g.writeEnd();
		
		g.writeEnd();
	}

	private void serialize_page(OidaDocument doc, OidaPage p, JsonGenerator g) {
		g.writeStartObject();
		g.write("iiif_canvas", uris.getIiifCanvasUri(doc, p));
		g.write("iiif_image", uris.getIiifImageBaseUri(doc, p));
		g.write("image", uris.getJpegImageUri(doc, p));		
		g.write("width", p.getWidth());		
		g.write("height", p.getHeight());
		g.write("index", p.getIndex());
		g.write("text", p.getText());		
		g.writeEnd();
	}
}
