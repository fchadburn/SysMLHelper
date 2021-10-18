package com.mbsetraining.sysmlhelper.activitydiagram;

import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import com.mbsetraining.sysmlhelper.common.ConfigurationSettings;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class RenameActions {

	protected ConfigurationSettings _context;
	protected int _renameDialogChoice = -999;
	protected int _infoChoice = -999;
	protected JCheckBox _dontAskAgainCheckBox;

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {

		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		ExecutableMBSE_Context context = new ExecutableMBSE_Context(theRhpApp.getApplicationConnectionString());

		RenameActions theRenamer = new RenameActions(context);
		theRenamer.performRenamesFor(theRhpApp.getListOfSelectedElements().toList());
	}

	public RenameActions( 
			ConfigurationSettings context ) {

		_context = context;
	}

	public void performRenamesFor(
			List<IRPModelElement> theSelectedEls){

		List<IRPActivityDiagram> theADs = _context.buildListOfActivityDiagramsFor(theSelectedEls);

		_context.info("There are " + theADs.size() + " Activity Diagrams nested under the selected list");

		_dontAskAgainCheckBox = new JCheckBox("Don't ask again.");

		for (IRPActivityDiagram theAD : theADs) {
			
			IRPFlowchart theFC = (IRPFlowchart) theAD.getOwner();

			if( _renameDialogChoice == JOptionPane.CANCEL_OPTION ||
					_infoChoice == JOptionPane.CANCEL_OPTION ){
				
				_context.debug("Rename actions is ignoring " + _context.elInfo(theFC) + " as user chose to cancel");

			} else {

				_context.info("Rename actions invoked for " + _context.elInfo( theFC ));

				ActionList actionsInfos = new ActionList( theAD, _context );		

				if (actionsInfos.isRenamingNeeded()){

					if( !_dontAskAgainCheckBox.isSelected() || 
							_renameDialogChoice == -999 ){
					
						JDialog.setDefaultLookAndFeelDecorated(true);

						String theMsg = "The checker has detected that " + actionsInfos.getNumberOfRenamesNeeded() + 
								" elements require renaming. Do you want to rename them?";
						
						Object[] params = {theMsg, _dontAskAgainCheckBox};
						
						_renameDialogChoice = JOptionPane.showConfirmDialog(null,//parent container of JOptionPane
								params,
								"Rename for " + _context.elInfo(theFC),
								JOptionPane.YES_NO_CANCEL_OPTION,//the titles of buttons
								JOptionPane.QUESTION_MESSAGE);//default button title
					}

					if (_renameDialogChoice == JOptionPane.CANCEL_OPTION){
						_context.info("Operation was cancelled by user with no changes made.");
					} else {
						if (_renameDialogChoice == JOptionPane.YES_OPTION) {
							actionsInfos.performRenames();
						} else if (_renameDialogChoice == JOptionPane.NO_OPTION){
							_context.info("Info: User chose not rename the actions.");
						} 
					}

				} else {

					String theMsg = "No action necessary. The checker has checked " + actionsInfos.size() + 
							" elements on \n" + _context.elInfo(theFC);

					// Don't show messages if response was yes all
					if( _dontAskAgainCheckBox.isSelected() && 
							_infoChoice == JOptionPane.OK_OPTION ){
						
						_context.info(theMsg);
					} else {
						JDialog.setDefaultLookAndFeelDecorated(true);
						
						Object[] params = {theMsg, _dontAskAgainCheckBox};
						
						_infoChoice = JOptionPane.showConfirmDialog(null,//parent container of JOptionPane
								params,
								"Rename for " + _context.elInfo(theFC),
								JOptionPane.OK_CANCEL_OPTION,//the titles of buttons
								JOptionPane.INFORMATION_MESSAGE);//default button title	
						
						if (_infoChoice == JOptionPane.CANCEL_OPTION){
							_context.info("Operation was cancelled by user with no changes made.");
						}
					}
				}
				
				_context.debug("Rename actions has finished (" + actionsInfos.getNumberOfRenamesNeeded() + 
						" out of " + actionsInfos.size() + " elements required renaming)");
			}
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

