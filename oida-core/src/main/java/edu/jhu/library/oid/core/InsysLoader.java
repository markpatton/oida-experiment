package edu.jhu.library.oid.core;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
 * Insys document dump file hierarchy looks like 
 * data/k/n/v/j/knvj0233/
 *   knvj0233.pdf
 *   knvj0233.ocr
 *   knvj0233-ocrlayout.xml
 *   knvj0233-?.jpg or knvj0233-??.jpg 
 */

public class InsysLoader implements OidaLoader {
	private final Path base_path;
	
	/**
	 * Documents are treated as relative to base_path.
	 *  
	 * @param base_path
	 */
	public InsysLoader(Path base_path) {
		this.base_path = base_path;
	}
	
	@Override
	public OidaDocument loadDocument(Path dir) {
		String id = dir.getFileName().toString();

		return new OidaDocument(id, id, base_path.relativize(dir).toString());
	}

	@Override
	public Stream<OidaPage> streamPages(OidaDocument doc) throws IOException {
		LinkedList<List<OidaPageAnnotation>> page_word_annos = new LinkedList<>();

		try {
			Path path = base_path.resolve(doc.getPath()).resolve(doc.getId() + "-ocrlayout.xml");
			if (Files.exists(path)) {
				SAXParserFactory.newInstance().newSAXParser().parse(path.toFile(),
						new OcrLayoutXmlHandler(page_word_annos));
			}
		} catch (ParserConfigurationException e) {
			throw new IOException(e);
		} catch (SAXException e) {
			throw new IOException(e);
		}

		AtomicInteger index = new AtomicInteger(0);
		return Files.list(base_path.resolve(doc.getPath()))
				.filter(p -> p.getFileName().toString().startsWith(doc.getId()) && p.toString().endsWith(".jpg"))
				.map(p -> create_page(p, index.getAndIncrement(),
						page_word_annos.isEmpty() ? null : page_word_annos.removeFirst()));
	}

	/**
	 * Parse out annotations by page.
	 * 
	 * XML structure looks like this:
	 * 
	 * <pre>
	 * page width="612.000000" height="792.000000"
	 *   flow
	 *     block xMin="43.000000" yMin="58.497000" xMax="91.000000" yMax="64.817000"
	 *       line xMin="43.000000" yMin="58.497000" xMax="91.000000" yMax="64.817000"
	 *         word xMin="43.000000" yMin="58.497000" xMax="91.000000" yMax="64.817000"
	 *           WORD TEXT
	 * </pre>
	 *
	 */
	private static class OcrLayoutXmlHandler extends DefaultHandler {
		private final LinkedList<List<OidaPageAnnotation>> page_word_annos;
		private final StringBuilder text;
		private double min_x, min_y, max_x, max_y;
		private double page_width, page_height;

		/**
		 * @param word_annos_list Used to return per page word annotation lists
		 */
		public OcrLayoutXmlHandler(LinkedList<List<OidaPageAnnotation>> page_word_annos) {
			this.page_word_annos = page_word_annos;
			this.text = new StringBuilder();
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			text.append(ch, start, length);
		}

		// Numbers are floating point, but we want integers.
		private double get_double(Attributes attributes, String name) throws SAXException {
			String s = attributes.getValue(name);

			if (s == null) {
				throw new SAXException("Required attribute missing: " + name);
			}

			return Double.parseDouble(s);
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			if (qName.equals("page")) {
				page_word_annos.add(new ArrayList<>());
				page_width = get_double(attributes, "width");
				page_height = get_double(attributes, "height");				
			} else if (qName.equals("word")) {
				min_x = get_double(attributes, "xMin");
				min_y = get_double(attributes, "yMin");
				max_x = get_double(attributes, "xMax");
				max_y = get_double(attributes, "yMax");
			}

			text.setLength(0);
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (qName.equals("word")) {
				double x = min_x / page_width;
				double y = min_y / page_height;
				double width = (max_x - min_x) / page_width;
				double height = (max_y - min_y) / page_height;
				
				page_word_annos.getLast().add(
						new OidaPageAnnotation(text.toString().trim(), x, y, width, height));
			}
		}
	}

	@Override
	public String getText(OidaDocument doc) {
		Path file = base_path.resolve(doc.getPath()).resolve(doc.getId() + ".ocr");

		if (!Files.exists(file)) {
			return null;
		}

		try {
			return Files.readString(file);
		} catch (IOException e) {
			throw new RuntimeException("Failed to get ocr text for " + doc.getId());
		}
	}

	private static int[] getImageDimensions(Path file) throws IOException {
		try (ImageInputStream in = ImageIO.createImageInputStream(file.toFile())) {
			final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
			if (readers.hasNext()) {
				ImageReader reader = readers.next();
				try {
					reader.setInput(in);
					return new int[] { reader.getWidth(0), reader.getHeight(0) };
				} finally {
					reader.dispose();
				}
			}
		}

		return null;
	}

	private OidaPage create_page(Path image, int index, List<OidaPageAnnotation> word_annos) {
		try {
			int width = -1;
			int height = -1;

			int[] dim = getImageDimensions(image);

			if (dim != null) {
				width = dim[0];
				height = dim[1];
			}

			if (word_annos == null) {
				word_annos = new ArrayList<>();
			}

			StringBuilder text = new StringBuilder();
			word_annos.forEach(a -> text.append(a.getText() + ' '));

			return new OidaPage(image.getFileName().toString(), index, width, height, text.toString(), word_annos);
		} catch (MalformedURLException e) {
			throw new RuntimeException("Failed to create image URL for " + image);
		} catch (IOException e) {
			throw new RuntimeException("Failed to get dimensions for " + image);
		}
	}
}
