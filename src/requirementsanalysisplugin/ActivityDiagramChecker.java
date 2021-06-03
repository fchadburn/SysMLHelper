package requirementsanalysisplugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class ActivityDiagramChecker extends JFrame{

	private ActionList actionsInfos;
	private JPanel thePanel;
	private JTable theTable;
	private JScrollPane theScrollPane;
	private MouseListener theListener;
	private List<ActionInfo> theCheckedElements; 
	private ExecutableMBSE_Context _context;
	
	private static final long serialVersionUID = 1L;

	// testing only
	public static void main(String[] args) {
		List<IRPModelElement> theEls = new ArrayList<IRPModelElement>();
		theEls.add( RhapsodyAppServer.getActiveRhapsodyApplication().getSelectedElement() );
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		ExecutableMBSE_Context context = new ExecutableMBSE_Context( theRhpApp.getApplicationConnectionString() );
		createActivityDiagramCheckersFor( theEls,context );
	}

	public static void launchThePanel(
			final String theAppID,
			IRPActivityDiagram forAD ){

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame( "Create new Actor" );

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

		actionsInfos = new ActionList( theAD, _context );

		if( actionsInfos.isRenamingNeeded() ){

			JDialog.setDefaultLookAndFeelDecorated(true);

			String theMsg = "The checker has detected that " + actionsInfos.getNumberOfRenamesNeeded() + 
					" elements require renaming. Do you want to rename them before producing the report?";

			int response = JOptionPane.showConfirmDialog(null, 
					theMsg, "Rename check for " + _context.elInfo(theFC),
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

			if (response == JOptionPane.CANCEL_OPTION){
				_context.debug("Operation was cancelled by user with no changes made.");
			} else {
				if (response == JOptionPane.YES_OPTION) {
					actionsInfos.performRenames();
				} else if (response == JOptionPane.NO_OPTION){
					_context.debug("Info: User chose not rename the actions.");
				} 
			}
		}	

		theCheckedElements = actionsInfos.getListOfActionsCheckedForTraceability();
		buildFrameUsing( theCheckedElements, "Traceability report for " + theFC.getName() );

		_context.debug(
				"ActivityDiagramChecker has finished (" + actionsInfos.getNumberOfTraceabilityFailures() + 
				" out of " + actionsInfos.size() + " elements failed traceability checks)");	
	}

	private void buildFrameUsing(
			List<ActionInfo> theList, 
			String withTitle) {

		// Set the frame characteristics
		setTitle( withTitle );
		setSize( 500, 200 );
		setBackground( Color.gray );

		// Create a panel to hold all other components
		thePanel = new JPanel();
		thePanel.setLayout( new BorderLayout() );
		getContentPane().add( thePanel );

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
		theTable = new JTable( dataValues, columnNames ){
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int row, int column) {                
				return false;               
			};
		};

		theTable.getColumnModel().getColumn(0).setPreferredWidth(1000);
		theTable.getColumnModel().getColumn(1).setPreferredWidth(600);
		theTable.getColumnModel().getColumn(2).setPreferredWidth(300);
		theTable.getColumnModel().getColumn(3).setPreferredWidth(1500);

		// Add the table to a scrolling pane
		theScrollPane = new JScrollPane( theTable );

		thePanel.add( theScrollPane, BorderLayout.CENTER );

		int xSize = 600;
		int ySize = (theList.size()*18)+40;
		if (ySize > 600) ySize = 600;

		thePanel.setPreferredSize(new Dimension(xSize, ySize));

		add(thePanel);
		pack();

		theListener = new MouseListener() {

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

				int theRow = theTable.rowAtPoint(e.getPoint());
				int theColumn = theTable.columnAtPoint(e.getPoint());

				if (theRow >= 0 && theColumn >= 0){

					ActionInfo theInfo = theCheckedElements.get(theRow);
					IRPModelElement theEl = theInfo.getTheElement();

					_context.debug("Highlighting " + _context.elInfo(theEl) + " on the diagram.");
					theEl.highLightElement();
				}
			}
		};

		theTable.addMouseListener(theListener);
	}

	static public void createActivityDiagramCheckersFor(
			List<IRPModelElement> theSelectedEls,
			ExecutableMBSE_Context context ){

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
