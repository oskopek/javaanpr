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

import net.sf.javaanpr.configurator.GlobalState;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;

public class FrameHelp extends JFrame {

    public enum MODE {
        SHOW_HELP,
        SHOW_ABOUT
    }

    private static final long serialVersionUID = 0L;
    public MODE mode;

    private JEditorPane editorPane;
    private JButton helpWindowClose;
    private JScrollPane jScrollPane1;

    /**
     * Creates new form FrameHelp.
     *
     * @param mode the mode
     * @throws IOException in case the file to show in given mode failed to load
     */
    public FrameHelp(MODE mode) throws IOException { // TODO javadoc
        initComponents();
        this.mode = mode;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = getWidth();
        int height = getHeight();
        setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2);
        if (mode == MODE.SHOW_ABOUT) {
            URL url = getClass().getResource(GlobalState.getInstance().getConfigurator().getPathProperty("help_file_about"));
            editorPane.setPage(url);
        } else if (mode == MODE.SHOW_HELP) {
            URL url = getClass().getResource(GlobalState.getInstance().getConfigurator().getPathProperty("help_file_help"));
            editorPane.setPage(url);
        }
        setVisible(true);
    }

    private void initComponents() {
        jScrollPane1 = new JScrollPane();
        editorPane = new JEditorPane();
        helpWindowClose = new JButton();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("JavaANPR - Help");
        setResizable(false);
        jScrollPane1.setViewportView(editorPane);

        helpWindowClose.setFont(new java.awt.Font("Arial", 0, 11));
        helpWindowClose.setText("Close");
        helpWindowClose.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpWindowCloseActionPerformed(evt);
            }
        });

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
