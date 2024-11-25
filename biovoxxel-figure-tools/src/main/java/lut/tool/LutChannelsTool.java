package lut.tool;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;

import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.plugin.LutLoader;
import ij.process.ImageProcessor;
import ij.process.LUT;
import javax.swing.JSeparator;


@Plugin(type = Command.class, menuPath="Plugins>BioVoxxel Figure Tools>LUT Channels Tool")
public class LutChannelsTool extends JFrame implements Command, WindowListener, ActionListener {

	private static final long serialVersionUID = -8417843918812937098L;
	private JPanel contentPane;
	private JComboBox<String> comboBox;
	private static String IJ_LUT_FOLDER = IJ.getDirectory("luts");
    private static String RELATIVE_PATH_TO_LUT_FILE_FOLDER = Prefs.get("biovoxxel.lut.button.panel.lut.folder", "");
    private JButton btnCDV;
   	
	
	public ImagePlus currentImagePlus;
	private int maxNumberOfChannels = 8;
	private JCheckBox[] checkBoxGroup = new JCheckBox[maxNumberOfChannels];
	private CompositeImage ci;
	private JButton btnSplitChannels;
	private JButton btnMergeChanels;
	private JPopupMenu popupMenu;
	private JPanel panelChannelCheckboxes;
	private JSeparator separator;
	private JButton btnInvertLut;
	
	
	/**
	 * Launch the application.
	 */
	public void run() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {	
					
					Frame existingLUTChannelsTool = WindowManager.getFrame("LUT Channels Tool");
					
					if (existingLUTChannelsTool == null) {
						LutChannelsTool frame = new LutChannelsTool();
						frame.setVisible(true);
						WindowManager.addWindow(frame);						
					} else {
						existingLUTChannelsTool.toFront();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public LutChannelsTool() {
		setTitle("LUT Channels Tool");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		int x = (int) Math.round(Prefs.get("biovoxxel.lut.button.tool.x", 100));
		int y = (int) Math.round(Prefs.get("biovoxxel.lut.button.tool.y", 100));
		int width = (int) Math.round(Prefs.get("biovoxxel.lut.button.tool.width", 300));
		int height = (int) Math.round(Prefs.get("biovoxxel.lut.button.tool.height", 480));
		setBounds(x, y, width, height);
//		setBounds(100, 100, 300, 480);	//for Windowbuilder
		
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		
		popupMenu = new JPopupMenu();
		
		JMenuItem mntmGeneralLutMenu = new JMenuItem("IJ LUTs");
		mntmGeneralLutMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				RELATIVE_PATH_TO_LUT_FILE_FOLDER = IJ_LUT_FOLDER;
				
				setAlternativeLutPath();				
			}
		});
		popupMenu.add(mntmGeneralLutMenu);
		separator = new JSeparator();
		popupMenu.add(separator);
		
		Vector<File> lutFolderList = getFileList(IJ_LUT_FOLDER, null);
		
