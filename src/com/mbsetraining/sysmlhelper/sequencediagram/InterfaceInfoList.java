package com.mbsetraining.sysmlhelper.sequencediagram;

import java.util.ArrayList;
import java.util.List;

import com.mbsetraining.sysmlhelper.common.ConfigurationSettings;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class InterfaceInfoList extends ArrayList<InterfaceInfo>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6862328390818322578L;
	/**
	 * 
	 */

	public static void main(String[] args) {
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();

		ExecutableMBSE_Context theContext = new ExecutableMBSE_Context( theRhpApp.getApplicationConnectionString() );
		InterfaceInfoList theInfoList = new InterfaceInfoList( theContext );
		theInfoList.dumpInfo();
	}

	ConfigurationSettings _context;

	protected List<IRPPort> getNonCongugatedPortsFor(
			IRPModelElement theEl ){

		List<IRPPort> thePorts = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theReferences = theEl.getReferences().toList();

		for( IRPModelElement theReference : theReferences ){

			if( theReference instanceof IRPPort ){

				IRPPort thePort = (IRPPort)theReference;

				boolean isConjugated = thePort.getIsReversed()==1;

				if( !isConjugated ){
					_context.debug( _context.elInfo( thePort ) + " is a non-congugated port for " + _context.elInfo( theEl ) );
					thePorts.add( thePort );
				}
			}
		}

		return thePorts;
	}

	protected List<IRPPort> getPortsAtOtherEndOfConnectorsFor(
			IRPPort thePort ){

		List<IRPPort> thePortsAtOtherEnd = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theReferences = thePort.getReferences().toList();

		for( IRPModelElement theReference : theReferences ){

			if( theReference instanceof IRPLink ){

				IRPLink theLink = (IRPLink)theReference;

				IRPPort toPort = theLink.getToPort();

				if( toPort != null && 
						!toPort.equals( thePort ) ){
					
					_context.debug( _context.elInfo( toPort ) + " is a connected port for " + _context.elInfo( thePort ) );
					thePortsAtOtherEnd.add( toPort );
					
				} else {
					
					IRPPort fromPort = theLink.getFromPort();
					
					if( fromPort != null && 
							!fromPort.equals( thePort ) ){
						
						_context.debug( _context.elInfo( fromPort ) + " is a connected port for " + _context.elInfo( thePort ) );
						thePortsAtOtherEnd.add( fromPort );
					}
				}
			}
		}

		return thePortsAtOtherEnd;
	}

	protected List<IRPClass> getInterfaceBlocksUnder(
			IRPModelElement theOwningEl ){

		List<IRPClass> theInterfaceBlocks = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theCandidates = _context.get_rhpPrj().getNestedElementsByMetaClass( "Class", 1 ).toList();

		for( IRPModelElement theCandidate : theCandidates ){

			boolean isReference = theCandidate.getSaveUnit().isReferenceUnit()==1;

			if( !isReference &&
					theCandidate.getUserDefinedMetaClass().equals( "InterfaceBlock" ) ){

				theInterfaceBlocks.add( (IRPClass) theCandidate );
			}
		}

		return theInterfaceBlocks;
	}

	public InterfaceInfoList(
			ConfigurationSettings theContext ){

		_context = theContext;

		List<IRPClass> theCandidates = getInterfaceBlocksUnder( _context.get_rhpPrj() );

		_context.debug( "There are " + theCandidates.size() + " candidates in " + _context.elInfo( _context.get_rhpPrj() ) );

		for( IRPClass theCandidate : theCandidates ){

			_context.debug( _context.elInfo( theCandidate ) + " is a candidate" );

			List<IRPPort> theNonConjugatedPorts = getNonCongugatedPortsFor( theCandidate );

			for( IRPPort thePort : theNonConjugatedPorts ){

				List<IRPPort> theOtherEndPorts = getPortsAtOtherEndOfConnectorsFor( thePort );

				for( IRPPort theOtherEndPort : theOtherEndPorts ){

					IRPClass theOtherEndContract = theOtherEndPort.getContract();

					if( theOtherEndContract == null ){

						_context.debug( _context.elInfo( theOtherEndPort ) + " does not have a contract set" );

					} else if( !theOtherEndContract.equals( theCandidate ) ){

						_context.debug( _context.elInfo( theOtherEndPort ) + " is contracted with " + 
								_context.elInfo( theOtherEndContract ) + 
								" rather than " + _context.elInfo( theCandidate ) );;

					} else if( theOtherEndPort.getIsReversed()==0 ){
						_context.debug( _context.elInfo( theOtherEndPort ) + " is contracted with " + 
								_context.elInfo( theCandidate ) + " but is not set as conjugated" );

					} else {
						InterfaceInfo theInfo = new InterfaceInfo( 
								(IRPClass) theCandidate, 
								(IRPClassifier)thePort.getOwner(), 
								(IRPClassifier)theOtherEndPort.getOwner(), 
								_context);

						add( theInfo );
					}
				}
			}
		}		
	}

	protected void dumpInfo(){

		for( InterfaceInfo theInfo : this ){
			theInfo.dumpInfo();
		}
	}
	
	public IRPClass getMatchingInterfaceBlockFrom(
			IRPClassifier fromClassifier,
			IRPClassifier toClassifier ){
		
		IRPClass theInterfaceBlock = null;
		
		for( InterfaceInfo theInfo : this ){
			
			if( theInfo.get_fromClassifier().equals( fromClassifier ) &&
					theInfo.get_toClassifier().equals( toClassifier )	){
				
				theInterfaceBlock = theInfo.get_interfaceClass();
			}
		}

		return theInterfaceBlock;
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