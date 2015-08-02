/*
 * Copyright 2013 JavaANPR contributors
 * Copyright 2006 Ondrej Martinsky
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package net.sf.javaanpr.gui.windows;

import net.sf.javaanpr.configurator.Configurator;

import java.awt.*;
import java.net.URL;

public class FrameHelp extends javax.swing.JFrame {
    static final long serialVersionUID = 0;

    public static int SHOW_HELP = 0;
    public static int SHOW_ABOUT = 1;
    public int mode;

    /**
     * Creates new form FrameHelp.
     *
     * @param mode the mode
     */
    public FrameHelp(int mode) { // TODO javadoc
        this.initComponents();
        this.mode = mode;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = this.getWidth();
        int height = this.getHeight();
        this.setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2);
        try {
            if (mode == FrameHelp.SHOW_ABOUT) {
                URL url = getClass().getResource(Configurator.getConfigurator().getPathProperty("help_file_about"));
                System.out.println(url);
                this.editorPane.setPage(url);
            } else {
                URL url = getClass().getResource(Configurator.getConfigurator().getPathProperty("help_file_help"));
                System.out.println(url);
                this.editorPane.setPage(url);
            }
        } catch (Exception e) {
            this.dispose();
        }
        this.setVisible(true);
    }

    // <editor-fold defaultstate="collapsed"
    // desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        this.jScrollPane1 = new javax.swing.JScrollPane();
        this.editorPane = new javax.swing.JEditorPane();
        this.helpWindowClose = new javax.swing.JButton();

        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.setTitle("JavaANPR");
        this.setResizable(false);
        this.jScrollPane1.setViewportView(this.editorPane);

        this.helpWindowClose.setFont(new java.awt.Font("Arial", 0, 11));
        this.helpWindowClose.setText("Close");
        this.helpWindowClose.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FrameHelp.this.helpWindowCloseActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout
                                .createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(org.jdesktop.layout.GroupLayout.TRAILING, this.helpWindowClose)
                                .add(this.jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 514,
                                        Short.MAX_VALUE)).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                org.jdesktop.layout.GroupLayout.TRAILING,
                layout.createSequentialGroup().addContainerGap()
                        .add(this.jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(this.helpWindowClose)
                        .addContainerGap()));
        this.pack();
    } // </editor-fold> //GEN-END:initComponents

    private void helpWindowCloseActionPerformed(
            java.awt.event.ActionEvent evt) { // GEN-FIRST:event_helpWindowCloseActionPerformed
        this.dispose();
    } // GEN-LAST:event_helpWindowCloseActionPerformed

    // Variables declaration - do not modify
    // GEN-BEGIN:variables
    private javax.swing.JEditorPane editorPane;
    private javax.swing.JButton helpWindowClose;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration //GEN-END:variables

}
