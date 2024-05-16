package inset.creator;

import java.awt.Color;

import javax.swing.JOptionPane;

import ij.ImagePlus;
import ij.Macro;
import ij.WindowManager;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.measure.Calibration;
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
//			ImageProcessor scaledProcessor = insetImageProcessor.resize(frameRoi.getBounds().width * magnification, frameRoi.getBounds().height * magnification);
			int channel = imagePlus.getC();
			int slice = imagePlus.getZ();
			int frame = imagePlus.getT();
			
			ImagePlus scaledImagePlus = imagePlus.resize(frameRoi.getBounds().width * Inset_Creator.magnification, frameRoi.getBounds().height * Inset_Creator.magnification, 1, "none");
			
			Roi ovalRoi = null;
			if (frameRoi instanceof OvalRoi) {
				
				ovalRoi = new OvalRoi(1, 1, scaledImagePlus.getWidth()-(frameWidth+1), scaledImagePlus.getHeight()-(frameWidth+1));

//obsolete after version 2.2.0 (just kept for this one version for security)
//
//				for (int c = 1; c <= scaledImagePlus.getNChannels(); c++) {
//					scaledImagePlus.setC(c);
//					for (int z = 1; z <= scaledImagePlus.getNSlices(); z++) {
//						scaledImagePlus.setZ(z);
//						for (int f = 1; f <= scaledImagePlus.getNFrames(); f++) {
//							scaledImagePlus.setT(f);
//							ImageProcessor scaledImageProcessor = scaledImagePlus.getProcessor();
//							scaledImageProcessor.setColor(Color.BLACK);
//							scaledImagePlus.getProcessor().fillOutside(ovalRoi);
//						}
//					}
//				}
			}
			
			if (Inset_Creator.addFrameToInset) {
				Roi insetRoi = null;
				if (frameRoi instanceof OvalRoi) {
					insetRoi = ovalRoi;
				} else {
					insetRoi = new Roi(1, 1, scaledImagePlus.getWidth()-2, scaledImagePlus.getHeight()-2);
					
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
				if (frameRoi instanceof OvalRoi) {
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
		ImagePlus imagePlus = Inset_Creator.inputImage;
		Roi currentRoi = imagePlus.getRoi();
		
		int x = 0;
		int y = 0;
		int width = 0;
		int height = 0;

		if (currentRoi != null) {
			x = currentRoi.getBounds().x;
			y = currentRoi.getBounds().y;
			width = (int)(imagePlus.getWidth() / magnification);
			height = (int)(imagePlus.getHeight() / magnification);
		} else {
			x = 0;
			y = 0;
			width = (int)(imagePlus.getWidth() / magnification);
			height = (int)(imagePlus.getHeight() / magnification);
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

		imagePlus.setRoi(currentRoi);
	}
	
}









