package svg.gui;


import java.io.File;

import org.scijava.command.Command;
import org.scijava.command.DynamicCommand;
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
@Plugin(type = Command.class, menuPath="Plugins>BioVoxxel Figure Tools>Export all images as SVG")
public class BatchSvgExporter extends DynamicCommand {
	
		@Parameter(label = "Target folder", required = true, style = "directory")
		File folder;
		
		@Parameter(label = "Keep multichannel composite", required = true, description = "keeps channels accessible in Inkscape but increases file size")
		Boolean keepComposite = false;
		
		@Parameter(label = "Export channels", choices = {"None", "Color", "Grayscale", "Color (no overlays)", "Grayscale (no overlays)"})
		String exportChannelsSeparately = "None";
		
		@Parameter(label = "Export also non-visible channels")
		Boolean exportAlsoNonVisibleChannels = false;
	
		@Parameter(label = "Interpolate ROIs", min = "0.0", persist = true, description = "if 0.0 polygon ROIs will not be interpolated")
		Double interpolationRange = 0.0;
		
		@Parameter(label = "Lock critical ROIs", description = "ROIs such as inset frames, calibration bars and scale bars will be locked in the SVG", persist = true)
		Boolean lockSensitiveROIs = true;
		
		public void run() {
		
			int[] imageIDList = WindowManager.getIDList();
			int exportCounter = 1;
			
			if (!folder.exists()) {
				folder.mkdir();
			}			
			
			for (int i = 0; i < imageIDList.length; i++) {
				
				ImagePlus imp = WindowManager.getImage(imageIDList[i]);
				System.out.println(imp);
				String originalImpTitle = imp.getTitle();
				
				
				imp.setTitle(String.format("%02d", exportCounter) + "_" + originalImpTitle);
				SVG_Object_Factory.saveImageAndOverlaysAsSVG(imp, folder, interpolationRange, keepComposite,true, lockSensitiveROIs);
				imp.setTitle(originalImpTitle);
				exportCounter++;
				
				if (!exportChannelsSeparately.equalsIgnoreCase("none") && imp.isComposite()) {
					LUT gray = SvgUtilities.getGrayLut();
					
					boolean[] activeChannels = ((CompositeImage)imp).getActiveChannels(); 
					
					for (int channel = 1; channel <= imp.getNChannels(); channel++) {
						if (activeChannels[channel-1] || exportAlsoNonVisibleChannels) {
							imp.setC(channel);
							String fileName = String.format("%02d", exportCounter) + "_C" + channel + "-" + imp.getTitle();
							ImagePlus currentChannel = new ImagePlus(fileName, imp.getProcessor().duplicate());
							if (exportChannelsSeparately.contains("Grayscale")) {
								currentChannel.setLut(gray);
							}
							if (!exportChannelsSeparately.contains("(no overlays)")) {
								currentChannel.setOverlay(imp.getOverlay());
							}
							System.out.println("fileName = " + fileName);
							SVG_Object_Factory.saveImageAndOverlaysAsSVG(currentChannel, folder, interpolationRange, false, true, lockSensitiveROIs);
							exportCounter++;
						}
					}
				}
			}
		}
		
}
