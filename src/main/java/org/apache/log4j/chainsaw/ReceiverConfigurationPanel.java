/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.log4j.chainsaw;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.chainsaw.prefs.SettingsManager;
import org.apache.log4j.net.SocketReceiver;
import org.apache.log4j.net.UDPReceiver;


/**
 * A panel providing receiver configuration options
 *
 * @author Paul Smith
 */
class ReceiverConfigurationPanel extends JPanel {
    private final Logger logger = LogManager.getLogger(ReceiverConfigurationPanel.class);

    private final PanelModel panelModel = new PanelModel();

    //network receiver widgets
    private JComboBox networkReceiverPortComboBox;
    private JComboBox networkReceiverClassNameComboBox;
    private DefaultComboBoxModel networkReceiverClassNameComboBoxModel;
    private DefaultComboBoxModel networkReceiverPortComboBoxModel;

    //logfile receiver widgets
    private JButton browseLogFileButton;
    private JComboBox logFileFormatTypeComboBox;

    private JComboBox logFileFormatComboBox;
    private JComboBox logFileFormatTimestampFormatComboBox;
    private JTextField logFileURLTextField;
    private DefaultComboBoxModel logFileFormatComboBoxModel;
    private DefaultComboBoxModel logFileFormatTimestampFormatComboBoxModel;

    //use existing configuration widgets
    private JButton browseForAnExistingConfigurationButton;
    private DefaultComboBoxModel existingConfigurationComboBoxModel;
    private JComboBox existingConfigurationComboBox;

    //don't warn again widgets
    private JCheckBox dontwarnIfNoReceiver;

    private JButton okButton;
    private JButton cancelButton;

    //radiobutton widgets
    private JRadioButton logFileReceiverRadioButton;
    private JRadioButton networkReceiverRadioButton;
    private JRadioButton useExistingConfigurationRadioButton;
    private JRadioButton useAutoSavedConfigRadioButton;
    private ButtonGroup buttonGroup;

    private JPanel lowerPanel;

    private final JPanel networkReceiverPanel = buildNetworkReceiverPanel();
    private final JPanel logFileReceiverPanel = buildLogFileReceiverPanel();
    private final JPanel useExistingConfigurationPanel = buildUseExistingConfigurationPanel();
    private final JPanel dontWarnAndOKPanel = buildDontWarnAndOKPanel();
    private final JPanel bottomDescriptionPanel = buildBottomDescriptionPanel();

    //set by LogUI to handle hiding of the dialog
    private ActionListener completionActionListener;

    ReceiverConfigurationPanel() {
        setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        setLayout(new GridBagLayout());

        buttonGroup = new ButtonGroup();

        lowerPanel = new JPanel(new BorderLayout());
        lowerPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        lowerPanel.setPreferredSize(new Dimension(600, 200));
        lowerPanel.setMinimumSize(new Dimension(600, 200));

        int yPos = 0;

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = yPos++;
        c.fill = GridBagConstraints.HORIZONTAL;
        logFileReceiverRadioButton = new JRadioButton("Load and tail events from a regular text log file");
        buttonGroup.add(logFileReceiverRadioButton);
        add(logFileReceiverRadioButton, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = yPos++;
        c.fill = GridBagConstraints.HORIZONTAL;
        networkReceiverRadioButton = new JRadioButton("Receive events from the network");
        buttonGroup.add(networkReceiverRadioButton);
        add(networkReceiverRadioButton, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = yPos++;
        c.fill = GridBagConstraints.HORIZONTAL;
        useExistingConfigurationRadioButton = new JRadioButton("Use an existing Chainsaw configuration file...");
        buttonGroup.add(useExistingConfigurationRadioButton);
        add(useExistingConfigurationRadioButton, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = yPos++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 0);
        useAutoSavedConfigRadioButton = new JRadioButton("Use auto-saved configuration from $HOME/.chainsaw/receiver-config.xml");
        buttonGroup.add(useAutoSavedConfigRadioButton);
        add(useAutoSavedConfigRadioButton, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = yPos++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 10, 10, 0);
        add(lowerPanel, c);
        
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = yPos++;
        c.weightx = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 10, 0, 0);
        add(dontWarnAndOKPanel, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = yPos++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 10, 10, 0);
        add(bottomDescriptionPanel, c);

        /**
         * This listener activates/deactivates certain controls based on the current
         * state of the options
         */
        ActionListener al = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updateEnabledState((Component)e.getSource());
                }
            };

        logFileReceiverRadioButton.addActionListener(al);
        networkReceiverRadioButton.addActionListener(al);
        useExistingConfigurationRadioButton.addActionListener(al);
        useAutoSavedConfigRadioButton.addActionListener(al);

