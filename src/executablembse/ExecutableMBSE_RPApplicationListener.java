package executablembse;

import functionalanalysisplugin.CreateOperationPanel;
import functionalanalysisplugin.GraphNodeInfo;
import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.StereotypeAndPropertySettings;
import generalhelpers.UserInterfaceHelpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import requirementsanalysisplugin.NestedActivityDiagram;

import com.telelogic.rhapsody.core.*;

public class ExecutableMBSE_RPApplicationListener extends RPApplicationListener {

	private String _expectedProfileName = null;
	private String _expectedAppID;

	public ExecutableMBSE_RPApplicationListener( 
			IRPApplication app,
			String expectedProfileName ) {

		Logger.info( "ExecutableMBSE_RPApplicationListener was loaded - Listening for events (double-click etc)" ); 

		_expectedProfileName = expectedProfileName;
		_expectedAppID = app.getApplicationConnectionString();
	}

	private boolean isOKToRun(){

		boolean isOKToRun = false;

		@SuppressWarnings("unchecked")
		List<String> theAppIDs = RhapsodyAppServer.getActiveRhapsodyApplicationIDList();

		if( theAppIDs.size() == 1 &&
				_expectedProfileName != null && 
				_expectedAppID != null &&
				_expectedAppID.equals( _expectedAppID ) ){

			String theAppID = theAppIDs.get( 0 );

			IRPApplication theRhpApp = 
					RhapsodyAppServer.getActiveRhapsodyApplicationByID( 
							theAppID );

			if( theRhpApp != null ){

				IRPProject theRhpPrj = theRhpApp.activeProject();

				IRPModelElement theProfile =
						theRhpPrj.findNestedElement( 
								_expectedProfileName, "Profile" );

				if( theProfile != null ){
					isOKToRun = true;
				}
			}
		}

		return isOKToRun;
	}

	public static void main(
			String[] args ){

		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();

		IRPModelElement theSelectedEl = theRhpApp.getSelectedElement();

		afterAddForCallOperation( (IRPCallOperation) theSelectedEl );
	}

	public boolean afterAddElement(
			IRPModelElement modelElement ){

		boolean doDefault = false;

		try {
			if( isOKToRun() ){

				StereotypeAndPropertySettings.setSavedInSeparateDirectoryIfAppropriateFor( 
						modelElement );

				if( modelElement != null && 
						modelElement instanceof IRPRequirement ){

					afterAddForRequirement( modelElement );

				} else if( modelElement != null && 
						modelElement instanceof IRPClass && 
						GeneralHelpers.hasStereotypeCalled( "Interface", modelElement )){

					afterAddForInterface( modelElement );

				} else if( modelElement != null && 
						modelElement instanceof IRPDependency && 
						modelElement.getUserDefinedMetaClass().equals(
								"Derive Requirement" ) ){

					afterAddForDeriveRequirement( (IRPDependency) modelElement );

				} else if( modelElement != null && 
						modelElement instanceof IRPCallOperation ){

					afterAddForCallOperation( 
							(IRPCallOperation) modelElement );

				} else if( modelElement != null && 
						modelElement instanceof IRPInstance && 
						GeneralHelpers.hasStereotypeCalled( "ActorUsage", modelElement )){

					afterAddForActorUsage( (IRPInstance) modelElement );

				} else if( modelElement != null && 
						modelElement instanceof IRPFlow ){

					afterAddForFlow( 
							(IRPFlow) modelElement );
				}
			}
		} catch( Exception e ){
			Logger.error( "ExecutableMBSE_RPApplicationListener.afterAddElement, " +
					" unhandled exception related to " + Logger.elementInfo( modelElement ) + 
					e.getMessage() );
		}

		return doDefault;
	}

