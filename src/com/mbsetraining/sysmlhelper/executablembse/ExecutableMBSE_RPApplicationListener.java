package com.mbsetraining.sysmlhelper.executablembse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import com.mbsetraining.sysmlhelper.common.ElementMover;
import com.mbsetraining.sysmlhelper.common.GraphEdgeInfo;
import com.mbsetraining.sysmlhelper.common.GraphNodeInfo;
import com.mbsetraining.sysmlhelper.common.NestedActivityDiagram;
import com.mbsetraining.sysmlhelper.common.RequirementMover;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.contextdiagram.CreateEventForFlowPanel;
import com.mbsetraining.sysmlhelper.graphelementhelpers.GraphNodeResizer;
import com.mbsetraining.sysmlhelper.portcreator.PortsForLinksCreator;
import com.mbsetraining.sysmlhelper.tracedelementpanels.CreateOperationPanel;
import com.telelogic.rhapsody.core.*;

public class ExecutableMBSE_RPApplicationListener extends RPApplicationListener {

	public static void main(String[] args) {
		String appID = RhapsodyAppServer.getActiveRhapsodyApplication().getApplicationConnectionString();

		ExecutableMBSE_Context theContext = new ExecutableMBSE_Context(appID);

		ExecutableMBSE_RPApplicationListener theListener = 
				new ExecutableMBSE_RPApplicationListener( "ExecutableMBSE", theContext );

		IRPModelElement theSelectedEl = theContext.getSelectedElement( false );
		theListener.afterAddElement( theSelectedEl );

		if( theSelectedEl instanceof IRPDiagram ) {

			IRPDiagram theDiagram = (IRPDiagram)theSelectedEl;

			// 4,32,32,204,32,204,998,32,998
			IRPGraphNode theNoteNode =
					theDiagram.addNewNodeByType( 
							"Note", 62, 62, 172, 966 );

			theNoteNode.setGraphicalProperty( 
					"TextDisplayMode",
					"Specification" );

			theNoteNode.setGraphicalProperty(
					"Text",
					"Hello" );		}
	}

	private ExecutableMBSE_Context _context;

	public ExecutableMBSE_RPApplicationListener( 
			String expectedProfileName,
			ExecutableMBSE_Context context ) {

		_context = context;
		_context.info( "ExecutableMBSE_RPApplicationListener was loaded - Listening for events (double-click etc)" ); 
	}

	@Override
	public boolean afterAddElement(
			IRPModelElement modelElement ){

		boolean doDefault = false;

		try {
			_context.setSavedInSeparateDirectoryIfAppropriateFor( 
					modelElement );

			String theUserDefinedMetaClass = modelElement.getUserDefinedMetaClass();

			if( modelElement instanceof IRPRequirement ){

				afterAddForRequirement( modelElement );

			} else if( modelElement instanceof IRPClass && 
					_context.hasStereotypeCalled( "Interface", modelElement )){

				afterAddForInterface( modelElement );

			} else if( modelElement instanceof IRPEvent ){

				afterAddForEvent( modelElement );

			} else if( modelElement instanceof IRPDependency && 
					modelElement.getUserDefinedMetaClass().equals(
							"Derive Requirement" ) ){

				afterAddForDeriveRequirement( (IRPDependency) modelElement );

			} else if( modelElement instanceof IRPCallOperation ){

				afterAddForCallOperation( 
						(IRPCallOperation) modelElement );

			} else if( modelElement instanceof IRPFlowchart && 
					_context.hasStereotypeCalled( _context.NEW_TERM_FOR_TEXTUAL_ACTIVITY_DIAGRAM, modelElement )){

				afterAddForTextualActivity( (IRPFlowchart) modelElement );

			} else if( modelElement instanceof IRPInstance && 
					_context.hasStereotypeCalled( _context.NEW_TERM_FOR_ACTOR_USAGE, modelElement )){

				afterAddForActorUsage( (IRPInstance) modelElement );

			} else if( modelElement instanceof IRPInstance && 
					_context.hasStereotypeCalled( _context.NEW_TERM_FOR_SYSTEM_USAGE, modelElement )){

				afterAddForSystemUsage( (IRPInstance) modelElement );

			} else if( modelElement instanceof IRPInstance && 
					_context.hasStereotypeCalled( _context.NEW_TERM_FOR_FUNCTION_USAGE, modelElement )){

				afterAddForFunctionUsage( (IRPInstance) modelElement );

			} else if( modelElement instanceof IRPInstance && 
					_context.hasStereotypeCalled( _context.NEW_TERM_FOR_DECISION_USAGE, modelElement )){

				afterAddForDecisionUsage( (IRPInstance) modelElement );

			} else if( modelElement instanceof IRPInstance && 
					_context.hasStereotypeCalled( _context.NEW_TERM_FOR_PARALLEL_GATEWAY_USAGE, modelElement )){

				afterAddForParallelGateway( (IRPInstance) modelElement );

			} else if( modelElement instanceof IRPInstance && 
					_context.hasStereotypeCalled( _context.NEW_TERM_FOR_START_USAGE, modelElement )){

				afterAddForStartUsage( (IRPInstance) modelElement );

			} else if( modelElement instanceof IRPInstance && 
					_context.hasStereotypeCalled( _context.NEW_TERM_FOR_FINAL_USAGE, modelElement )){

				afterAddForFinalUsage( (IRPInstance) modelElement );

			} else if( modelElement instanceof IRPInstance && 
					_context.hasStereotypeCalled( _context.NEW_TERM_FOR_TIME_EVENT_USAGE, modelElement )){

				afterAddForTimeEventUsage( (IRPInstance) modelElement );

			} else if( modelElement instanceof IRPInstance && 
					_context.hasStereotypeCalled( _context.NEW_TERM_FOR_DATA_OBJECT, modelElement )){

				afterAddForDataObjectUsage( (IRPInstance) modelElement );

			} else if( modelElement instanceof IRPInstance && 
					_context.hasStereotypeCalled( _context.NEW_TERM_FOR_FLOW_FINAL_USAGE, modelElement )){

				afterAddForFlowFinalUsage( (IRPInstance) modelElement );

			} else if( theUserDefinedMetaClass.equals( "Object" ) ){

				IRPInstance theInstance = (IRPInstance)modelElement;

				IRPClassifier theOtherClass = theInstance.getOtherClass();

				if( theOtherClass.getMetaClass().equals( "Actor" ) ){
					modelElement.changeTo( _context.ACTOR_USAGE );
				}

			} else if( modelElement instanceof IRPFlow ){

				afterAddForFlow( (IRPFlow) modelElement );

			} else if( modelElement instanceof IRPLink &&
					theUserDefinedMetaClass.equals( _context.FLOW_CONNECTOR ) ){

				afterAddForFlowConnector( (IRPLink) modelElement );

			} else if( modelElement instanceof IRPLink ){

				afterAddForLink( (IRPLink) modelElement );

			} else if( theUserDefinedMetaClass.equals( _context.FLOW_INPUT ) ){

				afterAddForFlowInput( (IRPSysMLPort) modelElement );

			} else if( theUserDefinedMetaClass.equals( _context.FLOW_OUTPUT ) ){

				afterAddForFlowOutput( (IRPSysMLPort) modelElement );

			} else if( theUserDefinedMetaClass.equals( _context.GUARDED_FLOW_OUTPUT ) ){

				afterAddForGuardedFlowOutput( (IRPSysMLPort) modelElement );

			} else if( theUserDefinedMetaClass.equals( _context.GUARDED_FLOW_OUTPUT ) ){

				afterAddForGuardedFlowOutput( (IRPSysMLPort) modelElement );

			} else if( theUserDefinedMetaClass.equals( _context.BLOCK_DEFINITION_DIAGRAM_SYSTEM ) ||
					theUserDefinedMetaClass.equals( _context.REQUIREMENTS_DIAGRAM_SYSTEM ) ){

				afterAddForBlockOrRequirementDiagram( (IRPObjectModelDiagram) modelElement );

			} else if( theUserDefinedMetaClass.equals( _context.INTERNAL_BLOCK_DIAGRAM_SYSTEM ) ||
					theUserDefinedMetaClass.equals( _context.INTERNAL_BLOCK_DIAGRAM_FUNCTIONAL ) ){

				afterAddForInternalBlockDiagram( (IRPStructureDiagram) modelElement );

			} else if( theUserDefinedMetaClass.equals( _context.SIMPLE_REQUIREMENTS_TABLE ) ){

				setScopeOfTableToOwningPackageIfOwnerIs( 
						new String[]{ _context.REQTS_ANALYSIS_REQUIREMENT_PACKAGE },
						(IRPTableView) modelElement,
						true );

			} else if( theUserDefinedMetaClass.equals( _context.SUBSYSTEM_TABLE ) ){

				setScopeOfTableToOwningPackageIfOwnerIs( 
						new String[]{ _context.DESIGN_SYNTHESIS_LOGICAL_SYSTEM_PACKAGE, 
								_context.DESIGN_SYNTHESIS_SUBSYSTEM_PACKAGE },
						(IRPTableView) modelElement,
						true );

			} else if( theUserDefinedMetaClass.equals( _context.CONTEXT_DIAGRAM_FLOWS_TABLE ) ){

				setScopeOfTableToOwningPackageIfOwnerIs( 
						new String[]{ _context.REQTS_ANALYSIS_CONTEXT_DIAGRAM_PACKAGE,
								_context.REQTS_ANALYSIS_EXTERNAL_SIGNALS_PACKAGE },
						(IRPTableView) modelElement,
						true );

			} else if( theUserDefinedMetaClass.equals( _context.REQUIREMENT_TO_USE_CASE_TABLE ) ||
					theUserDefinedMetaClass.equals( _context.USE_CASE_TO_REQUIREMENT_TABLE ) ||
					theUserDefinedMetaClass.equals( _context.REQUIREMENT_TO_FUNCTION_BLOCK_TABLE ) ){

				setScopeOfTableToOwningPackageIfOwnerIs( 
						new String[]{ _context.REQTS_ANALYSIS_REQUIREMENT_PACKAGE }, 
						(IRPTableView) modelElement,
						false );

			} else if( theUserDefinedMetaClass.equals( _context.REQUIREMENT_TO_ACTION_TABLE ) ) {

				afterAddNewRequirementToActionTable( (IRPTableView) modelElement );

			} else if( theUserDefinedMetaClass.equals( _context.ACTION_TO_REQUIREMENT_TABLE ) ) {

				afterAddNewActionToRequirementTable( (IRPTableView) modelElement );
				
			} else if( modelElement instanceof IRPPackage &&
					_context.hasStereotypeCalled( _context.REQTS_ANALYSIS_WORKING_COPY_PACKAGE, modelElement ) ){

				afterAddForWorkingCopyPackage( (IRPPackage) modelElement );
			}

		} catch( Exception e ){
			_context.error( "ExecutableMBSE_RPApplicationListener.afterAddElement, " +
					" unhandled exception related to " + _context.elInfo( modelElement ) + 
					e.getMessage() );
		}

		return doDefault;
	}

