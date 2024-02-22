package svg.gui;

import java.io.File;

import javax.swing.JOptionPane;

import org.scijava.command.Command;
import org.scijava.command.DynamicCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import svg.exporter.objects.SVG_Object_Factory;


/**
 * 
 * @author Jan Brocher / BioVoxxel
 *
 */
@Plugin(type = Command.class, menuPath="Plugins>BioVoxxel Figure Tools>Export time series as SVGs")
public class TimeSeriesSvgExporter extends DynamicCommand {
	
	private String subfolderPath = "";
	
	@Parameter
	ImagePlus imp;
	
	@Parameter(label = "Target folder", required = true, style = "directory")
	File folder;
	
	@Parameter(label = "Keep multichannel composite", required = true, description = "keeps channels accessible in Inkscape but increases file size")
	Boolean keepComposite = false;
	
	@Parameter(label = "First frame", required = true)
	Integer firstFrame = 1;
	
	@Parameter(label = "Increment", required = true)
	Integer increment = 1;
	
	@Parameter(label = "Interpolate ROIs", min = "0.0", persist = true, description = "if 0.0 polygon ROIs will not be interpolated")
	Double interpolationRange = 0.0;
	
	@Parameter(label = "Lock critical ROIs", description = "ROIs such as inset frames, calibration bars and scale bars will be locked in the SVG", persist = true)
	Boolean lockSensitiveROIs = true;
	
	public void run() {
			
		subfolderPath = createSubfolder();
		
		if (imp.getNFrames() == 1) {
			JOptionPane.showMessageDialog(null, "The image has only 1 time point\nCheck >Image >Properties...\nand set frame count correctly", "No Time Series", JOptionPane.INFORMATION_MESSAGE);
		} else {
			
			for (int slice = firstFrame; slice <= imp.getNFrames(); slice += increment) {
				
				imp.setT(slice);
				ImagePlus currentSliceImp = imp.crop("whole-slice");
				currentSliceImp.setTitle(imp.getTitle() + "_" + slice);
				
				SVG_Object_Factory.saveImageAndOverlaysAsSVG(currentSliceImp, createSVGFile(slice), interpolationRange, keepComposite, true, lockSensitiveROIs);
				
			}
		}
	}

	
	private File createSVGFile(Integer sliceNumber) {
						
		if (!subfolderPath.endsWith(File.separator)) {
			subfolderPath += File.separator;
		}
		
		String imageTitle = imp.getTitle();
		
		String finalFilePath = subfolderPath + imageTitle + "_" + sliceNumber + ".svg";
		File outputFile = new File(finalFilePath);
		System.out.println(outputFile);
		
		return outputFile;
	}

	
	private String createSubfolder() {
		
		String imageTitle = imp.getTitle();
		
		String folderPath = folder.getAbsolutePath();
		if (!folderPath.endsWith(File.separator)) {
			folderPath += File.separator;
		}
				
		String subfolder = "";
		int fileExtensionSeparatorPosition = imageTitle.lastIndexOf(".");
		if (fileExtensionSeparatorPosition != -1) {
			
			subfolder = folderPath + imageTitle.substring(0, fileExtensionSeparatorPosition);

		} else {
			
			subfolder = folderPath + imageTitle;
			
		}
		
		File subfolderFile = new File(subfolder);
		subfolderFile.mkdir();
		
		return subfolderFile.getAbsolutePath();
		
	}
	
}

