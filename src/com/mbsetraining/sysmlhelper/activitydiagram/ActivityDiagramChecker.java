package com.mbsetraining.sysmlhelper.activitydiagram;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.mbsetraining.sysmlhelper.common.ConfigurationSettings;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class ActivityDiagramChecker extends JFrame{

	private ActionList _actionsInfos;
	private JPanel _panel;
	private JTable _table;
	private JScrollPane _scrollPane;
	private MouseListener _listener;
	private List<ActionInfo> _checkedElements; 
	private ConfigurationSettings _context;
	
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		ExecutableMBSE_Context context = new ExecutableMBSE_Context(theRhpApp.getApplicationConnectionString());
		ActivityDiagramChecker.launchPanelsFor( theRhpApp.getListOfSelectedElements().toList(), context );

	}
	
	public static void launchPanelsFor(
			List<IRPModelElement> theSelectedEls,
			ConfigurationSettings context ){

		List<IRPActivityDiagram> theADs = context.buildListOfActivityDiagramsFor(theSelectedEls);

		context.debug("There are " + theADs.size() + " Activity Diagrams nested under the selected list");

		for (IRPActivityDiagram theAD : theADs) {

			IRPFlowchart theFC = (IRPFlowchart) theAD.getOwner();
			context.debug("Check Activity Diagram was invoked for " + context.elInfo( theFC ));

			ActivityDiagramChecker theChecker = 
					new ActivityDiagramChecker( context.get_rhpAppID(), theAD.getGUID() );

			theChecker.setVisible( true );
		}
	}
	
	public static void launchThePanel(
			final String theAppID,
			IRPActivityDiagram forAD ){

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame( "Activity diagram checker" );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				ActivityDiagramChecker thePanel = 
						new ActivityDiagramChecker( theAppID, forAD.getGUID() );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}

	public ActivityDiagramChecker(
			String theAppID,
			String theActivityDiagramGUID ){

		_context = new ExecutableMBSE_Context( theAppID );

		IRPActivityDiagram theAD = (IRPActivityDiagram) _context.get_rhpPrj().findElementByGUID( theActivityDiagramGUID );
		IRPFlowchart theFC = (IRPFlowchart) theAD.getOwner();
		
		_context.debug( "ActivityDiagramChecker was invoked for " + _context.elInfo( theFC ) );

		_actionsInfos = new ActionList( theAD, _context );

		if( _actionsInfos.isRenamingNeeded() ){

			JDialog.setDefaultLookAndFeelDecorated(true);

			String theMsg = "The checker has detected that " + _actionsInfos.getNumberOfRenamesNeeded() + 
					" elements require renaming. Do you want to rename them before producing the report?";

			int response = JOptionPane.showConfirmDialog(null, 
					theMsg, "Rename check for " + _context.elInfo(theFC),
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

			if (response == JOptionPane.CANCEL_OPTION){
				_context.debug("Operation was cancelled by user with no changes made.");
			} else {
				if (response == JOptionPane.YES_OPTION) {
					_actionsInfos.performRenames();
				} else if (response == JOptionPane.NO_OPTION){
					_context.debug("Info: User chose not rename the actions.");
				} 
			}
		}	

		_checkedElements = _actionsInfos.getListOfActionsCheckedForTraceability();
		buildFrameUsing( _checkedElements, "Traceability report for " + theFC.getName() );

		_context.debug(
				"ActivityDiagramChecker has finished (" + _actionsInfos.getNumberOfTraceabilityFailures() + 
				" out of " + _actionsInfos.size() + " elements failed traceability checks)");	
	}

	private void buildFrameUsing(
			List<ActionInfo> theList, 
			String withTitle) {

		// Set the frame characteristics
		setTitle( withTitle );
		setSize( 500, 200 );
		setBackground( Color.gray );

		// Create a panel to hold all other components
		_panel = new JPanel();
		_panel.setLayout( new BorderLayout() );
		getContentPane().add( _panel );

		// Create columns names
		String columnNames[] = { "Name", "Type", "Status", "Comment" };

		// Create some data
		String dataValues[][] = new String[theList.size()][4];

		for (int i = 0; i < theList.size(); i++) {

			ActionInfo theInfo = theList.get(i);
			IRPModelElement theEl = theInfo.getTheElement();

			dataValues[i][0] = theEl.getName();
			dataValues[i][1] = theInfo.getType();

			if (theInfo.isTraceabilityFailure()){
				dataValues[i][2] = "FAIL";
				dataValues[i][3] = theInfo.getComment();//"Traceability is missing";
			} else {
				dataValues[i][2] = "PASS";
				dataValues[i][3] = theInfo.getComment();
			}
		}

		// Create a new table instance
		_table = new JTable( dataValues, columnNames ){
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int row, int column) {                
				return false;               
			};
		};

		_table.getColumnModel().getColumn(0).setPreferredWidth(1000);
		_table.getColumnModel().getColumn(1).setPreferredWidth(600);
		_table.getColumnModel().getColumn(2).setPreferredWidth(300);
		_table.getColumnModel().getColumn(3).setPreferredWidth(1500);

		// Add the table to a scrolling pane
		_scrollPane = new JScrollPane( _table );

		_panel.add( _scrollPane, BorderLayout.CENTER );

		int xSize = 600;
		int ySize = (theList.size()*18)+40;
		if (ySize > 600) ySize = 600;

		_panel.setPreferredSize(new Dimension(xSize, ySize));

		add(_panel);
		pack();

		_listener = new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {

				int theRow = _table.rowAtPoint(e.getPoint());
				int theColumn = _table.columnAtPoint(e.getPoint());

				if (theRow >= 0 && theColumn >= 0){

					ActionInfo theInfo = _checkedElements.get(theRow);
					IRPModelElement theEl = theInfo.getTheElement();

					_context.debug("Highlighting " + _context.elInfo(theEl) + " on the diagram.");
					theEl.highLightElement();
				}
			}
		};

		_table.addMouseListener(_listener);
	}
}

/**
 * Copyright (C) 2016-2021  MBSE Training and Consulting Limited (www.executablembse.com)

    This file is part of SysMLHelperPlugin.

    SysMLHelperPlugin is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SysMLHelperPlugin is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SysMLHelperPlugin.  If not, see <http://www.gnu.org/licenses/>.
 */
