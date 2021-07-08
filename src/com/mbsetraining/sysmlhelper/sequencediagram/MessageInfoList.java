package com.mbsetraining.sysmlhelper.sequencediagram;

import java.util.ArrayList;
import java.util.List;

import com.mbsetraining.sysmlhelper.common.ConfigurationSettings;
import com.telelogic.rhapsody.core.*;

public class MessageInfoList extends ArrayList<MessageInfo>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7379143084838550398L;
	
	ConfigurationSettings _context;
	
	public MessageInfoList(
			IRPSequenceDiagram theSD,
			InterfaceInfoList theCandidateInterfaces,
			ConfigurationSettings theContext ){
		
		_context = theContext;
		
		IRPCollaboration theCollab = theSD.getLogicalCollaboration();
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theMessages = theCollab.getMessages().toList();
		
		_context.info( "There are " + theMessages.size() + " messages on " + _context.elInfo( theSD ) );
		
		for( IRPModelElement theMessage : theMessages ){
			
			if( theMessage instanceof IRPMessage ){
				MessageInfo theMessageInfo = new MessageInfo( (IRPMessage) theMessage, theCandidateInterfaces, _context );
				add( theMessageInfo );
			}
		}		
	}
	
	protected void dumpInfo(){
		
		for (MessageInfo theMessageInfo : this) {
			theMessageInfo.dumpInfo();
		}
	}
}
