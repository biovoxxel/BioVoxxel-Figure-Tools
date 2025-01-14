package svg.gui;

import java.io.File;

import org.scijava.command.Command;
import org.scijava.command.DynamicCommand;
import org.scijava.module.MutableModuleItem;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.CompositeImage;
import ij.ImagePlus;
import ij.WindowManager;
import ij.process.LUT;
import svg.exporter.objects.SVG_Object_Factory;
import svg.utilities.SvgUtilities;


/**
 * 
 * @author Jan Brocher / BioVoxxel
 *
 */
@Plugin(type = Command.class, menuPath="Plugins>BioVoxxel Figure Tools>Export SVG")
public class SVGExporter extends DynamicCommand {
	

	@Parameter
	ImagePlus imp;

	@Parameter(label = "File name", required = true, persist = false)
	String fileName = getSVGFileName();
	
	@Parameter(label = "Target folder", required = true, style = "directory")
	File folder;

	@Parameter(label = "Keep multichannel composite", required = true, description = "keeps channels accessible in Inkscape but increases file size")
	Boolean keepComposite = false;
	
	@Parameter(label = "Make merge interactive", required = true, description = "")
	Boolean makeInteractive = false;
	
	@Parameter(label = "Export channels", choices = {"None", "Color", "Grayscale", "Color (no overlays)", "Grayscale (no overlays)"})
	String exportChannelsSeparately = "None";
	
	@Parameter(label = "Export also non-visible channels")
	Boolean exportAlsoNonVisibleChannels = false;
	
	@Parameter(label = "Interpolate ROIs", min = "0.0", persist = true, description = "if 0.0 polygon ROIs will not be interpolated")
	Double interpolationRange = 0.0;
	
	@Parameter(label = "Lock critical ROIs", description = "ROIs such as inset frames, calibration bars and scale bars will be locked in the SVG", persist = true)
	Boolean lockSensitiveROIs = true;
	
	public void run() {
			
		SVG_Object_Factory.saveImageAndOverlaysAsSVG(imp, createSVGFile(), interpolationRange, keepComposite, makeInteractive, true, lockSensitiveROIs);
		
		if (!exportChannelsSeparately.equalsIgnoreCase("none") && imp.isComposite()) {
			exportIndividualChannels();			
		}
		
	}

	private void exportIndividualChannels() {
		
		LUT gray = SvgUtilities.getGrayLut();
		
		boolean[] activeChannels = ((CompositeImage)imp).getActiveChannels(); 
		
		for (int channel = 1; channel <= imp.getNChannels(); channel++) {
			System.out.println("channel active = " + activeChannels[channel-1]);
			if (activeChannels[channel-1] || exportAlsoNonVisibleChannels) {
				imp.setC(channel);
				fileName = "C" + channel + "-" + imp.getTitle();
				ImagePlus currentChannel = new ImagePlus(fileName, imp.getProcessor().duplicate());
				if (exportChannelsSeparately.contains("Grayscale")) {
					currentChannel.setLut(gray);
				}
				if (!exportChannelsSeparately.contains("(no overlays)")) {
					currentChannel.setOverlay(imp.getOverlay());
				}
				System.out.println("fileName = " + fileName);
				SVG_Object_Factory.saveImageAndOverlaysAsSVG(currentChannel, createSVGFile(), interpolationRange, false, false, true, lockSensitiveROIs);
			}
		}
	}	
	
	private File createSVGFile() {
		if (fileName.equals("")) {
			fileName = "ImageWithoutFileName.svg";
		}
		
		String folderPath = folder.getAbsolutePath();
		
		if (!folder.exists()) {
			folder.mkdir();
		}
		
		if (!folderPath.endsWith(File.separator)) {
			folderPath += File.separator;
		}
		
		if (!fileName.endsWith(".svg")) {
			fileName = fileName + ".svg";
		}
		
		String finalFilePath = folderPath + fileName;
		File outputFile = new File(finalFilePath);
		
		return outputFile;
	}
	
	
	private String getSVGFileName() {		
		String fileName = "";
		String imageTitle = WindowManager.getCurrentImage().getTitle();
		int fileExtensionSeparatorPosition = imageTitle.lastIndexOf(".");
		if (fileExtensionSeparatorPosition != -1) {
			fileName = imageTitle.substring(0, fileExtensionSeparatorPosition);
		} else {
			fileName = imageTitle;
		}
		return fileName;
	}
	
	
	@SuppressWarnings("unused")
	private void getImageName() {
		
		final MutableModuleItem<String> mutableFileName = getInfo().getMutableInput("fileName", String.class);

		mutableFileName.setValue(this, getSVGFileName());
		
	}	
}
