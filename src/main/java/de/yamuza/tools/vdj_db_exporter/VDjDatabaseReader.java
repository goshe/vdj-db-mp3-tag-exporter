package de.yamuza.tools.vdj_db_exporter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class reads the VDj database.
 * 
 * @author rene
 * 
 */
public class VDjDatabaseReader {
	private static final String NODE_NAME_FILE_PATH = "FilePath";
	private static final String NODE_NAME_SONG = "Song";
	private static final String NODE_NAME_FAVORITE_FOLDER = "FavoriteFolder";
	private static final String NODE_ATTR_VERSION = "Version";
	private Boolean debug;
	private String pathToDb;

	public Document readDB() throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new File(pathToDb));
		if (debug) {
			System.out.println(document.getFirstChild().getTextContent());
		}
		return document;
	}

	public Document sortVDjDB(Document vdjdatabase) throws ParserConfigurationException {
		Map<String, Node> nodesToSort = readDocumentToMap(vdjdatabase);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document sortedDB = builder.newDocument();

		// Create sorted Set
		TreeSet<String> sortedSet = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		sortedSet.addAll(nodesToSort.keySet());

		// create output Document
		Element root = sortedDB.createElement(vdjdatabase.getDocumentElement().getNodeName());
		root.setAttribute(NODE_ATTR_VERSION, vdjdatabase.getDocumentElement().getAttribute(NODE_ATTR_VERSION));
		sortedDB.appendChild(root);

		Iterator<String> iter = sortedSet.iterator();
		while (iter.hasNext()) {
			Node nodeToImport = sortedDB.importNode(nodesToSort.get(iter.next()), true);
			root.appendChild(nodeToImport);
		}

		// Import der FavoriteFolder nodes
		NodeList favoriteFolderNodes = vdjdatabase.getElementsByTagName(NODE_NAME_FAVORITE_FOLDER);

		for (int i = 0; i < favoriteFolderNodes.getLength(); i++) {
			Node item = favoriteFolderNodes.item(i);
			Node nodeToImport = sortedDB.importNode(item, true);
			root.appendChild(nodeToImport);
		}

		if (debug) {
			System.out.println("#------------------------# Sorted #----------------------#");
			System.out.println(sortedDB.getFirstChild().getTextContent());
		}

		return sortedDB;
	}

	private Map<String, Node> readDocumentToMap(Document vdjdatabase) {
		NodeList dbNodes = vdjdatabase.getDocumentElement().getChildNodes();
		Integer nodeCount = dbNodes.getLength();
		Map<String, Node> nodesToSort = new HashMap<String, Node>(nodeCount);

		for (int i = nodeCount - 1; i >= 0; i--) {
			Node actual = dbNodes.item(i);
			if (NODE_NAME_SONG.equals(actual.getNodeName())) {
				Node filePathNode = actual.getAttributes().getNamedItem(NODE_NAME_FILE_PATH);
				String path = filePathNode.getNodeValue();
				nodesToSort.put(path, actual);
			}
			System.out.println(actual);
		}

		return nodesToSort;
	}

	public Boolean getDebug() {
		return debug;
	}

	public void setDebug(Boolean debug) {
		this.debug = debug;
	}

	public String getPathToDb() {
		return pathToDb;
	}

	public void setPathToDb(String pathToDb) {
		this.pathToDb = pathToDb;
	}

}
