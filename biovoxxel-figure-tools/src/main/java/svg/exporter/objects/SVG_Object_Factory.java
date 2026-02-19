package svg.exporter.objects;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Vector;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.batik.svggen.SVGSyntax;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.gui.TextRoi;
import ij.plugin.ImageInfo;
import ij.process.ImageConverter;
import ij.CompositeImage;
import svg.exporter.converter.ImageStringConverter;
import svg.utilities.SvgUtilities;



public class SVG_Object_Factory {
	
	private static Document doc;
	private static Element svgRoot;
	private String docWidth;
	private String docHeight;
	private String docViewBox;
	private String pageUnits;
	
	
	public static final String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
	public static final String inkscapeNS = "http://www.inkscape.org/namespaces/inkscape";
	public static final String sodipodiNS= "http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd";
	private String DEFAULT_DOC_NAME = "svg";
	private boolean interpolate = false;
	private boolean smooth = true;
	private double interpolationRange = 3.0;
	private static final double ARROW_LENGTH_CORRECTION_FACTOR = 2.5;
	
	private static boolean lockObject = false;
	
	
	/**
	 * 
	 * @param doc an existing document for further modification. Can be <i>null</i>. Then a new empty Document will be created 
	 * @param format an Enum DocFormat to specify the basic page size. If <i>null</i> a DINA4 page will be used as standard
	 */
	public SVG_Object_Factory(Document doc, DocFormat format) {
		if (doc == null) {
			createNewDocument(DEFAULT_DOC_NAME);
		} else {
			SVG_Object_Factory.doc = doc;
			svgRoot = doc.getDocumentElement();
		}
		
		setPageSize(format);
	}
		
	
	/**
	 * 
	 * @param imp Hand in an ImageJ ImagePlus object to create an SVG document in exactly the image size
	 */
	public SVG_Object_Factory(ImagePlus imp) {
		createNewDocument(DEFAULT_DOC_NAME);
		setImageAsPageSize(imp);
	}
	
		
	public void createNewDocument(String qualifiedName) {
		
		DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
		
		if (qualifiedName == null) {
			SVG_Object_Factory.doc = impl.createDocument(svgNS, DEFAULT_DOC_NAME, null);
		} else {
			SVG_Object_Factory.doc = impl.createDocument(svgNS, qualifiedName, null);
		}

		svgRoot = doc.getDocumentElement();
		
//		System.out.println("doc=" + doc);
//		System.out.println("svgRoot=" + svgRoot);
	}
	
	
	
	
	public void setImageAsPageSize(ImagePlus imp) {
		
		docWidth = "" + imp.getWidth();
		docHeight = "" + imp.getHeight();
		docViewBox = "0 0 " + docWidth + " " + docHeight;
		pageUnits = "px";		
		
		svgRoot.setAttributeNS(svgNS, SVGSyntax.SVG_VIEW_BOX_ATTRIBUTE, docViewBox);
		svgRoot.setAttributeNS(svgNS, SVGSyntax.SVG_WIDTH_ATTRIBUTE, docWidth + pageUnits);
		svgRoot.setAttributeNS(svgNS, SVGSyntax.SVG_HEIGHT_ATTRIBUTE, docHeight + pageUnits);
		svgRoot.setAttribute("xmlns:sodipodi", sodipodiNS);
		svgRoot.setAttribute("xmlns:inkscape", inkscapeNS);
	
	}
	
	
	public void setPageSize(DocFormat format) {
		
		switch (format) {

		case DINA4: 
			docWidth = "210";
			docHeight = "297";
			pageUnits = "mm";
			break;
			
		case DINA0: 
			docWidth = "841";
			docHeight = "1189";
			pageUnits = "mm";
			break;
			
		case US_LETTER: 
			docWidth = "9";
			docHeight = "11";
			pageUnits = "in";
			break;
			
		case DINA1: 
			docWidth = "594";
			docHeight = "841";
			pageUnits = "mm";
			break;
			
		case DINA2: 
			docWidth = "420";
			docHeight = "594";
			pageUnits = "mm";
			break;
			
		case US_LEGAL: 
			docWidth = "9";
			docHeight = "14";
			pageUnits = "in";
			break;
		
		default:
			docWidth = "210";
			docHeight = "297";
			pageUnits = "mm";
			break;
		}
		
		docViewBox = "0 0 " + docWidth + " " + docHeight;
		
		svgRoot.setAttributeNS(svgNS, SVGSyntax.SVG_VIEW_BOX_ATTRIBUTE, docViewBox);
		svgRoot.setAttributeNS(svgNS, SVGSyntax.SVG_WIDTH_ATTRIBUTE, docWidth + pageUnits);
		svgRoot.setAttributeNS(svgNS, SVGSyntax.SVG_HEIGHT_ATTRIBUTE, docHeight + pageUnits);

	}
	
	
	public void setInterpolation(boolean interpolate, double interpolationRange, boolean smooth) {
		this.interpolate = interpolate;
		this.interpolationRange = interpolationRange;
		this.smooth = smooth;	
	}
	
	
	
	
	public static void saveImageAndOverlaysAsSVG(ImagePlus imp, File file, double interpolationInterval, boolean keepComposite, boolean makeInteractive, boolean embedImage, boolean lockCriticalObjects) {

		ImagePlus inputImp = imp.crop("whole-slice");
		inputImp.setTitle(imp.getTitle());
		
		ImageConverter ic = new ImageConverter(inputImp);
		ImageConverter.setDoScaling(true);
		if (imp.isComposite() && !keepComposite) {
			
//			System.out.println("Converting image to RGB");
			ic.convertToRGB();				
			
		} else if (imp.getBitDepth() != 8 && imp.getBitDepth() != 24 && !keepComposite)  {
//			System.out.println("Converting image to 8-bit grayscale");
			ic.convertToGray8();
		} else {
//			System.out.println("Keep composite");
		}
		
				
		SVG_Object_Factory svgDoc = new SVG_Object_Factory(inputImp);

		if (interpolationInterval > 0.0) {
			svgDoc.setInterpolation(true, interpolationInterval, true);
		}
				
		Element group = doc.createElementNS(svgNS, SVGSyntax.SVG_G_TAG);
		svgRoot.appendChild(group);

		group.setAttributeNS(svgNS, SVGSyntax.SVG_ID_ATTRIBUTE, inputImp.getTitle());
		group.setAttributeNS(inkscapeNS, "inkscape:label", "group_" + inputImp.getTitle());
		
		Element image;
		if (imp.isComposite() && keepComposite) {
			image = svgDoc.createComposite(inputImp, embedImage, makeInteractive);
			//image.setAttributeNS(inkscapeNS, "inkscape:label", "#composite_" + inputImp.getTitle());
		} else {
			image = svgDoc.createImage(inputImp, embedImage);
			//image.setAttributeNS(inkscapeNS, "inkscape:label", "#" + inputImp.getTitle());			
		}
				
		group.appendChild(image);
//		System.out.println("Added to document: " + image);
		
		Overlay overlay = inputImp.getOverlay();
				
		Vector<Roi> roiVector = svgDoc.getROIsFromOverlay(overlay);
			
		for (int o = 0; o < roiVector.size(); o++) {
			
			Roi roi = roiVector.get(o);
						
			String roiName = roi.getName();
			
			if(roiName != null && lockCriticalObjects) {
				if (roiName.equalsIgnoreCase("|SB|") || roiName.equalsIgnoreCase("|CB|") || roiName.equalsIgnoreCase("|INSET_FRAME|")) {
					lockObject = true;
				} else {
					lockObject = false;
				}
			} else {
				lockObject = false;
			}
			
			Element object = svgDoc.createObject(roi, roiName, lockObject);

			if (roiName != null && roiName.equalsIgnoreCase("|CLIP_ROI|")) {
				String clipPathId = addClipPathToDocument(object);
//				System.out.println("clipPathId = " + clipPathId);
				group.setAttributeNS(svgNS, SVGSyntax.SVG_CLIP_PATH_ATTRIBUTE, "url(#" + clipPathId + ")");
				
			} else {
//				System.out.println("appending " + object + System.lineSeparator());
				group.appendChild(object);
			}
			
//			System.out.println("Added to document: " + object);
		}
	
		svgDoc.saveSvgFile(SvgUtilities.getSvgFile(inputImp, file));
	}
	
	
	public static String addClipPathToDocument(Element clipObject) {
//		System.out.println("adding clipping path" + System.lineSeparator());
		Element clipDef = doc.createElementNS(svgNS, SVGSyntax.SVG_DEFS_TAG);
		svgRoot.appendChild(clipDef);
		
		Element clipPath = doc.createElementNS(svgNS, SVGSyntax.SVG_CLIP_PATH_TAG);
		clipPath.setAttributeNS(svgNS, SVGSyntax.SVG_ID_ATTRIBUTE, "clipPath" + Math.round(Math.random()*1000));
		
		clipDef.appendChild(clipPath);
		
		clipPath.appendChild(clipObject);
		
		return clipPath.getAttributeNS(svgNS, SVGSyntax.SVG_ID_ATTRIBUTE); //clipPath.getAttributeNS(svgNS, SVGSyntax.SVG_ID_ATTRIBUTE);	
	}