	private void afterAddNewRequirementToActionTable(
			IRPTableView theView ){
		
		IRPModelElement theOwner = theView.getOwner();

		if( theOwner instanceof IRPPackage &&
				Arrays.stream( new String[]{ _context.REQTS_ANALYSIS_REQUIREMENT_PACKAGE } ).
				anyMatch( theOwner.getUserDefinedMetaClass()::contains ) ){

			setScopeToDependentUseCasePackages( theView, theOwner );
		}
		
		String theProposedName = _context.TABLE_VIEW_PREFIX + " Requirement To Action";
		String theUniqueName = _context.determineUniqueNameBasedOn( theProposedName, "TableView", theOwner );

		theView.setName( theUniqueName );
	}

	private void afterAddNewActionToRequirementTable(
			IRPTableView theView ){
		
		IRPModelElement theOwner = theView.getOwner();

		if( theOwner instanceof IRPPackage &&
				Arrays.stream( new String[]{ _context.REQTS_ANALYSIS_REQUIREMENT_PACKAGE } ).
				anyMatch( theOwner.getUserDefinedMetaClass()::contains ) ){

			setScopeToDependentUseCasePackages( theView, theOwner );
		}
		
		String theProposedName = _context.TABLE_VIEW_PREFIX + " Action To Requirement";
		String theUniqueName = _context.determineUniqueNameBasedOn( theProposedName, "TableView", theOwner );

		theView.setName( theUniqueName );
	}
	
	private void setScopeToDependentUseCasePackages(
			IRPTableView theView, 
			IRPModelElement theOwner ){
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theReferences = theOwner.getReferences().toList();

		List<IRPModelElement> theScopingEls = new ArrayList<>();

		for( IRPModelElement theReference : theReferences ){

			if( theReference instanceof IRPDependency ){

				IRPDependency theDependency = (IRPDependency) theReference;
				IRPModelElement theDependent = theDependency.getDependent();

				if( theDependent instanceof IRPPackage &&
						Arrays.stream( new String[]{ _context.REQTS_ANALYSIS_USE_CASE_PACKAGE } ).
						anyMatch( theDependent.getUserDefinedMetaClass()::contains ) ) {

					theScopingEls.add( theDependency.getDependent() );
				}
			}
		}

		if( !theScopingEls.isEmpty() ) {
			
			IRPCollection theScopedCollection = _context.createNewCollection();
			theScopedCollection.setSize( theScopingEls.size() );
			
			for( IRPModelElement theScopeEl : theScopingEls ){
				
				_context.debug( "ExecutableMBSE_RPApplicationListener is setting scope of " + 
						_context.elInfo( theView ) + " to " + _context.elInfo( theScopeEl ) );
				
				theScopedCollection.addItem( theScopeEl );
			}

			theView.setScope( theScopedCollection );
		}
	}

	private void afterAddForWorkingCopyPackage(
			IRPPackage thePkg ){

		IRPModelElement theOwner = thePkg.getOwner();

		if( theOwner instanceof IRPPackage &&
				_context.hasStereotypeCalled( _context.FUNCT_ANALYSIS_FEATURE_FUNCTION_PACKAGE, theOwner ) ){

			String theOwnersName = theOwner.getName();

			Pattern pattern = Pattern.compile( "(.*)_(.*Pkg)" );

			Matcher matcher = pattern.matcher( theOwnersName );

			boolean matchFound = matcher.find();

			if( matchFound ){

				String theProposedName = null;

				if( matcher.group(2) != null ){

					theProposedName = matcher.group(1) + "_WorkingCopy" + matcher.group(2);

					String theUniqueName = _context.determineUniqueNameBasedOn( theProposedName, "Package", theOwner );

					_context.debug( "Renamed " + _context.elInfo( thePkg ) + " to " + theUniqueName );

					thePkg.setName( theUniqueName );
				}
			}
		}		
	}

