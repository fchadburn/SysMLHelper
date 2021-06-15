package com.mbsetraining.sysmlhelper.executablembse;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import com.mbsetraining.sysmlhelper.executablembse.CreateActorPkg.CreateActorPkgOption;
import com.telelogic.rhapsody.core.*;

public class CreateActorPkgChooser {
	
	private JComboBox<Object> _userChoiceComboBox = new JComboBox<Object>();
	private JTextField _nameTextField = new JTextField();
	private IRPPackage _ownerPkg = null;
	private IRPProject _project = null;
	private final String _doNothingOption = "Skip creation of a shared actor package";
	private final String _createNewOption = "Create new shared actor package using default actor names";
	private final String _createNewButEmptyOption = "Create new empty shared actor package";
	private final String _existingActorPkgPrefix = "Use actors from existing package called ";
	private final String _instantiateActorPkgPrefix = "Create a sub-package for copies of actors from package ";
	private final String m_None = "<None>";
	private List<IRPModelElement> _existingPkgs;
	private ExecutableMBSE_Context _context;
	
	public CreateActorPkgChooser(
			final IRPPackage theOwnerPkg,
			ExecutableMBSE_Context context ){
		
		_context = context;
		
		final String theDefaultName = _context.getDefaultActorPackageName();
		
		_ownerPkg = theOwnerPkg;
		_project = theOwnerPkg.getProject();
		
		_existingPkgs = 
				_context.findElementsWithMetaClassAndStereotype(
						"Package", 
						_context.getActorPackageStereotype( theOwnerPkg ), 
						_project, 
						1 );
				
		_userChoiceComboBox.addItem( _doNothingOption );	
		_userChoiceComboBox.addItem( _createNewOption );
		_userChoiceComboBox.addItem( _createNewButEmptyOption );
		
		if( _existingPkgs.isEmpty() ){	
			
			// set default to create new package
			_userChoiceComboBox.setSelectedItem( _createNewOption );
    		
			String theUniqueName = 
					_context.determineUniqueNameBasedOn( 
							theDefaultName, "Package", theOwnerPkg );
			
    		_nameTextField.setText( theUniqueName );
    		_nameTextField.setEnabled( true );	
    		
		} else { // !m_ExistingPkgs.isEmpty()
			
			for( IRPModelElement existingPkg : _existingPkgs ){
				_userChoiceComboBox.addItem( _existingActorPkgPrefix + existingPkg.getName() );
			}

			for( IRPModelElement existingPkg : _existingPkgs ){
				_userChoiceComboBox.addItem( _instantiateActorPkgPrefix + existingPkg.getName() );
			}
			
			String thePackageName = _existingPkgs.get(0).getName();
			 
			/// set default to first in list
			_userChoiceComboBox.setSelectedItem( _existingActorPkgPrefix + thePackageName );
    		_nameTextField.setText( thePackageName );
			_nameTextField.setEnabled( false );	
		}
			
		_userChoiceComboBox.addActionListener( new ActionListener(){
		    public void actionPerformed( ActionEvent e ){
		    	
		    	String selectedValue = _userChoiceComboBox.getSelectedItem().toString();
		    	
		    	if( selectedValue.equals( _doNothingOption ) ){
		    		
		    		_nameTextField.setText( m_None );
		    		_nameTextField.setEnabled( false );
		    		
		    	} else if( selectedValue.equals( _createNewOption ) ||
		    			   selectedValue.equals( _createNewButEmptyOption ) ){
		    		
					String theUniqueName = 
							_context.determineUniqueNameBasedOn( 
									theDefaultName, "Package", _ownerPkg );
					
		    		_nameTextField.setText( theUniqueName );
		    		_nameTextField.setEnabled( true );	
		    		
		    	} else if( selectedValue.contains( _instantiateActorPkgPrefix ) ){
		    	
		    		_nameTextField.setText( "Actors_<OwningPkg>" );
		    		_nameTextField.setEnabled( true );	
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
	
	public IRPPackage getExistingActorPkgIfChosen(){
		
		IRPPackage theActorPkg = null;

		String theUserChoice = (String) _userChoiceComboBox.getSelectedItem();
		
		if( theUserChoice.contains( _existingActorPkgPrefix ) ){
			
			// Strip off prefix to get the chosen package name
			String theChosenPkg = theUserChoice.replace( 
					_existingActorPkgPrefix, "" );

			for( IRPModelElement theExistingPkg : _existingPkgs ){
				
				if( theExistingPkg instanceof IRPPackage &&
					theExistingPkg.getName().equals( theChosenPkg ) ){
					
					theActorPkg = (IRPPackage) theExistingPkg;
					break;
				}
			}	
			
		} else if( theUserChoice.contains( _instantiateActorPkgPrefix ) ){
			
			// Strip off prefix to get the chosen package name
			String theChosenPkg = theUserChoice.replace( 
					_instantiateActorPkgPrefix, "" );

			for( IRPModelElement theExistingPkg : _existingPkgs ){
				
				if( theExistingPkg instanceof IRPPackage &&
					theExistingPkg.getName().equals( theChosenPkg ) ){
					
					theActorPkg = (IRPPackage) theExistingPkg;
					break;
				}
			}	
		}

		return theActorPkg;
	}
	
	public CreateActorPkg.CreateActorPkgOption getCreateActorPkgOption(){
		
		CreateActorPkgOption theOption = null;
		
		String theUserChoice = (String) _userChoiceComboBox.getSelectedItem();
		
		if( theUserChoice.equals( _createNewOption ) ){
			theOption = CreateActorPkgOption.CreateNew;
		} else if( theUserChoice.equals( _createNewButEmptyOption ) ){
			theOption = CreateActorPkgOption.CreateNewButEmpty;
		} else if( theUserChoice.contains( _existingActorPkgPrefix )){
			theOption = CreateActorPkgOption.UseExisting;
		} else if( theUserChoice.contains( _instantiateActorPkgPrefix )){
			theOption = CreateActorPkgOption.InstantiateFromExisting;
		} else if( theUserChoice.contains(_doNothingOption)){
			theOption = CreateActorPkgOption.DoNothing;
		} else {
			_context.error("Error in getCreateActorPkgOption, unhandled option = " + theUserChoice);
		}
	
		return theOption;
	}
	
	public IRPPackage getExistingTemplateActorPkgIfChosen(){
		
		String theUserChoice = (String) _userChoiceComboBox.getSelectedItem();
		IRPPackage theActorPkg = null;
		
		if( theUserChoice.contains( _instantiateActorPkgPrefix ) ){
			
			// Strip off prefix to get the chosen package name
			String theChosenPkg = theUserChoice.replace( 
					_instantiateActorPkgPrefix, "" );

			for( IRPModelElement theExistingPkg : _existingPkgs ){
				
				if( theExistingPkg instanceof IRPPackage &&
					theExistingPkg.getName().equals( theChosenPkg ) ){
					
					theActorPkg = (IRPPackage) theExistingPkg;
				}
			}	
		}

		return theActorPkg;
	}
	
	public String getActorsPkgNameIfChosen(){
		
		String theName = _nameTextField.getText().replaceAll( "\\<OwningPkg\\>", "" );
		return theName;
	}
}

/**
 * Copyright (C) 2018-2021  MBSE Training and Consulting Limited (www.executablembse.com)

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