	/**
	 * 
	 * @param imp
	 * @param embed
	 * @return
	 */
	public Element createImage(ImagePlus imp, boolean embed) {
		
		//System.out.println("Creating RGB SVG");
		
		Element image = doc.createElementNS(svgNS, SVGSyntax.SVG_IMAGE_TAG);
		
		image.setAttributeNS(svgNS, SVGSyntax.SVG_ID_ATTRIBUTE, imp.getTitle());
		image.setAttributeNS(inkscapeNS, "inkscape:label", imp.getTitle());
		image.setAttributeNS(svgNS, "x", "0");
		image.setAttributeNS(svgNS, "y", "0");
		image.setAttributeNS(svgNS, "width", "" + imp.getWidth());
		image.setAttributeNS(svgNS, "height", "" + imp.getHeight());
		image.setAttributeNS(svgNS, SVGSyntax.SVG_IMAGE_RENDERING_ATTRIBUTE, "pixelated");

		Element objectDescription = doc.createElementNS(svgNS, SVGSyntax.SVG_DESC_TAG);
		objectDescription.setTextContent(new ImageInfo().getImageInfo(imp));
		image.appendChild(objectDescription);
		
		if (embed) {
			
			String embedCode = new ImageStringConverter().getBase64StringFromImagePlus(imp);
			image.setAttributeNS(svgNS, SVGSyntax.XLINK_HREF_QNAME, "data:image/png;base64," + embedCode);
		} else {
			
			//images linked need to be located in the same folder as the actual SVG file to make the linking work
			image.setAttributeNS(svgNS, SVGSyntax.XLINK_HREF_QNAME, imp.getTitle());
		}
		
		return image;
	}
	
	
	public Element createComposite(ImagePlus composite, boolean embed, boolean makeInteractive) {

//		System.out.println("Creating composite SVG");
		
		Element compositeGroup = doc.createElementNS(svgNS, SVGSyntax.SVG_G_TAG);
		
//		System.out.println("Composite channels = " + composite.getNChannels());
		
		boolean[] activeChannels = ((CompositeImage)composite).getActiveChannels();
		
				
		for (int channel = composite.getNChannels(); channel >= 1; channel--) {
			
			composite.setC(channel);
			String fileName = "C" + channel + "-" + composite.getTitle();
//			System.out.println("Processing composite channel " + fileName);
			ImagePlus currentChannel = new ImagePlus(fileName, composite.getProcessor().duplicate());
						
			ImageConverter ic = new ImageConverter(currentChannel);
			ImageConverter.setDoScaling(true);
			ic.convertToRGB();			
			
			Element image = doc.createElementNS(svgNS, SVGSyntax.SVG_IMAGE_TAG);
			
			image.setAttributeNS(svgNS, SVGSyntax.SVG_ID_ATTRIBUTE, fileName);
			image.setAttributeNS(inkscapeNS, "inkscape:label", fileName);
			image.setAttributeNS(svgNS, "x", "0");
			image.setAttributeNS(svgNS, "y", "0");
			image.setAttributeNS(svgNS, "width", "" + currentChannel.getWidth());
			image.setAttributeNS(svgNS, "height", "" + currentChannel.getHeight());
			image.setAttributeNS(svgNS, SVGSyntax.SVG_IMAGE_RENDERING_ATTRIBUTE, "pixelated");
			
			String styleAttribute = "display:";
			if (activeChannels[channel-1]) {
				styleAttribute += "inline;";
			} else {
				styleAttribute += "none;";
			}
			
			
			if (composite.isInvertedLut()) {
				image.setAttributeNS(svgNS, SVGSyntax.SVG_STYLE_ATTRIBUTE, styleAttribute + "mix-blend-mode:darken;");
			} else {
				image.setAttributeNS(svgNS, SVGSyntax.SVG_STYLE_ATTRIBUTE, styleAttribute + "mix-blend-mode:screen;");
			}
			
			
			Element objectDescription = doc.createElementNS(svgNS, SVGSyntax.SVG_DESC_TAG);
			objectDescription.setTextContent(new ImageInfo().getImageInfo(composite));
			image.appendChild(objectDescription);
			
			if (embed) {
				
				String embedCode = new ImageStringConverter().getBase64StringFromImagePlus(currentChannel);
				image.setAttributeNS(svgNS, SVGSyntax.XLINK_HREF_QNAME, "data:image/png;base64," + embedCode);
			} else {
				
				//images linked need to be located in the same folder as the actual SVG file to make the linking work
				image.setAttributeNS(svgNS, SVGSyntax.XLINK_HREF_QNAME, currentChannel.getTitle());
			}
			
			compositeGroup.appendChild(image);
		}
		
		if (makeInteractive) {
//			System.out.println("Making composite SVG interactive");
			
			long random_number = Math.round(Math.random()*1000);
			
			compositeGroup.setAttributeNS(svgNS, SVGSyntax.SVG_ID_ATTRIBUTE, "image-group-" + random_number);
			
			String blendMode = "screen";
			if (composite.isInvertedLut()) {
				blendMode = "darken";
			}
			
			Element script = createChannelSwapScript(random_number, blendMode);
			compositeGroup.appendChild(script);
		}
		
		
		return compositeGroup;
	}
	
	
	/**
	 * 
	 * Does not work with Roi.COMPOSITE ROIs. Those need to be split first by using getROIsFrom Overlay
	 * 
	 * @param roi
	 * @param description
	 * @param lock
	 * @return Element
	 */
	public Element createObject(Roi roi, String description, boolean lock) {
						
		Element shapeObject = null;	
		
		int roiType = roi.getType();
		
		if (roi instanceof TextRoi) {
			
			shapeObject = createText(roi);
			
		} else {
			
			switch (roiType) {
			
			case Roi.RECTANGLE:
				shapeObject = createRectangle(roi);
				setGeneralObjectAttributes(roi, shapeObject);
				break;
				
			case Roi.POLYGON: case Roi.FREEROI: case Roi.FREELINE: case Roi.POLYLINE: case Roi.ANGLE: case Roi.LINE: case Roi.TRACED_ROI: case Roi.COMPOSITE:
				shapeObject = createPath(roi);
				setGeneralObjectAttributes(roi, shapeObject);
				break;
				
			case Roi.OVAL: case Roi.POINT:
				shapeObject = createEllipse(roi);
				setGeneralObjectAttributes(roi, shapeObject);
				break;

			default:
				break;
			}
		}
		
		
		if (description != null || description !="") {
			
			Element objectDescription = doc.createElementNS(svgNS, SVGSyntax.SVG_DESC_TAG);
			objectDescription.setTextContent(description);
			
			shapeObject.appendChild(objectDescription);
		}
		
		
		if (roi.getName() != null && !roi.getName().equals("")) {
			
			shapeObject.setAttributeNS(inkscapeNS, "inkscape:label", roi.getName());
			
		}
		
		
		if (lock) {
			
			shapeObject.setAttributeNS(sodipodiNS, "sodipodi:insensitive", "true");
		}
		
		
//		System.out.println(shapeObject + " created from " + roi);
		
		return shapeObject;
	}

	
	
