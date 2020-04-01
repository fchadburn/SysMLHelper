package functionalanalysisplugin;

import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.UserInterfaceHelpers;

import java.awt.BorderLayout;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.telelogic.rhapsody.core.*;

public class CreateOperationPanel extends CopyOfCreateTracedElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JCheckBox m_CallOperationIsNeededCheckBox;

	public static void main(String[] args) {
		launchThePanel();
	}
	
	public static void launchThePanel(){

		final String theAppID = 
				UserInterfaceHelpers.getAppIDIfSingleRhpRunningAndWarnUserIfNot();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );

				UserInterfaceHelpers.setLookAndFeel();

				JFrame frame = new JFrame(
						"Create an operation" );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateOperationPanel thePanel = new CreateOperationPanel(
						theAppID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}
	
	public CreateOperationPanel(
			String theAppID ){
		
		super( theAppID );
		
		IRPClass theBuildingBlock = 
				m_ElementContext.getBuildingBlock();

		if( theBuildingBlock == null ){
			
			buildUnableToRunDialog( 
					"Sorry, this helper is unable to run this command because \n" +
					"there was no execution context or block found in the model. \n " +
					"You need to add the relevant package structure first." );
			
		} else { // theBuildingBlock != null
			
			IRPClass theBlock = m_ElementContext.getBlockUnderDev(
					"Which Block do you want to add the Operation to?" );
			
			if( theBlock == null ){
				buildUnableToRunDialog( 
						"Sorry, this helper is unable to run this command because \n" +
						"there was no execution context or block found in the model. \n " +
						"You need to add the relevant package structure first." );
			} else {
				createCommonContent(
						m_ElementContext.getSelectedEl(),
						m_ElementContext.getSelectedReqts(), 
						theBlock );
			}
		}

	}
	
	private void createCommonContent(
			IRPModelElement forSourceModelElement,
			Set<IRPRequirement> withReqtsAlsoAdded,
			IRPClassifier onTargetBlock ){
		
		String theSourceText = GeneralHelpers.getActionTextFrom( forSourceModelElement );	
		
		if( theSourceText == null ){
			theSourceText = "function_name";
		}
		
		Logger.writeLine("CreateOperationPanel constructor called with text '" + theSourceText + "'");
		
		String theProposedName = GeneralHelpers.determineUniqueNameBasedOn( 
				GeneralHelpers.toMethodName( theSourceText, 40 ), 
				"Operation", 
				onTargetBlock );					
		
		Logger.writeLine("The proposed name is '" + theProposedName + "'");
		
		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

		m_RequirementsPanel.setAlignmentX( LEFT_ALIGNMENT );
		
		JPanel theNamePanel = createChosenNamePanelWith( "Create an operation called:  ", theProposedName );
		theNamePanel.setAlignmentX(LEFT_ALIGNMENT);
		
		m_CallOperationIsNeededCheckBox = new JCheckBox("Populate on diagram?");
		setupPopulateCheckbox( m_CallOperationIsNeededCheckBox );
				
		JPanel thePageStartPanel = new JPanel();
		thePageStartPanel.setLayout( new BoxLayout( thePageStartPanel, BoxLayout.X_AXIS ) );
		thePageStartPanel.add( theNamePanel );
		thePageStartPanel.add( m_CallOperationIsNeededCheckBox );
		
		add( thePageStartPanel, BorderLayout.PAGE_START );
		add( m_RequirementsPanel, BorderLayout.WEST );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}
	
	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled ){
		
		String errorMessage = null;
		boolean isValid = true;
		
		String theChosenName = m_ChosenNameTextField.getText();
		IRPClass theChosenBlock = m_ElementContext.getChosenBlock();
		
		boolean isLegalName = GeneralHelpers.isLegalName( theChosenName, theChosenBlock );
		
		if( !isLegalName ){
			
			errorMessage = theChosenName + " is not legal as an identifier representing an operation\n";				
			isValid = false;
			
		} else if (!GeneralHelpers.isElementNameUnique(
				theChosenName, 
				"Operation", 
				m_ElementContext.getChosenBlock(),//m_TargetOwningElement, 
				1)){

			errorMessage = "Unable to proceed as the name '" + theChosenName + "' is not unique";
			isValid = false;
		}

		if (isMessageEnabled && !isValid && errorMessage != null){

			UserInterfaceHelpers.showWarningDialog( errorMessage );
		}
		
		return isValid;
	}

	@Override
	protected void performAction() {

		// it is assumed that checkValidity has returned true

		IRPOperation theOperation = 
				m_ElementContext.getChosenBlock().addOperation(
						m_ChosenNameTextField.getText() );	

		IRPGraphElement theSourceGraphElement = m_ElementContext.getSelectedGraphEl();

		if( theSourceGraphElement != null ){

			if( theSourceGraphElement.getModelObject() instanceof IRPCallOperation ){

				IRPCallOperation theCallOp = (IRPCallOperation) theSourceGraphElement.getModelObject();

				IRPInterfaceItem theExistingOp = theCallOp.getOperation();

				if( theExistingOp == null ){

					Logger.writeLine("Setting the " + Logger.elementInfo( theCallOp ) + 
							" to " + Logger.elementInfo(theOperation) );

					theCallOp.setOperation( theOperation );

					String theProposedName = 
							GeneralHelpers.determineUniqueNameBasedOn( 
									GeneralHelpers.toMethodName( theOperation.getName(), 40 ), 
									"CallOperation", 
									theCallOp.getOwner() );

					theCallOp.setName( theProposedName );
				}
			} else {
				List<IRPRequirement> theSelectedReqtsList = m_RequirementsPanel.getSelectedRequirementsList();
				addTraceabilityDependenciesTo( theOperation, theSelectedReqtsList );
				m_ElementContext.bleedColorToElementsRelatedTo( theSelectedReqtsList );
			}
		}

		if( m_CallOperationIsNeededCheckBox.isSelected() ){
			populateCallOperationActionOnDiagram( theOperation );
		}

		theOperation.highLightElement();
	}
}

