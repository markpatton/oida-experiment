package edu.jhu.library.oida.cli;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import edu.jhu.library.oid.core.Iiif3Serializer;
import edu.jhu.library.oid.core.InsysLoader;
import edu.jhu.library.oid.core.OidaDocument;
import edu.jhu.library.oid.core.OidaLoader;
import edu.jhu.library.oid.core.OidaUris;
import edu.jhu.library.oid.core.RestheartSerializer;

/**
 *
 */
public class Main {
	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.err.println("Usage: write-iiif|serialize-restheart DIR");
			System.exit(1);
		}

		String base_data_url = System.getProperty("oida.base_data_url", "http://localhost:8080/data/");
		String base_image_url = System.getProperty("oida.base_image_url", "http://localhost:8182/iiif/3/");
		
		OidaUris uris = new OidaUris(base_data_url, base_image_url);

		switch (args[0]) {
		case "write-iiif": {
			Path dir = Paths.get(args[1]);
			OidaLoader loader = new InsysLoader(dir);

			Files.walk(dir).filter(p -> p.getFileName().toString().endsWith(".pdf")).forEach(p -> {
				try {
					Path doc_dir = p.getParent();
					
					System.out.println("Processing: " + doc_dir);

					OidaDocument doc = loader.loadDocument(doc_dir);

					try (OutputStream manifest_os = Files.newOutputStream(doc_dir.resolve(uris.getIiifManifestName(doc)))) {
						new Iiif3Serializer(uris, loader).serialize(doc, manifest_os, (d_, p_) -> {
							try {
								return Files.newOutputStream(doc_dir.resolve(uris.getIiifTextAnnotationPageName(doc, p_)));
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						});
					}
				} catch (IOException e) {
					System.err.println("Failure: " + dir);
					e.printStackTrace();
				}
			});

			break;
		}

		case "serialize-restheart": {
			Path dir = Paths.get(args[1]);
			OidaLoader loader = new InsysLoader(dir);

			Stream<OidaDocument> docs = Files.walk(dir).filter(p -> p.getFileName().toString().endsWith(".pdf")).map(p -> {
				try {
					return loader.loadDocument(p.getParent());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
			
			new RestheartSerializer(uris, loader).serialize(docs, System.out);

			break;
		}

		default:
			System.err.printf("Unknown command: '%s'. Expected 'write-iiif' or 'serialize-restheart'.%n", args[0]);
			break;
		}

	}
}
