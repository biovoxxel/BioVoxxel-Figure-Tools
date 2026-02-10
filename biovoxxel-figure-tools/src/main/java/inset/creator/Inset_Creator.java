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

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.WindowManager;
import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.plugin.ScaleBar;
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
	
	@Parameter (label = "Angle (-90 to 90)", callback = "roiRotated", min = "-90", max = "90", stepSize = "1", persist = false, style = "slider")
	public static Integer roiAngle = 0;
	
	@Parameter (label = "Add frame to original")
	public static Boolean addFrame = true;
	
	@Parameter (label = "Add frame to inset")
	public static Boolean addFrameToInset = false;
		
	@Parameter (label = "Frame width (px)", min = "1")
	public static Integer frameWidth = 3;
	
	@Parameter (label = "Frame color")
	public static ColorRGB frameColor = new ColorRGB(255, 255, 255);
	
	@Parameter (label = "Add Scalebar to inset", description = "this only takes efect if run from the GUI. It will not be executed when run from a macro")
	public static Boolean startScaleBarPlugin = false;
	
	@Parameter (label = "Create", callback = "createInset", required = false)
	public static Button createButton = null;
	
		
	@SuppressWarnings("unused")
	private void createInset() {
		
		setPreferences();
			
		String macroRecording = "//------------------------------------------\n"
				+ "//Create reproducible zoomed image inset\n";
		
		
		Roi roi = inputImage.getRoi();
		
		int channel = inputImage.getC();
		int slice = inputImage.getZ();
		int frame = inputImage.getT();
		
		if (inputImage.isComposite()) {
			
			CompositeImage ci = ((CompositeImage) inputImage);
			boolean[] activeChannels = ci.getActiveChannels();
			
			String activeChannelString = "";
			
			for (int c = 1; c <= activeChannels.length; c++) {
				if (activeChannels[c-1]) {
					activeChannelString += 1;					
				} else {
					activeChannelString += 0;	
				}
			}
			
			macroRecording += "Stack.setActiveChannels(" + activeChannelString + ");\n";
			
		}
						
		Recorder recorder = Recorder.getInstance();
		
		if (roi != null) {
			if (recorder != null) {
				Recorder.record = false;
				System.out.println("recording set = " + Recorder.record);
				
			}
						
			Rectangle roiBounds = inputImage.getRoi().getBounds();
			
			macroRecording += "Stack.setPosition(" + channel + ", " + slice + ", " + frame + ");\n"
					+ "selectImage(\"" + inputImage.getTitle() + "\");\n";
			
			if (roi instanceof OvalRoi) {
				macroRecording += "makeOval(" + roiBounds.x + ", " + roiBounds.y + ", " +  roiBounds.width + ", " +  roiBounds.height + ");\n";
			} else {
				
				macroRecording += "makeRectangle(" + roiBounds.x + ", " + roiBounds.y + ", " +  roiBounds.width + ", " +  roiBounds.height + ");\n";
			}
			
			macroRecording +=	"run(\"Create framed inset zoom\", \"magnification="+magnification
					+ " aspectratio=" + aspectRatio
					+ " roiangle=" + roiAngle
					+ " addframe=" + addFrame
					+ " addframetoinset=" + addFrameToInset
					+ " framewidth=" + frameWidth
					+ " framecolor=" + frameColor
					+ " startscalebarplugin=" + startScaleBarPlugin
					+ "\");\n";
					
			Recorder.recordString(macroRecording);
			
			InsetProcessor.createInset();
			
			
			addMetadata(inputImage, macroRecording);
			
			
			if (recorder != null) {
				Recorder.record = true;
				System.out.println("recording set = " + Recorder.record);
				
			}
			
			//this should just happen when used from the GUI
			if (startScaleBarPlugin) {
				new ScaleBar().run(null);
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
		prefs.put(getClass(), "startScaleBarPlugin", startScaleBarPlugin);
	}
	
	
	private void addMetadata(ImagePlus image, String recording) {
		
		String imageInfo = image.getInfoProperty();
		
		String newImageInfo = "";

		if (imageInfo == null) {
			newImageInfo = recording;
		} else {
			newImageInfo = imageInfo + System.lineSeparator() + recording;
			image.setProperty("Info", null);
		}
		
		image.setProperty("Info", newImageInfo);
		
	}
	
	
	
	
	@SuppressWarnings("unused")
	private void magnificationChanged() {
		inputImage = WindowManager.getCurrentImage();
		InsetProcessor.magnificationChanged();
		
		roiRotated();
		
	}
	
	private void roiRotated() {
		inputImage = WindowManager.getCurrentImage();
		InsetProcessor.roiRotated();
		
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
			roiAngle = Integer.parseInt(paramArray[2].substring(paramArray[2].indexOf("=")+1));
			addFrame = Boolean.parseBoolean(paramArray[3].substring(paramArray[3].indexOf("=")+1));
			addFrameToInset = Boolean.parseBoolean(paramArray[4].substring(paramArray[4].indexOf("=")+1));
			frameWidth = Integer.parseInt(paramArray[5].substring(paramArray[5].indexOf("=")+1));
			frameColor = new ColorRGB(paramArray[6].substring(paramArray[6].indexOf("=")+1));
//			if (paramArray.length < 8) {
//				startScaleBarPlugin = false;
//			} else {
//				startScaleBarPlugin = Boolean.parseBoolean(paramArray[7].substring(paramArray[7].indexOf("=")+1));				
//			}
			
			InsetProcessor.magnificationChanged();
			
			InsetProcessor.roiRotated();
			
			InsetProcessor.createInset();
			
		} else {
			System.out.println("run from GUI");
			InsetProcessor.magnificationChanged();
			InsetProcessor.roiRotated();
		}
	}

}
