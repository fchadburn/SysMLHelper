package com.mbsetraining.sysmlhelper.usecasepackage;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.contextdiagram.ContextDiagramCreator;
import com.mbsetraining.sysmlhelper.contextdiagram.ExternalSignalsPkgCreator;
import com.mbsetraining.sysmlhelper.contextdiagram.CreateSignalsPkgChooser;
import com.mbsetraining.sysmlhelper.executablembse.AutoPackageDiagram;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSEBasePanel;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.mbsetraining.sysmlhelper.usecasepackage.CreateActorPkg.CreateActorPkgOption;
import com.mbsetraining.sysmlhelper.usecasepackage.CreateRequirementsPkgChooser.CreateRequirementsPkgOption;
import com.telelogic.rhapsody.core.*;

public class CreateUseCasesPackagePanel extends ExecutableMBSEBasePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2320848946667282658L;
	private IRPPackage _ownerPkg;
	private CreateActorPkgChooser _createActorChooser;
	private CreateRequirementsPkgChooser _createRequirementsPkgChooser;
	private CreateContextPkgChooser _createContextPkgChooser;
	private CreateSignalsPkgChooser _createSignalsPkgChooser;
	private JTextField _nameTextField;
	private String _usecasePackagePostfix;
	private String _requirementPackagePostfix;

	public static void main(String[] args) {
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		String theRhpAppID = theRhpApp.getApplicationConnectionString();
		launchTheDialog(theRhpAppID);
	}

	public static void launchTheDialog(
			final String theAppID ){

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame( "Populate use case package structure" );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateUseCasesPackagePanel thePanel = 
						new CreateUseCasesPackagePanel(
								theAppID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}

	public CreateUseCasesPackagePanel( 
			String theAppID ){

		super( theAppID );

		_ownerPkg = _context.get_rhpPrj();
		_usecasePackagePostfix = _context.getDefaultUseCasePackagePostfix( _ownerPkg );
		_requirementPackagePostfix = _context.getDefaultRequirementPackagePostfix();

		setLayout( new BorderLayout() );
		setBorder( new EmptyBorder( 0, 10, 10, 10 ) );

		String theUniqueName = 
				_context.determineUniqueNameForPackageBasedOn(
						((ExecutableMBSE_Context) _context).getDefaultUseCasePackageName( _ownerPkg ),
						_ownerPkg );

		JPanel theReqtsAnalysisPanel = createContent( theUniqueName );
		theReqtsAnalysisPanel.setAlignmentX( Component.LEFT_ALIGNMENT );

		String introText = 
				"This helper will create a package hierarchy for simple activity-based use case analysis underneath the project. \n" + 
						"It creates a nested package structure and use case diagram, imports the appropriate profiles if not present, and sets default \n" +
						"display and other options to appropriate values for this using Rhapsody profile and property settings.\n";

		JPanel theStartPanel = new JPanel();

		theStartPanel.setLayout( new BoxLayout( theStartPanel, BoxLayout.PAGE_AXIS ) );
		theStartPanel.add( new JLabel( " " ) );
		theStartPanel.add( createTheNameThePackagePanel( _ownerPkg, theUniqueName ) );
		theStartPanel.add( createPanelWithTextCentered( introText ) );

		add( theStartPanel, BorderLayout.PAGE_START );
		add( theReqtsAnalysisPanel, BorderLayout.CENTER );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );		
	}

	private JPanel createContent(
			String theName ){

		JPanel thePanel = new JPanel();

		GroupLayout theGroupLayout = new GroupLayout( thePanel );
		thePanel.setLayout( theGroupLayout );
		theGroupLayout.setAutoCreateGaps( true );

		SequentialGroup theHorizSequenceGroup = theGroupLayout.createSequentialGroup();
		SequentialGroup theVerticalSequenceGroup = theGroupLayout.createSequentialGroup();

		ParallelGroup theColumn1ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );
		ParallelGroup theColumn2ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );

		theHorizSequenceGroup.addGroup( theColumn1ParallelGroup );
		theHorizSequenceGroup.addGroup( theColumn2ParallelGroup );

		_createActorChooser = new CreateActorPkgChooser( 
				_ownerPkg, 
				_context );

		_createRequirementsPkgChooser = new CreateRequirementsPkgChooser( 
				_ownerPkg, 
				theName + _requirementPackagePostfix, 
				true,
				_context );

		_createContextPkgChooser = new CreateContextPkgChooser( 
				_ownerPkg, 
				_context );

		_createSignalsPkgChooser = new CreateSignalsPkgChooser( 
				_ownerPkg, 
				_context );

		_createContextPkgChooser._userChoiceComboBox.addActionListener ( new ActionListener () {

			public void actionPerformed( ActionEvent e ){	
				configureOptionsRelatedToContextPkg();
			}
		});

		configureOptionsRelatedToContextPkg();

		theColumn1ParallelGroup.addComponent( _createActorChooser.getM_UserChoiceComboBox() );    
		theColumn1ParallelGroup.addComponent( _createRequirementsPkgChooser.getM_UserChoiceComboBox() ); 
		theColumn1ParallelGroup.addComponent( _createContextPkgChooser.getM_UserChoiceComboBox() ); 
		theColumn1ParallelGroup.addComponent( _createSignalsPkgChooser.getM_UserChoiceComboBox() ); 
		theColumn2ParallelGroup.addComponent( _createActorChooser.getM_NameTextField() );   
		theColumn2ParallelGroup.addComponent( _createRequirementsPkgChooser.getM_NameTextField() );   
		theColumn2ParallelGroup.addComponent( _createContextPkgChooser.getM_NameTextField() );   
		theColumn2ParallelGroup.addComponent( _createSignalsPkgChooser.getM_NameTextField() );
		
		ParallelGroup theVertical1ParallelGroup = 
				theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );

		theVertical1ParallelGroup.addComponent( _createActorChooser.getM_UserChoiceComboBox() );
		theVertical1ParallelGroup.addComponent( _createActorChooser.getM_NameTextField() );

		ParallelGroup theVertical2ParallelGroup = 
				theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );

		theVertical2ParallelGroup.addComponent( _createRequirementsPkgChooser.getM_UserChoiceComboBox() );
		theVertical2ParallelGroup.addComponent( _createRequirementsPkgChooser.getM_NameTextField() );

		ParallelGroup theVertical3ParallelGroup = 
				theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );

		theVertical3ParallelGroup.addComponent( _createContextPkgChooser.getM_UserChoiceComboBox() );
		theVertical3ParallelGroup.addComponent( _createContextPkgChooser.getM_NameTextField() );

		ParallelGroup theVertical4ParallelGroup = 
				theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );

		theVertical4ParallelGroup.addComponent( _createSignalsPkgChooser.getM_UserChoiceComboBox() );
		theVertical4ParallelGroup.addComponent( _createSignalsPkgChooser.getM_NameTextField() );

		theVerticalSequenceGroup.addGroup( theVertical1ParallelGroup );		
		theVerticalSequenceGroup.addGroup( theVertical2ParallelGroup );		
		theVerticalSequenceGroup.addGroup( theVertical3ParallelGroup );		
		theVerticalSequenceGroup.addGroup( theVertical4ParallelGroup );		

		theGroupLayout.setHorizontalGroup( theHorizSequenceGroup );
		theGroupLayout.setVerticalGroup( theVerticalSequenceGroup );

		return thePanel;
	}

	private void configureOptionsRelatedToContextPkg() {

		String selectedValue = _createContextPkgChooser._userChoiceComboBox.getSelectedItem().toString();
		//_context.info( selectedValue );

		if( selectedValue.equals( _createContextPkgChooser._doNothingOption ) ){

			// set context package name to none
			_createContextPkgChooser._nameTextField.setText( _createContextPkgChooser._none );
			_createContextPkgChooser._nameTextField.setEnabled( false );

			// set default to create new package
			_createSignalsPkgChooser._userChoiceComboBox.setSelectedItem( _createSignalsPkgChooser._doNothingOption );

			_createSignalsPkgChooser.getM_UserChoiceComboBox().setEnabled( false );

			_createSignalsPkgChooser._nameTextField.setVisible( true );
			_createSignalsPkgChooser._nameTextField.setEnabled( false );	

		} else if( selectedValue.equals( _createContextPkgChooser._createNewWithNameOption ) ){

			final String theDefaultContextPkgName = _context.getDefaultContextDiagramPackageName();

			String theUniqueName = 
					_context.determineUniqueNameBasedOn( 
							theDefaultContextPkgName, "Package", _ownerPkg );

			_createContextPkgChooser._nameTextField.setText( theUniqueName );
			_createContextPkgChooser._nameTextField.setEnabled( true );	

			_createSignalsPkgChooser._userChoiceComboBox.setSelectedItem( _createSignalsPkgChooser._createNewButEmptyOption );
			_createSignalsPkgChooser._nameTextField.setVisible( true );
			_createSignalsPkgChooser._nameTextField.setEnabled( true );	
			_createSignalsPkgChooser._userChoiceComboBox.setEnabled( true );


		} else if( selectedValue.contains( _createContextPkgChooser._createNewBasedOnExistingOption ) ){

		}
	}

	private void updateRelatedElementNames(){

		_createRequirementsPkgChooser.updateRequirementsPkgNameBasedOn( 
				_nameTextField.getText() + _requirementPackagePostfix );
	}

	private JPanel createTheNameThePackagePanel(
			IRPModelElement basedOnContext,
			String theUniqueName ){

		JPanel thePanel = new JPanel();
		thePanel.setLayout( new FlowLayout( FlowLayout.LEFT ) );

		_nameTextField = new JTextField( theUniqueName );
		_nameTextField.setPreferredSize( new Dimension( 200, 20 ));

		_nameTextField.getDocument().addDocumentListener(
				new DocumentListener() {

					@Override
					public void changedUpdate( DocumentEvent arg0 ){
						updateRelatedElementNames();					
					}

					@Override
					public void insertUpdate( DocumentEvent arg0 ){
						updateRelatedElementNames();
					}

					@Override
					public void removeUpdate( DocumentEvent arg0 ){
						updateRelatedElementNames();
					}	
				});

		thePanel.add( new JLabel( "Choose a unique name:" ) );
		thePanel.add( _nameTextField );	
		thePanel.add( new JLabel( " (package post-fixed with " + _usecasePackagePostfix + 
				" will created under " + _context.elInfo(basedOnContext) + ")" ) );

		return thePanel;
	}

	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled ){

		boolean isValid = true;
		String errorMsg = "";
		
		String theUseCasePackageName = _nameTextField.getText() + _usecasePackagePostfix;
		String theReqtPackageName = _createRequirementsPkgChooser.getReqtsPkgOptionalName();
		CreateRequirementsPkgChooser.CreateRequirementsPkgOption theReqtsPkgChoice = _createRequirementsPkgChooser.getReqtsPkgChoice();
		
		boolean isLegalName = _context.isLegalName( theUseCasePackageName, _ownerPkg );

		if( !isLegalName ){

			errorMsg += theUseCasePackageName + " is not legal as a package name\n";				
			isValid = false;

		} else if( !_context.isElementNameUnique(
				theUseCasePackageName, "Package", _ownerPkg, 1) ){

			errorMsg += "Unable to proceed as the package name '" + theUseCasePackageName + "' is not unique ";
			isValid = false;
		
		} else if( ( theReqtsPkgChoice == CreateRequirementsPkgOption.CreateUnderProject ||
				theReqtsPkgChoice == CreateRequirementsPkgOption.CreateUnderProjectWithStereotype ) &&
				!_context.isElementNameUnique(
				theReqtPackageName, "Package", _ownerPkg, 1)){

			errorMsg += "Unable to proceed as the package name '" + theReqtPackageName + "' is not unique";
			isValid = false;
		}

		if (isMessageEnabled && !isValid && errorMsg != null){

			UserInterfaceHelper.showWarningDialog( errorMsg );
		}

		return isValid;		
	}

	@Override
	protected void performAction(){

		if( checkValidity( false ) ){

			IRPProject theProject = _context.get_rhpPrj();
			
			String theUseCaseUnadornedName = _nameTextField.getText(); 
			String theAdornedName = theUseCaseUnadornedName + _usecasePackagePostfix;

			//_context.debug( "The name is " + theAdornedName );

			IRPPackage theUseCasePkg = _ownerPkg.addNestedPackage( theAdornedName );
			theUseCasePkg.changeTo( _context.REQTS_ANALYSIS_USE_CASE_PACKAGE );
			_context.setSavedInSeparateDirectoryIfAppropriateFor( theUseCasePkg );

			_createRequirementsPkgChooser.createRequirementsPackage( theUseCasePkg );
			
			CreateActorPkg theActorPkgCreator = new CreateActorPkg( _context );

			if( _createActorChooser.getCreateActorPkgOption() == CreateActorPkgOption.CreateNew ){

				theActorPkgCreator.createNew( theProject, _createActorChooser.getActorsPkgNameIfChosen(), theUseCasePkg );

			} else if( _createActorChooser.getCreateActorPkgOption() == CreateActorPkgOption.CreateNewButEmpty ){

				theActorPkgCreator.createNewButEmpty( theProject, _createActorChooser.getActorsPkgNameIfChosen(), theUseCasePkg );

			} else if( _createActorChooser.getCreateActorPkgOption() == CreateActorPkgOption.InstantiateFromExisting ){

				theActorPkgCreator.instantiateFromExisting( 
						theUseCasePkg, 
						_createActorChooser.getActorsPkgNameIfChosen() + theAdornedName, 
						theUseCasePkg, 
						_createActorChooser.getExistingActorPkgIfChosen(), 
						theUseCaseUnadornedName );

			} else if( _createActorChooser.getCreateActorPkgOption()  == CreateActorPkgOption.UseExisting ){

				theActorPkgCreator.useExisting( _createActorChooser.getExistingActorPkgIfChosen() );
			}

			List<IRPActor> theActors = theActorPkgCreator.getActors();

			ContextDiagramCreator theCTXCreator = new ContextDiagramCreator( _context );

			String theContextPkgChoice = (String) _createContextPkgChooser._userChoiceComboBox.getSelectedItem();

			IRPPackage theContextPkg = null;

			if( theContextPkgChoice.equals( _createContextPkgChooser._doNothingOption ) ){	

			} else if( theContextPkgChoice.equals( _createContextPkgChooser._createNewWithNameOption) ) {

				String theContextElementName = _createContextPkgChooser.getM_NameTextField().getText();
				theContextPkg = theCTXCreator.createContextPackage( _ownerPkg, theContextElementName );
				IRPRelation theContextEl = theContextPkg.addImplicitObject( theContextElementName );

				IRPStructureDiagram theContextDiagram = theCTXCreator.
						createContextDiagram( theContextPkg, theActors, theContextElementName, theContextEl );

				theContextDiagram.highLightElement();

			} else if( theContextPkgChoice.contains( _createContextPkgChooser._createNewBasedOnExistingOption ) ) {

				IRPClassifier theClassifier = _createContextPkgChooser.getExistingBlockIfChosen();
				String theContextElementName = theClassifier.getName();				
				theContextPkg = theCTXCreator.createContextPackage( _ownerPkg, theContextElementName );
				IRPRelation theContextEl = theContextPkg.addImplicitObject( "" );
				theContextEl.setOtherClass( (IRPClassifier) theClassifier );

				IRPStructureDiagram theContextDiagram = theCTXCreator.
						createContextDiagram( theContextPkg, theActors, theContextElementName, theContextEl );

				theContextDiagram.highLightElement();
			}

			ExternalSignalsPkgCreator theSignalsPkgCreator = new ExternalSignalsPkgCreator( _context );

			String theSignalsPkgChoice = (String) _createSignalsPkgChooser._userChoiceComboBox.getSelectedItem();

			if( theSignalsPkgChoice.equals( _createSignalsPkgChooser._doNothingOption ) ){	

				// Do nothing

			} else if( theSignalsPkgChoice.equals( _createSignalsPkgChooser._createNewButEmptyOption ) ){

				IRPPackage theSignalsPkg = theSignalsPkgCreator.createExternalSignalsPackage(
						theProject, 
						_createSignalsPkgChooser.getExternalSignalsPkgNameIfChosen() );


				theContextPkg.addDependencyTo( theSignalsPkg );

			} else if( theSignalsPkgChoice.contains( _createSignalsPkgChooser._existingPkgPrefix ) ){

				IRPPackage theSignalsPkg = _createSignalsPkgChooser.getExistingExternalSignalsPkgIfChosen();
				theContextPkg.addDependencyTo( theSignalsPkg );				
			}

			UseCaseDiagramCreator theUCDCreator = new UseCaseDiagramCreator( _context );
			IRPUseCaseDiagram theUCD = theUCDCreator.createUseCaseDiagram( theActors, theAdornedName, theUseCasePkg );
			theUCD.highLightElement();

			_context.deleteIfPresent( "Structure1", "StructureDiagram", theProject );
			_context.deleteIfPresent( "Model1", "ObjectModelDiagram", theProject );
			_context.deleteIfPresent( "Default", "Package", theProject );

			if( _context.getIsAutoPopulatePackageDiagram( theProject ) ){
				AutoPackageDiagram theAPD = new AutoPackageDiagram( _context );
				theAPD.drawDiagram();
			}

			theProject.save();

			_context.info( "Package structure construction of " + _context.elInfo( theUseCasePkg ) + " has completed");	
		}
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