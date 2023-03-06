package com.mbsetraining.sysmlhelper.doorsng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.mbsetraining.sysmlhelper.common.BaseContext;
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
				
				IRPAnnotation theAnnotation = (IRPAnnotation)theReference;
				
				String theText = theAnnotation.getDescription();
				
				if( !theText.isEmpty() ) {
					String theUserDefinedMetaclass = theReference.getUserDefinedMetaClass();
					
					List<IRPAnnotation> theAnnotations = this.getOrDefault( theUserDefinedMetaclass, new ArrayList<>() );
					
					//_context.info( _context.elInfo( theReference ) );
					theAnnotations.add( (IRPAnnotation) theReference );
					this.put( theUserDefinedMetaclass, theAnnotations );
				}
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