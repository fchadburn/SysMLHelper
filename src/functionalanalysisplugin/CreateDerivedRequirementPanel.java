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

import requirementsanalysisplugin.RequirementsAnalysisPlugin;

import com.mbsetraining.sysmlhelper.common.GraphNodeInfo;
import com.mbsetraining.sysmlhelper.common.NamedElementMap;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.telelogic.rhapsody.core.*;

import generalhelpers.UserInterfaceHelpers;

public class CreateDerivedRequirementPanel extends CreateTracedElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	NamedElementMap m_NamedElementMap;
	private JComboBox<Object> m_FromPackageComboBox = null;
	private JTextArea m_Specification = null;
	private RequirementSelectionPanel m_RequirementSelectionPanel;
	private JCheckBox m_PopulateOnDiagramCheckBox; 
	private JCheckBox m_MoveIntoCheckBox; 
	
	public void deriveDownstreamRequirement(
			List<IRPGraphElement> theSelectedGraphEls) {
		
		int selectedCount = theSelectedGraphEls.size();
		
		if (selectedCount > 1){
			_context.warning("Warning in deriveDownstreamRequirement, operation only works for " +
					"1 selected element and " + selectedCount + " were selected");
			
		} else if (selectedCount == 0){
			_context.error("Error in deriveDownstreamRequirement, no elements were selected");	
			
		} else {		
			IRPGraphElement theSelectedGraphEl = theSelectedGraphEls.get(0);
			IRPModelElement theModelObject = theSelectedGraphEl.getModelObject();
			
			if (theModelObject instanceof IRPRequirement){
				
				Set<IRPRequirement> theReqts = new HashSet<IRPRequirement>();
				
				theReqts.add( (IRPRequirement)theModelObject );
				
				CreateDerivedRequirementPanel.launchThePanel( 
						theSelectedGraphEls.get(0), 
						theReqts, 
						RequirementsAnalysisPlugin.getActiveProject() );

			} else if (theModelObject instanceof IRPCallOperation){
				
				IRPCallOperation theCallOp = (IRPCallOperation)theModelObject;
				IRPInterfaceItem theOperation = theCallOp.getOperation();
				
				Set<IRPRequirement> theReqts = 
						_context.getRequirementsThatTraceFromWithStereotype(
								theOperation, "satisfy");

				CreateDerivedRequirementPanel.launchThePanel( 
						theSelectedGraphEls.get(0), 
						theReqts, 
						RequirementsAnalysisPlugin.getActiveProject() );
			
			} else if (theModelObject instanceof IRPAcceptEventAction){
			
				IRPAcceptEventAction theAcceptEvent = (IRPAcceptEventAction)theModelObject;
				
				// not all accept events will have an event
				IRPInterfaceItem theEvent = theAcceptEvent.getEvent();

				if( theEvent != null ){
					Set<IRPRequirement> theReqts = 
							_context.getRequirementsThatTraceFromWithStereotype(
									theEvent, "satisfy");

					CreateDerivedRequirementPanel.launchThePanel( 
							theSelectedGraphEls.get(0), 
							theReqts, 
							RequirementsAnalysisPlugin.getActiveProject() );			
				} else {

					Set<IRPRequirement> theReqts = 
							_context.getRequirementsThatTraceFromWithStereotype(
									theAcceptEvent, "derive");

					CreateDerivedRequirementPanel.launchThePanel( 
							theSelectedGraphEls.get(0), 
							theReqts, 
							RequirementsAnalysisPlugin.getActiveProject() );
				}

			} else if (theModelObject instanceof IRPState) { // SendAction

				IRPState theState = (IRPState)theModelObject;
				String theStateType = theState.getStateType();
				
				if (theStateType.equals("EventState")){ // receive event
					
					IRPSendAction theSendAction = theState.getSendAction();
					
					if (theSendAction != null){
						IRPEvent theEvent = theSendAction.getEvent();
						
						if (theEvent != null){
							Set<IRPRequirement> theReqts = 
									_context.getRequirementsThatTraceFromWithStereotype(
											theEvent, "satisfy");

							CreateDerivedRequirementPanel.launchThePanel( 
									theSelectedGraphEls.get(0), 
									theReqts, 
									RequirementsAnalysisPlugin.getActiveProject() );
						} else {
							Set<IRPRequirement> theReqts = 
									_context.getRequirementsThatTraceFromWithStereotype(
											theState, "derive");

							CreateDerivedRequirementPanel.launchThePanel( 
									theSelectedGraphEls.get(0), 
									theReqts, 
									RequirementsAnalysisPlugin.getActiveProject() );
						}
					} else {
						_context.error("Error in deriveDownstreamRequirement, theSendAction is null");
					}
					
				} else if (theStateType.equals("Action")){
					
					Set<IRPRequirement> theReqts = 
							_context.getRequirementsThatTraceFromWithStereotype(
									theState, "derive");

					CreateDerivedRequirementPanel.launchThePanel( 
							theSelectedGraphEls.get(0), 
							theReqts, 
							RequirementsAnalysisPlugin.getActiveProject() );
					
				} else {
					_context.warning("Warning in deriveDownstreamRequirement, this operation is not supported for theState.getStateType()=" + theState.getStateType());
				}

			} else if (theModelObject instanceof IRPOperation || 
					  (theModelObject instanceof IRPEvent) ||
					  (theModelObject instanceof IRPAttribute) ){
				
				Set<IRPRequirement> theReqts = 
						_context.getRequirementsThatTraceFromWithStereotype(
								theModelObject, "satisfy");

				CreateDerivedRequirementPanel.launchThePanel( 
						theSelectedGraphEls.get(0), 
						theReqts, 
						RequirementsAnalysisPlugin.getActiveProject() );
				
			} else { 

				_context.warning( "Warning in , " + _context.elInfo(theModelObject) + 
						" was not handled as call with this type was not expected" );
			}
		}
	}
	
	public static void launchThePanel(
			final IRPGraphElement theSourceGraphElement,
			final Set<IRPRequirement> forHigherLevelReqts,
			final IRPProject inTheProject ){

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {				
				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame(
						"Derive a new requirement from " + _context.elInfo(theSourceGraphElement.getModelObject()) + "?");

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateDerivedRequirementPanel thePanel = 
						new CreateDerivedRequirementPanel( 
								theSourceGraphElement, forHigherLevelReqts, inTheProject );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}
	
	CreateDerivedRequirementPanel(
			final IRPGraphElement theSourceGraphElement,
			final Set<IRPRequirement> forHigherLevelReqts,
			final IRPProject inTheProject ){
		
		super(theSourceGraphElement, forHigherLevelReqts, null, inTheProject);
		
		_context.writeLine( "Create Derived Reqt panel invoked from " + _context.elInfo( m_SourceGraphElement.getModelObject() ) );
		
		final String preferredRootPackageName = "FunctionalAnalysisPkg";
		
		IRPModelElement thePreferredRootPkg = 
				m_Project.findElementsByFullName( preferredRootPackageName, "Package" );
		
		Set<IRPModelElement> thePackages = new LinkedHashSet<IRPModelElement>();
		Set<IRPModelElement> thePreferredPackages = new LinkedHashSet<IRPModelElement>();
		
		if( thePreferredRootPkg != null ){
			thePreferredPackages.addAll(
					GeneralHelpers.findModelElementsNestedUnder(
							thePreferredRootPkg, "Package", "from.*") );
		}
		
		thePackages.addAll( thePreferredPackages );
		
		// now add everything else not in a profile (i.e. superset of options)
		thePackages.addAll(
				_context.findModelElementsNestedUnder(
						_context.get_rhpPrj(), "Package", "from.*") );
		 
		m_NamedElementMap = new NamedElementMap( new ArrayList<IRPModelElement>( thePackages ) );
		
		m_FromPackageComboBox = new JComboBox<Object>( m_NamedElementMap.getFullNames() );
		m_FromPackageComboBox.setAlignmentX( LEFT_ALIGNMENT );
		
		m_Specification = new JTextArea( 5, 20 );
		m_Specification.setBorder( BorderFactory.createBevelBorder( 1 ));
		m_Specification.setAlignmentX( LEFT_ALIGNMENT );

		m_MoveIntoCheckBox = new JCheckBox( "Move into:  " );
		m_MoveIntoCheckBox.setAlignmentX( LEFT_ALIGNMENT );
		m_MoveIntoCheckBox.setBorder( BorderFactory.createEmptyBorder(0, 0, 0, 0));
		
		// only select by default if preferred packages are found
		m_MoveIntoCheckBox.setSelected( !thePreferredPackages.isEmpty() );
		
		if( m_MoveIntoCheckBox.isSelected() ){
			m_TargetOwningElement = 
					m_NamedElementMap.getElementUsingFullName( 
							m_FromPackageComboBox.getSelectedItem() );
		} else {
			m_TargetOwningElement = theSourceGraphElement.getDiagram();
		}
		
		String theProposedName =
				_context.determineUniqueNameBasedOn( 
						"derivedreqt",
						"Requirement",
						m_TargetOwningElement );
		
		m_RequirementSelectionPanel = new RequirementSelectionPanel( 
				"Create «deriveReqt» dependencies to:",
				forHigherLevelReqts, 
				forHigherLevelReqts,
				_context );
		
		m_RequirementSelectionPanel.setAlignmentX(LEFT_ALIGNMENT);
		
		JPanel theNamePanel = createChosenNamePanelWith( "Create a requirement called:  ", theProposedName );
		theNamePanel.setAlignmentX(LEFT_ALIGNMENT);
		
		m_PopulateOnDiagramCheckBox = new JCheckBox( "Populate on diagram?" );
		m_PopulateOnDiagramCheckBox.setSelected( true );
		
		JPanel thePageStartPanel = new JPanel();
		thePageStartPanel.setLayout( new BoxLayout( thePageStartPanel, BoxLayout.X_AXIS ) );
		thePageStartPanel.add( theNamePanel );
		thePageStartPanel.add( m_PopulateOnDiagramCheckBox );
		
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
		theMoveIntoPanel.add( m_MoveIntoCheckBox );
		theMoveIntoPanel.add( m_FromPackageComboBox );
		theMoveIntoPanel.setBorder( BorderFactory.createEmptyBorder(10, 0, 0, 0));
		theMoveIntoPanel.setAlignmentX( LEFT_ALIGNMENT );

		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.Y_AXIS ) );
		
		thePanel.add( m_RequirementSelectionPanel );
		thePanel.add( theSpecText ); 
		thePanel.add( m_Specification );
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

		if( m_MoveIntoCheckBox.isSelected() && !_context.isElementNameUnique(
					m_ChosenNameTextField.getText(), "Requirement", m_TargetOwningElement, 1 )){

			errorMsg = "Unable to proceed as the name '" + m_ChosenNameTextField.getText() + 
					"' is not unique in " + _context.elInfo( m_TargetOwningElement );

			isValid = false;

		} else if (!_context.isElementNameUnique(
					m_ChosenNameTextField.getText(), "Requirement", _context.get_rhpPrj(), 1)){

			errorMsg = "Unable to proceed as the name '" + m_ChosenNameTextField.getText() + 
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
				
				if( m_SourceGraphElement != null && m_SourceGraphElement instanceof IRPGraphNode ){

					GraphNodeInfo theNodeInfo = new GraphNodeInfo( (IRPGraphNode) m_SourceGraphElement, _context );
					
					int x = theNodeInfo.getTopLeftX() + 140;
					int y = theNodeInfo.getTopLeftY() + 140;

					IRPDiagram theDiagram = m_SourceGraphElement.getDiagram();

					IRPRequirement theRequirement = null;
					
					if( theDiagram instanceof IRPActivityDiagram ){
						
						IRPActivityDiagram theAD = (IRPActivityDiagram)theDiagram;
						IRPFlowchart theFC = (IRPFlowchart) theAD.getFlowchart();
						
						theRequirement = (IRPRequirement) theFC.addNewAggr(
								"Requirement", m_ChosenNameTextField.getText() );
						
					} else if( theDiagram instanceof IRPObjectModelDiagram ||
							   theDiagram instanceof IRPStatechartDiagram ||
							   theDiagram instanceof IRPStructureDiagram ||
							   theDiagram instanceof IRPUseCaseDiagram ){
					
						theRequirement = (IRPRequirement) theDiagram.addNewAggr(
								"Requirement", m_ChosenNameTextField.getText()  );
						
					} else {
						_context.warning("Warning in CreateDerivedRequirementPanel.performAction, " +
								_context.elInfo( theDiagram ) + " is not supported");
					}
					
					if( theRequirement != null ){
						
						IRPModelElement theSourceModelObject = m_SourceGraphElement.getModelObject();
						
						addDeriveRelationsTo( theRequirement, theSourceModelObject );

						/// Create collection for completeRelations purposes
						IRPCollection theCollection = 
								RhapsodyAppServer.getActiveRhapsodyApplication().createNewCollection();
						
						theRequirement.setSpecification( m_Specification.getText() );
						
						List<IRPRequirement> theUpstreamReqts = 
								m_RequirementSelectionPanel.getSelectedRequirementsList();

						if (theUpstreamReqts.isEmpty()){
							
							_context.warning("Warning in performAction, no upstream requirement was selected");
							
						} else {
							IRPStereotype theDeriveReqtStereotype = 
									_context.getExistingStereotype( "deriveReqt", theRequirement.getProject() );

							for (IRPRequirement theUpstreamReqt : theUpstreamReqts) {
								
								_context.addStereotypedDependencyIfOneDoesntExist(
										theRequirement, theUpstreamReqt, theDeriveReqtStereotype );
								
								if( m_PopulateOnDiagramCheckBox.isSelected() ){
									@SuppressWarnings("unchecked")
									List<IRPGraphElement> theGraphEls = 
											theDiagram.getCorrespondingGraphicElements( theUpstreamReqt ).toList();
									
									for (IRPGraphElement theGraphEl : theGraphEls) {
										theCollection.addGraphicalItem( theGraphEl );
									}									
								}
							}				
						}
						
						if( m_MoveIntoCheckBox.isSelected() ){
							moveAndStereotype( theRequirement );
						}
						
						if( m_PopulateOnDiagramCheckBox.isSelected() ){
					
							IRPGraphNode theNewReqtNode = 
									theDiagram.addNewNodeForElement( theRequirement, x, y, 300, 100 );
							
							theCollection.addGraphicalItem( m_SourceGraphElement );
							theCollection.addGraphicalItem( theNewReqtNode );
							theDiagram.completeRelations(theCollection, 0);							
						}
					}
				}			
			} else {
				_context.error("Error in CreateDerivedRequirementPanel.performAction, checkValidity returned false");
			}	
		} catch (Exception e) {
			_context.error("Error, unhandled exception detected in CreateDerivedRequirementPanel.performAction");
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
				m_NamedElementMap.getElementUsingFullName( 
						m_FromPackageComboBox.getSelectedItem() );
			
		if( theChosenPkg != null && theChosenPkg instanceof IRPPackage ){
				
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

					_context.debug("Moving " + _context.elInfo(theRequirement) 
							+ " into " + _context.elInfo(theChosenPkg));
						
					theRequirement.setOwner(theChosenPkg);
					
					_context.applyStereotypeToDeriveReqtDependenciesOriginatingFrom( 
							theRequirement, theStereotypeToApply );
						
					theRequirement.highLightElement();
				}
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

