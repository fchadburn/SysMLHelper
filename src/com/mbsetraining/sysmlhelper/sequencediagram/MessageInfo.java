package com.mbsetraining.sysmlhelper.sequencediagram;

import java.util.List;

import com.mbsetraining.sysmlhelper.common.ConfigurationSettings;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class MessageInfo {

	static IRPStereotype _directedFeatureStereotype;
	
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
		
		if( _directedFeatureStereotype == null ){
			_directedFeatureStereotype = _context.getStereotypeWith( "directedFeature" );
		}
		
		if( _isRealizationNeeded ){
			
			if( _messageType.equals( "EVENT" ) ){
				
				IRPModelElement theEvent = _context.get_rhpPrj().findAllByName( getName(), "Event" );
				
				if( theEvent instanceof IRPEvent ){
					_interfaceItem = (IRPEvent) theEvent;
					_isNewEventNeeded = false;
				} else {
					_isNewEventNeeded = true;
				}
			}
			
			_isAddToTargetClassiferNeeded = true;
			_isAddToInterfaceBlockNeeded = true;
			
		} else {
			_isNewEventNeeded = false;
		}
		
		_targetClassifier = getTargetClassifierFrom( _message );
		_sourceClassifier = getSourceClassifierFrom( _message );
		
		_context.info( _context.elInfo( _message ) + " is a message from " + 
				_context.elInfo( _targetClassifier ) + " to " + 
				_context.elInfo( _sourceClassifier ) );
		
		_isAddToTargetClassiferNeeded = false;
		
		if( _isNewEventNeeded ){
			
			_isAddToTargetClassiferNeeded = true;			

		} else if( _isRealizationNeeded &&
			_interfaceItem != null &&
			!isInterfaceItemAlreadyIn( _targetClassifier, _interfaceItem ) ){
			
			_isAddToTargetClassiferNeeded = true;			
		}
		
		_matchingInterfaceBlock = 
				_candidateInterfaces.getMatchingInterfaceBlockFrom( 
						_sourceClassifier, _targetClassifier );
		
		if( _matchingInterfaceBlock != null ){
			
			_context.info( _context.elInfo( _matchingInterfaceBlock ) + 
					" is the InterfaceBlock for " + _context.elInfo( _message ) );
			
			_isInterfaceBlockReversed = false;
		
		} else {
			
			_matchingInterfaceBlock = 
					_candidateInterfaces.getMatchingInterfaceBlockFrom( 
							_targetClassifier, 
							_sourceClassifier );
			
			if( _matchingInterfaceBlock == null ){
				_context.info( "No InterfaceBlock was found for " + _context.elInfo( _message ) );
			} else {
				
				_context.info( _context.elInfo( _matchingInterfaceBlock ) + 
						" is the reversed InterfaceBlock for " + _context.elInfo( _message ) );
				
				_isInterfaceBlockReversed = true;
			}
		}
		
		_isAddToInterfaceBlockNeeded = false;

		if( _matchingInterfaceBlock != null &&
				_interfaceItem != null &&
				!_isNewEventNeeded ){
			
			_isAddToInterfaceBlockNeeded = 
					!isInterfaceItemAlreadyIn( _matchingInterfaceBlock, _interfaceItem );
			
			if( _isAddToInterfaceBlockNeeded ){
				_context.info( "Found that " + _context.elInfo( _interfaceItem ) + 
						" requires adding to " + _context.elInfo( _matchingInterfaceBlock ) );			
			}
		}
		
		dumpInfo();
		
		_context.debug( "---" );
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

	private boolean isInterfaceItemAlreadyIn(
			IRPClassifier theClassifier,
			IRPInterfaceItem theInterfaceItem ) {
		
		boolean isInterfaceItemAlreadyIn = false;

		@SuppressWarnings("unchecked")
		List<IRPModelElement> existingInterfaceItems = theClassifier.getInterfaceItems().toList();
		
		for( IRPModelElement existingInterfaceItem : existingInterfaceItems ){
			
			_context.info( "Found " + _context.elInfo( existingInterfaceItem ) );

			if( existingInterfaceItem instanceof IRPEventReception ){
				
				IRPEventReception theEventReception = (IRPEventReception)existingInterfaceItem;
				IRPEvent theEvent = theEventReception.getEvent();
				
				if( theEvent.equals( theInterfaceItem ) ){
					
					_context.info( "Found that " + _context.elInfo( _interfaceItem ) + 
							" is in " + _context.elInfo( _matchingInterfaceBlock ) );
					
					isInterfaceItemAlreadyIn = true;
					break;
				}
				
			}
		}
		
		return isInterfaceItemAlreadyIn;
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
	}
	
	public String getActionDescription(){
	
		String theMsg = "";
		
		if( _isRealizationNeeded ){	
			
			theMsg += "Requires realization";
			
			if( _matchingInterfaceBlock != null ){
				
				if( _isInterfaceBlockReversed ){					
					theMsg += " and adding to " + _context.elInfo( _matchingInterfaceBlock ) + " as provided";
				} else {
					theMsg += " and adding to " + _context.elInfo( _matchingInterfaceBlock ) + " as required";
				}
				
			} else {
				theMsg += " (no InterfaceBlock was found)";
			}
			
		} else {
			
			if( _matchingInterfaceBlock != null ){
				
				if( _isAddToInterfaceBlockNeeded ){
					
					if( _isInterfaceBlockReversed ){					
						theMsg += "Requires adding to " + _context.elInfo( _matchingInterfaceBlock ) + " as provided";
					} else {
						theMsg += "Requires adding to " + _context.elInfo( _matchingInterfaceBlock ) + " as required";
					}
				}
				
			} else {
				theMsg += ", but no InterfaceBlock was found";
			}
		}
		
		return theMsg;	
	}
	
	public void performAction(){
				
		_context.info( "performAction is invoked for " + _context.elInfo( _message ) );
		
		if( _isRealizationNeeded ){	
			
			if( _isNewEventNeeded ){
				
				IRPPackage thePkg = _context.getOwningPackageFor( _message );
				_interfaceItem = thePkg.addEvent( getName() );
			}	
			
			if( _isAddToTargetClassiferNeeded ){
				
				if( _messageType.equals( "EVENT" ) ){
					_targetClassifier.addNewAggr( "Reception", getName() );
				}
			}
			
			_message.setFormalInterfaceItem( _interfaceItem );
		
		} else {
			
			if( _isAddToTargetClassiferNeeded ){
				
				if( _messageType.equals( "EVENT" ) ){
					_targetClassifier.addNewAggr( "Reception", getName() );
				}
			}
		}
		
		if( _isAddToInterfaceBlockNeeded ){
			
			if( _messageType.equals( "EVENT" ) ){		
				
				IRPEventReception theEventReception = _matchingInterfaceBlock.addEventReception( getName() );
				
				theEventReception.setStereotype( _directedFeatureStereotype );

				IRPTag baseTag = _directedFeatureStereotype.getTag( "direction" );
				
				if( _isInterfaceBlockReversed ){
					theEventReception.setTagValue(baseTag, "required");
				} else {
					theEventReception.setTagValue(baseTag, "provided");
				}
			}
		}
		
		/*
		if( _isAddToInterfaceBlockNeeded ){
			
			if( _matchingInterfaceBlock != null ){
									}
				if( _isInterfaceBlockReversed ){	
					
					if( _messageType.equals( "EVENT" ) ){
						
						_context.info( "Adding " + _context.elInfo( _message ) + 
								" to " + _context.elInfo( _matchingInterfaceBlock ) + " as provided" );
						
						_matchingInterfaceBlock.addReception( getName() );
					}
					
				} else {
					
					if( _messageType.equals( "EVENT" ) ){
						
						_context.info( "Adding " + _context.elInfo( _message ) + 
								" to " + _context.elInfo( _matchingInterfaceBlock ) + " as required" );
						
						_matchingInterfaceBlock.addReception( getName() );
					}
				}
				
			} else {
				_context.info( "Unable to add " + _message + " to an InterfaceBlock as none were found" );
			}
			
		} else {
			
			if( _matchingInterfaceBlock != null ){
				
				if( _isAddToInterfaceBlockNeeded ){
					
					if( _isInterfaceBlockReversed ){					
						_context.info("Add " + _context.elInfo( _interfaceItem ) + " to " + 
								_context.elInfo( _matchingInterfaceBlock ) + " as provided" );
						
						_matchingInterfaceBlock.addEventReception( _interfaceItem.getName() );
						
					} else {
						_context.info( "Add " + _context.elInfo( _interfaceItem ) + " to " +
								_context.elInfo( _matchingInterfaceBlock ) + " as required" );
						
						_matchingInterfaceBlock.addEventReception( _interfaceItem.getName() );
					}
				}
				
			} else {
				_context.info( "Unable to add " + _message + " to an InterfaceBlock as none were found" );
			}
		}	*/	
	}
}
