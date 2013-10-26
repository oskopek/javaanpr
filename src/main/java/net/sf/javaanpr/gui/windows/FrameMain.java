/*
------------------------------------------------------------------------
JavaANPR - Automatic Number Plate Recognition System for Java
------------------------------------------------------------------------

This file is a part of the JavaANPR, licensed under the terms of the
Educational Community License

Copyright (c) 2006-2007 Ondrej Martinsky. All rights reserved

This Original Work, including software, source code, documents, or
other related items, is being provided by the copyright holder(s)
subject to the terms of the Educational Community License. By
obtaining, using and/or copying this Original Work, you agree that you
have read, understand, and will comply with the following terms and
conditions of the Educational Community License:

Permission to use, copy, modify, merge, publish, distribute, and
sublicense this Original Work and its documentation, with or without
modification, for any purpose, and without fee or royalty to the
copyright holder(s) is hereby granted, provided that you include the
following on ALL copies of the Original Work or portions thereof,
including modifications or derivatives, that you make:

# The full text of the Educational Community License in a location
viewable to users of the redistributed or derivative work.

# Any pre-existing intellectual property disclaimers, notices, or terms
and conditions.

# Notice of any changes or modifications to the Original Work,
including the date the changes were made.

# Any modifications of the Original Work must be distributed in such a
manner as to avoid any confusion with the Original Work of the
copyright holders.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

The name and trademarks of copyright holder(s) may NOT be used in
advertising or publicity pertaining to the Original or Derivative Works
without specific, written prior permission. Title to copyright in the
Original Work and any associated documentation will at all times remain
with the copyright holders. 

If you want to alter upon this work, you MUST attribute it in 
a) all source files
b) on every place, where is the copyright of derivated work
exactly by the following label :

---- label begin ----
This work is a derivate of the JavaANPR. JavaANPR is a intellectual 
property of Ondrej Martinsky. Please visit http://javaanpr.sourceforge.net 
for more info about JavaANPR. 
----  label end  ----

------------------------------------------------------------------------
                                         http://javaanpr.sourceforge.net
------------------------------------------------------------------------
 */

package net.sf.javaanpr.gui.windows;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

import net.sf.javaanpr.gui.tools.FileListModel;
import net.sf.javaanpr.gui.tools.ImageFileFilter;
import net.sf.javaanpr.imageanalysis.CarSnapshot;
import net.sf.javaanpr.imageanalysis.Photo;
import net.sf.javaanpr.jar.Main;

public class FrameMain extends javax.swing.JFrame {
    static final long serialVersionUID = 0;

    public class RecognizeThread extends Thread {
        FrameMain parentFrame = null;

        public RecognizeThread(FrameMain parentFrame) {
            this.parentFrame = parentFrame;
        }

        @Override
        public void run() {
            String recognizedText = "";
            this.parentFrame.recognitionLabel.setText("processing ...");
            int index = this.parentFrame.selectedIndex;
            try {
                recognizedText = Main.systemLogic.recognize(this.parentFrame.car);
            } catch (Exception ex) {
                this.parentFrame.recognitionLabel.setText("");
                return;
            }
            this.parentFrame.recognitionLabel.setText(recognizedText);
            this.parentFrame.fileListModel.fileList.elementAt(index).recognizedPlate = recognizedText;
        }
    }

    public class LoadImageThread extends Thread {
        FrameMain parentFrame = null;
        String url = null;

        public LoadImageThread(FrameMain parentFrame, String url) {
            this.parentFrame = parentFrame;
            this.url = url;
        }

