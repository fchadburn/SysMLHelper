package com.mbsetraining.sysmlhelper.taumigratorplugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.mbsetraining.sysmlhelper.common.GraphNodeInfo;
import com.telelogic.rhapsody.core.*;

public abstract class RhpEl {
	
	protected String _elementName;
	protected String _elementType;
	protected String _elementGuid;
	protected TauMigrator_Context _context;
	protected IRPModelElement _rhpEl;

	public IRPModelElement get_rhpEl() {
		return _rhpEl;
	}

	public abstract IRPModelElement createRhpEl(
			RhpEl treeRoot ) throws Exception;

	public String get_elementGuid() {
		return _elementGuid;
	}

	protected List<RhpEl> children = new ArrayList<RhpEl>();
	protected RhpEl parent = null;

	public String getM_ElementName() {
		return _elementName;
	}

	public String getM_ElementType() {
		return _elementType;
	}

	public IRPInstance getM_Instance() {
		return m_Instance;
	}

	public IRPClass getM_Class() {
		return m_Class;
	}

	private IRPInstance m_Instance = null;
	private IRPClass m_Class = null;

	public RhpEl(
			String theElementName,
			String theElementType,
			String theElementGuid,
			TauMigrator_Context context){

		_context = context;
		_elementName = theElementName;
		_elementType = theElementType;
		_elementGuid = theElementGuid;
	}

	public RhpEl(
			String theElementName,
			String theElementType,
			String theElementGuid,
			RhpEl parent,
			TauMigrator_Context context ){

		_context = context;
		_elementName = theElementName;
		_elementType = theElementType;
		_elementGuid = theElementGuid;

		this.parent= parent;
	}

	public List<RhpEl> getChildren() {
		return children;
	}

	public void setParent(
			RhpEl parent ){
		this.parent = parent;
	}

	public RhpEl getParent(){
		return this.parent;
	}
	
	public void addChild(RhpEl child) {

		child.setParent(this);
		this.children.add(child);
	}

	public boolean isRoot() {
		return (this.parent == null);
	}

	public boolean isLeaf() {
		if(this.children.size() == 0) 
			return true;
		else 
			return false;
	}

	public void removeParent() {
		this.parent = null;
	}

	public String getString(){
		return _elementType + " called " + _elementName + " (with Guid=" + _elementGuid + ")";
	}

	public void dump(){

		String theMsg = getString() + " has " + children.size() + " child nodes ";

		if( children.size() > 0 ){

			Iterator<RhpEl> iterator = children.iterator();

			// while loop
			while( iterator.hasNext() ) {
				theMsg += iterator.next().getString();

				if( iterator.hasNext() ){
					theMsg += ", ";
				}
			}

			theMsg += ")";
		}

		_context.info( theMsg );
	}

	public int getNodeCount(){

		int theNodeCount = 1;

		for( RhpEl softwareNode : children ){
			theNodeCount += softwareNode.getNodeCount();
		}

		return theNodeCount;
	}

	public static String getReservedWordThatClashesWith( 
			String theString,
			IRPProject basedOnProject ){

		String theReservedWordThatClashes = null;

		String theReservedWordsList =
				basedOnProject.getPropertyValue( "General.Model.ReservedWords" );

		if( theReservedWordsList != null ){

			String[] theReservedWords = theReservedWordsList.split(" ");

			for( int i = 0; i < theReservedWords.length; i++ ){

				if( theString.equals( theReservedWords[i] ) ){

					theReservedWordThatClashes = theReservedWords[i];
					break;
				}
			}
		}

		return theReservedWordThatClashes;
	}
	public List<String> getWarnings(){
		List<String> theWarnings = new ArrayList<>();

		if( parent != null ){
			String theParentName = parent.getM_ElementName();

			if( theParentName != null && 
					_elementName.equals( theParentName ) ){

				String theWarning = this.getString() + 
						" has the same name as its parent " + theParentName + 
						" (" + _elementName + " will be appended with underscore)";

				theWarnings.add( theWarning );
			}
		}

		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		IRPProject theRhpPrj = theRhpApp.activeProject();

		String theReservedWordsThatClashes = 
				getReservedWordThatClashesWith(
						_elementName, 
						theRhpPrj );

		if( theReservedWordsThatClashes != null ){

			String theWarning = "Element name clashes with reserved word '" + 
					theReservedWordsThatClashes + 
					"' (name will be appended with underscore)";

			theWarnings.add( theWarning );
		}

		for( RhpEl child : children ){
			theWarnings.addAll( child.getWarnings() );
		}

		return theWarnings;
	}

