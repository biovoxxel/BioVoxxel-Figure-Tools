package dimension.labeler;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.List;

import org.scijava.command.Command;
import org.scijava.command.DynamicCommand;
import org.scijava.command.Interactive;
import org.scijava.module.MutableModuleItem;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.prefs.PrefService;
import org.scijava.util.ColorRGB;
import org.scijava.widget.Button;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.TextRoi;

@Plugin(type = Command.class, menuPath="Plugins>BioVoxxel Figure Tools>Dimension Labeler")
public class DimensionLabeler extends DynamicCommand implements Interactive {
	
	@Parameter
	private PrefService prefs;
	
	@Parameter
	ImagePlus inputImp;
	
	@Parameter (label = "Label dimension", choices = {"Time", "Volume", "Channel"}, callback = "updatePreview")
	String labelDimension = "Time";
	
	@Parameter (label = "Keep former label", callback = "setLabelRetention", description = ""
			+ "If you want to also keep the current label and add a new one, deactivate and reactivate this checkbox")
	Boolean keepFormerStamp = false;
	
	@Parameter (label = "Start", callback = "updatePreview")
	Integer stamperStart = 0;
	
	@Parameter (label = "Step", callback = "updatePreview")
	Integer stamperStep = 1;
	
	@Parameter (label = "Format", choices = {"#0", "00:00", "00:00:00", "Text"}, callback = "updatePreview", description = ""
			+ "If Text is chosen, only the fields Prefix and Suffix are used and combined")
	String stamperFormat = "00:00";	
	
	@Parameter (label = "Prefix", callback = "updatePreview")
	String prefix = "";
	
	@Parameter (label = "Suffix", callback = "updatePreview")
	String suffix = " min";
	
	@Parameter (label = "Font", choices = {"Arial"}, initializer = "setFontList", callback = "updatePreview")
	String fontType = "Arial";
	
	@Parameter (label = "Font style", choices = {"Normal", "Bold", "Italic", "Bold-Italic"}, callback = "updatePreview")
	String fontStyle = "Normal";
	
	@Parameter (label = "Font size", min = "1", callback="updatePreview")
	Integer fontSize = 12;
	
	@Parameter (label = "X location", min = "0", callback = "updatePreview")
	Integer xStart = 10;
	
	@Parameter (label = "Y location", min = "0", callback = "updatePreview")
	Integer yStart = 10;
	
	@Parameter (label = "Position", choices = {"Top-left", "Bottom-left", "Top-right", "Bottom-right"}, callback = "updatePreview")
	String stampPosition = "Top-left";
	
	@Parameter (label = "Text color", callback = "updatePreview")
	ColorRGB textColor = new ColorRGB(255, 255, 255);
	
	@Parameter (label = "Show background", callback = "updatePreview")
	Boolean showStamperBackground = false;
	
	@Parameter (label = "Background color", callback = "updatePreview")
	ColorRGB backgroundColor = new ColorRGB(0, 0, 0);
	
	@Parameter (label = "remove stamp", callback = "removeTimeStamp", required = false)
	Button removeStampButton = null;
	
	
	
	public void inputImageSwap() {
		inputImp = WindowManager.getCurrentImage();
	}
	
	public void updatePreview() {
		
		inputImageSwap();
		inputImp.setOverlay(getStamperOverlay());
		inputImp.updateAndDraw();
		setPreferences();
	}
	
	
	@SuppressWarnings("unused")
	private void setFontList() {
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] fonts = ge.getAvailableFontFamilyNames();
		
		List<String> fontList = Arrays.asList(fonts);
		
		final MutableModuleItem<String> mutableFontType = getInfo().getMutableInput("fontType", String.class);
		
