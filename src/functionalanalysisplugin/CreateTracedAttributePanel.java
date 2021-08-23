package functionalanalysisplugin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.telelogic.rhapsody.core.*;

import designsynthesisplugin.PortCreator;

public class CreateTracedAttributePanel extends CreateTracedElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected JTextField m_InitialValueTextField = null;
	private JCheckBox m_CheckOperationCheckBox;
	private String m_CheckOpName;
	private JCheckBox m_CallOperationIsNeededCheckBox;
	private JRadioButton m_NoFlowPort;
	private JRadioButton m_PubFlowPort;
	private JRadioButton m_SubFlowPort;
	
	public static void launchThePanel(
			String theAppID ){

		javax.swing.SwingUtilities.invokeLater( new Runnable(){

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );

				UserInterfaceHelper.setLookAndFeel();

				JFrame frame = new JFrame(
						"Create attribute/flow-port/check operation" );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateTracedAttributePanel thePanel = new CreateTracedAttributePanel(
						theAppID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}
	
	public CreateTracedAttributePanel(
			String theAppID ){
		
		super( theAppID );
			
		IRPClass theBuildingBlock = 
				_selectionContext.getBuildingBlock();

		if( theBuildingBlock == null ){
			
			buildUnableToRunDialog( 
					"Sorry, this helper is unable to run this command because \n" +
					"there was no execution context or block found in the model. \n " +
					"You need to add the relevant package structure first." );
			
		} else { // theBuildingBlock != null
			
			IRPClass theBlock = _selectionContext.getBlockUnderDev(
					"Which Block do you want to add the Operation to?" );
			
			if( theBlock == null ){
				buildUnableToRunDialog( 
						"Sorry, this helper is unable to run this command because \n" +
						"there was no execution context or block found in the model. \n " +
						"You need to add the relevant package structure first." );
			} else {
				
				IRPModelElement theModelObject = null;
				IRPGraphElement theSelectedGraphEl = _selectionContext.getSelectedGraphEl();
				String theSourceText = "attributeName";

				if( theSelectedGraphEl != null ){
					theModelObject = theSelectedGraphEl.getModelObject();
				}
				
				if( theModelObject != null ){
					theSourceText = _context.getActionTextFrom( theModelObject );
				}
				
				createCommonContent( _selectionContext.getChosenBlock(), theSourceText );
			}
		}

	}
	
	private void createCommonContent(
			IRPClassifier onTargetBlock,
			String theSourceText) {
		
		String theProposedName = _context.determineUniqueNameBasedOn( 
				_context.toMethodName( theSourceText, 40 ), 
				"Attribute", 
				onTargetBlock );

		_context.debug("The proposed name is '" + theProposedName + "'");
		
		JPanel thePageStartPanel = new JPanel();
		thePageStartPanel.setLayout( new BoxLayout( thePageStartPanel, BoxLayout.X_AXIS ) );
		
		JPanel theNamePanel = createChosenNamePanelWith( "Create an attribute called:  ", theProposedName );
		theNamePanel.setAlignmentX(LEFT_ALIGNMENT);
		thePageStartPanel.add( theNamePanel );

		JPanel theInitialValuePanel = createInitialValuePanel( "0" );
		theInitialValuePanel.setAlignmentX( LEFT_ALIGNMENT );
		thePageStartPanel.add( theInitialValuePanel );

		JPanel theCenterPanel = new JPanel();
		theCenterPanel.setLayout( new BoxLayout( theCenterPanel, BoxLayout.Y_AXIS ) );

		m_CheckOperationCheckBox = new JCheckBox();
		m_CheckOperationCheckBox.setBorder( BorderFactory.createEmptyBorder( 0, 0, 10, 0 ) );

		m_CheckOperationCheckBox.addActionListener( new ActionListener() {
			
		      public void actionPerformed(ActionEvent actionEvent) {
		    	  
			        AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			        
			        boolean selected = abstractButton.getModel().isSelected();
					
			        IRPDiagram theSourceDiagram = _selectionContext.getSourceDiagram();
			        
					boolean isPopulate = false;
					
					if( theSourceDiagram != null ){
						
						isPopulate = _context.getIsPopulateWantedByDefault(
								theSourceDiagram );
					}
					
			        m_CallOperationIsNeededCheckBox.setEnabled( selected );
			        m_CallOperationIsNeededCheckBox.setSelected( selected && isPopulate );
			        			        
			      }} );
		
		_chosenNameTextField.getDocument().addDocumentListener(
				new DocumentListener() {

					@Override
					public void changedUpdate(DocumentEvent arg0) {
						updateNames();					
					}

					@Override
					public void insertUpdate(DocumentEvent arg0) {
						updateNames();
					}

					@Override
					public void removeUpdate(DocumentEvent arg0) {
						updateNames();
					}	
				});
		
		m_CheckOperationCheckBox.setSelected(true);
		m_CheckOperationCheckBox.setEnabled(true);

		m_CallOperationIsNeededCheckBox = new JCheckBox("Populate the '" + m_CheckOpName + "' operation on diagram?");
		m_CallOperationIsNeededCheckBox.setBorder( BorderFactory.createEmptyBorder( 0, 0, 10, 0 ) );
		setupPopulateCheckbox( m_CallOperationIsNeededCheckBox );
		
		updateNames();
		
		theCenterPanel.add( m_CheckOperationCheckBox );
		theCenterPanel.add( m_CallOperationIsNeededCheckBox );

		m_NoFlowPort  = new JRadioButton( "None", true );
		m_PubFlowPort = new JRadioButton( "«Pub»" );
		m_SubFlowPort = new JRadioButton( "«Sub»" );
		
		ButtonGroup group = new ButtonGroup();
		group.add( m_NoFlowPort );
		group.add( m_PubFlowPort );
		group.add( m_SubFlowPort );

		JPanel theFlowPortOptions = new JPanel();
		theFlowPortOptions.setLayout( new BoxLayout( theFlowPortOptions, BoxLayout.LINE_AXIS ) );
		theFlowPortOptions.setAlignmentX( LEFT_ALIGNMENT );
		theFlowPortOptions.add ( new JLabel("Create a FlowPort: ") );
		theFlowPortOptions.add( m_NoFlowPort );
		theFlowPortOptions.add( m_PubFlowPort );
		theFlowPortOptions.add( m_SubFlowPort );
	    
		theCenterPanel.add( theFlowPortOptions );
		
		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		
		add( theCenterPanel, BorderLayout.WEST );
		_requirementSelectionPanel.setAlignmentX(LEFT_ALIGNMENT);
		theCenterPanel.add( _requirementSelectionPanel );
				
		add( thePageStartPanel, BorderLayout.PAGE_START );
		add( theCenterPanel, BorderLayout.WEST );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}

	private JPanel createInitialValuePanel(String withValue){
		
		JLabel theLabel =  new JLabel(" with the initial value:  ");
		
		m_InitialValueTextField = new JTextField();
		m_InitialValueTextField.setText( withValue );
		m_InitialValueTextField.setPreferredSize( new Dimension( 100, 20 ) );
		m_InitialValueTextField.setMaximumSize( new Dimension( 100, 20 ) );
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.X_AXIS ) );	
		thePanel.add( theLabel );
		thePanel.add( m_InitialValueTextField );
		
		return thePanel;
	}
	
