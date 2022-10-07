package inset.creator;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;

@Plugin(type = Command.class, menuPath="Plugins>BioVoxxel Figure Tools>Create framed inset Zoom")
public class Inset_Creator implements Command {

	@Parameter
	ImagePlus inputImage;
	
	
	public void run() {
		InsetCreator ic = new InsetCreator(inputImage);
		ic.setVisible(true);
		
	}
}