/**
 * Copyright (C) 2016-2019  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #022 30-MAY-2016: Improved handling and validation of event/operation creation by adding new forms (F.J.Chadburn) 
    #032 05-JUN-2016: Populate call operation/event actions on diagram check-box added (F.J.Chadburn)
    #033 05-JUN-2016: Add support for creation of operations and events from raw requirement selection (F.J.Chadburn)
    #034 05-JUN-2016: Re-factored design to move static constructors into appropriate panel class (F.J.Chadburn)
    #042 29-JUN-2016: launchThePanel renaming to improve Panel class design consistency (F.J.Chadburn)
    #043 03-JUL-2016: Add Derive downstream reqt for CallOps, InterfaceItems and Event Actions (F.J.Chadburn)
    #058 13-JUL-2016: Dropping CallOp on diagram now gives option to create Op on block (F.J.Chadburn)
	#078 28-JUL-2016: Added isPopulateWantedByDefault tag to FunctionalAnalysisPkg to give user option (F.J.Chadburn)
    #089 15-AUG-2016: Add a pull-down list to select Block when adding events/ops in white box (F.J.Chadburn)
    #093 23-AUG-2016: Added isPopulateOptionHidden tag to allow hiding of the populate check-box on dialogs (F.J.Chadburn)
    #099 14-SEP-2016: Allow event and operation creation from right-click on AD and RD diagram canvas (F.J.Chadburn)
    #115 13-NOV-2016: Removed use of isEnableBlockSelectionByUser tag and <<LogicalSystem>> by helper (F.J.Chadburn)
    #125 25-NOV-2016: AutoRipple used in UpdateTracedAttributePanel to keep check and FlowPort name updated (F.J.Chadburn)
    #130 25-NOV-2016: Improved consistency in handling of isPopulateOptionHidden and isPopulateWantedByDefault tags (F.J.Chadburn)
    #154 25-JAN-2017: Improved robustness by adding isLegalName check to CreateOperationPanel (F.J.Chadburn)
    #186 29-MAY-2017: Add context string to getBlockUnderDev to make it clearer for user when selecting (F.J.Chadburn)
    #196 05-JUN-2017: Enhanced create traced element dialogs to be context aware for blocks/parts (F.J.Chadburn)
    #245 11-OCT-2017: Fixed exception on CallOperation action drop when using detailed ADs with Ops (F.J.Chadburn)
    #252 29-MAY-2019: Implement generic features for profile/settings loading (F.J.Chadburn)
    #256 29-MAY-2019: Rewrite to Java Swing dialog launching to make thread safe between versions (F.J.Chadburn)

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
