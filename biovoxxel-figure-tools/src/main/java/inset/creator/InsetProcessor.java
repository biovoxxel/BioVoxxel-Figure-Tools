package inset.creator;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import javax.swing.JOptionPane;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Macro;
import ij.WindowManager;
import ij.gui.Arrow;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.RotatedRectRoi;
import ij.gui.ShapeRoi;
import ij.gui.TextRoi;
import ij.gui.Toolbar;
import ij.measure.Calibration;
import ij.plugin.CanvasResizer;
import ij.plugin.RoiRotator;
import ij.plugin.RoiScaler;
import ij.process.FloatPolygon;
import ij.process.ImageProcessor;

public class InsetProcessor {
	
	
	private static ImagePlus imagePlus;
	private static ImageProcessor insetImageProcessor;
	private static ImagePlus insetImagePlus;
	private static Roi frameRoi;
	private static int formerAngle = 0;
	private static int frameWidth;
	private static Color frameColor;
		
	
	public InsetProcessor() {
		boolean startedFromMacro = runFromMacro();
		System.out.println("started from macro = " + startedFromMacro);
	}
	
	public static boolean runFromMacro() {
		String macroParams = Macro.getOptions();
		if (macroParams != null) {
			System.out.println(macroParams);
			
			return true;
		}
		
		return false;
	}

	
	
	public static void createInset() {
		
		doSetup();
		
		if (imagePlus != null) {
			int channel = imagePlus.getC();
			int slice = imagePlus.getZ();
			int frame = imagePlus.getT();
			
			ImagePlus scaledImagePlus = null;

			Overlay finalOverlay;
			
			if (Inset_Creator.roiAngle != 0 && !Inset_Creator.aspectRatio.contains("Circle")) {
				
				ImagePlus duplicatedInset = imagePlus.crop();
				//duplicatedInset.duplicate().show(); //test
						
				straightenRotatedRect(imagePlus, frameRoi, duplicatedInset);
				
				finalOverlay = duplicatedInset.getOverlay().duplicate();
				
				scaledImagePlus = duplicatedInset.resize(duplicatedInset.getWidth() * Inset_Creator.magnification, duplicatedInset.getHeight() * Inset_Creator.magnification, 1, "none");
	
				
			} else {
				
				finalOverlay = imagePlus.getOverlay().duplicate().crop(frameRoi.getBounds());
				
				scaledImagePlus = imagePlus.resize(frameRoi.getBounds().width * Inset_Creator.magnification, frameRoi.getBounds().height * Inset_Creator.magnification, 1, "none");
			}
			//scaledImagePlus.getOverlay().clear();
			//scaledImagePlus.updateAndDraw();
				
			
			//add inset ROI to original image
			if (Inset_Creator.addFrame) {
				if (Inset_Creator.aspectRatio.contains("Circle")) {
					frameRoi = new OvalRoi(frameRoi.getBounds().x, frameRoi.getBounds().y, frameRoi.getBounds().width, frameRoi.getBounds().height);
				} else if (Inset_Creator.roiAngle == 0) {
					frameRoi = new Roi(frameRoi.getBounds().x, frameRoi.getBounds().y, frameRoi.getBounds().width, frameRoi.getBounds().height);
				}
				frameRoi.setStrokeWidth(Inset_Creator.frameWidth);
				frameRoi.setStrokeColor(frameColor);
				frameRoi.setName("|INSET_FRAME|");
				
				Overlay originalOverlay = imagePlus.getOverlay();
				if (originalOverlay == null) {
					originalOverlay = new Overlay();
					imagePlus.setOverlay(originalOverlay);
				}
				imagePlus.getOverlay().add(frameRoi);
				imagePlus.killRoi();
				
			}
			
			
			//add inset Roi to inset overlay
			if (Inset_Creator.addFrameToInset) {
				Roi insetRoi = null;
				if (Inset_Creator.aspectRatio.contains("Circle")) {
					insetRoi = new OvalRoi(frameWidth/2, frameWidth/2, scaledImagePlus.getWidth()-frameWidth, scaledImagePlus.getHeight()-frameWidth);
				} else {
					insetRoi = new Roi(frameWidth/2, frameWidth/2, scaledImagePlus.getWidth()-frameWidth, scaledImagePlus.getHeight()-frameWidth);
				}
			
				insetRoi.setStrokeWidth(Inset_Creator.frameWidth);
				insetRoi.setStrokeColor(frameColor);
				insetRoi.setName("|INSET_FRAME|");
				
				finalOverlay.add(insetRoi);

				scaledImagePlus.killRoi();
			}

			//add the final overlay in a upscaled manner to the inset image
			scaledImagePlus.setOverlay(translate(finalOverlay));
			
			//set a clipping Roi to cut off ROIs outside the image area and image areas outside a circular inset ROI
			Roi clippingRoi = null;
			if (Inset_Creator.aspectRatio.contains("Circle")) {
				clippingRoi = new OvalRoi(0, 0, scaledImagePlus.getWidth(), scaledImagePlus.getHeight());
			} else {
				clippingRoi = new Roi(0, 0, scaledImagePlus.getWidth(), scaledImagePlus.getHeight());
			}
			clippingRoi.setStrokeWidth(Inset_Creator.frameWidth);
			clippingRoi.setStrokeColor(frameColor);
			clippingRoi.setName("|CLIP_ROI|");
			scaledImagePlus.getOverlay().add(clippingRoi);	//add twice to have a clipping Roi in Inkscape available
			
			
			String insetTitle = WindowManager.getUniqueName("Inset_" + imagePlus.getTitle());
			scaledImagePlus.setTitle(insetTitle);
			
			Calibration imageCalibration = imagePlus.getCalibration();
			Calibration insetCalibration = imageCalibration.copy();
			
			insetCalibration.pixelWidth = imageCalibration.pixelWidth / Inset_Creator.magnification;
			insetCalibration.pixelHeight = imageCalibration.pixelHeight / Inset_Creator.magnification;
			
			
						
			scaledImagePlus.setCalibration(insetCalibration);
			
			scaledImagePlus.show();

			scaledImagePlus.setC(channel);
			scaledImagePlus.setZ(slice);
			scaledImagePlus.setT(frame);
			
			
						
		} else {
			JOptionPane.showMessageDialog(null, "No open image detected");
		}
	}