        buttonGroup.setSelected(logFileReceiverRadioButton.getModel(), true);
        updateEnabledState(logFileReceiverRadioButton);
    }

    private JPanel buildDontWarnAndOKPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.LINE_START;
        dontwarnIfNoReceiver = new JCheckBox("Always start Chainsaw with this configuration");
        panel.add(dontwarnIfNoReceiver, c);

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 0;
        okButton = new JButton(" OK ");
        panel.add(okButton, c);

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 0;
        cancelButton = new JButton(" Cancel ");
        panel.add(cancelButton, c);
        cancelButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                panelModel.setCancelled();
                completionActionListener.actionPerformed(new ActionEvent(this, -1, "cancelled"));
            }
        });

        okButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                completionActionListener.actionPerformed(new ActionEvent(this, -1, "cancelled"));
            }
        });
        return panel;
    }

    private JPanel buildBottomDescriptionPanel() {
        JTextPane descriptionTextPane = new JTextPane();
        StyledDocument doc = descriptionTextPane.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        descriptionTextPane.setText("An example configuration file is available from the Welcome tab");
        descriptionTextPane.setEditable(false);
        descriptionTextPane.setOpaque(false);
        descriptionTextPane.setFont(getFont());

        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;

        panel.add(descriptionTextPane, c);

        return panel;
    }

    private JPanel buildNetworkReceiverPanel() {
        networkReceiverPortComboBoxModel = new DefaultComboBoxModel();
        networkReceiverPortComboBoxModel.addElement("4445");
        networkReceiverPortComboBoxModel.addElement("4560");

        networkReceiverPortComboBox = new JComboBox(networkReceiverPortComboBoxModel);
        networkReceiverPortComboBox.setEditable(true);
        networkReceiverPortComboBox.setOpaque(false);

        networkReceiverClassNameComboBoxModel = new DefaultComboBoxModel();
        networkReceiverClassNameComboBoxModel.addElement(SocketReceiver.class.getName());
        networkReceiverClassNameComboBoxModel.addElement(UDPReceiver.class.getName());

        networkReceiverClassNameComboBox = new JComboBox(networkReceiverClassNameComboBoxModel);

        networkReceiverClassNameComboBox.setEditable(false);
        networkReceiverClassNameComboBox.setOpaque(false);

        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 0, 5);
        panel.add(networkReceiverClassNameComboBox, c);

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = 0;
        panel.add(networkReceiverPortComboBox, c);

        return panel;
    }

    private JPanel buildLogFileReceiverPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        browseLogFileButton = new JButton(new AbstractAction(" Find a log file ") {
            public void actionPerformed(ActionEvent e) {
                try {

                    URL url = browseLogFile();
                    if (url != null) {
                        String item = url.toURI().toString();
                        logFileURLTextField.setText(item);
                    }
                } catch (Exception ex) {
                    logger.error(
                        "Error browsing for log file", ex);
                }
            }
        });

        browseLogFileButton.setToolTipText("Shows a File Open dialog to allow you to find a log file");
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(0, 0, 5, 0);
        panel.add(browseLogFileButton, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.LINE_END;
        c.insets = new Insets(0, 0, 5, 5);
        panel.add(new JLabel(" Log file URL "), c);
        logFileURLTextField = new JTextField();
        logFileURLTextField.setEditable(true);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 1.0;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 5, 0);
        panel.add(logFileURLTextField, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.anchor = GridBagConstraints.LINE_END;
        c.insets = new Insets(0, 0, 5, 5);
        panel.add(new JLabel(" Log file format type "), c);

        DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
        comboBoxModel.addElement("PatternLayout format");
        comboBoxModel.addElement("LogFilePatternReceiver LogFormat");

        logFileFormatTypeComboBox = new JComboBox(comboBoxModel);
        logFileFormatTypeComboBox.setOpaque(false);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 2;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(0, 0, 5, 0);
        panel.add(logFileFormatTypeComboBox, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 3;
        c.anchor = GridBagConstraints.LINE_END;
        c.insets = new Insets(0, 5, 5, 5);
        panel.add(new JLabel(" Log file format "), c);

        logFileFormatComboBoxModel = new DefaultComboBoxModel();
        seedLogFileFormatComboBoxModel();
        logFileFormatComboBox = new JComboBox(logFileFormatComboBoxModel);
        logFileFormatComboBox.setEditable(true);
        logFileFormatComboBox.setOpaque(false);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 3;
        c.weightx = 0.5;
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 5, 0);
        panel.add(logFileFormatComboBox, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 4;
        c.insets = new Insets(0, 5, 5, 5);
        panel.add(new JLabel(" Log file timestamp format "), c);

        logFileFormatTimestampFormatComboBoxModel = new DefaultComboBoxModel();
        seedLogFileFormatTimestampComboBoxModel();
        logFileFormatTimestampFormatComboBox = new JComboBox(logFileFormatTimestampFormatComboBoxModel);
        logFileFormatTimestampFormatComboBox.setEditable(true);
        logFileFormatTimestampFormatComboBox.setOpaque(false);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 4;
        c.weightx = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 0);
        panel.add(logFileFormatTimestampFormatComboBox, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth=5;
        c.insets = new Insets(5, 5, 0, 5);
        panel.add(new JLabel("<html> See PatternLayout or LogFilePatternReceiver JavaDoc for details </html>"), c);
        return panel;
    }

    private void seedLogFileFormatComboBoxModel()
    {
        logFileFormatComboBoxModel.addElement("MESSAGE");
        logFileFormatComboBoxModel.addElement("%p %t %c - %m%n");
        logFileFormatComboBoxModel.addElement("LEVEL THREAD LOGGER - MESSAGE");
        logFileFormatComboBoxModel.addElement("%d{ABSOLUTE} %-5p [%c{1}] %m%n");
        logFileFormatComboBoxModel.addElement("TIMESTAMP LEVEL [LOGGER] MESSAGE");
    }

    private void seedLogFileFormatTimestampComboBoxModel()
    {
        logFileFormatTimestampFormatComboBoxModel.addElement("yyyy-MM-dd HH:mm:ss,SSS");
        logFileFormatTimestampFormatComboBoxModel.addElement("yyyyMMdd HH:mm:ss.SSS");
        logFileFormatTimestampFormatComboBoxModel.addElement("yyyy/MM/dd HH:mm:ss");
        logFileFormatTimestampFormatComboBoxModel.addElement("dd MMM yyyy HH:mm:ss,SSS");
        logFileFormatTimestampFormatComboBoxModel.addElement("HH:mm:ss,SSS");
        logFileFormatTimestampFormatComboBoxModel.addElement("yyyy-MM-ddTHH:mm");
        logFileFormatTimestampFormatComboBoxModel.addElement("yyyy-MM-ddTHH:mm:ss.SSS");
    }

    private JPanel buildUseExistingConfigurationPanel() {
        existingConfigurationComboBoxModel = new DefaultComboBoxModel();

        existingConfigurationComboBox = new JComboBox(existingConfigurationComboBoxModel);
        existingConfigurationComboBox.setOpaque(false);
        existingConfigurationComboBox.setToolTipText("Previously loaded configurations can be chosen here");
        existingConfigurationComboBox.setEditable(true);

        existingConfigurationComboBox.getEditor().getEditorComponent().addFocusListener(
            new FocusListener() {
                public void focusGained(FocusEvent e) {
                    selectAll();
                }

                private void selectAll() {
                    existingConfigurationComboBox.getEditor().selectAll();
                }

                public void focusLost(FocusEvent e) {
                }
            });

        browseForAnExistingConfigurationButton = new JButton(new AbstractAction(" Find an existing configuration ") {
                    public void actionPerformed(ActionEvent e) {
                        try {

                            URL url = browseConfig();

                            if (url != null) {
                                existingConfigurationComboBoxModel.addElement(url.toExternalForm());
                                existingConfigurationComboBox.getModel().setSelectedItem(
                                    url);
                            }
                        } catch (Exception ex) {
                            logger.error(
                                "Error browsing for Configuration file", ex);
                        }
                    }
                });

        browseForAnExistingConfigurationButton.setToolTipText(
            "Shows a File Open dialog to allow you to find a configuration file");
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(0, 0, 5, 0);
        panel.add(browseForAnExistingConfigurationButton, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(0, 5, 0, 5);
        panel.add(new JLabel(" Configuration file URL "), c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(existingConfigurationComboBox, c);


        return panel;
    }

    /**
     * Returns the current Model/state of the chosen options by the user.
     * @return model
     */
    PanelModel getModel() {
        return panelModel;
    }

    /**
     * Clients of this panel can configure the ActionListener to be used
     * when the user presses the OK button, so they can read
     * back this Panel's model top determine what to do.
     * @param actionListener listener which will be notified that ok was selected
     */
    void setCompletionActionListener(ActionListener actionListener) {
        completionActionListener = actionListener;
    }

    private void updateEnabledState(Component component) {
        lowerPanel.removeAll();
        if (component == useExistingConfigurationRadioButton) {
            lowerPanel.add(useExistingConfigurationPanel, BorderLayout.NORTH);
        }
        if (component == networkReceiverRadioButton) {
            lowerPanel.add(networkReceiverPanel, BorderLayout.NORTH);
        }
        if (component == logFileReceiverRadioButton) {
            lowerPanel.add(logFileReceiverPanel, BorderLayout.NORTH);
        }
        lowerPanel.revalidate();
        lowerPanel.repaint();
    }

    /**
     * Returns the URL chosen by the user for a Configuration file
     * or null if they cancelled.
     */
    private URL browseConfig() throws MalformedURLException {

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Use an existing configuration file...");
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setFileFilter(new FileFilter() {
                public boolean accept(File f) {

                    return f.isDirectory() ||
                    f.getName().endsWith(".properties") ||
                    f.getName().endsWith(".xml");
                }

                public String getDescription() {

                    return "Log4j Configuration file";
                }
            });

        chooser.showOpenDialog(this);

        File selectedFile = chooser.getSelectedFile();

        if (selectedFile == null) {

            return null;
        }

        if (!selectedFile.exists() || !selectedFile.canRead()) {

            return null;
        }

        return chooser.getSelectedFile().toURI().toURL();
    }

    /**
     * Returns the URL chosen by the user for a Configuration file
     * or null if they cancelled.
     */
    private URL browseLogFile() throws MalformedURLException {

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Browse for a log file...");
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.showOpenDialog(this);

        File selectedFile = chooser.getSelectedFile();

        if (selectedFile == null) {
            return null;
        }

        if (!selectedFile.exists() || !selectedFile.canRead()) {
            return null;
        }

        return chooser.getSelectedFile().toURI().toURL();
    }

    public static void main(String[] args) {

        JFrame frame = new JFrame();
        frame.getContentPane().add(new ReceiverConfigurationPanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * @return Returns the dontWarnMeAgain.
     */
    public final boolean isDontWarnMeAgain() {

        return dontwarnIfNoReceiver.isSelected();
    }

    /**
     * This class represents the model of the chosen options the user
     * has configured.
     *
     */
    class PanelModel {

        private File file;
        private boolean cancelled;

        public PanelModel(){
            file = new File(SettingsManager.getInstance().getSettingsDirectory(), "receiver-config.xml");
        }

        boolean isNetworkReceiverMode() {

            return !cancelled && networkReceiverRadioButton.isSelected();
        }

        int getNetworkReceiverPort() {

            return Integer.parseInt(networkReceiverPortComboBoxModel.getSelectedItem().toString());
        }

        Class getNetworkReceiverClass() throws ClassNotFoundException {
            return Class.forName(networkReceiverClassNameComboBoxModel.getSelectedItem().toString());
        }

        boolean isLoadConfig() {

            return !cancelled && useExistingConfigurationRadioButton.isSelected();
        }

        boolean isLoadSavedConfigs() {

            return !cancelled && useAutoSavedConfigRadioButton.isSelected();
        }

        boolean isLogFileReceiverConfig() {
            return !cancelled && logFileReceiverRadioButton.isSelected();
        }

        public Object[] getRememberedConfigs() {

            Object[] urls = new Object[existingConfigurationComboBoxModel.getSize()];

            for (int i = 0; i < existingConfigurationComboBoxModel.getSize(); i++) {
                urls[i] = existingConfigurationComboBoxModel.getElementAt(i);
            }

            return urls;
        }

        public void setRememberedConfigs(final Object[] configs) {
            SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        existingConfigurationComboBoxModel.removeAllElements();

                        for (int i = 0; i < configs.length; i++) {
                            existingConfigurationComboBoxModel.addElement(configs[i]);
                        }
                    }
                });
        }

        URL getConfigToLoad() {

            try
            {
                return new URL(existingConfigurationComboBoxModel.getSelectedItem().toString());
            }
            catch (MalformedURLException e)
            {
                return null;
            }
        }

        URL getSavedConfigToLoad() {
            try {
                return file.toURI().toURL();
            } catch (MalformedURLException e) {
                logger.error("Error loading saved configurations by Chainsaw", e);
            }
            return null;
        }

        URL getLogFileURL() {
            try
            {
                Object item = logFileURLTextField.getText();
                if (item != null) {
                    return new URL(item.toString());
                }
            }
            catch (MalformedURLException e)
            {
                logger.error("Error retrieving log file URL", e);
            }
            return null;
        }

        String getLogFormat() {
            Object item = logFileFormatComboBox.getSelectedItem();
            if (item != null) {
                return item.toString();
            }
            return null;
        }

        boolean isPatternLayoutLogFormat() {
            Object item = logFileFormatTypeComboBox.getSelectedItem();
            return item != null && item.toString().toLowerCase().contains("patternlayout");
        }

        String getLogFormatTimestampFormat() {
            Object item = logFileFormatTimestampFormatComboBox.getSelectedItem();
            if (item != null) {
                return item.toString();
            }

            return null;
        }

        public void setRememberedLayouts(Object[] layouts)
        {
            //TODO: implement
            //add an entry to the logformat, formattype and timestamp fields

        }

        public Object[] getRememberedLayouts()
        {
            //TODO: implement
            return new Object[0];
        }

        public void setCancelled()
        {
            cancelled = true;
        }
    }
}
