package com.mbsetraining.sysmlhelper.spreadsheetimporter;

import java.util.ArrayList;
import java.util.List;

import com.mbsetraining.sysmlhelper.businessvalueplugin.BusinessValue_Context;
import com.mbsetraining.sysmlhelper.businessvalueplugin.BusinessValue_RPUserPlugin;
import com.mbsetraining.sysmlhelper.common.BaseContext;
import com.telelogic.rhapsody.core.*;

public class RowInfo {

	public static void main(String[] args) {
		
		BaseContext context = new BusinessValue_Context( 
				RhapsodyAppServer.getActiveRhapsodyApplication().getApplicationConnectionString());
		
		IRPModelElement theSelectedEl = context.getSelectedElement(false);
		
		if( theSelectedEl instanceof IRPPackage ){
			
			String uniqueName = context.determineUniqueNameBasedOn( "TestAttribute", "Attribute", theSelectedEl );
			
			IRPModelElement modelEl = ((IRPPackage) theSelectedEl).addGlobalVariable( uniqueName );
		}
	}
	
	
	final double _xScaleFactor = 1.5;
	final double _yScaleFactor = 1.5;

	enum RowInfoStatus {
		UNDEFINED,
		ERROR,
		GRAPH_NODE_FOUND,
		GRAPH_EDGE_FOUND,
	};
	
	protected RowInfoStatus _Status = RowInfoStatus.UNDEFINED;
	protected String _DisplayedText; 
	protected String _MasterName = ""; 
	protected String _ShapeID = "";
	protected String _ShapeName = "";
	protected String _Col1 = "";
	protected String _Col2 = "";
	protected String _Col3 = "";
	protected String _Col4 = "";

	protected IRPModelElement _modelEl;
	protected IRPGraphElement _graphEl;
	
	protected BusinessValue_Context _context;
	
	public RowInfo(
			BusinessValue_Context context ) {
		
		_context = context;
	}
	
	public String getInfo(){
		
		return 
				"XLocation=" +_Col1 + 
				", YLocation =" + _Col2 +
				", Height=" + _Col3 + 
				", Width=" + _Col4 + 
				", DisplayText=" + _DisplayedText + 
				", MasterName=" + _MasterName + 
				", ShapeID=" + _ShapeID + 
				", ShapeName=" + _ShapeName;
	}
	
	public String getParsedInfo(){
		
		String msg = "";
		
		if( _Status == RowInfoStatus.GRAPH_NODE_FOUND ){
			msg = "_xPosition=" + _xPosition + 
					", _yPosition =" + _yPosition +
					", _nHeight=" + _nHeight + 
					", _nWidth=" + _nWidth;
		} else if( _Status == RowInfoStatus.GRAPH_EDGE_FOUND ){
			msg = "_fromX=" + _xFromPosition + 
					", _fromY=" + _yFromPosition + 
					", _toX=" + _xToPosition + 
					",_toY=" + _yToPosition;
			
		}
		
		return msg;
	}
	
	protected int _xPosition;
	protected int _yPosition;
	protected int _nHeight;
	protected int _nWidth;
	protected String _name;
	
	protected int _xFromPosition;
	protected int _yFromPosition;
	protected int _xToPosition;
	protected int _yToPosition;

	public boolean isToRightOf( 
			RowInfo theInfo ){
	
		boolean isOverlappingWith = false;
		
		int leftXMin = theInfo._xPosition + theInfo._nWidth - 4;
		int leftXMax = theInfo._xPosition + theInfo._nWidth + 4;
		
		boolean yAxisOverlaps =
				( _yPosition >= theInfo._yPosition &&
				_yPosition <= theInfo._yPosition + theInfo._nHeight ) ||
				( _yPosition + _nHeight <= theInfo._yPosition + theInfo._nHeight && 
			      _yPosition + _nHeight >= theInfo._yPosition );

		if( _xPosition >= leftXMin && 
				_xPosition <= leftXMax &&
				yAxisOverlaps ){
			isOverlappingWith = true;
		}
		
		return isOverlappingWith;
	}
	
	public void setStereotype(
			IRPStereotype theStereotype ){
		
		if( _modelEl != null ){			
			_modelEl.setStereotype( theStereotype );
		}
	}
	
	public RowInfoStatus parse(){
		
		if( _Status == RowInfoStatus.UNDEFINED ){
			
			if( _ShapeID.contains( "Dynamic" ) ){
				
				parseAsGraphEdge();
			} else {
				parseAsGraphNode();
			}
		}
		
		return _Status;
	}