	private static void doSetup() {
		imagePlus = Inset_Creator.inputImage;
		System.out.println("Original image: " + imagePlus.getTitle());
		
		frameRoi = imagePlus.getRoi();
		System.out.println("Selected ROI: " + frameRoi);
		
		frameWidth = Inset_Creator.frameWidth;
		
		if (imagePlus != null) {
			
			imagePlus.setRoi(frameRoi);
			insetImagePlus = imagePlus.crop();
			//IMAGE_PLUS.killRoi();
					
			insetImageProcessor = insetImagePlus.getProcessor();
			System.out.println("INSET_IMAGE_PROCESSOR: " + insetImageProcessor);
			insetImageProcessor.setInterpolationMethod(ImageProcessor.NONE);
		} else {
			frameRoi = null;
			insetImageProcessor = null;
		}
		

		System.out.println("MAGNIFICATION = " + Inset_Creator.magnification);
		
		System.out.println("ADD_FRAME = " + Inset_Creator.addFrame);
		
		System.out.println("ADD_FRAME TO INSET = " + Inset_Creator.addFrameToInset);
		
		System.out.println("FRAME_WIDTH = " + Inset_Creator.frameWidth);
		
		frameColor = new Color(Inset_Creator.frameColor.getRed(), Inset_Creator.frameColor.getGreen(), Inset_Creator.frameColor.getBlue());
		System.out.println("FRAME_COLOR = " + frameColor);

	}
	
	
	public static void magnificationChanged() {
		int magnification = Inset_Creator.magnification;
		ImagePlus inputImagePlus = Inset_Creator.inputImage;
		Roi currentRoi = inputImagePlus.getRoi();
		
		double x = 0;
		double y = 0;
		int width = 0;
		int height = 0;

		if (currentRoi != null) {
			width = (int)(inputImagePlus.getWidth() / magnification);
			height = (int)(inputImagePlus.getHeight() / magnification);
			
			x = currentRoi.getBounds().getCenterX() - width / 2;
			y = currentRoi.getBounds().getCenterY() - height / 2;
		} else {
			x = 0;
			y = 0;
			width = (int)(inputImagePlus.getWidth() / magnification);
			height = (int)(inputImagePlus.getHeight() / magnification);
		}
		
		
		switch (Inset_Creator.aspectRatio) {
		case "Image":
			//keep upper settings
			break;
		case "Square_width":
			height = width;
			break;
		case "Square_height":
			width = height;
			break;
		case "Circle_width":
			height = width;
			break;
		case "Circle_height":
			width = height;
			break;
		default:
			break;
		}
		
		if (Inset_Creator.aspectRatio.contains("Circle")) {
			currentRoi = new OvalRoi(x, y, width, height);
		} else {
			currentRoi = new Roi(x, y, width, height);			
		}
		
		ShapeRoi shapeRoi = new ShapeRoi(currentRoi);
		inputImagePlus.setRoi(shapeRoi);
		inputImagePlus.updateAndDraw();
		
		formerAngle = 0;
		
	}
	
	
	public static void roiRotated() {
		
		ImagePlus inputImagePlus = Inset_Creator.inputImage;
		Roi currentRoi = inputImagePlus.getRoi();
		
		
		if (currentRoi == null) {
			
			magnificationChanged();
			currentRoi = inputImagePlus.getRoi();
		} 
		
		if (!Inset_Creator.aspectRatio.contains("Circle")) {
			
			currentRoi = RoiRotator.rotate(currentRoi, Inset_Creator.roiAngle - formerAngle);
			
			formerAngle = Inset_Creator.roiAngle;
			
			ShapeRoi shapeRoi = new ShapeRoi(currentRoi);
			inputImagePlus.setRoi(shapeRoi);
		}
	}
	
