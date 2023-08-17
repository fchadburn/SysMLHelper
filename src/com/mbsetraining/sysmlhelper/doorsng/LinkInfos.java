package com.mbsetraining.sysmlhelper.doorsng;

import java.util.HashSet;
import java.util.List;

import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.IRPDependency;
import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPPackage;
import com.telelogic.rhapsody.core.IRPRequirement;

public class LinkInfos extends HashSet<LinkInfo> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4672140081964785445L;

	private ExecutableMBSE_Context _context;
	private boolean _isIncludeReqtToReqt;
	private boolean _isIncludePkgToReqt;

	public LinkInfos( 
			IRPRequirement theRequirement,
			boolean isIncludeReqtToReqt,
			boolean isIncludePkgToReqt,
			ExecutableMBSE_Context context ) {

		_context = context;
		_isIncludeReqtToReqt = isIncludeReqtToReqt;
		_isIncludePkgToReqt = isIncludePkgToReqt;

		addLinkInfosFor( theRequirement );
	}

	public LinkInfos( 
			boolean isIncludeReqtToReqt,
			boolean isIncludePkgToReqt,
			ExecutableMBSE_Context context ) {

		_context = context;
		_isIncludeReqtToReqt = isIncludeReqtToReqt;
		_isIncludePkgToReqt = isIncludePkgToReqt;
	}

	private String getLinkTypeFor(
			IRPDependency theDependency ) {

		String theLinkType = theDependency.getUserDefinedMetaClass();

		if( theLinkType.equals( "Dependency" ) ){

			if( _context.hasStereotypeCalled( "trace", theDependency ) ) {
				theLinkType = "Trace";
			}
		}

		return theLinkType;
	}

	public void addLinkInfosFor( 
			IRPRequirement theRequirement ) {

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theReferences = theRequirement.getReferences().toList();

		for( IRPModelElement theReference : theReferences ){

			if( theReference instanceof IRPDependency ) {

				IRPDependency theDependency = (IRPDependency) theReference;

				IRPModelElement theDependent = theDependency.getDependent();
				//_context.info( _context.elInfo( theDependency ) + " is a Dependency reference from " + _context.elInfo( theDependent ) );

				String theType = getLinkTypeFor( theDependency );

				if( !_isIncludeReqtToReqt && 
						theDependent instanceof IRPRequirement ){

					_context.debug( "Ignoring " +  _context.elInfo( theReference ) + " from " + _context.elInfo( theDependent ) );

				} else if( !_isIncludePkgToReqt && 
						theDependent instanceof IRPPackage ){

					_context.debug( "Ignoring " +  _context.elInfo( theReference ) + " from " + _context.elInfo( theDependent ) );
					
				} else if( !LinkInfo.LEGAL_OSLC_TYPES.contains( theType ) ) {

					theReference.highLightElement();
					_context.warning( "Ignoring " +  _context.elInfo( theReference ) + " from " + _context.elInfo( theDependent ) + " as not a legal OSLC type" );

				} else {

					LinkInfo theLinkInfo = new LinkInfo( theDependent, theRequirement, theType, _context );					
					this.add( theLinkInfo );
				}
			}
		}
	}

	public boolean isEquivalentPresentFor( 
			LinkInfo theLinkInfo ) {

		boolean isEquivalentPresent = false;

		for( LinkInfo theCandidateLinkInfo : this ){

			if( theCandidateLinkInfo.isEquivalentTo( theLinkInfo ) ) {
				isEquivalentPresent = true;
				break;
			}
		}

		return isEquivalentPresent;
	}

	public void dumpInfo() {

		for( LinkInfo theLinkInfo : this ){
			theLinkInfo.dumpInfo();
		}
	}
}

/**
 * Copyright (C) 2023  MBSE Training and Consulting Limited (www.executablembse.com)

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
