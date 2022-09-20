package com.mbsetraining.sysmlhelper.graphtraversal;

import java.util.ArrayList;
import java.util.Iterator;

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
			IRPModelElement fromEl ){
		
		if( this.isEmpty() ){
			_context.warning( "Unable to createDependencies as graph path is empty" );
		} else {
			Iterator<Node> iterator = this.iterator();
			
			while( iterator.hasNext() ){
				
				Node theNode = (Node) iterator.next();
				IRPModelElement theModelEl = theNode._modelEl;
				
				_context.info( "Adding dependency from " + fromEl.getName() + " to " + 
						theModelEl.getName() + " (" + theModelEl.getUserDefinedMetaClass() + ")" );

				fromEl.addDependencyTo( theModelEl ); 
			}
		}
	}
}
