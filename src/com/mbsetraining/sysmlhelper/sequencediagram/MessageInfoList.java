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
	InterfaceInfoList _candidateInterfaces;
	
	int _upToDateCount = 0;
	
	public MessageInfoList(
			IRPSequenceDiagram theSD,
			InterfaceInfoList theCandidateInterfaces,
			ConfigurationSettings theContext ){
		
		_context = theContext;
		_candidateInterfaces = theCandidateInterfaces;
		
		IRPCollaboration theCollab = theSD.getLogicalCollaboration();
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theMessages = theCollab.getMessages().toList();
		
		_context.debug( "MessageInfoList constructor determined that there are " + theMessages.size() + " messages on " + _context.elInfo( theSD ) );
		
		for( IRPModelElement theMessage : theMessages ){
			
			if( theMessage instanceof IRPMessage ){
				addOrUpdateCount( (IRPMessage) theMessage, theSD );
			}
		}		
		
		_context.debug( "The MessageInfoList constructor completed (" + _upToDateCount + 
				" messages on " + _context.elInfo( theSD ) + " are up to date, and " + this.size() + " are not)");

	}

	private void addOrUpdateCount(
			IRPMessage theMessage,
			IRPSequenceDiagram onDiagram ){
		
		String theMessageType = theMessage.getMessageType();
		
		boolean isMessage = theMessageType.equals( "EVENT" ) || theMessageType.equals( "OPERATION" );
		
		if( isMessage ){
			MessageInfo theMessageInfo = new MessageInfo( theMessage, onDiagram, _candidateInterfaces, _context );
			
			if( theMessageInfo.isUpToDate() ){
				_upToDateCount++;
			} else {					
				add( theMessageInfo );
			}			
		} else {
			_context.debug( "addOrUpdateCount is ignoring " + _context.elInfo( theMessage ) + " as it's not an event or operation" );
		}
	}
	
	public int get_upToDateCount() {
		return _upToDateCount;
	}

	public MessageInfoList(
			IRPMessage theMessage,
			IRPSequenceDiagram onDiagram,
			InterfaceInfoList theCandidateInterfaces,
			ConfigurationSettings theContext ){
		
		_context = theContext;
		_candidateInterfaces = theCandidateInterfaces;

		_context.debug( "MessageInfoList constructor invoked for " + _context.elInfo( theMessage ) );
		
		addOrUpdateCount( theMessage, onDiagram );
	}
	
	protected void dumpInfo(){
		
		for (MessageInfo theMessageInfo : this) {
			theMessageInfo.dumpInfo();
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