	private void afterAddForTextualActivity(
			IRPFlowchart theDiagram ){

		// Need to programmatically create the diagram as cloning and
		// deleting old one results in Rhapsody closing

		IRPModelElement theOwner = theDiagram.getOwner();

		if( theOwner.getName().equals("TopLevel") &&
				theOwner.getOwner() instanceof IRPPackage ) {

			theOwner = theOwner.getOwner();
		}

		//_context.info( "theOwner is + " + _context.elInfo( theOwner ) );

		String theProposedName = _context.ACTIVITY_DIAGRAM_PREFIX + theOwner.getName().replaceAll(" -\\s*(\\d*)$", "");

		String theUniqueName = 
				_context.determineUniqueNameBasedOn( 
						theProposedName, "ActivityDiagram", theOwner );

		//_context.info( "theUniqueName is + " + theUniqueName );

		theDiagram.setName( theUniqueName );

		// Create the graph elements

		IRPState theRootState = theDiagram.getRootState();

		IRPAcceptEventAction theAcceptEvent = 
				theDiagram.addAcceptEventAction( "<Type text-based Use Case Trigger>", theRootState );

		IRPTransition theDefaultTransition = theAcceptEvent.createDefaultTransition( theRootState );

		IRPState theAction = (IRPState) theDiagram.addNewAggr( "State", "" );
		theAction.setStateType("Action");
		theAction.setName( "action_0" );

		IRPTransition theTransition = theAcceptEvent.addTransition( theAction );

		IRPConstraint thePreConditionConstraint = (IRPConstraint) theDiagram.addNewAggr( "Constraint", "UC pre-conditions" );
		thePreConditionConstraint.setSpecification( "<type any pre-conditions else delete from model>" );
		thePreConditionConstraint.changeTo( "Use Case Precondition" );

		IRPState theTerminationState = theRootState.addState( "" );
		theTerminationState.setStateType( "LocalTermination" );
		theTerminationState.setName( "activityfinal" );

		IRPConstraint thePostConditionConstraint = (IRPConstraint) theDiagram.addNewAggr( "Constraint", "UC post-conditions" );
		thePostConditionConstraint.setSpecification( "<type any post-conditions else delete from model>" );
		thePostConditionConstraint.changeTo( "Use Case Postcondition" );	

		// Create the graph elements

		IRPActivityDiagram theAD = theDiagram.getFlowchartDiagram();

		// 4,286,152,669,152,669,192,286,192
		IRPGraphNode theAcceptEventNode = 
				theAD.addNewNodeForElement( 
						theAcceptEvent, 286, 152, 383, 40 );

		// 2,481,113,481,152
		IRPGraphEdge theDefaultTransitionEdge = 
				theAD.addNewEdgeForElement(
						theDefaultTransition, null, 481, 113, theAcceptEventNode, 481, 152 );

		// 4,402,235,558,235,558,306,402,306
		IRPGraphNode theActionNode = 
				theAD.addNewNodeForElement( 
						theAction, 402, 235, 156, 71 );

		// 2,480,192,480,235
		@SuppressWarnings("unused")
		IRPGraphEdge theTransitionEdge = 
		theAD.addNewEdgeForElement(
				theTransition, theAcceptEventNode, 480, 192, theActionNode, 480, 235 );

		// 4,551,21,769,21,769,109,551,109
		IRPGraphNode thePreConditionConstraintNode = 
				theAD.addNewNodeForElement( 
						thePreConditionConstraint, 551, 21, 218, 88 );

		// 2,50,0,551,48
		@SuppressWarnings("unused")
		IRPGraphEdge theAnchorToPreCondition = 
		theAD.addNewEdgeByType( 
				"Anchor", theDefaultTransitionEdge, 552, 217, thePreConditionConstraintNode, 481, 121 );

		// Height=26, Width=25, Position=475,910
		IRPGraphNode theTerminationStateNode =
				theAD.addNewNodeForElement( 
						theTerminationState, 475, 910, 25, 26 );

		// 4,567,900,785,900,785,1004,567,1004
		IRPGraphNode thePostConditionConstraintNode = 
				theAD.addNewNodeForElement( 
						thePostConditionConstraint, 567, 900, 218, 104 );

		// SourcePosition=500,925, TargetPosition=567,941
		@SuppressWarnings("unused")
		IRPGraphEdge theAnchorToPostCondition = 
		theAD.addNewEdgeByType( 
				"Anchor", theTerminationStateNode, 500, 925, thePostConditionConstraintNode, 567, 941 );

		String theNoteText = "A use case is a set of sequences of actions, including variants, that yield an observable result of value to an actor. In SysML we can capture use case steps using a simplified Activity Diagram (similar to a flow chart).\r\n"
				+ "\r\n"
				+ "Tips:\r\n"
				+ "\r\n"
				+ "1. Start with a sunny day scenario. Typically, a trigger from an actor will initiate the use case. We can capture interactions with Actors as Accept/Send Events/Actions and capture things the system does as text-based Actions. \r\n"
				+ "\r\n"
				+ "2. Once we have a sunny day scenario, we can capture the rainy days, i.e. what happens when things go wrong (alternative or exception flows). We can capture alternate flows using decision nodes. If necessary, precede the decision node with an action to state the check. \r\n"
				+ "\r\n"
				+ "3. As a convention use Guard conditions on control flows to capture conditions causing a flow to be taken, e.g., [vehicle is moving].\r\n"
				+ "\r\n"
				+ "4. Interruptible regions with Accept Events can also be useful, e.g., to show an interrupt that terminates a set of actions, together with the resulting flow to an action outside.\r\n"
				+ "\r\n"
				+ "5.  Verify the use case steps with stakeholders. Is this what you expect?\r\n"
				+ "\r\n"
				+ "6. Once verified steps, create requirements for each step and trace from the action to the requirement, e.g., using a «derive» dependency.\r\n"
				+ "\r\n"
				+ "7. Rules have been relaxed on this diagram to allow free-flowing text. Take care not to move diagram into an area of the model that does not allow this.\r\n"
				+ "\r\n"
				+ "8. The right-click menu provides a mechanism to check the diagram for traceability completeness. This will also rename the actions in the browser. Run this periodically.\r\n"
				+ "\r\n"
				+ "9. A menu command is also provided to move the requirements you create into the RequirementsPkg, ready for Gateway sync with DOORS\r\n"
				+ "\r\n"
				+ "10. Delete this note, if you want!\r\n";

		// 4,32,32,204,32,204,998,32,998
		IRPGraphNode theNoteNode =
				theAD.addNewNodeByType( 
						"Note", 32, 32, 172, 966 );

		theNoteNode.setGraphicalProperty( 
				"TextDisplayMode",
				"Specification" );

		theNoteNode.setGraphicalProperty(
				"Text",
				theNoteText );

		theNoteNode.setGraphicalProperty( 
				"TextFontName",
				"Tahoma" );

		theNoteNode.setGraphicalProperty( 
				"TextFontSize",
				"8" );
	}

	private void setScopeOfTableToOwningPackageIfOwnerIs(
			String[] theUserDefinedMetaClasses,
			IRPTableView theView,
			boolean isRename ){

		IRPModelElement theOwner = theView.getOwner();

		if( theOwner instanceof IRPPackage &&
				Arrays.stream( theUserDefinedMetaClasses ).
				anyMatch(theOwner.getUserDefinedMetaClass()::contains ) ){

			IRPCollection theScopedEls = _context.createNewCollection();
			theScopedEls.setSize( 1 );
			theScopedEls.addItem( theOwner );

			_context.debug( "ExecutableMBSE_RPApplicationListener has set scope of " + 
					_context.elInfo( theView ) + " to " + _context.elInfo( theOwner ) );

			theView.setScope( theScopedEls );

			if( isRename ){

				String theProposedName = _context.TABLE_VIEW_PREFIX + " " + theOwner.getName();
				String theUniqueName = _context.determineUniqueNameBasedOn( theProposedName, "TableView", theOwner );

				theView.setName( theUniqueName );
			}
		}
	}

	private void afterAddForBlockOrRequirementDiagram(
			IRPObjectModelDiagram theDiagram ){

		IRPModelElement theOwner = theDiagram.getOwner();

		if( theOwner instanceof IRPClassifier ){

			// The resizer will deal with width and height, hence just use 50,50 to start with
			IRPGraphNode theGraphNode = theDiagram.addNewNodeForElement( theOwner, 450, 150, 50, 50 );
			GraphNodeResizer theResizer = new GraphNodeResizer( theGraphNode, _context );
			theResizer.performResizing();
		}		    

		_context.setDiagramNameToOwningClassifierIfAppropriate( theDiagram, theOwner );
	}

	private void afterAddForInternalBlockDiagram(
			IRPStructureDiagram theDiagram ){

		IRPModelElement theOwner = theDiagram.getOwner();    
		_context.setDiagramNameToOwningClassifierIfAppropriate( theDiagram, theOwner );
	}

	private void afterAddForFlowOutput(
			IRPSysMLPort theSysMLPort ){

		_context.setPortDirectionFor( theSysMLPort, "Out", "Untyped" );
	}

	private void afterAddForGuardedFlowOutput(
			IRPSysMLPort theSysMLPort ){

		_context.setPortDirectionFor( theSysMLPort, "Out", "Untyped" );

		int count = 0;

		String theOriginalName = theSysMLPort.getName();
		String theUniqueName = "[" + theOriginalName + "]";

		while( !_context.isElementNameUnique(
				theUniqueName, "SysMLPort", theSysMLPort.getOwner(), 1 ) ){

			count++;
			theUniqueName = "[" + theOriginalName + count + "]";
		}

		try {
			theSysMLPort.setName( theUniqueName );

		} catch( Exception e ){
			_context.warning( "Unable to set name to " + theUniqueName + 
					" for " + _context.elInfo( theSysMLPort ) + 
					" owned by " + _context.elInfo( theSysMLPort.getOwner() ) );
			_context.warning( "Try setting the General::Model::NamesRegExp property to ^<(.*)$" );
		}
	}

	private void afterAddForFlowInput(
			IRPSysMLPort theSysMLPort ){

		_context.setPortDirectionFor( theSysMLPort, "In", "Untyped" );
	}

	private void afterAddForCallOperation(
			IRPCallOperation theCallOp ){

		// only do move if property is set
		boolean isEnabled = _context.getIsCallOperationSupportEnabled();

		if( isEnabled ){

			IRPInterfaceItem theOp = theCallOp.getOperation();

			IRPDiagram theDiagram = _context.get_rhpApp().getDiagramOfSelectedElement();

			if( theDiagram != null ){ 

				CreateOperationPanel.launchThePanel( _context.get_rhpAppID() );

			} // else probably drag from browser

			if( theOp != null ){

				// Use the operation name for the COA if possible
				try {
					String theProposedName = 
							_context.determineUniqueNameBasedOn( 
									_context.toMethodName( theOp.getName(), 40 ), 
									"CallOperation", 
									theCallOp.getOwner() );

					theCallOp.setName( theProposedName );

				} catch( Exception e ) {
					_context.error( "Cannot rename Call Operation to match Operation" );
				}

				// If the operation has an Activity Diagram under it, then populate an RTF 
				// description with a link to the lower diagram
				IRPFlowchart theAD = theOp.getActivityDiagram();

				if( theAD != null ){

					IRPActivityDiagram theFC = theAD.getFlowchartDiagram();

					if( theFC != null ){
						_context.debug( "Creating Hyperlinks in Description of COA" );

						IRPCollection targets = _context.get_rhpApp().createNewCollection();

						targets.setSize( 2 );
						targets.setModelElement( 1, theOp );
						targets.setModelElement( 2, theFC );

						String rtfText = "{\\rtf1\\fbidis\\ansi\\ansicpg1255\\deff0\\deflang1037{\\fonttbl{\\f0\\fnil\\fcharset0 Arial;}}\n{\\colortbl;\\red0\\green0\\blue255;}\n\\viewkind4\\uc1 " + 
								"\\pard\\ltrpar\\qc\\fs18 Function: \\cf1\\ul\\protect " + theOp.getName() + "\\cf0\\ulnone\\protect0\\par" + 
								"\\pard\\ltrpar\\qc\\fs18 Decomposed by: \\cf1\\ul\\protect " + theFC.getName() + "\\cf0\\ulnone\\protect0\\par\n}";

						theCallOp.setDescriptionAndHyperlinks( rtfText, targets );
					}
				}
			}
		}
	}

