package com.mbsetraining.sysmlhelper.executablembse;

import functionalanalysisplugin.ActorMappingInfo;
import functionalanalysisplugin.PopulateFunctionalAnalysisPkg.SimulationType;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.mbsetraining.sysmlhelper.common.BlockDiagramHelper;
import com.mbsetraining.sysmlhelper.common.NamedElementMap;
import com.mbsetraining.sysmlhelper.common.RhapsodyComboBox;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.sequencediagram.SequenceDiagramCreator;
import com.telelogic.rhapsody.core.*;

public class CreateFunctionalExecutablePackagePanel extends ExecutableMBSEBasePanel {

	final private String _blankName = "<Put Name Here>";
	private IRPPackage _rootPackage;
	private List<IRPActor> _originalActors;
	private List<IRPPackage> _useCasePkgs;
	private List<ActorMappingInfo> _actorChoices;
	private RhapsodyComboBox _blockInheritanceChoice;
	private JTextField _blockNameTextField;
	private RhapsodyComboBox _testDriverInheritanceChoice;
	private JTextField _testDriverNameTextField;
	private JCheckBox _testDriverCheckBox;
	private SimulationType _simulationType;
	private RhapsodyComboBox _chosenStereotype;
	private Set<String> _excludeMetaClasses = new HashSet<>( Arrays.asList( "Actor" ) );

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
	
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		launchThePanel( SimulationType.FullSim, theRhpApp.getApplicationConnectionString() );
	}
	
	public static void launchThePanel(
			final SimulationType withSimulationType,
			String theAppID ) {

		javax.swing.SwingUtilities.invokeLater( new Runnable() {

			@Override
			public void run() {

				//JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame( "Populate a 'system' block package hierarchy" );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateFunctionalExecutablePackagePanel thePanel = 
						new CreateFunctionalExecutablePackagePanel(
								theAppID, 
								withSimulationType );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}
	
	private List<IRPActor> getActorsAssociatedToUseCases(
			IRPModelElement underneathTheEl ){

		List<IRPActor> theActors = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theUseCaseEls = underneathTheEl.getNestedElementsByMetaClass( 
				"UseCase", 1 ).toList();

		for( IRPModelElement theUseCaseEl : theUseCaseEls ){

			IRPUseCase theUseCase = (IRPUseCase) theUseCaseEl;

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theRelationEls = theUseCase.getReferences().toList();

			for( IRPModelElement theRelationEl : theRelationEls ){

				//_context.debug("Found " + _context.elInfo( theRelationEl ) );

				if( theRelationEl.getMetaClass().equals( "AssociationEnd" ) ){
					
					IRPRelation theRelation = (IRPRelation) theRelationEl;
					IRPClassifier theOtherClass = theRelation.getOtherClass();

					if( theOtherClass instanceof IRPActor &&
							!theActors.contains( theOtherClass ) ){
						theActors.add( (IRPActor) theOtherClass );
					}

					IRPClassifier theOfClass = theRelation.getOfClass();

					if( theOfClass instanceof IRPActor &&
							!theActors.contains( theOfClass ) ){
						theActors.add( (IRPActor) theOfClass );
					}
				}
			}
		}

		return theActors;
	}

	public static int getSelection( JOptionPane optionPane ){
		
		int returnValue = JOptionPane.OK_CANCEL_OPTION;

		Object selectedValue = optionPane.getValue();
		
		if( selectedValue != null ){
			
			Object options[] = optionPane.getOptions();
			
			if( options == null ){
				if( selectedValue instanceof Integer ) {
					returnValue = ((Integer) selectedValue).intValue();
				}
			} else {
				for( int i = 0, n = options.length; i < n; i++ ){
					if( options[ i ].equals( selectedValue ) ){
						returnValue = i;
						break; // out of for loop
					}
				}
			}
		}
		
		return returnValue;
	}

	CreateFunctionalExecutablePackagePanel(
			String appID,
			final SimulationType withSimulationType ){

		super( appID );
		
		_rootPackage = _context.get_rhpPrj();

		List<IRPModelElement> theUseCasePackages =
				_context.findElementsWithMetaClassAndStereotype(
						"Package", 
						_context.REQTS_ANALYSIS_USE_CASE_PACKAGE, 
						_context.get_rhpPrj(), 
						1 );

		NamedElementMap theElementMap = new NamedElementMap( theUseCasePackages );
		JList<?> list = new JList<Object>( theElementMap.getFullNames() );

		list.setSelectionMode(
				ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );

		JOptionPane pane = new JOptionPane( new JScrollPane( list )  );

		pane.add( new JLabel( "" ) );
		pane.add( new JLabel( "Choose the use case packages you want to base structure package on" ) );
		pane.add( new JLabel( "" ) );

		JDialog d = pane.createDialog( 
				null, 
				"Multi-select the packages you want to draw Actors or Activity Diagrams from" );

		d.setVisible(true);
		@SuppressWarnings("unused")
		int selection = getSelection(pane);

		List<?> theValues = list.getSelectedValuesList();

		_originalActors = new ArrayList<>();
		_useCasePkgs = new ArrayList<>();

		for( Object theValue : theValues ){
			//_context.debug( "Value is " + theValue.toString() );

			IRPModelElement theUseCasePkg =
					theElementMap.getElementUsingFullName( theValue.toString() );

			_useCasePkgs.add( (IRPPackage) theUseCasePkg );

			List<IRPActor> theActors = 
					getActorsAssociatedToUseCases( theUseCasePkg );

			for (IRPActor theActor : theActors) {
				if( !_originalActors.contains( theActor ) ){
					_originalActors.add( theActor );
				}
			}
		}

		//_context.debug("Setting root package to " + _context.elInfo( _rootPackage ) );
		//_context.debug("There are " + _originalActors.size() + " original actors found" );

		_simulationType = withSimulationType;

		setLayout( new BorderLayout() );

		String theBlockName = 
				_context.determineUniqueNameBasedOn(
						_blankName, 
						"Class", 
						_rootPackage.getProject() );

		add( createTheNameTheBlockPanel( theBlockName ), BorderLayout.PAGE_START );
		add( createContent( theBlockName ), BorderLayout.CENTER );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}

	private void clearActorNamesIfNeeded(){

		for( ActorMappingInfo theInfo : _actorChoices ){	
			JTextField theField = theInfo.getTextField();
			theField.setVisible( theInfo.isSelected() );			
		}			
	}

	private void updateRelatedElementNames(){

		String theBlockName = _blockNameTextField.getText();

		if( _actorChoices != null ){
			for( ActorMappingInfo theInfo : _actorChoices ){
				theInfo.updateToBestActorNamesBasedOn( theBlockName );				
			}	
		}

		if( _testDriverNameTextField != null ){

			_testDriverNameTextField.setText(
					determineTestDriverName( theBlockName ) );
		}
	}

	private String determineTestDriverName(
			String basedOnBlockName ){

		String theProposedName = _context.determineUniqueNameBasedOn( 
				_context.toLegalClassName( basedOnBlockName ) + "_TestDriver", 
				"Class", 
				_rootPackage.getProject() );

		return theProposedName;
	}

	private JPanel createTheNameTheBlockPanel(String theBlockName){

		JPanel thePanel = new JPanel();
		thePanel.setLayout( new FlowLayout( FlowLayout.LEFT ) );

		List<IRPModelElement> theExistingBlocks = 
				_context.findElementsWithMetaClassAndStereotype(
						"Class", "LogicalSystem", _rootPackage, 1);

		_blockNameTextField = new JTextField( theBlockName );
		_blockNameTextField.setPreferredSize( new Dimension( 200, 20 ));

		_blockNameTextField.getDocument().addDocumentListener(
				new DocumentListener() {

					@Override
					public void changedUpdate(DocumentEvent arg0) {
						updateRelatedElementNames();					
					}

					@Override
					public void insertUpdate(DocumentEvent arg0) {
						updateRelatedElementNames();
					}

					@Override
					public void removeUpdate(DocumentEvent arg0) {
						updateRelatedElementNames();
					}	
				});

		_blockInheritanceChoice = new RhapsodyComboBox(theExistingBlocks, false);

		thePanel.add( new JLabel("                       Block name:") );
		thePanel.add( _blockNameTextField );			

		List<IRPModelElement> theStereotypes = 
				_context.getStereotypesForBlockPartCreation();

		_chosenStereotype = new RhapsodyComboBox( theStereotypes, false );

		if( theStereotypes.size() > 0 ){
			_chosenStereotype.setSelectedRhapsodyItem( theStereotypes.get( 1 ) );
		}

		thePanel.add( new JLabel( "  Stereotype as: " ) );
		thePanel.add( _chosenStereotype );

		return thePanel;
	}

	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled ){

		boolean isValid = true;
		String errorMsg = "";

		String theChosenBlockName = _blockNameTextField.getText();

		if ( theChosenBlockName.contains( _blankName ) ){

			errorMsg += "Please choose a valid name for the Block";
			isValid = false;

		} else {
			boolean isLegalBlockName = _context.isLegalName( theChosenBlockName, _rootPackage );

			if( !isLegalBlockName ){

				errorMsg += theChosenBlockName + " is not legal as an identifier representing an executable Block\n";				
				isValid = false;

			} else if( !_context.isElementNameUnique(
					theChosenBlockName, "Class", _rootPackage, 1 ) ){

				errorMsg += "Unable to proceed as the Block name '" + theChosenBlockName + "' is not unique";
				isValid = false;

			} else {

				for( ActorMappingInfo actorChoice : _actorChoices ){

					String theChosenActorName = actorChoice.getName();

					if( actorChoice.isSelected() ){

						boolean isLegalActorName = _context.isLegalName( theChosenActorName, _rootPackage );

						if( !isLegalActorName ){
							errorMsg += theChosenActorName + "is not legal as an identifier representing an executable actor\n";
							isValid = false;

						} else if( !_context.isElementNameUnique(
								theChosenActorName, "Actor", _rootPackage, 1 ) ){

							errorMsg += theChosenActorName + " is not unique, please choose again\n";
						}
					}
				}
			}
		}

		if( isMessageEnabled && !isValid && errorMsg != null ){

			UserInterfaceHelper.showWarningDialog( errorMsg );
		}

		return isValid;
	}

	private JPanel createContent(
			String theBlockName ){

		JPanel thePanel = new JPanel();
		thePanel.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

		GroupLayout theGroupLayout = new GroupLayout( thePanel );
		thePanel.setLayout( theGroupLayout );
		theGroupLayout.setAutoCreateGaps( true );

		SequentialGroup theHorizSequenceGroup = theGroupLayout.createSequentialGroup();
		SequentialGroup theVerticalSequenceGroup = theGroupLayout.createSequentialGroup();

		ParallelGroup theColumn1ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );
		ParallelGroup theColumn2ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );

		theHorizSequenceGroup.addGroup( theColumn1ParallelGroup );
		theHorizSequenceGroup.addGroup( theColumn2ParallelGroup );

		_actorChoices = new ArrayList<ActorMappingInfo>();

		if( _simulationType==SimulationType.FullSim ){

			//_context.debug( "There are " + _originalActors.size() );

			for( IRPModelElement theActor : _originalActors ){

				//_context.debug( "Creating actor '"+ theActor.getName() + "'" );

				JCheckBox theActorCheckBox = new JCheckBox("Create actor called:");

				theActorCheckBox.setSelected(true);

				theActorCheckBox.addActionListener( new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						clearActorNamesIfNeeded();		
					}
				});

				JTextField theActorNameTextField = new JTextField();
				theActorNameTextField.setPreferredSize( new Dimension( 200, 20 ) );

				List<IRPModelElement> theBlankList = new ArrayList<>();

				RhapsodyComboBox theInheritedActorComboBox = new RhapsodyComboBox( theBlankList, false );			
				theInheritedActorComboBox.setPreferredSize(new Dimension(100, 20));

				ActorMappingInfo theMappingInfo = 
						new ActorMappingInfo(
								theInheritedActorComboBox, 
								theActorCheckBox, 
								theActorNameTextField, 
								(IRPActor)theActor,								
								_context );

				theMappingInfo.updateToBestActorNamesBasedOn( theBlockName );

				_actorChoices.add( theMappingInfo );

				theColumn1ParallelGroup.addComponent( theActorCheckBox );   
				theColumn2ParallelGroup.addComponent( theActorNameTextField );    

				ParallelGroup theVertical1ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE);
				theVertical1ParallelGroup.addComponent( theActorCheckBox );
				theVertical1ParallelGroup.addComponent( theActorNameTextField );

				theVerticalSequenceGroup.addGroup( theVertical1ParallelGroup );		    		    
			}

			_testDriverCheckBox = new JCheckBox("Create TestDriver called:");
			_testDriverCheckBox.setEnabled( false );
			_testDriverCheckBox.setSelected( true );

			_testDriverNameTextField = new JTextField( determineTestDriverName( theBlockName ) );
			_testDriverNameTextField.setPreferredSize( new Dimension( 200, 20 ));
			_testDriverNameTextField.setEnabled( false );
			_testDriverNameTextField.setEditable( false );

			List<IRPModelElement> theExistingBlocks = 
					_context.findElementsWithMetaClassAndStereotype(
							"Class", "TestDriver", _rootPackage, 1);

			_testDriverInheritanceChoice = new RhapsodyComboBox( theExistingBlocks, false );			
			_testDriverInheritanceChoice.setPreferredSize( new Dimension(100, 20) );

			theColumn1ParallelGroup.addComponent( _testDriverCheckBox );   
			theColumn2ParallelGroup.addComponent( _testDriverNameTextField );    

			ParallelGroup theVertical1ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE);
			theVertical1ParallelGroup.addComponent( _testDriverCheckBox );
			theVertical1ParallelGroup.addComponent( _testDriverNameTextField );

			theVerticalSequenceGroup.addGroup( theVertical1ParallelGroup );	
		}

		theGroupLayout.setHorizontalGroup( theHorizSequenceGroup );
		theGroupLayout.setVerticalGroup( theVerticalSequenceGroup );

		return thePanel;
	}


	@Override
	protected void performAction(){

		if( checkValidity( false ) ){

			String theName = _blockNameTextField.getText();

			// Create packages first
			IRPPackage theRootPkg = 
					_context.addNewTermPackageAndSetUnitProperties(
							theName + "Pkg",
							_rootPackage,
							_context.FUNCT_ANALYSIS_SCENARIOS_PACKAGE );
			
			_context.info( "Starting construction of " + _context.elInfo( theRootPkg ) );

			// Create nested package for block		
			IRPPackage theBlockPkg = _context.addNewTermPackageAndSetUnitProperties(
					"Blocks_" + theName + "Pkg",
					theRootPkg,
					_context.FUNCT_ANALYSIS_DESIGN_PACKAGE );

			// Create nested package for events and interfaces
			IRPPackage theInterfacesPkg = 
					_context.addNewTermPackageAndSetUnitProperties(
							"Interfaces_" + theName + "Pkg",
							theRootPkg,
							_context.FUNCT_ANALYSIS_INTERFACES_PACKAGE );

			// Create nested TestPkg package with components necessary for wiring up a simulation
			IRPPackage theTestPkg = 
					_context.addNewTermPackageAndSetUnitProperties(
							"Test_" + theName  + "Pkg",
							theRootPkg,
							_context.FUNCT_ANALYSIS_TEST_PACKAGE );

			// Create nested package for housing the ADs
			IRPPackage theWorkingPackage = 
					_context.addNewTermPackageAndSetUnitProperties(
					"Working_" + theName + "Pkg",
					theRootPkg,
					_context.REQTS_ANALYSIS_WORKING_COPY_PACKAGE );

			// Add dependency to the use case package to enable drawing of activity diagram copies into the working package
			for( IRPPackage theUseCasePkg : _useCasePkgs ){
				theWorkingPackage.addDependencyTo( theUseCasePkg );
			}
			
			// Populate content for the BlockPkg
			IRPClass theLogicalSystemBlock = theBlockPkg.addClass( theName );

			IRPClass theSystemAssemblyBlock = 
					theRootPkg.addClass( theName + "_SystemAssembly" );

			theSystemAssemblyBlock.changeTo("Block");

			// Make the LogicalSystem a part of the SystemAssembly block
			IRPInstance theLogicalSystemPart = 
					(IRPInstance) theSystemAssemblyBlock.addNewAggr(
							"Part", "" );

			theLogicalSystemPart.setOtherClass( theLogicalSystemBlock );
			
			_context.get_selectedContext().setupFunctionalAnalysisTagsFor( 
					theRootPkg,
					theSystemAssemblyBlock,
					theInterfacesPkg,
					theTestPkg,
					theBlockPkg );

			_context.get_selectedContext().setContextTo( theRootPkg );

			//IRPModelElement theChosenStereotype = m_ChosenStereotype.getSelectedRhapsodyItem();
			//
			//if( theChosenStereotype != null && 
			//	theChosenStereotype instanceof IRPStereotype ){
			//	
			//	theLogicalSystemBlock.setStereotype( (IRPStereotype) theChosenStereotype );
			//}

			theLogicalSystemBlock.changeTo( "Block" );
			theLogicalSystemBlock.highLightElement();

			// only apply generalisation to create the state chart if simulation applies
			if( _simulationType==SimulationType.FullSim || 
					_simulationType==SimulationType.SimpleSim ){

				IRPModelElement theChosenOne = 
						_blockInheritanceChoice.getSelectedRhapsodyItem();

				if( theChosenOne==null ){

					IRPStereotype theTimeElapsedBlockStereotype = 
							_context.getStereotypeForTimeElapsedBlock();

					theLogicalSystemBlock.setStereotype( theTimeElapsedBlockStereotype );					

				} else {
					theLogicalSystemBlock.addGeneralization( (IRPClassifier) theChosenOne );
					//_context.debug( _context.elInfo( theChosenOne ) + "was the chosen one" );
				}

				// Add Usage dependency to the interfaces package that will contain the system events
				IRPDependency theBlocksUsageDep = theBlockPkg.addDependencyTo( theInterfacesPkg );
				theBlocksUsageDep.addStereotype( "Usage", "Dependency" );

				// Add Usage dependency to the interfaces package that will contain the events
				IRPDependency theUsageDep = theTestPkg.addDependencyTo( theInterfacesPkg );
				theUsageDep.addStereotype( "Usage", "Dependency" );
			}

			//			if( theReqtsPkg != null && theReqtsPkg instanceof IRPPackage ){
			//				
			//				// Don't create RD if one already exists
			//				String theRDName = "RD - " + theName;
			//				
			//				IRPModelElement theRD = 
			//						GeneralHelpers.getExistingOrCreateNewElementWith(
			//								theRDName, "ObjectModelDiagram", theReqtsPkg );
			//				
			//				if( theRD != null ){					
			//					theRD.changeTo( "Requirements Diagram" );
			//				}
			//			}



			//if( theChosenStereotype != null && 
			//	theChosenStereotype instanceof IRPStereotype ){
			//		
			//	theLogicalSystemPart.setStereotype( (IRPStereotype) theChosenStereotype );
			//}

			// Populate nested TestPkg package with components necessary for wiring up a simulation

			if( _simulationType==SimulationType.FullSim ||
					_simulationType==SimulationType.SimpleSim ){

				// Make ElapsedTime actor part of the SystemAssembly block
			
				IRPActor theElapsedTimeActor = 
						theTestPkg.addActor( "ElapsedTime_" + theName );

				theElapsedTimeActor.setStereotype( 
						_context.getStereotypeForTimeElapsedActor() );

				IRPInstance theElapsedTimePart = null;

				theElapsedTimePart = 
						(IRPInstance) theSystemAssemblyBlock.addNewAggr(
								"Part", "");

				theElapsedTimePart.setOtherClass( 
						(IRPClassifier) theElapsedTimeActor );

				IRPSysMLPort theActorsElapsedTimePort = 
						(IRPSysMLPort) _context.findNestedElementUnder( 
								(IRPClassifier) theElapsedTimeActor,
								"elapsedTime",
								"SysMLPort",
								true );

				IRPSysMLPort theBlocksElapsedTimePort = 
						(IRPSysMLPort) _context.findNestedElementUnder( 
								(IRPClassifier) theLogicalSystemBlock,
								"elapsedTime",
								"SysMLPort",
								true );

				if( theActorsElapsedTimePort != null &&
						theBlocksElapsedTimePort != null ){

					_context.addConnectorBetweenSysMLPortsIfOneDoesntExist(
							theActorsElapsedTimePort, 
							theElapsedTimePart, 
							theBlocksElapsedTimePort, 
							theLogicalSystemPart );

				} else {
					_context.error("Error in CreateFunctionalBlockPackagePanel.performAction(), unable to find elapsedTime ports") ;
				}

				IRPPanelDiagram thePD = 
						theTestPkg.addPanelDiagram(
								"PD - " + theLogicalSystemBlock.getName() );

				if( _simulationType==SimulationType.FullSim ){

					IRPClass theTesterBlock = 
							theTestPkg.addClass( _testDriverNameTextField.getText() );

					_context.applyExistingStereotype( "TestDriver", theTesterBlock );

					theTesterBlock.changeTo( "Block" );

					// Make the TestDriver a part of the UsageDomain block
					IRPInstance theTestDriverPart = 
							(IRPInstance) theSystemAssemblyBlock.addNewAggr(
									"Part", "" );

					theTestDriverPart.setOtherClass( theTesterBlock );

					//					GeneralHelpers.applyExistingStereotype( "TestDriver", theTestDriverPart );

					for( ActorMappingInfo theInfo : _actorChoices ){

						IRPInstance theActorPart = 
								theInfo.performActorPartCreationIfSelectedIn( 
										theSystemAssemblyBlock,
										theLogicalSystemBlock );

						PortBasedConnector thePortBasedConnector = 
								new PortBasedConnector( theActorPart, theLogicalSystemPart, _context ); 

						thePortBasedConnector.getExistingOrCreateNewProvidedInterfaceOnTargetPort( theInterfacesPkg );
						thePortBasedConnector.getExistingOrCreateNewProvidedInterfaceOnSourcePort( theInterfacesPkg );

					}

					// Connect TestDriver to elapsedTime actor
					IRPPort theElapsedTimePortOnTesterBlock = 
							(IRPPort) theTesterBlock.addNewAggr( "Port", "pElapsedTime" );

					IRPPort theTesterPortOnElapsedTimeActor =
							(IRPPort) theElapsedTimeActor.findNestedElement( "pTester", "Port" );

					if( theTesterPortOnElapsedTimeActor != null &&
							theElapsedTimePart != null ){

						IRPLink theLink = theSystemAssemblyBlock.addLink(
								theElapsedTimePart, 
								theTestDriverPart, 
								null, 
								theTesterPortOnElapsedTimeActor,
								theElapsedTimePortOnTesterBlock );

						theLink.changeTo( "connector" );
					} else {
						_context.error( "Error, either part or port is null" );
					}

					//					for( IRPPackage theUseCasePkg : m_UseCasePkgs ){		
					//						theFunctionalBlockPkg.addDependencyTo( theUseCasePkg );
					//					}

				} else {
					// assume panel diagram simulation will be used (esp. for simple sim)
					_context.applyExistingStereotype("AutoShow", thePD);
				} // end FullSim only				

				// Add a sequence diagram
				SequenceDiagramCreator theHelper = new SequenceDiagramCreator( _context );
				
				theHelper.createSequenceDiagramFor(
						theSystemAssemblyBlock, 
						theRootPkg, 
						"SD - " + theName,
						_context.getIsCreateSDWithAutoShowApplied( theRootPkg ),
						false,
						false );

				IRPStatechartDiagram theStatechart = 
						theLogicalSystemBlock.getStatechart().getStatechartDiagram();

				if( theStatechart != null ){
					theStatechart.highLightElement();
					theStatechart.openDiagram();
				}

				// Add a component
				_context.addAComponentWith( theName, theTestPkg, theSystemAssemblyBlock, "StateOriented" );
			}

			BlockDiagramHelper theHelper = new BlockDiagramHelper(_context);
			
			theHelper.createBDDFor(
					theSystemAssemblyBlock,
					theBlockPkg,
					"BDD - " + theSystemAssemblyBlock.getName(),
					"Block Definition Diagram",
					_excludeMetaClasses );

			theHelper.createIBDFor( 
					theSystemAssemblyBlock, 
					"IBD - " + theSystemAssemblyBlock.getName(),
					"Internal Block Diagram" );

			_context.autoPopulateProjectPackageDiagramIfNeeded();
			
			_context.info( "Package structure construction of " + _context.elInfo( theRootPkg ) + " has completed");
		} else {
			_context.error("Error in CreateFunctionalBlockPackagePanel.performAction, checkValidity returned false");
		}	
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