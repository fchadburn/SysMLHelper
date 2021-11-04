package com.mbsetraining.sysmlhelper.taumigratorplugin;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mbsetraining.sysmlhelper.common.GraphNodeInfo;
import com.telelogic.rhapsody.core.*;

public class RhpElTransition extends RhpElGraphEdge {

	boolean _isDefaultTransition;
	String _srcGuid = null;
	String _newTerm = null;
	
	Pattern _p = Pattern.compile("^\\d+,*");

	public boolean is_isDefaultTransition() {
		return _isDefaultTransition;
	}

	public void set_isDefaultTransition(boolean _isDefaultTransition) {
		this._isDefaultTransition = _isDefaultTransition;
	}

	public void set_srcGuid(String _srcGuid) {
		this._srcGuid = _srcGuid;
	}

	public void set_dstGuid(String _dstGuid) {
		this._dstGuid = _dstGuid;
	}

	public String get_guard() {
		return _guard;
	}

	public void set_guard(String _guard) {
		this._guard = _guard;
	}

	public String get_srcGuid() {
		return _srcGuid;
	}

	public String get_dstGuid() {
		return _dstGuid;
	}

	String _dstGuid = null;
	String _guard = null;
	
	public RhpElTransition(
			String theElementName, 
			String theElementType,
			String theElementGuid,
			String theSegmentPoints,
			boolean isDefaultTransition,
			String theSrcGuid,
			String theDstGuid,
			String theGuard,
			String theNewTerm,
			TauMigrator_Context context ) throws Exception{
		
		super( theElementName, theElementType, theElementGuid, theSegmentPoints, context );
		
		_isDefaultTransition = isDefaultTransition;
		_srcGuid = theSrcGuid;
		_dstGuid = theDstGuid;
		_guard = theGuard.trim();
		_newTerm = theNewTerm;
		
		dumpInfo();
	}

	public RhpElTransition(
			String theElementName, 
			String theElementType,
			String theElementGuid,
			RhpEl theParent,
			String theSegmentPoints,
			boolean isDefaultTransition,
			String theSrcGuid,
			String theDstGuid,
			String theGuard,
			String theNewTerm,
			TauMigrator_Context context ) throws Exception {
		
		super( theElementName, theElementType, theElementGuid, theParent, theSegmentPoints, context );
	
		_isDefaultTransition = isDefaultTransition;
		_srcGuid = theSrcGuid;
		_dstGuid = theDstGuid;
		_guard = theGuard.trim();
		_newTerm = theNewTerm;
		
		dumpInfo();
	}
	
	public void dumpInfo() {
		
		String theMsg = "";
		theMsg += "===================================\n"; 
		theMsg += "Create " + this.getString() + "\n";
		theMsg += "_isDefaultTransition = " + _isDefaultTransition + "\n";
		theMsg += "_srcGuid             = " + _srcGuid + "\n";
		theMsg += "_dstGuid             = " + _dstGuid + "\n";
		theMsg += "_guard               = " + _guard + "\n";
		theMsg += "_newTerm             = " + _newTerm + "\n";
		theMsg += "_segmentPoints       = " + _segmentPoints + "\n";

//		theMsg += "_xSrcPosition     	= " + _xSrcPosition + "\n";
//		theMsg += "_ySrcPosition        = " + _ySrcPosition + "\n";
//		theMsg += "_xTrgPosition        = " + _xTrgPosition + "\n";
//		theMsg += "_yTrgPosition        = " + _yTrgPosition + "\n";
		theMsg += "===================================\n";
		
		_context.info( theMsg );
	}

