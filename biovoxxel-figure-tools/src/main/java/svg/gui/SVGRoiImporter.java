package svg.gui;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import svg.importer.SVGImageReader;

//@Plugin(type = Command.class, menuPath="Plugins>BioVoxxel Figure Tools>Experimental>Import Polygon-ROIs from SVG")
public class SVGRoiImporter implements Command {

	@Parameter(label = "SVG File", required = true, style = "file")
	public static File inputFile;
	
	
	@Override
	public void run() {

		if (inputFile.getAbsolutePath().endsWith(".svg")) {
			SVGImageReader svgImporter = new SVGImageReader();
			try {
				svgImporter.readRoisToRoiManager(inputFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			JOptionPane.showMessageDialog(null, "Only .SVG files are supported", "Wrong file type", JOptionPane.ERROR_MESSAGE);
		}
		
	}
}
