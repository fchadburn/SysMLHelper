package com.mbsetraining.sysmlhelper.switchanchors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mbsetraining.sysmlhelper.common.BaseContext;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class SwitchAnchorsToDependencies {

	public static void main(String[] args) {
		
		ExecutableMBSE_Context theContext = new ExecutableMBSE_Context( 
				RhapsodyAppServer.getActiveRhapsodyApplication().getApplicationConnectionString() );
		
		SwitchAnchorsToDependencies theSwitcher = new SwitchAnchorsToDependencies( theContext );
		
		theSwitcher.performSwitch( theContext.getSelectedElement( false ), 1 );	
	}

	private BaseContext _context;
	
	public SwitchAnchorsToDependencies(
			BaseContext context ) {
		
		_context = context;
	}
	
	public void performSwitch(
			IRPModelElement underEl,
			int recursive ){
		
		@SuppressWarnings("unchecked")
		List<IRPRequirement> theReqts = underEl.getNestedElementsByMetaClass( 
				"Requirement", recursive ).toList();

		Map<IRPAnnotation,IRPRequirement> theAnnotationsToConvert = new HashMap<>();
		Set<IRPRequirement> theRequirementsWithAnnotations = new HashSet<>();
		
		_context.info( "There are " + theReqts.size() + " requirements under " + _context.elInfo( underEl ) );
				
		for( IRPRequirement theReqt : theReqts ){
			
			_context.info( "Requirement is " + _context.elInfo( theReqt ) );

			List<IRPAnnotation> theAnnotations = getRelatedAnnotations( theReqt );
			
			for( IRPAnnotation theAnnotation : theAnnotations ) {
				_context.info( "Annotation is " + _context.elInfo( theAnnotation ) );	
				theAnnotationsToConvert.put( theAnnotation, theReqt );
				theRequirementsWithAnnotations.add( theReqt );
			}
		}
		
		_context.info( "There are " + theAnnotationsToConvert.size() + " anchors to convert" );
		
		IRPStereotype theTraceStereotype = _context.getStereotypeWith( "trace" );
		
		for( Map.Entry<IRPAnnotation, IRPRequirement> entry : theAnnotationsToConvert.entrySet() ){
			
			IRPAnnotation theAnnotation = entry.getKey();
			IRPRequirement theRequirement = entry.getValue();
			
			IRPDependency theAddedDependency = _context.addStereotypedDependencyIfOneDoesntExist(
					theAnnotation, theRequirement, theTraceStereotype );
			
			if( theAddedDependency != null ) {
				
				_context.info( "Added " + _context.elInfo( theAddedDependency ) + " from " + 
						_context.elInfo( theAnnotation ) + " to " + _context.elInfo( theRequirement ) );
			}
		}
		
		for (IRPRequirement theRequirementWithAnnotations : theRequirementsWithAnnotations) {
			
			List<IRPDiagram> theDiagrams = getRelatedDiagrams( theRequirementWithAnnotations );
			
			for( IRPDiagram theDiagram : theDiagrams ){
				
				IRPCollection theGraphElsCollection = theDiagram.
						getCorrespondingGraphicElements( theRequirementWithAnnotations );
				
				theDiagram.completeRelations( theGraphElsCollection, 1 );
			}
		}
		
		for( Map.Entry<IRPAnnotation, IRPRequirement> entry : theAnnotationsToConvert.entrySet() ){
			
			IRPAnnotation theAnnotation = entry.getKey();
			IRPRequirement theRequirement = entry.getValue();
			
			theAnnotation.removeAnchor( theRequirement );
		}
	}
	
	private List<IRPAnnotation> getRelatedAnnotations(
			IRPRequirement forEl ) {
		
		List<IRPAnnotation> theAnnotations = new ArrayList<>();
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theReferences = forEl.getReferences().toList();
	
		for( IRPModelElement theReference : theReferences ){
			
			if( theReference instanceof IRPAnnotation ) {
				
				IRPAnnotation theAnnotation = (IRPAnnotation)theReference;
				theAnnotations.add( theAnnotation );
			}
		}
		
		return theAnnotations;
	}
	
	private List<IRPDiagram> getRelatedDiagrams(
			IRPModelElement forEl ){
		
		List<IRPDiagram> theDiagrams = new ArrayList<>();
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theReferences = forEl.getReferences().toList();
		
		for( IRPModelElement theReference : theReferences ){
			if( theReference instanceof IRPDiagram ) {
				theDiagrams.add( (IRPDiagram) theReference );
			}
		}
		
		return theDiagrams;
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