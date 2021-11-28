package com.mbsetraining.sysmlhelper.pubsubportcreation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class AutoConnectFlowPortsInfo {

	final private String _doNothingOption = "Do nothing";
	final private String _createNewAttributeOption = "Create new attribute";
	final private String _noneOption = "<None>";
	
	private String _attributeName;
	private IRPAttribute _subscribingAttribute;
	private IRPSysMLPort _subscribingFlowPort;
	private IRPInstance _subscribingPart;
	private IRPClassifier _subscribingBlock;
	private Set<IRPStructureDiagram> _diagramsToUpdate;
	private IRPAttribute _publishingAttribute;
	private IRPSysMLPort _publishingFlowPort;
	private IRPInstance _publishingPart;
	private IRPClassifier _buildingBlock;
	private JComboBox<Object> _bindingChoiceComboBox;
	
	private ExecutableMBSE_Context _context;

	private Set<IRPLink> _links = new HashSet<IRPLink>();
	
	protected JTextField _chosenNameTextField = new JTextField();
	
	private FlowType _existingFlowType = FlowType.Nothing;
		
	public enum FlowType {
	    Nothing, Publish, Subscribe
	}	
	
	public List<IRPAttribute> getExistingAttributesLinkedTo(
			IRPAttribute theSrcAttribute,
			IRPInstance inTgtPart ){

		//_context.debug( "getExistingAttributesLinkedTo invoked for " + 
		//		_context.elInfo( theSrcAttribute ) + " owned by " + 
		//		_context.elInfo( theSrcAttribute.getOwner() ) + " to see if there are links to " + 
		//		_context.elInfo( inTgtPart ) );
		
		List<IRPAttribute> theExistingAttributes = 
				new ArrayList<IRPAttribute>();
		
		IRPSysMLPort theSrcFlowPort = _context.getExistingFlowPort( theSrcAttribute );

		if( theSrcFlowPort == null ){

			_context.debug( "getExistingAttributesLinkedTo found that " + 
					_context.elInfo( theSrcAttribute ) + 
					" has no flowport when one is expected");
			
		} else { // if( theSrcFlowPort != null ){

			IRPClassifier theAssemblyBlock = 
					(IRPClassifier) inTgtPart.getOwner();
			
			@SuppressWarnings("unchecked")
			List<IRPAttribute> theTgtAttributes = 
					inTgtPart.getOtherClass().getAttributes().toList();
			
			for( IRPAttribute theTgtAttribute : theTgtAttributes ){
				
				IRPSysMLPort theTgtFlowPort = 
						_context.getExistingFlowPort( 
								theTgtAttribute );

				if( theTgtFlowPort != null ){
					
					_links.addAll( _context.getLinksBetween(
							theSrcFlowPort, 
							_publishingPart,
							theTgtFlowPort, 
							inTgtPart,
							theAssemblyBlock ) );
					
					boolean isExistingLink = _links.size() > 0 ;
							
					if( isExistingLink ){
						//_context.debug( "Found " + _context.elInfo( theTgtFlowPort ) );
						theExistingAttributes.add( theTgtAttribute );
					}
				}									
			}
		}
		
		return theExistingAttributes;
	}
	
	public AutoConnectFlowPortsInfo(
			IRPAttribute toPublishingAttribute,
			IRPInstance toPublishingPart,
			IRPInstance fromSubscribingPart,
			ExecutableMBSE_Context context ) {
		
		_context = context;

		_context.debug( "AutoConnectFlowPortsInfo constructor was invoked for " + 
				_context.elInfo( toPublishingAttribute ) + " to subscriber " +
				_context.elInfo( fromSubscribingPart ) );
		
		_publishingAttribute = toPublishingAttribute;
		_publishingFlowPort = _context.getExistingFlowPort( _publishingAttribute );
		_publishingPart = toPublishingPart;
		
		_attributeName = toPublishingAttribute.getName();
		_subscribingPart = fromSubscribingPart;
		_subscribingBlock = fromSubscribingPart.getOtherClass();
		_buildingBlock = (IRPClassifier) fromSubscribingPart.getOwner();

		_bindingChoiceComboBox = new JComboBox<Object>();
		
		_bindingChoiceComboBox.addActionListener( new ActionListener(){
		    public void actionPerformed( ActionEvent e ){
		    	
		    	String selectedValue = _bindingChoiceComboBox.getSelectedItem().toString();
		    	
		    	if( selectedValue.equals( _doNothingOption ) ){
		    		
		    		_chosenNameTextField.setText( _noneOption );
		    		_chosenNameTextField.setEnabled( false );
		    		
		    	} else if( selectedValue.equals( _createNewAttributeOption ) ){
		    		
		    		String theProposedName = _context.determineUniqueNameBasedOn( 
		    				_publishingAttribute.getName(), 
		    				"Attribute", 
		    				_subscribingBlock );
		    		
		    		_chosenNameTextField.setText( theProposedName );
		    		_chosenNameTextField.setEnabled( true );		    		
		    	} else {
		    		_chosenNameTextField.setText( selectedValue );
		    		_chosenNameTextField.setEnabled( false );		 		    		
		    	}
		    	
		    	//_context.debug( selectedValue + " was selected" );
		    }
		});

		@SuppressWarnings("unchecked")
		List<IRPAttribute> theAttributes = 
				_subscribingBlock.getAttributes().toList();
				
		List<IRPAttribute> theExistingLinkedAttributes = 
				getExistingAttributesLinkedTo( 
						_publishingAttribute, _subscribingPart );
		
		if( theExistingLinkedAttributes.size() > 0 ){

			IRPAttribute theExistingAttribute = 
					theExistingLinkedAttributes.get(0);

			String theOption = "Already connected";
			_bindingChoiceComboBox.addItem( theOption );			
			_bindingChoiceComboBox.setSelectedItem( theOption );
			_bindingChoiceComboBox.setEnabled( false );				
			
			if( theExistingLinkedAttributes.size() == 1 ){
				_chosenNameTextField.setText( theExistingAttribute.getName() );
			} else {
				_chosenNameTextField.setText( "<Multiple Connectors>" );
			}
			
			_chosenNameTextField.setEnabled( false );
		
		} else {
			
			IRPAttribute theAttributeWithSameName = 
					_subscribingBlock.findAttribute( toPublishingAttribute.getName() );

			_bindingChoiceComboBox.addItem( _doNothingOption );
			_bindingChoiceComboBox.addItem( _createNewAttributeOption );

			for( IRPAttribute theAttribute : theAttributes ){			
				String theName = theAttribute.getName();
				_bindingChoiceComboBox.addItem( theName );		
			}

			_bindingChoiceComboBox.setSelectedItem( _doNothingOption );
			_bindingChoiceComboBox.setEnabled( true );

			if( theAttributeWithSameName != null ){

				_bindingChoiceComboBox.setSelectedItem( toPublishingAttribute.getName() );
				_chosenNameTextField.setText( toPublishingAttribute.getName() );
				_chosenNameTextField.setEnabled( false );

			} else {
				_chosenNameTextField.setText( _noneOption );
				_chosenNameTextField.setEnabled( false );
			}
		}
		
		IRPModelElement theEl = 
				fromSubscribingPart.getOtherClass().findNestedElement( 
						_attributeName, "Attribute" );
		
		if( theEl != null ){
			
			_subscribingAttribute = (IRPAttribute) theEl;
			_subscribingFlowPort = _context.getExistingFlowPort( _subscribingAttribute );
			
			if( _subscribingFlowPort != null ){
				
				_links.addAll( _context.getLinksBetween(
						_subscribingFlowPort, 
						_subscribingPart,
						_publishingFlowPort, 
						_publishingPart,
						_buildingBlock ) );
						
				
			} else {
				//m_LinkToExistingButton.setSelected( true );
			}

		} else {
			
			_context.debug( _context.elInfo( fromSubscribingPart ) + " has no attribute called " + _attributeName );

			_subscribingAttribute = null;
			_subscribingFlowPort = null;
			_existingFlowType = FlowType.Nothing;

		}
	}
	
	public IRPSysMLPort getSubscribingFlowPort() {
		
		_context.debug("getSubscribingFlowPort was invoked for " + _context.elInfo( _subscribingPart ) + " and is returning " + _context.elInfo (_subscribingFlowPort) + " for attribute " + _context.elInfo( _subscribingAttribute ));
		return _subscribingFlowPort;
	}
	
	public IRPSysMLPort getPublishingFlowPort() {
		return _publishingFlowPort;
	}
	
	public IRPClassifier getOwningBlock() {
		return _subscribingBlock;
	}
	
	public IRPClassifier getBuildingBlock() {
		return _buildingBlock;
	}
	
	public IRPInstance getOwnedByPart() {
		return _subscribingPart;
	}
	
	public boolean isThereAnExistingAttribute(){
		return _subscribingAttribute != null;
	}
	
	public FlowType getExistingAttributeFlowType(){
		return _existingFlowType;
	}
	
	public String getIDString(){
		return _subscribingPart.getName() + ":" + _subscribingPart.getOtherClass().getName();
	}
	
	public boolean isCreateNewSelected(){
		return _bindingChoiceComboBox.isEnabled() &&
			   _bindingChoiceComboBox.getSelectedItem().equals( _createNewAttributeOption );
	}
		
	public void performSelectedOperations(){
		
		IRPLink theLink = null;
		
		_diagramsToUpdate = new HashSet<IRPStructureDiagram>();
		
		if( isCreateNewSelected() ){

				_context.info( "Add a new " + _context.elInfo( _publishingAttribute ) + 
						" to " + getIDString() );
				
				IRPAttribute theAttribute = (IRPAttribute) _publishingAttribute.clone( 
						_chosenNameTextField.getText(), 
						getOwningBlock() );
				
				PortCreator theCreator = new PortCreator(_context);
				_subscribingFlowPort = theCreator.createSubscribeFlowportFor( theAttribute );

				theLink = _context.addConnectorBetweenSysMLPortsIfOneDoesntExist(
						_publishingFlowPort,
						_publishingPart, 
						_subscribingFlowPort, 
						_subscribingPart );
				
				_links.add( theLink );

			
		} else if( !_bindingChoiceComboBox.getSelectedItem().equals( _doNothingOption ) ) {
			
			PortCreator theCreator = new PortCreator(_context);
			_subscribingFlowPort = theCreator.createSubscribeFlowportFor( _subscribingAttribute );

			theLink = _context.addConnectorBetweenSysMLPortsIfOneDoesntExist(
					_publishingFlowPort,
					_publishingPart, 
					_subscribingFlowPort, 
					_subscribingPart );

			_links.add( theLink );
		}
		
		if( theLink != null ){
			
			IRPSysMLPort fromPort = theLink.getFromSysMLPort();
			IRPSysMLPort toPort = theLink.getToSysMLPort();
			
			_diagramsToUpdate.addAll( getIBDsWith( fromPort ) );
			_diagramsToUpdate.retainAll( getIBDsWith( toPort ) );
		}		
	}
	
	Set<IRPStructureDiagram> getIBDsWith( 
			IRPSysMLPort theFlowPort ){
		
		Set<IRPStructureDiagram> theIBDsWith = new HashSet<IRPStructureDiagram>();
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theReferences = 
				theFlowPort.getReferences().toList();
		
		for( IRPModelElement theReference : theReferences ){
			
			if( theReference instanceof IRPStructureDiagram ){
				
				theIBDsWith.add( (IRPStructureDiagram) theReference );
				
				_context.debug( _context.elInfo( theFlowPort ) + " owned by " + 
						_context.elInfo( theFlowPort.getOwner() ) + 
						" has a reference to " + _context.elInfo( theReference ) );
			}
		}
		
		return theIBDsWith;
	}
	
	public IRPClassifier getM_SubscribingBlock() {
		return _subscribingBlock;
	}
	
	public JComboBox<Object> getM_BindingChoiceComboBox() {
		return _bindingChoiceComboBox;
	}
	
	public Set<IRPStructureDiagram> getM_DiagramsToUpdate(){
		return _diagramsToUpdate;
	}
	
	JTextField getM_ChosenNameTextField() {
		return _chosenNameTextField;
	}
	
	Set<IRPLink> getM_Links(){
		return _links;
	}

}

/**
 * Copyright (C) 2017-2021  MBSE Training and Consulting Limited (www.executablembse.com)
    
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
