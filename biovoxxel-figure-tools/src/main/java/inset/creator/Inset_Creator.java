package inset.creator;

import java.awt.Rectangle;

import org.scijava.command.Command;
import org.scijava.command.DynamicCommand;
import org.scijava.command.Interactive;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.prefs.PrefService;
import org.scijava.util.ColorRGB;
import org.scijava.widget.Button;

import ij.ImagePlus;
import ij.Macro;
import ij.WindowManager;
import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.plugin.frame.Recorder;

@Plugin(type = Command.class, menuPath="Plugins>BioVoxxel Figure Tools>Create framed inset zoom")
public class Inset_Creator extends DynamicCommand implements Interactive {
	
	@Parameter
	private PrefService prefs;
	
	@Parameter
	public static ImagePlus inputImage;
	
	@Parameter (label = "Fold magnification", min = "2", stepSize = "1", callback = "magnificationChanged")
	public static Integer magnification = 2;
	
	@Parameter (label = "Aspect ratio", choices = {"Image", "Square_height", "Square_width", "Circle_height", "Circle_width"}, callback = "magnificationChanged")
	public static String aspectRatio = "Image";
	
	@Parameter (label = "Add frame to original")
	public static Boolean addFrame = true;
	
	@Parameter (label = "Add frame to inset")
	public static Boolean addFrameToInset = true;
	
	@Parameter (label = "Frame width (px)", min = "1")
	public static Integer frameWidth = 3;
	
	@Parameter (label = "Frame color")
	public static ColorRGB frameColor = new ColorRGB(255, 255, 255);
	
	@Parameter (label = "Create", callback = "createInset")
	public static Button createButton = null;

	
	
		
	@SuppressWarnings("unused")
	private void createInset() {
		
		setPreferences();
			
		
		Roi roi = inputImage.getRoi();
		
		Recorder recorder = Recorder.getInstance();
		
		if (roi != null) {
			if (recorder != null) {
				Recorder.record = false;
				System.out.println("recording set = " + Recorder.record);
				
			}
						
			Rectangle roiBounds = inputImage.getRoi().getBounds();
			
			Recorder.recordString("selectImage(\"" + inputImage.getTitle() + "\");\n");
			if (roi instanceof OvalRoi) {
				Recorder.recordString("makeOval(" + roiBounds.x + ", " + roiBounds.y + ", " +  roiBounds.width + ", " +  roiBounds.height + ");\n");
			} else {
				
				Recorder.recordString("makeRectangle(" + roiBounds.x + ", " + roiBounds.y + ", " +  roiBounds.width + ", " +  roiBounds.height + ");\n");
			}
			Recorder.recordString("run(\"Create framed inset zoom\", \"magnification="+magnification
					+ " aspectratio=" + aspectRatio
					+ " addframe=" + addFrame
					+ " addframetoinset=" + addFrameToInset
					+ " framewidth=" + frameWidth
					+ " framecolor=" + frameColor
					+ "\");\n");
			
			InsetProcessor.createInset();
			
			
			if (recorder != null) {
				Recorder.record = true;
				System.out.println("recording set = " + Recorder.record);
				
			}
		}
	}


	private void setPreferences() {
		prefs.put(getClass(), "magnification", magnification);
		prefs.put(getClass(), "aspectRatio", aspectRatio);
		prefs.put(getClass(), "addFrame", addFrame);
		prefs.put(getClass(), "addFrameToInset", addFrameToInset);
		prefs.put(getClass(), "frameWidth", frameWidth);
		prefs.put(getClass(), "frameColor", frameColor.toString());
	}
	
	
	@SuppressWarnings("unused")
	private void magnificationChanged() {
		inputImage = WindowManager.getCurrentImage();
		InsetProcessor.magnificationChanged();
		
	}
	
	public void run() {
						
		if (Recorder.getInstance() != null) {
//			Recorder.record = false;
			System.out.println("recording set = " + Recorder.record);
			
		}
	
		String macroParams = Macro.getOptions();
		System.out.println("Macro parameters = " + macroParams);
		
		if (macroParams != null && !macroParams.equals("createbutton=null ")) {
			System.out.println("run from Macro");
			String[] paramArray = macroParams.split(" ");
			
			for (int i = 0; i < paramArray.length; i++) {
				System.out.println(paramArray[i]);
			}
			
			inputImage = WindowManager.getCurrentImage();
			magnification = Integer.parseInt(paramArray[0].substring(paramArray[0].indexOf("=")+1));
			aspectRatio = paramArray[1].substring(paramArray[1].indexOf("=")+1);
			addFrame = Boolean.parseBoolean(paramArray[2].substring(paramArray[2].indexOf("=")+1));
			addFrameToInset = Boolean.parseBoolean(paramArray[3].substring(paramArray[3].indexOf("=")+1));
			frameWidth = Integer.parseInt(paramArray[4].substring(paramArray[4].indexOf("=")+1));
			frameColor = new ColorRGB(paramArray[5].substring(paramArray[5].indexOf("=")+1));
			
			InsetProcessor.createInset();
			
		} else {
			System.out.println("run from GUI");
			InsetProcessor.magnificationChanged();
		}
	}

}