	private Vector<Roi> getROIsFromOverlay(Overlay overlay) {
				
		Vector<Roi> roiVector = new Vector<Roi>();
		
		if (overlay != null) {
			for (int r = 0; r < overlay.size(); r++) {
				
				Roi roi = overlay.get(r);
				
				if (roi.getType() == Roi.COMPOSITE) {
					
					Roi[] individualRois = ((ShapeRoi) roi).getRois();
									
					for (int s = 0; s < individualRois.length; s++) {

						individualRois[s].setStrokeWidth(Math.max(roi.getStrokeWidth(), 1.0f));
						individualRois[s].setStrokeColor(roi.getStrokeColor());
						individualRois[s].setFillColor(roi.getFillColor());
						
						roiVector.add(individualRois[s]);
//						System.out.println(individualRois[s] + " added");
					}
					
				} else {
					roiVector.add(roi);
//					System.out.println(roi + " added");
				}
			}
		}
		return roiVector;
	}
	
	
	private void setGeneralObjectAttributes(Roi roi, Element shapeObject) {
		shapeObject.setAttributeNS(null, SVGSyntax.SVG_STROKE_WIDTH_ATTRIBUTE, "" + Math.max(roi.getStrokeWidth(), 1.0f));
		shapeObject.setAttributeNS(null, SVGSyntax.SVG_STROKE_DASHARRAY_ATTRIBUTE, "none");
		
		if (roi.getStrokeColor() != null) {
			shapeObject.setAttributeNS(null, SVGSyntax.SVG_STROKE_ATTRIBUTE, "#" + Integer.toHexString(roi.getStrokeColor().getRGB()).substring(2));
			shapeObject.setAttributeNS(null, SVGSyntax.SVG_STROKE_OPACITY_ATTRIBUTE, "" + (double)roi.getStrokeColor().getAlpha() / 255);
						
		} else {
			shapeObject.setAttributeNS(null, SVGSyntax.SVG_STROKE_ATTRIBUTE, "none");
		}
		
		if (roi.getFillColor() != null) {
			shapeObject.setAttributeNS(null, SVGSyntax.SVG_FILL_ATTRIBUTE, "#" + Integer.toHexString(roi.getFillColor().getRGB()).substring(2));
			shapeObject.setAttributeNS(null, SVGSyntax.SVG_FILL_OPACITY_ATTRIBUTE, "" + (double)roi.getFillColor().getAlpha() / 255);			
		} else {
			shapeObject.setAttributeNS(null, SVGSyntax.SVG_FILL_ATTRIBUTE, "none");
		}
				
	}
	
	
	private Element createPath(Roi pathRoi) {
			
		if (interpolate) {
			pathRoi = SvgUtilities.interpolateRoi(pathRoi, interpolationRange, smooth);
		}

		
		float[] xCoordinates;
		float[] yCoordinates;

		
		if (pathRoi.getType() == Roi.LINE) {
			
			Line lineRoi = (Line)pathRoi;
			
			xCoordinates = new float[] {(float)lineRoi.x1d, (float)lineRoi.x2d};
			yCoordinates = new float[] {(float)lineRoi.y1d, (float)lineRoi.y2d};
			
		} else {
			
			xCoordinates = pathRoi.getFloatPolygon().xpoints;
			yCoordinates = pathRoi.getFloatPolygon().ypoints;
			
		}
		
		if (pathRoi instanceof ij.gui.Arrow) {
			double arrowHeadSize = ((ij.gui.Arrow)pathRoi).getHeadSize();
			
			double lengthCorrection = arrowHeadSize * ARROW_LENGTH_CORRECTION_FACTOR;
			//System.out.println(lengthCorrection);
			
			double arrowAngle = ((ij.gui.Arrow)pathRoi).getAngle();

			float xCorr = Math.abs((float)(lengthCorrection * Math.cos(Math.toRadians(arrowAngle))));
			float yCorr = Math.abs((float)(lengthCorrection * Math.sin(Math.toRadians(arrowAngle))));
						
			if (arrowAngle > 0 && arrowAngle <=90) {
				xCoordinates[1] -= xCorr;
				yCoordinates[1] += yCorr;
			} else if (arrowAngle > 90 && arrowAngle <=180) {
				xCoordinates[1] += xCorr;
				yCoordinates[1] += yCorr;
			} else if (arrowAngle < 0 && arrowAngle >=-90) {
				xCoordinates[1] -= xCorr;
				yCoordinates[1] -= yCorr;
			} else if (arrowAngle < -90 && arrowAngle >-180) {
				xCoordinates[1] += xCorr;
				yCoordinates[1] -= yCorr;
			} 
		}
		
		StringBuilder coordinateString = new StringBuilder();
		
		for (int c = 0; c < xCoordinates.length; c++) {
			coordinateString.append(xCoordinates[c] + "," + yCoordinates[c] + " ");
		}
		
		
		
		Element path = doc.createElementNS(svgNS, SVGSyntax.SVG_PATH_ATTRIBUTE);
	
		if (pathRoi.getType() == Roi.FREELINE || pathRoi.getType() == Roi.POLYLINE || pathRoi instanceof ij.gui.Arrow) {
			
			path.setAttributeNS(null, SVGSyntax.SVG_D_ATTRIBUTE, SVGSyntax.PATH_MOVE + " " + coordinateString);
			
		} else {
			
			path.setAttributeNS(null, SVGSyntax.SVG_D_ATTRIBUTE, SVGSyntax.PATH_MOVE + " " + coordinateString + " " + SVGSyntax.PATH_CLOSE);
			
		}
		
		path.setAttributeNS(svgNS, SVGSyntax.SVG_STROKE_LINECAP_ATTRIBUTE, "butt");
		path.setAttributeNS(svgNS, SVGSyntax.SVG_STROKE_LINEJOIN_ATTRIBUTE, "miter");
		
		if (pathRoi instanceof ij.gui.Arrow) {
			
//			System.out.println("Marker existing=" + SVG_Object_Factory.doc.getDocumentElement().hasAttributeNS(svgNS, SVGSyntax.SVG_MARKER_TAG));
//			
//			if (!SVG_Object_Factory.doc.getDocumentElement().hasAttributeNS(svgNS, SVGSyntax.SVG_MARKER_TAG)) {
//				
//			}
		
			addMarkerToDocument();				
			
			
			if (((ij.gui.Arrow) pathRoi).getDoubleHeaded()) {
//				System.out.println("Path is double headed");
				
				path.setAttributeNS(svgNS, SVGSyntax.SVG_STYLE_ATTRIBUTE, SVGSyntax.CSS_MARKER_START_PROPERTY + ":url(#Arrow2);"
																		+ SVGSyntax.CSS_MARKER_END_PROPERTY + ":url(#Arrow2)");
			} else {
				path.setAttributeNS(svgNS, SVGSyntax.SVG_STYLE_ATTRIBUTE, SVGSyntax.CSS_MARKER_END_PROPERTY + ":url(#Arrow2)");
			}
			
			path.setAttributeNS(inkscapeNS, "inkscape:label", "Arrow");
		}
		
		return path;
	}


