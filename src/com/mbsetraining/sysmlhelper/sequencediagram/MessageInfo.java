package com.mbsetraining.sysmlhelper.sequencediagram;

import java.util.List;

import com.mbsetraining.sysmlhelper.common.ConfigurationSettings;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class MessageInfo {

	private static final String OPERATION = "OPERATION";
	private static final String EVENT = "EVENT";

	protected static IRPStereotype _directedFeatureStereotype;
	protected IRPMessage _message;
	protected String _messageType;
	protected IRPInterfaceItem _interfaceItem;
	protected boolean _isRealizationNeeded;
	protected IRPClassifier _targetClassifier;
	protected IRPClassifier _sourceClassifier;
	protected boolean _isNewEventNeeded;
	protected IRPClass _matchingInterfaceBlock;
	protected boolean _isInterfaceBlockReversed;
	protected InterfaceInfoList _candidateInterfaces;
	protected boolean _isAddToInterfaceBlockNeeded;
	protected boolean _isAddToTargetClassiferNeeded;
	protected boolean _isDirectedFeatureSettingNeeded;

	protected ConfigurationSettings _context;

	public static void main(String[] args) {
		
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		ExecutableMBSE_Context theContext = new ExecutableMBSE_Context( theRhpApp.getApplicationConnectionString() );
		
		IRPModelElement theSelectedEl = theRhpApp.getSelectedElement();
		
		InterfaceInfoList theCandidateInterfaces = new InterfaceInfoList( theContext );
		if( theSelectedEl instanceof IRPMessage ){
			
			MessageInfo theMessageInfo = 
					new MessageInfo( 
							(IRPMessage) theSelectedEl, theCandidateInterfaces, theContext );
			
			theMessageInfo.dumpInfo();
			
		} else if( theSelectedEl instanceof IRPSequenceDiagram ){
			
			MessageInfoList theMessageInfoList = 
					new MessageInfoList( 
							(IRPSequenceDiagram) theSelectedEl, theCandidateInterfaces, theContext );
			
			theMessageInfoList.dumpInfo();
		}
	}
	
	public MessageInfo(
			IRPMessage theMessage,
			InterfaceInfoList candidateInterfaces,
			ConfigurationSettings context ) {
		
		_context = context;
		_message = theMessage;
		_messageType = _message.getMessageType();
		_candidateInterfaces = candidateInterfaces;
		_interfaceItem = _message.getFormalInterfaceItem();
		_isRealizationNeeded = _interfaceItem == null;
		_targetClassifier = getTargetClassifierFrom( _message );
		_sourceClassifier = getSourceClassifierFrom( _message );
		
		if( _isRealizationNeeded ){
			
			if( _messageType.equals( EVENT ) ){
				
				IRPModelElement theEvent = _context.get_rhpPrj().findAllByName( getName(), "Event" );
				
				if( theEvent instanceof IRPEvent ){
					_interfaceItem = (IRPEvent) theEvent;
					_isNewEventNeeded = false;
				} else {
					_isNewEventNeeded = true;
				}
			} else if( _messageType.equals( OPERATION ) ){
				
				_isNewEventNeeded = false;
			}	
						
		} else {
			_isNewEventNeeded = false;
		}
		
		_isAddToTargetClassiferNeeded = false;
		
		if( _isNewEventNeeded ){
			
			_isAddToTargetClassiferNeeded = true;			

		} else if( _isRealizationNeeded ){
			
			_interfaceItem = 
					getInterfaceItemAlreadyIn( 
							_targetClassifier, _message.getName() );
			
			if( _interfaceItem == null ){
				_isAddToTargetClassiferNeeded = true;			
			}
		}
		
		_isDirectedFeatureSettingNeeded = false;

		if( _targetClassifier == null ||
			_sourceClassifier == null ||
			_targetClassifier.equals( _sourceClassifier ) ){
		
			_context.debug( _context.elInfo( _message ) + " is a self-message and hence no interfaces are needed" );
			
			_isAddToInterfaceBlockNeeded = false;
			
		} else {
			
			_context.debug( _context.elInfo( _message ) + " is a message from " + 
					_context.elInfo( _targetClassifier ) + " to " + 
					_context.elInfo( _sourceClassifier ) );
			
			if( _directedFeatureStereotype == null ){
				_directedFeatureStereotype = _context.getStereotypeWith( "directedFeature" );
			}
			
			_matchingInterfaceBlock = 
					_candidateInterfaces.getMatchingInterfaceBlockFrom( 
							_sourceClassifier, _targetClassifier );
			
			if( _matchingInterfaceBlock != null ){
				
				_context.debug( _context.elInfo( _matchingInterfaceBlock ) + 
						" is the InterfaceBlock for " + _context.elInfo( _message ) );
				
				_isInterfaceBlockReversed = false;
			
			} else {
				
				_matchingInterfaceBlock = 
						_candidateInterfaces.getMatchingInterfaceBlockFrom( 
								_targetClassifier, 
								_sourceClassifier );
				
				if( _matchingInterfaceBlock == null ){
					
					_context.debug( "No InterfaceBlock was found for " + _context.elInfo( _message ) );
					
				} else {
					
					_context.debug( _context.elInfo( _matchingInterfaceBlock ) + 
							" is the reversed InterfaceBlock for " + _context.elInfo( _message ) );
					
					_isInterfaceBlockReversed = true;
				}
			}
			
			if( _matchingInterfaceBlock != null &&
					_interfaceItem != null &&
					!_isNewEventNeeded ){
				
				IRPInterfaceItem theInterfaceItem = 
						getInterfaceItemAlreadyIn( 
								_matchingInterfaceBlock, _message.getName() );
				
				if( theInterfaceItem == null ){
					
					_context.debug( "Found that " + _context.elInfo( _interfaceItem ) + 
							" requires adding to " + _context.elInfo( _matchingInterfaceBlock ) );		
					
					_isAddToInterfaceBlockNeeded = true;
					_isDirectedFeatureSettingNeeded = true;

				} else {
					_context.debug( _context.elInfo( _interfaceItem ) + 
							" is already in" + _context.elInfo( _matchingInterfaceBlock ) );
					
					_isAddToInterfaceBlockNeeded = false;
				}

			} else {
				_isAddToInterfaceBlockNeeded = true;
				_isDirectedFeatureSettingNeeded = true;
			}
		}
	}

	private IRPClassifier getTargetClassifierFrom(
			IRPMessage theMessage ){
		
		IRPClassifierRole theClassifierRole = theMessage.getTarget();
		
		IRPClassifier theTargetClassifier = null;
		
		if( theClassifierRole != null ){
			theTargetClassifier = theClassifierRole.getFormalClassifier();
		}
		
		return theTargetClassifier;
	}
	
	private IRPClassifier getSourceClassifierFrom(
			IRPMessage theMessage ){
		
		IRPClassifierRole theClassifierRole = theMessage.getSource();
		
		IRPClassifier theTargetClassifier = null;
		
		if( theClassifierRole != null ){
			theTargetClassifier = theClassifierRole.getFormalClassifier();
		}
		
		return theTargetClassifier;
	}

	private IRPInterfaceItem getInterfaceItemAlreadyIn(
			IRPClassifier theClassifier,
			String theName ) {
		
		IRPInterfaceItem theInterfaceItem = null;

		@SuppressWarnings("unchecked")
		List<IRPModelElement> existingInterfaceItems = theClassifier.getInterfaceItems().toList();
		
		for( IRPModelElement existingInterfaceItem : existingInterfaceItems ){
			
			//_context.debug( "Found " + _context.elInfo( existingInterfaceItem ) );

			if( existingInterfaceItem instanceof IRPEventReception ||
					existingInterfaceItem instanceof IRPOperation ){
				
				if( existingInterfaceItem.getName().equals( theName ) ){
					
					_context.debug( "Found that " + theName + 
							" is in " + _context.elInfo( theClassifier ) );
					
					theInterfaceItem = (IRPInterfaceItem) existingInterfaceItem;
					break;
				}
			}
		}
		
		return theInterfaceItem;
	}
	
	public boolean get_isRealizationNeeded() {
		return _isRealizationNeeded;
	}
	
	public String getName(){
		return _message.getName();
	}
	
	public void dumpInfo(){
		
		_context.debug( "_message is " + _context.elInfo( _message ) );
		if( _messageType != null ) _context.debug( "_messageType is " + _messageType);
		_context.debug( "_interfaceItem is " + _context.elInfo( _interfaceItem ) );
		_context.debug( "_isRealizationNeeded = " + _isRealizationNeeded );
		_context.debug( "_isNewEventNeeded = " + _isNewEventNeeded );
		_context.debug( "_matchingInterfaceBlock is " + _context.elInfo( _matchingInterfaceBlock ) );
		_context.debug( "_isInterfaceBlockReversed = " + _isInterfaceBlockReversed );
		_context.debug( "_isAddToInterfaceBlockNeeded = " + _isAddToInterfaceBlockNeeded );
		_context.debug( "_targetClassifier = " + _context.elInfo( _targetClassifier ) );
		_context.debug( "_sourceClassifier = " + _context.elInfo( _sourceClassifier ) );
		_context.debug( "_isAddToTargetClassiferNeeded = " + _isAddToTargetClassiferNeeded );
		_context.debug( "_isDirectedFeatureSettingNeeded = " + _isDirectedFeatureSettingNeeded );
	}
	
	public String getActionDescription(){
	
		String theMsg = "";
		
		if( _isRealizationNeeded ){	
			theMsg += "Realization needed. ";
		}
		
		if( _matchingInterfaceBlock != null ){
			
			theMsg += " Add to " + _context.elInfo( _matchingInterfaceBlock ) + ".";
			
			if( _isInterfaceBlockReversed ){					
				theMsg += " direction='provided'. ";
			} else {
				theMsg += " direction='required'. ";
			}
			
		} else if( _targetClassifier.equals( _sourceClassifier ) ) {
		
			theMsg += " Self call. ";
		} else {
			theMsg += " No InterfaceBlock found. ";
		}
		
		return theMsg;	
	}
	
	public boolean isUpToDate(){
	
		boolean isUpToDate = 	
				!_isRealizationNeeded &&
				!_isNewEventNeeded &&
				!_isAddToInterfaceBlockNeeded &&
				!_isAddToTargetClassiferNeeded &&
				!_isDirectedFeatureSettingNeeded;
		
		_context.debug( "isUpToDate for " + _context.elInfo( _message ) + " is returning " + isUpToDate );
		
		return isUpToDate;
	}
	
	public void performAction(){
				
		_context.debug( "performAction invoked for " + _context.elInfo( _message ) );
		
		if( _isRealizationNeeded ){	
			
			if( _isNewEventNeeded ){
				
				IRPPackage thePkg = _context.getOwningPackageFor( _message );
				_context.debug( "Adding event called " + getName() + " to " + _context.elInfo( thePkg ) );
				_interfaceItem = thePkg.addEvent( getName() );
			}	
			
			if( _isAddToTargetClassiferNeeded ){
				
				if( _messageType.equals( EVENT ) ){
					_context.debug( "Adding reception called " + getName() + " to " + _context.elInfo( _targetClassifier ) );
					_targetClassifier.addNewAggr( "Reception", getName() );
				
				} else if( _messageType.equals( OPERATION ) ){
					_context.debug( "Adding operation called " + getName() + " to " + _context.elInfo( _targetClassifier ) );
					_interfaceItem = _targetClassifier.addOperation( getName() );
				}
			}
			
			_context.debug( "Setting formal interface item for " + _context.elInfo( _message ) + 
					" to " + _context.elInfo( _interfaceItem ) );
			
			_message.setFormalInterfaceItem( _interfaceItem );
		
		} else {
			
			if( _isAddToTargetClassiferNeeded ){
				
				if( _messageType.equals( EVENT ) ){
					_context.debug( "Adding reception called " + getName() + " to " + _context.elInfo( _targetClassifier ) );
					_targetClassifier.addNewAggr( "Reception", getName() );
				}
			}
		}
		
		if( _isAddToInterfaceBlockNeeded ){
			
			if( _matchingInterfaceBlock == null ){
				_context.warning( "No InterfaceBlock was found between " + 
						_context.elInfo( _sourceClassifier ) + " and " + 
						_context.elInfo( _targetClassifier ) + " for which to add " + 
						_context.elInfo( _interfaceItem ) );
				
			} else {
				IRPInterfaceItem theInterfaceItem = null;
				
				if( _messageType.equals( EVENT ) ){		
					
					theInterfaceItem = _matchingInterfaceBlock.addEventReception( getName() );
					
				} else if( _messageType.equals( OPERATION ) ){
					
					theInterfaceItem = _matchingInterfaceBlock.addOperation( getName() );
				}
				
				if( theInterfaceItem != null ){
				
					_context.debug( "Stereotyping " + _context.elInfo( theInterfaceItem ) + 
							" with " + _context.elInfo( _directedFeatureStereotype ) );
					
					theInterfaceItem.setStereotype( _directedFeatureStereotype );

					IRPTag baseTag = _directedFeatureStereotype.getTag( "direction" );
					
					if( _isInterfaceBlockReversed ){
						
						_context.debug( "Setting " + _context.elInfo( baseTag ) + 
								" on " + _context.elInfo( theInterfaceItem ) + " to provided" );
						
						theInterfaceItem.setTagValue(baseTag, "provided");
						

					} else {
						
						_context.debug( "Setting " + _context.elInfo( baseTag ) + 
								" on " + _context.elInfo( theInterfaceItem ) + " to required" );
						
						theInterfaceItem.setTagValue(baseTag, "required");
					}
					
					theInterfaceItem.highLightElement();
				}
			}
		}
	}
	
	public IRPMessage get_message() {
		return _message;
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