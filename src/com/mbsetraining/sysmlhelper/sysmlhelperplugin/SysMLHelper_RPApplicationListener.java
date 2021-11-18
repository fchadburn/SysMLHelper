package com.mbsetraining.sysmlhelper.sysmlhelperplugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import com.mbsetraining.sysmlhelper.common.NestedActivityDiagram;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.mbsetraining.sysmlhelper.tracedelementpanels.CreateOperationPanel;
import com.telelogic.rhapsody.core.*;

public class SysMLHelper_RPApplicationListener extends RPApplicationListener {

	private ExecutableMBSE_Context _context;

	public SysMLHelper_RPApplicationListener( 
			String expectedProfileName,
			ExecutableMBSE_Context context ) {

		_context = context;
		_context.info( "SysMLHelper_RPApplicationListener was loaded - Listening for events (double-click etc)" ); 
	}

	public boolean afterAddElement(
			IRPModelElement modelElement ){

		boolean doDefault = false;

		try {
			_context.setSavedInSeparateDirectoryIfAppropriateFor( 
					modelElement );

			if( modelElement instanceof IRPDependency && 
					modelElement.getUserDefinedMetaClass().equals(
							"Derive Requirement" ) ){

				afterAddForDeriveRequirement( (IRPDependency) modelElement );

			} else if( modelElement instanceof IRPCallOperation ){

				afterAddForCallOperation( 
						(IRPCallOperation) modelElement );
			}

		} catch( Exception e ){
			_context.error( "SysMLHelper_RPApplicationListener.afterAddElement, " +
					" unhandled exception related to " + _context.elInfo( modelElement ) + 
					e.getMessage() );
		}

		return doDefault;
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