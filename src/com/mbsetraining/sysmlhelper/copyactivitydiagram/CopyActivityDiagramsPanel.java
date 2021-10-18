package com.mbsetraining.sysmlhelper.copyactivitydiagram;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JScrollPane;

import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSEBasePanel;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class CopyActivityDiagramsPanel extends ExecutableMBSEBasePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5634296009352088660L;
	
	private Map<IRPUseCase, CopyActivityDiagramsInfo> m_RadioButtonMap = new HashMap<IRPUseCase, CopyActivityDiagramsInfo>();
	private IRPModelElement m_ToElement = null;
	//private IRPModelElement m_UnderneathTheEl = null;
	private JCheckBox m_ApplyMoreDetailedADCheckBox; 
	private JCheckBox m_CopyAllCheckBox;
	private JCheckBox m_OpenDiagramsCheckBox;

	public static void main(String[] args) {
		
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		launchThePanel( theRhpApp.getApplicationConnectionString() );
	}
	
	public static void launchThePanel(
			String theAppID ){
				
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				
				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame( "Copy Activity Diagams" );
				
				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CopyActivityDiagramsPanel thePanel = 
						new CopyActivityDiagramsPanel( theAppID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}
	
	private IRPPackage getToPackage( 
			IRPModelElement fromSelectedEl ){
		
		IRPPackage thePkg = null;
		
		if( fromSelectedEl instanceof IRPPackage &&
				_context.hasStereotypeCalled( 
						((ExecutableMBSE_Context) _context).getUseCasePackageWorkingStereotype( fromSelectedEl ), 
						fromSelectedEl )){
			
			thePkg = (IRPPackage) fromSelectedEl;
			
		} else if( fromSelectedEl instanceof IRPPackage && 
				_context.hasStereotypeCalled( 
						((ExecutableMBSE_Context) _context).getSimulationPackageStereotype( fromSelectedEl ),
						fromSelectedEl )){
			
			List<IRPModelElement> theWorkingPkgs = 
					_context.findElementsWithMetaClassAndStereotype(
							"Package", 
							((ExecutableMBSE_Context) _context).getUseCasePackageWorkingStereotype( fromSelectedEl ), 
							fromSelectedEl, 
							0 ); // not recursive
			
			if( theWorkingPkgs.size() == 1 ){
				thePkg  = (IRPPackage) theWorkingPkgs.get( 0 );
			}
		}
		
		if( thePkg == null ){
			
			@SuppressWarnings("unchecked")
			List<IRPPackage> theNestedPkgs = 
					fromSelectedEl.getProject().getNestedElementsByMetaClass(
							"Package", 1 ).toList();

			for( IRPPackage theNestedPkg : theNestedPkgs ){

				List<IRPModelElement> theDependencies = 
						_context.findElementsWithMetaClassAndStereotype(
								"Dependency", "AppliedProfile", theNestedPkg, 0 );

				for (IRPModelElement theDependencyElement : theDependencies) {

					IRPDependency theDependency = (IRPDependency)theDependencyElement;
					IRPModelElement theDependsOn = theDependency.getDependsOn();

					if (theDependsOn.getName().equals("RequirementsAnalysisProfile")){
						IRPModelElement theDependent = theDependency.getDependent();
						thePkg = (IRPPackage) theDependent;
						break;
					}
				}
			}
			
			if( thePkg == null ){
				_context.warning( "Unable to find working package based on " + 
						_context.elInfo( fromSelectedEl ) );
			}
		}
		
		return thePkg;
	}
	
	@SuppressWarnings("unchecked")
	public CopyActivityDiagramsPanel(
			String appID ) {
		
		super( appID );
				
		IRPModelElement theSelectedEl = _context.getSelectedElement();
		
		Set<IRPPackage> thePullFromPkgs = 
				((ExecutableMBSE_Context) _context).getPullFromPackage( 
						theSelectedEl );
		
		List<IRPUseCase> theUseCases = new ArrayList<IRPUseCase>();
		
		for( IRPPackage thePullFromPkg : thePullFromPkgs ){
			theUseCases.addAll( 
					thePullFromPkg.getNestedElementsByMetaClass(
							"UseCase", 1 ).toList() );
		}
		
		m_ToElement = getToPackage( theSelectedEl );
		
		for (IRPUseCase theUseCase : theUseCases) {	
			m_RadioButtonMap.put( theUseCase, new CopyActivityDiagramsInfo( theUseCase ) );
		}

		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		
		Box theBox = Box.createVerticalBox();

		if( theUseCases.isEmpty() ){
			JLabel theLabel = new JLabel("There are no use cases");
			theLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			theBox.add( theLabel );
		} else {
			m_CopyAllCheckBox = new JCheckBox("Copy and link to existing upstream activity diagrams");
			m_CopyAllCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
			m_CopyAllCheckBox.setSelected(true);
			
			m_CopyAllCheckBox.addActionListener( new ActionListener() {
				
				public void actionPerformed(ActionEvent actionEvent) {

					AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();

					boolean selected = abstractButton.getModel().isSelected();

					for (Entry<IRPUseCase, CopyActivityDiagramsInfo> entry : m_RadioButtonMap.entrySet()){

						CopyActivityDiagramsInfo theValue = entry.getValue();

						if (!selected){
							theValue.getDoNothingButton().setSelected(true);
						} else {
							if (theValue.hasActivityDiagrams()){
								theValue.getCopyExistingButton().setSelected(true);
							}
						}
					}

				}} );
			
			theBox.add( m_CopyAllCheckBox );

			JPanel theRadioButtonTable = createCopyADChoicesPanel();
			theRadioButtonTable.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			JScrollPane theScrollPane = new JScrollPane( theRadioButtonTable );
			
			if( m_RadioButtonMap.size() > 10 ){
				theScrollPane.setPreferredSize( new Dimension( 450, 311 ) );				
			}
			
			theBox.add( theScrollPane );
			
		}
		
		m_OpenDiagramsCheckBox = new JCheckBox("Open the copied/newly created activity diagrams?");
		m_OpenDiagramsCheckBox.setSelected( true );
		m_OpenDiagramsCheckBox.setVisible( true );
		
		theBox.add( m_OpenDiagramsCheckBox );
		
		boolean isConvertToDetailedADOptionEnabled = 
				((ExecutableMBSE_Context) _context).getIsConvertToDetailedADOptionEnabled(
						m_ToElement.getProject() );
		
		boolean isConvertToDetailedADOptionWantedByDefault = 
				((ExecutableMBSE_Context) _context).getIsConvertToDetailedADOptionWantedByDefault(
						m_ToElement.getProject() );
		
		m_ApplyMoreDetailedADCheckBox = new JCheckBox("Switch toolbars and formatting to more detailed AD ready for conversion?");
		m_ApplyMoreDetailedADCheckBox.setSelected( isConvertToDetailedADOptionWantedByDefault );
		m_ApplyMoreDetailedADCheckBox.setVisible( isConvertToDetailedADOptionEnabled );
		
		if( !m_RadioButtonMap.isEmpty() ){
			theBox.add( m_ApplyMoreDetailedADCheckBox );			
		}

		add( theBox, BorderLayout.CENTER );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}
	
	@Override
	protected boolean checkValidity(boolean isMessageEnabled) {
		return true;
	}

	@Override
	protected void performAction() {
		
		try {
			if( checkValidity( false ) ){
				
				int clonedCount = 0;
				int createdCount = 0;
				
				for (Entry<IRPUseCase, CopyActivityDiagramsInfo> entry : m_RadioButtonMap.entrySet()){
					
					IRPUseCase theUseCase = entry.getKey();
					CopyActivityDiagramsInfo theValue = entry.getValue();
				    
				    if( theValue.getCopyExistingButton().isSelected() ){
				    	
				    	List<IRPFlowchart> theFlowcharts = theValue.getFlowcharts();
				    	
			    		_context.debug("Copying " + theFlowcharts.size() + " nested ADs for " + 
			    				_context.elInfo(theUseCase) );

				    	for( IRPFlowchart theFlowchart : theFlowcharts ){
				    		
				    		IRPFlowchart theNewFlowchart = cloneTheFlowchart( 
				    				m_ToElement, 
				    				theFlowchart, 
				    				m_ApplyMoreDetailedADCheckBox.isSelected() );

				    		if( m_OpenDiagramsCheckBox.isSelected() ){
				    			theNewFlowchart.highLightElement();			   
				    		}
				    		
				    		clonedCount++;
						}    
				    	
				    } else if( theValue.getCreateNewButton().isSelected() ){
				    					    	
			    		_context.debug( "Create new ADs for " + 
			    				_context.elInfo( theUseCase ) );

			    		String theUniqueName = _context.determineUniqueNameBasedOn(
			    				"Working - AD - " + theUseCase.getName(), "ActivityDiagram", m_ToElement );
			    		
			    		_context.debug("Creating new AD on " + 
			    				_context.elInfo( m_ToElement ) + " with unique name " + theUniqueName );
			    		
			    		IRPFlowchart theNewFlowchart = 
			    				(IRPFlowchart) m_ToElement.addNewAggr( "ActivityDiagram", theUniqueName );
			    		
			    		IRPDependency theDependency = theNewFlowchart.addDependencyTo( theUseCase );
			    		theDependency.changeTo( "Refinement" );
			    		
			    		_context.debug( "A " + _context.elInfo( theDependency ) + " from " + _context.elInfo( theNewFlowchart ) + 
			    				" to " + _context.elInfo( theUseCase ) + " has been added to the model." );
			    		
			    		AddNoteTo( theNewFlowchart );

			    		if( m_ApplyMoreDetailedADCheckBox.isSelected() ){
			    			theNewFlowchart.changeTo("ActivityDiagram");
			    		} else {
			    			theNewFlowchart.changeTo("Textual Activity Diagram");
			    		}
			    		
			    		createdCount++;

			    		if( m_OpenDiagramsCheckBox.isSelected() ){
			    			theNewFlowchart.highLightElement();			   
			    		}	
				    }
				}	
				
			    if( clonedCount > 0 || createdCount > 0 ){
			    	
			    	JDialog.setDefaultLookAndFeelDecorated(true);
					
					JOptionPane.showMessageDialog(
							null,  
				    		"Finished. " + clonedCount + " diagram(s) were copied " + 
				    		"and " + createdCount + " new diagram(s) created in Package \ncalled " + m_ToElement.getFullPathName(),
				    		"Information",
				    		JOptionPane.INFORMATION_MESSAGE);
					
					m_ToElement.highLightElement();
			    }
			} else {
				_context.error( "Error in CreateNewActorPanel.performAction, checkValidity returned false" );
			}	
		} catch (Exception e) {
			_context.error( "Error in CopyActivityDiagramsPanel.performAction, unhandled exception was detected, e=" + e.getMessage() );
		}
	}
	
	private JPanel createCopyADChoicesPanel(){
		
		JPanel thePanel = new JPanel();

		GroupLayout theGroupLayout = new GroupLayout( thePanel );
		thePanel.setLayout( theGroupLayout );
		theGroupLayout.setAutoCreateGaps( true );

		SequentialGroup theHorizSequenceGroup = theGroupLayout.createSequentialGroup();
		SequentialGroup theVerticalSequenceGroup = theGroupLayout.createSequentialGroup();

		ParallelGroup theColumn1ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );
		ParallelGroup theColumn2ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );
		ParallelGroup theColumn3ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );
		ParallelGroup theColumn4ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );

		theHorizSequenceGroup.addGroup( theColumn1ParallelGroup );
		theHorizSequenceGroup.addGroup( theColumn2ParallelGroup );
		theHorizSequenceGroup.addGroup( theColumn3ParallelGroup );
		theHorizSequenceGroup.addGroup( theColumn4ParallelGroup );
		
		for (Entry<IRPUseCase, CopyActivityDiagramsInfo> entry : m_RadioButtonMap.entrySet()){
		    
			IRPUseCase theUseCase = entry.getKey();
			CopyActivityDiagramsInfo theValue = entry.getValue();

			JLabel theName = new JLabel( theUseCase.getName()) ;
			theName.setMinimumSize( new Dimension( 150, 22 ) );
			theName.setBorder( BorderFactory.createEmptyBorder(0, 10, 0, 10));

			
			theColumn1ParallelGroup.addComponent( theName );   
			theColumn2ParallelGroup.addComponent( theValue.getCopyExistingButton() );    
			theColumn3ParallelGroup.addComponent( theValue.getCreateNewButton() );    
			theColumn4ParallelGroup.addComponent( theValue.getDoNothingButton() );    

			ParallelGroup theVertical1ParallelGroup = 
					theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );

			theVertical1ParallelGroup.addComponent( theName );
			theVertical1ParallelGroup.addComponent( theValue.getCopyExistingButton() );
			theVertical1ParallelGroup.addComponent( theValue.getCreateNewButton() );
			theVertical1ParallelGroup.addComponent( theValue.getDoNothingButton() );
			
			theVerticalSequenceGroup.addGroup( theVertical1ParallelGroup );		  
		}

		theGroupLayout.setHorizontalGroup( theHorizSequenceGroup );
		theGroupLayout.setVerticalGroup( theVerticalSequenceGroup );

	    return thePanel;

	}
	
	private IRPFlowchart cloneTheFlowchart(
			IRPModelElement toNewOwner,
			IRPFlowchart theFlowchart,
			boolean isSwitchToDetailedAD ) {
		
		String theUniqueName = _context.determineUniqueNameBasedOn(
				"Working - " + theFlowchart.getName(), "ActivityDiagram", toNewOwner);
		
		_context.debug("Cloned " + _context.elInfo(theFlowchart) + " to " + 
				_context.elInfo(toNewOwner) + " with unique name " + theUniqueName);
		
		IRPFlowchart theNewFlowchart = 
				(IRPFlowchart) theFlowchart.clone( theUniqueName, toNewOwner );
		
		IRPDependency theDependency = theNewFlowchart.addDependencyTo( theFlowchart );
		theDependency.changeTo("Refinement");
		
		theNewFlowchart.setIsAnalysisOnly( 1 ); // to allow call op parameter sync-ing
		
		_context.debug( _context.elInfo( theDependency ) + " was added between " + 
				_context.elInfo( theNewFlowchart ) + " and " + _context.elInfo( theFlowchart ) );
		
		AddNoteTo( theNewFlowchart );
		
		if( isSwitchToDetailedAD ){	
			theNewFlowchart.changeTo("ActivityDiagram");
		}
		
		return theNewFlowchart;
	}

	private void AddNoteTo(IRPFlowchart theNewFlowchart) {
		IRPGraphNode theNote = theNewFlowchart.addNewNodeByType("Note", 20, 44, 120, 70);
		
		theNote.setGraphicalProperty(
				"Text", 
				"This working copy of the use case steps can be used to generate operations, events & attributes \n" 
				+ "(e.g. for state-machine/iteraction model).");
		
		theNote.setGraphicalProperty(
				"BackgroundColor",
				"255,0,0" ); // red
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