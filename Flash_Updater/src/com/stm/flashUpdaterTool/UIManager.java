package com.stm.flashUpdaterTool;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
//import javax.swing.WiderDropDownCombo;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;

import com.stm.flashUpdaterTool.Common.ControlStatus;
import com.stm.flashUpdaterTool.Common.UpdaterFunction;


public class UIManager extends JFrame {
	
	private static final long serialVersionUID = 1L;

	private JsscPortManager jsscPortManager;

	private static Component updaterToolsItem;
	private static Component advToolsItem;
	private Component portListCombo;
	private Component portListButton;
	private Component fwUpdateButton;
	private Component epUserInfo;
	private Component pBarPanel;
	private JProgressBar pBar;
	private ControlStatus btnStatus;

	private static FileFilter imgFilter;
	private int MY_MINIMUM = 0;

	private static int updateCyles = 0;
	
	/**
	 * Constructor
	 * @param jsscPortManager
	 */
	public UIManager(JsscPortManager jsscPortManager) {
		this.jsscPortManager = jsscPortManager;
		
		imgFilter = new ExtensionFileFilter("Image file (*.bin)", "bin");

		initUI();
		
	}

	/**
	 * Class for managing file selection filter
	 */
	private class ExtensionFileFilter extends FileFilter {
  	  String description;

  	  String extensions[];

  	  public ExtensionFileFilter(String description, String extension) {
  	    this(description, new String[] { extension });
  	  }

  	  public ExtensionFileFilter(String description, String extensions[]) {
  	    if (description == null) {
  	      this.description = extensions[0];
  	    } else {
  	      this.description = description;
  	    }
  	    this.extensions = (String[]) extensions.clone();
  	    toLower(this.extensions);
  	  }

  	  private void toLower(String array[]) {
  	    for (int i = 0, n = array.length; i < n; i++) {
  	      array[i] = array[i].toLowerCase();
  	    }
  	  }

  	  public String getDescription() {
  	    return description;
  	  }

  	  public boolean accept(File file) {
  	    if (file.isDirectory()) {
  	      return true;
  	    } else {
  	      String path = file.getAbsolutePath().toLowerCase();
  	      for (int i = 0, n = extensions.length; i < n; i++) {
  	        String extension = extensions[i];
  	        if ((path.endsWith(extension) && (path.charAt(path.length() - extension.length() - 1)) == '.')) {
  	          return true;
  	        }
  	      }
  	    }
  	    return false;
  	  }
  	}

	/**
	 * Add Menu Bar
	 */
	private void addMenuBar() {
		// Menu Bar
        JMenuBar menuBar = new JMenuBar();
        //ImageIcon icon = new ImageIcon("exit.png");
        
        // Add File Menu item
        addFileMenu(menuBar);
        
        // Add Tools Menu item
        /** Uncomment/comment this line to enable/disable
          * file system browsing for .img/.bin file choice
          */
        addToolsMenu(menuBar);
        
        // Add Help Menu item        
        addHelpMenu(menuBar);
        
        setJMenuBar(menuBar);
	}
	
	/**
	 * File Menu item
	 * @param menuBar
	 */
	private void addFileMenu(JMenuBar menuBar) {
        //Menu File
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);

