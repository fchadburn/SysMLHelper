package com.mbsetraining.sysmlhelper.businessvalueplugin;

import java.util.List;

import com.mbsetraining.sysmlhelper.common.BaseContext;
import com.mbsetraining.sysmlhelper.common.GraphNodeInfo;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class ConvertToNote {

	public static void main(String[] args) {

		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		ExecutableMBSE_Context theContext = new ExecutableMBSE_Context( theRhpApp.getApplicationConnectionString() );

		List<IRPGraphElement> theGraphEls = theContext.getSelectedGraphElements();
		
		for (IRPGraphElement theGraphEl : theGraphEls) {
			if( theGraphEl instanceof IRPGraphNode ){
				
				ConvertToNote theConverter = new ConvertToNote( (IRPGraphNode) theGraphEl, theContext );
				theConverter.performConversion();
			}	
		}
	}

	protected boolean _isValid;
	protected IRPGraphNode _srcGraphNode;
	protected IRPGraphNode _tgtGraphNode;
	protected IRPDiagram _diagram;
	protected IRPModelElement _srcModelEl;
	protected BaseContext _context;
	
	public ConvertToNote(
			IRPGraphNode theNode,
			BaseContext context ) {
		
		_srcGraphNode = theNode;
		_context = context;
		_srcModelEl = _srcGraphNode.getModelObject();
		_diagram = _srcGraphNode.getDiagram();
		
		@SuppressWarnings("unchecked")
		List<IRPDependency> theExistingDeps = _srcModelEl.getDependencies().toList();
		
		if( theExistingDeps.isEmpty() ){
			_isValid = true;
		} else {
			_isValid = false;
		}
	}
	
	public void performConversion(){

		if( _isValid ){

			GraphNodeInfo theNodeInfo = new GraphNodeInfo( _srcGraphNode, _context );
			
			_tgtGraphNode = _diagram.addNewNodeByType(
					"Note", 
					theNodeInfo.getTopLeftX(), 
					theNodeInfo.getTopLeftY(), 
					theNodeInfo.getWidth(), 
					theNodeInfo.getHeight() );
			
			if( _srcModelEl instanceof IRPClass ){
				String name = _srcModelEl.getName();
				_tgtGraphNode.setGraphicalProperty( "Text", name );
			}
			
			IRPCollection theElsToRemove = _context.createNewCollection();
			theElsToRemove.addGraphicalItem( _srcGraphNode );
			_diagram.removeGraphElements( theElsToRemove );
			
			_srcModelEl.deleteFromProject();

		}
	}
	
}
