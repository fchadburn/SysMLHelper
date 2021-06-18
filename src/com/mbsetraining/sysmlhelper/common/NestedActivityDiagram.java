package com.mbsetraining.sysmlhelper.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import com.telelogic.rhapsody.core.*;
 
public class NestedActivityDiagram {

	protected final static String m_Prefix = "AD - ";
	protected ConfigurationSettings _context;
	
	public NestedActivityDiagram(
			ConfigurationSettings context ) {
		
		_context = context;
	}
	
	public void renameNestedActivityDiagramsFor(
			List<IRPModelElement> theSelectedEls){
		
		Map<IRPFlowchart, String> theNewNameMappings = new HashMap<IRPFlowchart, String>(); 
		
		List<IRPActivityDiagram> theADs = 
				_context.buildListOfActivityDiagramsFor( theSelectedEls );
		
		_context.info( "There are " + theADs.size() + 
				" Activity Diagrams nested under the selected list" );

		for( IRPActivityDiagram theAD : theADs ){
			
			IRPModelElement theOwner = theAD.getOwner();
			IRPModelElement theOwnersOwner = theOwner.getOwner();
			
			if( theOwner instanceof IRPFlowchart && 
				theOwnersOwner instanceof IRPUseCase ){
				
				String theUseCaseName = theOwnersOwner.getName();
				String thePreferredName = m_Prefix + theUseCaseName;
				
				if( theOwner.getName().equals( thePreferredName ) ){
					_context.info( "Determined that " + _context.elInfo( theOwner ) + 
							" already matches " + _context.elInfo( theOwnersOwner ) );
				} else {
					theNewNameMappings.put( (IRPFlowchart) theOwner, thePreferredName );	
				}
			}
		}
		
		if( theNewNameMappings.isEmpty() ){
	
			UserInterfaceHelper.showInformationDialog( 
					"Nothing to do. The checker has determined that the " + 
					theADs.size() + " activity diagrams are correctly named." );
			
		} else {
			String theMsg = "The checker has determined that " + theNewNameMappings.size() + 
					" of the " + theADs.size() + " activity diagrams require " +
					"renaming to match the use cases:\n";
			
			int count = 0;
			
			for( Map.Entry<IRPFlowchart, String> entry : theNewNameMappings.entrySet() ){
			
				count++;
				
				if( count > 10 ){
					theMsg = theMsg + "...\n";
				} else {
					theMsg = theMsg + _context.elInfo(entry.getKey()) + "\n";
				}
			}
			
			theMsg = theMsg + "\nDo you want to rename them?";

			int response = JOptionPane.showConfirmDialog(
					null, 
					theMsg, 
					"Update activity diagram names",
					JOptionPane.YES_NO_CANCEL_OPTION, 
					JOptionPane.QUESTION_MESSAGE);

			if (response == JOptionPane.CANCEL_OPTION){
				_context.debug("Operation was cancelled by user with no changes made.");
			} else {
				if (response == JOptionPane.YES_OPTION) {

					for (Map.Entry<IRPFlowchart, String> entry : theNewNameMappings.entrySet()){
						
						IRPFlowchart theAD = (IRPFlowchart)entry.getKey();
					    
					    try {
					    	theAD.setName( entry.getValue() );		
						    _context.debug("Renamed " + _context.elInfo( theAD ) + " to " + entry.getValue() );
						    
						} catch (Exception e) {
							_context.debug("Error: Unable to rename " + _context.elInfo( theAD ) + " to " + entry.getValue());
						}			    
					}

				} else if (response == JOptionPane.NO_OPTION){
					_context.debug("Info: User chose not rename the actions.");
				} 
			}
		}
	}
	
	public void createNestedActivityDiagramsFor(
			List<IRPModelElement> theElements ){
		 
		for( IRPModelElement theElement : theElements ){
			
			if( theElement instanceof IRPUseCase ){
				
				_context.info( "Creating a nested Activity Diagram underneath " + 
						_context.elInfo( theElement ) );
				
				createNestedActivityDiagram( 
						(IRPUseCase)theElement, 
						m_Prefix + theElement.getName(), 
						"ExecutableMBSEProfile.RequirementsAnalysis.TemplateForActivityDiagram" );
			} 
		}
	}
	
	public void createNestedActivityDiagram(
			IRPClassifier forClassifier, 
			String withUnadornedName,
			String basedOnPropertyKey ){
		
		String theName = withUnadornedName;
		 
		// check if existing AD with same name
		IRPFlowchart theAD = (IRPFlowchart) forClassifier.findNestedElement( 
				theName, 
				"ActivityDiagram" );
		
		int count = 0;
		
		while (theAD != null){
			
			_context.warning( _context.elInfo( forClassifier ) + " already has a nested activity diagram called " + theName );
			count++;
			theName = withUnadornedName + " " + count;
			theAD = (IRPFlowchart) forClassifier.findNestedElement( theName , "ActivityDiagram" );
		}
		
		IRPModelElement theTemplate = null;
		
		try {
			theTemplate = _context.getTemplateForActivityDiagram( forClassifier, basedOnPropertyKey );
			
		} catch (Exception e) {
			_context.error("Exception trying to find template based on property " + basedOnPropertyKey);
		}
		
		IRPFlowchart theFlowchart = null;
		
		if( theTemplate != null ){
			
			try {
				theFlowchart = (IRPFlowchart) theTemplate.clone( theName, forClassifier );
				_context.debug( "the cloned flowchart is " + _context.elInfo( theFlowchart ) );

			} catch (Exception e) {
				_context.debug("Exception while cloning");
			}

		} else {
			_context.debug("Warning, Could not find template so creating fresh AD");
			theFlowchart = forClassifier.addActivityDiagram();
			theFlowchart.setName( theName );
			
			IRPStatechartDiagram theStatechart = theFlowchart.getStatechartDiagram();

			try {
				theStatechart.createGraphics();

			} catch (Exception e) {
				_context.debug("Exception creating graphics");
			}
		}
		
		if( theFlowchart != null ){
			
			theFlowchart.setIsAnalysisOnly( 1 ); // so that call op right-click parameter sync menus appear
			IRPStatechartDiagram theStatechart = theFlowchart.getStatechartDiagram();
			theStatechart.highLightElement();
			theFlowchart.setAsMainBehavior();
		}
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