        JMenuItem eMenuItem = new JMenuItem("Exit", null);
        eMenuItem.setMnemonic(KeyEvent.VK_E);
        eMenuItem.setToolTipText("Exit");
        eMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        });

        file.add(eMenuItem);
        menuBar.add(file);

	}
	
	/**
	 * Tools Menu Item
	 * @param menuBar
	 */
	private void addToolsMenu(JMenuBar menuBar) {
        //Menu Tools
        JMenu tools = new JMenu("Tools");
        tools.setMnemonic(KeyEvent.VK_T);
        //Sub menu Updater
        updaterToolsItem = new JMenuItem(Common.FW_UPDATE_BUTTON_STR, null);
        JMenuItem uToolsItem1 = (JMenuItem)updaterToolsItem;
        uToolsItem1.setMnemonic(KeyEvent.VK_U);
        uToolsItem1.setToolTipText("GNSS FW Upgrader");
        uToolsItem1.setEnabled(false);

        uToolsItem1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
            	automaticFWUpgrade();
            }
        });

        advToolsItem = new JMenuItem(Common.FW_UPDATE_ADV_STR, null);
        JMenuItem uToolsItem2 = (JMenuItem)advToolsItem;
        uToolsItem2.setMnemonic(KeyEvent.VK_A);
        uToolsItem2.setToolTipText("Advanced option");
        uToolsItem2.setEnabled(false);

        uToolsItem2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
            	int result = handleFileChoice(imgFilter, updaterToolsItem);
            	if(result == JFileChooser.APPROVE_OPTION) {
            		disableUpdateTools();
            	}
            }
        });

        tools.add(uToolsItem1);
        tools.add(uToolsItem2);

        menuBar.add(tools);
	}
	
	/**
	 * Dialog for FW binary file choice
	 * @param filter
	 * @param c
	 * @return
	 */
	private int handleFileChoice(FileFilter filter, Component c) {

		int result;
		UpdaterFunction uFunction = Common.UpdaterFunction.STACK;
		
		JFileChooser fileChooser = new JFileChooser();
		String userDir = System.getProperty("user.home");
		fileChooser.setCurrentDirectory(new java.io.File(userDir));

    	// Apply filters
    	fileChooser.setFileFilter(filter);
    	fileChooser.setAcceptAllFileFilterUsed(false);

    	// Set title
    	fileChooser.setDialogTitle("Choose FW file");

		// pop up an "Open File" file chooser dialog   	
    	result = fileChooser.showOpenDialog(this);

    	uFunction = Common.UpdaterFunction.STACK;
    	
		if (result == JFileChooser.APPROVE_OPTION) {
			InputStream inputStream;
			try {
				inputStream = new FileInputStream(fileChooser.getSelectedFile());
				initUpdateProcess(inputStream, uFunction);
			} catch (IOException ioe) {
				Common.showMessage("Image file coudn't be read", JOptionPane.ERROR_MESSAGE);

			} catch (Exception e) {
				Common.showMessage("Image file coudn't be found", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		return result;

	}

	/**
	 * Help Menu Item
	 * @param menuBar
	 */
	private void addHelpMenu(JMenuBar menuBar) {
        //Menu Help
        JMenu help = new JMenu("Help");
        help.setMnemonic(KeyEvent.VK_H);
       
        // for copying style
        JLabel label = new JLabel();
        Font font = label.getFont();

        // create some css from the label's font
        StringBuffer style = new StringBuffer("font-family:" + font.getFamily() + ";");
        style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
        style.append("font-size:" + font.getSize() + "pt;");

        // html content
        
        final JEditorPane ep = new JEditorPane("text/html",
        		"<html><body style=\"" + style + "\">" //
                + Common.UI_TITLE+"<br><br>" //
                + "Copyright \u00a9 STMicroelectronics 2019<br><br>" //
               // + "Version 2.0.0<br><br>" //
                + "<a href=\"http://www.st.com/\">www.st.com</a>" //
                + "</body></html>");
        
        // handle link events
        ep.addHyperlinkListener(new HyperlinkListener()
        {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e)
            {
            	URI uri = null;
            	try {
					uri = new URI(e.getURL().toString());
				} catch (URISyntaxException e2) {
					Common.showMessage(Common.LINK_ERROR_STR, JOptionPane.WARNING_MESSAGE);
				}
                if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
					try {
						Desktop.getDesktop().browse(uri);
					} catch (IOException e1) {
						Common.showMessage(Common.INVALID_LINK_STR, JOptionPane.WARNING_MESSAGE);
					}
            }
        });
        ep.setEditable(false);
        ep.setBackground(label.getBackground());
              
        //Sub menu About
        JMenuItem aHelpItem = new JMenuItem("About", null);
        aHelpItem.setMnemonic(KeyEvent.VK_A);
        aHelpItem.setToolTipText("About");
        aHelpItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
            	Common.showMessage(ep, JOptionPane.PLAIN_MESSAGE);
            }
        });
        
        help.add(aHelpItem);
        menuBar.add(help);
	}
	
	/**
	 * Control section of the main frame
	 */
	private void addControlPanel() {
        JPanel controlPanel = new JPanel();
        GridLayout glyt = new GridLayout(3, 2);
        controlPanel.setLayout(glyt);

        add(controlPanel);

        // Add the panel for serial port controls
        addSerialPortPanel(this, controlPanel);
        
        // Add the panel for update buttons
        addUpdatePanel(controlPanel);
        
        // Add the panel containing FW/HW info
        addInfoPanel(this, controlPanel);
        
        // Add the progress bar panel
        addProgressBarPanel(this, controlPanel);
        
        // Add the image (ST logo) panel 
        addImgPanel(this, controlPanel);
        
	}

	/**
	 * UI helper
	 * @param panel
	 * @param panelTitle
	 */
	private void setPanelBorder(JPanel panel, String panelTitle) {
		Border bLine = BorderFactory.createLineBorder(Color.GRAY, 1, true);
	    Border bTitle = BorderFactory.createTitledBorder(bLine, panelTitle, TitledBorder.LEFT, TitledBorder.TOP);
	    panel.setBorder(bTitle);
	}
    
	/**
	 * ST logo Panel
	 * @param uiManager
	 * @param controlPanel
	 */
	private void addImgPanel(UIManager uiManager, JPanel controlPanel) {
        JPanel iconPanel = new JPanel(new BorderLayout());
        
        URL url = FlashUpdater.class.getResource(Common.ST_LOGO_PATH);
        ImageIcon icon = new ImageIcon(url);
        Image scaledImage = icon.getImage().getScaledInstance(115,85,Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        JLabel lbl = new JLabel("", scaledIcon, JLabel.RIGHT);
        lbl.setVisible(true);
        iconPanel.add(lbl);
        
        // Add serialPortPanel to the control panel
        controlPanel.add(new JPanel());
        controlPanel.add(iconPanel);
	}
	
	/**
	 * Serial Port Panel
	 * @param uiManager
	 * @param controlPanel
	 */
	private void addSerialPortPanel(UIManager uiManager, JPanel controlPanel) {
        JPanel serialPortPanel = new JPanel();

        //Add label to panel1
        JLabel lbl = new JLabel(Common.SERIAL_PORT_LABEL_STR);
        lbl.setVisible(true);
        serialPortPanel.add(lbl);

        //Add the port list names to the combo and the combo to the serialPortPanel
        portListCombo = new WiderDropDownCombo();
        final WiderDropDownCombo cb = (WiderDropDownCombo)portListCombo;
        cb.setPreferredSize(new Dimension(120, 25));
        cb.setVisible(true);
        serialPortPanel.add(cb);

        //Add the COM Port Open/Close Button to the serialPortPanel
        portListButton = new JButton(Common.SERIAL_PORT_OPEN_STR);
        final JButton btn = (JButton)portListButton;
        btnStatus = Common.ControlStatus.P_CLOSED;//Common.OPEN;
        btn.addActionListener(new ActionListener() {

			@Override
            public void actionPerformed(ActionEvent event) {
            	if(btnStatus == Common.ControlStatus.P_CLOSED) {
            		try {
            			String portName = (String) cb.getSelectedItem();
            			jsscPortManager.openPort(portName);
            			//disablePortControl();
            		} catch (SerialPortException e) {
            			Common.showMessage(e.getMessage(), JOptionPane.ERROR_MESSAGE);
            		} catch (Exception e) {
            			Common.showMessage(e.getMessage(), JOptionPane.ERROR_MESSAGE);
                		try {
							jsscPortManager.closePort();
						} catch (SerialPortException ex) {
							Common.showMessage(ex.getMessage(), JOptionPane.ERROR_MESSAGE);
						}
            		}
            	} else if(btnStatus == Common.ControlStatus.P_OPENED) {
            		try {
						jsscPortManager.closePort();
					} catch (SerialPortException e) {
						Common.showMessage(e.getMessage(), JOptionPane.ERROR_MESSAGE);
					}
            	}
            }
        });
        serialPortPanel.add(btn);

        // Add border
        setPanelBorder(serialPortPanel, Common.SERIAL_PORT_BORDER_STR);
        
        // Add serialPortPanel to the control panel
        controlPanel.add(serialPortPanel);
	}
	
	/**
	 * Update the list of the serial ports
	 * @param portNames
	 */
	public void refreshPortList (String[] portNames) {
        WiderDropDownCombo cb = (WiderDropDownCombo)portListCombo;
        
       	cb.removeAllItems();
       	for(int i = 0; i < portNames.length; i++){
           	cb.addItem(portNames[i]);        
    	}
        cb.setWide(true);
	}

	/**
	 * Disable the serial port controls
	 * @param setEn
	 */
	private void portButtonSetEnabled(boolean setEn) {
		JButton btn = (JButton)portListButton;
		btn.setEnabled(setEn);
    }

	/**
	 * Disable the serial port controls
	 */
	private void disablePortControl() {
		WiderDropDownCombo cb = (WiderDropDownCombo)portListCombo;
		JButton btn = (JButton)portListButton;
		
		cb.setEnabled(false);
		enableUpdateTools();
		btn.setText(Common.SERIAL_PORT_CLOSE_STR);
		btn.setEnabled(true);
		btnStatus = Common.ControlStatus.P_OPENED;
    }
	
	/**
	 * Enable the serial port controls
	 */
	private void enablePortControl() {
		WiderDropDownCombo cb = (WiderDropDownCombo)portListCombo;
		JButton btn = (JButton)portListButton;

		setUserInfo(Common.USER_INFO_DEFAULT_STR);
    	cb.setEnabled(true);
    	disableUpdateTools();
		btn.setText(Common.SERIAL_PORT_OPEN_STR);
		btn.setEnabled(true);
		btnStatus = Common.ControlStatus.P_CLOSED;
    }

	/**
	 * Disable the controls for the Update FW tool
	 */
	private void disableUpdateTools() {
		if(updaterToolsItem != null) {
			updaterToolsItem.setEnabled(false);
		}
		if(advToolsItem != null) {
			advToolsItem.setEnabled(false);
		}
		if(fwUpdateButton != null) {
			fwUpdateButton.setEnabled(false);
		}
	}

	/**
	 * Enable the controls for the Update FW tool
	 */
	private void enableUpdateTools() {
		if(updaterToolsItem != null) {
			updaterToolsItem.setEnabled(true);
		}
		if(advToolsItem != null) {
			advToolsItem.setEnabled(true);
		}
		if(fwUpdateButton != null) {
			fwUpdateButton.setEnabled(true);
		}
	}

	/**
	 * Update FW Panel
	 * @param uiManager
	 * @param controlPanel
	 */
	private void addUpdatePanel(JPanel controlPanel) {
		JPanel updatePanel = new JPanel();

		URL url = FlashUpdater.class.getResource(Common.TOOLS_ICON_PATH);
        ImageIcon icon = new ImageIcon(url);
        Image si = icon.getImage().getScaledInstance(18,18,Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(si);
        
        //Add the  Button to the updatePanel
        fwUpdateButton = new JButton(Common.FW_UPDATE_BUTTON_STR, scaledIcon);
        final JButton fwBtn = (JButton)fwUpdateButton;
    	fwUpdateButton.setEnabled(false);
        fwBtn.addActionListener(new ActionListener() {
        	
			@Override
			public void actionPerformed(ActionEvent event) {
				automaticFWUpgrade();
            }
        });
        updatePanel.add(fwBtn);

        // Add border
        setPanelBorder(updatePanel, Common.FWUPG_BUTTON_STR);
        
        // Add updatePanel to the control panel
        controlPanel.add(updatePanel);
	}

	/**
	 * Handle dialogs for upgrading FW using the embedded .bin
	 */
	private void automaticFWUpgrade() {
		int option = JOptionPane.NO_OPTION;

//		if(!jsscPortManager.fwNeedsUpdate()) {
//			Common.showMessage(Common.FW_VERSION_WARN_STR, JOptionPane.WARNING_MESSAGE);
//		}

		option = Common.showConfirm(Common.FW_UPDATE_CONFIRM_STR, JOptionPane.YES_NO_OPTION);

		if(option == JOptionPane.YES_OPTION) {
			Common.showMessage(Common.GNSS_FW_UPDATE_WARNING, JOptionPane.WARNING_MESSAGE);
			try {
				InputStream fwInputStream = null;

				fwInputStream = FlashUpdater.class.getResourceAsStream(Common.GNSS_FW_FILE_PATH);
				initUpdateProcess(fwInputStream, Common.UpdaterFunction.STACK);

			} catch (IOException ioe) {
				Common.showMessage(Common.FILE_READ_FAILURE_STR, JOptionPane.ERROR_MESSAGE);

			}
		}

	}

	/**
	 * FW/HW Info Panel
	 * @param uiManager
	 * @param controlPanel
	 */
	private void addInfoPanel(UIManager uiManager, JPanel controlPanel) {
 
        Font jtfFont = new Font("Times", Font.BOLD, 11);
        Border jtfBorder = BorderFactory.createBevelBorder(1);

        // Create panel for HW/FW userInfo info or Application info
        JPanel infoPanel = new JPanel();

        epUserInfo = new JEditorPane();
        JEditorPane userInfo = (JEditorPane)epUserInfo;
        userInfo.setSize(215, 500);
        userInfo.setEditable(false);
        //userInfo.setVisible(false);
        userInfo.setText(Common.USER_INFO_DEFAULT_STR);
        userInfo.setBackground(controlPanel.getBackground());
        userInfo.setFont(jtfFont);
        userInfo.setBorder(jtfBorder);
        infoPanel.add(userInfo);

        // Add infoPanel to the control panel
        controlPanel.add(infoPanel);
	}
	
	/**
	 * Utility to set FW/HW userInfo or Application info
	 * @param message
	 */
	public void setUserInfo(Object message) {
		JEditorPane userInfo = (JEditorPane)epUserInfo;
		
		userInfo.setVisible(true);
		userInfo.setText((String)message);
    }
    
	/**
	 * Utility to reset FW/HW userInfo or Application info
	 */
    public void unsetUserInfo() {
    	JEditorPane userInfo = (JEditorPane)epUserInfo;
    	
    	userInfo.setText("");
    }
    
    /**
     * Progress Bar Panel
     * @param uiManager
     * @param controlPanel
     */
	private void addProgressBarPanel(UIManager uiManager, JPanel controlPanel) {
        JLabel uLabel = new JLabel(Common.FW_UPDATE_LABEL_STR);
        uLabel.setVisible(true);
        
        pBarPanel = new JPanel();
        JPanel pbPanel = (JPanel)pBarPanel;

        pbPanel.add(uLabel);
        setProgressBar(pbPanel);
        pbPanel.setVisible(false);
        
        //Add pBarPanel to the control panel
        controlPanel.add(pbPanel);

	}
	
	/**
	 * Set progress bar for indicating FW update status
	 * @param panel
	 */
	private void setProgressBar(JPanel panel) {
		
        // initialize Progress Bar
		pBar = new JProgressBar();
        pBar.setMinimum(MY_MINIMUM);
        pBar.setStringPainted(true);
        // add to JPanel
        panel.add(pBar);

    }
	
	/**
	 * Utility to update progress bar
	 */
	private void updateProgressBar() {
		updateCyles++;
        pBar.setValue(updateCyles);
	}
	
	/**
	 * Utility to hide progress bar on update completion
	 * @param uEvent
	 */
	private void hidePBarPanel(Common.UpdaterEvent uEvent) {
		updateCyles = 0;
    	pBarPanel.setVisible(false);

    	if(uEvent == Common.UpdaterEvent.UPDATE_DONE) {
	    	Common.showMessage(Common.FW_UPDATE_DONE_STR, JOptionPane.INFORMATION_MESSAGE);	
    	}
    }

	/**
	 * Utility
	 * @param uEvent
	 */
	private void handleApplPortClosed(Common.UpdaterEvent uEvent) {
		hidePBarPanel(uEvent);
		enablePortControl();
	}

	/**
	 * Handle events for Update FW tool
	 * @param uEvent
	 * @param message
	 */
	public void handleEvent (Common.UpdaterEvent uEvent, Object message) {

		switch(uEvent) {
			case PORT_OPENED:
				setUserInfo(message);
				disablePortControl();
				break;
			case PORT_CLOSED:
				handleApplPortClosed(uEvent);
				break;
			case WAIT:
				portButtonSetEnabled(false);
				break;
			case UPDATING:
				//unsetUserInfo();
				setUserInfo(message);
				updateProgressBar();
				break;
			case UPDATE_DONE:
				setUserInfo(message);
				hidePBarPanel(uEvent);
				enableUpdateTools();
				break;
			default:
		}
    	
    }

	/**
	 * Initialize UI
	 */
	public void initUI() {
		addMenuBar();

        addControlPanel();
 
        URL url = FlashUpdater.class.getResource(Common.ST_ICON_PATH);
        ImageIcon icon = new ImageIcon(url);
        //icon.getImage().getScaledInstance(115,85,Image.SCALE_SMOOTH);
        setIconImage(icon.getImage().getScaledInstance(128,128,Image.SCALE_SMOOTH));
        setTitle(Common.UI_TITLE_VER_STR);
       	setSize(500, 350);
 
        //setResizable(false);
        setLocationRelativeTo(null);
        
        addWindowListener(new uiManagerWindowAdapter(this, jsscPortManager));

    }

	/**
	 * Initialize the Update FW process
	 * @param imgFile
	 * @param uFunction
	 * @throws IOException
	 */
	public void initUpdateProcess (Object imgFile, UpdaterFunction uFunction) throws IOException {
		

		if(uFunction == Common.UpdaterFunction.STACK) {
			pBar.setMaximum((((InputStream) imgFile).available()/16384)+5);
		}

		try {
			jsscPortManager.initUpdateProcess(imgFile, uFunction);
			unsetUserInfo();
			pBarPanel.setVisible(true);

		} catch (IOException ioe) {
			throw ioe;
		}
	}

	/**
	 * Window adapter class
	 * @author CLABs - Lecce
	 *
	 */
	class uiManagerWindowAdapter extends WindowAdapter {
    	JsscPortManager jsscPortManager = null;
    	        
    	uiManagerWindowAdapter(UIManager uiManager, JsscPortManager jsscPortManager) {
            this.jsscPortManager = jsscPortManager;
        }

    	// implement windowClosing method
        public void windowClosing(WindowEvent e) {
       		try {
				jsscPortManager.closePort();
			} catch (SerialPortException ex) {
    			Common.showMessage(ex.getMessage(), JOptionPane.ERROR_MESSAGE);
			}
        	// exit the application when window's close button is clicked
            System.exit(0);
        }

    }
}
