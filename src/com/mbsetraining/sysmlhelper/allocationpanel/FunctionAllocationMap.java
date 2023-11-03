package com.mbsetraining.sysmlhelper.allocationpanel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class FunctionAllocationMap extends HashMap<IRPInstance, FunctionAllocationInfo> {

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
	private static final long serialVersionUID = 3298437951727162862L;
	protected ExecutableMBSE_Context _context;
	protected IRPClass _systemRootEl;
	protected List<IRPModelElement> _allocateToEls;

	public FunctionAllocationMap(
			ExecutableMBSE_Context context ){
		
		_context = context;
		List<IRPInstance> theElsToMap = getElementsForMapping();
		
		for( IRPInstance theElToMap : theElsToMap ){
			
			FunctionAllocationInfo theFlowPortInfo = 
					new FunctionAllocationInfo( 
							theElToMap,
							_context );

			this.put( theElToMap, theFlowPortInfo );
		}
	}
	
	public void buildContentWithChoicesFor( 
			IRPClass theRootEl ) {
		
		_systemRootEl = theRootEl;
		
		_allocateToEls = _context.getClassifiersOfPartsOwnedBy( _systemRootEl );
		
		for( Entry<IRPInstance, FunctionAllocationInfo> entry : this.entrySet() ){
			
			FunctionAllocationInfo theInfo = entry.getValue();
			theInfo.buildContentFor( _allocateToEls );
		}
	}

	@SuppressWarnings("unchecked")
	private List<IRPInstance>  getElementsForMapping() {
		
		List<IRPInstance> theElementsForMapping = new ArrayList<>();
		
		List<IRPInstance> theCandidates = getSelectedInstances();
		
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
				
				theElementsForMapping.add( theCandidate );
			}
		}
		
		return theElementsForMapping;
	}

	private List<IRPInstance> getSelectedInstances() {
		
		List<IRPGraphNode> theSelectedGraphNodes = _context.getSelectedGraphNodes();

		List<IRPInstance> theCandidates = new ArrayList<>();
		
		for( IRPGraphNode theGraphNode : theSelectedGraphNodes ){
		
			IRPModelElement theModelObject = theGraphNode.getModelObject();
			
			String theMetaClass = theModelObject.getMetaClass();
			
			if( theMetaClass.equals( "Part" ) ) {
				theCandidates.add( (IRPInstance) theModelObject );
			}
		}
		return theCandidates;
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