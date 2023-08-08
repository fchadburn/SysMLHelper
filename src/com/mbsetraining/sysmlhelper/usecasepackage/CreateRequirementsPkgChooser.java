package com.mbsetraining.sysmlhelper.usecasepackage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.mbsetraining.sysmlhelper.gateway.GatewayFileParser;
import com.mbsetraining.sysmlhelper.gateway.GatewayFileSection;
import com.telelogic.rhapsody.core.*;

public class CreateRequirementsPkgChooser {

	public enum CreateRequirementsPkgOption {
		DoNothing,
		CreateUnderProject,
		CreateUnderProjectWithStereotype,
		CreateUnderUseCasePkg,
		CreateUnderUseCasePkgWithStereotype,
		UseExistingReqtsPkg
	}

	private JComboBox<Object> _userChoiceComboBox = new JComboBox<Object>();
	private List<GatewayFileSection> _availableTypeTemplates = new ArrayList<GatewayFileSection>();
	private List<String> _packageStereotypeNames = new ArrayList<>();
	private JTextField _nameTextField = new JTextField();
	private final String _doNothingOption = "Skip creation of a requirements package";
	private final String _createNewUnderProjectOption = "Create new requirements package under project";
	private final String _createNewUnderProjectWithStereotypeOptionPre = "Create new «";
	private final String _createNewUnderProjectWithStereotypeOptionPost = "» requirements package under project";
	private final String _createNewUnderUseCasePkgOption = "Create new requirements package under owning package";
	private final String _createNewUnderUseCasePkgWithStereotypeOptionPre = "Create new «";
	private final String _createNewUnderUseCasePkgWithStereotypeOptionPost = "» requirements package under owning package";
	private final String _existingPkgPrefix = "Flow requirements to existing package called ";
	private final String _cone = "<None>";
	private String _name;
	private List<IRPModelElement> _existingPkgs;

	private ExecutableMBSE_Context _context;