	/** Rotates duplicated part of image.
	 * Code adapted from the original ImageJ code from the Author: N. Vischer
	 */
	private static void straightenRotatedRect(ImagePlus impA, Roi roiA, ImagePlus impB) {
		
		Overlay oldOverlay = impB.getOverlay().duplicate();
		//System.out.println(oldOverlay);
		Overlay rotatedOverlay = oldOverlay.create();
		
		
		for (int i=0; i<oldOverlay.size(); i++) {
			Roi roi = oldOverlay.get(i);
			int position = roi.getPosition();
			
			if (!(roi instanceof TextRoi)) {
				roi = rotate(roi, -Inset_Creator.roiAngle, impB.getWidth()/2, impB.getHeight()/2);				
			}

			roi.setPosition(position);
			rotatedOverlay.add(roi);
			
		}
		//impB.setOverlay(rotatedOverlay);
		//System.out.println(rotatedOverlay);
		
		//impB.duplicate().show();
		
		impB.deleteRoi(); //we have it in roiA
		Color colorBack = Toolbar.getBackgroundColor();	
		IJ.setBackgroundColor(0,0,0);
		String title = impB.getTitle();
//		if(impB.getOverlay() != null)
//			impB.getOverlay().clear();
		int boundLeft = roiA.getBounds().x;
		int boundTop = roiA.getBounds().y;
		int boundWidth = roiA.getBounds().width;
		int boundHeight = roiA.getBounds().height;

		float[] xx = roiA.getFloatPolygon().xpoints;
		float[] yy = roiA.getFloatPolygon().ypoints;

		double dx1 = xx[1] - xx[0];//calc sides and angle
		double dy1 = yy[1] - yy[0];
		double dx2 = xx[2] - xx[1];
		double dy2 = yy[2] - yy[1];

		double rrWidth = Math.sqrt(dx1 * dx1 + dy1 * dy1);//width of rot rect
		double rrHeight = Math.sqrt(dx2 * dx2 + dy2 * dy2);
		double rrDia = Math.sqrt(rrWidth * rrWidth + rrHeight * rrHeight);

		double phi1 = -Math.atan2(dy1, dx1);
		double phi0 = phi1 * 180 / Math.PI;

		double usedL = Math.max(boundLeft, 0); //usedrect is orthogonal rect to be rotated
		double usedR = Math.min(boundLeft + boundWidth, impA.getWidth());
		double usedT = Math.max(boundTop, 0);
		double usedB = Math.min(boundTop + boundHeight, impA.getHeight());
		double usedCX = (usedL + usedR) / 2;
		double usedCY = (usedT + usedB) / 2; //Center of UsedRect

		double boundsCX = boundLeft + boundWidth / 2;//Center of Bound = center of RotRect
		double boundsCY = boundTop + boundHeight / 2;

		double dx3 = boundsCX - usedCX;//calculate excentricity
		double dy3 = boundsCY - usedCY;
		double rad3 = Math.sqrt(dx3 * dx3 + dy3 * dy3);
		double phi3 = Math.atan2(dy3, dx3);
		double phi4 = phi3 + phi1;
		double dx4 = -rad3 * Math.cos(phi4);
		double dy4 = -rad3 * Math.sin(phi4);
		
		//Increase canvas to a square large enough for rotation
		ImageStack stackOld = impB.getStack();
		int currentSlice = impB.getCurrentSlice();
		double xOff = (rrDia - (usedR - usedL)) / 2;//put img in center
		double yOff = (rrDia - (usedB - usedT)) / 2;

		ImageStack stackNew = (new CanvasResizer()).expandStack(stackOld, (int) rrDia, (int) rrDia, (int) xOff, (int) yOff);
		impB.setStack(stackNew);
	
		//impB.setOverlay(translatedRotatedOverlay);
		//impB.duplicate().show(); //test
		
		ImageProcessor ip = impB.getProcessor();
		ip.setInterpolationMethod(ImageProcessor.BICUBIC);
		ip.setBackgroundValue(0);

		for (int slc = 0; slc < stackNew.size(); slc++) {
			impB.setSlice(slc+1);
			ip.rotate(phi0); //Rotate
			ip.translate(dx4, dy4); //Translate
		}

				

		int x = (impB.getWidth() - (int) rrWidth) / 2;
		int y = (impB.getHeight() - (int) rrHeight) / 2;

		impB.setStack(impB.getStack().crop(x, y, 0, (int) rrWidth, (int) rrHeight, impB.getStack().getSize()));//Crop
		impB.setSlice(currentSlice);
		impB.setTitle(title);
		
		
		Overlay translatedRotatedOverlay = rotatedOverlay.duplicate();
		translatedRotatedOverlay.translate(xOff, yOff);
		Overlay finalOverlay = translatedRotatedOverlay.crop(new Rectangle(x, y, (int)rrWidth, (int)rrHeight));
		
		impB.setOverlay(finalOverlay);
		//impB.show();
		//impB.updateAndDraw();
		impA.setRoi(roiA); //restore rotated rect in source image
		Toolbar.setBackgroundColor(colorBack);
	}	
	
	
	private static Overlay translate(Overlay inputOverlay) {
		
		//Overlay inputOverlay = inputImage.getOverlay();
		Overlay outputOverlay = new Overlay();
		
		for (int i=0; i<inputOverlay.size(); i++) {
			Roi roi = inputOverlay.get(i);
			
			int position = roi.getPosition();
			
			if (roi.getName() == "|INSET_FRAME|" || roi.getName() == "|CLIP_ROI|") {
				
				outputOverlay.add(roi);
				
			} else if (roi instanceof Arrow) {		
				
				Arrow arrow = ((Arrow)roi);				
				Arrow newArrow = new Arrow(arrow.x2d * Inset_Creator.magnification + (arrow.x1d - arrow.x2d), arrow.y2d * Inset_Creator.magnification + (arrow.y1d - arrow.y2d), arrow.x2d * Inset_Creator.magnification, arrow.y2d * Inset_Creator.magnification);
				
				newArrow.setStrokeColor(arrow.getStrokeColor());
				newArrow.setStrokeWidth(arrow.getStrokeWidth());
				newArrow.setHeadSize(arrow.getHeadSize());
				
				roi = newArrow;
	
				roi.setPosition(position);

				outputOverlay.add(roi);			
				
			} else if (roi instanceof TextRoi) {
				
				double xBase = roi.getXBase();
				double yBase = roi.getYBase();
				
				roi.setLocation(xBase * Inset_Creator.magnification, yBase * Inset_Creator.magnification);
				
				TextRoi currentTextRoi = ((TextRoi) roi);
				
				Font textFont = currentTextRoi.getCurrentFont();
								
				TextRoi newTextRoi = new TextRoi(xBase * Inset_Creator.magnification + (currentTextRoi.getBounds().width * (Inset_Creator.magnification-1)), (yBase * Inset_Creator.magnification) - (textFont.getSize() * (Inset_Creator.magnification-1)), ((TextRoi) roi).getText(), currentTextRoi.getCurrentFont());
				newTextRoi.setStrokeColor(currentTextRoi.getStrokeColor());
				
				roi = newTextRoi;
				
				roi.setPosition(position);

				outputOverlay.add(roi);		
				
			} else {
				
				Roi roi_scaled = RoiScaler.scale(roi, Inset_Creator.magnification, Inset_Creator.magnification, false);
				roi_scaled.setStrokeWidth(roi.getStrokeWidth());
				outputOverlay.add(roi_scaled);
				
			}
			
		}
		
		return outputOverlay;
	}
	
	
	public static Roi rotate(Roi roi, double angle, double xcenter, double ycenter) {
		
		double theta = -angle*Math.PI/180.0;
		if (roi instanceof ShapeRoi)
			return rotateShape((ShapeRoi)roi, -theta, xcenter, ycenter);
		FloatPolygon poly = roi.getFloatPolygon();
		int type = roi.getType();
		boolean rotatedRect = roi instanceof RotatedRectRoi;
		double rotatedRectWidth = 0;
		if (type==Roi.LINE) {
			Line line = (Line)roi;
			double x1=line.x1d;
			double y1=line.y1d;
			double x2=line.x2d;
			double y2=line.y2d;
			poly = new FloatPolygon();
			poly.addPoint(x1, y1);
			poly.addPoint(x2, y2);
		} else if (rotatedRect) {
			double[] p = ((RotatedRectRoi)roi).getParams();
			poly = new FloatPolygon();
			poly.addPoint(p[0], p[1]);
			poly.addPoint(p[2], p[3]);
			rotatedRectWidth = p[4];
		}
		for (int i=0; i<poly.npoints; i++) {
			double dx = poly.xpoints[i]-xcenter;
			double dy = ycenter-poly.ypoints[i];
			double radius = Math.sqrt(dx*dx+dy*dy);
			double a = Math.atan2(dy, dx);
			poly.xpoints[i] = (float)(xcenter + radius*Math.cos(a+theta));
			poly.ypoints[i] = (float)(ycenter - radius*Math.sin(a+theta));
		}
		Roi roi2 = null;
		if (type==Roi.LINE) {
			
			//the following distinction is missing in IJ's RoiRotator and destroys arrows
			if (roi instanceof Arrow) {
				roi2 = new Arrow(poly.xpoints[0], poly.ypoints[0], poly.xpoints[1], poly.ypoints[1]);
			} else {
				roi2 = new Line(poly.xpoints[0], poly.ypoints[0], poly.xpoints[1], poly.ypoints[1]);				
			}
		}
		else if (rotatedRect)
			roi2 = new RotatedRectRoi(poly.xpoints[0], poly.ypoints[0], poly.xpoints[1], poly.ypoints[1], rotatedRectWidth);
		else if (type==Roi.POINT)
			roi2 = new PointRoi(poly.xpoints, poly.ypoints,poly.npoints);
		else {
			if (type==Roi.RECTANGLE)
				type = Roi.POLYGON;
			if (type==Roi.RECTANGLE && poly.npoints>4) // rounded rectangle
				type = Roi.FREEROI;
			if (type==Roi.OVAL||type==Roi.TRACED_ROI)
				type = Roi.FREEROI;
			roi2 = new PolygonRoi(poly.xpoints, poly.ypoints,poly.npoints, type);
		}
		roi2.copyAttributes(roi);
		return roi2;
	
	}
	
	private static Roi rotateShape(ShapeRoi roi, double angle, double xcenter, double ycenter) {
		Shape shape = roi.getShape();
		AffineTransform at = new AffineTransform();
		at.rotate(angle, xcenter, ycenter);
		Rectangle r = roi.getBounds();
		at.translate(r.x, r.y);
		Shape shape2 = at.createTransformedShape(shape);
		Roi roi2 = new ShapeRoi(shape2);
		roi2.copyAttributes(roi);
		return roi2;
	}


}