	private void addMarkerToDocument() {
//		System.out.println("Adding arrow marker to document");
		
		Element arrowDefs = doc.createElementNS(svgNS, SVGSyntax.SVG_DEFS_TAG);
		
		Element arrowMarker = doc.createElementNS(svgNS, SVGSyntax.SVG_MARKER_TAG);
		arrowMarker.setAttributeNS(svgNS, SVGSyntax.SVG_ID_ATTRIBUTE, "Arrow2");	//important
		arrowMarker.setAttributeNS(svgNS, SVGSyntax.SVG_STYLE_ATTRIBUTE, "overflow:visible");
		arrowMarker.setAttributeNS(svgNS, SVGSyntax.SVG_REF_X_ATTRIBUTE, "3");
		arrowMarker.setAttributeNS(svgNS, SVGSyntax.SVG_REF_Y_ATTRIBUTE, "0");
		arrowMarker.setAttributeNS(svgNS, SVGSyntax.SVG_ORIENT_ATTRIBUTE, "auto-start-reverse");
		arrowMarker.setAttributeNS(svgNS, SVGSyntax.SVG_MARKER_WIDTH_ATTRIBUTE, "7");
		arrowMarker.setAttributeNS(svgNS, SVGSyntax.SVG_MARKER_HEIGHT_ATTRIBUTE, "5");
		arrowMarker.setAttributeNS(svgNS, SVGSyntax.SVG_VIEW_BOX_ATTRIBUTE, "0 0 7.0 5.0");
		arrowMarker.setAttributeNS(inkscapeNS, "inkscape:isstock", "true");
		arrowMarker.setAttributeNS(inkscapeNS, "inkscape:collect", "always");

		Element arrowPath = doc.createElementNS(svgNS, SVGSyntax.SVG_PATH_ATTRIBUTE);
		arrowPath.setAttributeNS(svgNS, SVGSyntax.SVG_TRANSFORM_ATTRIBUTE, "scale(0.5)");
		arrowPath.setAttributeNS(svgNS, SVGSyntax.SVG_D_ATTRIBUTE, "M -2,-4 9,0 -2,4 c 2,-2.33 2,-5.66 0,-8 z");
		arrowPath.setAttributeNS(svgNS, SVGSyntax.SVG_STYLE_ATTRIBUTE, "fill:context-stroke;fill-rule:evenodd;stroke:none");
		
		arrowMarker.appendChild(arrowPath);
		arrowDefs.appendChild(arrowMarker);
		
		SVG_Object_Factory.doc.getDocumentElement().appendChild(arrowDefs);
	}