	private void parseAsGraphEdge() {
		
		_Status = RowInfoStatus.GRAPH_EDGE_FOUND;

		if( _Col1 == null ){
			_context.error( "Unable to parse row " + _ShapeID + " as no from x was given" );
			_Status = RowInfoStatus.ERROR;
		}
		
		if( _Col2 == null ){
			_context.error( "Unable to parse row " + _ShapeID + " as no from y was given" );
			_Status = RowInfoStatus.ERROR;
		}
		
		if( _Col3 == null ){
			_context.error( "Unable to parse row " + _ShapeID + " as no to y was given" );
			_Status = RowInfoStatus.ERROR;
		}
		
		if( _Col4 == null ){
			_context.error( "Unable to parse row " + _ShapeID + " as no to x was given" );
			_Status = RowInfoStatus.ERROR;
		}

		
		if( _Status != RowInfoStatus.ERROR ){
			
			try {
				double x = Double.parseDouble( _Col1 ) * _xScaleFactor;
				_xFromPosition = (int)x;

			} catch( Exception e ){
				_context.error( "Unable to parse row " + _ShapeID + " _xFromPosition of " + _Col1 + " e=" + e.getMessage() );
				_Status = RowInfoStatus.ERROR;
			}
			
			try {
				double y = Double.parseDouble( _Col2 ) * _yScaleFactor;
				_yFromPosition = (int)y;

			} catch( Exception e ){
				_context.error( "Unable to parse row " + _ShapeID + " _yFromPosition of " + _Col2 + " e=" + e.getMessage() );
				_Status = RowInfoStatus.ERROR;
			}
			
			try {
				double x = Double.parseDouble( _Col3 ) * _xScaleFactor;
				_xToPosition = (int)x;

			} catch (Exception e) {
				_context.error( "Unable to parse row " + _ShapeID + " _xToPosition of " + _Col3 + " e=" + e.getMessage() );
				_Status = RowInfoStatus.ERROR;
			}
			
			try {
				double y = Double.parseDouble( _Col4 ) * _yScaleFactor;
				_yToPosition = (int)y;

			} catch( Exception e ){
				_context.error( "Unable to parse row " + _ShapeID + " _yToPosition of " + _Col4 + " e=" + e.getMessage() );
				_Status = RowInfoStatus.ERROR;

			}
			
			if( _DisplayedText == null ){
				_name = "";
			} else {				
				_name = _DisplayedText.replaceAll("\\.", "").trim();
			}				
		}
	}
	
	private void parseAsGraphNode() {
		
		_Status = RowInfoStatus.GRAPH_NODE_FOUND;
		
		if( _DisplayedText == null ){
			_context.error( "Unable to parse row " + _ShapeID + " as no _DisplayedText was given" );
			_Status = RowInfoStatus.ERROR;
		}
		
		if( _Col1 == null ){
			_context.error( "Unable to parse row " + _ShapeID + " as no _XLocation was given" );
			_Status = RowInfoStatus.ERROR;
		}
		
		if( _Col2 == null ){
			_context.error( "Unable to parse row " + _ShapeID + " as no _YLocation was given" );
			_Status = RowInfoStatus.ERROR;
		}
		
		if( _Col4 == null ){
			_context.error( "Unable to parse row " + _ShapeID + " as no _Width was given" );
			_Status = RowInfoStatus.ERROR;
		}
		
		if( _Col3 == null ){
			_context.error( "Unable to parse row " + _ShapeID + " as no _Height was given" );
			_Status = RowInfoStatus.ERROR;
		}
		
		if( _Status != RowInfoStatus.ERROR ){
			
			try {
				double x = Double.parseDouble( _Col1 ) * _xScaleFactor;
				_xPosition = (int)x;

			} catch (Exception e) {
				_context.error( "Unable to parse row " + _ShapeID + " _XLocation of " + _Col1 + " e=" + e.getMessage() );
				_Status = RowInfoStatus.ERROR;
			}
			
			try {
				double y = Double.parseDouble( _Col2 ) * _yScaleFactor;
				_yPosition = (int)y;

			} catch (Exception e) {
				_context.error( "Unable to parse row " + _ShapeID + " _YLocation of " + _Col2 + " e=" + e.getMessage() );
				_Status = RowInfoStatus.ERROR;
			}
			
			try {
				double w = Double.parseDouble( _Col4 ) * _xScaleFactor;
				_nWidth = (int)w;

			} catch (Exception e) {
				_context.error( "Unable to parse row " + _ShapeID + " _Width of " + _Col4 + " e=" + e.getMessage() );
				_Status = RowInfoStatus.ERROR;
			}
			
			try {
				double h = Double.parseDouble( _Col3 ) * _yScaleFactor;
				_nHeight = (int)h;

			} catch (Exception e) {
				_context.error( "Unable to parse row " + _ShapeID + " _Height of " + _Col3 + " e=" + e.getMessage() );
				_Status = RowInfoStatus.ERROR;

			}
			
			if( _DisplayedText == null ){
				_name = "";
			} else {				
				_name = _DisplayedText.replaceAll("\\.", "").trim();
			}				
		}
	}
	
	public void addClassToDiagram(
			IRPPackage thePackage,
			IRPDiagram theDiagram,
			RowList theOtherRowInfos ){
		
		String uniqueName = _context.determineUniqueNameBasedOn( _name, "Class", thePackage );
		
		_modelEl = thePackage.addClass( uniqueName );
		
		_context.addHyperLink( _modelEl, _modelEl );
		
		_context.info( getInfo() );
		_context.info( getParsedInfo() );

		_graphEl = theDiagram.addNewNodeForElement(_modelEl, _xPosition, _yPosition, _nWidth, _nHeight);	
	}
	
