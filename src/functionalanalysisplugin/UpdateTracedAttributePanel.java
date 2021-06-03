package functionalanalysisplugin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class UpdateTracedAttributePanel extends CreateTracedElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected JTextField m_InitialValueTextField = null;
	private JCheckBox m_CheckOperationCheckBox;
	private String m_CheckOpName;
	private IRPOperation m_ExistingCheckOp = null;
	private IRPSysMLPort m_ExistingFlowPort = null;
		
	private UpdateTracedAttributePanel(
		String theAppID ){
/*			IRPAttribute forExistingAttribute, 
			final Set<IRPRequirement> withReqtsAlsoAdded,
			IRPClassifier onTargetBlock) {
	*/	
		//super( forExistingAttribute, withReqtsAlsoAdded, onTargetBlock, onTargetBlock.getProject() );
		super( theAppID );

		if( !(_context.getSelectedElement() instanceof IRPAttribute) ){
			_context.error( _context.elInfo(_context.getSelectedElement() ) + " is not an attribut" );
		}
		
		
		Set<IRPRequirement> theReqts = 
				_context.getRequirementsThatTraceFrom( 
						_context.getSelectedElement(), false );
		
		IRPAttribute forExistingAttribute = (IRPAttribute)_context.getSelectedElement();

		String theProposedName = forExistingAttribute.getName();
						
		_context.debug( "The proposed name is '" + theProposedName + "'" );
		
		JPanel thePageStartPanel = new JPanel();
		thePageStartPanel.setLayout( new BoxLayout( thePageStartPanel, BoxLayout.X_AXIS ) );
		
		JPanel theNamePanel = createChosenNamePanelWith( "Update attribute called:  ", theProposedName );
		theNamePanel.setAlignmentX(LEFT_ALIGNMENT);
		thePageStartPanel.add( theNamePanel );

		JPanel theInitialValuePanel = createInitialValuePanel( forExistingAttribute.getDefaultValue() );
		theInitialValuePanel.setAlignmentX( LEFT_ALIGNMENT );
		thePageStartPanel.add( theInitialValuePanel );

		JPanel theCenterPanel = new JPanel();
		theCenterPanel.setLayout( new BoxLayout( theCenterPanel, BoxLayout.Y_AXIS ) );

		m_CheckOperationCheckBox = new JCheckBox();
		m_CheckOperationCheckBox.setBorder( BorderFactory.createEmptyBorder( 0, 0, 10, 0 ) );

		m_ChosenNameTextField.getDocument().addDocumentListener(
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
		
		m_ExistingCheckOp = _context.getExistingCheckOp( forExistingAttribute );
		m_ExistingFlowPort = _context.getExistingFlowPort( forExistingAttribute );
		
		updateNames();
		
		m_CheckOperationCheckBox.setSelected(true);
		m_CheckOperationCheckBox.setEnabled(true);
		theCenterPanel.add( m_CheckOperationCheckBox );

		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		
		add( theCenterPanel, BorderLayout.WEST );
		m_RequirementsPanel.setAlignmentX(LEFT_ALIGNMENT);
		theCenterPanel.add( m_RequirementsPanel );
				
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

	public static void launchThePanel(
			final String theAppID,
			ExecutableMBSE_Context context ){

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );
				
				IRPModelElement theBlockEl = context.getSelectedElement().getOwner();

				JFrame frame = new JFrame(
						"Update attribute called " + context.getSelectedElement().getName() + 
						" on " + context.elInfo( theBlockEl ) );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				UpdateTracedAttributePanel thePanel = new UpdateTracedAttributePanel(
						theAppID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});		
	}	
	
	private void updateNames(){
		
		m_CheckOpName = _context.determineBestCheckOperationNameFor(
				(IRPClassifier)_selectionContext.getChosenBlock(), 
				m_ChosenNameTextField.getText(),
				40 );
		
		if( m_ExistingCheckOp==null ){
			m_CheckOperationCheckBox.setText(
					"Add a new '" +  m_CheckOpName + "' operation to the block that returns the attribute value");			
		} else {
			m_CheckOperationCheckBox.setText(
					"Update existing '" +  m_CheckOpName + "' operation on the block that returns the attribute value");			
		}
	}
	
	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled) {
		
		String errorMessage = "";
		boolean isValid = true;
		
		String theChosenName = m_ChosenNameTextField.getText();
		
		boolean isLegalName = _context.isLegalName( theChosenName, _selectionContext.getChosenBlock() );
		
		if (!isLegalName){
			
			errorMessage += theChosenName + " is not legal as an identifier representing an executable attribute\n";				
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
			
			errorMessage = "Unable to proceed as the initial value '" + m_ChosenNameTextField.getText() + "' is not an integer";
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
		if( checkValidity( false ) ){

			IRPAttribute theExistingAttribute = (IRPAttribute)_context.getSelectedElement();

			theExistingAttribute.setName( m_ChosenNameTextField.getText() );				
			theExistingAttribute.highLightElement();
			theExistingAttribute.setDefaultValue( m_InitialValueTextField.getText() );

			// check if flow requires renaming
			if( m_ExistingFlowPort != null ){
				
				String theDesiredFlowPortName = theExistingAttribute.getName();
			
				if( !m_ExistingFlowPort.getName().equals( theDesiredFlowPortName ) ){
					
					_context.debug( "Renaming " + _context.elInfo( m_ExistingFlowPort ) + 
							" to " + theDesiredFlowPortName );
					
					m_ExistingFlowPort.setName( theDesiredFlowPortName );
				} else {
					_context.debug( "Existing " + _context.elInfo( m_ExistingFlowPort ) + 
							" is already correctly named" );					
				}
			}
			
			if( m_CheckOperationCheckBox.isSelected() ){

				List<IRPRequirement> selectedReqtsList = 
						m_RequirementsPanel.getSelectedRequirementsList();

				_context.debug( _context.elInfo( theExistingAttribute ) + " is the existing attribute");

				if( m_ExistingCheckOp == null ){

					addTraceabilityDependenciesTo( theExistingAttribute, selectedReqtsList );

					if( m_CheckOperationCheckBox.isSelected() ){

						IRPOperation theCheckOp = addCheckOperationFor( theExistingAttribute, m_CheckOpName );
						addTraceabilityDependenciesTo( theCheckOp, selectedReqtsList );	
						theCheckOp.highLightElement();
					}

				} else { // m_ExistingCheckOp != null

					if( !m_ExistingCheckOp.getName().equals( m_CheckOpName ) ){

						_context.debug( "Changed the name of " + _context.elInfo( m_ExistingCheckOp ) + " to " + m_CheckOpName );
						m_ExistingCheckOp.setName( m_CheckOpName );
						m_ExistingCheckOp.setBody("OM_RETURN( " + m_ChosenNameTextField.getText() + " );");
					}

					addTraceabilityDependenciesTo( m_ExistingCheckOp, selectedReqtsList );
				}
			}
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