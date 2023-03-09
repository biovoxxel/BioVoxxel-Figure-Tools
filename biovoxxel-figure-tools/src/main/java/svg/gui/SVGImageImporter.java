package svg.gui;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import svg.importer.SVGImageReader;

@Plugin(type = Command.class, menuPath="Plugins>BioVoxxel Figure Tools>Import images from SVG")
public class SVGImageImporter implements Command {

	@Parameter(label = "File", required = true, style = "file")
	public static File inputFile;
	
	
	@Override
	public void run() {

		if (inputFile.getAbsolutePath().endsWith(".svg")) {
			SVGImageReader svgImporter = new SVGImageReader();
			try {
				svgImporter.readSVG(inputFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			JOptionPane.showMessageDialog(null, "Only .SVG files are supported", "Wrong file type", JOptionPane.ERROR_MESSAGE);
		}
		
	}
	
	
	public static void main(String[] args) {
		inputFile = new File("C:\\Users\\Admin\\Desktop\\Test\\hela-cells_overlay.svg");
		new SVGImageImporter().run();
	}

}
