package svg.gui;


import java.io.File;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

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
public class BatchSvgExporter implements Command {
	
		@Parameter(label = "Target folder", required = true, style = "directory")
		File folder;
		
		@Parameter(label = "Export channels", choices = {"None", "Color", "Grayscale", "Color (no overlays)", "Grayscale (no overlays)"})
		String exportChannelsSeparately = "None";
	
		@Parameter(label = "Interpolate ROIs", min = "0.0", persist = true, description = "if 0.0 polygon ROIs will not be interpolated")
		Double interpolationRange = 0.0;
		
		public void run() {
		
			int[] imageIDList = WindowManager.getIDList();
			
			for (int i = 0; i < imageIDList.length; i++) {
				
				ImagePlus imp = WindowManager.getImage(imageIDList[i]);
				System.out.println(imp);
									
				SVG_Object_Factory.saveImageAndOverlaysAsSVG(imp, folder, interpolationRange, true);
				
				if (!exportChannelsSeparately.equalsIgnoreCase("none")) {
					LUT gray = SvgUtilities.getGrayLut();
					
					for (int channel = 1; channel <= imp.getNChannels(); channel++) {
						imp.setC(channel);
						String fileName = "C" + channel + "-" + imp.getTitle();
						ImagePlus currentChannel = new ImagePlus(fileName, imp.getProcessor());
						if (exportChannelsSeparately.contains("Grayscale")) {
							currentChannel.setLut(gray);					
						}
						if (!exportChannelsSeparately.contains("(no overlays)")) {
							currentChannel.setOverlay(imp.getOverlay());					
						}
						System.out.println("fileName = " + fileName);
						SVG_Object_Factory.saveImageAndOverlaysAsSVG(currentChannel, folder, interpolationRange, true);
					}
				}
			}
		}
		
}