	public CreateRequirementsPkgChooser(
			IRPPackage theOwnerPkg,
			String theName,
			boolean isDefaultToNewUnderProject,
			ExecutableMBSE_Context context ){

		_context = context;
		_name = theName;

		_existingPkgs = 
				_context.findElementsWithMetaClassAndStereotype(
						"Package", 
						_context.REQTS_ANALYSIS_REQUIREMENT_PACKAGE, 
						_context.get_rhpPrj(), 
						1 );

		String theSysMLHelperProfilePath =
				RhapsodyAppServer.getActiveRhapsodyApplication().getOMROOT() + 
				"\\Profiles\\SysMLHelper\\SysMLHelper_rpy";

		if( _context.getIsEnableGatewayTypes(theOwnerPkg) ){
			buildAvailableTypesList( theSysMLHelperProfilePath, "^.*.types$" );
		}

		_userChoiceComboBox.addItem( _doNothingOption );	
		_userChoiceComboBox.addItem( _createNewUnderProjectOption );
		_userChoiceComboBox.addItem( _createNewUnderUseCasePkgOption );

		_packageStereotypeNames = _context.getStereotypeNamesForRequirementsPkgFromUseCases();

		for( GatewayFileSection theAvailableTypeTemplate : _availableTypeTemplates ){

			_userChoiceComboBox.addItem( 
					_createNewUnderProjectWithStereotypeOptionPre + "from" +
							theAvailableTypeTemplate.getSectionName() + _createNewUnderProjectWithStereotypeOptionPost );
			
			_userChoiceComboBox.addItem( 
					_createNewUnderUseCasePkgWithStereotypeOptionPre + "from" +
							theAvailableTypeTemplate.getSectionName() + _createNewUnderUseCasePkgWithStereotypeOptionPost );
		}

		for( String thePackageStereotype : _packageStereotypeNames ){

			_userChoiceComboBox.addItem( 
					_createNewUnderProjectWithStereotypeOptionPre + 
					thePackageStereotype + _createNewUnderProjectWithStereotypeOptionPost );
			
			_userChoiceComboBox.addItem( 
					_createNewUnderUseCasePkgWithStereotypeOptionPre + 
					thePackageStereotype + _createNewUnderUseCasePkgWithStereotypeOptionPost );
		}

		for( IRPModelElement theExistingReqtsPkg : _existingPkgs ){
			_userChoiceComboBox.addItem( 
					_existingPkgPrefix + theExistingReqtsPkg.getName() );
		}

		if( _existingPkgs.isEmpty() || isDefaultToNewUnderProject ){	

			// set default to create new package
			_userChoiceComboBox.setSelectedItem( _createNewUnderProjectOption );

			String theUniqueName = 
					_context.determineUniqueNameBasedOn( 
							_name, "Package", theOwnerPkg );

			_nameTextField.setText( theUniqueName );
			_nameTextField.setEnabled( true );	

		} else { // !m_ExistingPkgs.isEmpty()

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

					_nameTextField.setText( _cone );
					_nameTextField.setEnabled( false );

				} else if( selectedValue.equals( _createNewUnderProjectOption ) ||
						( selectedValue.startsWith( _createNewUnderProjectWithStereotypeOptionPre ) && 
								selectedValue.endsWith( _createNewUnderProjectWithStereotypeOptionPost ) ) ||
						selectedValue.equals( _createNewUnderUseCasePkgOption ) ||
						( selectedValue.startsWith( _createNewUnderUseCasePkgWithStereotypeOptionPre ) && 
								selectedValue.endsWith( _createNewUnderUseCasePkgWithStereotypeOptionPost ) ) ){

					updateRequirementsPkgNameBasedOn( _name );
					_nameTextField.setEnabled( true );		

				} else if( selectedValue.startsWith( _existingPkgPrefix ) ){

					String theName = selectedValue.replaceFirst( _existingPkgPrefix, "" );

					_nameTextField.setText( theName );
					_nameTextField.setEnabled( false );
				}

				_context.debug( selectedValue + " was selected" );
			}
		});
	}

	private void buildAvailableTypesList(
			String theSysMLHelperProfilePath,
			String theFileRegEx ){

		List<File> theFiles = 
				_context.getFilesMatching( 
						theFileRegEx, 
						theSysMLHelperProfilePath );

		for( File theFile : theFiles ){

			final GatewayFileParser theSysMLTypesFile = 
					new GatewayFileParser( theFile, _context );

			if( theSysMLTypesFile != null ){

				List<GatewayFileSection> theFileSections =
						theSysMLTypesFile.getAllTheFileSections();

				for( GatewayFileSection theFileSection : theFileSections ){

					addToTypeTemplates( theFileSection, _availableTypeTemplates );
				}				
			}
		}
	}

	private void addToTypeTemplates(
			GatewayFileSection theFileSection,
			List<GatewayFileSection> theList ){

		String theName = theFileSection.getSectionName();

		if( !theName.equals("Types" ) ){

			boolean isFound = isTypeAlreadyAvailable( theName, theList );

			if( !isFound ){
				_availableTypeTemplates.add( theFileSection );
			} else {
				_context.debug( "Skipped added " + theName + 
						" as already in the list of " + _availableTypeTemplates.size() + 
						" types" );
			}
		}
	}

	private boolean isTypeAlreadyAvailable( 
			String theName,
			List<GatewayFileSection> inTheList ){

		boolean isFound = false;

		for( GatewayFileSection theAvailableTypeTemplate : inTheList ){

			if( theAvailableTypeTemplate.getSectionName().equals( theName ) ){
				isFound = true;
				break;
			}
		}

		return isFound;
	}

	public JTextField getM_NameTextField() {
		return _nameTextField;
	}

	public JComboBox<Object> getM_UserChoiceComboBox() {
		return _userChoiceComboBox;
	}

	public CreateRequirementsPkgOption getReqtsPkgChoice(){

		CreateRequirementsPkgOption theOption = null;

		String theUserChoice = (String) _userChoiceComboBox.getSelectedItem();

		if( theUserChoice.equals( _createNewUnderProjectOption ) ){
			theOption = CreateRequirementsPkgOption.CreateUnderProject;
		} else if( ( theUserChoice.startsWith( _createNewUnderProjectWithStereotypeOptionPre ) && 
				theUserChoice.endsWith( _createNewUnderProjectWithStereotypeOptionPost ) ) ){
			theOption = CreateRequirementsPkgOption.CreateUnderProjectWithStereotype;
		} else if( theUserChoice.equals( _createNewUnderUseCasePkgOption ) ){
			theOption = CreateRequirementsPkgOption.CreateUnderUseCasePkg;
		} else if( ( theUserChoice.startsWith( _createNewUnderUseCasePkgWithStereotypeOptionPre ) && 
				theUserChoice.endsWith( _createNewUnderUseCasePkgWithStereotypeOptionPost ) ) ){
			theOption = CreateRequirementsPkgOption.CreateUnderUseCasePkgWithStereotype;
		} else if( theUserChoice.startsWith( _existingPkgPrefix ) ){
			theOption = CreateRequirementsPkgOption.UseExistingReqtsPkg;
		} else if( theUserChoice.contains( _doNothingOption ) ){
			theOption = CreateRequirementsPkgOption.DoNothing;
		} else {
			_context.error("Error in getReqtsPkgChoice, unhandled option=" + theUserChoice);
		}

		return theOption;
	}

	public String getReqtsPkgOptionalName(){
		return _nameTextField.getText();
	}

	public IRPPackage getExistingReqtsPkgIfChosen(){

		IRPPackage theReqtsPkg = null;

		if( getReqtsPkgChoice()== CreateRequirementsPkgOption.UseExistingReqtsPkg ){

			String theUserChoice = (String) _userChoiceComboBox.getSelectedItem();
			String thePackageName = theUserChoice.replaceFirst( _existingPkgPrefix, "" );

			int count = 0;

			for( IRPModelElement theExistingPkg : _existingPkgs ){

				if( theExistingPkg instanceof IRPPackage &&
						theExistingPkg.getName().equals( thePackageName ) ){

					theReqtsPkg = (IRPPackage) theExistingPkg;
					count++;
				}
			}

			if( count == 0 ){
				_context.error("Error in getExistingReqtsPkgIfChosen, " + 
						" no packages called " + thePackageName + " were found when expecting 1");
			} else if( count > 1 ){
				_context.error("Error in getExistingReqtsPkgIfChosen, " + count + 
						" packages called " + thePackageName + " were found when expecting 1");
			}
		}

		return theReqtsPkg;
	}

	public String getStereotypeNameIfChosen(){

		String theStereotypeName = "";
		String theUserChoice = (String) _userChoiceComboBox.getSelectedItem();

		if( ( theUserChoice.startsWith( _createNewUnderProjectWithStereotypeOptionPre ) && 
				theUserChoice.endsWith( _createNewUnderProjectWithStereotypeOptionPost ) ) ){

			theStereotypeName = theUserChoice.replaceAll( _createNewUnderProjectWithStereotypeOptionPre, "" );
			theStereotypeName = theStereotypeName.replaceAll( _createNewUnderProjectWithStereotypeOptionPost, "" );
			
		} else if( ( theUserChoice.startsWith( _createNewUnderUseCasePkgWithStereotypeOptionPre ) && 
				theUserChoice.endsWith( _createNewUnderUseCasePkgWithStereotypeOptionPost ) ) ){

			theStereotypeName = theUserChoice.replaceAll( _createNewUnderUseCasePkgWithStereotypeOptionPre, "" );
			theStereotypeName = theStereotypeName.replaceAll( _createNewUnderUseCasePkgWithStereotypeOptionPost, "" );
		}

		return theStereotypeName;
	}

	public void updateRequirementsPkgNameBasedOn(
			String theName ){

		String selectedValue = (String) _userChoiceComboBox.getSelectedItem();

		if( selectedValue.equals( _createNewUnderProjectOption ) ||
				( selectedValue.startsWith( _createNewUnderProjectWithStereotypeOptionPre ) &&
						selectedValue.endsWith( _createNewUnderProjectWithStereotypeOptionPost ) ) ){

			_name = _context.determineUniqueNameBasedOn(
					theName,
					"Package",
					_context.get_rhpPrj() );
		} else {
			_name = theName;
		}

		if( selectedValue.equals( _createNewUnderProjectOption ) ||
				( selectedValue.startsWith( _createNewUnderProjectWithStereotypeOptionPre ) &&
						selectedValue.endsWith( _createNewUnderProjectWithStereotypeOptionPost ) ) ||
				selectedValue.equals( _createNewUnderUseCasePkgOption ) ||
				( selectedValue.startsWith( _createNewUnderUseCasePkgWithStereotypeOptionPre ) &&
						selectedValue.endsWith( _createNewUnderUseCasePkgWithStereotypeOptionPost ) ) ){

			_nameTextField.setText( _name );
		}
	}
	
	public void createRequirementsPackage(
			IRPPackage theUseCasePkg ){
		
		CreateRequirementsPkgOption theReqtsPkgChoice = getReqtsPkgChoice();
		
		IRPPackage theReqtsPkg = null;
		IRPProject theProject = theUseCasePkg.getProject();
		
		if( theReqtsPkgChoice == CreateRequirementsPkgOption.CreateUnderProject ||
				theReqtsPkgChoice == CreateRequirementsPkgOption.CreateUnderProjectWithStereotype ){

			theReqtsPkg = createReqtsPackageWithDependencyTo(
					theUseCasePkg, 
					getReqtsPkgOptionalName(), 
					theProject );
			
		} else if( theReqtsPkgChoice == CreateRequirementsPkgOption.CreateUnderUseCasePkg ||
				theReqtsPkgChoice == CreateRequirementsPkgOption.CreateUnderUseCasePkgWithStereotype ){

			theReqtsPkg = createReqtsPackageWithDependencyTo(
					theUseCasePkg, 
					getReqtsPkgOptionalName(), 
					theUseCasePkg );

		} else if( theReqtsPkgChoice == CreateRequirementsPkgOption.UseExistingReqtsPkg ){

			theUseCasePkg.addDependencyTo( getExistingReqtsPkgIfChosen() );

		} else if( theReqtsPkgChoice == CreateRequirementsPkgOption.DoNothing ){	

			_context.debug( "User chose to do nothing with Requirements package" );
		}

		if( theReqtsPkgChoice == CreateRequirementsPkgOption.CreateUnderProjectWithStereotype ||
				theReqtsPkgChoice == CreateRequirementsPkgOption.CreateUnderUseCasePkgWithStereotype ){

			String theStereotypeName = getStereotypeNameIfChosen(); // or theName?

			createIfNecessaryAndApplyStereotypeTo( theReqtsPkg, theStereotypeName );
		}
	}

	private void createIfNecessaryAndApplyStereotypeTo(
			IRPPackage theReqtsPkg,
			String theStereotypeName ){
		
		IRPStereotype theFoundStereotype = (IRPStereotype) _context.get_rhpPrj().
				findAllByName( theStereotypeName , "Stereotype" );

		IRPStereotype theStereotype = null;

		if( theFoundStereotype == null ){
			
			IRPStereotype theTemplateStereotype = _context.getStereotypeTemplateForRequirementsPkgFromUseCases();
			
			if( theTemplateStereotype != null ) {
				
				theStereotype = (IRPStereotype) theTemplateStereotype.clone( theStereotypeName, theReqtsPkg );
				theStereotype.addMetaClass( "Package" );
				
				theReqtsPkg.addSpecificStereotype( theStereotype );

			} else {
				theStereotype = theReqtsPkg.addStereotype( theStereotypeName, "Package" );
			}

			
			theStereotype.addMetaClass( "Dependency" );
			theStereotype.addMetaClass( "HyperLink" );
			theStereotype.addMetaClass( "Requirement" );
			theStereotype.addMetaClass( "Type" );

			theStereotype.setOwner( theReqtsPkg );
			theStereotype.highLightElement();

		} else {
			theReqtsPkg.addSpecificStereotype( theFoundStereotype );
		}
	}
	
	public IRPPackage createReqtsPackageWithDependencyTo(
			IRPPackage theFlowFromPkg, 
			String theName,
			IRPPackage theReqtsPackageOwner ){

		IRPPackage theReqtsPkg;
		theReqtsPkg = theReqtsPackageOwner.addNestedPackage( theName );
		theReqtsPkg.changeTo( _context.REQTS_ANALYSIS_REQUIREMENT_PACKAGE );	
		_context.setSavedInSeparateDirectoryIfAppropriateFor( theReqtsPkg );
		theFlowFromPkg.addDependencyTo( theReqtsPkg );

		return theReqtsPkg;
	}
}

/**
 * Copyright (C) 2018-2023  MBSE Training and Consulting Limited (www.executablembse.com)

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