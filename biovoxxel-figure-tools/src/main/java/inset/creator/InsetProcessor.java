package inset.creator;

import java.awt.Color;

import javax.swing.JOptionPane;

import ij.ImagePlus;
import ij.WindowManager;
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
	private static int magnification;
	
	private static boolean addFrame;
	
	
	
	public InsetProcessor() {
		
	}


	
	public static void createInset() {
		
		doSetup();
		
		if (imagePlus != null) {
//			ImageProcessor scaledProcessor = insetImageProcessor.resize(frameRoi.getBounds().width * magnification, frameRoi.getBounds().height * magnification);
			
			ImagePlus scaledImagePlus = imagePlus.resize(frameRoi.getBounds().width * magnification, frameRoi.getBounds().height * magnification, 1, "none");
			String insetTitle = WindowManager.getUniqueName("Inset_" + imagePlus.getTitle());
			scaledImagePlus.setTitle(insetTitle);
			
			Calibration imageCalibration = imagePlus.getCalibration();
			Calibration insetCalibration = imageCalibration.copy();
			
			insetCalibration.pixelWidth = imageCalibration.pixelWidth / magnification;
			insetCalibration.pixelHeight = imageCalibration.pixelHeight / magnification;
			
			if (InsetCreator.isAddFrameActive()) {
				frameRoi.setStrokeWidth(InsetCreator.getFrameWidth());
				frameRoi.setStrokeColor(InsetCreator.getFrameColor());
				frameRoi.setName("|INSET_FRAME|");
				
				Overlay overlay = imagePlus.getOverlay();
				if (overlay == null) {
					overlay = new Overlay();
					imagePlus.setOverlay(overlay);
				}
				imagePlus.getOverlay().add(frameRoi);
				imagePlus.killRoi();
				
			}
						
			scaledImagePlus.setCalibration(insetCalibration);
			
			scaledImagePlus.show();
			
			
			
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
		imagePlus = InsetCreator.getImage();
		System.out.println("Original image: " + imagePlus.getTitle());
		
		frameRoi = imagePlus.getRoi();
		System.out.println("Selected ROI: " + frameRoi);
		
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
		
		magnification = InsetCreator.getMagnification();
		System.out.println("MAGNIFICATION = " + magnification);
		
		addFrame = InsetCreator.addFrameToInset();
		System.out.println("ADD_FRAME = " + addFrame);
		
		frameWidth = InsetCreator.getFrameWidth();
		System.out.println("FRAME_WIDTH = " + frameWidth);
		
		frameColor = InsetCreator.getFrameColor();
		System.out.println("FRAME_COLOR = " + frameColor);
				
		InsetCreator.getPreserveButtonGroup();
	}
	
	
	public static void magnificationChanged() {
		int magnification = InsetCreator.getMagnification();
		ImagePlus imagePlus = InsetCreator.getImage();
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
		
		
		switch (InsetCreator.getPreserveSelection()) {
		case 0:
			//keep upper settings
			break;
		case 1:
			width = height;
			break;
		case 2:
			height = width;
			break;
		default:
			break;
		}
		
		currentRoi = new Roi(x, y, width, height);

		imagePlus.setRoi(currentRoi);
	}
	
}









