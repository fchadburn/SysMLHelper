package com.mbsetraining.sysmlhelper.usecasepackage;

import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class CreateContextPkgChooser {
	
	protected JComboBox<Object> _userChoiceComboBox = new JComboBox<Object>();
	
	protected JTextField _nameTextField = new JTextField();
	protected IRPPackage _ownerPkg = null;
	protected IRPProject _project = null;
	protected final String _doNothingOption = "Skip creation of a context package";
	protected final String _createNewBasedOnExistingOption = "Create new context package based on ";
	protected final String _createNewWithNameOption = "Create new context package based on system usage ";
	protected final String _none = "<None>";
	protected List<IRPModelElement> _existingSystemBlocks;
	protected ExecutableMBSE_Context _context;
	
	public CreateContextPkgChooser(
			final IRPPackage theOwnerPkg,
			ExecutableMBSE_Context context ){
		
		_context = context;
		
		final String theDefaultName = _context.getDefaultContextDiagramPackageName();
		
		_ownerPkg = theOwnerPkg;
		_project = theOwnerPkg.getProject();
		
		_existingSystemBlocks = _context.
				findElementsWithMetaClassAndStereotype(
						"Class", 
						_context.SYSTEM_BLOCK, 
						_project, 
						1 );
				
		_userChoiceComboBox.addItem( _doNothingOption );	
		_userChoiceComboBox.addItem( _createNewWithNameOption );
		
		// set default to create new package
		_userChoiceComboBox.setSelectedItem( _doNothingOption );
		
		String theUniqueName = 
				_context.determineUniqueNameBasedOn( 
						theDefaultName, "Package", theOwnerPkg );
		
		_nameTextField.setText( theUniqueName );
		_nameTextField.setEnabled( false );	
		
		if( !_existingSystemBlocks.isEmpty() ){	
			
			for( IRPModelElement theSystemBlocks : _existingSystemBlocks ){
				_userChoiceComboBox.addItem( _createNewBasedOnExistingOption + theSystemBlocks.getName() );
			}

			String theBlockName = _existingSystemBlocks.get(0).getName();
			 
			/// set default to first in list
			_userChoiceComboBox.setSelectedItem( _createNewBasedOnExistingOption + theBlockName );
    		_nameTextField.setText( theBlockName );
			_nameTextField.setEnabled( false );	
		}
	}
	
	public JTextField getM_NameTextField() {
		return _nameTextField;
	}

	public JComboBox<Object> getM_UserChoiceComboBox() {
		return _userChoiceComboBox;
	}
	
	public IRPClassifier getExistingBlockIfChosen(){
		
		IRPClassifier theExistingBlock = null;
		
		String theUserChoice = (String) _userChoiceComboBox.getSelectedItem();
		
		if( theUserChoice.contains( _createNewBasedOnExistingOption ) ){
			
			// Strip off prefix to get the chosen package name
			String theChosenPkg = theUserChoice.replace( 
					_createNewBasedOnExistingOption, "" );

			for( IRPModelElement theSystemBlock : _existingSystemBlocks ){
				
				if( theSystemBlock instanceof IRPClassifier &&
						theSystemBlock.getName().equals( theChosenPkg ) ){
					
					theExistingBlock = (IRPClassifier) theSystemBlock;
					break;
				}
			}	
			
		}

		return theExistingBlock;
	}	
}

/**
 * Copyright (C) 2023  MBSE Training and Consulting Limited (www.executablembse.com)

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