		mutableFontType.setChoices(fontList);
		
	}
	
	
	private Font getFont() {
		
		int style = Font.PLAIN;
		switch (fontStyle) {
		case "Bold":
			style = Font.BOLD;
			break;
		case "Italic":
			style = Font.ITALIC;
			break;
		case "Bold-Italic":
			style = Font.BOLD | Font.ITALIC;
			break;	
		default:
			style = Font.PLAIN;
			break;
		}
		
		return new Font(fontType, style, fontSize);
	}
	
	
	
	public Overlay getStamperOverlay() {
		
		removeTimeStamp();
		
		Overlay overlay = inputImp.getOverlay();
			
		for (int s = 1; s <= inputImp.getNSlices(); s++) {
			for (int c = 1; c <= inputImp.getNChannels(); c++) {
				for (int f = 1; f <= inputImp.getNFrames(); f++) {	
						
					String text = "";
					switch (labelDimension) {
					case "Time":
						text = getStampText(f);
						break;
					case "Volume":
						text = getStampText(s);
						break;
					case "Channel":
						text = getStampText(c);
						break;
					default:
						break;
					}
					
					TextRoi stamperRoi = new TextRoi(text, xStart, yStart, getFont());
					
					stamperRoi.setName("|TIME_STAMP|");
					stamperRoi.setPosition(c, s, f);
					stamperRoi.setStrokeColor(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue()));

					if (showStamperBackground) {
						stamperRoi.setFillColor(new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue()));						
					} else {
						stamperRoi.setFillColor(null);
					}
					
					switch (stampPosition) {
					case "Top-left":
						stamperRoi.setLocation(xStart, yStart);
						break;
						
					case "Bottom-left":
						stamperRoi.setLocation(xStart, inputImp.getHeight() - stamperRoi.getBounds().height - yStart);
						break;
						
					case "Top-right":
						stamperRoi.setLocation(inputImp.getWidth() - stamperRoi.getBounds().width - xStart, yStart);
						break;
					case "Bottom-right":
						stamperRoi.setLocation(inputImp.getWidth() - stamperRoi.getBounds().width - xStart, inputImp.getHeight() - stamperRoi.getBounds().height - yStart);
						break;

					default:
						break;
					}

					overlay.add(stamperRoi);
				}			
			}
		}
		
		return overlay;
	}
	
	public void removeTimeStamp() {
		inputImageSwap();
		Overlay oldOverlay = inputImp.getOverlay();
		Overlay newOverlay = new Overlay();
		
		if (oldOverlay != null) {
			for (Roi roi : oldOverlay) {
				if (roi.getName() == null || !roi.getName().equals("|TIME_STAMP|")) {
					newOverlay.add(roi);
				}
			}
		}
		inputImp.setOverlay(newOverlay);
	}
	
	
	public void setLabelRetention() {
		
		Overlay oldOverlay = inputImp.getOverlay();
		
		if (oldOverlay != null) {
			for (Roi roi : oldOverlay) {
				if (roi.getName() != null && roi.getName().equals("|TIME_STAMP|") && keepFormerStamp) {
					roi.setName("|PROTECTED_TIME_STAMP|");
				} else if (roi.getName() != null && roi.getName().equals("|PROTECTED_TIME_STAMP|") && !keepFormerStamp) {
					roi.setName("|TIME_STAMP|");
				}
				System.out.println();
			}
		}
		
	}
	
	
	public String getStampText(int i) {
		
		int counter = stamperStart + (stamperStep * (i - 1));
		
		String str = IJ.pad(Math.abs((int)Math.floor(counter/3600)), 2) + ":" + IJ.pad(Math.abs((int)Math.floor((counter/60)%60)), 2) + ":" + IJ.pad(Math.abs(counter%60), 2);
						
		switch (stamperFormat) {
		case "#0":
			int size = inputImp.getNFrames() + stamperStart;
			switch (labelDimension) {
			case "Volume":
				size = inputImp.getNSlices() + stamperStart;
				break;
			case "Channel":
				size = inputImp.getNChannels() + stamperStart;
				break;
			default:
				break;
			}
			int padUpTo = 1;
			
			if (size>=10) padUpTo = 2;
			if (size>=100) padUpTo = 3;
			if (size>=1000) padUpTo = 4;
			if (size>=10000) padUpTo = 5;
			
			
			str = IJ.pad(Math.abs(counter), padUpTo);
			break;
		
		case "00:00":
			str = str.substring(3);
			break;
		
		case "Text":
			str = "";
			break;

		default:
			break;
		}
		
		if (!stamperFormat.equals("Text")) {
			if (counter < 0) {
				str = "-" + str;
			} else if (stamperStart < 0 && counter >= 0) {
				str = " " + str;
			}
		}
		
		return prefix + str + suffix;
		
	}
	
	public void isCancel() {
		removeTimeStamp();
	}
	
	public void run() {
		setLabelRetention();
		updatePreview();
	}
	
	public void setPreferences() {
		prefs.put(getClass(), "labelDimension", labelDimension);
		prefs.put(getClass(), "keepFormerStamp", keepFormerStamp);
		prefs.put(getClass(), "stamperStart", stamperStart);
		prefs.put(getClass(), "stamperStep", stamperStep);
		prefs.put(getClass(), "stamperFormat", stamperFormat);
		prefs.put(getClass(), "prefix", prefix);
		prefs.put(getClass(), "suffix", suffix);
		prefs.put(getClass(), "fontType", fontType);
		prefs.put(getClass(), "fontStyle", fontStyle);
		prefs.put(getClass(), "fontSize", fontSize);
		prefs.put(getClass(), "xStart", xStart);
		prefs.put(getClass(), "yStart", yStart);
		prefs.put(getClass(), "stampPosition", stampPosition);
		prefs.put(getClass(), "textColor", textColor.toString());
		prefs.put(getClass(), "showStamperBackground", showStamperBackground);
		prefs.put(getClass(), "backgroundColor", backgroundColor.toString());
	}
	

}