        @Override
        public void run() {
            try {
                this.parentFrame.car = new CarSnapshot(this.url);
                this.parentFrame.panelCarContent = this.parentFrame.car.duplicate().getBi();
                this.parentFrame.panelCarContent = Photo.linearResizeBi(this.parentFrame.panelCarContent,
                        this.parentFrame.panelCar.getWidth(), this.parentFrame.panelCar.getHeight());
                this.parentFrame.panelCar.paint(this.parentFrame.panelCar.getGraphics());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    CarSnapshot car;
    BufferedImage panelCarContent;

    JFileChooser fileChooser;
    private FileListModel fileListModel;
    int selectedIndex = -1;

    /** Creates new form MainFrame */
    public FrameMain() {
        this.initComponents();

        // init : file chooser
        this.fileChooser = new JFileChooser();
        this.fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        this.fileChooser.setFileFilter(new ImageFileFilter());

        // init : window dimensions and visibility
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = this.getWidth();
        int height = this.getHeight();
        this.setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2);
        this.setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed"
    // desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        this.recognitionLabel = new javax.swing.JLabel();
        this.panelCar = new JPanel() {
            static final long serialVersionUID = 0;

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                g.drawImage(FrameMain.this.panelCarContent, 0, 0, null);
            }
        };
        this.fileListScrollPane = new javax.swing.JScrollPane();
        this.fileList = new javax.swing.JList<Object>();
        this.recognizeButton = new javax.swing.JButton();
        this.bottomLine = new javax.swing.JLabel();
        this.menuBar = new javax.swing.JMenuBar();
        this.imageMenu = new javax.swing.JMenu();
        this.openDirectoryItem = new javax.swing.JMenuItem();
        this.exitItem = new javax.swing.JMenuItem();
        this.helpMenu = new javax.swing.JMenu();
        this.aboutItem = new javax.swing.JMenuItem();
        this.helpItem = new javax.swing.JMenuItem();

        this.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        this.setTitle("JavaANPR");
        this.setResizable(false);
        this.recognitionLabel.setBackground(new java.awt.Color(0, 0, 0));
        this.recognitionLabel.setFont(new java.awt.Font("Arial", 0, 24));
        this.recognitionLabel.setForeground(new java.awt.Color(255, 204, 51));
        this.recognitionLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        this.recognitionLabel.setText(null);
        this.recognitionLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        this.recognitionLabel.setOpaque(true);

        this.panelCar.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        org.jdesktop.layout.GroupLayout panelCarLayout = new org.jdesktop.layout.GroupLayout(this.panelCar);
        this.panelCar.setLayout(panelCarLayout);
        panelCarLayout.setHorizontalGroup(panelCarLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(0, 585, Short.MAX_VALUE));
        panelCarLayout.setVerticalGroup(panelCarLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(0, 477, Short.MAX_VALUE));

        this.fileListScrollPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        this.fileListScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        this.fileList.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        this.fileList.setFont(new java.awt.Font("Arial", 0, 11));
        this.fileList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            @Override
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                FrameMain.this.fileListValueChanged(evt);
            }
        });

        this.fileListScrollPane.setViewportView(this.fileList);

        this.recognizeButton.setFont(new java.awt.Font("Arial", 0, 11));
        this.recognizeButton.setText("recognize plate");
        this.recognizeButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FrameMain.this.recognizeButtonActionPerformed(evt);
            }
        });

        this.bottomLine.setFont(new java.awt.Font("Arial", 0, 11));
        this.bottomLine.setText("Copyright (c) 2006 Ondrej Martinsky");

        this.menuBar.setFont(new java.awt.Font("Arial", 0, 11));
        this.imageMenu.setText("Image");
        this.imageMenu.setFont(new java.awt.Font("Arial", 0, 11));
        this.openDirectoryItem.setFont(new java.awt.Font("Arial", 0, 11));
        this.openDirectoryItem.setText("Load snapshots from directory");
        this.openDirectoryItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FrameMain.this.openDirectoryItemActionPerformed(evt);
            }
        });

        this.imageMenu.add(this.openDirectoryItem);

        this.exitItem.setFont(new java.awt.Font("Arial", 0, 11));
        this.exitItem.setText("Exit");
        this.exitItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FrameMain.this.exitItemActionPerformed(evt);
            }
        });

        this.imageMenu.add(this.exitItem);

        this.menuBar.add(this.imageMenu);

        this.helpMenu.setText("Help");
        this.helpMenu.setFont(new java.awt.Font("Arial", 0, 11));
        this.helpMenu.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FrameMain.this.helpMenuActionPerformed(evt);
            }
        });

        this.aboutItem.setFont(new java.awt.Font("Arial", 0, 11));
        this.aboutItem.setText("About");
        this.aboutItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FrameMain.this.aboutItemActionPerformed(evt);
            }
        });

        this.helpMenu.add(this.aboutItem);

        this.helpItem.setFont(new java.awt.Font("Arial", 0, 11));
        this.helpItem.setText("Help");
        this.helpItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FrameMain.this.helpItemActionPerformed(evt);
            }
        });

        this.helpMenu.add(this.helpItem);

        this.menuBar.add(this.helpMenu);

        this.setJMenuBar(this.menuBar);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout
                                .createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, this.bottomLine,
                                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, this.panelCar,
                                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout
                                .createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                .add(this.fileListScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 190,
                                        Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, this.recognitionLabel,
                                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                                .add(this.recognizeButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 190,
                                        Short.MAX_VALUE)).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout
                                .createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(layout
                                        .createSequentialGroup()
                                        .add(this.fileListScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                402, Short.MAX_VALUE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(this.recognizeButton)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(this.recognitionLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 44,
                                                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(this.panelCar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(this.bottomLine)));
        this.pack();
    }// </editor-fold>//GEN-END:initComponents

    private void helpMenuActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_helpMenuActionPerformed
    }// GEN-LAST:event_helpMenuActionPerformed

    private void helpItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_helpItemActionPerformed
        new FrameHelp(FrameHelp.SHOW_HELP);
    }// GEN-LAST:event_helpItemActionPerformed

    private void aboutItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_aboutItemActionPerformed
        new FrameHelp(FrameHelp.SHOW_ABOUT);
    }// GEN-LAST:event_aboutItemActionPerformed

    private void recognizeButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_recognizeButtonActionPerformed
        //String plate = null;

        // namiesto tohto urobime thread plate =
        // Main.systemLogic.recognize(this.car);
        // thread code start
        new RecognizeThread(this).start();
        // thread code end

        // this.fileListModel.fileList.elementAt(this.selectedIndex).recognizedPlate
        // = plate;
        // this.label.setText(plate);

    }// GEN-LAST:event_recognizeButtonActionPerformed

    private void fileListValueChanged(javax.swing.event.ListSelectionEvent evt) {// GEN-FIRST:event_fileListValueChanged
        int selectedNow = this.fileList.getSelectedIndex();

        if ((selectedNow != -1) && (this.selectedIndex != selectedNow)) {
            this.recognitionLabel.setText(this.fileListModel.fileList.elementAt(selectedNow).recognizedPlate);
            this.selectedIndex = selectedNow;
            // proceed selectedNow
            String path = ((FileListModel.FileListModelEntry) this.fileListModel.getElementAt(selectedNow)).fullPath;
            // this.showImage(path);
            new LoadImageThread(this, path).start();
        }
    }// GEN-LAST:event_fileListValueChanged

    private void exitItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_exitItemActionPerformed
        System.exit(0);
    }// GEN-LAST:event_exitItemActionPerformed

    private void openDirectoryItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_openDirectoryItemActionPerformed
        int returnValue;
        String fileURL;

        this.fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        this.fileChooser.setDialogTitle("Load snapshots from directory");
        returnValue = this.fileChooser.showOpenDialog((Component) evt.getSource());

        if (returnValue != JFileChooser.APPROVE_OPTION) {
            return;
        }

        fileURL = this.fileChooser.getSelectedFile().getAbsolutePath();
        File selectedFile = new File(fileURL);

        this.fileListModel = new FileListModel();
        for (String fileName : selectedFile.list()) {
            if (!ImageFileFilter.accept(fileName)) {
                continue; // not a image
            }
            this.fileListModel.addFileListModelEntry(fileName, selectedFile + File.separator + fileName);
        }
        this.fileList.setModel(this.fileListModel);

    }// GEN-LAST:event_openDirectoryItemActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutItem;
    private javax.swing.JLabel bottomLine;
    private javax.swing.JMenuItem exitItem;
    private javax.swing.JList<Object> fileList;
    private javax.swing.JScrollPane fileListScrollPane;
    private javax.swing.JMenuItem helpItem;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenu imageMenu;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem openDirectoryItem;
    private javax.swing.JPanel panelCar;
    private javax.swing.JLabel recognitionLabel;
    private javax.swing.JButton recognizeButton;
    // End of variables declaration//GEN-END:variables

}