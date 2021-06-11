package com.mbsetraining.sysmlhelper.executablembse;

import functionalanalysisplugin.RhapsodyComboBox;
import generalhelpers.CreateStructuralElementPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.mbsetraining.sysmlhelper.common.ElementMover;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.telelogic.rhapsody.core.*;

public class CreateEventForFlowPanel extends CreateStructuralElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected IRPPackage _eventCreationPackage;
	protected JTextField _nameTextField = null;
	protected RhapsodyComboBox _selectEventComboBox = null;
	protected JCheckBox _createEventCheckBox;
	protected IRPFlow _flow = null;
	
	public static void launchThePanel(
			String theAppID ){
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				
				JFrame.setDefaultLookAndFeelDecorated( true );
				
				JFrame frame = new JFrame( "Add event to a flow" );
				
				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateEventForFlowPanel thePanel = 
						new CreateEventForFlowPanel( theAppID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}
	
	public CreateEventForFlowPanel( String theAppID ){
		
		super( theAppID );
		
		_eventCreationPackage = _context.getPackageForSelectedEl();
		_flow = (IRPFlow) _context.getSelectedElement();
		
		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		add( createEventChoicePanel( "" ), BorderLayout.PAGE_START );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}

	@SuppressWarnings("unchecked")
	private JPanel createEventChoicePanel(String theBlockName){
		
		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.X_AXIS ) );	
		
		_nameTextField = new JTextField();
		_nameTextField.setPreferredSize( new Dimension( 250, 20 ) );
		
		List<IRPModelElement> theExistingEvents = _eventCreationPackage.getNestedElementsByMetaClass( 
					"Event", 1 ).toList();
				
		_selectEventComboBox = new RhapsodyComboBox( theExistingEvents, false );
		
		_selectEventComboBox.addActionListener( new ActionListener () {
		    public void actionPerformed( ActionEvent e ){
		        
		    	IRPModelElement theItem = _selectEventComboBox.getSelectedRhapsodyItem();
		    	
		    	if( theItem instanceof IRPEvent ){
		    		_nameTextField.setEnabled( false );
		    	} else {
		    		_nameTextField.setEnabled( true );
		    	}
		    }
		});
		
		_createEventCheckBox = new JCheckBox( "Create event called:" );
		_createEventCheckBox.setSelected( true );
		
	    thePanel.add( _createEventCheckBox );
	    thePanel.add( _nameTextField );
	    
		JLabel theLabel = new JLabel( "  or select existing: " );
		thePanel.add( theLabel );
		thePanel.add( _selectEventComboBox );	    	
	    
		_nameTextField.requestFocusInWindow();
		
		return thePanel;
	}
	
	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled) {
		
		boolean isValid = true;
		String errorMsg = "";
		
		String theChosenName = _nameTextField.getText();
		
		boolean isLegalBlockName = _context.isLegalName( theChosenName, _eventCreationPackage );
					
		if (!isLegalBlockName){
				
			errorMsg += theChosenName + " is not legal as an identifier representing an executable Actor\n";				
			isValid = false;
				
		} else if (!_context.isElementNameUnique(		
			theChosenName, "Event", _eventCreationPackage, 1 ) ){

			errorMsg += "Unable to proceed as the Event name '" + theChosenName + "' is not unique";
			isValid = false;
		}
		
		if( isMessageEnabled && 
				!isValid && 
				errorMsg != null ){

			UserInterfaceHelper.showWarningDialog( errorMsg );
		}
		
		return isValid;
	}

	@Override
	protected void performAction() {
		
		if( checkValidity( false ) ){
	
			if( _createEventCheckBox.isSelected() ){
				
				IRPEvent theEvent;
				
				 IRPModelElement theExistingEvent = _selectEventComboBox.getSelectedRhapsodyItem(); 
				 
				if( theExistingEvent instanceof IRPEvent ){
					
					theEvent = (IRPEvent) theExistingEvent;
					_flow.addConveyed( theExistingEvent );
					
				} else {
					
					String theChosenName = _nameTextField.getText();
					
					_context.debug( "Creating event with name " + theChosenName + 
							" under " + _context.elInfo( _eventCreationPackage ) );
					
					theEvent = (IRPEvent) _eventCreationPackage.addNewAggr( "Event", theChosenName );
				}
				
				_flow.addConveyed( theEvent );

				theEvent.highLightElement();
				
				// only do move if property is set
				boolean isEnabled = _context.getIsEnableAutoMoveOfEventsOnFlowCreation();
				
				if( isEnabled ){
					ElementMover theElementMover = new ElementMover( 
							theEvent, 
							_context.getExternalSignalsPackageStereotype(), 
							_context );
					
					theElementMover.performMove();					
				}
			}
		} else {
			_context.error("Error in CreateEventForFlowPanel.performAction, checkValidity returned false");
		}		
	}
	
}

/**
 * Copyright (C) 2021  MBSE Training and Consulting Limited (www.executablembse.com)

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