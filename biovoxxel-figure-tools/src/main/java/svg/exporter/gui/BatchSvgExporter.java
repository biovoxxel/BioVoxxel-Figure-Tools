package svg.exporter.gui;


import java.io.File;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.WindowManager;
import svg.exporter.objects.SVG_Object_Factory;


/**
 * 
 * @author Jan Brocher / BioVoxxel
 *
 */
@Plugin(type = Command.class, menuPath="Plugins>BioVoxxel Figure Tools>Export all images as SVG")
public class BatchSvgExporter implements Command{
	
		@Parameter(label = "Target folder", required = true, style = "directory")
		File folder;
	
		@Parameter(label = "Interpolate ROIs", min = "0.0", persist = true, description = "if 0.0 polygon ROIs will not be interpolated")
		Double interpolationRange = 0.0;
		
		public void run() {
		
			int[] imageIDList = WindowManager.getIDList();
			
			for (int i = 0; i < imageIDList.length; i++) {
				
				ImagePlus imp = WindowManager.getImage(imageIDList[i]);
				System.out.println(imp);
									
				SVG_Object_Factory.saveImageAndOverlaysAsSVG(imp, folder, 0.0, true);
				
			}
		
		}
}
