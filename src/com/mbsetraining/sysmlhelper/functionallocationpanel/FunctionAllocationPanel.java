package com.mbsetraining.sysmlhelper.functionallocationpanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JScrollPane;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSEBasePanel;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.mbsetraining.sysmlhelper.pubsubportcreation.AutoConnectFlowPortsInfo;
import com.telelogic.rhapsody.core.*;

public class FunctionAllocationPanel extends ExecutableMBSEBasePanel {

	private final List<String> _userDefinedMetaClassesForAllocation = 
			Arrays.asList( 
					"Function Usage", 
					"Start Usage", 
					"Final Usage", 
					"Flow Final Usage", 
					"Time Event Usage", 
					"Data Object", 
					"Accept Event Usage", 
					"Parallel Gateway Usage", 
					"Decision Usage" );
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1814353152453871188L;
	
	protected FunctionAllocationMap _radioButtonMap = new FunctionAllocationMap();
	protected IRPClass _selectedClass = null;
	protected List<IRPInstance> _functionUsages;
	protected List<IRPModelElement> _allocateToEls;

	
	public static void main( String[] args ) {
		
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		ExecutableMBSE_Context theContext = new ExecutableMBSE_Context( theRhpApp.getApplicationConnectionString() );
		launchThePanel(  theContext.get_rhpAppID() );
	}
	
