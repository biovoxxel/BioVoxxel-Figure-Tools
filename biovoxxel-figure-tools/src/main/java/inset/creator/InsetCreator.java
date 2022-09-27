package inset.creator;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.scijava.plugin.Parameter;

import ij.ImagePlus;


public class InsetCreator extends JFrame {

	
	@Parameter
	ImagePlus inputImage;
	
	private static final long serialVersionUID = -635607259313890639L;
	private static InsetCreator INSET_CREATOR;
	
	private JPanel contentPane;

	private static JRadioButton rdbtnAspectRatio;
	private static JRadioButton rdbtnSquare;
	private static JRadioButton rdbtnCentered;
	private static ButtonGroup radioButtonGroup;
		
	private static JSpinner spinnerMagnification;
	
	private static JCheckBox chckbxAddFrame;
	private static JSpinner spinnerInsetFrameWidth;
	private static JComboBox<String> comboBoxFrameColor;
	
	private static ImagePlus IMAGE_PLUS;
	public static int IMAGE_WIDTH;
	public static int IMAGE_HEIGHT;
	private static JComboBox<String> comboBoxPreserve;

	

	/**
	 * Create the frame.
	 */
	public InsetCreator(final ImagePlus imagePlus) {
		
		setup(imagePlus);
		setTitle("Inset Creator");
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 272, 191);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{0, 0, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 1.0, 1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		spinnerMagnification = new JSpinner();
		spinnerMagnification.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				
				InsetProcessor.magnificationChanged();
				
			}
		});
		
		JLabel lblMagnification = new JLabel("fold magnification");
		GridBagConstraints gbc_lblMagnification = new GridBagConstraints();
		gbc_lblMagnification.anchor = GridBagConstraints.WEST;
		gbc_lblMagnification.insets = new Insets(0, 0, 5, 5);
		gbc_lblMagnification.gridx = 0;
		gbc_lblMagnification.gridy = 0;
		contentPane.add(lblMagnification, gbc_lblMagnification);
		spinnerMagnification.setModel(new SpinnerNumberModel(2, 1, 100, 1));
		GridBagConstraints gbc_spinnerMagnification = new GridBagConstraints();
		gbc_spinnerMagnification.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinnerMagnification.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerMagnification.gridx = 0;
		gbc_spinnerMagnification.gridy = 1;
		contentPane.add(spinnerMagnification, gbc_spinnerMagnification);
		
		chckbxAddFrame = new JCheckBox("Add frame");
		chckbxAddFrame.setSelected(true);
		chckbxAddFrame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				insetFrameOptionChanged();
				
			}
		});
		GridBagConstraints gbc_chckbxMakeFrame = new GridBagConstraints();
		gbc_chckbxMakeFrame.anchor = GridBagConstraints.WEST;
		gbc_chckbxMakeFrame.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxMakeFrame.gridx = 0;
		gbc_chckbxMakeFrame.gridy = 2;
		contentPane.add(chckbxAddFrame, gbc_chckbxMakeFrame);
		
		
		comboBoxPreserve = new JComboBox<String>();
		comboBoxPreserve.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				InsetProcessor.magnificationChanged();
				
			}
		});
		comboBoxPreserve.setModel(new DefaultComboBoxModel<String>(new String[] {"Image size", "Square height", "Square width"}));
		comboBoxPreserve.setSelectedIndex(0);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 1;
		contentPane.add(comboBoxPreserve, gbc_comboBox);
				
		
		spinnerInsetFrameWidth = new JSpinner();
		spinnerInsetFrameWidth.setModel(new SpinnerNumberModel(new Integer(3), new Integer(1), null, new Integer(1)));
		GridBagConstraints gbc_spinnerInsetFrameWidth = new GridBagConstraints();
		gbc_spinnerInsetFrameWidth.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinnerInsetFrameWidth.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerInsetFrameWidth.gridx = 0;
		gbc_spinnerInsetFrameWidth.gridy = 3;
		contentPane.add(spinnerInsetFrameWidth, gbc_spinnerInsetFrameWidth);
		
		JLabel lblFrameWidth = new JLabel("px frame width");
		GridBagConstraints gbc_lblFrameWidth = new GridBagConstraints();
		gbc_lblFrameWidth.anchor = GridBagConstraints.WEST;
		gbc_lblFrameWidth.insets = new Insets(0, 0, 5, 0);
		gbc_lblFrameWidth.gridx = 1;
		gbc_lblFrameWidth.gridy = 3;
		contentPane.add(lblFrameWidth, gbc_lblFrameWidth);
		
		
		comboBoxFrameColor = new JComboBox<String>();
		comboBoxFrameColor.setModel(new DefaultComboBoxModel<String>(new String[] {"White", "Black", "Light Gray", "Gray", "Dark Gray", "Red", "Green", "Blue", "Cyan", "Magenta", "Yellow"}));
		GridBagConstraints gbc_comboBoxFrameColor = new GridBagConstraints();
		gbc_comboBoxFrameColor.insets = new Insets(0, 0, 5, 5);
		gbc_comboBoxFrameColor.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxFrameColor.gridx = 0;
		gbc_comboBoxFrameColor.gridy = 4;
		contentPane.add(comboBoxFrameColor, gbc_comboBoxFrameColor);
		
		
		JButton btnAddImage = new JButton("Create");
		btnAddImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				InsetProcessor.createInset();
				INSET_CREATOR = null;
				dispose();
				
			}
		});
		GridBagConstraints gbc_btnAddImage = new GridBagConstraints();
		gbc_btnAddImage.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnAddImage.insets = new Insets(0, 0, 0, 5);
		gbc_btnAddImage.gridx = 0;
		gbc_btnAddImage.gridy = 5;
		contentPane.add(btnAddImage, gbc_btnAddImage);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				INSET_CREATOR = null;
				imagePlus.killRoi();
				dispose();
				
			}
		});
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnCancel.gridx = 1;
		gbc_btnCancel.gridy = 5;
		contentPane.add(btnCancel, gbc_btnCancel);

		
		InsetProcessor.magnificationChanged();
		INSET_CREATOR = this;
		
		setupPreserveButtonGroup();
	}

	private void setup(ImagePlus imagePlus) {
		IMAGE_PLUS = imagePlus;
		IMAGE_WIDTH = imagePlus.getWidth();
		IMAGE_HEIGHT = imagePlus.getHeight();
	}
	
	
	public static ImagePlus getImage() {
		return IMAGE_PLUS;
	}

	public void setupPreserveButtonGroup() {
		radioButtonGroup = new ButtonGroup();
		radioButtonGroup.add(rdbtnAspectRatio);
		radioButtonGroup.add(rdbtnSquare);
		radioButtonGroup.add(rdbtnCentered);
	}
	
	private static void insetFrameOptionChanged() {
		if (addFrame()) {
			spinnerInsetFrameWidth.setEnabled(true);
			comboBoxFrameColor.setEnabled(true);
		} else {
			spinnerInsetFrameWidth.setEnabled(false);
			comboBoxFrameColor.setEnabled(false);
		}
		
	}
	
	
	
	
	private static boolean addFrame() {
		return chckbxAddFrame.isSelected();
	}
	
	
	
	public static int getPreserveSelection() {
		return comboBoxPreserve.getSelectedIndex();
	}
	
	
	
	public static Color getFrameColor() {
		return getColor(comboBoxFrameColor.getSelectedIndex());
	}
	
	
	
	public static int getFrameWidth() {
		return Integer.parseInt(spinnerInsetFrameWidth.getValue().toString());
	}
	
	

	public static ButtonGroup getPreserveButtonGroup() {
		return radioButtonGroup;
	}
	
	
	
	public static int getMagnification() {
		return Integer.parseInt(spinnerMagnification.getValue().toString());
	}
	
	
	
		
	public static boolean addFrameToInset() {
		return chckbxAddFrame.isSelected();
	}

	
	
	private static Color getColor(int comboBoxIndex) {
		
		System.out.println("Selected color index: " + comboBoxIndex);
		
		Color color;
		
		switch (comboBoxIndex) {
		case 0:
			color = Color.WHITE;
			break;
		case 1:
			color = Color.BLACK;
			break;
		case 2:
			color = Color.LIGHT_GRAY;
			break;
		case 3:
			color = Color.GRAY;
			break;
		case 4:
			color = Color.DARK_GRAY;
			break;
		case 5:
			color = Color.RED;
			break;
		case 6:
			color = Color.GREEN;
			break;
		case 7:
			color = Color.BLUE;
			break;
		case 8:
			color = Color.CYAN;
			break;
		case 9:
			color = Color.MAGENTA;
			break;
		case 10:
			color = Color.YELLOW;
			break;

		default:
			color = Color.WHITE;
			break;
		}
		
		return color;
	}

	

	public static InsetCreator getInstance() {
		return INSET_CREATOR;
	}
	
	public static Boolean isAddFrameActive() {
		return chckbxAddFrame.isSelected();
	}

}
