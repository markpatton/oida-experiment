package edu.jhu.library.oid.core;

/**
 *
 */
public class OidaDocument {
	private final String id;
	private final String path;

	public OidaDocument(String id, String label, String path) {
		this.id = id;
		this.path = path;
	}

	public String getId() {
		return id;
	}

	public String getPath() {
		return path;
	}
}