//	public static void launchThePanel(
//			final IRPGraphElement selectedDiagramEl, 
//			final IRPModelElement orTheModelElement,
//			final Set<IRPRequirement> withReqtsAlsoAdded,
//			final IRPProject inProject){
//
//		javax.swing.SwingUtilities.invokeLater(new Runnable() {
//
//			@Override
//			public void run() {
//				
//				IRPClass theBlock = getBlock( 
//						selectedDiagramEl, 
//						orTheModelElement, 
//						"Select Block to add attribute to:" );
//						
//				JFrame.setDefaultLookAndFeelDecorated( true );
//
//				JFrame frame = new JFrame(
//						"Create an attribute on " + theBlock.getUserDefinedMetaClass() 
//						+ " called " + theBlock.getName());
//
//				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
//
//				if( selectedDiagramEl != null ){
//					
//					CreateTracedAttributePanel thePanel = 
//							new CreateTracedAttributePanel(
//									selectedDiagramEl, 
//									withReqtsAlsoAdded,
//									theBlock );
//
//					frame.setContentPane( thePanel );
//					
//				} else if( orTheModelElement != null ){
//					
//					CreateTracedAttributePanel thePanel = 
//							new CreateTracedAttributePanel(
//									orTheModelElement, 
//									withReqtsAlsoAdded,
//									theBlock );
//
//					frame.setContentPane( thePanel );
//				}
//
//				frame.pack();
//				frame.setLocationRelativeTo( null );
//				frame.setVisible( true );
//			}
//		});			
//	}
	
	private void updateNames(){
		
		m_CheckOpName = _context.determineBestCheckOperationNameFor(
				_selectionContext.getChosenBlock(),
				_chosenNameTextField.getText(),
				40 );
		
		m_CheckOperationCheckBox.setText(
				"Add a '" + m_CheckOpName + 
				"' operation to the block that returns the attribute value" );
		
		m_CallOperationIsNeededCheckBox.setText(
				"Populate the '" + m_CheckOpName + "' operation on diagram?");
	}
	
	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled ){
		
		String errorMessage = "";
		boolean isValid = true;
		
		String theChosenName = _chosenNameTextField.getText();
		
		boolean isLegalName = _context.isLegalName( theChosenName, _selectionContext.getChosenBlock() );
		
		if( !isLegalName ){
			
			errorMessage += theChosenName + " is not legal as an identifier representing an executable attribute\n";				
			isValid = false;
			
		} else if( !_context.isElementNameUnique(
				_chosenNameTextField.getText(), 
				"Attribute", 
				_selectionContext.getChosenBlock(), 
				1 ) ){

			errorMessage = "Unable to proceed as the name '" + _chosenNameTextField.getText() + "' is not unique";
			isValid = false;

		} else if( m_CheckOperationCheckBox.isSelected() && 
				   !_context.isElementNameUnique(
						   m_CheckOpName,
						   "Operation",
						   _selectionContext.getChosenBlock(), 
						   1 ) ){

			errorMessage = "Unable to proceed as the derived check operation name '" + 
					m_CheckOpName + "' is not unique";
			
			isValid = false;
			
		} else if (!isInteger( m_InitialValueTextField.getText() )){
			
			errorMessage = "Unable to proceed as the initial value '" + _chosenNameTextField.getText() + "' is not an integer";
			isValid = false;
		}

		if (isMessageEnabled && !isValid && errorMessage != null){

			UserInterfaceHelper.showWarningDialog( errorMessage );
		}
		
		return isValid;
	}
	
	public static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	        
	    } catch(NumberFormatException e) { 
	        return false; 
	        
	    } catch(NullPointerException e) {
	        return false;
	    }
	    
	    return true;
	}

	
	@Override
	protected void performAction() {
		
		// do silent check first
		if (checkValidity( false )){

			List<IRPRequirement> selectedReqtsList = _requirementSelectionPanel.getSelectedRequirementsList();

			IRPAttribute theAttribute = addAttributeTo( 
					_selectionContext.getChosenBlock(), 
					_chosenNameTextField.getText(), 
					m_InitialValueTextField.getText(),
					selectedReqtsList );
												
			if( m_CheckOperationCheckBox.isSelected() ){
				
				IRPOperation theCheckOp = addCheckOperationFor( theAttribute, m_CheckOpName );
				addTraceabilityDependenciesTo( theCheckOp, selectedReqtsList );	
				
				if( m_CallOperationIsNeededCheckBox.isSelected() ){
					populateCallOperationActionOnDiagram( theCheckOp );
				}
				
				theCheckOp.highLightElement();
			}
			
			if( _selectionContext.getSelectedGraphEl() != null ){
				bleedColorToElementsRelatedTo( _selectionContext.getSelectedGraphEl() );
			}
			
			PortCreator theCreator = new PortCreator(_context);
			
			if( m_PubFlowPort.isSelected() ){
				theCreator.createPublishFlowportFor( theAttribute );
			}

			if( m_SubFlowPort.isSelected() ){
				theCreator.createSubscribeFlowportFor( theAttribute );
			}
			
			theAttribute.highLightElement();
			
		} else {
			_context.error("Error in CreateOperationPanel.performAction, checkValidity returned false");
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