	private void afterAddForDeriveRequirement(
			IRPDependency theDependency ){

		IRPModelElement theDependent = theDependency.getDependent();

		if( theDependent instanceof IRPRequirement ){

			IRPStereotype theExistingGatewayStereotype = 
					_context.getStereotypeAppliedTo(
							theDependent, 
							"from.*" );

			if( theExistingGatewayStereotype != null ){

				theDependency.setStereotype( theExistingGatewayStereotype );
				theDependency.changeTo( "Derive Requirement" );
			}			
		}
	}

	private void afterAddForInterface(
			IRPModelElement modelElement ){

		// only do move if property is set
		boolean isEnabled = _context.getIsEnableAutoMoveOfInterfaces( modelElement );

		if( isEnabled ){
			ElementMover theElementMover = new ElementMover( 
					modelElement, 
					_context.getInterfacesPackageStereotype( modelElement ),
					_context );

			theElementMover.performMove( modelElement );
		}
	}

	private void afterAddForEvent(
			IRPModelElement modelElement ){

		_context.debug( "afterAddForEvent invoked for " + _context.elInfo( modelElement ) );

		// only do move if property is set
		boolean isEnabled = _context.getIsEnableAutoMoveOfEventsOnAddNewElement();

		if( isEnabled ){

			ElementMover theExternalSignalsPackageMover = new ElementMover( 
					modelElement, 
					_context.REQTS_ANALYSIS_EXTERNAL_SIGNALS_PACKAGE,
					_context );

			ElementMover theSubsystemInterfacesPackageMover = new ElementMover( 
					modelElement, 
					_context.DESIGN_SYNTHESIS_SUBSYSTEM_INTERFACES_PACKAGE,
					_context );

			ElementMover theChosenMover = null;

			if( theExternalSignalsPackageMover.isMovePossible() &&
					!theSubsystemInterfacesPackageMover.isMovePossible() ){

				theChosenMover = theExternalSignalsPackageMover;

			} else if( !theExternalSignalsPackageMover.isMovePossible() &&
					theSubsystemInterfacesPackageMover.isMovePossible() ){

				theChosenMover = theSubsystemInterfacesPackageMover;

			} else if( theExternalSignalsPackageMover.isMovePossible() &&
					theSubsystemInterfacesPackageMover.isMovePossible() ){

				_context.warning( "Package hierarchy has dependencies on both an external signals package and a subsystem interfaces package hence cannot decide. It is recommended to delete one of the dependencies in the hierarchy");
			}	

			if( theChosenMover != null ){

				IRPModelElement moveToPkg = theChosenMover.get_newOwner();

				// This delay + the save should mean that Event won't crash Rhapsody
				boolean isContinue = UserInterfaceHelper.askAQuestion( 
						"Move " + modelElement.getUserDefinedMetaClass() + " to " + 
								moveToPkg.getName() + "?" );

				if( isContinue ){

					theChosenMover.performMove( modelElement );

					try {
						_context.get_rhpPrj().save();

					} catch( Exception e ){
						_context.error( "Exception on save in afterAddForEvent, e=" + e.getMessage() );
					}
				}
			}
		}
	}

	private void afterAddForActorUsage(
			IRPInstance modelElement ){

		IRPPackage theOwningPackage = _context.getOwningPackageFor( modelElement );

		List<IRPModelElement> existingActors = _context.getExistingElementsBasedOn( 
				theOwningPackage, _context.NEW_TERM_FOR_ACTOR_PACKAGE, "Actor" );

		if( !existingActors.isEmpty() ){

			List<IRPModelElement> elsToChooseFrom = new ArrayList<>();
			elsToChooseFrom.addAll( existingActors );

			List<IRPModelElement> existingActorUsages = 
					getExistingGlobalObjectsBasedOn( theOwningPackage, _context.NEW_TERM_FOR_ACTOR_USAGE );

			existingActorUsages.remove( modelElement ); // Don't include this element

			elsToChooseFrom.addAll( existingActorUsages );

			IRPModelElement theSelectedElement =
					UserInterfaceHelper.launchDialogToSelectElement( 
							elsToChooseFrom, "Select existing actor or actor usage", true );

			if( theSelectedElement instanceof IRPInstance ){

				switchGraphNodeFor( modelElement, (IRPInstance) theSelectedElement );

			} else if( theSelectedElement instanceof IRPClassifier ){

				modelElement.setOtherClass( (IRPClassifier) theSelectedElement );
			}
		}		
	}

	private void afterAddForSystemUsage(
			IRPInstance modelElement ){

		// Listener only operates on context diagram to avoid issues when drawing compositions on bdds
		if( _context.isElementOnlyOnOneDiagramWith( 
				_context.CONTEXT_DIAGRAM, modelElement ) ){

			IRPPackage theOwningPackage = _context.getOwningPackageFor( modelElement );

			List<IRPModelElement> elsToChooseFrom = 
					_context.getExistingElementsBasedOn( 
							theOwningPackage, 
							_context.NEW_TERM_FOR_SYSTEM_ARCHITECTURE_PACKAGE, 
							"Class", 
							_context.SYSTEM_BLOCK );

			elsToChooseFrom.addAll(
					getExistingGlobalObjectsBasedOn( 
							theOwningPackage, _context.NEW_TERM_FOR_SYSTEM_USAGE ) );

			elsToChooseFrom.remove( modelElement ); // Don't include this element

			if( !elsToChooseFrom.isEmpty() ){

				IRPModelElement theSelectedElement =
						UserInterfaceHelper.launchDialogToSelectElement( 
								elsToChooseFrom, "Select existing " + _context.SYSTEM_BLOCK + 
								" or " + _context.SYSTEM_USAGE, true );

				if( theSelectedElement instanceof IRPInstance ){

					switchGraphNodeFor( modelElement, (IRPInstance) theSelectedElement );

				} else if( theSelectedElement instanceof IRPClassifier ){

					modelElement.setOtherClass( (IRPClassifier) theSelectedElement );
				}
			}		
		}
	}

	private void afterAddForFunctionUsage(
			IRPInstance theInstance ){

		IRPGraphElement theGraphEl = _context.getGraphElIfOnlyOneExistsFor( theInstance );

		// Only do this for parts, not directed compositions
		if( theGraphEl instanceof IRPGraphNode ){

			_context.debug( "afterAddForFunctionUsage found a graphNode for " + _context.elInfo( theGraphEl.getModelObject() ) );

			CreateUsageBlockPanel.launchThePanel( 
					_context.FUNCTION_USAGE,
					_context.FUNCTION_BLOCK,
					_context.get_rhpAppID(), 
					theInstance.getGUID(), 
					theGraphEl.getDiagram().getGUID() );
		}
	}

	private void afterAddForParallelGateway(
			IRPInstance theInstance ){

		IRPGraphElement theGraphEl = _context.getGraphElIfOnlyOneExistsFor( theInstance );		

		// Only do this for parts, not directed compositions
		if( theGraphEl instanceof IRPGraphNode ){

			GraphNodeResizer theResizer = new GraphNodeResizer( (IRPGraphNode) theGraphEl, _context);
			theResizer.performResizing();
		}
	}

	private void afterAddForStartUsage(
			IRPInstance theInstance ){

		IRPGraphElement theGraphEl = _context.getGraphElIfOnlyOneExistsFor( theInstance );		

		// Only do this for parts, not directed compositions
		if( theGraphEl instanceof IRPGraphNode ){

			GraphNodeResizer theResizer = new GraphNodeResizer( (IRPGraphNode) theGraphEl, _context);
			theResizer.performResizing();

			GraphNodeInfo theNodeInfo = new GraphNodeInfo( (IRPGraphNode) theGraphEl, _context );

			_context.debug( "afterAddForStartUsage found a graphNode for " + _context.elInfo( theGraphEl.getModelObject() ) );

			IRPSysMLPort thePort = (IRPSysMLPort) theInstance.addNewAggr( "SysMLPort", "" );
			thePort.changeTo( _context.FLOW_OUTPUT );	
			_context.setPortDirectionFor( thePort, "Out", "Untyped" );
			IRPGraphElement thePortGraphEl = _context.getGraphElIfOnlyOneExistsFor( thePort );
			thePortGraphEl.setGraphicalProperty( "Position", theNodeInfo.getTopRightX() + "," + theNodeInfo.getMiddleY() );
		}
	}

	private void afterAddForFinalUsage(
			IRPInstance theInstance ){

		IRPGraphElement theGraphEl = _context.getGraphElIfOnlyOneExistsFor( theInstance );		

		// Only do this for parts, not directed compositions
		if( theGraphEl instanceof IRPGraphNode ){

			GraphNodeResizer theResizer = new GraphNodeResizer( (IRPGraphNode) theGraphEl, _context);
			theResizer.performResizing();

			GraphNodeInfo theNodeInfo = new GraphNodeInfo( (IRPGraphNode) theGraphEl, _context );

			_context.debug( "afterAddForFinalUsage found a graphNode for " + _context.elInfo( theGraphEl.getModelObject() ) );

			IRPSysMLPort thePort = (IRPSysMLPort) theInstance.addNewAggr( "SysMLPort", "" );
			thePort.changeTo( _context.FLOW_INPUT );	
			_context.setPortDirectionFor( thePort, "In", "Untyped" );
			IRPGraphElement thePortGraphEl = _context.getGraphElIfOnlyOneExistsFor( thePort );
			thePortGraphEl.setGraphicalProperty( "Position", theNodeInfo.getTopLeftX() + "," + theNodeInfo.getMiddleY() );
		}
	}


