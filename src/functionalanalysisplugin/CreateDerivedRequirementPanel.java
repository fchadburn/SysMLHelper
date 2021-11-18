package functionalanalysisplugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.mbsetraining.sysmlhelper.common.GraphNodeInfo;
import com.mbsetraining.sysmlhelper.common.NamedElementMap;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.tracedelementpanels.CreateTracedElementPanel;
import com.mbsetraining.sysmlhelper.tracedelementpanels.RequirementSelectionPanel;
import com.telelogic.rhapsody.core.*;

public class CreateDerivedRequirementPanel extends CreateTracedElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3688632776838701799L;
	
	protected NamedElementMap _namedElementMap;
	protected JComboBox<Object> _fromPackageComboBox = null;
	protected JTextArea _specificationTextArea = null;
//	private RequirementSelectionPanel m_RequirementSelectionPanel;
	protected JCheckBox _populateOnDiagramCheckBox; 
	protected JCheckBox _moveIntoCheckBox; 
	protected Set<IRPRequirement> _forHigherLevelReqts;
	protected IRPGraphElement _sourceGraphElement;
	protected IRPModelElement _targetOwningElement;

	public static void main(String[] args) {

		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		launchThePanel( theRhpApp.getApplicationConnectionString() );
	}

	public static void launchThePanel(
			String theRhpID ){

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {				
				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame(
						"Derive a new requirement?");

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateDerivedRequirementPanel thePanel = 
						new CreateDerivedRequirementPanel( 
								theRhpID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}

	CreateDerivedRequirementPanel(
			String theAppID ){

		super( theAppID );

		List<IRPGraphElement> theSelectedGraphEls = _context.getSelectedGraphElements();

		int selectedCount = theSelectedGraphEls.size();

		if (selectedCount > 1){
			_context.warning("Warning in deriveDownstreamRequirement, operation only works for " +
					"1 selected element and " + selectedCount + " were selected");

		} else if (selectedCount == 0){
			_context.error("Error in deriveDownstreamRequirement, no elements were selected");	

		} else {		
			IRPGraphElement theSelectedGraphEl = theSelectedGraphEls.get(0);
			IRPModelElement theModelObject = theSelectedGraphEl.getModelObject();

			_forHigherLevelReqts = new HashSet<IRPRequirement>();
			_sourceGraphElement = theSelectedGraphEls.get(0);

			if (theModelObject instanceof IRPRequirement){

				_forHigherLevelReqts.add( (IRPRequirement)theModelObject );

			} else if (theModelObject instanceof IRPCallOperation){

				IRPCallOperation theCallOp = (IRPCallOperation)theModelObject;
				IRPInterfaceItem theOperation = theCallOp.getOperation();

				_forHigherLevelReqts = 
						_context.getRequirementsThatTraceFromWithStereotype(
								theOperation, "satisfy");

			} else if (theModelObject instanceof IRPAcceptEventAction){

				IRPAcceptEventAction theAcceptEvent = (IRPAcceptEventAction)theModelObject;

				// not all accept events will have an event
				IRPInterfaceItem theEvent = theAcceptEvent.getEvent();

				if( theEvent != null ){
					_forHigherLevelReqts = 
							_context.getRequirementsThatTraceFromWithStereotype(
									theEvent, "satisfy");			
				} else {

					_forHigherLevelReqts = 
							_context.getRequirementsThatTraceFromWithStereotype(
									theAcceptEvent, "derive");
				}

			} else if (theModelObject instanceof IRPState) { // SendAction

				IRPState theState = (IRPState)theModelObject;
				String theStateType = theState.getStateType();

				if (theStateType.equals("EventState")){ // receive event

					IRPSendAction theSendAction = theState.getSendAction();

					if (theSendAction != null){
						IRPEvent theEvent = theSendAction.getEvent();

						if (theEvent != null){
							_forHigherLevelReqts = 
									_context.getRequirementsThatTraceFromWithStereotype(
											theEvent, "satisfy");
						} else {
							_forHigherLevelReqts = 
									_context.getRequirementsThatTraceFromWithStereotype(
											theState, "derive");
						}
					} else {
						_context.error("Error in deriveDownstreamRequirement, theSendAction is null");
					}

				} else if (theStateType.equals("Action")){

					_forHigherLevelReqts = 
							_context.getRequirementsThatTraceFromWithStereotype(
									theState, "derive");

				} else {
					_context.warning("Warning in deriveDownstreamRequirement, this operation is not supported for theState.getStateType()=" + theState.getStateType());
				}

			} else if (theModelObject instanceof IRPOperation || 
					(theModelObject instanceof IRPEvent) ||
					(theModelObject instanceof IRPAttribute) ){

				_forHigherLevelReqts = 
						_context.getRequirementsThatTraceFromWithStereotype(
								theModelObject, "satisfy");

				_sourceGraphElement = theSelectedGraphEls.get(0);

			} else { 

				_context.warning( "Warning in , " + _context.elInfo(theModelObject) + 
						" was not handled as call with this type was not expected" );
			}
		}

		_context.info( "Create Derived Reqt panel invoked from " + _context.elInfo( _sourceGraphElement.getModelObject() ) );

		final String preferredRootPackageName = "FunctionalAnalysisPkg";

		IRPModelElement thePreferredRootPkg = 
				_context.get_rhpPrj().findElementsByFullName( preferredRootPackageName, "Package" );

		Set<IRPModelElement> thePackages = new LinkedHashSet<IRPModelElement>();
		Set<IRPModelElement> thePreferredPackages = new LinkedHashSet<IRPModelElement>();

		if( thePreferredRootPkg != null ){
			thePreferredPackages.addAll(
					_context.findModelElementsNestedUnder(
							thePreferredRootPkg, "Package", "from.*") );
		}

		thePackages.addAll( thePreferredPackages );

		// now add everything else not in a profile (i.e. superset of options)
		thePackages.addAll(
				_context.findModelElementsNestedUnder(
						_context.get_rhpPrj(), "Package", "from.*") );

		thePackages.addAll(
				_context.findElementsWithMetaClassAndStereotype(
						"Package", 
						_context.REQTS_ANALYSIS_REQUIREMENT_PACKAGE, 
						_context.get_rhpPrj(), 
						1 ) );

		_namedElementMap = new NamedElementMap( new ArrayList<IRPModelElement>( thePackages ) );

		_fromPackageComboBox = new JComboBox<Object>( _namedElementMap.getFullNames() );
		_fromPackageComboBox.setAlignmentX( LEFT_ALIGNMENT );

		_specificationTextArea = new JTextArea( 5, 20 );
		_specificationTextArea.setBorder( BorderFactory.createBevelBorder( 1 ));
		_specificationTextArea.setAlignmentX( LEFT_ALIGNMENT );

		_moveIntoCheckBox = new JCheckBox( "Move into:  " );
		_moveIntoCheckBox.setAlignmentX( LEFT_ALIGNMENT );
		_moveIntoCheckBox.setBorder( BorderFactory.createEmptyBorder(0, 0, 0, 0));

		// only select by default if preferred packages are found
		_moveIntoCheckBox.setSelected( !thePreferredPackages.isEmpty() );

		if( _moveIntoCheckBox.isSelected() ){
			_targetOwningElement = 
					_namedElementMap.getElementUsingFullName( 
							_fromPackageComboBox.getSelectedItem() );
		} else {
			_targetOwningElement = _sourceGraphElement.getDiagram();
		}

		String theProposedName =
				_context.determineUniqueNameBasedOn( 
						"derivedreqt",
						"Requirement",
						_targetOwningElement );

		_requirementSelectionPanel = new RequirementSelectionPanel( 
				"Create «deriveReqt» dependencies to:",
				_forHigherLevelReqts, 
				_forHigherLevelReqts,
				_context );

		_requirementSelectionPanel.setAlignmentX(LEFT_ALIGNMENT);

		JPanel theNamePanel = createChosenNamePanelWith( "Create a requirement called:  ", theProposedName );
		theNamePanel.setAlignmentX(LEFT_ALIGNMENT);

		_populateOnDiagramCheckBox = new JCheckBox( "Populate on diagram?" );
		_populateOnDiagramCheckBox.setSelected( true );

		JPanel thePageStartPanel = new JPanel();
		thePageStartPanel.setLayout( new BoxLayout( thePageStartPanel, BoxLayout.X_AXIS ) );
		thePageStartPanel.add( theNamePanel );
		thePageStartPanel.add( _populateOnDiagramCheckBox );

		// create panel
		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );		
		add( thePageStartPanel, BorderLayout.PAGE_START );
		add( createWestCentrePanel(), BorderLayout.CENTER );
		add( createPageEndPanel(), BorderLayout.PAGE_END );
	}

	private Component createWestCentrePanel() {

		JLabel theSpecText = new JLabel( "Specification text:" );
		theSpecText.setBorder( BorderFactory.createEmptyBorder(10, 0, 0, 0));
		theSpecText.setAlignmentX( LEFT_ALIGNMENT );

		JPanel theMoveIntoPanel = new JPanel();
		theMoveIntoPanel.setLayout( new BoxLayout( theMoveIntoPanel, BoxLayout.X_AXIS ) );
		theMoveIntoPanel.add( _moveIntoCheckBox );
		theMoveIntoPanel.add( _fromPackageComboBox );
		theMoveIntoPanel.setBorder( BorderFactory.createEmptyBorder(10, 0, 0, 0));
		theMoveIntoPanel.setAlignmentX( LEFT_ALIGNMENT );

		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.Y_AXIS ) );

		thePanel.add( _requirementSelectionPanel );
		thePanel.add( theSpecText ); 
		thePanel.add( _specificationTextArea );
		thePanel.add( theMoveIntoPanel );

		return thePanel;
	}

	private Component createPageEndPanel() {

		JLabel theLabel = new JLabel( "Do you want to proceed?" );
		theLabel.setAlignmentX(CENTER_ALIGNMENT);
		theLabel.setBorder( BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.Y_AXIS ) );	
		thePanel.setAlignmentX(CENTER_ALIGNMENT);
		thePanel.add( theLabel );
		thePanel.add( createOKCancelPanel() );

		return thePanel;
	}

	@Override
	protected boolean checkValidity(boolean isMessageEnabled) {

		String errorMsg = "";
		boolean isValid = true;

		if( _moveIntoCheckBox.isSelected() && !_context.isElementNameUnique(
				_chosenNameTextField.getText(), "Requirement", _targetOwningElement, 1 )){

			errorMsg = "Unable to proceed as the name '" + _chosenNameTextField.getText() + 
					"' is not unique in " + _context.elInfo( _targetOwningElement );

			isValid = false;

		} else if (!_context.isElementNameUnique(
				_chosenNameTextField.getText(), "Requirement", _context.get_rhpPrj(), 1)){

			errorMsg = "Unable to proceed as the name '" + _chosenNameTextField.getText() + 
					"' is not unique in " + _context.elInfo( _context.get_rhpPrj() );

			isValid = false;
		}

		if (isMessageEnabled && !isValid && errorMsg != null){
			UserInterfaceHelper.showWarningDialog( errorMsg );
		}

		return isValid;
	}

	@Override
	protected void performAction() {

		try {
			// do silent check first
			if( checkValidity( false ) ){

				if( _sourceGraphElement instanceof IRPGraphNode ){

					GraphNodeInfo theNodeInfo = new GraphNodeInfo( (IRPGraphNode) _sourceGraphElement, _context );

					int x = theNodeInfo.getTopLeftX() + 140;
					int y = theNodeInfo.getTopLeftY() + 140;

					IRPDiagram theDiagram = _sourceGraphElement.getDiagram();

					IRPRequirement theRequirement = null;

					if( theDiagram instanceof IRPActivityDiagram ){

						IRPActivityDiagram theAD = (IRPActivityDiagram)theDiagram;
						IRPFlowchart theFC = (IRPFlowchart) theAD.getFlowchart();

						theRequirement = (IRPRequirement) theFC.addNewAggr(
								"Requirement", _chosenNameTextField.getText() );

					} else if( theDiagram instanceof IRPObjectModelDiagram ||
							theDiagram instanceof IRPStatechartDiagram ||
							theDiagram instanceof IRPStructureDiagram ||
							theDiagram instanceof IRPUseCaseDiagram ){

						theRequirement = (IRPRequirement) theDiagram.addNewAggr(
								"Requirement", _chosenNameTextField.getText()  );

					} else {
						_context.warning("Warning in CreateDerivedRequirementPanel.performAction, " +
								_context.elInfo( theDiagram ) + " is not supported");
					}

					if( theRequirement != null ){

						IRPModelElement theSourceModelObject = _sourceGraphElement.getModelObject();

						addDeriveRelationsTo( theRequirement, theSourceModelObject );

						/// Create collection for completeRelations purposes
						IRPCollection theCollection = 
								RhapsodyAppServer.getActiveRhapsodyApplication().createNewCollection();

						theRequirement.setSpecification( _specificationTextArea.getText() );

						List<IRPRequirement> theUpstreamReqts = 
								_requirementSelectionPanel.getSelectedRequirementsList();

						if (theUpstreamReqts.isEmpty()){

							_context.warning("Warning in performAction, no upstream requirement was selected");

						} else {
							IRPStereotype theDeriveReqtStereotype = 
									_context.getExistingStereotype( "deriveReqt", theRequirement.getProject() );

							for (IRPRequirement theUpstreamReqt : theUpstreamReqts) {

								_context.addStereotypedDependencyIfOneDoesntExist(
										theRequirement, theUpstreamReqt, theDeriveReqtStereotype );

								if( _populateOnDiagramCheckBox.isSelected() ){
									@SuppressWarnings("unchecked")
									List<IRPGraphElement> theGraphEls = 
									theDiagram.getCorrespondingGraphicElements( theUpstreamReqt ).toList();

									for (IRPGraphElement theGraphEl : theGraphEls) {
										theCollection.addGraphicalItem( theGraphEl );
									}									
								}
							}				
						}

						if( _moveIntoCheckBox.isSelected() ){
							moveAndStereotype( theRequirement );
						}

						if( _populateOnDiagramCheckBox.isSelected() ){

							IRPGraphNode theNewReqtNode = 
									theDiagram.addNewNodeForElement( theRequirement, x, y, 300, 100 );

							theCollection.addGraphicalItem( _sourceGraphElement );
							theCollection.addGraphicalItem( theNewReqtNode );
							theDiagram.completeRelations(theCollection, 0);							
						}
					}
				}			
			} else {
				_context.error("Error in CreateDerivedRequirementPanel.performAction, checkValidity returned false");
			}	
		} catch (Exception e) {
			_context.error("Error, unhandled exception detected in CreateDerivedRequirementPanel.performAction, e=" + e.getMessage() );
		}

	}

	private void addDeriveRelationsTo(
			IRPRequirement theRequirement,
			IRPModelElement theSourceModelObject ){

		IRPStereotype theDeriveStereotype = 
				_context.getExistingStereotype( 
						"derive", theRequirement.getProject() );

		// Add a derive dependency if the source element is an 
		// accept event or call operation
		if( theSourceModelObject instanceof IRPCallOperation ){

			IRPCallOperation theCallOp = (IRPCallOperation)theSourceModelObject;

			IRPInterfaceItem theOperation = theCallOp.getOperation();

			if( theOperation != null ){

				_context.addStereotypedDependencyIfOneDoesntExist(
						theCallOp, theRequirement, theDeriveStereotype );

				_context.addStereotypedDependencyIfOneDoesntExist(
						theOperation, theRequirement, theDeriveStereotype );

			} else {
				_context.debug("Skipped adding derive dependencies as no Operation found for the call operation");
			}

		} else if ( theSourceModelObject instanceof IRPAcceptEventAction ){

			IRPAcceptEventAction theAcceptEventAction = 
					(IRPAcceptEventAction)theSourceModelObject;

			IRPInterfaceItem theEvent = theAcceptEventAction.getEvent();

			if( theEvent != null ){

				_context.addStereotypedDependencyIfOneDoesntExist(
						theAcceptEventAction, theRequirement, theDeriveStereotype );

				_context.addStereotypedDependencyIfOneDoesntExist(
						theEvent, theRequirement, theDeriveStereotype );

			} else {
				_context.debug("Skipped adding derive dependencies as no Event was found for the accept event action");
			}

		} else if ( theSourceModelObject instanceof IRPState ){

			IRPState theState = (IRPState)theSourceModelObject;

			String theStateType = theState.getStateType();

			if( theStateType.equals( "EventState") ){

				IRPSendAction theSendAction = theState.getSendAction();

				if( theSendAction != null ){
					IRPEvent theEvent = theSendAction.getEvent();

					if( theEvent != null ){

						_context.addStereotypedDependencyIfOneDoesntExist(
								theState, theRequirement, theDeriveStereotype );

						_context.addStereotypedDependencyIfOneDoesntExist(
								theEvent, theRequirement, theDeriveStereotype );

					} else {
						_context.debug("Skipped adding derive dependencies as no event found for " + _context.elInfo(theState));
					}

				} else {
					_context.debug("Error in addDeriveRelationsTo, theSendAction==null for " + _context.elInfo(theState)); 
				}

			} else if ( theStateType.equals( "Action") ){ // i.e. just text

				// don't add 

			} else {
				_context.warning("Warning in addDeriveRelationsTo, getStateType=" + theStateType + " is not supported");
			}

		} else if ( theSourceModelObject instanceof IRPOperation || 
				theSourceModelObject instanceof IRPEvent ||
				theSourceModelObject instanceof IRPAttribute ){

			_context.addStereotypedDependencyIfOneDoesntExist(
					theSourceModelObject, theRequirement, theDeriveStereotype );

		} else {
			_context.warning("Warning in addDeriveRelationsTo, " + _context.elInfo( theSourceModelObject ) + " was not handled");

		}
	}

	private void moveAndStereotype( 
			IRPRequirement theRequirement ) {

		IRPModelElement theChosenPkg = 
				_namedElementMap.getElementUsingFullName( 
						_fromPackageComboBox.getSelectedItem() );

		if( theChosenPkg instanceof IRPPackage ){

			// check if already element of same name
			IRPModelElement alreadyExistingEl = 
					theChosenPkg.findNestedElement(
							theRequirement.getName(), "Requirement" );

			if (alreadyExistingEl != null){
				_context.error("Error: Unable to move " + _context.elInfo( theRequirement ) 
						+ " as requirement of same name already exists under " 
						+ _context.elInfo( theChosenPkg ) + ". Consider renaming it.");
			} else {
				IRPStereotype theStereotypeToApply = 
						_context.getStereotypeAppliedTo( theChosenPkg, "from.*" );

				_context.debug( _context.elInfo( theStereotypeToApply ) + " is the stereotype to apply");

				if (theStereotypeToApply != null){

					theRequirement.setStereotype( theStereotypeToApply );
				}
					_context.debug("Moving " + _context.elInfo(theRequirement) 
							+ " into " + _context.elInfo(theChosenPkg));

					theRequirement.setOwner(theChosenPkg);

					_context.applyStereotypeToDeriveReqtDependenciesOriginatingFrom( 
							theRequirement, theStereotypeToApply );

					theRequirement.highLightElement();
			}

		} else {
			_context.error("Error in moveAndStereotype, no package was selected for the move");
		}
	}
}

/**
 * Copyright (C) 2016-2021  MBSE Training and Consulting Limited (www.executablembse.com)

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

