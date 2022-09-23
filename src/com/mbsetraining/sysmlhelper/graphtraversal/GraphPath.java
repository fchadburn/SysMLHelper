package com.mbsetraining.sysmlhelper.graphtraversal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mbsetraining.sysmlhelper.common.BaseContext;
import com.telelogic.rhapsody.core.*;

public class GraphPath extends ArrayList<Node>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1010058914906885198L;
	
	BaseContext _context;
	
	public GraphPath(
			BaseContext context ){
		
		_context = context;
	}
	
	public void dumpInfo(){
		
		String msg = "";
		
		if( this.isEmpty() ){
			msg = "Empty graph path";
		} else {
			Iterator<Node> iterator = this.iterator();
			
			while( iterator.hasNext() ){
				
				Node theNode = (Node) iterator.next();
				IRPModelElement theModelEl = theNode._modelEl;
				
				msg += theModelEl.getName() + " (" + theModelEl.getUserDefinedMetaClass() + ")";
				
				if( iterator.hasNext() ){
					msg += " > ";
				} 
			}
		}
		
		_context.info( msg );
	}
	
	public boolean hasBeenVisited(
			IRPModelElement theModelEl ){
		
		boolean hasBeenVisited = false;
		
		for( Node theNode : this ){
			
			if( theNode._modelEl.equals( theModelEl ) ){
				hasBeenVisited = true;
				break;
			}
		}
		
		return hasBeenVisited;
	}
	
	public void createDependencies(
			IRPModelElement fromEl,
			IRPStereotype withTheStereotype ){
		
		if( this.isEmpty() ){
			_context.warning( "Unable to createDependencies as graph path is empty" );
		} else {
			Iterator<Node> iterator = this.iterator();
			
			Node previousNode = null;
			
			while( iterator.hasNext() ){
				
				Node theNode = (Node) iterator.next();			
				
				_context.getExistingOrAddNewDependency( 
						fromEl, theNode._modelEl, withTheStereotype );
								
				if( previousNode != null ){
					
					for( IRPDependency theDependency : previousNode._relations ){	
						
						IRPModelElement theDependsOn = theDependency.getDependsOn();
						
						if( theDependsOn.equals( theNode._modelEl ) ){
							_context.getExistingOrAddNewDependency( 
									fromEl, theDependency, withTheStereotype );
						}
					}
				}
				
				List<IRPModelElement> measuredBys = _context.findElementsWithMetaClassAndStereotype( "Attribute", "MeasuredBy", theNode._modelEl, 0 );
				
				for (IRPModelElement measuredBy : measuredBys) {
					_context.getExistingOrAddNewDependency( 
							fromEl, measuredBy, withTheStereotype );
				}
				
				previousNode = theNode;
			}
		}
	}
	
	String getLastNodeName(){
		
		String theLastNodeName = "";
		
		int size = this.size();
		
		Node lastNode = this.get( size-1 );
		
		theLastNodeName = lastNode._modelEl.getName();
		
		return theLastNodeName;
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