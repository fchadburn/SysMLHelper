package com.mbsetraining.sysmlhelper.contextdiagram;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class CreateSignalsPkgChooser {

	public JComboBox<Object> _userChoiceComboBox = new JComboBox<Object>();
	public JTextField _nameTextField = new JTextField();
	public final String _doNothingOption = "Skip creation of a shared external signals package";
	public final String _createNewButEmptyOption = "Create new empty external signals package";
	public final String _existingPkgPrefix = "Use external signals from existing package called ";
	protected IRPPackage _ownerPkg = null;
	protected IRPProject _project = null;
	protected final String _none = "<None>";
	protected List<IRPModelElement> _existingPkgs;
	
	private ExecutableMBSE_Context _context;
	
	public CreateSignalsPkgChooser(
			final IRPPackage theOwnerPkg,
			ExecutableMBSE_Context context ){
		
		_context = context;
				
		final String theDefaultName = _context.getDefaultExternalSignalsPackageName();
		
		_ownerPkg = theOwnerPkg;
		_project = theOwnerPkg.getProject();
		
		_existingPkgs = 
				_context.findElementsWithMetaClassAndStereotype(
						"Package", 
						_context.REQTS_ANALYSIS_EXTERNAL_SIGNALS_PACKAGE, 
						_project, 
						1 );
				
		_userChoiceComboBox.addItem( _doNothingOption );	
		_userChoiceComboBox.addItem( _createNewButEmptyOption );
		
		if( _existingPkgs.isEmpty() ){	
			
			// set default to create new package
			_userChoiceComboBox.setSelectedItem( _createNewButEmptyOption );
    		
			String theUniqueName = 
					_context.determineUniqueNameBasedOn( 
							theDefaultName, "Package", theOwnerPkg );
			
    		_nameTextField.setText( theUniqueName );
    		_nameTextField.setEnabled( true );	
    		
		} else { // !m_ExistingPkgs.isEmpty()
			
			for( IRPModelElement theExistingReqtsPkg : _existingPkgs ){
				_userChoiceComboBox.addItem( _existingPkgPrefix + theExistingReqtsPkg.getName() );
			}
			
			String thePackageName = _existingPkgs.get(0).getName();
			 
			/// set default to first in list
			_userChoiceComboBox.setSelectedItem( _existingPkgPrefix + thePackageName );
    		_nameTextField.setText( thePackageName );
			_nameTextField.setEnabled( false );	
		}
			
		_userChoiceComboBox.addActionListener( new ActionListener(){
		    public void actionPerformed( ActionEvent e ){
		    	
		    	String selectedValue = _userChoiceComboBox.getSelectedItem().toString();
		    	
		    	if( selectedValue.equals( _doNothingOption ) ){
		    		
		    		_nameTextField.setText( _none );
		    		_nameTextField.setEnabled( false );
		    		
		    	} else if( selectedValue.equals( _createNewButEmptyOption ) ){
		    		
					String theUniqueName = 
							_context.determineUniqueNameBasedOn( 
									theDefaultName, "Package", _ownerPkg );
					
		    		_nameTextField.setText( theUniqueName );
		    		_nameTextField.setEnabled( true );	
		    		
		    	} else if( selectedValue.contains( _existingPkgPrefix ) ) {
		    		
		    		_nameTextField.setText( _none );
		    		_nameTextField.setEnabled( false );
		    	}
		    	
		    	_context.debug( selectedValue + " was selected" );
		    }
		});
	}
	
	public JTextField getM_NameTextField() {
		return _nameTextField;
	}

	public JComboBox<Object> getM_UserChoiceComboBox() {
		return _userChoiceComboBox;
	}
	
	public IRPPackage getExistingExternalSignalsPkgIfChosen(){
		
		IRPPackage theExistingPkg = null;
		
		String theUserChoice = (String) _userChoiceComboBox.getSelectedItem();
		
		if( theUserChoice.contains( _existingPkgPrefix ) ){
			
			// Strip off prefix to get the chosen package name
			String theChosenPkg = theUserChoice.replace( 
					_existingPkgPrefix, "" ).trim();

			for( IRPModelElement existingPkg : _existingPkgs ){
				
				String theName = existingPkg.getName();
				
				_context.debug( "existingPkg is " + theName );
				
				if( theChosenPkg.equals( theName ) ){
					
					theExistingPkg = (IRPPackage) existingPkg;
					break;
				}
			}	
		} 
		
		return theExistingPkg;
	}
	
	public String getExternalSignalsPkgNameIfChosen(){
		return _nameTextField.getText();
	}
}

/**
 * Copyright (C) 2021-2023  MBSE Training and Consulting Limited (www.executablembse.com)

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