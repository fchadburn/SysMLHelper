package com.mbsetraining.sysmlhelper.graphtraversal;

import java.util.ArrayList;
import java.util.List;

import com.mbsetraining.sysmlhelper.common.BaseContext;
import com.telelogic.rhapsody.core.*;

public class Node {

	protected List<Node> _neighbors;	
	protected List<IRPGraphNode> _graphNodes;
	protected IRPModelElement _modelEl;
	protected List<IRPDependency> _relations;
	protected BaseContext _context;

	public IRPModelElement get_modelEl() {
		return _modelEl;
	}

	/*
	public Node(
			IRPGraphNode graphNode, 
			BaseContext context) {

		_graphNodes = new ArrayList<>();

		this._graphNodes.add( graphNode );
		this._context = context;
		this._modelEl = graphNode.getModelObject();
		this._neighbors = new ArrayList<>();	

		_context.info( "Created " + toString() );
	}*/

	public Node(
			IRPModelElement modelEl, 
			BaseContext context) {

		this._context = context;
		this._modelEl = modelEl;
		this._neighbors = new ArrayList<>();
		this._relations = new ArrayList<>();

		_graphNodes = new ArrayList<>();	

		_context.info( "Created " + toString() );
	}

	public void addEdge(
			IRPDependency relation,
			Node to ){

		_relations.add( relation );
		_neighbors.add( to );	
		_context.info( "addEdge to " + to.toString() + " invoked for " + toString() );
	}

	public boolean isValid(){
		return _modelEl != null;
	}

	public void dumpInfo(){
		_context.info( toString() );
	}

	@Override
	public String toString(){

		String theString = "Node for " + _context.elInfo( _modelEl ) + 
				" tied to " + _neighbors.size() + " neighbours";

		return theString;

	}

	/*
	public List<IRPModelElement> determineDependsOnElements(){

		List<IRPModelElement> theDependsOnEls = new ArrayList<>();

		IRPModelElement theModelEl = this._modelEl;

		@SuppressWarnings("unchecked")
		List<IRPDependency> theDependencies = theModelEl.getDependencies().toList();

		for( IRPDependency theDependency : theDependencies ){

			IRPModelElement theTarget = theDependency.getDependsOn();

			if( theTarget instanceof IRPModelElement ){

				theDependsOnEls.add( theTarget );
			}
		}	

		return theDependsOnEls;
	}*/
	
	public List<IRPDependency> determineEdgeElements(){

		List<IRPDependency> theEdgeElements = new ArrayList<>();

		IRPModelElement theModelEl = this._modelEl;

		@SuppressWarnings("unchecked")
		List<IRPDependency> theDependencies = theModelEl.getDependencies().toList();

		for( IRPDependency theDependency : theDependencies ){

			IRPModelElement theTarget = theDependency.getDependsOn();

			if( theTarget instanceof IRPModelElement ){

				theEdgeElements.add( theDependency );
			}
		}	

		return theEdgeElements;
	}

	public void buildRecursively(
			GraphPath currentPath,
			GraphPaths allPaths ){

		IRPModelElement theModelEl = this._modelEl;

		_context.info( "buildRecursively invoked for " + _context.elInfo( theModelEl ) );

		currentPath.add( this );

		List<IRPDependency> theEdgeElements = determineEdgeElements();

		if( theEdgeElements.isEmpty() ){

			_context.info( "Stopping at " + toString() );

			GraphPath newPath = new GraphPath( _context );
			
			for (Node node : currentPath) {
				newPath.add( node );
			}

			allPaths.add( newPath );
						
			currentPath.dumpInfo();
			currentPath.remove( this );

		} else {
					
			for( IRPDependency theEdgeElement : theEdgeElements ){

				IRPModelElement theDependsOnEl = theEdgeElement.getDependsOn();

				if( theDependsOnEl instanceof IRPModelElement ){

					if( !currentPath.hasBeenVisited( theDependsOnEl ) ){

						Node theTargetNode = new Node( theDependsOnEl, _context );
						this.addEdge( theEdgeElement, theTargetNode );      

						theTargetNode.buildRecursively( currentPath, allPaths );
					}
				}
			}
		}
	}
	
	public void addDependenciesFrom( 
			IRPModelElement fromEl ){
				
		_context.info( "Adding dependency from " + fromEl.getName() + " to " + 
				_modelEl.getName() + " (" + _modelEl.getUserDefinedMetaClass() + ")" );

		fromEl.addDependencyTo( _modelEl ); 
		
		for( IRPDependency relationEl : _relations ){
			
			_context.info( "Adding dependency from " + fromEl.getName() + " to " + 
					relationEl.getName() + " (" + relationEl.getUserDefinedMetaClass() + ")" );

			fromEl.addDependencyTo( relationEl ); 
		}
	}
}