		for (int lutFolder = 0; lutFolder < lutFolderList.size(); lutFolder++) {
			
			if (lutFolderList.get(lutFolder).isDirectory()) {
				
				final String LUT_FILE_FOLDER = lutFolderList.get(lutFolder).getAbsolutePath();
				
				System.out.println(lutFolderList.get(lutFolder).getName());
				
				JMenuItem mntmAlternativeLutPath = new JMenuItem(lutFolderList.get(lutFolder).getName());
				mntmAlternativeLutPath.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						
						RELATIVE_PATH_TO_LUT_FILE_FOLDER = LUT_FILE_FOLDER;
						
						setAlternativeLutPath();				
					}
				});
				popupMenu.add(mntmAlternativeLutPath);
			}
			
		}
		addPopup(contentPane, popupMenu);
						
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0};
		contentPane.setLayout(gbl_contentPane);
		
		comboBox = new JComboBox<String>();
		comboBox.addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				int currentIndex = comboBox.getSelectedIndex();
				if (currentIndex + e.getWheelRotation() >= 0 && currentIndex + e.getWheelRotation() < comboBox.getModel().getSize() && currentIndex + e.getWheelRotation() >= 0) {
					comboBox.setSelectedIndex(currentIndex + e.getWheelRotation());
				}
			}
		});
		comboBox.setModel(new DefaultComboBoxModel<String>(new String[] {"Composite", "Composite Invert", "Color", "Grayscale"}));
		comboBox.setSelectedIndex(0);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.gridwidth = 4;
		gbc_comboBox.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 0;
		gbc_comboBox.gridy = 0;
		contentPane.add(comboBox, gbc_comboBox);
		
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				setupImage();
						
				if (ci != null) {
					
					String mode = comboBox.getSelectedItem().toString();
					
					switch (mode) {
					case "Composite":
						invertLUTs(IJ.COMPOSITE, "Sum");
						break;
						
					case "Composite Invert":
						invertLUTs(IJ.COMPOSITE, "Invert");
						break;
						
					case "Color":
						invertLUTs(IJ.COLOR, null);
						break;
						
					case "Grayscale":
						invertLUTs(IJ.GRAYSCALE, null);
						break;
						
					}				
				}
			}
		});
		
		btnInvertLut = new JButton("Invert");
		btnInvertLut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				invertSingleChannelLUT();
			}
		});
		GridBagConstraints gbc_btnInvertLut = new GridBagConstraints();
		gbc_btnInvertLut.gridwidth = 3;
		gbc_btnInvertLut.insets = new Insets(0, 0, 5, 5);
		gbc_btnInvertLut.gridx = 4;
		gbc_btnInvertLut.gridy = 0;
		contentPane.add(btnInvertLut, gbc_btnInvertLut);
			
		panelChannelCheckboxes = new JPanel();
		GridBagConstraints gbc_panelChannelCheckboxes = new GridBagConstraints();
		gbc_panelChannelCheckboxes.gridwidth = 7;
		gbc_panelChannelCheckboxes.insets = new Insets(0, 0, 5, 0);
		gbc_panelChannelCheckboxes.fill = GridBagConstraints.BOTH;
		gbc_panelChannelCheckboxes.gridx = 0;
		gbc_panelChannelCheckboxes.gridy = 1;
		contentPane.add(panelChannelCheckboxes, gbc_panelChannelCheckboxes);
		
		GridBagConstraints[] gbc_chckbx = new GridBagConstraints[maxNumberOfChannels];
		for (int cbg = 0; cbg < maxNumberOfChannels; cbg++) {
			checkBoxGroup[cbg] = new JCheckBox("C" + (cbg + 1));
			checkBoxGroup[cbg].setSelected(true);
			gbc_chckbx[cbg] = new GridBagConstraints();
			gbc_chckbx[cbg].insets = new Insets(0, 0, 5, 5);
			gbc_chckbx[cbg].gridx = cbg;
			gbc_chckbx[cbg].gridy = 1;
			panelChannelCheckboxes.add(checkBoxGroup[cbg], gbc_chckbx[cbg]);
			
			checkBoxGroup[cbg].addActionListener(this);
			
		}		
		
		JPanel buttonPanel = new JPanel();
		GridBagConstraints gbc_buttonPanel = new GridBagConstraints();
		gbc_buttonPanel.insets = new Insets(0, 0, 5, 0);
		gbc_buttonPanel.gridwidth = 7;
		gbc_buttonPanel.fill = GridBagConstraints.BOTH;
		gbc_buttonPanel.gridx = 0;
		gbc_buttonPanel.gridy = 2;
		contentPane.add(buttonPanel, gbc_buttonPanel);
		
		btnSplitChannels = new JButton("Split Channels");
		btnSplitChannels.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					IJ.run(currentImagePlus, "Split Channels", "");					
				} catch (RuntimeException rte) {
					
				}
			}
		});
		GridBagConstraints gbc_btnSplitChannels = new GridBagConstraints();
		gbc_btnSplitChannels.gridwidth = 2;
		gbc_btnSplitChannels.insets = new Insets(0, 0, 0, 5);
		gbc_btnSplitChannels.gridx = 0;
		gbc_btnSplitChannels.gridy = 3;
		contentPane.add(btnSplitChannels, gbc_btnSplitChannels);
		
		btnMergeChanels = new JButton("Merge Channels");
		btnMergeChanels.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					IJ.run(currentImagePlus, "Merge Channels...", "");				
				} catch (RuntimeException rte) {
					
				}
			}
		});
		GridBagConstraints gbc_btnMergeChanels = new GridBagConstraints();
		gbc_btnMergeChanels.gridwidth = 2;
		gbc_btnMergeChanels.insets = new Insets(0, 0, 0, 5);
		gbc_btnMergeChanels.gridx = 2;
		gbc_btnMergeChanels.gridy = 3;
		contentPane.add(btnMergeChanels, gbc_btnMergeChanels);
		
				
		btnCDV = new JButton("CDV Test");
		btnCDV.setToolTipText("Display the main color deficient vision simulation of the current image");
		btnCDV.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				performCdvTest();				
			}
		});
		GridBagConstraints gbc_btnCDV = new GridBagConstraints();
		gbc_btnCDV.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnCDV.gridwidth = 3;
		gbc_btnCDV.gridx = 4;
		gbc_btnCDV.gridy = 3;
		contentPane.add(btnCDV, gbc_btnCDV);
		
		ImagePlus lutButtonIcon = IJ.createImage("", "8-bit ramp", 64, 20, 1);
		
