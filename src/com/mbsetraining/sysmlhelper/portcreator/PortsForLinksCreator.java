package com.mbsetraining.sysmlhelper.portcreator;

import javax.swing.JOptionPane;

import com.mbsetraining.sysmlhelper.common.GraphEdgeInfo;
import com.mbsetraining.sysmlhelper.executablembse.CreateEventForFlowConnectorPanel;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.IRPClass;
import com.telelogic.rhapsody.core.IRPClassifier;
import com.telelogic.rhapsody.core.IRPDiagram;
import com.telelogic.rhapsody.core.IRPGraphEdge;
import com.telelogic.rhapsody.core.IRPGraphNode;
import com.telelogic.rhapsody.core.IRPLink;
import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPPackage;
import com.telelogic.rhapsody.core.IRPPort;
import com.telelogic.rhapsody.core.IRPSysMLPort;

public class PortsForLinksCreator {

	protected ExecutableMBSE_Context _context;
	protected IRPLink _link;
	
	public PortsForLinksCreator(
			ExecutableMBSE_Context context,
			IRPLink link ) {
		
		_context = context;
		_link = link;
	}
	
	public void createPortsFor(String selectedPortType) {
		
		if( selectedPortType.equals( "No" ) ){
			// do nothing
		} else if( selectedPortType.equals( "ProxyPorts" ) ){
			autoCreateProxyPorts();
		} else if( selectedPortType.equals( "StandardPorts" ) ){
			autoCreateStandardPortsAndInterfaces();
		} else if( selectedPortType.equals( "RapidPorts" ) ){
			autoCreateRapidPorts();
		} else {
			_context.warning( "Ignoring request as " + selectedPortType + 
					" is not supported" );
		}
	}

	private void autoCreateStandardPortsAndInterfaces(){

		IRPGraphEdge theGraphEdge = _context.getCorrespondingGraphEdgeFor( _link );

		if( theGraphEdge != null ){

			IRPDiagram theDiagram = theGraphEdge.getDiagram();

			IRPModelElement fromClassifierEl = _link.getFrom().getOtherClass();
			IRPModelElement toClassifierEl = _link.getTo().getOtherClass();

			_context.debug( "theLink.getFrom() = " + _context.elInfo( _link.getFrom() ) );

			if( fromClassifierEl instanceof IRPClassifier && 
					toClassifierEl instanceof IRPClassifier ){

				//_context.debug( "fromClassifierEl = " + _context.elInfo( fromClassifierEl ) );
				//_context.debug( "toClassifierEl = " + _context.elInfo( toClassifierEl ) );

				String toClassifierName = _context.capitalize( toClassifierEl.getName().replace( " ", "" ) );
				String fromClassifierName = _context.capitalize( fromClassifierEl.getName().replace( " ", "" ) );

				String fromPortName = _context.determineUniqueNameBasedOn( 
						"p" + toClassifierName, 
						"Port", 
						fromClassifierEl );

				String toPortName = _context.determineUniqueNameBasedOn( 
						"p" + fromClassifierName, 
						"Port", 
						toClassifierEl );

				GraphEdgeInfo theGraphEdgeInfo = new GraphEdgeInfo( theGraphEdge, _context ); 

				IRPPackage theOwningPkg = _context.getOwningPackageFor( theDiagram );

				String fromInterfaceName =
						_context.determineUniqueNameBasedOn( 
								"I" + fromClassifierName, 
								"Class", 
								theOwningPkg );

				IRPClass fromInterface = theOwningPkg.addClass( fromInterfaceName );
				fromInterface.changeTo( "Interface" );

				IRPPort fromPort = (IRPPort) fromClassifierEl.addNewAggr( "Port", fromPortName );
				//_context.debug( "Created fromPort as " + _context.elInfo( fromPort ) );

				String toInterfaceName =
						_context.determineUniqueNameBasedOn( 
								"I" + toClassifierName, 
								"Class", 
								theOwningPkg );

				IRPClass toInterface = theOwningPkg.addClass( toInterfaceName );
				toInterface.changeTo( "Interface" );

				IRPPort toPort = (IRPPort) toClassifierEl.addNewAggr( "Port", toPortName );		
				_context.debug( "Created toPort as " + _context.elInfo( toPort ) );

				fromPort.addProvidedInterface( fromInterface );
				fromPort.addRequiredInterface( toInterface );

				toPort.addProvidedInterface( toInterface );
				toPort.addRequiredInterface( fromInterface );

				IRPGraphNode fromPortNode = _context.addGraphNodeFor( 
						fromPort, theDiagram, theGraphEdgeInfo.getStartX(), theGraphEdgeInfo.getStartY() );

				IRPGraphNode toPortNode = _context.addGraphNodeFor( 
						toPort, theDiagram, theGraphEdgeInfo.getEndX(), theGraphEdgeInfo.getEndY() );					

				IRPLink newLink = _link.getFrom().addLinkToElement( _link.getTo(), null, fromPort, toPort );
				_context.debug( "Created " + _context.elInfo( newLink ) );

				theDiagram.addNewEdgeForElement( 
						newLink, 
						fromPortNode, 
						theGraphEdgeInfo.getStartX(), 
						theGraphEdgeInfo.getStartY(), 
						toPortNode, 
						theGraphEdgeInfo.getEndX(), 
						theGraphEdgeInfo.getEndY() );

				_link.deleteFromProject();
			}
		}
	}

