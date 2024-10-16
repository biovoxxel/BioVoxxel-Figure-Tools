package inset.creator;

import java.awt.Color;

import javax.swing.JOptionPane;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Macro;
import ij.WindowManager;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.gui.Toolbar;
import ij.measure.Calibration;
import ij.plugin.CanvasResizer;
import ij.plugin.RoiRotator;
import ij.process.ImageProcessor;

public class InsetProcessor {
	
	
	private static ImagePlus imagePlus;
	private static ImageProcessor insetImageProcessor;
	private static ImagePlus insetImagePlus;
	private static Roi frameRoi;
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
			
			if (Inset_Creator.roiAngle != 0 && !Inset_Creator.aspectRatio.contains("Circle")) {
				ImagePlus duplicatedInset = imagePlus.crop();
				straightenRotatedRect(imagePlus, frameRoi, duplicatedInset);
				//duplicatedInset.show(); //test
				scaledImagePlus = duplicatedInset.resize(duplicatedInset.getWidth() * Inset_Creator.magnification, duplicatedInset.getHeight() * Inset_Creator.magnification, 1, "none");
				
			} else {
				scaledImagePlus = imagePlus.resize(frameRoi.getBounds().width * Inset_Creator.magnification, frameRoi.getBounds().height * Inset_Creator.magnification, 1, "none");				
			}
			
			
			
			Roi ovalRoi = null;
			if (Inset_Creator.aspectRatio.contains("Circle")) {			
				ovalRoi = new OvalRoi(frameWidth/2, frameWidth/2, scaledImagePlus.getWidth()-frameWidth, scaledImagePlus.getHeight()-frameWidth);
			}
			
			if (Inset_Creator.addFrameToInset) {
				Roi insetRoi = null;
				if (Inset_Creator.aspectRatio.contains("Circle")) {
					insetRoi = ovalRoi;
				} else {
					insetRoi = new Roi(frameWidth/2, frameWidth/2, scaledImagePlus.getWidth()-frameWidth, scaledImagePlus.getHeight()-frameWidth);
					
				}
				insetRoi.setStrokeWidth(Inset_Creator.frameWidth);
				insetRoi.setStrokeColor(frameColor);
				insetRoi.setName("|INSET_FRAME|");
				
				Overlay insetOverlay = scaledImagePlus.getOverlay();
				if (insetOverlay == null) {
					insetOverlay = new Overlay();
					scaledImagePlus.setOverlay(insetOverlay);
				}
				insetOverlay.add(insetRoi);
				if (Inset_Creator.aspectRatio.contains("Circle")) {
					Roi clippingRoi = (Roi)insetRoi.clone();
					clippingRoi.setName("|CLIP_ROI|");
					insetOverlay.add(clippingRoi);	//add twice to have a clipping Roi in Inkscape available
				}
				
				scaledImagePlus.killRoi();
			}
			
			String insetTitle = WindowManager.getUniqueName("Inset_" + imagePlus.getTitle());
			scaledImagePlus.setTitle(insetTitle);
			
			Calibration imageCalibration = imagePlus.getCalibration();
			Calibration insetCalibration = imageCalibration.copy();
			
			insetCalibration.pixelWidth = imageCalibration.pixelWidth / Inset_Creator.magnification;
			insetCalibration.pixelHeight = imageCalibration.pixelHeight / Inset_Creator.magnification;
			
			if (Inset_Creator.addFrame) {
				if (Inset_Creator.aspectRatio.contains("Circle")) {
					frameRoi = new OvalRoi(frameRoi.getBounds().x, frameRoi.getBounds().y, frameRoi.getBounds().width, frameRoi.getBounds().height);
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
						
			scaledImagePlus.setCalibration(insetCalibration);
			
			scaledImagePlus.show();

			scaledImagePlus.setC(channel);
			scaledImagePlus.setZ(slice);
			scaledImagePlus.setT(frame);
			
			
						
		} else {
			JOptionPane.showMessageDialog(null, "No open image detected");
		}
	}

	
//	private static boolean isCalibrated(ImagePlus imagePlus) {
//		
//		System.out.println(imagePlus.getTitle() + " is spatially calibrated: " + imagePlus.getCalibration().scaled());
//		return imagePlus.getCalibration().scaled();
//		
//	}


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
		
		if (!Inset_Creator.aspectRatio.contains("Circle")) {
						
			currentRoi = RoiRotator.rotate(currentRoi, Inset_Creator.roiAngle);
			
		} 
		
		ShapeRoi shapeRoi = new ShapeRoi(currentRoi);
		inputImagePlus.setRoi(shapeRoi);
		
	}
	
	/** Rotates duplicated part of image
	- impA is original image,
	- roiA is orig rotatedRect
	- impB contains duplicated overlapping bounding rectangle	
	processing steps:
	- increase canvas of impB before rotation
	- rotate impB
	- calculate excentricity
	- translate to compensate excentricity 
	- create orthogonal rectangle in center
	- crop to impC	
	Author: N. Vischer
	*/
	private static void straightenRotatedRect(ImagePlus impA, Roi roiA, ImagePlus impB) {
		impB.deleteRoi(); //we have it in roiA
		Color colorBack = Toolbar.getBackgroundColor();	
		IJ.setBackgroundColor(0,0,0);
		String title = impB.getTitle();
		if(impB.getOverlay() != null)
			impB.getOverlay().clear();
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
		//impB.show();
		//impB.updateAndDraw();
		impA.setRoi(roiA); //restore rotated rect in source image
		Toolbar.setBackgroundColor(colorBack);
	}	
	
	
	
}









