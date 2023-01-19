package svg.reader;

import java.io.File;
import java.io.IOException;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;

public class SVGImageReader {
	
	
	public static String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
	
	public SVGImageReader() {
		
	}
	
	public Document readSVG(File file) throws IOException {
		
		Document doc = null;
		try {
			String parser = XMLResourceDescriptor.getXMLParserClassName();
			SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
			doc = factory.createDocument(file.toURI().toString());
			
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
