package com.mbsetraining.sysmlhelper.allocationpanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class ConnectorAllocationMap extends HashMap<IRPLink, FlowConnectorInfo>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3210872293213448973L;
	protected ExecutableMBSE_Context _context;
	protected FunctionAllocationMap _functionAllocationMap;

	public static void main(String[] args) {
		
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		
		ExecutableMBSE_Context theContext = new ExecutableMBSE_Context( theRhpApp.getApplicationConnectionString() );
		
		FunctionAllocationMap functionAllocationMap = new FunctionAllocationMap( theContext );
		ConnectorAllocationMap theConnectorMap = new ConnectorAllocationMap( theContext );
		
		List<IRPModelElement> theSystemBlocks = 
				theContext.findElementsWithMetaClassAndStereotype( 
						"Class", 
						theContext.SYSTEM_BLOCK, 
						theContext.get_rhpPrj(), 
						1 );
		
		IRPClass theSelectedClass = (IRPClass) UserInterfaceHelper.
				launchDialogToSelectElement( 
						theSystemBlocks, 
						"Select the " + theContext.SYSTEM_BLOCK + 
						" that represents the architecture you want to allocate functions to", 
						true );		

		List<IRPModelElement> allocateToEls = theContext.getClassifiersOfPartsOwnedBy( theSelectedClass );
			
		functionAllocationMap.buildContentWithChoicesFor( allocateToEls );
		theConnectorMap.buildConnectorAllocationMapBasedOn( functionAllocationMap );
	}
	
	public ConnectorAllocationMap(
			ExecutableMBSE_Context context ){
		
		_context = context;
	}
	
	public void buildConnectorAllocationMapBasedOn(
			FunctionAllocationMap functionAllocationMap ) {
		
		_functionAllocationMap = functionAllocationMap;
		
		for( Entry<IRPInstance, FunctionAllocationInfo> entry : _functionAllocationMap.entrySet() ){
			
			IRPInstance theUsage = entry.getKey();
			
			List<IRPLink> theLinks = getLinksToFrom( theUsage );
			
			for( IRPLink theLink : theLinks ){
				addToMapping( theLink );				
			}
		}	
	}

	private void addToMapping(
			IRPLink theLink ){
		
		//_context.info( "addToMapping invoked for " + _context.elInfo( theLink ) );

		if( this.containsKey( theLink ) ){
			
			_context.info( "Skipping " + _context.elInfo( theLink) + " as it is already in connector map" );
		
		} else {
			
			FlowConnectorInfo theFlowConnectorInfo = new FlowConnectorInfo( theLink, _functionAllocationMap, _context);
			
			if( theFlowConnectorInfo.isMappable() ) {
				_context.info( "Adding " + _context.elInfo( theLink ) + " to the mapping" );
				this.put( theLink, theFlowConnectorInfo );
			} else {
				_context.info( "Skipping " + _context.elInfo( theLink ) );
				theFlowConnectorInfo.dumpInfo();
			}
			
		}
	}
	
	public List<IRPLink> getLinksToFrom(
			IRPInstance theInstance ){
		
		//_context.info( "getLinksToFrom invoked for " + _context.elInfo( theInstance) );
		
		List<IRPLink> theLinks = new ArrayList<>();
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theReferences = theInstance.getReferences().toList();
		
		for( IRPModelElement theReference : theReferences ){
		
			if( theReference instanceof IRPLink ) {
				//_context.info( _context.elInfo( theReference ) );
				theLinks.add( (IRPLink) theReference );
			}
		}
		
		return theLinks;
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