	private static void afterAddForCallOperation(
			IRPCallOperation theCallOp ){

		// only do move if property is set
		boolean isEnabled = 
				StereotypeAndPropertySettings.getIsCallOperationSupportEnabled(
						theCallOp );

		if( isEnabled ){

			IRPApplication theRhpApp = 
					RhapsodyAppServer.getActiveRhapsodyApplication();

			IRPInterfaceItem theOp = theCallOp.getOperation();

			IRPDiagram theDiagram = theRhpApp.getDiagramOfSelectedElement();

			if( theDiagram != null ){ 

				CreateOperationPanel.launchThePanel();

			} // else probably drag from browser

			if( theOp != null ){

				// Use the operation name for the COA if possible
				try {
					String theProposedName = 
							GeneralHelpers.determineUniqueNameBasedOn( 
									GeneralHelpers.toMethodName( theOp.getName(), 40 ), 
									"CallOperation", 
									theCallOp.getOwner() );

					theCallOp.setName( theProposedName );

				} catch( Exception e ) {
					Logger.writeLine( theCallOp, "Error: Cannot rename Call Operation to match Operation" );
				}

				// If the operation has an Activity Diagram under it, then populate an RTF 
				// description with a link to the lower diagram
				IRPFlowchart theAD = theOp.getActivityDiagram();

				if( theAD != null ){

					IRPActivityDiagram theFC = theAD.getFlowchartDiagram();

					if( theFC != null ){
						Logger.writeLine("Creating Hyperlinks in Description of COA");

						IRPCollection targets = theRhpApp.createNewCollection();

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
					GeneralHelpers.getStereotypeAppliedTo(
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
		boolean isEnabled = 
				StereotypeAndPropertySettings.getIsEnableAutoMoveOfInterfaces(
						modelElement );

		if( isEnabled ){
			ElementMover theElementMover = new ElementMover( 
					modelElement, 
					StereotypeAndPropertySettings.getInterfacesPackageStereotype( 
							modelElement ) );

			theElementMover.performMove();
		}
	}

	private void afterAddForActorUsage(
			IRPInstance modelElement ){

		IRPPackage theOwningPackage = getOwningPackageFor( modelElement );
		
		List<IRPModelElement> existingActors = getExistingActorsBasedOn( theOwningPackage );
		
		if( !existingActors.isEmpty() ){
		
			List<IRPModelElement> elsToChooseFrom = new ArrayList<>();
			elsToChooseFrom.addAll( existingActors );
			
			List<IRPModelElement> existingActorUsages = getExistingActorUsagesBasedOn( theOwningPackage );
			existingActorUsages.remove( modelElement ); // Don't include this element
			
			elsToChooseFrom.addAll( existingActorUsages );

			IRPModelElement theSelectedElement =
					UserInterfaceHelpers.launchDialogToSelectElement( 
							elsToChooseFrom, "Select existing actor or actor usage", true );

			if( theSelectedElement instanceof IRPInstance ){

				IRPGraphElement theGraphEl = getGraphElFor( modelElement );
				
				if( theGraphEl instanceof IRPGraphNode ){
					
					IRPDiagram theDiagram = theGraphEl.getDiagram();

					IRPGraphNode theNewNode = null;
					
					GraphNodeInfo theNodeInfo;
										
					try {
						theNodeInfo = new GraphNodeInfo( (IRPGraphNode) theGraphEl );
						
						theNewNode = theDiagram.addNewNodeForElement(
								theSelectedElement, 
								theNodeInfo.getTopLeftX(), 
								theNodeInfo.getTopLeftY(), 
								theNodeInfo.getWidth(), 
								theNodeInfo.getHeight() );
						
						String theStructuredViewPropertyValue =
								theGraphEl.getGraphicalProperty( "StructureView" ).getValue();
						
						theNewNode.setGraphicalProperty( "StructureView", theStructuredViewPropertyValue );
												
					} catch( Exception e ){
						
						Logger.writeLine( "Exception in afterAddForActorUsage when switching " + 
								Logger.elementInfo( theGraphEl.getModelObject() ) + 
								" to " + Logger.elementInfo( theSelectedElement ) );
						
						e.printStackTrace();
					}
					
					//dumpGraphicalProperties(theGraphEl);
					
					if( theNewNode instanceof IRPGraphNode ){
						modelElement.deleteFromProject();
						theSelectedElement.highLightElement();
					}
				}
				
			} else if( theSelectedElement instanceof IRPClassifier ){
				
				modelElement.setOtherClass( (IRPClassifier) theSelectedElement );
				
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

	private List<IRPModelElement> getExistingActorsBasedOn(
			IRPPackage theOwningPackage ){
		
		List<IRPModelElement> existingActors = new ArrayList<>();

		if( theOwningPackage != null ){
			
			@SuppressWarnings("unchecked")
			List<IRPModelElement> theActorsInOwningPackage =
					theOwningPackage.getNestedElementsByMetaClass( "Actor", 0 ).toList();
			
			existingActors.addAll( theActorsInOwningPackage );

			@SuppressWarnings("unchecked")
			List<IRPDependency> theDependencies = theOwningPackage.getDependencies().toList();

			for (IRPDependency theDependency : theDependencies) {

				IRPModelElement theDependsOn = theDependency.getDependsOn();

				if( theDependsOn instanceof IRPPackage &&
						GeneralHelpers.hasStereotypeCalled( "ActorPackage", theDependsOn ) ){

					@SuppressWarnings("unchecked")
					List<IRPModelElement> theActorsInDependsOnActorPackage =
							theDependsOn.getNestedElementsByMetaClass( "Actor", 0 ).toList();
					
					existingActors.addAll( theActorsInDependsOnActorPackage );
				}
			}
		}
		
		return existingActors;
	}
	
	private List<IRPModelElement> getExistingActorUsagesBasedOn(
			IRPPackage theOwningPackage ){
		
		List<IRPModelElement> existingActorUsages = new ArrayList<>();

		if( theOwningPackage != null ){
			
			@SuppressWarnings("unchecked")
			List<IRPModelElement> theCandidates =
					theOwningPackage.getGlobalObjects().toList();

			for( IRPModelElement theCandidate : theCandidates ){
				
				if( GeneralHelpers.hasStereotypeCalled( "ActorUsage", theCandidate ) ){
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
				StereotypeAndPropertySettings.getIsEnableAutoMoveOfRequirements(
						modelElement );

		if( isEnabled ){
			RequirementMover theElementMover = new RequirementMover( modelElement );
			theElementMover.performMove();
		}
	}

	private void afterAddForFlow(
			IRPFlow modelElement ){

		IRPPackage theOwningPkg = getOwningPackageFor( modelElement );

		if( theOwningPkg != null &&
				!modelElement.getOwner().equals( theOwningPkg ) ){

			try {
				modelElement.setOwner( theOwningPkg );	

			} catch( Exception e ){
				Logger.warning( "Exception in afterAddForFlow trying to move " + Logger.elementInfo( modelElement ) + 
						" owned by " + Logger.elementInfo( modelElement.getOwner() ) + 
						" to " + Logger.elementInfo( theOwningPkg ) );
			}
		}

		CreateEventForFlow.launchThePanel();
	}

	private IRPPackage getOwningPackageFor(
			IRPModelElement theElement ){

		IRPPackage theOwningPackage = null;

		if( theElement == null ){

			Logger.warning( "getOwningPackage for was invoked for a null element" );

		} else if( theElement instanceof IRPPackage ){
			theOwningPackage = (RPPackage)theElement;

		} else if( theElement instanceof IRPProject ){
			Logger.warning( "Unable to find an owning package for " + theElement.getFullPathNameIn() + " as I reached project" );

		} else {
			theOwningPackage = getOwningPackageFor( theElement.getOwner() );
		}

		return theOwningPackage;
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

				boolean theAnswer = UserInterfaceHelpers.askAQuestion(
						"This use case has no nested text-based Activity Diagram.\n"+
								"Do you want to create one called '" + theUnadornedName + "'?");

				if( theAnswer == true ){

					Logger.writeLine("User chose to create a new activity diagram");

					NestedActivityDiagram.createNestedActivityDiagram( 
							(IRPClassifier) pModelElement, 
							"AD - " + pModelElement.getName(),
							"SysMLHelper.RequirementsAnalysis.TemplateForActivityDiagram" );
				}

				theReturn = true; // don't launch the Features  window									

			} else {
				theReturn = false; // do default, i.e. open the features dialog
			}	

		} catch( Exception e ){
			Logger.error("Unhandled exception in onDoubleClick(), e=" + e.getMessage() );			
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

				IRPModelElement theSelection = UserInterfaceHelpers.launchDialogToSelectElement(
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
			Logger.writeLine("Unhandled exception in onDoubleClick()");			
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
	protected void finalize() throws Throwable {
		super.finalize();
	}
	
	public static void dumpGraphicalProperties(IRPGraphElement theElement) {
		
		@SuppressWarnings("unchecked")
		List<IRPGraphicalProperty> theGraphicalProperties = theElement.getAllGraphicalProperties().toList();
		
		for (IRPGraphicalProperty theGraphicalProperty : theGraphicalProperties) {

			System.out.println(theGraphicalProperty.getKey() + "::" + theGraphicalProperty.getValue());
			Logger.writeLine(theGraphicalProperty.getKey() + "::" + theGraphicalProperty.getValue());
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