package svg.gui;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;

@Plugin(type = Command.class, menuPath = "Plugins>BioVoxxel Figure Tools>Start IJ Scale Bar")
public class StartJIScaleBar implements Command {

	@Parameter
	ImagePlus imp;

	@Override
	public void run() {
		IJ.run(imp, "Scale Bar...", "");
	}
	
	
}