	private void afterAddForTimeEventUsage(
			IRPInstance theInstance ){

		IRPGraphElement theGraphEl = _context.getGraphElIfOnlyOneExistsFor( theInstance );		

		// Only do this for parts, not directed compositions
		if( theGraphEl instanceof IRPGraphNode ){

			GraphNodeResizer theResizer = new GraphNodeResizer( (IRPGraphNode) theGraphEl, _context);
			theResizer.performResizing();

			GraphNodeInfo theNodeInfo = new GraphNodeInfo( (IRPGraphNode) theGraphEl, _context );

			_context.debug( "afterAddForTimeEventUsage found a graphNode for " + _context.elInfo( theGraphEl.getModelObject() ) );

			theInstance.setDisplayName( "<TBD> secs" );

			IRPSysMLPort thePort = (IRPSysMLPort) theInstance.addNewAggr( "SysMLPort", "" );
			thePort.changeTo( _context.FLOW_OUTPUT );	
			_context.setPortDirectionFor( thePort, "Out", "Untyped" );
			IRPGraphElement thePortGraphEl = _context.getGraphElIfOnlyOneExistsFor( thePort );
			thePortGraphEl.setGraphicalProperty( "Position", theNodeInfo.getTopRightX() + "," + theNodeInfo.getMiddleY() );
		}
	}

	private void afterAddForDataObjectUsage(
			IRPInstance theInstance ){

		IRPGraphElement theGraphEl = _context.getGraphElIfOnlyOneExistsFor( theInstance );		

		// Only do this for parts, not directed compositions
		if( theGraphEl instanceof IRPGraphNode ){

			GraphNodeResizer theResizer = new GraphNodeResizer( (IRPGraphNode) theGraphEl, _context);
			theResizer.performResizing();

			CreateUsageBlockPanel.launchThePanel( 
					_context.DATA_OBJECT,
					_context.ITEM_BLOCK,
					_context.get_rhpAppID(), 
					theInstance.getGUID(), 
					theGraphEl.getDiagram().getGUID() );
		}
	}

	private void afterAddForFlowFinalUsage(
			IRPInstance theInstance ){

		IRPGraphElement theGraphEl = _context.getGraphElIfOnlyOneExistsFor( theInstance );		

		// Only do this for parts, not directed compositions
		if( theGraphEl instanceof IRPGraphNode ){

			GraphNodeResizer theResizer = new GraphNodeResizer( (IRPGraphNode) theGraphEl, _context);
			theResizer.performResizing();

			GraphNodeInfo theNodeInfo = new GraphNodeInfo( (IRPGraphNode) theGraphEl, _context );

			_context.debug( "afterAddForFlowFinalUsage found a graphNode for " + _context.elInfo( theGraphEl.getModelObject() ) );

			IRPSysMLPort theOutPort = (IRPSysMLPort) theInstance.addNewAggr( "SysMLPort", "" );
			theOutPort.changeTo( _context.FLOW_INPUT );	
			_context.setPortDirectionFor( theOutPort, "In", "Untyped" );
			IRPGraphElement thePortGraphEl = _context.getGraphElIfOnlyOneExistsFor( theOutPort );
			thePortGraphEl.setGraphicalProperty( "Position", theNodeInfo.getTopLeftX() + "," + theNodeInfo.getMiddleY() );
		}
	}

	private void afterAddForDecisionUsage(
			IRPInstance theInstance ){

		IRPGraphElement theGraphEl = _context.getGraphElIfOnlyOneExistsFor( theInstance );		

		// Only do this for parts, not directed compositions
		if( theGraphEl instanceof IRPGraphNode ){

			GraphNodeResizer theResizer = new GraphNodeResizer( (IRPGraphNode) theGraphEl, _context);
			theResizer.performResizing();

			GraphNodeInfo theDecisionNodeInfo = new GraphNodeInfo( (IRPGraphNode) theGraphEl, _context );

			_context.debug( "afterAddForDecisionUsage found a graphNode for " + _context.elInfo( theGraphEl.getModelObject() ) );

			IRPSysMLPort theInPort = (IRPSysMLPort) theInstance.addNewAggr("SysMLPort", "");
			theInPort.changeTo( _context.FLOW_INPUT );	
			_context.setPortDirectionFor( theInPort, "In", "Untyped" );	
			IRPGraphElement theInPortGraphEl = _context.getGraphElIfOnlyOneExistsFor( theInPort );
			//_context.dumpGraphicalPropertiesFor( theInPortGraphEl );
			theInPortGraphEl.setGraphicalProperty( "Position", theDecisionNodeInfo.getTopLeftX() + "," + theDecisionNodeInfo.getMiddleY() );
			theInPortGraphEl.setGraphicalProperty( "ShowName", "None" );

			IRPSysMLPort theCondPort = (IRPSysMLPort) theInstance.addNewAggr("SysMLPort", "[put condition here]");
			theCondPort.changeTo( _context.GUARDED_FLOW_OUTPUT );	
			_context.setPortDirectionFor( theCondPort, "Out", "Untyped" );
			IRPGraphElement theCondPortGraphEl = _context.getGraphElIfOnlyOneExistsFor( theCondPort );
			theCondPortGraphEl.setGraphicalProperty( "Position", theDecisionNodeInfo.getTopRightX() + "," + theDecisionNodeInfo.getMiddleY() );

			IRPSysMLPort theElsePort = (IRPSysMLPort) theInstance.addNewAggr("SysMLPort", "[else]");
			theElsePort.changeTo( _context.GUARDED_FLOW_OUTPUT );	
			_context.setPortDirectionFor( theElsePort, "Out", "Untyped" );
			IRPGraphElement theElsePortGraphEl = _context.getGraphElIfOnlyOneExistsFor( theElsePort );
			theElsePortGraphEl.setGraphicalProperty( "Position", theDecisionNodeInfo.getMiddleX() + "," + theDecisionNodeInfo.getBottomLeftY() );
		}
	}

	private void switchGraphNodeFor(
			IRPInstance modelElement,
			IRPInstance toTheElement ) {

		IRPGraphElement theGraphEl = _context.getGraphElIfOnlyOneExistsFor( modelElement );

		if( theGraphEl instanceof IRPGraphNode ){

			IRPDiagram theDiagram = theGraphEl.getDiagram();

			_context.debug( "switchGraphNodeFor " + _context.elInfo( modelElement ) + 
					" to " + _context.elInfo( toTheElement ) + " on " + _context.elInfo( theDiagram ) );

			IRPGraphNode theNewNode = null;

			GraphNodeInfo theNodeInfo;

			try {
				theNodeInfo = new GraphNodeInfo( (IRPGraphNode) theGraphEl, _context );

				_context.debug( "Adding node for " + _context.elInfo( toTheElement ) ); 

				theNewNode = theDiagram.addNewNodeForElement(
						toTheElement, 
						theNodeInfo.getTopLeftX()+5, 
						theNodeInfo.getTopLeftY()+5, 
						theNodeInfo.getWidth(), 
						theNodeInfo.getHeight() );

				String theStructuredViewPropertyValue =
						theGraphEl.getGraphicalProperty( "StructureView" ).getValue();

				theNewNode.setGraphicalProperty( "StructureView", theStructuredViewPropertyValue );

			} catch( Exception e ){

				_context.error( "Exception in switchGraphNodeFor when switching " + 
						_context.elInfo( theGraphEl.getModelObject() ) + 
						" to " + _context.elInfo( toTheElement ) );

				e.printStackTrace();
			}

			if( theNewNode instanceof IRPGraphNode ){

				modelElement.deleteFromProject();
				toTheElement.highLightElement();
			}
		}
	}

	private List<IRPModelElement> getExistingGlobalObjectsBasedOn(
			IRPPackage theOwningPackage,
			String andNewTerm ){

		List<IRPModelElement> existingActorUsages = new ArrayList<>();

		if( theOwningPackage != null ){

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theCandidates =
			theOwningPackage.getGlobalObjects().toList();

			for( IRPModelElement theCandidate : theCandidates ){

				if( _context.hasStereotypeCalled( andNewTerm, theCandidate ) ){
					existingActorUsages.add( theCandidate );
				}
			}
		}

		return existingActorUsages;
	}

