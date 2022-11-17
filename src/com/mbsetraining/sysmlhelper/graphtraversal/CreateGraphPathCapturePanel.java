package com.mbsetraining.sysmlhelper.graphtraversal;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JTextField;

import com.mbsetraining.sysmlhelper.businessvalueplugin.BusinessValue_BasePanel;
import com.mbsetraining.sysmlhelper.businessvalueplugin.BusinessValue_Context;
import com.telelogic.rhapsody.core.*;

public class CreateGraphPathCapturePanel extends BusinessValue_BasePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4947635266699088981L;

	protected JTextField _nameTextField = null;
	protected IRPPackage _creationPackage;
	protected IRPDiagram _sourceDiagram;
	protected IRPModelElement _selectedElement;
	protected List<GraphPaths> _graphPathsToCreate;
	List<IRPModelElement> _rootEls;

	public static void main(String[] args) {

		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();

		String theAppID = theRhpApp.getApplicationConnectionString();

		BusinessValue_Context context = new BusinessValue_Context( theAppID );

		IRPModelElement theSelectedEl = context.getSelectedElement(true);
		IRPGraphElement theGraphNode = context.getSelectedGraphEl();

		if( theSelectedEl instanceof IRPClass &&
				theGraphNode instanceof IRPGraphNode ){

			IRPDiagram theDiagram = theGraphNode.getDiagram();

			launchThePanel(theAppID, theSelectedEl.getGUID(), theDiagram.getGUID());

		} else if( theSelectedEl instanceof IRPDiagram ) {

			launchThePanel(theAppID, theSelectedEl.getGUID(), theSelectedEl.getGUID());
		}
	}

	public static void launchThePanel(
			String theAppID,
			String theElementGUID,
			String onDiagramGUID ){

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame( "Perform graphical analysis" );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateGraphPathCapturePanel thePanel = 
						new CreateGraphPathCapturePanel( 
								theAppID, 
								theElementGUID,
								onDiagramGUID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}

	public CreateGraphPathCapturePanel( 
			String theAppID,
			String theElementGUID,
			String onDiagramGUID ){

		super( theAppID );

		_context.debug( "CreateGraphPathCapturePanel constructor invoked" );

		_selectedElement = (IRPModelElement) _context.get_rhpPrj().findElementByGUID( theElementGUID );

		_creationPackage = _context.getOwningPackageFor( _context.getSelectedElement( false ) );

		_sourceDiagram = (IRPDiagram) _context.get_rhpPrj().findElementByGUID( onDiagramGUID );

		_rootEls = getRootElsFrom( _sourceDiagram );

		analyzeGraphPaths();

		String msg;

		if( _selectedElement instanceof IRPDiagram ) {

			msg = _graphPathsToCreate.size() + " paths were found for " + 
					_context.elInfo( _selectedElement ) + ". \n\nCreate view structures for these?";
		} else {

			msg = _graphPathsToCreate.size() + " paths were found that include " + 
					_context.elInfo( _selectedElement ) + ". \n\nCreate view structures for these?";
		}

		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		add( createPanelWithTextCentered( msg ), BorderLayout.PAGE_START );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}

	private List<IRPModelElement> getRootElsFrom(
			IRPDiagram theDiagram ){

		List<IRPModelElement> theRootEls = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theCandidateEls = theDiagram.getElementsInDiagram().toList();

		for( IRPModelElement theCandidateEl : theCandidateEls ){

			if( isRootElement( theCandidateEl) ) {
				theRootEls.add( theCandidateEl );
			}
		}

		_context.info( "getRootElsFrom found that there are " + theRootEls.size() + " root els");

		for (IRPModelElement theCandidateEl : theCandidateEls) {
			_context.info( _context.elInfo( theCandidateEl ) );
		}

		return theRootEls;
	}

	private boolean isGoalElement(
			IRPModelElement theElement ) {

		//_context.info( "isGoalElement invoked for " + _context.elInfo( theElement ) );
		
		boolean isGoalElement = theElement instanceof IRPClass && (
				theElement.getUserDefinedMetaClass().equals( 
						BusinessValue_Context.METACLASS_FOR_TIER_1_GOAL ) ||
						theElement.getUserDefinedMetaClass().equals( 
								BusinessValue_Context.METACLASS_FOR_TIER_2_GOAL ) ||
						theElement.getUserDefinedMetaClass().equals( 
								BusinessValue_Context.METACLASS_FOR_TIER_3_GOAL ) );

		return isGoalElement;

	}
	private boolean isRootElement(
			IRPModelElement theElement ){

		boolean isRootElement;

		if( !isGoalElement( theElement ) ){
		
			isRootElement = false;
			
		} else {

			isRootElement = true;
			
			@SuppressWarnings("unchecked")
			List<IRPModelElement> theReferences = theElement.getReferences().toList();

			//_context.info( "isRootElement is looking at " + theReferences.size() + " references for " + _context.elInfo( theElement ) );

			for( IRPModelElement theReference : theReferences ){

				//_context.info( "Checking " + _context.elInfo( theReference ) );

				if( theReference.getUserDefinedMetaClass().equals( 
						BusinessValue_Context.NEEDS_NEW_TERM ) ) {

					IRPDependency theDependency = (IRPDependency)theReference;

					if( theDependency.getDependsOn().equals( theElement ) ) {

						//_context.info( "Found that " + _context.elInfo( theElement ) + 
						//		" is not a root el as it has an incoming dependency from" + 
						//		_context.elInfo( theDependency.getDependent() )  );

						isRootElement = false;
						break;
					}
				}
			}
		}
		
		//_context.info( "isRootElement is returning " + isRootElement + " for " + _context.elInfo( theElement ) );

		return isRootElement;
	}

	private boolean isLeafElement(
			IRPModelElement theElement ){

		boolean isLeafElement = true;

		if( isGoalElement( theElement ) ) {

			@SuppressWarnings("unchecked")
			List<IRPDependency> theExistingDeps = theElement.getDependencies().toList();

			for( IRPDependency theDependency : theExistingDeps ){

				IRPModelElement theDependsOn = theDependency.getDependsOn();

				if ( isGoalElement( theDependsOn ) ) {

					isLeafElement = false;
					break;
				}
			}
		}

		//_context.info( "isLeafElement is returning " + isLeafElement + " for " + _context.elInfo( theElement ) );

		return isLeafElement;
	}

	private String getPathsLegalNameFor( 
			IRPModelElement theEl ) {

		int maxLength = 100;

		String preFix;

		if( isRootElement(theEl) ) {
			preFix = "Paths starting ";
		} else if( isLeafElement(theEl)) {
			preFix = "Paths ending ";
		} else {
			preFix = "Paths through ";
		}

		String theFullName = preFix + theEl.getUserDefinedMetaClass() + 
				" called " + theEl.getName().replaceAll("[/():;]", "_");

		if (theFullName.length() <= maxLength) {
			return theFullName.trim();
		} else {
			return theFullName.substring(0, maxLength).trim();
		}
	}

	private void analyzeGraphPaths() {

		_graphPathsToCreate = new ArrayList<>();

		GraphPaths allPaths = new GraphPaths( _context, "All paths", _sourceDiagram );

		for( IRPModelElement rootEl : _rootEls ){
			Node startNode = new Node( rootEl, _context );
			GraphPath theCurrentPath = new GraphPath( _context );
			startNode.buildRecursively( theCurrentPath, allPaths );
		}

		allPaths.dumpInfo();

		if( _selectedElement instanceof IRPDiagram ) {

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theModelEls = ((IRPDiagram) _selectedElement).getElementsInDiagram().toList();

			for( IRPModelElement theModelEl : theModelEls ){

				String theUserDefinedMetaClass = theModelEl.getUserDefinedMetaClass();

				if( theUserDefinedMetaClass.equals( BusinessValue_Context.METACLASS_FOR_TIER_1_GOAL ) ||
						theUserDefinedMetaClass.equals( BusinessValue_Context.METACLASS_FOR_TIER_2_GOAL ) ||
						theUserDefinedMetaClass.equals( BusinessValue_Context.METACLASS_FOR_TIER_3_GOAL ) ) {


					GraphPaths selectedPaths = allPaths.getGraphPathsThatInclude( 
							theModelEl, 
							getPathsLegalNameFor( theModelEl ) );

					_graphPathsToCreate.add( selectedPaths );
				}
			}

		} else {			
			GraphPaths selectedPaths = allPaths.getGraphPathsThatInclude( 
					_selectedElement, 
					getPathsLegalNameFor(_selectedElement ) );

			_graphPathsToCreate.add( selectedPaths );
		}

		for (GraphPaths graphPaths : _graphPathsToCreate) {
			graphPaths.dumpInfo();
		}
	}


	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled) {

		boolean isValid = true;
		String errorMsg = "";

		/*
		String theChosenName = _nameTextField.getText();

		boolean isLegalName = _context.isLegalName( theChosenName, _creationPackage );

		if (!isLegalName){

			errorMsg += theChosenName + " is not legal as an identifier representing an Event\n";				
			isValid = false;

		} else if (!_context.isElementNameUnique(		
				theChosenName, "Class", _creationPackage, 1 ) ){

			errorMsg += "Unable to proceed as the Class name '" + theChosenName + "' is not unique";
			isValid = false;
		}

		if( isMessageEnabled && 
				!isValid && 
				errorMsg != null ){

			UserInterfaceHelper.showWarningDialog( errorMsg );
		}*/

		return isValid;
	}

	@Override
	protected void performAction() {

		if( checkValidity( false ) ){

			for (GraphPaths selectedElementPaths : _graphPathsToCreate) {

				IRPPackage theViewStructure = 
						selectedElementPaths.createSingleViewStructureUnder(
								_creationPackage );

				if( theViewStructure instanceof IRPPackage ) {				
					theViewStructure.highLightElement();
				}
			}

		} else {
			_context.error( "Error in CreateEventForFlowConnectorPanel.performAction, checkValidity returned false" );
		}		
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