package metadataRecorder;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.plugin.frame.Recorder;
import javax.swing.JLabel;

/*
 * Copyright (C), 2023, Jan Brocher / BioVoxxel. All rights reserved.
 * 
 * Original macro written by Jan Brocher/BioVoxxel.
 * 
 * BSD-3 License
 * 
 * Redistribution and use in source and binary forms of all plugins and macros, 
 * with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer 
 *    in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 *
 * DISCLAIMER:
 * 
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS “AS IS” AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

public class MetadataRecorderGUI extends JFrame implements UndoableEditListener, KeyListener, FocusListener {

	private static final long serialVersionUID = 8334480879178704804L;
	private JLabel lblImageTitle;
	private JPanel contentPane;
	private JTextArea textArea;
	private UndoManager undomanager = new UndoManager();
	private ImagePlus imagePlus = null;
	private String EXCLUDED_FROM_RECORDING[] = {"Record", "Show Info", "Close", "Console"};
	
	private static long PREVIOUS_EVENT_TRIGGER_TIME = 0;
	private Recorder ijRecorder = null;
	protected boolean imageClosed = false;
	
	
	private TextListener textListener = new TextListener() {
			
		@Override
		public void textValueChanged(TextEvent e) {
			long currentTime = Calendar.getInstance().getTime().getTime();

			if (currentTime - PREVIOUS_EVENT_TRIGGER_TIME > 0) {
								
				String lastRecordedLine = "";
								
				String recorderText = ijRecorder.getText();
				
				String[] commandOptions = recorderText.split("\n");
				
				if (commandOptions.length > 0) {
				
					lastRecordedLine = commandOptions[commandOptions.length-1];
					
					if (!isExcluded(lastRecordedLine)) {
						
						//System.out.println("Last command = " + lastRecordedLine);
						//System.out.println("lastRecordedLine.matches(\"selectWindow.*\") = " + lastRecordedLine.matches("selectWindow.*"));
						
						if (lastRecordedLine.matches("selectWindow.*") || lastRecordedLine.matches("selectImage.*")) {
							
							imagePlus = WindowManager.getCurrentImage();
							//System.out.println("Active image = " + imagePlus);
							
							readMetadataFromImage();
						}
						
						if (ijRecorder != null && imagePlus != null && !lastRecordedLine.matches("selectWindow.*") && !lastRecordedLine.matches("selectImage.*")) {
													
							textArea.append(lastRecordedLine + System.lineSeparator());
							saveMetadataToImage();
							
							imagePlus = WindowManager.getCurrentImage();
							readMetadataFromImage();
						}
						
						//setTitle("Meta-D-Rex: " + imagePlus.getTitle());
						lblImageTitle.setText(imagePlus.getTitle());
						
						
					} else {
						//System.out.println(lastRecordedLine + " is excluded according to settings");
					}
					
				}
				
				PREVIOUS_EVENT_TRIGGER_TIME = currentTime;
				//System.out.println("Execution time = " + currentTime);
			}
		}	
	};
	

	
	
	protected boolean isExcluded(String text) {
		EXCLUDED_FROM_RECORDING = Prefs.get("biovoxxel.metadata.recorder.exclusion", "Record, Show Info, Close, Console").split(",");
		for (int i = 0; i < EXCLUDED_FROM_RECORDING.length; i++) {
			if (text.contains(EXCLUDED_FROM_RECORDING[i].trim())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Create the frame.
	 */
	public MetadataRecorderGUI(ImagePlus imagePlus) {
		this.imagePlus = imagePlus;
		setTitle("Meta-D-Rex");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 500, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		
		JPopupMenu popupMenu = new JPopupMenu();
		addPopup(contentPane, popupMenu);
		
		JCheckBoxMenuItem chckbxmntmShowNativeRecorder = new JCheckBoxMenuItem("Show IJ native Recorder");
		chckbxmntmShowNativeRecorder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Recorder existingRecorder = (Recorder)WindowManager.getWindow("Recorder");
				if (chckbxmntmShowNativeRecorder.isSelected() && existingRecorder != null) {
					existingRecorder.setVisible(true);
				} else {
					existingRecorder.setVisible(false);
				}
			}
		});
		popupMenu.add(chckbxmntmShowNativeRecorder);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{0, 0, 0, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		JToggleButton tglbtnToggleRecording = new JToggleButton("<html><b>Pause recording");
		tglbtnToggleRecording.setForeground(Color.BLACK);
		tglbtnToggleRecording.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (tglbtnToggleRecording.isSelected()) {
					tglbtnToggleRecording.setText("<html><i>Continue Recording");
					tglbtnToggleRecording.setForeground(Color.RED);
					Recorder.record = false;
					
				} else {
					tglbtnToggleRecording.setText("<html><b>Pause Recording");
					tglbtnToggleRecording.setForeground(Color.BLACK);
					Recorder.record = true;
				} 
			}
		});
		
		lblImageTitle = new JLabel(imagePlus.getTitle());
		GridBagConstraints gbc_lblImageTitle = new GridBagConstraints();
		gbc_lblImageTitle.anchor = GridBagConstraints.WEST;
		gbc_lblImageTitle.gridwidth = 3;
		gbc_lblImageTitle.insets = new Insets(0, 0, 5, 0);
		gbc_lblImageTitle.gridx = 0;
		gbc_lblImageTitle.gridy = 0;
		contentPane.add(lblImageTitle, gbc_lblImageTitle);
		GridBagConstraints gbc_tglbtnToggleRecording = new GridBagConstraints();
		gbc_tglbtnToggleRecording.anchor = GridBagConstraints.WEST;
		gbc_tglbtnToggleRecording.insets = new Insets(0, 0, 5, 5);
		gbc_tglbtnToggleRecording.gridx = 0;
		gbc_tglbtnToggleRecording.gridy = 1;
		contentPane.add(tglbtnToggleRecording, gbc_tglbtnToggleRecording);
		
		JButton btnSettings = new JButton("Settings");
		btnSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openSettings();
			}
		});
		
		JButton btnReadImage = new JButton("Reload data");
		btnReadImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textArea.setText(WindowManager.getCurrentImage().getInfoProperty());
			}
		});
		GridBagConstraints gbc_btnReadImage = new GridBagConstraints();
		gbc_btnReadImage.insets = new Insets(0, 0, 5, 5);
		gbc_btnReadImage.gridx = 1;
		gbc_btnReadImage.gridy = 1;
		contentPane.add(btnReadImage, gbc_btnReadImage);
		GridBagConstraints gbc_btnSettings = new GridBagConstraints();
		gbc_btnSettings.insets = new Insets(0, 0, 5, 0);
		gbc_btnSettings.gridx = 2;
		gbc_btnSettings.gridy = 1;
		contentPane.add(btnSettings, gbc_btnSettings);
		
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 3;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 2;
		contentPane.add(scrollPane, gbc_scrollPane);
		
		textArea = new JTextArea();
		scrollPane.setViewportView(textArea);

		textArea.getDocument().addUndoableEditListener(this);
		textArea.addKeyListener(this);
		textArea.addFocusListener(this);		
				
		addTextListenerToRecorder();
		
		readMetadataFromImage();
	}
	

	protected void openSettings() {
		
		Window settingsDialog = WindowManager.getWindow("Metadata Recorder Settings");
		
		if (settingsDialog == null) {
			MetadataRecorderSettings frame = new MetadataRecorderSettings();
			WindowManager.addWindow(frame);
			frame.setVisible(true);			
		} else {
			settingsDialog.toFront();
		}
	}

	private void readMetadataFromImage() {
		//System.out.println("Reading Metadata from " + imagePlus);
		textArea.setText(imagePlus.getInfoProperty());
	}
	
	
	protected void saveMetadataToImage() {
		imagePlus.setProperty("Info", textArea.getText());
		//System.out.println("Metadata saved in image header");
		imagePlus.changes = true;
	}

	private void addTextListenerToRecorder() {
		
		setupRecorder();
		
		Component[] comp = ijRecorder.getComponents();
		for (int i = 0; i < comp.length; i++) {
			//System.out.println(comp[i]);
		}
		
		TextListener[] existingTextListener = ((TextArea)ijRecorder.getComponent(1)).getTextListeners();
		
		if (existingTextListener.length == 0) {
			addTextAreaListener();				
		}	
	}
	
	private void setupRecorder() {
		Recorder existingRecorder = (Recorder)WindowManager.getWindow("Recorder");
		//System.out.println("Existing IJ Recorder = " + existingRecorder);
		
		if (existingRecorder == null) {
			ijRecorder = new Recorder(false);
			addTextAreaListener();
		} else if (existingRecorder != null) {
			ijRecorder = existingRecorder;
		}
		//System.out.println("IJ Recorder = " + ijRecorder);
	}

	private void addTextAreaListener() {
		
		if(textListenerExists()) {
			//do nothing
		} else {
			if (ijRecorder.getComponentCount() > 1) {
				((TextArea)ijRecorder.getComponent(1)).addTextListener(textListener);				
			} else {
				
			}
		}
	}

	private boolean textListenerExists() {
		
		TextListener[] textListener = new TextListener[0];
		if (ijRecorder != null && ijRecorder.getComponentCount() > 1) {
			textListener = ((TextArea)ijRecorder.getComponent(1)).getTextListeners();			
		} 
		
		return textListener.length > 0 ? true : false;
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

	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if ((e.getKeyCode() == KeyEvent.VK_Z) && (e.isControlDown())) {
		      try {
		    	  //System.out.println("undo");
		    	  undomanager.undo();
		      } catch (CannotUndoException cue) {
		    	  //cue.printStackTrace();
		    	  Toolkit.getDefaultToolkit().beep();
		      }
		    }

		    if ((e.getKeyCode() == KeyEvent.VK_Y) && (e.isControlDown())) {
		      try {
		    	  //System.out.println("redo");
		    	  undomanager.redo();
		      } catch (CannotRedoException cue) {
		    	  //cue.printStackTrace();
		    	  Toolkit.getDefaultToolkit().beep();
		      }
		    }
	}

	@Override
	public void keyReleased(KeyEvent e) {
		
	}

	@Override
	public void undoableEditHappened(UndoableEditEvent e) {
		//System.out.println(e.getEdit());
		undomanager.addEdit(e.getEdit());
		
	}
	
	private void createUndoMananger() {
		//System.out.println("createUndoMananger");
		undomanager = new UndoManager();
	  }

	
	private void removeUndoMananger() {
		//System.out.println("removeUndoMananger");
		undomanager.end();
	}
	
	public void focusGained(FocusEvent fe) {
		//System.out.println("focusGained");
	    createUndoMananger();
	}
	
	public void focusLost(FocusEvent fe) {
		//System.out.println("focusLost");
	    removeUndoMananger();
	}
}