	private IRPClassifier getParentFunctionBlockFor(
			IRPModelElement theEl ){

		IRPClassifier theParentFunctionBlock = null;
		String theUserDefinedMetaClass = theEl.getUserDefinedMetaClass();

		//_context.info( "getParentFunctionBlockFor checking " + _context.elInfo( theEl ) + 
		//		" owned by " + _context.elInfo( theEl.getOwner() ) );

		if( theEl instanceof IRPClassifier && 
				( theUserDefinedMetaClass.equals( _context.FUNCTION_BLOCK ) || 
						theUserDefinedMetaClass.equals( _context.ITEM_BLOCK ) ) ){

			theParentFunctionBlock = (IRPClassifier) theEl;

		} else if( theEl != null && 
				!( theEl instanceof IRPProject ) ){

			theParentFunctionBlock = getParentFunctionBlockFor( theEl.getOwner() );
		} else {
			theParentFunctionBlock = null;
		}

		return theParentFunctionBlock;
	}

	private IRPDiagram getDiagramFor(
			IRPModelElement forEl ){

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theReferences = forEl.getReferences().toList();
		List<IRPDiagram> theDiagrams = new ArrayList<>();
		IRPDiagram theDiagram = null;

		for( IRPModelElement theReference : theReferences ){

			if( theReference instanceof IRPDiagram ){
				theDiagrams.add( (IRPDiagram) theReference );
			}
		}

		if( theDiagrams.size() == 1 ){
			theDiagram = theDiagrams.get( 0 );

		}

		return theDiagram;
	}

	@SuppressWarnings("unchecked")
	private void afterAddForRequirement(
			IRPModelElement modelElement ){

		// only do move if property is set
		boolean isEnabled = _context.getIsEnableAutoMoveOfRequirements();

		if( isEnabled ){

			boolean isKeepUnderFunctionBlock = _context.
					getIsKeepRequirementUnderFunctionBlock( modelElement );

			IRPDiagram theDiagram = getDiagramFor( modelElement );

			IRPClassifier theParentFunctionBlock = null;

			if( theDiagram != null ){
				theParentFunctionBlock = getParentFunctionBlockFor( theDiagram );
			} else {	
				theParentFunctionBlock = getParentFunctionBlockFor( modelElement );
			}

			RequirementMover theElementMover = new RequirementMover( 
					modelElement, _context.REQTS_ANALYSIS_REQUIREMENT_PACKAGE, _context );

			if( theElementMover.isMovePossible() ){

				if( theParentFunctionBlock != null ){

					if( isKeepUnderFunctionBlock ){
						theElementMover.set_newOwner( theParentFunctionBlock );
					}

					theElementMover.performMove( modelElement );

				} else {
					theElementMover.performMove( modelElement );
				}

			} else if( isKeepUnderFunctionBlock &&
					theParentFunctionBlock != null &&
					!modelElement.getOwner().equals( theParentFunctionBlock ) ){

				theElementMover.set_newOwner( theParentFunctionBlock );
				theElementMover.performMove( modelElement );
			}

			if( theParentFunctionBlock != null ){

				try {
					//_context.debug( "Adding satisfy dependency from " + 
					//		_context.elInfo( theParentFunctionBlock ) + " to " + 
					//		_context.elInfo( modelElement ) );

					IRPDependency theDependency = theParentFunctionBlock.addDependencyTo( modelElement );
					theDependency.setStereotype( _context.getStereotypeForSatisfaction() );

				} catch( Exception e ){
					_context.info( "Unable to apply satisfy dependency from " + 
							_context.elInfo( theParentFunctionBlock ) + " to " + 
							_context.elInfo( modelElement ) + ", e=" + e.getMessage() );
				}

				if( theDiagram != null ){

					IRPCollection theCollection = _context.createNewCollection();

					List<IRPGraphElement> theGraphEls = 
							theDiagram.getCorrespondingGraphicElements( theParentFunctionBlock ).toList();

					theGraphEls.addAll( 
							theDiagram.getCorrespondingGraphicElements( modelElement ).toList() );

					if( theGraphEls.size() == 2 ){

						for( IRPGraphElement theGraphEl : theGraphEls ){
							theCollection.addGraphicalItem( theGraphEl );
						}

						theDiagram.completeRelations( theCollection, 0 );
					}
				}
			}
		}
	}

	private void afterAddForFlow(
			IRPFlow modelElement ){

		IRPPackage theOwningPkg = _context.getOwningPackageFor( modelElement );

		if( theOwningPkg != null &&
				!modelElement.getOwner().equals( theOwningPkg ) ){

			try {
				modelElement.setOwner( theOwningPkg );	

			} catch( Exception e ){
				_context.warning( "Exception in afterAddForFlow trying to move " + 
						_context.elInfo( modelElement ) + 
						" owned by " + _context.elInfo( modelElement.getOwner() ) + 
						" to " + _context.elInfo( theOwningPkg ) + ", e=" + e.getMessage() );
			}
		}

		if( _context.isElementOnlyOnOneDiagramWith( 
				_context.CONTEXT_DIAGRAM, modelElement ) ) {

			CreateEventForFlowPanel.launchThePanel( _context.get_rhpAppID() );
		}
	}

