package com.mbsetraining.sysmlhelper.activitydiagram;

import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import com.mbsetraining.sysmlhelper.common.ConfigurationSettings;
import com.telelogic.rhapsody.core.*;
 
public class RenameActions {
 
	ConfigurationSettings _context;
	
	public RenameActions( 
			ConfigurationSettings context ) {
		
		_context = context;
	}
	
	public void performRenamesFor(
			List<IRPModelElement> theSelectedEls){
		 
		List<IRPActivityDiagram> theADs = _context.buildListOfActivityDiagramsFor(theSelectedEls);
		
		_context.info("There are " + theADs.size() + " Activity Diagrams nested under the selected list");
		
		for (IRPActivityDiagram theAD : theADs) {
			
			IRPFlowchart theFC = (IRPFlowchart) theAD.getOwner();
			_context.info("Rename actions invoked for " + _context.elInfo( theFC ));
			
			ActionList actionsInfos = new ActionList( theAD, _context );		
					
			if (actionsInfos.isRenamingNeeded()){

				JDialog.setDefaultLookAndFeelDecorated(true);

				String theMsg = "The checker has detected that " + actionsInfos.getNumberOfRenamesNeeded() + 
						" elements require renaming. Do you want to rename them?";

				int response = JOptionPane.showConfirmDialog(null, 
						theMsg, "Rename for " + _context.elInfo(theFC),
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

				if (response == JOptionPane.CANCEL_OPTION){
					_context.info("Operation was cancelled by user with no changes made.");
				} else {
					if (response == JOptionPane.YES_OPTION) {
						actionsInfos.performRenames();
					} else if (response == JOptionPane.NO_OPTION){
						_context.info("Info: User chose not rename the actions.");
					} 
				}

			} else {
				JDialog.setDefaultLookAndFeelDecorated(true);
				
				String theMsg = "No action necessary. The checker has checked " + actionsInfos.size() + 
						" elements on the diagram.";
				
				JOptionPane.showMessageDialog(null, theMsg, "Rename for " + _context.elInfo(theFC), JOptionPane.INFORMATION_MESSAGE);
			}
			
			_context.debug("Rename actions has finished (" + actionsInfos.getNumberOfRenamesNeeded() + " out of " + actionsInfos.size() + " elements required renaming)");
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