	private Element createText(Roi roi) {
				
		TextRoi textRoi = ((TextRoi) roi);
		String textAsString = textRoi.getText().trim();

		Font currentFont = textRoi.getCurrentFont();
//		System.out.println("Font = " + currentFont);
		int fontSize = currentFont.getSize();
//		System.out.println(fontSize);
		int fontStyle = currentFont.getStyle();
//		System.out.println(fontStyle);
		String fontFamily = currentFont.getFamily();
		if (fontFamily.equalsIgnoreCase("SansSerif") || fontFamily.equalsIgnoreCase("Dialog")) {
			fontFamily = "Arial";
		} 
//		System.out.println(fontFamily);
		
		
				
		Color textColor = textRoi.getStrokeColor();
		
		double textOpacity = (double) textColor.getAlpha() / 255;	
			
		double roiAngle = textRoi.getAngle();
		
		double x = (double) roi.getBounds().x;
		double y = (double) roi.getBounds().y + roi.getBounds().height * 0.6667;
		
		System.out.println(roi);
		System.out.println(x);
		System.out.println(y);
		
		
		Element text = doc.createElementNS(svgNS, SVGSyntax.SVG_TEXT_TAG);

		text.setAttributeNS(svgNS, SVGSyntax.SVG_X_ATTRIBUTE, "" + x);
		text.setAttributeNS(svgNS, SVGSyntax.SVG_Y_ATTRIBUTE, "" + y);			

		text.setAttributeNS(svgNS, SVGSyntax.SVG_TRANSFORM_ATTRIBUTE, "rotate(" + -roiAngle + " " + roi.getBounds().x + " " + roi.getBounds().y + ")");
		text.setAttributeNS(svgNS, SVGSyntax.CSS_LETTER_SPACING_PROPERTY, "0px");
		text.setAttributeNS(svgNS, SVGSyntax.CSS_WORD_SPACING_PROPERTY, "0px");
		text.setAttributeNS(svgNS, SVGSyntax.CSS_STROKE_WIDTH_PROPERTY, "0px");
		text.setAttributeNS(svgNS, SVGSyntax.CSS_FONT_FAMILY_PROPERTY, fontFamily);
		text.setAttributeNS(svgNS, SVGSyntax.CSS_FONT_SIZE_PROPERTY, "" + fontSize);
		text.setAttributeNS(svgNS, SVGSyntax.CSS_FILL_VALUE, "#" + Integer.toHexString(textColor.getRGB()).substring(2));
		text.setAttributeNS(svgNS, SVGSyntax.CSS_FILL_OPACITY_PROPERTY, "" + textOpacity);
		
		
		switch (fontStyle) {
		case Font.PLAIN:
			
			text.setAttributeNS(svgNS, SVGSyntax.CSS_FONT_STYLE_PROPERTY, SVGSyntax.CSS_NORMAL_VALUE);
			text.setAttributeNS(svgNS, SVGSyntax.CSS_FONT_WEIGHT_PROPERTY, SVGSyntax.CSS_NORMAL_VALUE);
			break;
		case Font.BOLD:
			text.setAttributeNS(svgNS, SVGSyntax.CSS_FONT_STYLE_PROPERTY, SVGSyntax.CSS_NORMAL_VALUE);
			text.setAttributeNS(svgNS, SVGSyntax.CSS_FONT_WEIGHT_PROPERTY, SVGSyntax.CSS_BOLD_VALUE);
			break;
		case Font.ITALIC:
			text.setAttributeNS(svgNS, SVGSyntax.CSS_FONT_STYLE_PROPERTY, SVGSyntax.CSS_ITALIC_VALUE);
			text.setAttributeNS(svgNS, SVGSyntax.CSS_FONT_WEIGHT_PROPERTY, SVGSyntax.CSS_NORMAL_VALUE);
			break;
		case (Font.BOLD + Font.ITALIC):
			text.setAttributeNS(svgNS, SVGSyntax.CSS_FONT_STYLE_PROPERTY, SVGSyntax.CSS_ITALIC_VALUE);
			text.setAttributeNS(svgNS, SVGSyntax.CSS_FONT_WEIGHT_PROPERTY, SVGSyntax.CSS_BOLD_VALUE);
			break;
		
		default:
			text.setAttributeNS(svgNS, SVGSyntax.CSS_FONT_STYLE_PROPERTY, SVGSyntax.CSS_NORMAL_VALUE);
			text.setAttributeNS(svgNS, SVGSyntax.CSS_FONT_WEIGHT_PROPERTY, SVGSyntax.CSS_NORMAL_VALUE);
			break;
		}
		
		Element tspan = doc.createElementNS(svgNS, SVGSyntax.SVG_TSPAN_TAG);
		
		tspan.setAttributeNS(svgNS, SVGSyntax.SVG_X_ATTRIBUTE, "" + x);
		tspan.setAttributeNS(svgNS, SVGSyntax.SVG_Y_ATTRIBUTE, "" + y);
		tspan.setTextContent(textAsString);
		
		text.appendChild(tspan);
		
		return text;
	}


