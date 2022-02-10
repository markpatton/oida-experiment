package edu.jhu.library.oid.core;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class OidaUris {
	private final String base_data_url;
	private final String base_image_url;

	/**
	 * @param base_data_url  Base URL to access static data. Should probably abe absolute.
	 * @param base_image_url Base URL to IIIF image server. Must be absolute.
	 */
	public OidaUris(String base_data_url, String base_image_url) {
		this.base_data_url = base_data_url.endsWith("/") ? base_data_url : base_data_url + "/";
		this.base_image_url = base_image_url.endsWith("/") ? base_image_url : base_image_url + "/";
	}

	private String get_base_url(OidaDocument doc) {
		return base_data_url + doc.getPath() + "/";
	}

	public String getIiifManifestUri(OidaDocument doc) {
		String s = URLEncoder.encode(getIiifManifestName(doc), StandardCharsets.UTF_8);
		return get_base_url(doc) + s;
	}

	public String getIiifManifestName(OidaDocument doc) {
		return doc.getId() + "-manifest.json";
	}

	public String getIiifCanvasUri(OidaDocument doc, OidaPage page) {
		return get_base_url(doc) + "canvas/" + page.getIndex();
	}

	public String getIiifPaintingAnnotationPageUri(OidaDocument doc, OidaPage page) {
		return get_base_url(doc) + "painting/" + page.getIndex();
	}

	public String getIiifTextAnnotationPageUri(OidaDocument doc, OidaPage page) {
		String s = URLEncoder.encode(getIiifTextAnnotationPageName(doc, page), StandardCharsets.UTF_8);
		return get_base_url(doc) + s;
	}

	public String getIiifTextAnnotationPageName(OidaDocument doc, OidaPage page) {
		return doc.getId() + "-page-" + page.getIndex() + ".json";
	}

	public String getIiifAnnotationImageUri(OidaDocument doc, OidaPage page) {
		return get_base_url(doc) + "image/" + page.getIndex();
	}

	public String getIiifImageBaseUri(OidaDocument doc, OidaPage page) {
		String s = URLEncoder.encode(doc.getPath() + "/" + page.getImageName(), StandardCharsets.UTF_8);
		return base_image_url + s;
	}

	public String getJpegImageUri(OidaDocument doc, OidaPage page) {
		return getIiifImageBaseUri(doc, page) + "/full/max/0/default.jpg";
	}

	public String getPdfUri(OidaDocument doc) {
		String s = URLEncoder.encode(doc.getId() + ".pdf", StandardCharsets.UTF_8);
		return get_base_url(doc) + s;
	}

	// TODO Needs more thought
	public String getIiifAnnotationTextUri(OidaDocument doc, OidaPage page, OidaPageAnnotation a) {
		return get_base_url(doc) + "anno/" + page.getIndex() + "/" + a.hashCode();
	}

	public String getIiifCanvasTarget(OidaDocument doc, OidaPage p, OidaPageAnnotation a) {
		int x = (int) Math.round(a.getX() * p.getWidth());
		int y = (int) Math.round(a.getY() * p.getHeight());
		int width = (int) Math.round(a.getWidth() * p.getWidth());
		int height = (int) Math.round(a.getHeight() * p.getHeight());

		return getIiifCanvasUri(doc, p) + "#xywh=" + x + "," + y + "," + width + "," + height;
	}

	// TODO Probably have a separate base url for search
	public String getIiifSearchService(OidaDocument doc) {
		return get_base_url(doc) + "search/";
	}

	public String getJpegThumbUri(OidaDocument doc, OidaPage page) {
		return getIiifImageBaseUri(doc, page) + "/full/128,/0/default.jpg";		
	}
}