	private void afterAddForFlowConnector(
			IRPLink theLink ){

		IRPSysMLPort fromSysMLPort = theLink.getFromSysMLPort();
		IRPInstance fromInstance = theLink.getFrom();

		IRPSysMLPort toSysMLPort = theLink.getToSysMLPort();
		IRPInstance toInstance = theLink.getTo();

		//String fromMetaClass = ( ( theLink.getFrom() == null ) ? "null from" : theLink.getFrom().getUserDefinedMetaClass() );
		//String toMetaClass = ( ( theLink.getTo() == null ) ? "null to" : theLink.getTo().getUserDefinedMetaClass() );

		IRPClassifier fromClassifierEl = theLink.getFrom().getOtherClass();	
		IRPClassifier toClassifierEl = theLink.getTo().getOtherClass();

		boolean isFromPortCreationNeeded;
		boolean isToPortCreationNeeded;
		IRPClassifier toPortType;
		IRPClassifier fromPortType;

		boolean isFromPortTypeNeeded = false;
		boolean isToPortTypeNeeded = false;

		if( toSysMLPort instanceof IRPSysMLPort ){
			toPortType = toSysMLPort.getType();
		} else if( toInstance instanceof IRPSysMLPort ){
			toSysMLPort = (IRPSysMLPort) toInstance;
			toPortType = toSysMLPort.getType();
		} else {
			toPortType = null;
		}

		if( fromSysMLPort instanceof IRPSysMLPort ){
			fromPortType = fromSysMLPort.getType();
		} else if( fromInstance instanceof IRPSysMLPort ){
			fromSysMLPort = (IRPSysMLPort) fromInstance;
			fromPortType = fromSysMLPort.getType();
		} else {
			fromPortType = null;
		}

		if( toSysMLPort instanceof IRPSysMLPort ){
			isToPortCreationNeeded = false;

			if( toPortType != null && 
					fromPortType == null ){

				isFromPortTypeNeeded = true;
				fromPortType = toPortType;
			}

		} else {
			isToPortCreationNeeded = true;

			if( fromPortType != null ){
				isToPortTypeNeeded = true;
				toPortType = fromPortType;
			}
		}

		if( fromSysMLPort instanceof IRPSysMLPort ){
			isFromPortCreationNeeded = false;

			if( fromPortType != null &&
					toPortType == null ){

				isToPortCreationNeeded = true;
				toPortType = fromPortType;
			}

		} else {
			isFromPortCreationNeeded = true;

			if( toPortType != null ){
				isFromPortTypeNeeded = true;
				fromPortType = toPortType;
			}
		}

		if( isFromPortCreationNeeded || 
				isToPortCreationNeeded ||
				isFromPortTypeNeeded ||
				isToPortTypeNeeded ){

			boolean isContinue = false;

			String autoGenPolicy = _context.getAutoGenerationOfPortsForLinksPolicy( 
					_context.get_rhpPrj() );

			if( autoGenPolicy.equals( "Always" ) ){

				isContinue = true;

			} else if( autoGenPolicy.equals( "UserDialog" ) ){

				String msg = "You have drawn a " + _context.FLOW_CONNECTOR + " \n";

				if ( isFromPortCreationNeeded && isToPortCreationNeeded ){
					msg = "Do you want to automatically create " + _context.FLOW_OUTPUT + " and " +
							_context.FLOW_INPUT + " ports?";
				} else if( isFromPortCreationNeeded ){
					msg = "Do you want to automatically create a " + _context.FLOW_OUTPUT + " port?";
				} else if( isToPortCreationNeeded ){
					msg = "Do you want to automatically create a " + _context.FLOW_INPUT + " port?";
				}

				isContinue = UserInterfaceHelper.askAQuestion( msg );
			}

			if( isContinue ){

				IRPGraphEdge theGraphEdge = _context.getCorrespondingGraphEdgeFor( theLink );
				IRPDiagram theDiagram = theGraphEdge.getDiagram();
				GraphEdgeInfo theGraphEdgeInfo = new GraphEdgeInfo( theGraphEdge, _context ); 

				IRPGraphNode fromPortNode = null;

				if( isFromPortCreationNeeded ){

					String fromPortName = _context.determineUniqueNameBasedOn( 
							"out", 
							"SysMLPort", 
							fromClassifierEl );					

					fromSysMLPort = (IRPSysMLPort) fromClassifierEl.addNewAggr( "SysMLPort", fromPortName );
					_context.setPortDirectionFor( fromSysMLPort, "Out", "Untyped" );
					fromSysMLPort.changeTo( _context.FLOW_OUTPUT );

					fromPortNode = _context.addGraphNodeFor( 
							fromSysMLPort, theDiagram, theGraphEdgeInfo.getStartX(), theGraphEdgeInfo.getStartY() );
				} else {
					fromPortNode = (IRPGraphNode) theGraphEdge.getSource();
				}

				IRPGraphNode toPortNode = null;

				if( isToPortCreationNeeded ){

					String toPortName = _context.determineUniqueNameBasedOn( 
							"in",
							"SysMLPort", 
							toClassifierEl );

					toSysMLPort = (IRPSysMLPort) toClassifierEl.addNewAggr( "SysMLPort", toPortName );		
					_context.setPortDirectionFor( toSysMLPort, "In", "Untyped" );
					toSysMLPort.changeTo( _context.FLOW_INPUT );

					toPortNode = _context.addGraphNodeFor( 
							toSysMLPort, theDiagram, theGraphEdgeInfo.getEndX(), theGraphEdgeInfo.getEndY() );					
				} else {
					toPortNode = (IRPGraphNode) theGraphEdge.getTarget();
				}

				if( isToPortTypeNeeded ){
					toSysMLPort.setType( toPortType );
				}

				if( isFromPortTypeNeeded ){
					fromSysMLPort.setType( fromPortType );
				}				

				try {
					IRPLink newLink;

					if( fromClassifierEl == null &&
							fromInstance instanceof IRPSysMLPort ){
						fromClassifierEl = (IRPClassifier) fromInstance.getOwner();

						// delegated link from port on boundary of frame
						newLink = ((IRPClass) fromClassifierEl).addLinkToPartViaPort( toInstance, toSysMLPort, fromSysMLPort, null );

					} else {
						newLink = theLink.getFrom().addLinkToElement( theLink.getTo(), null, fromSysMLPort, toSysMLPort );
					}

					theDiagram.addNewEdgeForElement( 
							newLink, 
							fromPortNode, 
							theGraphEdgeInfo.getStartX(), 
							theGraphEdgeInfo.getStartY(), 
							toPortNode, 
							theGraphEdgeInfo.getEndX(), 
							theGraphEdgeInfo.getEndY() );

					newLink.changeTo( _context.FLOW_CONNECTOR );

					theLink.deleteFromProject();

					if( fromSysMLPort instanceof IRPSysMLPort &&
							fromSysMLPort.getOwner().getUserDefinedMetaClass().equals( _context.ITEM_BLOCK ) &&
							fromSysMLPort.getType().getName().equals( "Untyped" ) &&
							toSysMLPort instanceof IRPSysMLPort &&
							toSysMLPort.getType().getName().equals( "Untyped" ) ) {

						IRPClassifier theItemBlock = (IRPClassifier) fromSysMLPort.getOwner();

						_context.info( "Setting " + _context.elInfo( fromSysMLPort ) + " to " + _context.elInfo( theItemBlock ) );
						fromSysMLPort.setType( theItemBlock );

						_context.info( "Setting " + _context.elInfo( toSysMLPort ) + " to " + _context.elInfo( theItemBlock ) );
						toSysMLPort.setType( theItemBlock );

					} else if( toSysMLPort instanceof IRPSysMLPort &&
							toSysMLPort.getOwner().getUserDefinedMetaClass().equals( _context.ITEM_BLOCK ) &&
							toSysMLPort.getType().getName().equals( "Untyped" ) &&
							fromSysMLPort instanceof IRPSysMLPort &&
							fromSysMLPort.getType().getName().equals( "Untyped" ) ) {

						IRPClassifier theItemBlock = (IRPClassifier) toSysMLPort.getOwner();

						_context.info( "Setting " + _context.elInfo( fromSysMLPort ) + " to " + _context.elInfo( theItemBlock ) );
						fromSysMLPort.setType( theItemBlock );

						_context.info( "Setting " + _context.elInfo( toSysMLPort ) + " to " + _context.elInfo( theItemBlock ) );
						toSysMLPort.setType( theItemBlock );

					} else if( isToPortCreationNeeded && 
							isFromPortCreationNeeded ){

						// Only launch the create event dialog if it was a non-port to non-port connector that was drawn
						CreateEventForFlowConnectorPanel.launchThePanel( 
								_context.get_rhpAppID(), 
								newLink.getGUID(),
								theDiagram.getGUID() );			
					}

				} catch( Exception e ){

					String msg = e.getMessage();
					_context.error( "Exception trying to re-create link in afterAddForFlowConnector, e=" + msg );
				}
			}
		}
	}

	private void afterAddForLink(
			IRPLink theLink ){

		if( _context.isOwnedUnderPackageHierarchy( 
				_context.FUNCT_ANALYSIS_SCENARIOS_PACKAGE, theLink ) ){

			String fromUserDefinedMetaClass = ( ( theLink.getFrom() == null ) ? "null from" : theLink.getFrom().getUserDefinedMetaClass() );
			String toUserDefinedMetaClass = ( ( theLink.getTo() == null ) ? "null to" : theLink.getTo().getUserDefinedMetaClass() );

			_context.debug( "afterAddForLink invoked for  " + _context.elInfo( theLink ) + 
					" owned by " + _context.elInfo( theLink.getOwner() ) );

			//_context.debug( "fromUserDefinedMetaClass is " + fromUserDefinedMetaClass );
			//_context.debug( "toUserDefinedMetaClass is " + toUserDefinedMetaClass );

			if( toUserDefinedMetaClass.equals( _context.OBJECT ) && 
					fromUserDefinedMetaClass.equals( _context.OBJECT ) ){

				IRPClassifier fromClassifier = theLink.getFrom().getOtherClass();
				IRPClassifier toClassifier = theLink.getTo().getOtherClass();

				if( fromClassifier instanceof IRPActor && 
						toClassifier instanceof IRPClass ){

					PortsForLinksCreator theCreator = new PortsForLinksCreator( _context, theLink );
					theCreator.createPortsBasedOnPropertyPolicies();

				} else if( toClassifier instanceof IRPActor && 
						fromClassifier instanceof IRPClass ){

					PortsForLinksCreator theCreator = new PortsForLinksCreator( _context, theLink );
					theCreator.createPortsBasedOnPropertyPolicies();
				}
			}

		} else if( _context.isOwnedUnderPackageHierarchy( 
				_context.DESIGN_SYNTHESIS_LOGICAL_SYSTEM_PACKAGE, theLink ) ||
				_context.isOwnedUnderPackageHierarchy( 
						_context.FUNCT_ANALYSIS_FEATURE_FUNCTION_PACKAGE, theLink ) ){

			String fromUserDefinedMetaClass = ( ( theLink.getFrom() == null ) ? "null from" : theLink.getFrom().getUserDefinedMetaClass() );
			String toUserDefinedMetaClass = ( ( theLink.getTo() == null ) ? "null to" : theLink.getTo().getUserDefinedMetaClass() );

			//_context.info( "fromUserDefinedMetaClass is " + fromUserDefinedMetaClass );
			//_context.info( "toUserDefinedMetaClass is " + toUserDefinedMetaClass );

			_context.debug( "afterAddForLink invoked for  " + _context.elInfo( theLink ) + 
					" owned by " + _context.elInfo( theLink.getOwner() ) );

			if( theLink.getFromPort() != null || 
					theLink.getToPort() != null || 
					theLink.getFromSysMLPort() != null || 
					theLink.getToSysMLPort() != null ){

				//_context.debug( "skip link due to ports for  " + _context.elInfo( theLink ) + 
				//		" owned by " + _context.elInfo( theLink.getOwner() ) );

			} else if( (theLink.getUserDefinedMetaClass().equals( 
					_context.FLOW_CONNECTOR ) &&
					fromUserDefinedMetaClass.equals( 
							_context.FUNCTION_USAGE ) &&
					toUserDefinedMetaClass.equals( 
							_context.FUNCTION_USAGE ) || 
					(theLink.getUserDefinedMetaClass().equals( 
							_context.FLOW_CONNECTOR ) &&
							fromUserDefinedMetaClass.equals( 
									_context.FUNCTION_BLOCK ) &&
							toUserDefinedMetaClass.equals( 
									_context.FUNCTION_BLOCK )) ) ){

				boolean isContinue = false;

				String autoGenPolicy = _context.getAutoGenerationOfPortsForLinksPolicy( 
						_context.get_rhpPrj() );

				if( autoGenPolicy.equals( "Always" ) ){

					isContinue = true;

				} else if( autoGenPolicy.equals( "UserDialog" ) ){

					isContinue = UserInterfaceHelper.askAQuestion(
							"You have drawn a connector between two " + 
									_context.FUNCTION_USAGE + "s.\n"+
									"Do you want to automatically create " + _context.FLOW_OUTPUT + " and " +
									_context.FLOW_INPUT + " ports?");
				}

				if( isContinue ){
					PortsForLinksCreator theCreator = new PortsForLinksCreator( _context, theLink );
					theCreator.autoCreateFlowPorts();
				}

				// Allow to work for subsystem => subsystem or actor (object) => system or 
				// system => actor (object)
			} else if( ( fromUserDefinedMetaClass.equals( 
					_context.SUBSYSTEM_USAGE ) &&
					toUserDefinedMetaClass.equals( 
							_context.SUBSYSTEM_USAGE ) ) ||
					( fromUserDefinedMetaClass.equals( 
							_context.SYSTEM_USAGE ) &&
							toUserDefinedMetaClass.equals( 
									_context.OBJECT ) ) ||
					( toUserDefinedMetaClass.equals( 
							_context.SYSTEM_USAGE ) &&
							fromUserDefinedMetaClass.equals( 
									_context.OBJECT ) )){

				PortsForLinksCreator theCreator = new PortsForLinksCreator( _context, theLink );
				theCreator.createPortsBasedOnPropertyPolicies();
			}
		}
	}