	@Override
	public IRPModelElement createRhpEl(
			RhpEl treeRoot ) throws Exception {

		_rhpEl = null;
		
		_context.info("createRhpEl invoked for " + getString() + " owned by " + parent.getString());
		_context.info("with guard = " + _guard);
		
		RhpEl theSrc = treeRoot.findNestedElementWith( _srcGuid );
		RhpEl theDst = treeRoot.findNestedElementWith( _dstGuid );

		if( theSrc == null ){
			_context.info("Sorry, unable to find Src element with guid = " + _srcGuid );
		} else {
			_context.info("Success! findNestedElementWith matched with " + theSrc.getString() );				
		}
		
		if( theDst == null ){
			_context.info("Sorry, unable to find Dst element with guid = " + _dstGuid );
		} else {
			_context.info("Success! findNestedElementWith matched with " + theDst.getString() );				
		}
		
		if( theSrc != null && 
				theSrc instanceof RhpElGraphNode && 
				theDst != null && 
				theDst instanceof RhpElGraphNode ){
			
			IRPModelElement theSrcModelEl = theSrc.get_rhpEl();
			_context.info( "theSrcModelEl = " + _context.elInfo( theSrcModelEl ) );
			theSrcModelEl.highLightElement();
			
			IRPModelElement theDstModelEl = theDst.get_rhpEl();
			_context.info( "theDstModelEl = " + _context.elInfo( theDstModelEl ) );
			theDstModelEl.highLightElement();
			
//			RhpElGraphNode theSrcRhlElGraphNode = (RhpElGraphNode)theSrc;
			
//			_context.info( "The parent is " + _context.elementInfo( parent.get_rhpEl() ) );

			IRPFlowchart theActivityDiagram = (IRPFlowchart) parent.get_rhpEl();
			IRPActivityDiagram theActivityDiagramGE = theActivityDiagram.getFlowchartDiagram();
			IRPState theRootState = theActivityDiagram.getRootState();

			IRPGraphElement theSrcGraphNode = 
					_context.getCorrespondingGraphElement( theSrcModelEl, theActivityDiagramGE ) ;
			
			IRPGraphElement theDstGraphNode = 
					_context.getCorrespondingGraphElement( theDstModelEl, theActivityDiagramGE ) ;

			//RhpElGraphNode theDstRhpElGraphNode = (RhpElGraphNode)theDst;
			
//			IRPGraphNode theDstGraphNode = theDstRhpElGraphNode.get_graphEl(); 

			_context.info( "The parent is " + _context.elInfo( parent.get_rhpEl() ) );
			_context.info( "The theSrcModelEl is " + _context.elInfo( theSrcModelEl ) );
			_context.info( "The theDstModelEl is " + _context.elInfo( theDstModelEl ) );

			IRPModelElement theParentOfDiagram = parent.getParent().get_rhpEl();
			//_context.info("The parent of diagram is " + _context.elementInfo(theParentOfDiagram));
			
			
			String theGuard = "";
			
			if( theSrc instanceof RhpElDecisionNodeAsReceiveEvent ){
				
				IRPPin thePin = _context.getPin("value", (IRPAcceptEventAction) theSrcModelEl);
				
				if( thePin != null ){
					
					_context.info("Switch object flow to go from the pin");
					theSrcModelEl = thePin;
					theSrcGraphNode = (IRPGraphNode) _context.getCorrespondingGraphElement( 
							thePin, theActivityDiagramGE );
				}


			} else if( theSrc instanceof RhpElDecisionNodeAsCallOperation ){

				RhpElDecisionNodeAsCallOperation theDecisionNodeAsCallOp = 
						(RhpElDecisionNodeAsCallOperation)theSrc;
				
				theSrcModelEl = theDecisionNodeAsCallOp.get_decisionNode();
				theSrcGraphNode = theDecisionNodeAsCallOp.get_decisionNodeGraphNode();
				
				if( _guard.equals("\"False\"") ){

					theGuard = "else";

				} else {
					
					IRPAttribute theAttribute = theDecisionNodeAsCallOp.get_attribute();
					theGuard = theAttribute.getName() + "==" + _guard;
					
				}
				
			} else if( theSrc instanceof RhpElDecisionNode ){
				
				String theAttributeName = ((RhpElDecisionNode) theSrc).get_text();
				
				_context.info( "theAttributeName is " + theAttributeName );
				
				IRPModelElement theAttrEl = 
						theParentOfDiagram.findNestedElement( 
								theAttributeName, "Attribute" );
				
				if( theAttrEl != null ){
					
					_context.info( "Successfully found that DecisionNode refers to " + _context.elInfo( theAttrEl ) );
					
					Matcher m = _p.matcher( _guard );
					
					if( m.find() ){
						
						String[] split = _guard.split(",");
						
						for(int i = 0; i < split.length; i++){
						    theGuard += theAttributeName + " == " + split[i];
						    if( i < split.length-1 ){
						    	theGuard += " || "; // Or
						    }
						}
						
						_context.info( "Changing the guard from " + _guard + " to " + theGuard );						
					}
				}
			}
			
			if( theSrcModelEl instanceof IRPStateVertex ){

				_context.info( "theSrcModelEl is " + _context.elInfo( theSrcModelEl) );
				IRPStateVertex theSrcState = (IRPStateVertex)theSrcModelEl;
				
				if( theDstModelEl instanceof IRPStateVertex ){
					
					IRPStateVertex theDstState = (IRPStateVertex)theDstModelEl;
					
					boolean isSrcADecisionNode = ( theSrcState instanceof IRPConnector &&
							((IRPConnector)theSrcState).getConnectorType().equals("Condition"));
					
					_context.info( "theSrc " + _context.elInfo( theSrcModelEl ) + " is decision node = " + isSrcADecisionNode );

					boolean isDstAFinalFlow = ( theDstState instanceof IRPState &&
							((IRPState)theDstState).getStateType().equals("FlowFinal"));
					
					_context.info( "theDst " + _context.elInfo( theDstModelEl ) + " is flow final = " + isDstAFinalFlow );
					
					theDstModelEl.highLightElement();
					
					if( isSrcADecisionNode && isDstAFinalFlow ){
						
						IRPGraphNode theExistingDstGraphNode = 
								(IRPGraphNode) _context.getCorrespondingGraphElement( 
										theDstModelEl, theActivityDiagramGE );
												
						//_context.dumpGraphicalPropertiesFor(theExistingDstGraphNode);
						
						GraphNodeInfo theDstNodeInfo = new GraphNodeInfo( theExistingDstGraphNode, _context );

						IRPGraphNode theExistingSrcGraphNode = 
								(IRPGraphNode) _context.getCorrespondingGraphElement( 
										theDstModelEl, theActivityDiagramGE );
						
						GraphNodeInfo theSrcNodeInfo = new GraphNodeInfo( theExistingSrcGraphNode, _context );

						IRPState dummyState = theRootState.addState("");
						
						dummyState.setEntryAction("// do nothing");
						
						int dummyStateX = theDstNodeInfo.getTopLeftX() - 47 + theDstNodeInfo.getWidth()/2;
						int dummyStateY = theDstNodeInfo.getTopLeftY() - 80;
						int dummyStateWidth = 95;
						int dummyStateHeight = 60;
						
						IRPGraphNode dummyStateGraphNode = theActivityDiagramGE.addNewNodeForElement(
								dummyState, 
								dummyStateX, 
								dummyStateY, 
								dummyStateWidth, 
								dummyStateHeight );
												
						_rhpEl = createTransitionWith(
								theGuard, 
								theSrcState, 
								dummyState );
						
						_rhpEl.changeTo("Control Flow");
						
						_graphEdge = theActivityDiagram.addNewEdgeForElement(
								_rhpEl, 
								(IRPGraphNode)theSrcGraphNode, 
								theSrcNodeInfo.getTopLeftX(), 
								theSrcNodeInfo.getTopLeftY() - theSrcNodeInfo.getHeight()/2, 
								(IRPGraphNode)dummyStateGraphNode, 
								dummyStateX + dummyStateWidth/2, 
								dummyStateY );
						
						IRPTransition theFollowOnTransition =
								dummyState.addTransition( theDstState );
						
						@SuppressWarnings("unused")
						IRPGraphEdge followOnGraphEdge = 
								theActivityDiagram.addNewEdgeForElement(
										theFollowOnTransition, 
										(IRPGraphNode)dummyStateGraphNode, 
										dummyStateX + dummyStateWidth/2, 
										dummyStateY + dummyStateHeight, 
										(IRPGraphNode)theDstGraphNode, 
										theDstNodeInfo.getTopLeftX() + theDstNodeInfo.getWidth()/2, 
										theDstNodeInfo.getTopLeftY() );
						
					} else {
						
						// these can only have 1 incoming flow
						if( theDst instanceof RhpElDiagramConnector ){
														
							@SuppressWarnings("unchecked")
							List<IRPTransition> inTransitions = theDstState.getInTransitions().toList();

							@SuppressWarnings("unchecked")
							List<IRPTransition> outTransitions = theDstState.getOutTransitions().toList();

							if( inTransitions.size() > 0 ){
								
								IRPGraphNode theMergeNode = addMergeNodeFor(
										theDstGraphNode,
										inTransitions );
								
								IRPModelElement theMergeEl = theMergeNode.getModelObject();
								
								_context.info( "Switching destination from " + _context.elInfo( theDstState ) + 
										" to " + _context.elInfo( theMergeEl ) );
								
								theDstGraphNode = theMergeNode;
								theDstState = (IRPStateVertex) theMergeEl;
								
							} else if( outTransitions.size() > 0){
								_context.info( "Error, Unable to add transition due to presence of " + 
										outTransitions.size() + " existing out transitions" );
							} else {
								addTransitionTo(
										theActivityDiagram, 
										theSrcGraphNode,
										theDstGraphNode, 
										theGuard, 
										theSrcState,
										theDstState);
							}

						} else if( theSrc instanceof RhpElDiagramConnector ){
							
							@SuppressWarnings("unchecked")
							List<IRPTransition> outTransitions = theSrcState.getOutTransitions().toList();
							
							@SuppressWarnings("unchecked")
							List<IRPTransition> inTransitions = theSrcState.getInTransitions().toList();

							if( outTransitions.size() == 0 && 
									inTransitions.size() == 0 ){
								
								addTransitionTo(
										theActivityDiagram, 
										theSrcGraphNode,
										theDstGraphNode, 
										theGuard, 
										theSrcState,
										theDstState);
							} else {
								_context.info("Error, unable to add transition from " + _context.elInfo( theSrcModelEl ) + " owned by " + _context.elInfo( theActivityDiagram ) + " as the destination " + theDstModelEl + " has " +
										outTransitions.size() + " out transitions and " + inTransitions.size() + 
										" in transitions, and it is a diagram connector (which can only have 1)");
							}
						} else {
							
							addTransitionTo(
									theActivityDiagram, 
									theSrcGraphNode,
									theDstGraphNode, 
									theGuard, 
									theSrcState,
									theDstState);
						}
					}
				}
			}

		}

		_rhpEl.highLightElement();
		
		return _rhpEl;
	}

