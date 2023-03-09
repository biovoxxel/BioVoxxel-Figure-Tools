package svg.importer;

import java.io.File;
import java.io.IOException;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.svggen.SVGSyntax;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ij.ImagePlus;
import svg.exporter.converter.ImageStringConverter;

public class SVGImageReader {
	
	
	public static String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
	private final String TEMP_FILE_PATH = System.getProperty("java.io.tmpdir") + "temp_base64_string_file.txt"; 
	
	
	public SVGImageReader() {
		
	}
	
	public Document readSVG(File file) throws IOException {
		
		System.out.println(TEMP_FILE_PATH);
		Document doc = null;
		try {
			String parser = XMLResourceDescriptor.getXMLParserClassName();
			SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
			doc = factory.createDocument(file.toURI().toString());
			System.out.println(doc);
			
			NodeList elementList = doc.getElementsByTagNameNS(svgNS, SVGSyntax.SVG_IMAGE_TAG);
			System.out.println(elementList);
			System.out.println(elementList.getLength());
			
			String imageTitle = "";
			
			for (int i = 0; i < elementList.getLength(); i++) {
				
				Element element = (Element) elementList.item(i);
				System.out.println("element = " + element);
			
				NamedNodeMap nnm = element.getAttributes();
				
				String imageString = "";
				
				for (int j = 0; j < nnm.getLength(); j++) {
					
					Node currentNode = nnm.item(j);
					String nodeItemName = currentNode.getNodeName();
					String nodeValue = currentNode.getNodeValue();
					
					System.out.println(nodeItemName);
					
					if (nodeItemName.equals("xlink:href") && nodeValue.contains("base64")) {
						imageString = nodeValue.substring(nodeValue.indexOf(",")+1);
						
						imageTitle = file.getName() + "_image_" + (i+1);
					}
				}
				
//				System.out.println(imageString);
				
				ImageStringConverter converter = new ImageStringConverter();
				ImagePlus currentImagePlus = converter.getImagePlusFromBase64String(imageString.substring(imageString.indexOf(",")+1), TEMP_FILE_PATH);
				
				currentImagePlus.setTitle(imageTitle);
				currentImagePlus.show();
			}
			
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return doc;
	}
	
	public void getImagePlusFromDocument(Document doc) {
		
		//TODO: to be implemented
		
	}
	
	
	public static void main(String[] args) {
		
		SVGImageReader sir = new SVGImageReader();
		
		try {
			Document doc = sir.readSVG(new File("C:\\Users\\Admin\\Desktop\\boats.svg"));
			
			sir.getImagePlusFromDocument(doc);
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
	}
	
}