	public Element createRectangle(Roi rectangleRoi) {
		
		Element rectangle = doc.createElementNS(svgNS, SVGSyntax.SVG_RECT_TAG);
		
		rectangle.setAttributeNS(svgNS, "x", "" + rectangleRoi.getBounds().x);
		rectangle.setAttributeNS(svgNS, "y", "" + rectangleRoi.getBounds().y);
		rectangle.setAttributeNS(svgNS, "width", "" + rectangleRoi.getBounds().width);
		rectangle.setAttributeNS(svgNS, "height", "" + rectangleRoi.getBounds().height);
		
		rectangle.setAttributeNS(svgNS, "rx", "" + rectangleRoi.getCornerDiameter());
		rectangle.setAttributeNS(svgNS, "ry", "" + rectangleRoi.getCornerDiameter());
		rectangle.setAttributeNS(svgNS, SVGSyntax.SVG_TRANSFORM_ATTRIBUTE, SVGSyntax.SVG_ROTATE_ATTRIBUTE + "(" + rectangleRoi.getAngle() + ")");
				
		return rectangle;
	}
	

	public Element createEllipse(Roi ellipseRoi) {
		
		Element ellipse = doc.createElementNS(svgNS, SVGSyntax.SVG_ELLIPSE_TAG);
		
		Rectangle bounds = ellipseRoi.getBounds();
		
		if (bounds.width < 3) {
			bounds.width = 3;
		}
		if (bounds.height < 3) {
			bounds.height = 3;
		}
				
		ellipse.setAttributeNS(svgNS, "cx", "" + (bounds.x + bounds.width/2));
		ellipse.setAttributeNS(svgNS, "cy", "" + (bounds.y + bounds.height/2));
		ellipse.setAttributeNS(svgNS, "rx", "" + bounds.width/2);
		ellipse.setAttributeNS(svgNS, "ry", "" + bounds.height/2);		
		
		return ellipse;
	}
	
	
	public void saveSvgFile(File outputFile) {
		SVGGraphics2D graphGen = new SVGGraphics2D(doc);
		
		if (outputFile == null) {
			outputFile = new File(System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "no_name.svg");
		}
		
		try {
			Writer writer = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");
			graphGen.stream(svgRoot, writer);
			writer.close();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (SVGGraphics2DIOException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public Element createChannelSwapScript(long id, String blendMode) {
		Element script = doc.createElementNS(svgNS, SVGSyntax.SVG_SCRIPT_TAG);
		
		script.setAttributeNS(svgNS, SVGSyntax.SVG_ID_ATTRIBUTE, "channelswitch");
		script.setAttributeNS(svgNS, SVGSyntax.SVG_TYPE_ATTRIBUTE, "text/javascript");
		
		Text scriptText = doc.createCDATASection(""
					+ "        // Function to handle image combinations\r\n"
					+ "        function initializeImageCycle() {\r\n"
					+ "            const images = Array.from(document.querySelectorAll('#image-group-" + id + " image'));\r\n"
					+ "            const totalImages = images.length;\r\n"
					+ "            let currentCombinationIndex = -1;\r\n"
					+ "\r\n"
					+ "            // Generate all unique pairs of image indices\r\n"
					+ "            const combinations = [];\r\n"
					+ "            for (let i = 0; i < totalImages; i++) {\r\n"
					+ "                for (let j = i + 1; j < totalImages; j++) {\r\n"
					+ "                    combinations.push([i, j]);\r\n"
					+ "                }\r\n"
					+ "            }\r\n"
					+ "\r\n"
					+ "            // Function to apply the screen blending mode\r\n"
					+ "            function showCombination(index) {\r\n"
					+ "                // Reset all images to normal blend mode and hide them\r\n"
					+ "                images.forEach(img => {\r\n"
					+ "                    img.setAttribute('style', 'display: none; mix-blend-mode: normal; position: absolute;');\r\n"
					+ "                });\r\n"
					+ "\r\n"
					+ "                // If showing all merged (index === -1)\r\n"
					+ "                if (index === -1) {\r\n"
					+ "                    images.forEach(img => {\r\n"
					+ "                        img.setAttribute('style', 'display: block; mix-blend-mode: " + blendMode + "; position: absolute;');\r\n"
					+ "                    });\r\n"
					+ "                    return;\r\n"
					+ "                }\r\n"
					+ "\r\n"
					+ "                // Get the pair for the current index\r\n"
					+ "                const [first, second] = combinations[index];\r\n"
					+ "                images[first].setAttribute('style', 'display: block; mix-blend-mode: " + blendMode + "; position: absolute;');\r\n"
					+ "                images[second].setAttribute('style', 'display: block; mix-blend-mode: " + blendMode + "; position: absolute;');\r\n"
					+ "            }\r\n"
					+ "\r\n"
					+ "            // Event listener to cycle through combinations\r\n"
					+ "            document.getElementById('image-group-" + id + "').addEventListener('click', () => {\r\n"
					+ "                currentCombinationIndex++;\r\n"
					+ "\r\n"
					+ "                if (currentCombinationIndex >= combinations.length) {\r\n"
					+ "                    currentCombinationIndex = -1; // Reset to show all merged\r\n"
					+ "                }\r\n"
					+ "\r\n"
					+ "                showCombination(currentCombinationIndex);\r\n"
					+ "            });\r\n"
					+ "\r\n"
					+ "            // Initialize with all images merged\r\n"
					+ "            showCombination(-1);\r\n"
					+ "        }\r\n"
					+ "\r\n"
					+ "        // Call the function to set up the cycle\r\n"
					+ "        initializeImageCycle();\r\n"
					);
				
		script.appendChild(scriptText);
		
		return script;
	}
	
	
	
	
	public enum DocFormat {
		DINA4, DINA0, US_LETTER, US_LEGAL, DINA1, DINA2
	}

	
	
	public static void main(String[] args) {
		
		ImagePlus testImp = IJ.openImage(System.getProperty("user.home") + "/Desktop/boats.tif");
				
		SVG_Object_Factory.saveImageAndOverlaysAsSVG(testImp, new File(testImp.getOriginalFileInfo().getFilePath()), 3.0, false, true, true, false);
	}
	
	
	
}
