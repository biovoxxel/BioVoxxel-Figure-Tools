package svg.utilities;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import ij.ImagePlus;
import ij.gui.Arrow;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.FloatPolygon;
import ij.process.LUT;

public class SvgUtilities {

	

		
	protected static final FileNameExtensionFilter SVG_FILE_FILTER = new FileNameExtensionFilter("SVG", "svg");

	
	
	@SuppressWarnings("unused")
	private static File getSaveLocation(FileNameExtensionFilter filter, int fileSelectionMode) {

		JFileChooser fileChooser = new JFileChooser();
		
		if (filter != null) {
			if (filter.equals(SVG_FILE_FILTER)) {
				fileChooser.setSelectedFile(new File("Figure_X.svg"));
			} 

			fileChooser.setFileFilter(filter);
		}

		fileChooser.setFileSelectionMode(fileSelectionMode);
		
		int buttonPressed = fileChooser.showSaveDialog(null);
		
		File selectedFile = fileChooser.getSelectedFile();

		
		if (buttonPressed==JFileChooser.CANCEL_OPTION) {
			selectedFile = null;
		}
		
		return selectedFile;
	
	}
	
	
//	public static File getSvgFile(ImagePlus imp) {
//		
//		return getSvgFile(new File(imp.getOriginalFileInfo().getFilePath()));
//		
//	}
	/**
	 * If file == null the file path will be deduced from the ImagePlus (imp) given. If imp == null as well
	 * the file path will be directed to the users desktop and the file will be named "no_name_image.svg".
	 * This does not work if multiple files will be saved in batch
	 * 
	 * @param imp	
	 * @param file	
	 * @return
	 */
	public static File getSvgFile(ImagePlus imp, File file) throws NullPointerException {
		
		String filePath = null;
		
		if (file.isDirectory()) {
			
			String originalImageTitle = imp.getTitle();
			if (originalImageTitle.lastIndexOf(".") != -1) {
				originalImageTitle = originalImageTitle.substring(0, originalImageTitle.lastIndexOf("."));
			}
			
			filePath = file.getPath() + File.separator + originalImageTitle + ".svg";
		} else {
			
			filePath = file.getAbsolutePath();
			
			if (!filePath.endsWith(".svg")) {
				
				if (filePath.lastIndexOf(".") != -1) {
					filePath = filePath.substring(0, filePath.length()-4) + ".svg";
				} else {
					filePath = filePath + ".svg";
				}
				
			}
		}
		
		filePath = filePath.replace("DUP_", "");
		System.out.println("File saving path = " + filePath);
		return new File(filePath);
	}
		
//		} else if (imp != null) {
//			
//			if (imp.getOriginalFileInfo() != null) {
//				
//				String imageFilePath = imp.getOriginalFileInfo().getFilePath();
//				
//				if (imageFilePath != null) {
//					
//					if (imageFilePath.lastIndexOf(File.separator) == -1) {
//						
//						filePath = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + imp.getShortTitle() + ".svg";
//						
//					} else {
//						
//						filePath = imageFilePath.substring(0, imageFilePath.lastIndexOf(File.separator) + 1) + imp.getShortTitle() + ".svg";
//					}
//				}
//				
//			} else {
//				filePath = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + imp.getShortTitle() + ".svg";
//			}
//			
//		} else {
//			
//			filePath = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "no_name_image.svg";
//		}
		
	
	
	
	
	public static Roi interpolateRoi(Roi roi, double interpolationInterval, boolean smooth) {
		
		int roiType = roi.getType();
		System.out.println("roiType = " + roiType);
		
		PolygonRoi interpolatedPolygonRoi = null;
		
		if (roiType!=Roi.LINE && roiType!=Roi.ANGLE && roiType!=Roi.POINT && !(roi instanceof Arrow) && interpolationInterval != 0) {
				
			FloatPolygon interpolatedPolygon;
			interpolatedPolygon = roi.getInterpolatedPolygon(interpolationInterval, smooth);							
				
			if (roiType==Roi.COMPOSITE) {

				roiType = Roi.TRACED_ROI;

			} else if (roiType==Roi.RECTANGLE || roiType==Roi.OVAL) {

				roiType = Roi.POLYGON;

			}
			
			interpolatedPolygonRoi = new PolygonRoi(interpolatedPolygon, roiType);

			interpolatedPolygonRoi.setFillColor(roi.getFillColor());
			interpolatedPolygonRoi.setStrokeColor(roi.getStrokeColor());
			interpolatedPolygonRoi.setStrokeWidth(roi.getStrokeWidth());
		
			return interpolatedPolygonRoi;
			
		} else {
			
			return roi;
			
		}
	}
	
	public static LUT getGrayLut() {
		byte[] reds = new byte[256];
		byte[] greens = new byte[256];
		byte[] blues = new byte[256];
		
		for (int i = 0; i < 256; i++) {
			reds[i] = (byte)i;
			greens[i] = (byte)i;
			blues[i] = (byte)i;
		}
		
		return new LUT(reds, greens, blues);
	}
	
		

}
