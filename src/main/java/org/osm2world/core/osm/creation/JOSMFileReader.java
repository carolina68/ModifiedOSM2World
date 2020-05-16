package org.osm2world.core.osm.creation;

import static java.lang.Double.parseDouble;
import static java.lang.Math.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * variant of {@link StrictOSMFileReader} with the necessary robustness to
 * ignore small standard incompabilities in .osm files written by JOSM.
 */
public class JOSMFileReader extends StrictOSMFileReader {

	public JOSMFileReader(File file) throws IOException,
			ParserConfigurationException, SAXException,
			TransformerException {
		super(createTempOSMFile(file));
	}

	/**
	 * creates a temporary file in the .osm format. This removes some
	 * JOSM-specific attributes present in the original file,
	 * sets fake versions for unversioned elements,
	 * and merges multiple bound elements.
	 *
	 * The generated file should <em>not</em> be used for anything except
	 * feeding it to OSM2World.
	 */
	private static final File createTempOSMFile(File josmFile) throws
			IOException, ParserConfigurationException, SAXException,
			TransformerException {

		/* parse original file */

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(josmFile);

		/* modify DOM */

		NodeList nodes = doc.getDocumentElement().getChildNodes();
		List<Node> nodesToDelete = new ArrayList<Node>();
		List<Element> boundsElements = new ArrayList<Element>();

		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i) instanceof Element) {
				Element element = (Element) nodes.item(i);
				if ("node".equals(element.getNodeName())
						|| "way".equals(element.getNodeName())
						|| "relation".equals(element.getNodeName())) {
					if ("delete".equals(element.getAttribute("action"))) {
						nodesToDelete.add(element);
					} else if (!element.hasAttribute("version")) {
						element.setAttribute("version", "424242");
					}
				} else if ("bounds".equals(element.getNodeName())) {
					boundsElements.add(element);
				}
			}
		}

		if (boundsElements.size() > 1) {

			double minLat = Double.POSITIVE_INFINITY;
			double minLon = Double.POSITIVE_INFINITY;
			double maxLat = Double.NEGATIVE_INFINITY;
			double maxLon = Double.NEGATIVE_INFINITY;

			for (Element bounds : boundsElements) {
				minLat = min(minLat, parseDouble(bounds.getAttribute("minlat")));
				minLon = min(minLon, parseDouble(bounds.getAttribute("minlon")));
				maxLat = max(maxLat, parseDouble(bounds.getAttribute("maxlat")));
				maxLon = max(maxLon, parseDouble(bounds.getAttribute("maxlon")));
			}

			Element firstBounds = boundsElements.remove(0);
			firstBounds.setAttribute("minlat", Double.toString(minLat));
			firstBounds.setAttribute("minlon", Double.toString(minLon));
			firstBounds.setAttribute("maxlat", Double.toString(maxLat));
			firstBounds.setAttribute("maxlon", Double.toString(maxLon));

			nodesToDelete.addAll(boundsElements);

			System.out.println("WARNING: input file contains multiple <bounds>." +
					" This can lead to wrong coastlines and other issues."); //TODO proper logging

		}

		for (Node node : nodesToDelete) {
			doc.getDocumentElement().removeChild(node);
		}

		/* write result */

		File tempFile = File.createTempFile("workaround", ".osm", null);
		tempFile.deleteOnExit();

		try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {

			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();

			StreamResult result = new StreamResult(outputStream);
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);

			return tempFile;

		}

	}

}