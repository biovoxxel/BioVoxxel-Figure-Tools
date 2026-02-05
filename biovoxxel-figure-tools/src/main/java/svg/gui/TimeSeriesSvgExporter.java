package svg.gui;

import java.io.File;

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
	
	@Parameter (initializer = "checkDimension")
	ImagePlus imp;
	
	@Parameter(label = "Target folder", required = true, style = "directory")
	File folder;
	
	@Parameter(label = "Keep multichannel composite", required = true, description = "keeps channels accessible in Inkscape but increases file size")
	Boolean keepComposite = false;
	
	@Parameter(label = "Make merge interactive", required = true, description = "")
	Boolean makeInteractive = false;
	
	@Parameter(label = "First frame", required = true)
	Integer firstFrame = 1;
	
	@Parameter(label = "Increment", required = true)
	Integer increment = 1;
	
	@Parameter(label = "Use Slice label as title", required = true)
	Boolean useSliceLabel = false;
	
	@Parameter(label = "Interpolate ROIs", min = "0.0", persist = true, description = "if 0.0 polygon ROIs will not be interpolated")
	Double interpolationRange = 0.0;
	
	@Parameter(label = "Lock critical ROIs", description = "ROIs such as inset frames, calibration bars and scale bars will be locked in the SVG", persist = true)
	Boolean lockSensitiveROIs = true;
	
	public void run() {
			
		subfolderPath = createSubfolder();
							
		for (int frame = firstFrame; frame <= imp.getNFrames(); frame += increment) {
			
			imp.setT(frame);
			ImagePlus currentSliceImp = imp.crop("whole-slice");
			
			if (useSliceLabel && imp.getStack().getSliceLabel(frame) != null) {
				currentSliceImp.setTitle(imp.getStack().getSliceLabel(frame));
			} else {
				currentSliceImp.setTitle(imp.getTitle() + "_T_" + String.format("%04d", frame));
			}
			
			SVG_Object_Factory.saveImageAndOverlaysAsSVG(currentSliceImp, createSVGFile(String.format("%04d", frame)), interpolationRange, keepComposite, makeInteractive, true, lockSensitiveROIs);
			
		}
	}

	
	private File createSVGFile(String sliceNumber) {
						
		if (!subfolderPath.endsWith(File.separator)) {
			subfolderPath += File.separator;
		}
		
		String imageTitle = null;
		if (useSliceLabel && imp.getStack().getSliceLabel(imp.getT()) != null) {
			
			imageTitle = imp.getStack().getSliceLabel(imp.getT());
		} else {
			imageTitle = imp.getTitle();
		}
	
		
		String finalFilePath = subfolderPath + imageTitle + "_T_" + sliceNumber + ".svg";
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
	
	@SuppressWarnings("unused")
	private void checkDimension() {
		if (imp.getNFrames() == 1) {
			cancel("The image has only 1 time frame\n"
					+ "Check >Image >Properties... and set tine frame count correctly\n"
					+ "or use Export Z-slices as SVGs instead");			
		}
	}
	
}