	public List<String> getInfos(){
		List<String> theInfos = new ArrayList<>();

		if( parent != null ){
			String theInfo = this.getString() + " owned by " + parent.getString(); 
			theInfos.add( theInfo );	
		}
		
		if( children != null ){
			for( RhpEl child : children ){
				theInfos.addAll( child.getInfos() );
			}
		}

		return theInfos;
	}

	private boolean isFirstPass(){
		
		boolean isFirstPass = false;
		

		if( this instanceof RhpElEvent || 
			this instanceof RhpElPackage ||
			this instanceof RhpElProject ){
			
			isFirstPass = true;
		}
		
		return isFirstPass;
	}
	
	public void createNodeElementsAndChildrenForJustEvents( 
			IRPModelElement theRootOwner,
			RhpEl treeRoot ){

		try {
			if( isFirstPass() ){

				_context.info( "createNodeElementsAndChildrenForJustEvents " + this.getString() + 
						" was invoked for theRootOwner " + 
						_context.elInfo( theRootOwner ) );

				this.createRhpEl( treeRoot );
			}
			
			if( children != null && !children.isEmpty() ){

				for( RhpEl child : children ){

					child.createNodeElementsAndChildrenForJustEvents( 
							theRootOwner,
							treeRoot );
				}
			}

		} catch (Exception e) {

			_context.info("Unhandled exception in createElementsAndChildren, e=" + e.getMessage());
		}
	}
	
	public void createNodeElementsAndChildren( 
			IRPModelElement theRootOwner,
			RhpEl treeRoot ){

		try {
			if( this instanceof RhpElElement ){

				if( !isFirstPass() ){
					
					_context.info( "createElements " + this.getString() + " was invoked for theRootOwner " + 
							_context.elInfo( theRootOwner ) );

					this.createRhpEl( treeRoot );
				}

				if( children != null && !children.isEmpty() ){

					for( RhpEl child : children ){

						child.createNodeElementsAndChildren( 
								theRootOwner,
								treeRoot );
					}
				}
			} else if( !(this instanceof RhpElRelation) ){
				throw new Exception("Expected type found");
			}

		} catch (Exception e) {

			_context.info("Unhandled exception in createElementsAndChildren, e=" + e.getMessage());
		}
	}

	public RhpEl findNestedElementWith( 
			String theElementGuid ){

		RhpEl theElement = null;

//		_context.info("findNestedElementWith was invoked to see if " + 
//				this.getString() + " has the Guid = " + theElementGuid );

		try {

			if( _elementGuid.equals( theElementGuid )){
				
				theElement = this;

			} else if( children != null && !children.isEmpty() ){

				for( RhpEl child : children ){

					theElement = child.findNestedElementWith( 
							theElementGuid );

					if( theElement != null ){
						break;
					}
				}
			}

		} catch (Exception e) {

			_context.info("Unhandled exception in findNestedElementWith, e=" + e.getMessage());
		}

		return theElement;
	}
	/*
	private ModelElement findNestedTransitionWithTarget(
			String theTargetGuid ){
				
		ModelElement theNestedTrans = null;
		
		if( children != null && !children.isEmpty() ){

			for( ModelElement child : children ){
				
				if( child instanceof Transition ){
					_context.info( child.getString() + " is a child of " + this.getString() );
				
					Transition theTransition = (Transition)child;
					theTransition.g
				}
				
				if( child.get_elementGuid().equals(theTargetGuid) ){
					
				}
			}
		}
	}*/
	
	public void createRelationshipsAndChildren(
			RhpEl treeRoot ){

		try {
			if( this instanceof RhpElRelation  ){

				_context.info( "createRelationshipsAndChildren " + this.getString() + " was invoked for theRootOwner " );

				this.createRhpEl( treeRoot );
			}

			if( children != null && !children.isEmpty() ){

				for( RhpEl child : children ){

					child.createRelationshipsAndChildren(
							treeRoot );
				}
			}

		} catch (Exception e) {

			_context.info("Unhandled exception in createRelationshipsAndChildren, e=" + e.getMessage());
		}
	}
	

