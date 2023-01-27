package metadataRecorder;

import java.awt.TextArea;
import java.awt.Window;
import java.awt.event.TextListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JOptionPane;

import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.frame.Recorder;

/**
 * 
 * @author Jan Brocher / BioVoxxel
 *
 */
@Plugin(type = Command.class, menuPath="Plugins>BioVoxxel Figure Tools>Meta-D-Rex")
public class MetadataRecorder implements Command, WindowListener {
	
	protected static MetadataRecorderGUI METADATA_RECORDER = null;
	
	public void run() {
		ImagePlus currentImage = WindowManager.getCurrentImage();
		
		if (currentImage == null) {
			JOptionPane.showMessageDialog(null, "No open image detected", "No Image", JOptionPane.ERROR_MESSAGE);
		} else {
			
			Window existingMetadataRecorder = WindowManager.getWindow("Meta-D-Rex");
			
			if (existingMetadataRecorder == null) {
				METADATA_RECORDER = new MetadataRecorderGUI(currentImage);
				METADATA_RECORDER.setVisible(true);
				METADATA_RECORDER.addWindowListener(this);
				
				WindowManager.addWindow(METADATA_RECORDER);
			} else {
				existingMetadataRecorder.toFront();
				JOptionPane.showMessageDialog(null, "Metadata Recorder is already running", "Already Running", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
		
		
	@Override
	public void windowOpened(WindowEvent e) {
		
	}



	@Override
	public void windowClosing(WindowEvent e) {
		
		Recorder ijRecorder = (Recorder) WindowManager.getFrame("Recorder");
		if (ijRecorder != null) {
			
			TextListener[] textListener = ((TextArea)ijRecorder.getComponent(1)).getTextListeners();
			if (textListener.length > 0) {
				((TextArea)ijRecorder.getComponent(1)).removeTextListener(textListener[0]);
			}
			
			if (!ijRecorder.isVisible()) {
//				ijRecorder.setVisible(true);
				ijRecorder.close();
//				WindowManager.removeWindow(ijRecorder);
			}
		}
		
		
		WindowManager.removeWindow(METADATA_RECORDER);
		System.out.println("removed MetadataRecorder from WindowManager");
		
		METADATA_RECORDER.dispose();
		System.out.println("disposed " + METADATA_RECORDER);
	}



	@Override
	public void windowClosed(WindowEvent e) {
		
	}



	@Override
	public void windowIconified(WindowEvent e) {
		
	}



	@Override
	public void windowDeiconified(WindowEvent e) {
		
	}



	@Override
	public void windowActivated(WindowEvent e) {
		
	}



	@Override
	public void windowDeactivated(WindowEvent e) {
		
		METADATA_RECORDER.saveMetadataToImage();
		
	}
	
}
