package com.mbsetraining.sysmlhelper.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.telelogic.rhapsody.core.*;

public class AnnotationMap extends HashMap<String, List<IRPAnnotation>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8035885110544569045L;
	
	protected IRPModelElement _element;
	protected BaseContext _context;
	
	public AnnotationMap(
			IRPRequirement forEl,
			BaseContext context ) {

		_context = context;
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theReferences = forEl.getReferences().toList();
	
		for( IRPModelElement theReference : theReferences ){
			
			if( theReference instanceof IRPAnnotation ) {
				
				putAnnotationInMapIfNotBlank( (IRPAnnotation) theReference );
				
			} else if( theReference instanceof IRPDependency ) {
				
				IRPDependency theDependency = (IRPDependency)theReference;
				IRPModelElement theDependent = theDependency.getDependent();
				
				if( theDependent instanceof IRPAnnotation ) {
					putAnnotationInMapIfNotBlank( (IRPAnnotation) theDependent );
				}
			}
		}
	}

	private void putAnnotationInMapIfNotBlank(
			IRPAnnotation theAnnotation ){
				
		String theText = theAnnotation.getDescription();
		
		if( theText.isEmpty() ){
			
			_context.info( "Ignoring " + _context.elInfo( theAnnotation ) + " as its Description is empty" );
			
		} else {
			String theUserDefinedMetaclass = theAnnotation.getUserDefinedMetaClass();
			
			List<IRPAnnotation> theAnnotations = this.getOrDefault( theUserDefinedMetaclass, new ArrayList<>() );
			
			//_context.info( _context.elInfo( theReference ) );
			
			if( !theAnnotations.contains( theAnnotation ) ) {
				theAnnotations.add( theAnnotation );
				this.put( theUserDefinedMetaclass, theAnnotations );
			} else {
				_context.warning( "Found that " + _context.elInfo( theAnnotation ) + " has more than one relationship to the same requirement, it's recommended to use trace dependency only" );
			}
		}
	}
	
	public void dumpInfo() {
		
		int count = 0;
		
		for( Entry<String, List<IRPAnnotation>>  nodeId : this.entrySet() ){

			count++;
			List<IRPAnnotation> value = nodeId.getValue();

			String msg = count + " - " + nodeId.getKey() + " has " + value.size() + " annotations";
			_context.info( msg );
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