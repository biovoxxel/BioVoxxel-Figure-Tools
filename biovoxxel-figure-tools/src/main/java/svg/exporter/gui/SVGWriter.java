package svg.exporter.gui;

import java.io.File;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import svg.exporter.objects.SVG_Object_Factory;


/**
 * 
 * @author Jan Brocher / BioVoxxel
 *
 */
@Plugin(type = Command.class, menuPath="Plugins>BioVoxxel Figure Tools>Export SVG")
public class SVGWriter implements Command {
	

	@Parameter
	ImagePlus imp;

	
	@Parameter(label = "File path", required = true, style = "file")
	File file;

	
	@Parameter(label = "Interpolate ROIs", min = "0.0", persist = true, description = "if 0.0 polygon ROIs will not be interpolated")
	Double interpolationRange = 0.0;
	
	public void run() {
			
		SVG_Object_Factory.saveImageAndOverlaysAsSVG(imp, file, interpolationRange, true);
		
	}
}
