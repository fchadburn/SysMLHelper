package com.mbsetraining.sysmlhelper.businessvalueplugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import com.mbsetraining.sysmlhelper.common.NestedActivityDiagram;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.telelogic.rhapsody.core.*;

public class BusinessValue_RPApplicationListener extends RPApplicationListener {
	
	private BusinessValue_Context _context;

	public BusinessValue_RPApplicationListener( 
			String expectedProfileName,
			BusinessValue_Context context ) {

		_context = context;
		_context.info( "BusinessValue_RPApplicationListener was loaded - Listening for events)" ); 
	}

	@Override
	public boolean afterAddElement(
			IRPModelElement modelElement ){

		boolean doDefault = false;

		try {
			String theUserDefinedMetaClass = modelElement.getUserDefinedMetaClass();
			
			if( theUserDefinedMetaClass.equals( _context.METACLASS_FOR_MEASURED_BY ) ){
				afterAddElementForMeasureBy( (IRPAttribute) modelElement );
				
			} else if( theUserDefinedMetaClass.equals( BusinessValue_Context.METACLASS_FOR_TIER_1_GOAL ) ||
				theUserDefinedMetaClass.equals( BusinessValue_Context.METACLASS_FOR_TIER_2_GOAL ) ||
				theUserDefinedMetaClass.equals( BusinessValue_Context.METACLASS_FOR_TIER_3_GOAL ) ){
				
				afterAddElementForGoals( (IRPClass) modelElement );
			}

		} catch( Exception e ){
			_context.error( "BusinessValue_RPApplicationListener.afterAddElement, " +
					" unhandled exception related to " + _context.elInfo( modelElement ) + 
					e.getMessage() );
		}

		return doDefault;
	}

	public void afterAddElementForMeasureBy(
			IRPAttribute theAttribute ){
	
		IRPClassifier theClassifier = _context.getMeasuredByDefaultClass();
		
		if( theClassifier != null ){
			
			theAttribute.setType( theClassifier );
		}
	}
	
	public void afterAddElementForGoals(
			IRPClass theGoal ){
		
		_context.addHyperLink( theGoal, theGoal );
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
			//if( _context.getIsDoubleClickFunctionalityEnabled() ){

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

						NestedActivityDiagram theHelper = new NestedActivityDiagram(_context);

						theHelper.createNestedActivityDiagram( 
								pModelElement, 
								NestedActivityDiagram._prefix + pModelElement.getName(),
								"ExecutableMBSEProfile.RequirementsAnalysis.TemplateForActivityDiagram" );
					}

					theReturn = true; // don't launch the Features  window									

				} else {
					theReturn = false; // do default, i.e. open the features dialog
				}	
			//}

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