//		System.out.println(RELATIVE_PATH_TO_LUT_FILE_FOLDER);
		RELATIVE_PATH_TO_LUT_FILE_FOLDER = Prefs.get("biovoxxel.lut.button.panel.lut.folder", IJ_LUT_FOLDER + "LutButtonPanel" + File.separator);
		Vector<File> lutFiles = getFileList(RELATIVE_PATH_TO_LUT_FILE_FOLDER, ".lut");
		
		if (lutFiles == null || lutFiles.size() == 0) {
			JOptionPane.showMessageDialog(null, "Specified folder is not a folder or does not contain LUT files", "No LUTs detected", JOptionPane.ERROR_MESSAGE);
			lutFiles = getFileList(IJ_LUT_FOLDER + "LutButtonPanel" + File.separator, ".lut");
		}
		
		JButton[] lutButton = new JButton[lutFiles.size()];
		GridBagConstraints[] gbc_panelLUTButtons = new GridBagConstraints[lutFiles.size()];
		
		for(int b=0; b<lutFiles.size(); b++) {
			try {
				lutButton[b] = new JButton();
				File lutFile = lutFiles.get(b);
				
				lutButton[b].setName(lutFile.getName());
				lutButton[b].setToolTipText(lutFile.getName());
				
				LUT currentLUT = LutLoader.openLut(lutFile.getAbsolutePath());
				ImageProcessor newLutButtonIconProcessor = lutButtonIcon.getProcessor().duplicate();
				
				newLutButtonIconProcessor.setLut(currentLUT);
				newLutButtonIconProcessor.convertToColorProcessor();
				Image currentLUTImage = new ImagePlus("", newLutButtonIconProcessor).getImage();
				lutButton[b].setIcon(new ImageIcon(currentLUTImage));
			} catch (Exception e) {
				e.printStackTrace();
			}
			gbc_panelLUTButtons[b] = new GridBagConstraints();
			gbc_panelLUTButtons[b].fill = GridBagConstraints.HORIZONTAL;
			gbc_panelLUTButtons[b].insets = new Insets(0, 0, 0, 5);
			gbc_panelLUTButtons[b].gridx = (int)Math.floor(b%3);;
			gbc_panelLUTButtons[b].gridy = (int)Math.floor(b/3);;
			buttonPanel.add(lutButton[b], gbc_panelLUTButtons[b]);
			
			lutButton[b].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						applyLutToImage(RELATIVE_PATH_TO_LUT_FILE_FOLDER + ((JButton)e.getSource()).getName());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			});
			
			addWindowListener(this);
		}
	}


	protected void invertSingleChannelLUT() {
		
		IJ.run(currentImagePlus, "Invert LUT", "");
	}

	protected void setAlternativeLutPath() {
//		try {
//			RELATIVE_PATH_TO_LUT_FILE_FOLDER = JOptionPane.showInputDialog(null, "LUT Directory", RELATIVE_PATH_TO_LUT_FILE_FOLDER).trim();
//		} catch (NullPointerException e) {
//			
//		}
								
		if (RELATIVE_PATH_TO_LUT_FILE_FOLDER.equals("")) {
			RELATIVE_PATH_TO_LUT_FILE_FOLDER = IJ_LUT_FOLDER + "LutButtonPanel" + File.separator;
		} 
		
		if (!RELATIVE_PATH_TO_LUT_FILE_FOLDER.endsWith(File.separator)) {
			RELATIVE_PATH_TO_LUT_FILE_FOLDER += File.separator;
		}
		
		if (!new File(RELATIVE_PATH_TO_LUT_FILE_FOLDER).isDirectory()) {
			JOptionPane.showMessageDialog(null, "Path does not point to a folder", "Wrong Path", JOptionPane.ERROR_MESSAGE);
			RELATIVE_PATH_TO_LUT_FILE_FOLDER = IJ_LUT_FOLDER + "LutButtonPanel" + File.separator;
		}
		
		System.out.println("RELATIVE_PATH_TO_LUT_FILE_FOLDER = " + RELATIVE_PATH_TO_LUT_FILE_FOLDER);
		Prefs.set("biovoxxel.lut.button.panel.lut.folder", RELATIVE_PATH_TO_LUT_FILE_FOLDER);
		
		dispose();
		run();
	}

	protected void performCdvTest() {
		
		IJ.runMacro("setBatchMode(true);\r\n"
				+ "originalImageName = getTitle();\r\n"
				+ "if (bitDepth() != 24) {\r\n"
				+ "	run(\"Flatten\", \"slice\");\r\n"
				+ "}\r\n"
				+ "currentImageID = getImageID();\r\n"
				+ "currentImageName = getTitle();\r\n"
				+ "selectImage(currentImageID);\r\n"
				+ "run(\"Duplicate...\", \"title=[CDV_Protanopia_(no_red)_\"+currentImageName+\"]\");\r\n"
				+ "run(\"Simulate Color Blindness\", \"mode=[Protanopia (no red)]\");\r\n"
				+ "selectImage(currentImageID);\r\n"
				+ "run(\"Duplicate...\", \"title=[CDV_Deuteranopia_(no_green)_\"+currentImageName+\"]\");\r\n"
				+ "run(\"Simulate Color Blindness\", \"mode=[Deuteranopia (no green)]\");\r\n"
				+ "selectImage(currentImageID);\r\n"
				+ "run(\"Duplicate...\", \"title=[CDV_Tritanopia_(no_blue)_\"+currentImageName+\"]\");\r\n"
				+ "run(\"Simulate Color Blindness\", \"mode=[Tritanopia (no blue)]\");\r\n"
				+ "if (bitDepth() != 24) {\r\n"
				+ "	close(currentImageName);\r\n"
				+ "}\r\n"
				+ "run(\"Images to Stack\", \"  title=CDV_ use\");\r\n"
				+ "rename(\"CDV_\" + originalImageName);\r\n"
				+ "setBatchMode(false);\r\n");
			
	
	}

	/**
	 * 
	 * @param path
	 * @param fileEnding can be null, this returns the complete file list, otherwise only files with the specified ending
	 * @return Vector<File>
	 */
	private static Vector<File> getFileList(String path, String fileEnding) {
		
		File folder = new File(path);
//		System.out.println(folder.getAbsolutePath());
		File[] fileList = folder.listFiles();
//		System.out.println("fileList = " + fileList);
		Vector<File> specificFileList = new Vector<File>();
		
		if (fileList != null) {
			if (fileEnding != null) {
				for (int f = 0; f < fileList.length; f++) {
					
					if (fileList[f].getAbsolutePath().endsWith(fileEnding)) {
						specificFileList.add(fileList[f]);
					}
				}			
			} else {
				for (int f = 0; f < fileList.length; f++) {
					specificFileList.add(fileList[f]);
				}
			}			
		}
		
	
		return specificFileList;
	}
	
	private void invertLUTs(int mode, String modeString) {
				
		String compImpProp = ci.getProp("CompositeProjection");
		int activeChannel = currentImagePlus.getC(); 
		
		if (modeString != null && !modeString.equals("Sum") && compImpProp==null) {
			ci.setProp("CompositeProjection", modeString);
		} 
		
		if (mode==IJ.COMPOSITE && (("Min".equals(modeString)||"Invert".equals(modeString)) && !currentImagePlus.isInvertedLut()) 
			|| ("Max".equals(modeString)||"Sum".equals(modeString)) && currentImagePlus.isInvertedLut()) {
			
			//workaround for the not error-free implementation in Java
			System.out.println("Inverting all LUTs");
			IJ.runMacro("// \"Invert all LUTs\"\r\n"
					+ "// Converts all LUTs in a multi-channel image between\r\n"
					+ "// inverted and non inverted and switches the\r\n"
					+ "// composite rendering mode accordingly.\r\n"
					+ "// Author: Kevin Terretaz\r\n"
					+ "// Contributor: Nicolás De Francesco\r\n"
					+ "\r\n"
					+ "requires(\"1.53o\");\r\n"
					+ "getDimensions(width, height, channels, slices, frames);\r\n"
					+ "REDS = newArray(256);\r\n"
					+ "GREENS = newArray(256);\r\n"
					+ "BLUES = newArray(256);\r\n"
					+ "for (c=1; c<=channels; c++) {\r\n"
					+ "    Stack.setChannel(c);\r\n"
					+ "    getLut(reds,greens,blues);\r\n"
					+ "    for (i=0; i<256; i++) {\r\n"
					+ "        hsv = HSV_from_RGB(255-reds[i], 255-greens[i], 255-blues[i]);\r\n"
					+ "        hsv[0] = (hsv[0]+128)%256;\r\n"
					+ "        rgb = RGB_from_HSV(hsv[0], hsv[1], hsv[2]);\r\n"
					+ "        REDS[i] = round(rgb[0]);\r\n"
					+ "        GREENS[i] = round(rgb[1]);\r\n"
					+ "        BLUES[i] = round(rgb[2]);\r\n"
					+ "    }\r\n"
					+ "    setLut(REDS, GREENS, BLUES);\r\n"
					+ "}\r\n"
					+ "\r\n"
					+ "Stack.setChannel(1);\r\n"
					+ "  // CompositeProjection mode switch :\r\n"
					+ "  mode = Property.get(\"CompositeProjection\");\r\n"
					+ "  if (is(\"Inverting LUT\")&&(mode==\"Min\"||mode==\"Invert\"))\r\n"
					+ "     ; // do nothing\r\n"
					+ "  else if (!is(\"Inverting LUT\")&&(mode==\"Sum\"||mode==\"Max\"))\r\n"
					+ "     ; // do nothing\r\n"
					+ "  else if (mode==\"Invert\")\r\n"
					+ "     Property.set(\"CompositeProjection\", \"Sum\");\r\n"
					+ "  else if (mode==\"Min\")\r\n"
					+ "     Property.set(\"CompositeProjection\", \"Max\");\r\n"
					+ "  else if (mode==\"Max\")\r\n"
					+ "     Property.set(\"CompositeProjection\", \"Min\");\r\n"
					+ "  else // if Composite Sum\r\n"
					+ "     Property.set(\"CompositeProjection\", \"Invert\"); \r\n"
					+ "  updateDisplay();\r\n"
					+ "\r\n"
					+ "function HSV_from_RGB(r, g, b) {\r\n"
					+ "    // adapted from https://en.wikipedia.org/wiki/HSL_and_HSV#From_RGB\r\n"
					+ "    v = maxOf(r, maxOf(g, b)); // value (brightness)\r\n"
					+ "    c = v - minOf(r, minOf(g, b)); // chroma    \r\n"
					+ "    h = 0;\r\n"
					+ "    if (c>0) {\r\n"
					+ "        if (v==r) h = 256/6 * (0 + g-b)/c;\r\n"
					+ "        if (v==g) h = 256/6 * (2 + (b-r)/c);\r\n"
					+ "        if (v==b) h = 256/6 * (4 + (r-g)/c);\r\n"
					+ "    }\r\n"
					+ "    h = (h+256)%256;\r\n"
					+ "    s = 0;\r\n"
					+ "    if (v>0) s = c/v*256;\r\n"
					+ "    hsv = newArray(h, s, v);\r\n"
					+ "    return hsv;\r\n"
					+ "}   \r\n"
					+ "    \r\n"
					+ "function RGB_from_HSV(h, s, v) {\r\n"
					+ "    // adapted from https://en.wikipedia.org/wiki/HSL_and_HSV#HSV_to_RGB_alternative\r\n"
					+ "    r = v - v * s / 256 * maxOf(0, minOf((5+h/256*6)%6, minOf(4-(5+h/256*6)%6, 1)));\r\n"
					+ "    g = v - v * s / 256 * maxOf(0, minOf((3+h/256*6)%6, minOf(4-(3+h/256*6)%6, 1)));\r\n"
					+ "    b = v - v * s / 256 * maxOf(0, minOf((1+h/256*6)%6, minOf(4-(1+h/256*6)%6, 1)));\r\n"
					+ "    rgb = newArray(r, g, b);\r\n"
					+ "    return rgb;\r\n"
					+ "}");
			
// The implementation of the upper macro in Java does not yet work error-free
			
//			byte[] old_reds = new byte[256];
//			byte[] old_greens = new byte[256];
//			byte[] old_blues = new byte[256];
//			
//			byte[] new_reds = new byte[256];
//			byte[] new_greens = new byte[256];
//			byte[] new_blues = new byte[256];
//				
//			int channelCount = currentImagePlus.getNChannels();
//			
//			for (int c = 1; c <= channelCount; c++) {
//				currentImagePlus.setC(c);
//				LUT currentLUT = currentImagePlus.getProcessor().getLut();
//				System.out.println(currentLUT);
//				System.out.println("currentLUT is inverted = " + currentImagePlus.isInvertedLut());
//				
//				currentLUT.getReds(old_reds);
//				currentLUT.getGreens(old_greens);
//				currentLUT.getBlues(old_blues);
//				
//				for (int i = 0; i < 256; i++) {
//					double[] HSV = HSVFromRGB(255-old_reds[i], 255-old_greens[i], 255-old_blues[i]);
//					
//					HSV[0] = (HSV[0] + 128d) % 256d;
//					
//					double[] RGB = RGBFromHSV(HSV[0], HSV[1], HSV[2]);
//					
//					new_reds[i] = (byte)Math.round(RGB[0]);
//					new_greens[i] = (byte)Math.round(RGB[1]);
//					new_blues[i] = (byte)Math.round(RGB[2]);
//				}
//				LUT invertedLUT = new LUT(new_reds, new_greens, new_blues);
//				currentImagePlus.getChannelProcessor().setLut(invertedLUT);
//				System.out.println("New LUT is inverted = " + currentImagePlus.isInvertedLut());
//			}
//			
//			
////			ci.setC(1);
////			
//			// CompositeProjection mode switch :
//			compImpProp = ci.getProp("CompositeProjection");
//			if (currentImagePlus.isInvertedLut() && (compImpProp.equals("Min") || compImpProp.equals("Invert"))) {
//			  //do nothing 
//			} else if (!currentImagePlus.isInvertedLut() && (compImpProp.equals("Sum") || compImpProp.equals("Max"))) {
//				// do nothing
//			} else if (compImpProp.equals("Invert")) {
//				ci.setProp("CompositeProjection", "Sum");
//			} else if (compImpProp.equals("Min")) {
//				ci.setProp("CompositeProjection", "Max");
//			} else if (compImpProp.equals("Max")) {
//				ci.setProp("CompositeProjection", "Min");
//			} else {
//				// if Composite Sum
//				ci.setProp("CompositeProjection", "Invert");
//			}
		} 
		
		ci.setMode(mode);
		ci.updateAndDraw();		
		currentImagePlus.setC(activeChannel);
	}
	

