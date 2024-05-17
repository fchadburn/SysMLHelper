package com.mbsetraining.sysmlhelper.featurefunctionpkgcreator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class CreateFunctionPkgChooser {

	public enum CreateFunctionPkgOption {
		DoNothing,
		CreateUnderProject,
		CreateUnderProjectWithStereotype,
		CreateUnderPackage,
		CreateUnderPackageWithStereotype,
	}

	private JComboBox<Object> _userChoiceComboBox = new JComboBox<Object>();
	private List<String> _packageStereotypeNames = new ArrayList<>();
	private JTextField _nameTextField = new JTextField();
	private JTextField _filenameTextField = new JTextField();
	private final String _doNothingOption = "Skip creation of a function package";
	private final String _createNewUnderProjectOption = "Create new function package under project";
	private final String _createNewUnderProjectWithStereotypeOptionPre = "Create new «";
	private final String _createNewUnderProjectWithStereotypeOptionPost = "» function package under project";
	private final String _createNewUnderPackageOption = "Create new function package under owning package";
	private final String _createNewUnderPackageWithStereotypeOptionPre = "Create new «";
	private final String _createNewUnderPackageWithStereotypeOptionPost = "» function package under owning package";
	private final String _none = "<None>";
	private String _postFix;
	private List<String> _stereotypeChoicesForPackage;
	private IRPPackage _invokedFromPkg;

	private ExecutableMBSE_Context _context;

	public CreateFunctionPkgChooser(
			IRPPackage theInvokedFromPkg,
			String theName,
			String thePostfix,
			ExecutableMBSE_Context context ){

		_context = context;
		_postFix = thePostfix;
		_invokedFromPkg = theInvokedFromPkg;

		_filenameTextField.setEnabled( false );

		_userChoiceComboBox.addItem( _doNothingOption );	
		_userChoiceComboBox.addItem( _createNewUnderProjectOption );
		_userChoiceComboBox.setSelectedItem( _createNewUnderProjectOption );

		boolean isAllowCreateUnderPackage = !( _invokedFromPkg instanceof IRPProject );

		// Only give option to create under owning package if it's not a project
		if( isAllowCreateUnderPackage ) {
			_userChoiceComboBox.addItem( _createNewUnderPackageOption );
			_userChoiceComboBox.setSelectedItem( _createNewUnderPackageOption );
		}

		_stereotypeChoicesForPackage = _context.getStereotypeNamesForFunctionPkg();

		_packageStereotypeNames.addAll( _stereotypeChoicesForPackage ); // E.g. Tier0

		String theFirstOptionWithPackageStereotype = null;

		for( String thePackageStereotype : _packageStereotypeNames ){

			String createNewUnderProjectWithStereotypeOptionString =
					_createNewUnderProjectWithStereotypeOptionPre + 
					thePackageStereotype + _createNewUnderProjectWithStereotypeOptionPost;

			if( theFirstOptionWithPackageStereotype == null ) {
				theFirstOptionWithPackageStereotype = 
						createNewUnderProjectWithStereotypeOptionString;
			}

			_userChoiceComboBox.addItem( 
					createNewUnderProjectWithStereotypeOptionString );

			// Only give option to create under owning package if it's not a project
			if( isAllowCreateUnderPackage ){
				
				_userChoiceComboBox.addItem( 
						_createNewUnderPackageWithStereotypeOptionPre + 
						thePackageStereotype + _createNewUnderPackageWithStereotypeOptionPost );
			}
		}

		_nameTextField.setText( theName );
		_nameTextField.setEnabled( true );	
			
		updateFilenameTextField();

		_userChoiceComboBox.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){

				String selectedValue = _userChoiceComboBox.getSelectedItem().toString();

				if( selectedValue.equals( _doNothingOption ) ){

					_nameTextField.setText( _none );
					_nameTextField.setEnabled( false );

					_filenameTextField.setText( _none );

				} else {

					_nameTextField.setText( theName );
					_nameTextField.setEnabled( true );
					
					updateFilenameTextField();
				}

				_context.debug( selectedValue + " was selected" );
			}
		});

		// Listen for changes in the text
		_nameTextField.getDocument().addDocumentListener( new DocumentListener() {
			
			public void changedUpdate( DocumentEvent e ){
				updateFilenameTextField();
			}
			public void removeUpdate( DocumentEvent e ){
				updateFilenameTextField();
			}
			public void insertUpdate( DocumentEvent e ){
				updateFilenameTextField();
			}			
		});
	}

	private void updateFilenameTextField() {
		
		String theProposedName = _context.toLegalClassName( _nameTextField.getText() ) + _postFix;
		
		CreateFunctionPkgOption theChoice = getChoice();
		
		if( theChoice == CreateFunctionPkgOption.CreateUnderPackage ||
				theChoice == CreateFunctionPkgOption.CreateUnderPackageWithStereotype ) {
			
			String theUniqueName = _context.
					determineUniqueNameBasedOn( theProposedName, "Package", _invokedFromPkg );
			
			_filenameTextField.setText( theUniqueName );

		} else if( theChoice == CreateFunctionPkgOption.CreateUnderProject || 
				theChoice == CreateFunctionPkgOption.CreateUnderProjectWithStereotype ) {
			
			String theUniqueName = _context.
					determineUniqueNameBasedOn( theProposedName, "Package", _invokedFromPkg.getProject() );
			
			_filenameTextField.setText( theUniqueName );
		}
	}
	
	public JTextField getM_NameTextField() {
		return _nameTextField;
	}

	public JTextField getM_FileNameTextField() {
		return _filenameTextField;
	}

	public JComboBox<Object> getM_UserChoiceComboBox() {
		return _userChoiceComboBox;
	}

	public CreateFunctionPkgOption getChoice(){

		CreateFunctionPkgOption theOption = null;

		String theUserChoice = (String) _userChoiceComboBox.getSelectedItem();

		if( theUserChoice.equals( _createNewUnderProjectOption ) ){
			theOption = CreateFunctionPkgOption.CreateUnderProject;
		} else if( ( theUserChoice.startsWith( _createNewUnderProjectWithStereotypeOptionPre ) && 
				theUserChoice.endsWith( _createNewUnderProjectWithStereotypeOptionPost ) ) ){
			theOption = CreateFunctionPkgOption.CreateUnderProjectWithStereotype;
		} else if( theUserChoice.equals( _createNewUnderPackageOption ) ){
			theOption = CreateFunctionPkgOption.CreateUnderPackage;
		} else if( ( theUserChoice.startsWith( _createNewUnderPackageWithStereotypeOptionPre ) && 
				theUserChoice.endsWith( _createNewUnderPackageWithStereotypeOptionPost ) ) ){
			theOption = CreateFunctionPkgOption.CreateUnderPackageWithStereotype;
		} else if( theUserChoice.contains( _doNothingOption ) ){
			theOption = CreateFunctionPkgOption.DoNothing;
		} else {
			_context.error( "Error in getChoice, unhandled option=" + theUserChoice );
		}

		return theOption;
	}

	public String getReqtsPkgOptionalName(){
		return _nameTextField.getText();
	}

	public String getStereotypeNameIfChosen(){

		String theStereotypeName = "";
		String theUserChoice = (String) _userChoiceComboBox.getSelectedItem();

		if( ( theUserChoice.startsWith( _createNewUnderProjectWithStereotypeOptionPre ) && 
				theUserChoice.endsWith( _createNewUnderProjectWithStereotypeOptionPost ) ) ){

			theStereotypeName = theUserChoice.replaceAll( _createNewUnderProjectWithStereotypeOptionPre, "" );
			theStereotypeName = theStereotypeName.replaceAll( _createNewUnderProjectWithStereotypeOptionPost, "" );

		} else if( ( theUserChoice.startsWith( _createNewUnderPackageWithStereotypeOptionPre ) && 
				theUserChoice.endsWith( _createNewUnderPackageWithStereotypeOptionPost ) ) ){

			theStereotypeName = theUserChoice.replaceAll( _createNewUnderPackageWithStereotypeOptionPre, "" );
			theStereotypeName = theStereotypeName.replaceAll( _createNewUnderPackageWithStereotypeOptionPost, "" );
		}

		return theStereotypeName;
	}

	public void createFunctionPackage(){

		CreateFunctionPkgOption theChoice = getChoice();

		FunctionPkgCreator theCreator = new FunctionPkgCreator( _context );
		
		String theBlockName = _nameTextField.getText();
		String thePkgName = _filenameTextField.getText();
		
		IRPStereotype theStereotype = null;

		if( theChoice == CreateFunctionPkgOption.CreateUnderPackageWithStereotype ||
				theChoice == CreateFunctionPkgOption.CreateUnderProjectWithStereotype ){
			
			String theStereotypeName = getStereotypeNameIfChosen(); // or theName?

			if( !theStereotypeName.isEmpty()) {
				
				theStereotype = (IRPStereotype) _context.get_rhpPrj().
						findAllByName( theStereotypeName , "Stereotype" );
				
				if( theStereotype == null ) {
					_context.warning( "Unable to find stereotype called " + theStereotypeName + " to apply to function" );
				}
			}
		}
		
		if( theChoice == CreateFunctionPkgOption.CreateUnderPackage ||
				theChoice == CreateFunctionPkgOption.CreateUnderPackageWithStereotype ) {
			
			theCreator.createFunctionPkg( _invokedFromPkg, thePkgName, theBlockName, theStereotype );


		} else if( theChoice == CreateFunctionPkgOption.CreateUnderProject || 
				theChoice == CreateFunctionPkgOption.CreateUnderProjectWithStereotype ) {
			
			theCreator.createFunctionPkg( _invokedFromPkg.getProject(), thePkgName, theBlockName, theStereotype );
		}
	}
}

/**
 * Copyright (C) 2024  MBSE Training and Consulting Limited (www.executablembse.com)

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