	public static void launchThePanel(
			String theAppID ){

		javax.swing.SwingUtilities.invokeLater( new Runnable() {

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );

				UserInterfaceHelper.setLookAndFeel();

				JFrame frame = new JFrame(
						"Functional Allocation" );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				// Cannot pass any Rhapsody elements or context in this
				FunctionAllocationPanel thePanel = 
						new FunctionAllocationPanel( 
								theAppID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	private List<IRPInstance> getSelectedFlowUsages(){
		
		List<IRPInstance> theFlowUsages = new ArrayList<>();
		
		List<IRPGraphNode> theSelectedGraphNodes = _context.getSelectedGraphNodes();

		List<IRPInstance> theCandidates = new ArrayList<>();
		
		for( IRPGraphNode theGraphNode : theSelectedGraphNodes ){
		
			IRPModelElement theModelObject = theGraphNode.getModelObject();
			
			String theMetaClass = theModelObject.getMetaClass();
			
			if( theMetaClass.equals( "Part" ) ) {
				theCandidates.add( (IRPInstance) theModelObject );
			}
		}
		
		if( theCandidates.isEmpty() ) {
			
			IRPModelElement theSelectedEl = _context.getSelectedElement( false );
			
			if( theSelectedEl instanceof IRPStructureDiagram ) {
				theSelectedEl = theSelectedEl.getOwner();
			}
			
			theCandidates = theSelectedEl.getNestedElementsByMetaClass( "Part", 0 ).toList();
		}
		
		for( IRPInstance theCandidate : theCandidates ){
			
			String theUserDefinedMetaClass = theCandidate.getUserDefinedMetaClass();
			
			if( _userDefinedMetaClassesForAllocation.
					contains( theUserDefinedMetaClass ) ) {
				
				theFlowUsages.add( theCandidate );
			}
		}
		
		return theFlowUsages;
	}
	
	private List<IRPModelElement> getAllocateToBlocksFor(
			IRPClass theClass ){
		
		List<IRPModelElement> theAllocateToBlocks = new ArrayList<>();
		
		@SuppressWarnings("unchecked")
		List<IRPInstance> theParts =  theClass.getNestedElementsByMetaClass( "Part", 1 ).toList();
		
		for( IRPInstance thePart : theParts ){
			
			if( thePart.isTypelessObject()==0 ) {
				
				IRPClassifier theOtherClass = thePart.getOtherClass();
				theAllocateToBlocks.add( theOtherClass );
			}
		}
		
		return theAllocateToBlocks;
	}

	public FunctionAllocationPanel(
			String theAppID ){

		super( theAppID );
				
		_functionUsages = getSelectedFlowUsages();
		
		if( _functionUsages.isEmpty() ) {
			
			buildUnableToRunDialog( 
					"Sorry, this helper is unable to run this command because \n" +
							"there are no functional elements is in the selection." );
		} else {

			List<IRPModelElement> theSystemBlocks = 
					_context.findElementsWithMetaClassAndStereotype( 
							"Class", 
							_context.SYSTEM_BLOCK, 
							_context.get_rhpPrj(), 
							1 );
			
			if( theSystemBlocks.isEmpty() ) {
				
				buildUnableToRunDialog( 
						"Sorry, this helper is unable to run this command because \n" +
								"there are no " + _context.SYSTEM_BLOCK + "s in the project . \n " +
						"You need to create an architecture to allocate to first." );

			} else {
				
				_selectedClass = (IRPClass) UserInterfaceHelper.
						launchDialogToSelectElement( 
								theSystemBlocks, 
								"Select the " + _context.SYSTEM_BLOCK + 
								" that represents the architecture you want to allocate functions to", 
								true );		

				if( _selectedClass == null ){

					buildUnableToRunDialog( 
							"Sorry, this helper is unable to run this command because \n" +
									"no " + _context.SYSTEM_BLOCK + " was selected. \n\n " +
							"You need to select a " + _context.SYSTEM_BLOCK + " that owns the parts you want to allocate to." );

				} else {
					
					_allocateToEls = getAllocateToBlocksFor( _selectedClass );
					
					if( _allocateToEls.isEmpty() ) {
						
						buildUnableToRunDialog( 
								"Sorry, this helper is unable to run this command because \n" +
										"no " + _context.SUBSYSTEM_BLOCK + "s were found under " + _context.elInfo( _selectedClass ) + 
										" to allocate to. \n\n " +
								"You need to select a " + _context.SYSTEM_BLOCK + " that owns the parts you want to allocate to." );
					} else {
						
						buildAllocationPanelContent();
					}
				}
			}
		}
	}

	private void buildAllocationPanelContent() {
		
		// Add choice panels for all the usages
		for( IRPInstance theUsage : _functionUsages ){	

			//_context.debug( "theCandidatePart is " + _context.elInfo( theCandidatePart ) );
			
			FunctionAllocationInfo theFlowPortInfo = 
					new FunctionAllocationInfo( 
							theUsage,
							_allocateToEls,
							_context );

			_radioButtonMap.put( 
					theUsage, 
					theFlowPortInfo );
		}
		
		

		setLayout( new BorderLayout( 10, 10 ) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

		Box theBox = Box.createVerticalBox();

		if( _radioButtonMap.isEmpty() ){

			JLabel theLabel = new JLabel( 
					"There are no other parts under the " + _selectedClass.getName() + " block that " + 
					"parts can be allocated to." );
					
			theLabel.setAlignmentX( Component.LEFT_ALIGNMENT );
			theBox.add( theLabel );

		} else {

			JPanel theRadioButtonTable = createMakeChoicesPanel( _radioButtonMap );
			theRadioButtonTable.setAlignmentX( Component.LEFT_ALIGNMENT );

			JScrollPane theScrollPane = new JScrollPane( theRadioButtonTable );

			if( _radioButtonMap.size() > 10 ){
				theScrollPane.setPreferredSize( new Dimension( 450, 311 ) );				
			}

			String theIntroMsg = 
					"This is the intro message'";

			theBox.add( new JLabel( theIntroMsg ) );
			theBox.add( new JLabel( "   " ) );
			theBox.add( new JLabel( "Do you want to auto-connect to attribute(s) in the following and set them to �subscribe�:\n") );
			theBox.add( new JLabel( "   " ) );

			theBox.add( theScrollPane );
		}
		
		add( theBox, BorderLayout.CENTER );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}

	private JPanel createMakeChoicesPanel(
			Map<IRPInstance, FunctionAllocationInfo> theButtonMap ){

		JPanel thePanel = new JPanel();

		GroupLayout theGroupLayout = new GroupLayout( thePanel );
		thePanel.setLayout( theGroupLayout );
		theGroupLayout.setAutoCreateGaps( true );

		SequentialGroup theHorizSequenceGroup = theGroupLayout.createSequentialGroup();
		SequentialGroup theVerticalSequenceGroup = theGroupLayout.createSequentialGroup();

		ParallelGroup theColumn1ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );
		ParallelGroup theColumn2ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );
		ParallelGroup theColumn3ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );

		theHorizSequenceGroup.addGroup( theColumn1ParallelGroup );
		theHorizSequenceGroup.addGroup( theColumn2ParallelGroup );
		theHorizSequenceGroup.addGroup( theColumn3ParallelGroup );

		for (Entry<IRPInstance, FunctionAllocationInfo> entry : theButtonMap.entrySet()){

			FunctionAllocationInfo theValue = entry.getValue();

			JLabel theName = new JLabel( theValue.getUsageName() );
			theName.setMinimumSize( new Dimension( 150, 22 ) );
			theName.setBorder( BorderFactory.createEmptyBorder( 0, 10, 0, 10 ) );

			theValue._statusLabel.setMinimumSize( new Dimension( 150, 22 ) );
			theValue._statusLabel.setBorder( BorderFactory.createEmptyBorder( 0, 0, 0, 10 ) );
			
			theColumn1ParallelGroup.addComponent( theName );  
			theColumn2ParallelGroup.addComponent( theValue._allocationChoicesComboBox );    
			theColumn3ParallelGroup.addComponent( theValue._statusLabel );        

			ParallelGroup theVertical1ParallelGroup = 
					theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );

			theVertical1ParallelGroup.addComponent( theName );
			theVertical1ParallelGroup.addComponent( theValue._allocationChoicesComboBox );
			theVertical1ParallelGroup.addComponent( theValue._statusLabel );

			theVerticalSequenceGroup.addGroup( theVertical1ParallelGroup );
		}

		theGroupLayout.setHorizontalGroup( theHorizSequenceGroup );
		theGroupLayout.setVerticalGroup( theVerticalSequenceGroup );

		return thePanel;
	}

	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled ){

		boolean isValid = true;
		String errorMsg = "";

		if( isMessageEnabled && !isValid && errorMsg != null ){

			UserInterfaceHelper.showWarningDialog( errorMsg );
		}

		return isValid;
	}

	@Override
	protected void performAction() {

		try {
			if( checkValidity( false ) ){

				for( Entry<IRPInstance, FunctionAllocationInfo> entry : _radioButtonMap.entrySet() ){

					FunctionAllocationInfo theInfo = entry.getValue();
					theInfo.performAllocationOfUsages();
				}
			} else {
				_context.error( "AutoConnectFlowPortsPanel.performAction, checkValidity returned false" );
			}	

		} catch( Exception e ){
			_context.error( "AutoConnectFlowPortsPanel.performAction, unhandled exception was detected, e=" + e.getMessage() );
		}

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