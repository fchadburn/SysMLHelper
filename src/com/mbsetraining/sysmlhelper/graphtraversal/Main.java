package com.mbsetraining.sysmlhelper.graphtraversal;

import java.util.List;

import com.mbsetraining.sysmlhelper.businessvalueplugin.BusinessValue_Context;
import com.mbsetraining.sysmlhelper.common.BaseContext;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.telelogic.rhapsody.core.*;

class Main {
	
	protected BaseContext _context;
	protected Node _rootNode;
	
	public Main( 
			Node rootNode,
			BaseContext context) {
		
		_rootNode = rootNode;
		_context = context;
	}
	
    public static void main(String[] args) {
    
    	IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
    	BusinessValue_Context context = new BusinessValue_Context( theRhpApp.getApplicationConnectionString() );
    	
    	List<IRPGraphNode> theGraphNodes = context.getSelectedGraphNodes();
    	
    	if( theGraphNodes.size() == 1 ){
    		
    		IRPGraphNode theSelectedGraphNode = theGraphNodes.get( 0 );
    		IRPDiagram theSourceDiagram = theSelectedGraphNode.getDiagram();
    		
    		Node startNode = new Node( theSelectedGraphNode.getModelObject(), context );

    		context.info( "root node is " + startNode.toString() );

    		GraphPath theCurrentPath = new GraphPath( context );
    		GraphPaths allPaths = new GraphPaths( context );

    		startNode.buildRecursively( theCurrentPath, allPaths );
    		
    		allPaths.dumpInfo();
    		
    		IRPPackage thePackage = context.getOwningPackageFor( theSelectedGraphNode.getModelObject() );
    		
    		if( thePackage != null ){
    			
    			allPaths.createDependenciesAndPathVisualization( 
    					"MaxShldrVal", thePackage, theSourceDiagram );
    		}
    		
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