	public void addMergeNodes( 
			RhpEl treeRoot ){

		try {
			if( this instanceof RhpElGraphNode ){

				RhpElGraphNode theRhpElGraphNode = (RhpElGraphNode)this;
				
				IRPGraphElement theGraphEl = theRhpElGraphNode.get_graphEl();
				
				IRPModelElement theDstModelObject = theGraphEl.getModelObject();
				
				if( theDstModelObject instanceof IRPStateVertex ){
					
					IRPStateVertex theDstStateVertex = (IRPStateVertex)theDstModelObject;
					
					@SuppressWarnings("unchecked")
					List<IRPTransition> inTransitions = theDstStateVertex.getInTransitions().toList();
					
					if( inTransitions.size() > 1 ){
						addMergeNodeFor( theGraphEl, inTransitions );
					}
				}
				
				_context.info( "addMergeNodes " + this.getString() + 
						" was invoked, found model object is " + _context.elInfo( theDstModelObject ) );
			}
			
			if( children != null && !children.isEmpty() ){

				for( RhpEl child : children ){

					child.addMergeNodes( 
							treeRoot );
				}
			}

		} catch (Exception e) {

			_context.info("Unhandled exception in createElementsAndChildren, e=" + e.getMessage());
		}
	}

	protected IRPGraphNode addMergeNodeFor(
			IRPGraphElement theGraphEl,
			List<IRPTransition> inTransitions ) throws Exception {

		IRPModelElement theModelObject = theGraphEl.getModelObject();

		IRPStateVertex theDstStateVertex = (IRPStateVertex)theModelObject;

		_context.info( _context.elInfo( theModelObject ) + " has " + inTransitions.size() + " incoming transitions");
		
//		theModelObject.highLightElement();

		_context.info( "The parent is " + _context.elInfo( parent.get_rhpEl() ) );

		IRPFlowchart theActivityDiagram = (IRPFlowchart) parent.get_rhpEl();
		IRPActivityDiagram theActivityDiagramGE = theActivityDiagram.getFlowchartDiagram();

		// condition connector; see also Fork, History, Join, and Termination
		IRPStateVertex mergeNode = theActivityDiagram.getRootState().addConnector(
				"MergeNode");

		GraphNodeInfo theDstGraphNodeInfo = new GraphNodeInfo( (IRPGraphNode) theGraphEl, _context );

		int nHeightMergeNode = 77;
		int nWidthMergeNode = 48;
		int xMergeNode = theDstGraphNodeInfo.getMiddleX() - nWidthMergeNode/2;
		int yMergeNode = theDstGraphNodeInfo.getTopLeftY() - nHeightMergeNode - 10;

		IRPGraphNode theMergeGraphNode = theActivityDiagramGE.addNewNodeForElement(
				mergeNode, 
				xMergeNode, 
				yMergeNode, 
				nWidthMergeNode, 
				nHeightMergeNode );

//		mergeNode.highLightElement();

		ListIterator<IRPTransition> iter = inTransitions.listIterator();

		while( iter.hasNext() ){

			IRPTransition theExistingTrans = iter.next();

			IRPStateVertex theExistingSrc = theExistingTrans.getItsSource();

			IRPGraphNode theExistingSrcGraphNode = (IRPGraphNode) _context.getCorrespondingGraphElement( 
					theExistingSrc, theActivityDiagramGE );

			GraphNodeInfo theSrcInfo = new GraphNodeInfo( theExistingSrcGraphNode, _context );

			IRPTransition theNewTransition = theExistingSrc.addTransition( mergeNode );

			IRPGuard theExistingGuard = theExistingTrans.getItsGuard();

			if( theExistingGuard != null ){

				_context.info("Copying over guard as " + theExistingGuard.getBody() + " with Label = " + theExistingTrans.getDisplayName());
				theNewTransition.setItsGuard( theExistingGuard.getBody() );
				theNewTransition.setDisplayName( theExistingTrans.getDisplayName() );

			} else {
				_context.info( "The transition has no guard condition" );
			}

			theNewTransition.changeTo( theExistingTrans.getUserDefinedMetaClass() );

			@SuppressWarnings("unused")
			IRPGraphEdge theNewGraphEdge = theActivityDiagram.addNewEdgeForElement(
					theNewTransition, 
					theExistingSrcGraphNode, 
					theSrcInfo.getMiddleX(), 
					theSrcInfo.getMiddleY(), 
					theMergeGraphNode, 
					xMergeNode + nWidthMergeNode/2, 
					yMergeNode );

			theExistingTrans.deleteFromProject();
		}

		// add control flow from merge node to the previous destination
		IRPTransition outTransition = mergeNode.addTransition( theDstStateVertex );
		outTransition.changeTo("ControlFlow");

//		GraphNodeInfo theMergeGraphNodeInfo = new GraphNodeInfo( theMergeGraphNode );

		IRPGraphElement theExistingTgtGraphEl = _context.getCorrespondingGraphElement( 
				theDstStateVertex, theActivityDiagramGE );

		GraphNodeInfo theExistingTgtGraphElInfo = 
				new GraphNodeInfo( (IRPGraphNode) theExistingTgtGraphEl, _context );

		@SuppressWarnings("unused")
		IRPGraphEdge theOutEdge = theActivityDiagram.addNewEdgeForElement(
				outTransition, 
				theMergeGraphNode, 
				xMergeNode + nWidthMergeNode/2, 
				yMergeNode + nHeightMergeNode, 
				(IRPGraphNode) theExistingTgtGraphEl, 
				xMergeNode + nWidthMergeNode/2, 
				theExistingTgtGraphElInfo.getMiddleY()-1 );
		
		return theMergeGraphNode;
	}
	