	public void addNoteToDiagram(
			IRPPackage thePackage,
			IRPDiagram theDiagram,
			RowList theOtherRowInfos ){
		
		//String uniqueName = _context.determineUniqueNameBasedOn( _name, "Comment", thePackage );
		//_modelEl = thePackage.addNewAggr( "Comment", uniqueName );
		
		_context.info( getInfo() );
		_context.info( getParsedInfo() );

		_graphEl = theDiagram.addNewNodeByType("Note", _xPosition, _yPosition, _nWidth, _nHeight );
		
		_graphEl.setGraphicalProperty( "Text", _name );
	}
	
	public void addObjectToDiagram(
			IRPPackage thePackage,
			IRPDiagram theDiagram,
			RowList theOtherRowInfos ){
		
		String uniqueName = _context.determineUniqueNameBasedOn( _name, "Object", thePackage );
		
		_modelEl = thePackage.addImplicitObject( uniqueName );
		
		_context.info( getInfo() );
		_context.info( getParsedInfo() );

		_graphEl = theDiagram.addNewNodeForElement(_modelEl, _xPosition, _yPosition, _nWidth, _nHeight);	
	}
	
	public void addAttributeToDiagram(
			IRPPackage thePackage,
			IRPDiagram theDiagram,
			RowList theOtherRowInfos ){
		
		String uniqueName = _context.determineUniqueNameBasedOn( _name, "Attribute", thePackage );
		
		_modelEl = thePackage.addGlobalVariable( uniqueName );
		
		IRPClassifier theClassifier = _context.getMeasuredByDefaultClass();
		
		if( theClassifier instanceof IRPClassifier &&
				_modelEl instanceof IRPAttribute ){
			
			IRPAttribute theAttr = (IRPAttribute)_modelEl;
			theAttr.setType( theClassifier );
		}
		
		_context.info( getInfo() );
		_context.info( getParsedInfo() );

		_graphEl = theDiagram.addNewNodeForElement(_modelEl, _xPosition, _yPosition, _nWidth, _nHeight);	
	}
	
	public void addDependencyToDiagram(
			IRPPackage thePackage,
			IRPDiagram theDiagram,
			RowList theOtherRowInfos ){
		
			List<RowInfo> fromNodes = getNodesInfosAt( _xFromPosition, _yFromPosition, theOtherRowInfos );
			List<RowInfo> toNodes = getNodesInfosAt( _xToPosition, _yToPosition, theOtherRowInfos );

			if( fromNodes.size() == 1 &&
					toNodes.size() == 1 ){
				
				RowInfo fromNode = fromNodes.get(0);
				RowInfo toNode = toNodes.get(0);
				
				IRPModelElement fromEl = fromNode._modelEl;
				
				if( fromEl instanceof IRPInstance ){
					
					IRPModelElement theOwner = fromEl.getOwner();

					if( theOwner instanceof IRPClass ){
						_context.warning( "fromEl is not a class " + _context.elInfo( fromEl ) + " but able to switch to " + _context.elInfo( theOwner ) );

					} else {
						_context.error( "fromEl is not a class " + _context.elInfo( fromEl ) + " and is not owned by one" );
					}
				}
				
				IRPModelElement toEl = toNode._modelEl;
						
				if( toEl instanceof IRPInstance ){
					
					IRPModelElement theOwner = toEl.getOwner();

					if( theOwner instanceof IRPClass ){
						_context.warning( "toEl is not a class " + _context.elInfo( toEl ) + " but able to switch to " + _context.elInfo( theOwner ) );

					} else {
						_context.error( "toEl is not a class " + _context.elInfo( toEl ) + " and is not owned by one" );
					}
				}
				
				_modelEl = fromEl.addDependencyTo( toEl );
				
				_graphEl = theDiagram.addNewEdgeForElement(
						_modelEl, 
						(IRPGraphNode)fromNode._graphEl, 
						_xFromPosition, 
						_yFromPosition, 
						(IRPGraphNode)toNode._graphEl, 
						_xToPosition, 
						_yToPosition );
			}
	}
	
	public List<RowInfo> getNodesInfosAt( 
			int xPosition, 
			int yPosition, 
			RowList theCandidates ){
		
		List<RowInfo> theNodeInfos = new ArrayList<RowInfo>();
		
		for( java.util.Map.Entry<String, RowInfo>  nodeId : theCandidates.entrySet() ){
			
			RowInfo rowInfo = nodeId.getValue();
			
			if( rowInfo._Status == RowInfoStatus.GRAPH_NODE_FOUND ){
				
				//_context.info( "checking " + this.getParsedInfo() + " against " + rowInfo.getParsedInfo() );

				if(	xPosition >= rowInfo._xPosition &&
						xPosition <= rowInfo._xPosition + rowInfo._nWidth &&
						yPosition >= rowInfo._yPosition &&
						yPosition <= rowInfo._yPosition + rowInfo._nHeight ){
					
					theNodeInfos.add( rowInfo );
				}
			}

		}
		
		return theNodeInfos;
	}
}