	public void autoCreateFlowPorts(){

		IRPGraphEdge theGraphEdge = _context.getCorrespondingGraphEdgeFor( _link );

		if( theGraphEdge != null ){

			IRPDiagram theDiagram = theGraphEdge.getDiagram();

			IRPModelElement fromClassifierEl = _link.getFrom().getOtherClass();
			IRPModelElement toClassifierEl = _link.getTo().getOtherClass();

			_context.debug( "theLink.getFrom() = " + _context.elInfo( _link.getFrom() ) );

			if( fromClassifierEl instanceof IRPClassifier && 
					toClassifierEl instanceof IRPClassifier ){

				//_context.debug( "fromClassifierEl = " + _context.elInfo( fromClassifierEl ) );
				//_context.debug( "toClassifierEl = " + _context.elInfo( toClassifierEl ) );

				String fromPortName = _context.determineUniqueNameBasedOn( 
						"out", 
						"SysMLPort", 
						fromClassifierEl );

				//_context.debug( "fromPortName is " + fromPortName );

				String toPortName = _context.determineUniqueNameBasedOn( 
						"in",
						"SysMLPort", 
						toClassifierEl );

				//_context.debug( "toPortName is " + toPortName );

				GraphEdgeInfo theGraphEdgeInfo = new GraphEdgeInfo( theGraphEdge, _context ); 

				IRPSysMLPort fromPort = (IRPSysMLPort) fromClassifierEl.addNewAggr( "SysMLPort", fromPortName );
				//_context.debug( "Created fromPort as " + _context.elInfo( fromPort ) );
				_context.setPortDirectionFor( fromPort, "Out", "Untyped" );
				fromPort.changeTo( _context.FLOW_OUTPUT );

				IRPGraphNode fromPortNode = _context.addGraphNodeFor( 
						fromPort, theDiagram, theGraphEdgeInfo.getStartX(), theGraphEdgeInfo.getStartY() );

				IRPSysMLPort toPort = (IRPSysMLPort) toClassifierEl.addNewAggr( "SysMLPort", toPortName );		
				//_context.debug( "Created toPort as " + _context.elInfo( toPort ) );
				//_context.setPortDirectionFor( toPort, "In", "Untyped" );
				toPort.changeTo( _context.FLOW_INPUT );

				IRPGraphNode toPortNode = _context.addGraphNodeFor( 
						toPort, theDiagram, theGraphEdgeInfo.getEndX(), theGraphEdgeInfo.getEndY() );					

				IRPLink newLink = _link.getFrom().addLinkToElement( _link.getTo(), null, fromPort, toPort );
				_context.debug( "Created " + _context.elInfo( newLink ) );

				theDiagram.addNewEdgeForElement( 
						newLink, 
						fromPortNode, 
						theGraphEdgeInfo.getStartX(), 
						theGraphEdgeInfo.getStartY(), 
						toPortNode, 
						theGraphEdgeInfo.getEndX(), 
						theGraphEdgeInfo.getEndY() );

				_link.deleteFromProject();

				CreateEventForFlowConnectorPanel.launchThePanel( 
						_context.get_rhpAppID(), 
						newLink.getGUID(),
						theDiagram.getGUID() );
			}
		}
	}

