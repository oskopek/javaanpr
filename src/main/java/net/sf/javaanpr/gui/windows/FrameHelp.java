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
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;

public class FrameHelp extends JFrame {

    public enum FrameHelpContent {
        SHOW_HELP,
        SHOW_ABOUT
    }

    private static final long serialVersionUID = 0L;

    private JEditorPane editorPane;

    /**
     * Creates new form FrameHelp.
     *
     * @param frameHelpContent the frameHelpContent
     * @throws IOException in case the file to show in given frameHelpContent failed to load
     */
    public FrameHelp(FrameHelpContent frameHelpContent) throws IOException { // TODO javadoc
        initComponents();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = getWidth();
        int height = getHeight();
        setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2);
        if (frameHelpContent == FrameHelpContent.SHOW_ABOUT) {
            URL url = getClass().getResource(Configurator.getConfigurator().getPathProperty("help_file_about"));
            editorPane.setPage(url);
        } else if (frameHelpContent == FrameHelpContent.SHOW_HELP) {
            URL url = getClass().getResource(Configurator.getConfigurator().getPathProperty("help_file_help"));
            editorPane.setPage(url);
        }
        setVisible(true);
    }

    private void initComponents() {
        JScrollPane jScrollPane1 = new JScrollPane();
        editorPane = new JEditorPane();
        JButton helpWindowClose = new JButton();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("JavaANPR - Help");
        setResizable(false);
        jScrollPane1.setViewportView(editorPane);

        helpWindowClose.setFont(new java.awt.Font("Arial", 0, 11));
        helpWindowClose.setText("Close");
        helpWindowClose.addActionListener(this::helpWindowCloseActionPerformed);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.LEADING)
                .add(layout.createSequentialGroup().addContainerGap()
                        .add(layout.createParallelGroup(GroupLayout.LEADING)
                                .add(GroupLayout.TRAILING, helpWindowClose)
                                .add(jScrollPane1, GroupLayout.DEFAULT_SIZE, 514, Short.MAX_VALUE))
                        .addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.LEADING).add(GroupLayout.TRAILING,
                layout.createSequentialGroup().addContainerGap()
                        .add(jScrollPane1, GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.RELATED).add(helpWindowClose).addContainerGap()));
        pack();
    }

    private void helpWindowCloseActionPerformed(ActionEvent evt) {
        dispose();
    }
}