//	private double[] HSVFromRGB(double r, double g, double b) {
//		
//		double value = Math.max(r, Math.max(g, b));
//		double chroma = value - Math.min(r, Math.min(g, b));
//		double hue = 0;
//		
//		if (chroma > 0) {
//			if (value == r) hue = 256 / 6 * (0 + g - b) / chroma;
//			if (value == g) hue = 256 / 6 * (2 + (b - r) / chroma);
//			if (value == b) hue = 256 / 6 * (4 + (r - g) / chroma);
//		}
//		
//		hue = (hue + 256) % 256;
//		
//		double saturation = 0;
//		if (value > 0) saturation = chroma / value * 256;
//		
//		return new double[] {hue, saturation, value};
//	}
//	
//	private double[] RGBFromHSV(double h, double s, double v) {
//		
//		double red = v - v * s / 256 * Math.max(0, Math.min((5+h/256*6)%6, Math.min(4-(5+h/256*6)%6, 1)));
//		double green = v - v * s / 256 * Math.max(0, Math.min((3+h/256*6)%6, Math.min(4-(3+h/256*6)%6, 1)));
//		double blue = v - v * s / 256 * Math.max(0, Math.min((1+h/256*6)%6, Math.min(4-(1+h/256*6)%6, 1)));
//		
//		return new double[] {red, green, blue};
//	}
	

	
	
	
	private void applyLutToImage(String path) {
		
		if (currentImagePlus == null && WindowManager.getCurrentImage() == null) {
			JOptionPane.showMessageDialog(null, "No open image detected", "No Image", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		if (currentImagePlus.isRGB()) {
			JOptionPane.showMessageDialog(null, "LUTs cannot be applied on RGB images", "RGB not supported", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		ImageProcessor ip = null;
		if (currentImagePlus != null && currentImagePlus.isComposite()) {
			ip = currentImagePlus.getChannelProcessor();
		} else {
			ip = currentImagePlus.getProcessor();			
		}
//		System.out.println("current image processor = " + ip);
//		System.out.println("current LUT = " + ip.getLut());
		
		
		if (currentImagePlus != null) {
			try {
				File file = new File(path);
//				System.out.println(file.getAbsolutePath());
				
				LUT activeLUT = LutLoader.openLut(file.getAbsolutePath());
				
				if (currentImagePlus.isComposite() && ci != null) {
					ci.setChannelLut(activeLUT);
				} else {
					ip.setLut(activeLUT);					
				}
//				System.out.println("apply " + activeLUT);
								
				currentImagePlus.updateChannelAndDraw();
								
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
	}

	@Override
	public void windowOpened(WindowEvent e) {}

	@Override
	public void windowClosing(WindowEvent e) {
		storeAndCleanUp();
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		storeAndCleanUp();
	}
	
	
	private void storeAndCleanUp() {
		Rectangle frameBounds = getBounds();
		
		Prefs.set("biovoxxel.lut.button.tool.x", frameBounds.x);
		Prefs.set("biovoxxel.lut.button.tool.y", frameBounds.y);
		Prefs.set("biovoxxel.lut.button.tool.width", frameBounds.width);
		Prefs.set("biovoxxel.lut.button.tool.height", frameBounds.height);
		
		WindowManager.removeWindow(this);
	}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {
		setupImage();
		setupCheckboxes();
	}

	@Override
	public void windowActivated(WindowEvent e) {
		setupImage();
		setupCheckboxes();
	}

	@Override
	public void windowDeactivated(WindowEvent e) {}
	
	
	private void setupImage() {
		currentImagePlus = WindowManager.getCurrentImage();
		
		if(currentImagePlus != null && currentImagePlus.isComposite()) {	
			ci = (CompositeImage) currentImagePlus;
		} else {
			ci = null;
		}
		
		
	}

	private void setupCheckboxes() {
		if (currentImagePlus != null) {
			int channelCount = currentImagePlus.getNChannels();
			
			boolean[] activeChannels = null;
			
			if (currentImagePlus.isComposite() && ci != null) {
				activeChannels = ci.getActiveChannels();
			}
			
			for (int c = 0; c < maxNumberOfChannels ; c++) {
				if (c < channelCount) {
					checkBoxGroup[c].setEnabled(true);
					if (activeChannels != null) {
						checkBoxGroup[c].setSelected(activeChannels[c]);
					}
				} else {
					checkBoxGroup[c].setEnabled(false);
				}
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
				
		if (source instanceof JCheckBox && ci != null) {
			
			boolean[] activeChannels = ci.getActiveChannels();
			
			JCheckBox checkbox = (JCheckBox) source;
			
			for (int i=0; i<checkBoxGroup.length; i++) {
												
				if (checkbox == checkBoxGroup[i]) {
					
					if (ci.getMode()==IJ.COMPOSITE && checkbox == checkBoxGroup[i]) {
												
						activeChannels[i] = checkbox.isSelected();
												
					} else if (ci.getMode() != IJ.COMPOSITE && checkbox == checkBoxGroup[i]) {
						
						checkBoxGroup[i].setSelected(true);
						currentImagePlus.setPosition(i+1, currentImagePlus.getSlice(), currentImagePlus.getFrame());
						
					} 
					
					ci.updateAndDraw();
					
				}
			}
			String activeChannelString = "";
			for (int a = 0; a < activeChannels.length; a++) {
				if (activeChannels[a]) {
					activeChannelString += 1;					
				} else {
					activeChannelString += 0;	
				}
			}
//			System.out.println(activeChannelString);
			ci.setActiveChannels(activeChannelString);
		}
	}
	
	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
}