	private void autoCreateProxyPorts(){

		IRPGraphEdge theGraphEdge = _context.getCorrespondingGraphEdgeFor( _link );

		if( theGraphEdge != null ){

			IRPDiagram theDiagram = theGraphEdge.getDiagram();

			IRPModelElement fromClassifierEl = _link.getFrom().getOtherClass();
			IRPModelElement toClassifierEl = _link.getTo().getOtherClass();

			//_context.debug( "theLink.getFrom() = " + _context.elInfo( theLink.getFrom() ) );

			if( fromClassifierEl instanceof IRPClassifier && 
					toClassifierEl instanceof IRPClassifier ){

				//_context.debug( "fromClassifierEl = " + _context.elInfo( fromClassifierEl ) );
				//_context.debug( "toClassifierEl = " + _context.elInfo( toClassifierEl ) );

				String toClassifierName = _context.capitalize( toClassifierEl.getName().replace( " ", "" ) );
				String fromClassifierName = _context.capitalize( fromClassifierEl.getName().replace( " ", "" ) );

				String fromPortName = _context.determineUniqueNameBasedOn( 
						"p" + toClassifierName, 
						"Port", 
						fromClassifierEl );

				String toPortName = _context.determineUniqueNameBasedOn( 
						"p" + fromClassifierName, 
						"Port", 
						toClassifierEl );

				GraphEdgeInfo theGraphEdgeInfo = new GraphEdgeInfo( theGraphEdge, _context ); 

				IRPPackage theOwningPkg = _context.getOwningPackageFor( theDiagram );

				String theInterfaceBlockName =
						_context.determineUniqueNameBasedOn( 
								"IB_" + fromClassifierName + "_To_" + toClassifierName, 
								"Class", 
								theOwningPkg );

				IRPClass theInterfaceBlock = theOwningPkg.addClass( theInterfaceBlockName );
				theInterfaceBlock.changeTo( "InterfaceBlock" );

				IRPPort fromPort = (IRPPort) fromClassifierEl.addNewAggr( "Port", fromPortName );
				//_context.debug( "Created fromPort as " + _context.elInfo( fromPort ) );
				fromPort.changeTo( "ProxyPort" );
				fromPort.setOtherClass( theInterfaceBlock );

				IRPGraphNode fromPortNode = _context.addGraphNodeFor( 
						fromPort, theDiagram, theGraphEdgeInfo.getStartX(), theGraphEdgeInfo.getStartY() );

				IRPPort toPort = (IRPPort) toClassifierEl.addNewAggr( "Port", toPortName );		
				//_context.debug( "Created toPort as " + _context.elInfo( toPort ) );
				toPort.changeTo( "ProxyPort" );
				toPort.setOtherClass( theInterfaceBlock );
				toPort.setIsReversed( 1 );

				IRPGraphNode toPortNode = _context.addGraphNodeFor( 
						toPort, theDiagram, theGraphEdgeInfo.getEndX(), theGraphEdgeInfo.getEndY() );					

				IRPLink newLink = _link.getFrom().addLinkToElement( _link.getTo(), null, fromPort, toPort );
				_context.debug( "Created " + _context.elInfo( newLink ) );

				theDiagram.addNewEdgeForElement( 
						newLink, 
						fromPortNode, 
						theGraphEdgeInfo.getStartX(), 
						theGraphEdgeInfo.getStartY(), 
						toPortNode, 
						theGraphEdgeInfo.getEndX(), 
						theGraphEdgeInfo.getEndY() );

				_link.deleteFromProject();
			}
		}
	}

