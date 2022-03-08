package com.mbsetraining.sysmlhelper.executablembse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import com.mbsetraining.sysmlhelper.common.ElementMover;
import com.mbsetraining.sysmlhelper.common.GraphEdgeInfo;
import com.mbsetraining.sysmlhelper.common.GraphNodeInfo;
import com.mbsetraining.sysmlhelper.common.NestedActivityDiagram;
import com.mbsetraining.sysmlhelper.common.RequirementMover;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.contextdiagram.CreateEventForFlowPanel;
import com.mbsetraining.sysmlhelper.tracedelementpanels.CreateOperationPanel;
import com.telelogic.rhapsody.core.*;

public class ExecutableMBSE_RPApplicationListener extends RPApplicationListener {

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

			} else if( modelElement instanceof IRPInstance && 
					_context.hasStereotypeCalled( "ActorUsage", modelElement )){

				afterAddForActorUsage( (IRPInstance) modelElement );

			} else if( modelElement instanceof IRPInstance && 
					_context.hasStereotypeCalled( "FunctionUsage", modelElement )){

				afterAddForFunctionUsage( (IRPInstance) modelElement );

			} else if( modelElement instanceof IRPInstance && 
					_context.hasStereotypeCalled( "DecisionUsage", modelElement )){

				afterAddForDecisionUsage( (IRPInstance) modelElement );
				
			} else if( theUserDefinedMetaClass.equals( "Object" ) ){
				
				IRPInstance theInstance = (IRPInstance)modelElement;
				
				IRPClassifier theOtherClass = theInstance.getOtherClass();
				
				if( theOtherClass.getMetaClass().equals( "Actor" ) ){
					modelElement.changeTo( _context.ACTOR_USAGE );
				}
				
			} else if( modelElement instanceof IRPFlow ){

				afterAddForFlow( (IRPFlow) modelElement );

			} else if( modelElement instanceof IRPLink ){

				afterAddForLink( (IRPLink) modelElement );

			} else if( theUserDefinedMetaClass.equals( _context.FLOW_INPUT ) ){

				afterAddForFlowInput( (IRPSysMLPort) modelElement );

			} else if( theUserDefinedMetaClass.equals( _context.FLOW_OUTPUT ) ){

				afterAddForFlowOutput( (IRPSysMLPort) modelElement );
				
			} else if( theUserDefinedMetaClass.equals( _context.GUARDED_FLOW_OUTPUT ) ){

				afterAddForGuardedFlowOutput( (IRPSysMLPort) modelElement );
			}

		} catch( Exception e ){
			_context.error( "ExecutableMBSE_RPApplicationListener.afterAddElement, " +
					" unhandled exception related to " + _context.elInfo( modelElement ) + 
					e.getMessage() );
		}

		return doDefault;
	}


	private void afterAddForFlowOutput(
			IRPSysMLPort theSysMLPort ){

		setPortDirectionFor( theSysMLPort, "Out", "Untyped" );
	}
	
	private void afterAddForGuardedFlowOutput(
			IRPSysMLPort theSysMLPort ){

		setPortDirectionFor( theSysMLPort, "Out", "Untyped" );
		
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

		setPortDirectionFor( theSysMLPort, "In", "Untyped" );
	}

	private void setPortDirectionFor(
			IRPSysMLPort theSysMLPort,
			String theDirection,
			String theTypeName ){

		IRPType theType = _context.get_rhpPrj().findType( theTypeName );

		if( theType == null ){

			_context.error( "pluginMethodForInputPort was unable to find the '" + 
					theTypeName + "' type to use for " + _context.elInfo( theSysMLPort ) );

		} else {

			theSysMLPort.setType( theType );
			theSysMLPort.setPortDirection( theDirection );
		}
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

				IRPModelElement moveToPkg = theChosenMover.get_moveToPkg();

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
				theOwningPackage, "ActorPackage", "Actor" );

		if( !existingActors.isEmpty() ){

			List<IRPModelElement> elsToChooseFrom = new ArrayList<>();
			elsToChooseFrom.addAll( existingActors );

			List<IRPModelElement> existingActorUsages = getExistingActorUsagesBasedOn( theOwningPackage );
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


	private void afterAddForFunctionUsage(
			IRPInstance theInstance ){

		IRPGraphElement theGraphEl = getGraphElFor( theInstance );

		// Only do this for parts, not directed compositions
		if( theGraphEl instanceof IRPGraphNode ){

			_context.debug( "afterAddForFunctionUsage found a graphNode for " + _context.elInfo( theGraphEl.getModelObject() ) );

			IRPPackage theOwningPackage = _context.getOwningPackageFor( theInstance );

			List<IRPModelElement> elsToChooseFrom = _context.findElementsWithMetaClassAndStereotype( 
					"Class", "FunctionBlock", theOwningPackage, 1 );

			if( !elsToChooseFrom.isEmpty() ){

				IRPModelElement theSelectedElement =
						UserInterfaceHelper.launchDialogToSelectElement( 
								elsToChooseFrom, "Select existing function block to type the function usage with", true );

				if( theSelectedElement instanceof IRPClassifier ){

					IRPModelElement theOwner = theInstance.getOwner();

					IRPInstance theExistingInstance = null;

					@SuppressWarnings("unchecked")
					List<IRPInstance> theCandidates = 
					theOwner.getNestedElementsByMetaClass( "Instance", 0 ).toList();

					for( IRPInstance theCandidate : theCandidates ){

						IRPClassifier theClassifier = theCandidate.getOtherClass();

						if( theClassifier != null && 
								theClassifier.equals( theSelectedElement ) ){

							theExistingInstance = theCandidate;
							break;
						}
					}

					if( theExistingInstance instanceof IRPInstance ){

						boolean isContinue = UserInterfaceHelper.askAQuestion( 
								"There is a FunctionUsage for " + 
										_context.elInfo( theSelectedElement ) + " already.\n" +
								"Do you want to populate this one, rather than create a new one?" );

						if( isContinue ){						

							switchGraphNodeFor( theInstance, theExistingInstance );

						} else {
							theInstance.setOtherClass( (IRPClassifier) theSelectedElement );
						}

					} else {

						theInstance.setOtherClass( (IRPClassifier) theSelectedElement );
					}			
				}
			}		
		}
	}

	private void afterAddForDecisionUsage(
			IRPInstance theInstance ){

		IRPGraphElement theGraphEl = getGraphElFor( theInstance );		

		// Only do this for parts, not directed compositions
		if( theGraphEl instanceof IRPGraphNode ){

			GraphNodeInfo theDecisionNodeInfo = new GraphNodeInfo( (IRPGraphNode) theGraphEl, _context );
			
			_context.debug( "afterAddForDecisionUsage found a graphNode for " + _context.elInfo( theGraphEl.getModelObject() ) );
			
			IRPSysMLPort theInPort = (IRPSysMLPort) theInstance.addNewAggr("SysMLPort", "");
			theInPort.changeTo( _context.FLOW_INPUT );	
			setPortDirectionFor( theInPort, "In", "Untyped" );	
			IRPGraphElement theInPortGraphEl = getGraphElFor( theInPort );
			//_context.dumpGraphicalPropertiesFor( theInPortGraphEl );
			theInPortGraphEl.setGraphicalProperty( "Position", theDecisionNodeInfo.getTopLeftX() + "," + theDecisionNodeInfo.getMiddleY() );
			theInPortGraphEl.setGraphicalProperty( "ShowName", "None" );

			IRPSysMLPort theCondPort = (IRPSysMLPort) theInstance.addNewAggr("SysMLPort", "[put condition here]");
			theCondPort.changeTo( _context.GUARDED_FLOW_OUTPUT );	
			setPortDirectionFor( theCondPort, "Out", "Untyped" );
			IRPGraphElement theCondPortGraphEl = getGraphElFor( theCondPort );
			theCondPortGraphEl.setGraphicalProperty( "Position", theDecisionNodeInfo.getTopRightX() + "," + theDecisionNodeInfo.getMiddleY() );

			IRPSysMLPort theElsePort = (IRPSysMLPort) theInstance.addNewAggr("SysMLPort", "[else]");
			theElsePort.changeTo( _context.GUARDED_FLOW_OUTPUT );	
			setPortDirectionFor( theElsePort, "Out", "Untyped" );
			IRPGraphElement theElsePortGraphEl = getGraphElFor( theElsePort );
			theElsePortGraphEl.setGraphicalProperty( "Position", theDecisionNodeInfo.getMiddleX() + "," + theDecisionNodeInfo.getBottomLeftY() );
		}
	}

	private void switchGraphNodeFor(
			IRPInstance modelElement,
			IRPInstance toTheElement ) {

		IRPGraphElement theGraphEl = getGraphElFor( modelElement );

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

	private IRPGraphElement getGraphElFor( 
			IRPInstance modelElement ){

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theReferences = modelElement.getReferences().toList();

		IRPGraphElement theGraphEl = null;

		for( IRPModelElement theReference : theReferences) {

			if( theReference instanceof IRPDiagram ){

				IRPDiagram theDiagram = (IRPDiagram)theReference;

				@SuppressWarnings("unchecked")
				List<IRPGraphElement> theGraphEls = 
				theDiagram.getCorrespondingGraphicElements( modelElement ).toList();

				if( theGraphEls.size() == 1 ){					
					theGraphEl = theGraphEls.get( 0 );
				}				
			}
		}

		return theGraphEl;
	}

	private List<IRPModelElement> getExistingActorUsagesBasedOn(
			IRPPackage theOwningPackage ){

		List<IRPModelElement> existingActorUsages = new ArrayList<>();

		if( theOwningPackage != null ){

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theCandidates =
			theOwningPackage.getGlobalObjects().toList();

			for( IRPModelElement theCandidate : theCandidates ){

				if( _context.hasStereotypeCalled( "ActorUsage", theCandidate ) ){
					existingActorUsages.add( theCandidate );
				}
			}
		}

		return existingActorUsages;
	}

	private void afterAddForRequirement(
			IRPModelElement modelElement ){

		// only do move if property is set
		boolean isEnabled = 
				_context.getIsEnableAutoMoveOfRequirements();

		String theReqtsPkgStereotypeName = _context.REQTS_ANALYSIS_REQUIREMENT_PACKAGE;

		if( isEnabled && 
				theReqtsPkgStereotypeName != null ){

			RequirementMover theElementMover = new RequirementMover( modelElement, theReqtsPkgStereotypeName, _context );
			theElementMover.performMove( modelElement );
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
				_context.warning( "Exception in afterAddForFlow trying to move " + _context.elInfo( modelElement ) + 
						" owned by " + _context.elInfo( modelElement.getOwner() ) + 
						" to " + _context.elInfo( theOwningPkg ) );
			}
		}

		CreateEventForFlowPanel.launchThePanel( _context.get_rhpAppID() );
	}

	private IRPGraphEdge getCorrespondingGraphEdgeFor( 
			IRPLink theLink ){

		IRPGraphEdge theGraphEdge = null;

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theReferences = theLink.getReferences().toList();

		if( theReferences.size() == 1 ){
			IRPModelElement theReference = theReferences.get( 0 );	

			_context.debug( "theReference is " + _context.elInfo( theReference ) );

			if( theReference instanceof IRPStructureDiagram ){

				IRPStructureDiagram theDiagram = (IRPStructureDiagram)theReference;

				@SuppressWarnings("unchecked")
				List<IRPGraphElement> theGraphEls = theDiagram.getCorrespondingGraphicElements( theLink ).toList();

				if( theGraphEls.size() == 1 ){
					IRPGraphElement theGraphEl = theGraphEls.get( 0 );

					if( theGraphEl instanceof IRPGraphEdge ){
						theGraphEdge = (IRPGraphEdge) theGraphEl;
					}
				}
			}
		}

		return theGraphEdge;
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

					createPortsBasedOnPropertyPoliciesFor( theLink );

				} else if( toClassifier instanceof IRPActor && 
						fromClassifier instanceof IRPClass ){

					createPortsBasedOnPropertyPoliciesFor( theLink );
				}
			}

		} else if( _context.isOwnedUnderPackageHierarchy( 
				_context.DESIGN_SYNTHESIS_LOGICAL_SYSTEM_PACKAGE, theLink ) ||
				_context.isOwnedUnderPackageHierarchy( 
						_context.DESIGN_SYNTHESIS_FEATURE_FUNCTION_PACKAGE, theLink ) ){

			String fromUserDefinedMetaClass = ( ( theLink.getFrom() == null ) ? "null from" : theLink.getFrom().getUserDefinedMetaClass() );
			String toUserDefinedMetaClass = ( ( theLink.getTo() == null ) ? "null to" : theLink.getTo().getUserDefinedMetaClass() );

			//_context.info( "fromUserDefinedMetaClass is " + fromUserDefinedMetaClass );
			//_context.info( "toUserDefinedMetaClass is " + toUserDefinedMetaClass );

			_context.debug( "afterAddForLink invoked for  " + _context.elInfo( theLink ) + 
					" owned by " + _context.elInfo( theLink.getOwner() ) );

			if( (theLink.getUserDefinedMetaClass().equals( 
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
					autoCreateFlowPortsFor( theLink );
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

				createPortsBasedOnPropertyPoliciesFor( theLink );
			}
		}
	}

	private void createPortsBasedOnPropertyPoliciesFor(
			IRPLink theLink ){

		String autoGenPolicy = _context.getAutoGenerationOfPortsForLinksPolicy( 
				theLink );

		String autoGenDefaultType = _context.getAutoGenerationOfPortsForLinksDefaultType( 
				theLink );

		if( autoGenPolicy.equals( "Always" ) ){

			createPortsFor( theLink, autoGenDefaultType );

		} else if( autoGenPolicy.equals( "UserDialog" ) ){

			String[] options = {"No","StandardPorts","ProxyPorts","RapidPorts"};

			String selectedPortType = (String) JOptionPane.showInputDialog(
					null,
					"You have drawn a  connector from " + theLink.getFrom().getUserDefinedMetaClass() + 
					" => " + theLink.getTo().getUserDefinedMetaClass() + ".\n"+
					"Do you want to automatically create ports?",
					"Input",
					JOptionPane.QUESTION_MESSAGE,
					null,
					options,
					autoGenDefaultType );

			createPortsFor( theLink, selectedPortType );
		}
	}

	private void createPortsFor(IRPLink theLink, String selectedPortType) {
		if( selectedPortType.equals( "No" ) ){
			// do nothing
		} else if( selectedPortType.equals( "ProxyPorts" ) ){
			autoCreateProxyPortsFor( theLink );
		} else if( selectedPortType.equals( "StandardPorts" ) ){
			autoCreateStandardPortsAndInterfacesFor( theLink );
		} else if( selectedPortType.equals( "RapidPorts" ) ){
			autoCreateRapidPortsFor( theLink );
		} else {
			_context.warning( "Ignoring request as " + selectedPortType + 
					" is not supported" );
		}
	}

	private void autoCreateStandardPortsAndInterfacesFor(
			IRPLink theLink ){

		IRPGraphEdge theGraphEdge = getCorrespondingGraphEdgeFor( theLink );

		if( theGraphEdge != null ){

			IRPDiagram theDiagram = theGraphEdge.getDiagram();

			IRPModelElement fromClassifierEl = theLink.getFrom().getOtherClass();
			IRPModelElement toClassifierEl = theLink.getTo().getOtherClass();

			_context.debug( "theLink.getFrom() = " + _context.elInfo( theLink.getFrom() ) );

			if( fromClassifierEl instanceof IRPClassifier && 
					toClassifierEl instanceof IRPClassifier ){

				_context.debug( "fromClassifierEl = " + _context.elInfo( fromClassifierEl ) );
				_context.debug( "toClassifierEl = " + _context.elInfo( toClassifierEl ) );

				String toClassifierName = _context.capitalize( toClassifierEl.getName().replace( " ", "" ) );
				String fromClassifierName = _context.capitalize( fromClassifierEl.getName().replace( " ", "" ) );

				String fromPortName = _context.determineUniqueNameBasedOn( 
						"p" + toClassifierName, 
						"Port", 
						fromClassifierEl );

				String toPortName = _context.determineUniqueNameBasedOn( 
						"p" + fromClassifierName, 
						"Port", 
						toClassifierEl );

				GraphEdgeInfo theGraphEdgeInfo = new GraphEdgeInfo( theGraphEdge, _context ); 

				IRPPackage theOwningPkg = _context.getOwningPackageFor( theDiagram );

				String fromInterfaceName =
						_context.determineUniqueNameBasedOn( 
								"I" + fromClassifierName, 
								"Class", 
								theOwningPkg );

				IRPClass fromInterface = theOwningPkg.addClass( fromInterfaceName );
				fromInterface.changeTo( "Interface" );

				IRPPort fromPort = (IRPPort) fromClassifierEl.addNewAggr( "Port", fromPortName );
				_context.debug( "Created fromPort as " + _context.elInfo( fromPort ) );



				String toInterfaceName =
						_context.determineUniqueNameBasedOn( 
								"I" + toClassifierName, 
								"Class", 
								theOwningPkg );

				IRPClass toInterface = theOwningPkg.addClass( toInterfaceName );
				toInterface.changeTo( "Interface" );

				IRPPort toPort = (IRPPort) toClassifierEl.addNewAggr( "Port", toPortName );		
				_context.debug( "Created toPort as " + _context.elInfo( toPort ) );

				fromPort.addProvidedInterface( fromInterface );
				fromPort.addRequiredInterface( toInterface );

				toPort.addProvidedInterface( toInterface );
				toPort.addRequiredInterface( fromInterface );

				IRPGraphNode fromPortNode = addGraphNodeFor( 
						fromPort, theDiagram, theGraphEdgeInfo.getStartX(), theGraphEdgeInfo.getStartY() );

				IRPGraphNode toPortNode = 
						addGraphNodeFor( toPort, theDiagram, theGraphEdgeInfo.getEndX(), theGraphEdgeInfo.getEndY() );					

				IRPLink newLink = theLink.getFrom().addLinkToElement( theLink.getTo(), null, fromPort, toPort );
				_context.debug( "Created " + _context.elInfo( newLink ) );

				theDiagram.addNewEdgeForElement( 
						newLink, 
						fromPortNode, 
						theGraphEdgeInfo.getStartX(), 
						theGraphEdgeInfo.getStartY(), 
						toPortNode, 
						theGraphEdgeInfo.getEndX(), 
						theGraphEdgeInfo.getEndY() );

				theLink.deleteFromProject();
			}
		}
	}

	private void autoCreateFlowPortsFor(
			IRPLink theLink ){

		IRPGraphEdge theGraphEdge = getCorrespondingGraphEdgeFor( theLink );

		if( theGraphEdge != null ){

			IRPDiagram theDiagram = theGraphEdge.getDiagram();

			IRPModelElement fromClassifierEl = theLink.getFrom().getOtherClass();
			IRPModelElement toClassifierEl = theLink.getTo().getOtherClass();

			_context.debug( "theLink.getFrom() = " + _context.elInfo( theLink.getFrom() ) );

			if( fromClassifierEl instanceof IRPClassifier && 
					toClassifierEl instanceof IRPClassifier ){

				_context.debug( "fromClassifierEl = " + _context.elInfo( fromClassifierEl ) );
				_context.debug( "toClassifierEl = " + _context.elInfo( toClassifierEl ) );

				String fromPortName = _context.determineUniqueNameBasedOn( 
						"out", 
						"SysMLPort", 
						fromClassifierEl );

				_context.debug( "fromPortName is " + fromPortName );

				String toPortName = _context.determineUniqueNameBasedOn( 
						"in",
						"SysMLPort", 
						toClassifierEl );

				_context.debug( "toPortName is " + toPortName );

				GraphEdgeInfo theGraphEdgeInfo = new GraphEdgeInfo( theGraphEdge, _context ); 

				IRPSysMLPort fromPort = (IRPSysMLPort) fromClassifierEl.addNewAggr( "SysMLPort", fromPortName );
				_context.debug( "Created fromPort as " + _context.elInfo( fromPort ) );
				setPortDirectionFor( fromPort, "Out", "Untyped" );
				fromPort.changeTo( _context.FLOW_OUTPUT );

				IRPGraphNode fromPortNode = addGraphNodeFor( 
						fromPort, theDiagram, theGraphEdgeInfo.getStartX(), theGraphEdgeInfo.getStartY() );

				IRPSysMLPort toPort = (IRPSysMLPort) toClassifierEl.addNewAggr( "SysMLPort", toPortName );		
				_context.debug( "Created toPort as " + _context.elInfo( toPort ) );
				setPortDirectionFor( toPort, "In", "Untyped" );
				toPort.changeTo( _context.FLOW_INPUT );

				IRPGraphNode toPortNode = addGraphNodeFor( 
						toPort, theDiagram, theGraphEdgeInfo.getEndX(), theGraphEdgeInfo.getEndY() );					

				IRPLink newLink = theLink.getFrom().addLinkToElement( theLink.getTo(), null, fromPort, toPort );
				_context.debug( "Created " + _context.elInfo( newLink ) );

				theDiagram.addNewEdgeForElement( 
						newLink, 
						fromPortNode, 
						theGraphEdgeInfo.getStartX(), 
						theGraphEdgeInfo.getStartY(), 
						toPortNode, 
						theGraphEdgeInfo.getEndX(), 
						theGraphEdgeInfo.getEndY() );

				theLink.deleteFromProject();

				CreateEventForFlowConnectorPanel.launchThePanel( 
						_context.get_rhpAppID(), 
						newLink.getGUID(),
						theDiagram.getGUID() );
			}
		}
	}

	private void autoCreateProxyPortsFor(
			IRPLink theLink ){

		IRPGraphEdge theGraphEdge = getCorrespondingGraphEdgeFor( theLink );

		if( theGraphEdge != null ){

			IRPDiagram theDiagram = theGraphEdge.getDiagram();

			IRPModelElement fromClassifierEl = theLink.getFrom().getOtherClass();
			IRPModelElement toClassifierEl = theLink.getTo().getOtherClass();

			_context.debug( "theLink.getFrom() = " + _context.elInfo( theLink.getFrom() ) );

			if( fromClassifierEl instanceof IRPClassifier && 
					toClassifierEl instanceof IRPClassifier ){

				_context.debug( "fromClassifierEl = " + _context.elInfo( fromClassifierEl ) );
				_context.debug( "toClassifierEl = " + _context.elInfo( toClassifierEl ) );

				String toClassifierName = _context.capitalize( toClassifierEl.getName().replace( " ", "" ) );
				String fromClassifierName = _context.capitalize( fromClassifierEl.getName().replace( " ", "" ) );

				String fromPortName = _context.determineUniqueNameBasedOn( 
						"p" + toClassifierName, 
						"Port", 
						fromClassifierEl );

				String toPortName = _context.determineUniqueNameBasedOn( 
						"p" + fromClassifierName, 
						"Port", 
						toClassifierEl );

				GraphEdgeInfo theGraphEdgeInfo = new GraphEdgeInfo( theGraphEdge, _context ); 

				IRPPackage theOwningPkg = _context.getOwningPackageFor( theDiagram );

				String theInterfaceBlockName =
						_context.determineUniqueNameBasedOn( 
								"IB_" + fromClassifierName + "_To_" + toClassifierName, 
								"Class", 
								theOwningPkg );

				IRPClass theInterfaceBlock = theOwningPkg.addClass( theInterfaceBlockName );
				theInterfaceBlock.changeTo( "InterfaceBlock" );

				IRPPort fromPort = (IRPPort) fromClassifierEl.addNewAggr( "Port", fromPortName );
				_context.debug( "Created fromPort as " + _context.elInfo( fromPort ) );
				fromPort.changeTo( "ProxyPort" );
				fromPort.setOtherClass( theInterfaceBlock );

				IRPGraphNode fromPortNode = addGraphNodeFor( 
						fromPort, theDiagram, theGraphEdgeInfo.getStartX(), theGraphEdgeInfo.getStartY() );

				IRPPort toPort = (IRPPort) toClassifierEl.addNewAggr( "Port", toPortName );		
				_context.debug( "Created toPort as " + _context.elInfo( toPort ) );
				toPort.changeTo( "ProxyPort" );
				toPort.setOtherClass( theInterfaceBlock );
				toPort.setIsReversed( 1 );

				IRPGraphNode toPortNode = 
						addGraphNodeFor( toPort, theDiagram, theGraphEdgeInfo.getEndX(), theGraphEdgeInfo.getEndY() );					

				IRPLink newLink = theLink.getFrom().addLinkToElement( theLink.getTo(), null, fromPort, toPort );
				_context.debug( "Created " + _context.elInfo( newLink ) );

				theDiagram.addNewEdgeForElement( 
						newLink, 
						fromPortNode, 
						theGraphEdgeInfo.getStartX(), 
						theGraphEdgeInfo.getStartY(), 
						toPortNode, 
						theGraphEdgeInfo.getEndX(), 
						theGraphEdgeInfo.getEndY() );

				theLink.deleteFromProject();
			}
		}
	}

	private void autoCreateRapidPortsFor(
			IRPLink theLink ){

		IRPGraphEdge theGraphEdge = getCorrespondingGraphEdgeFor( theLink );

		if( theGraphEdge != null ){

			IRPDiagram theDiagram = theGraphEdge.getDiagram();

			IRPModelElement fromClassifierEl = theLink.getFrom().getOtherClass();
			IRPModelElement toClassifierEl = theLink.getTo().getOtherClass();

			_context.debug( "theLink.getFrom() = " + _context.elInfo( theLink.getFrom() ) );

			if( fromClassifierEl instanceof IRPClassifier && 
					toClassifierEl instanceof IRPClassifier ){

				_context.debug( "fromClassifierEl = " + _context.elInfo( fromClassifierEl ) );
				_context.debug( "toClassifierEl = " + _context.elInfo( toClassifierEl ) );

				String toClassifierName = _context.capitalize( toClassifierEl.getName().replace( " ", "" ) );
				String fromClassifierName = _context.capitalize( fromClassifierEl.getName().replace( " ", "" ) );

				String fromPortName = _context.determineUniqueNameBasedOn( 
						"p" + toClassifierName, 
						"Port", 
						fromClassifierEl );

				String toPortName = _context.determineUniqueNameBasedOn( 
						"p" + fromClassifierName, 
						"Port", 
						toClassifierEl );

				GraphEdgeInfo theGraphEdgeInfo = new GraphEdgeInfo( theGraphEdge, _context ); 

				IRPPort fromPort = (IRPPort) fromClassifierEl.addNewAggr( "Port", fromPortName );
				_context.debug( "Created fromPort as " + _context.elInfo( fromPort ) );

				IRPGraphNode fromPortNode = addGraphNodeFor( 
						fromPort, theDiagram, theGraphEdgeInfo.getStartX(), theGraphEdgeInfo.getStartY() );

				IRPPort toPort = (IRPPort) toClassifierEl.addNewAggr( "Port", toPortName );		
				_context.debug( "Created toPort as " + _context.elInfo( toPort ) );

				IRPGraphNode toPortNode = 
						addGraphNodeFor( toPort, theDiagram, theGraphEdgeInfo.getEndX(), theGraphEdgeInfo.getEndY() );					

				IRPLink newLink = theLink.getFrom().addLinkToElement( theLink.getTo(), null, fromPort, toPort );
				_context.debug( "Created " + _context.elInfo( newLink ) );

				theDiagram.addNewEdgeForElement( 
						newLink, 
						fromPortNode, 
						theGraphEdgeInfo.getStartX(), 
						theGraphEdgeInfo.getStartY(), 
						toPortNode, 
						theGraphEdgeInfo.getEndX(), 
						theGraphEdgeInfo.getEndY() );

				theLink.deleteFromProject();
			}
		}
	}
	private IRPGraphNode addGraphNodeFor(
			IRPModelElement thePortEl,
			IRPDiagram toDiagram,
			int x,
			int y ){

		IRPGraphNode thePortNode = null;

		@SuppressWarnings("unchecked")
		List<IRPGraphNode> theGraphNodes = 
		toDiagram.getCorrespondingGraphicElements( thePortEl ).toList();

		if( theGraphNodes.size() == 1 ){

			thePortNode = theGraphNodes.get( 0 );

			String thePosition = x + "," + y;

			_context.debug( "Setting Position of existing " + _context.elInfo( thePortEl ) + " to " + thePosition );
			thePortNode.setGraphicalProperty( "Position", thePosition );

		} else {

			thePortNode = toDiagram.addNewNodeForElement( thePortEl, x, y, 12, 12 );
		}

		return thePortNode;
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
			if( _context.getIsDoubleClickFunctionalityEnabled() ){

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

					String theUnadornedName = "AD - " + pModelElement.getName();

					boolean theAnswer = UserInterfaceHelper.askAQuestion(
							"This use case has no nested text-based Activity Diagram.\n"+
									"Do you want to create one called '" + theUnadornedName + "'?");

					if( theAnswer == true ){

						_context.debug( "User chose to create a new activity diagram" );

						NestedActivityDiagram theHelper = new NestedActivityDiagram(_context);

						theHelper.createNestedActivityDiagram( 
								(IRPClassifier) pModelElement, 
								"AD - " + pModelElement.getName(),
								"ExecutableMBSEProfile.RequirementsAnalysis.TemplateForActivityDiagram" );
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
 * Copyright (C) 2018-2022  MBSE Training and Consulting Limited (www.executablembse.com)

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