	@Override
	public boolean afterProjectClose(
			String bstrProjectName ){
		return false;
	}

	@Override
	public boolean onDoubleClick(
			IRPModelElement pModelElement ){

		boolean theReturn = false;

		try {	
			// Don't override double-click for packages to minimise issues with 
			// new users accidentally popping up the dialog 
			if( _context.getIsDoubleClickFunctionalityEnabled() &&
					!( pModelElement instanceof IRPPackage ) ){

				List<IRPModelElement> optionsList = null;

				if( pModelElement instanceof IRPCallOperation ){

					IRPCallOperation theCallOp = (IRPCallOperation)pModelElement;

					IRPInterfaceItem theInterfaceItem = theCallOp.getOperation();

					if( theInterfaceItem != null &&
							theInterfaceItem instanceof IRPOperation ){

						IRPOperation theOp = (IRPOperation)theInterfaceItem;

						optionsList = getDiagramsFor( theOp );
					}

				} else if( pModelElement instanceof IRPInstance ){

					IRPInstance thePart = (IRPInstance)pModelElement;

					IRPClassifier theClassifier = thePart.getOtherClass();

					if( theClassifier != null ){
						optionsList = getDiagramsFor( theClassifier );
					}
				}

				if (optionsList == null){
					optionsList = getDiagramsFor( pModelElement );
				}

				int numberOfDiagrams = optionsList.size();

				if( numberOfDiagrams > 0 ){

					theReturn = openNestedDiagramDialogFor( optionsList, pModelElement );

				} else if( pModelElement instanceof IRPUseCase ){

					String theUnadornedName = NestedActivityDiagram._prefix + pModelElement.getName();

					boolean theAnswer = UserInterfaceHelper.askAQuestion(
							"This use case has no nested text-based Activity Diagram.\n"+
									"Do you want to create one called '" + theUnadornedName + "'?");

					if( theAnswer == true ){

						_context.debug( "User chose to create a new activity diagram" );

						IRPFlowchart fc = ((IRPUseCase) pModelElement).addActivityDiagram();
						fc.changeTo( _context.NEW_TERM_FOR_TEXTUAL_ACTIVITY_DIAGRAM );
						fc.createGraphics();

						afterAddForTextualActivity( fc );

						if( fc != null ){

							fc.setIsAnalysisOnly( 1 ); // so that call op right-click parameter sync menus appear
							IRPStatechartDiagram theStatechart = fc.getStatechartDiagram();
							theStatechart.highLightElement();
							fc.setAsMainBehavior();
						}
					}

					theReturn = true; // don't launch the Features  window									

				} else {
					theReturn = false; // do default, i.e. open the features dialog
				}	
			}

		} catch( Exception e ){
			_context.error( "Unhandled exception in onDoubleClick(), e=" + e.getMessage() );			
		}

		return theReturn;
	}

	private static Set<IRPDiagram> getHyperLinkDiagramsFor(
			IRPModelElement theElement ){

		Set<IRPDiagram> theDiagrams = new HashSet<IRPDiagram>();

		@SuppressWarnings("unchecked")
		List<IRPHyperLink> theHyperLinks = theElement.getHyperLinks().toList();

		for (IRPHyperLink theHyperLink : theHyperLinks) {

			IRPModelElement theTarget = theHyperLink.getTarget();

			if (theTarget != null){

				if (theTarget instanceof IRPDiagram){
					theDiagrams.add( (IRPDiagram) theTarget );

				} else if (theTarget instanceof IRPFlowchart){
					IRPFlowchart theFlowchart = (IRPFlowchart)theTarget;
					theDiagrams.add( theFlowchart.getStatechartDiagram() );

				} else if (theTarget instanceof IRPStatechart){
					IRPStatechart theStatechart = (IRPStatechart)theTarget;
					theDiagrams.add( theStatechart.getStatechartDiagram() );			
				}
			}
		}

		return theDiagrams;
	}

	private static Set<IRPDiagram> getNestedDiagramsFor(
			IRPModelElement pModelElement) {

		Set<IRPDiagram> theDiagrams = new HashSet<IRPDiagram>();

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theNestedElements = pModelElement.getNestedElementsRecursive().toList();

		for (IRPModelElement theNestedElement : theNestedElements) {

			if (theNestedElement instanceof IRPDiagram){
				theDiagrams.add( (IRPDiagram) theNestedElement );
			}
		}

		return theDiagrams;
	}

	@Override
	public boolean onFeaturesOpen(IRPModelElement pModelElement) {
		return false;
	}

	@Override
	public boolean onSelectionChanged() {
		return false;
	}

	@Override
	public boolean beforeProjectClose(IRPProject pProject) {
		return false;
	}

	@Override
	public String getId() {
		return null;
	}

	@Override
	public boolean onDiagramOpen(IRPDiagram pDiagram) {
		return false;
	}

	private boolean openNestedDiagramDialogFor(
			List<IRPModelElement> theListOfDiagrams, 
			IRPModelElement relatedToModelEl) {

		boolean theReturn = false;

		try {		
			int numberOfDiagrams = theListOfDiagrams.size();

			if( numberOfDiagrams==1 ){

				IRPDiagram theDiagramToOpen = (IRPDiagram) theListOfDiagrams.get(0);

				if (theDiagramToOpen != null){

					String theType = theDiagramToOpen.getUserDefinedMetaClass();
					String theName = theDiagramToOpen.getName();

					if (theDiagramToOpen instanceof IRPFlowchart){

						theDiagramToOpen = (IRPDiagram) theDiagramToOpen.getOwner();


					} else if (theDiagramToOpen instanceof IRPActivityDiagram){

						theType = theDiagramToOpen.getOwner().getUserDefinedMetaClass();
						theName = theDiagramToOpen.getOwner().getName();	
					}

					JDialog.setDefaultLookAndFeelDecorated(true);

					int confirm = JOptionPane.showConfirmDialog(null, 
							"The " + relatedToModelEl.getUserDefinedMetaClass() + " called '" +
									relatedToModelEl.getName() + "' has an associated " + theType + "\n" +
									"called '" + theName + "'.\n\n" +
									"Do you want to open it? (Click 'No' to open the Features dialog instead)\n\n",
									"Confirm choice",
									JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

					if (confirm == JOptionPane.YES_OPTION){

						theDiagramToOpen.openDiagram();	
						theReturn = true; // don't launch the Features  window

					} else if (confirm == JOptionPane.NO_OPTION){

						theReturn = false; // open the features dialog

					} else { // Cancel

						theReturn = true; // don't open the features dialog
					}
				}

			} else if ( numberOfDiagrams>1 ){

				IRPModelElement theSelection = UserInterfaceHelper.launchDialogToSelectElement(
						theListOfDiagrams, 
						"The " + relatedToModelEl.getUserDefinedMetaClass() + " called '" +
								relatedToModelEl.getName() + "' has " + numberOfDiagrams + " associated diagrams.\n\n" +
								"Which one do you want to open? (Click 'Cancel' to open Features dialog instead)\n",
								true);

				if (theSelection != null && theSelection instanceof IRPDiagram){

					IRPDiagram theDiagramToOpen = (IRPDiagram) theSelection;
					theDiagramToOpen.openDiagram();
					theReturn = true; // don't launch the Features  window
				}
			}

		} catch (Exception e) {
			_context.error( "Unhandled exception in onDoubleClick()" );
		}

		return theReturn;
	}

	private List<IRPModelElement> getDiagramsFor(
			IRPModelElement theModelEl){

		Set<IRPDiagram> allDiagrams = new HashSet<IRPDiagram>();

		Set<IRPDiagram> theHyperLinkedDiagrams = getHyperLinkDiagramsFor(theModelEl);	
		allDiagrams.addAll(theHyperLinkedDiagrams);

		Set<IRPDiagram> theNestedDiagrams = getNestedDiagramsFor(theModelEl);
		allDiagrams.addAll(theNestedDiagrams);

		List<IRPModelElement> optionsList = new ArrayList<IRPModelElement>();
		optionsList.addAll( allDiagrams );

		return optionsList;
	}

	@Override
	public void finalize() throws Throwable {
		super.finalize();
	}
}

/**
 * Copyright (C) 2018-2023  MBSE Training and Consulting Limited (www.executablembse.com)

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