	public void reflowControlNodesFromReceiveEvents( 
			RhpEl treeRoot ){

		try {
			if( this instanceof RhpElAcceptEventAction ){

				RhpElGraphNode theRhpElGraphNode = (RhpElGraphNode)this;
				
				IRPGraphElement theGraphEl = theRhpElGraphNode.get_graphEl();
				
				IRPModelElement theModelObject = theGraphEl.getModelObject();
				
				_context.info( "Looking for pins on " + _context.elInfo( theModelObject) );
				List<IRPPin> thePins = _context.getPins( (IRPAcceptEventAction) theModelObject );
				
				if( thePins.size() == 1 ){
					
					IRPPin thePin = thePins.get( 0 );
					
					_context.info("Found an accept event action with a single pin = " + _context.elInfo(thePin) );

					IRPStateVertex thePinTarget = 
							_context.getTargetOfOutTransitionIfSingleOneExisting( 
									thePin );
					
					_context.info("The pin target is " + _context.elInfo( thePinTarget ) );

					@SuppressWarnings("unchecked")
					List<IRPTransition> theOutTransitions = 
						((IRPStateVertex) theModelObject).getOutTransitions().toList();
					
					if( theOutTransitions.size() == 1 ){
						
						IRPTransition theExistingTransition = theOutTransitions.get( 0 );
						
						IRPModelElement theTarget = theExistingTransition.getItsTarget();
						
						IRPTransition theNewTransition =
								thePinTarget.addTransition( (IRPStateVertex) theTarget );
						
						theNewTransition.changeTo( "ControlFlow" );
						
						IRPGraphNode theSrcGraphNode = (IRPGraphNode) _context.getCorrespondingGraphElement( 
								thePinTarget, (IRPActivityDiagram) theGraphEl.getDiagram() );
						
						GraphNodeInfo theSrcGraphNodeInfo = new GraphNodeInfo( theSrcGraphNode, _context );

						IRPGraphNode theDstGraphNode = (IRPGraphNode) _context.getCorrespondingGraphElement( 
								theTarget, (IRPActivityDiagram) theGraphEl.getDiagram() );
						
						GraphNodeInfo theDstGraphNodeInfo = new GraphNodeInfo( theDstGraphNode, _context );

						@SuppressWarnings("unused")
						IRPGraphEdge theGraphEdge = theGraphEl.getDiagram().addNewEdgeForElement(
								theNewTransition, 
								theSrcGraphNode, 
								theSrcGraphNodeInfo.getMiddleX(), 
								theSrcGraphNodeInfo.getMiddleY(), 
								theDstGraphNode, 
								theDstGraphNodeInfo.getMiddleX(), 
								theDstGraphNodeInfo.getMiddleX() );
						
						theExistingTransition.deleteFromProject();
					}
				}
				
				_context.info( "reflow " + this.getString() + 
						" was invoked, found model object is " + _context.elInfo( theModelObject ) );
			}
			
			if( children != null && !children.isEmpty() ){

				for( RhpEl child : children ){

					child.reflowControlNodesFromReceiveEvents(
							treeRoot );
				}
			}

		} catch (Exception e) {

			_context.info("Unhandled exception in createElementsAndChildren, e=" + e.getMessage());
		}
	}
	
