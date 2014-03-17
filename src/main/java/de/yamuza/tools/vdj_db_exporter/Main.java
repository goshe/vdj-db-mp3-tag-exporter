/**
 * 
 */
package de.yamuza.tools.vdj_db_exporter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author rene
 * 
 */
public class Main {

	private static final String SPACE = " ";
	private static final String PLUS = "+";
	private static final String NODE_NAME_SONG = "Song";
	private static final String EMPTY_STRING = "";
	private static final String NODE_NAME_FILE_PATH = "FilePath";
	private static final String NODE_NAME_COMMENT = "Comment";
	private static final String SUFFIX_MP3 = ".mp3";
	private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

	private static final int MAX_SPACE_COUNT = 10;

	/**
	 * @param args
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, TransformerException {
		if (args.length >= 1) {
			VDjDatabaseReader databaseReader = new VDjDatabaseReader();
			databaseReader.setDebug(args.length >= 2);

			databaseReader.setPathToDb(args[0]);
			Document vdjdatabase = databaseReader.readDB();
			vdjdatabase = databaseReader.sortVDjDB(vdjdatabase);

			clearComments(vdjdatabase);
			System.out.println("\n writing to " + System.getProperty(JAVA_IO_TMPDIR));
			writeDocumentToFile(vdjdatabase, System.getProperty(JAVA_IO_TMPDIR) + "out.xml");

		} else {
			System.out.println("Wrong number of arguments passed!");
			System.out.println("[1]:\tPath to VDj-Database to read from");
			System.out.println("[2]:\t(optional) D for debug informations on console");
		}
	}

	public static void writeDocumentToFile(Document document, String filename) throws FileNotFoundException, TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "1");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		DOMSource source = new DOMSource(document);
		StreamResult result = new StreamResult(new FileOutputStream(filename));
		transformer.transform(source, result);
	}

	public static void clearComments(Document document) {
		NodeList songNodes = document.getElementsByTagName(NODE_NAME_SONG);
		Integer nodeCount = songNodes.getLength();

		for (int i = nodeCount - 1; i >= 0; i--) {
			Node actual = songNodes.item(i);

			NodeList childNodes = actual.getChildNodes();
			Boolean commentFound = Boolean.FALSE;

			for (int j = 0; j < childNodes.getLength(); j++) {
				Node child = childNodes.item(j);
				if (NODE_NAME_COMMENT.equals(child.getNodeName())) {
					commentFound = Boolean.TRUE;
					String commentText = child.getTextContent();
					if (commentText.contains(PLUS)) {
						// format comment
						child.setTextContent(formatComment(commentText));
					} else {
						// create comment from file-name
						Node filePathNode = actual.getAttributes().getNamedItem(NODE_NAME_FILE_PATH);
						String fileName = filePathNode.getNodeValue();
						child.setTextContent(createCommentContent(fileName));
					}
				}
			}

			if (!commentFound) {
				Node filePathNode = actual.getAttributes().getNamedItem(NODE_NAME_FILE_PATH);
				String fileName = filePathNode.getNodeValue();
				if (fileName.contains(PLUS)) {
					// create comment
					Node comment = document.createElement(NODE_NAME_COMMENT);
					comment.setTextContent(createCommentContent(fileName));
					actual.appendChild(document.createTextNode(SPACE));
					actual.appendChild(comment);
				}
			}
		}
	}

	private static String createCommentContent(String fileName) {
		if (fileName.contains(PLUS)) {
			System.out.println("File\t: " + fileName);
			Integer firstIndex = fileName.indexOf('+');
			String subStr = fileName.substring(firstIndex--);
			System.out.println("Sub\t: " + subStr);
			subStr = subStr.toLowerCase().replace(SUFFIX_MP3, EMPTY_STRING);
			System.out.println("Sub\t: " + subStr);
			formatComment(subStr);

			return subStr;
		} else {
			return EMPTY_STRING;
		}
	}

	public static String formatComment(String comment) {
		int lastPlusIndex = comment.lastIndexOf(PLUS);
		lastPlusIndex++;
		if (lastPlusIndex > 0 && comment.length() > lastPlusIndex) {
			String plusses = comment.substring(0, lastPlusIndex);
			String additionalContent = comment.substring(lastPlusIndex);
			int plusCount = plusses.length();
			int spaceCount = MAX_SPACE_COUNT - plusCount;

			StringBuilder commentBuilder = new StringBuilder(plusses.trim());

			for (int i = 0; i < spaceCount; i++) {
				commentBuilder.append(SPACE);
			}
			commentBuilder.append(additionalContent.trim());

			return commentBuilder.toString();
		} else {
			return comment;
		}
	}

}