	private void addTransitionTo(
			IRPFlowchart theActivityDiagram,
			IRPGraphElement theSrcGraphNode, 
			IRPGraphElement theDstGraphNode,
			String theGuard, 
			IRPStateVertex theSrcState,
			IRPStateVertex theDstState) throws Exception {
		
		_rhpEl = createTransitionWith(
				theGuard, 
				theSrcState, 
				theDstState );

		GraphNodeInfo theSrcInfo = new GraphNodeInfo( (IRPGraphNode) theSrcGraphNode, _context ); 
		GraphNodeInfo theDstInfo = new GraphNodeInfo( (IRPGraphNode) theDstGraphNode, _context ); 
		
		int srcX;
		int srcY;
		int dstX;
		int dstY;
		
		if( theSrcInfo.getMiddleX() > theDstInfo.getMiddleX() + 5  ){
			
			srcX = theSrcInfo.getTopLeftX();
			srcY = theSrcInfo.getMiddleY();
			dstX = theDstInfo.getTopRightX();
			dstY = theDstInfo.getMiddleY();
		
		} else if( theSrcInfo.getMiddleX() + 5 < theDstInfo.getMiddleX()  ){
			
			srcX = theSrcInfo.getTopRightX();
			srcY = theSrcInfo.getMiddleY();
			dstX = theDstInfo.getTopLeftX();
			dstY = theDstInfo.getMiddleY();
			
		} else {
			
			srcX = theSrcInfo.getMiddleX();
			srcY = theSrcInfo.getBottomRightY();
			dstX = theDstInfo.getMiddleX();
			dstY = theDstInfo.getTopLeftY();
		}			
		
		_graphEdge = theActivityDiagram.addNewEdgeForElement(
				_rhpEl, 
				(IRPGraphNode)theSrcGraphNode, 
				srcX, 
				srcY, 
				(IRPGraphNode)theDstGraphNode, 
				dstX, 
				dstY );
	}

	private IRPTransition createTransitionWith(
			String withTheGuard,
			IRPStateVertex fromSrc, 
			IRPStateVertex toDst ){
		
		IRPTransition theTransition =
				fromSrc.addTransition( toDst );
		
		theTransition.changeTo( _newTerm );
		
		if( _guard != null & !_guard.isEmpty() ){
			_context.info( "Setting Guard to " + _guard + " with Label = '" + _guard + "'" );
			theTransition.setItsGuard( withTheGuard );
			theTransition.setDisplayName( _guard );
		}
		
		return theTransition;
	}
}