	public static IRPTransition getElseTransitionFrom( 
			IRPStateVertex theDecisionNodeStateVertex ){
	
		@SuppressWarnings("unchecked")
		List<IRPTransition> theCandidates = 
				theDecisionNodeStateVertex.getOutTransitions().toList();
		
		IRPTransition theElseTransition = null;
		
		for( IRPTransition theCandidate : theCandidates ){
			
			IRPGuard theGuard = theCandidate.getItsGuard();
			
			if( theGuard != null ){
				String theBody = theGuard.getBody().trim();
				
				if( theBody.equals( "else" ) ){
					theElseTransition = theCandidate;
					break;
				}
			}
		}
		
		return theElseTransition;
	}
	
	public void addElseTransitionsIfNeeded( 
			RhpEl treeRoot ){

		try {
			if( this instanceof RhpElDecisionNodeAsCallOperation ){
				
				RhpElDecisionNodeAsCallOperation theRhpElDecisionNodeAsCallOp = 
						(RhpElDecisionNodeAsCallOperation)this;
				
				IRPGraphNode theDecisionNode = 
						theRhpElDecisionNodeAsCallOp.get_decisionNodeGraphNode();
				
				IRPStateVertex theDecisionNodeStateVertex = 
						(IRPStateVertex) theDecisionNode.getModelObject();
				
//				theDecisionNodeStateVertex.highLightElement();
				
				IRPTransition theElseTransition =
						getElseTransitionFrom( theDecisionNodeStateVertex );
				
				if( theElseTransition == null ){
					
					_context.info("Found that " + _context.elInfo( theDecisionNodeStateVertex ) + 
							" has no outgoing else transition, which violates a Rhapsody executable rule" );
					
					_context.info( "The parent is " + _context.elInfo( parent.get_rhpEl() ) );
					IRPFlowchart theActivityDiagram = (IRPFlowchart) parent.get_rhpEl();
					IRPActivityDiagram theActivityDiagramGE = theActivityDiagram.getFlowchartDiagram();
					IRPState theRootState = theActivityDiagram.getRootState();
					
					// condition connector; see also Fork, History, Join, and Termination
					IRPState theFinalFlow = theRootState.addState( "" );

					theFinalFlow.setStateType( "FlowFinal" );

					GraphNodeInfo theDecisionNodeInfo = new GraphNodeInfo( theDecisionNode, _context );
					
					IRPGraphNode theFlowFinalGraphNode = 
							theActivityDiagramGE.addNewNodeForElement(
							theFinalFlow, 
							theDecisionNodeInfo.getMiddleX()-12, 
							theDecisionNodeInfo.getBottomLeftY() + 40, 
							25,
							26 );
					
					theElseTransition = theDecisionNodeStateVertex.addTransition( theFinalFlow );
//					theElseTransition.changeTo("Control Flow");
					theElseTransition.setItsGuard("else");

//					theElseTransition.changeTo("Control Flow");
					
					@SuppressWarnings("unused")
					IRPGraphEdge theNewTransitionEdge = 
							theActivityDiagramGE.addNewEdgeForElement(
									theElseTransition, 
									theDecisionNode, 
									theDecisionNodeInfo.getMiddleX(),
									theDecisionNodeInfo.getBottomLeftY(), 
									theFlowFinalGraphNode, 
									theDecisionNodeInfo.getMiddleX(), 
									theDecisionNodeInfo.getBottomLeftY() + 40 );

				} else {
					_context.info("Found that " + _context.elInfo( theDecisionNodeStateVertex ) + 
							" does not need an else transition adding" );
				}
			}
			
			if( children != null && !children.isEmpty() ){

				for( RhpEl child : children ){

					child.addElseTransitionsIfNeeded(
							treeRoot );
				}
			}

		} catch (Exception e) {

			_context.error("Unhandled exception in createElementsAndChildren, e=" + e.getMessage());
		}
	}
}

/**
 * Copyright (C) 2018-2021  MBSE Training and Consulting Limited (www.executablembse.com)

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