	private void autoCreateRapidPorts(){

		IRPGraphEdge theGraphEdge = _context.getCorrespondingGraphEdgeFor( _link );

		if( theGraphEdge != null ){

			IRPDiagram theDiagram = theGraphEdge.getDiagram();

			IRPModelElement fromClassifierEl = _link.getFrom().getOtherClass();
			IRPModelElement toClassifierEl = _link.getTo().getOtherClass();

			//_context.debug( "theLink.getFrom() = " + _context.elInfo( theLink.getFrom() ) );

			if( fromClassifierEl instanceof IRPClassifier && 
					toClassifierEl instanceof IRPClassifier ){

				//_context.debug( "fromClassifierEl = " + _context.elInfo( fromClassifierEl ) );
				//_context.debug( "toClassifierEl = " + _context.elInfo( toClassifierEl ) );

				String toClassifierName = _context.capitalize( toClassifierEl.getName().replace( " ", "" ) );
				String fromClassifierName = _context.capitalize( fromClassifierEl.getName().replace( " ", "" ) );

				String fromPortName = _context.determineUniqueNameBasedOn( 
						"p" + toClassifierName, 
						"Port", 
						fromClassifierEl );

				String toPortName = _context.determineUniqueNameBasedOn( 
						"p" + fromClassifierName, 
						"Port", 
						toClassifierEl );

				GraphEdgeInfo theGraphEdgeInfo = new GraphEdgeInfo( theGraphEdge, _context ); 

				IRPPort fromPort = (IRPPort) fromClassifierEl.addNewAggr( "Port", fromPortName );
				//_context.debug( "Created fromPort as " + _context.elInfo( fromPort ) );

				IRPGraphNode fromPortNode = _context.addGraphNodeFor( 
						fromPort, theDiagram, theGraphEdgeInfo.getStartX(), theGraphEdgeInfo.getStartY() );

				IRPPort toPort = (IRPPort) toClassifierEl.addNewAggr( "Port", toPortName );		
				//_context.debug( "Created toPort as " + _context.elInfo( toPort ) );

				IRPGraphNode toPortNode = _context.addGraphNodeFor( 
						toPort, theDiagram, theGraphEdgeInfo.getEndX(), theGraphEdgeInfo.getEndY() );					

				IRPLink newLink = _link.getFrom().addLinkToElement( _link.getTo(), null, fromPort, toPort );
				_context.debug( "Created " + _context.elInfo( newLink ) );

				theDiagram.addNewEdgeForElement( 
						newLink, 
						fromPortNode, 
						theGraphEdgeInfo.getStartX(), 
						theGraphEdgeInfo.getStartY(), 
						toPortNode, 
						theGraphEdgeInfo.getEndX(), 
						theGraphEdgeInfo.getEndY() );

				_link.deleteFromProject();
			}
		}
	}
	
	public void createPortsBasedOnPropertyPolicies(){

		String autoGenPolicy = _context.getAutoGenerationOfPortsForLinksPolicy( 
				_link );

		String autoGenDefaultType = _context.getAutoGenerationOfPortsForLinksDefaultType( 
				_link );

		if( autoGenPolicy.equals( "Always" ) ){

			createPortsFor( autoGenDefaultType );

		} else if( autoGenPolicy.equals( "UserDialog" ) ){

			String[] options = {"No","StandardPorts","ProxyPorts","RapidPorts"};

			String selectedPortType = (String) JOptionPane.showInputDialog(
					null,
					"You have drawn a  connector from " + _link.getFrom().getUserDefinedMetaClass() + 
					" => " + _link.getTo().getUserDefinedMetaClass() + ".\n"+
					"Do you want to automatically create ports?",
					"Input",
					JOptionPane.QUESTION_MESSAGE,
					null,
					options,
					autoGenDefaultType );

			createPortsFor( selectedPortType );
		}
	}
}

/**
 * Copyright (C) 2022  MBSE Training and Consulting Limited (www.executablembse.com)

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