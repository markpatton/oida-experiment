package edu.jhu.library.oid.core;

import java.util.List;

/**
 * Page of a document which has an image.
 */
public class OidaPage {
	private final String image_name;
	private final int width;
	private final int height;
	private final int index;
	private final String text;
	private final List<OidaPageAnnotation> word_annos;

	public OidaPage(String image_name, int index, int width, int height, String text, List<OidaPageAnnotation> word_annos) {
		this.image_name = image_name;
		this.index = index;
		this.width = width;
		this.height = height;
		this.text = text;
		this.word_annos = word_annos; 
	}
	
	public int getIndex() {
		return index;
	}
	
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public String getImageName() {
		return image_name;
	}
	
	public String getText() {
		return text;
	}
	
	public List<OidaPageAnnotation> getWordAnnotations() {
		return word_annos;
	}
}
