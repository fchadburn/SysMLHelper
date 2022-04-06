package com.mbsetraining.sysmlhelper.doorsng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mbsetraining.sysmlhelper.common.BaseContext;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class RepairLinks {

	BaseContext _context;
	
	String [] _baseDiagramMetaClaases =new String [] {"ObjectModelDiagram","UseCaseDiagram","ActivityDiagramGE"}; 
	
	public static void main(String[] args) {
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();

		BaseContext theContext = new ExecutableMBSE_Context(theRhpApp.getApplicationConnectionString());
		RepairLinks theRepairer = new RepairLinks( theContext );
		IRPModelElement theSelectedEl = theContext.getSelectedElement( false );
		theRepairer.repairAllDiagrams( theSelectedEl );
	}

	public RepairLinks(
			BaseContext context ) {
		_context = context;
	}

	public List<IRPGraphEdge> getGraphEdgesFor( IRPDiagram theDiagram ){

		List<IRPGraphEdge> theGraphEdges = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theGraphEls = theDiagram.getGraphicalElements().toList();

		for (IRPGraphElement theGraphEl : theGraphEls) {

			if( theGraphEl instanceof IRPGraphEdge ){
				theGraphEdges.add( (IRPGraphEdge) theGraphEl );
			}
		}

		return theGraphEdges;
	}

	@SuppressWarnings("unchecked")
	public void repairAllDiagrams( 
			IRPModelElement underRootEl ){
				
		Map<IRPDiagram, List<LinkInfo>> theFixMap = new HashMap<>();

		for( String diagramType : _baseDiagramMetaClaases ){
			
			List<IRPDiagram> theDiagrams = new ArrayList<>();
			
			if( underRootEl instanceof IRPDiagram ){
				theDiagrams.add( (IRPDiagram) underRootEl );
			} else {
				theDiagrams = underRootEl.getNestedElementsByMetaClass( diagramType, 1 ).toList();
			}

			for( IRPDiagram theDiagram : theDiagrams ){
								
				List<LinkInfo> theLinkInfo = getRepairableLinksFrom( theDiagram );
				theFixMap.put( theDiagram, theLinkInfo );	
			}
		}
		
		int diagramCount = 0;
		int fixCount = 0;
		
		for( Entry<IRPDiagram, List<LinkInfo>>  entryForId : theFixMap.entrySet() ){
			
			List<LinkInfo> value = entryForId.getValue();	
			diagramCount++;
			fixCount += value.size();
		}
		
		if( fixCount == 0 ){
			UserInterfaceHelper.showInformationDialog( 
					"No links were found that need repairing in " + diagramCount + " diagrams under " + _context.elInfo( underRootEl ) );
		} else {
			boolean answer = UserInterfaceHelper.askAQuestion( 
					"Found " + fixCount + " links that seem to be broken in " + diagramCount + 
					" diagrams under " + _context.elInfo( underRootEl ) +  ".\n" +
					"Do you want to attempt to fix?" );
			
			if( answer==true ){
				for( Entry<IRPDiagram, List<LinkInfo>>  entryForId : theFixMap.entrySet() ){

					IRPDiagram key = entryForId.getKey();
					List<LinkInfo> value = entryForId.getValue();
					
					if( value.isEmpty() ){
					
						_context.info( "No fixes were needed for " + _context.elInfo( key ) + 
								" owned by " + _context.elInfo( key.getOwner() ) );
					} else {
						_context.info( value.size() + " fixes needed for " + _context.elInfo( key ) + 
								" owned by " + _context.elInfo( key.getOwner() ) );
					}
					
					for (LinkInfo linkInfo : value) {
						
						_context.info( "Fixing " + linkInfo.getInfo() + " on " + _context.elInfo( key ) );				
						linkInfo.createLink();
					}
				}
			}
		}
	}
	
	public List<LinkInfo> getRepairableLinksFrom(
			IRPDiagram theDiagram ){

		List<LinkInfo> theRepairableLinks = new ArrayList<>();
		
		List<IRPGraphEdge> theGraphEdges = getGraphEdgesFor( theDiagram );

		for( IRPGraphEdge theGraphEdge : theGraphEdges ) {

			IRPModelElement theModelObject = theGraphEdge.getModelObject();

			if( theModelObject == null ){

				IRPGraphElement theSourceGraphEl = theGraphEdge.getSource();
				IRPGraphElement theTargetGraphEl = theGraphEdge.getTarget();

				IRPModelElement theSourceEl = theSourceGraphEl.getModelObject();
				IRPModelElement theTargetEl = theTargetGraphEl.getModelObject();

				if( theSourceEl != null ){

					if( theTargetEl == null ){

						_context.info( "Unresolved edge from " + _context.elInfo( theSourceEl ) + 
								" to an element that doesn't exist. Have you loaded the requirements?" );

					} else if( theTargetEl instanceof IRPRequirement ){

						IRPRequirement theTargetReqt = (IRPRequirement)theTargetEl;

						String theRemoteURI = theTargetReqt.getRemoteURI();

						if( theRemoteURI != null ){

							LinkInfo theLinkInfo = new LinkInfo(theSourceEl, theTargetReqt, "Satisfaction", _context );
							theRepairableLinks.add( theLinkInfo );
						}
					}
				}
			}
		}
		
		return theRepairableLinks;
	}
}

/**
 * Copyright (C) 2022  MBSE Training and Consulting Limited (www.executablembse.com)

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