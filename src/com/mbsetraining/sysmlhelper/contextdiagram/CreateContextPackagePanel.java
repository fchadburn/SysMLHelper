package com.mbsetraining.sysmlhelper.contextdiagram;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
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

import com.mbsetraining.sysmlhelper.common.RhapsodyComboBox;
import com.mbsetraining.sysmlhelper.executablembse.AutoPackageDiagram;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSEBasePanel;
import com.mbsetraining.sysmlhelper.requirementpackage.CreateRequirementsPkgChooser;
import com.mbsetraining.sysmlhelper.usecasepackage.CreateActorPkg;
import com.mbsetraining.sysmlhelper.usecasepackage.CreateActorPkgChooser;
import com.mbsetraining.sysmlhelper.usecasepackage.CreateActorPkg.CreateActorPkgOption;
import com.telelogic.rhapsody.core.*;

public class CreateContextPackagePanel extends ExecutableMBSEBasePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6513253713132959527L;
	private IRPPackage _ownerPkg;
	private CreateActorPkgChooser _createActorChooser;
	private CreateRequirementsPkgChooser _createRequirementsPkgChooser;
	private CreateSignalsPkgChooser _createExternalSignalsPkgChooser;
	private JTextField _nameTextField;
	protected RhapsodyComboBox _selectClassComboBox = null;

	public static void main(String[] args) {
	
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		
		launchTheDialog( theRhpApp.getApplicationConnectionString() );
	}
	
	public static void launchTheDialog(
			final String theAppID ){

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame( "Populate context diagram package structure" );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateContextPackagePanel thePanel = 
						new CreateContextPackagePanel(
								theAppID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}

	public CreateContextPackagePanel( 
			String theAppID ){

		super( theAppID );

		_ownerPkg = _context.get_rhpPrj();

		setLayout( new BorderLayout() );
		setBorder( new EmptyBorder( 0, 10, 10, 10 ) );

		String theUniqueName = 
				_context.determineUniqueNameForPackageBasedOn(
						_context.getDefaultContextDiagramPackageName(),
						_ownerPkg );

		JPanel theMainPanel = createContent();
		theMainPanel.setAlignmentX( Component.LEFT_ALIGNMENT );

		String introText = 
				"This helper will create a package hierarchy for simple context diagrams underneath the " + 
						_context.elInfo( _ownerPkg ) + ". \n" +
						"It creates a nested package structure and context diagram, imports the appropriate profiles if not present, and sets default \n" +
						"display and other options to appropriate values for this using Rhapsody profile and property settings.\n";

		JPanel theStartPanel = new JPanel();

		theStartPanel.setLayout( new BoxLayout( theStartPanel, BoxLayout.PAGE_AXIS ) );
		theStartPanel.add( new JLabel( " " ) );
		theStartPanel.add( createContextNameChoicePanel( theUniqueName ) );
		theStartPanel.add( createPanelWithTextCentered( introText ) );

		add( theStartPanel, BorderLayout.PAGE_START );
		add( theMainPanel, BorderLayout.CENTER );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );		
	}

	private JPanel createContent(){

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
				_context.getDefaultRequirementPackageName(), 
				false,
				true,
				_context );

		_createExternalSignalsPkgChooser = new CreateSignalsPkgChooser( 
				_ownerPkg, 
				_context );
		
		theColumn1ParallelGroup.addComponent( _createExternalSignalsPkgChooser.getM_UserChoiceComboBox() ); 
		theColumn1ParallelGroup.addComponent( _createActorChooser.getM_UserChoiceComboBox() );    
		theColumn1ParallelGroup.addComponent( _createRequirementsPkgChooser.getM_UserChoiceComboBox() ); 
		theColumn2ParallelGroup.addComponent( _createExternalSignalsPkgChooser.getM_NameTextField() );   
		theColumn2ParallelGroup.addComponent( _createActorChooser.getM_NameTextField() );   
		theColumn2ParallelGroup.addComponent( _createRequirementsPkgChooser.getM_NameTextField() );   

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

		theVertical3ParallelGroup.addComponent( _createExternalSignalsPkgChooser.getM_UserChoiceComboBox() );
		theVertical3ParallelGroup.addComponent( _createExternalSignalsPkgChooser.getM_NameTextField() );

		
		theVerticalSequenceGroup.addGroup( theVertical1ParallelGroup );		
		theVerticalSequenceGroup.addGroup( theVertical2ParallelGroup );		
		theVerticalSequenceGroup.addGroup( theVertical3ParallelGroup );		

		theGroupLayout.setHorizontalGroup( theHorizSequenceGroup );
		theGroupLayout.setVerticalGroup( theVerticalSequenceGroup );

		return thePanel;
	}
	
	private JPanel createContextNameChoicePanel(
			String theUniqueName ){
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.X_AXIS ) );	
		
		_nameTextField = new JTextField( theUniqueName );
		_nameTextField.setPreferredSize( new Dimension( 200, 20 ) );
		
		List<IRPModelElement> theSystemBlocks = 
				_context.findElementsWithMetaClassAndStereotype( 
						"Class", 
						_context.SYSTEM_BLOCK, 
						_context.get_rhpPrj(), 
						1 );
		
		_selectClassComboBox = new RhapsodyComboBox( theSystemBlocks, false );
		
		_selectClassComboBox.addActionListener( new ActionListener () {
		    public void actionPerformed( ActionEvent e ){
		        
		    	IRPModelElement theItem = _selectClassComboBox.getSelectedRhapsodyItem();
		    	
		    	if( theItem instanceof IRPClass ){
		    		_nameTextField.setEnabled( false );
		    	} else {
		    		_nameTextField.setEnabled( true );
		    	}
		    }
		});
		
		thePanel.add( new JLabel( "Choose a unique name: " ) );
	    thePanel.add( _nameTextField );
	    
		JLabel theLabel = new JLabel( "  or select existing " + _context.SYSTEM_BLOCK + ":  " );
		thePanel.add( theLabel );
		thePanel.add( _selectClassComboBox );	    	
	    
		_nameTextField.requestFocusInWindow();
		
		return thePanel;
	}

	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled ){
		// TODO 	
		return true;
	}

	@Override
	protected void performAction(){

		if( checkValidity( false ) ){

			IRPModelElement theSystemBlock = _selectClassComboBox.getSelectedRhapsodyItem();
			
			String theContextElementName;
			
			ContextDiagramCreator theCreator = new ContextDiagramCreator( _context );
			IRPPackage theContextPkg;
			IRPRelation theContextEl;
			
			if( theSystemBlock instanceof IRPClassifier ){
				
				theContextElementName = theSystemBlock.getName();				
				theContextPkg = theCreator.createContextPackage( _ownerPkg, theContextElementName );
				theContextEl = theContextPkg.addImplicitObject( "" );
				theContextEl.setOtherClass( (IRPClassifier) theSystemBlock );
			} else {
				theContextElementName = _nameTextField.getText(); 
				theContextPkg = theCreator.createContextPackage( _ownerPkg, theContextElementName );
				theContextEl = theContextPkg.addImplicitObject( theContextElementName );		
			}	
											
			_createRequirementsPkgChooser.createRequirementsPackage( theContextPkg );
			
			CreateActorPkg theActorPkgCreator = new CreateActorPkg( _context );
			
			CreateActorPkgOption theActorPkgChoice = _createActorChooser.getCreateActorPkgOption();
			
			IRPProject theProject = _context.get_rhpPrj();

			if( theActorPkgChoice == CreateActorPkgOption.CreateNew ){
				
				theActorPkgCreator.createNew( theProject, _createActorChooser.getActorsPkgNameIfChosen(), theContextPkg );
			
			} else if( theActorPkgChoice == CreateActorPkgOption.CreateNewButEmpty ){
				
				theActorPkgCreator.createNewButEmpty( theProject, _createActorChooser.getActorsPkgNameIfChosen(), theContextPkg );
				
			} else if( theActorPkgChoice == CreateActorPkgOption.InstantiateFromExisting ){
				
				theActorPkgCreator.instantiateFromExisting( 
						theContextPkg, 
						_createActorChooser.getActorsPkgNameIfChosen() + theContextElementName, 
						theContextPkg, 
						_createActorChooser.getExistingActorPkgIfChosen(), 
						theContextElementName );
				
			} else if( theActorPkgChoice == CreateActorPkgOption.UseExisting ){
				
				theActorPkgCreator.useExisting( _createActorChooser.getExistingActorPkgIfChosen() );
			}
			
			ExternalSignalsPkgCreator theSignalsPkgCreator = new ExternalSignalsPkgCreator( _context );

			String theSignalsPkgChoice = (String) _createExternalSignalsPkgChooser._userChoiceComboBox.getSelectedItem();

			if( theSignalsPkgChoice.equals( _createExternalSignalsPkgChooser._doNothingOption ) ){	
				
				// Do nothing
				
			} else if( theSignalsPkgChoice.equals( _createExternalSignalsPkgChooser._createNewButEmptyOption ) ){
			
				IRPPackage theSignalsPkg = theSignalsPkgCreator.createExternalSignalsPackage(
						theProject, 
						_createExternalSignalsPkgChooser.getExternalSignalsPkgNameIfChosen() );
				
				
				theContextPkg.addDependencyTo( theSignalsPkg );
				
			} else if( theSignalsPkgChoice.contains( _createExternalSignalsPkgChooser._existingPkgPrefix ) ){
				
				IRPPackage theSignalsPkg = _createExternalSignalsPkgChooser.getExistingExternalSignalsPkgIfChosen();
				theContextPkg.addDependencyTo( theSignalsPkg );				
			}
			
			List<IRPActor> theActors = theActorPkgCreator.getActors();
			
			IRPDiagram theDiagram = theCreator.
					createContextDiagram( theContextPkg, theActors, theContextElementName, theContextEl );
			
			theDiagram.highLightElement();
					
			_context.deleteIfPresent( "Structure1", "StructureDiagram", theProject );
			_context.deleteIfPresent( "Model1", "ObjectModelDiagram", theProject );
			_context.deleteIfPresent( "Default", "Package", theProject );
			
			if( _context.getIsAutoPopulatePackageDiagram( theProject ) ){
				AutoPackageDiagram theAPD = new AutoPackageDiagram( _context );
				theAPD.drawDiagram();
			}
				    			
			theProject.save();
		}
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