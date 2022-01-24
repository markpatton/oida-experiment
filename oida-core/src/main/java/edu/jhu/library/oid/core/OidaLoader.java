package edu.jhu.library.oid.core;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface OidaLoader {
	OidaDocument loadDocument(Path path) throws IOException;
	
	Stream<OidaPage> streamPages(OidaDocument doc) throws IOException;
	
	String getText(OidaDocument doc) throws IOException;
}
