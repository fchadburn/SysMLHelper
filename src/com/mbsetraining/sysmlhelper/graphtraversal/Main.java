package com.mbsetraining.sysmlhelper.graphtraversal;

import java.util.List;
import com.mbsetraining.sysmlhelper.businessvalueplugin.BusinessValue_Context;
import com.mbsetraining.sysmlhelper.common.BaseContext;
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
    		
    		Node startNode = new Node( theSelectedGraphNode.getModelObject(), context );

    		context.info( "root node is " + startNode.toString() );

    		GraphPath theCurrentPath = new GraphPath( context );
    		GraphPaths allPaths = new GraphPaths( context );

    		startNode.buildRecursively( theCurrentPath, allPaths );
    		
    		allPaths.dumpInfo();
    		
    		IRPPackage thePackage = context.getOwningPackageFor( theSelectedGraphNode.getModelObject() );
    		
    		if( thePackage != null ){
    			allPaths.createDependenciesAndPathVisualization(thePackage);
    		}
    		
       	}
    }
}