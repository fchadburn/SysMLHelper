package com.mbsetraining.sysmlhelper.spreadsheetimporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.mbsetraining.sysmlhelper.common.BaseContext;
import com.mbsetraining.sysmlhelper.spreadsheetimporter.RowInfo.RowInfoStatus;
import com.telelogic.rhapsody.core.*;

public class RowList extends HashMap<String, RowInfo> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3425978918360059424L;

	protected BaseContext _context;

	protected IRPStereotype _measuredByStereotype;
	protected IRPStereotype _needsStereotype;
	protected IRPStereotype _tier1GoalStereotype;
	protected IRPStereotype _tier2GoalStereotype;
	protected IRPStereotype _tier3GoalStereotype;

	public RowList(
			BaseContext context ) {
		_context = context;
		
		_needsStereotype = (IRPStereotype) _context.get_rhpPrj().
				findElementsByFullName(
						"BusinessValueProfile::_Relations::Needs", 
						"Stereotype" );
		
		_measuredByStereotype = (IRPStereotype) _context.get_rhpPrj().
				findElementsByFullName(
						"BusinessValueProfile::_Elements::MeasuredBy", 
						"Stereotype" );
		
		_tier1GoalStereotype = (IRPStereotype) _context.get_rhpPrj().
				findElementsByFullName(
						"BusinessValueProfile::_Elements::Tier1Goal", 
						"Stereotype" );
		
		_tier2GoalStereotype = (IRPStereotype) _context.get_rhpPrj().
				findElementsByFullName(
						"BusinessValueProfile::_Elements::Tier2Goal", 
						"Stereotype" );
		
		_tier3GoalStereotype = (IRPStereotype) _context.get_rhpPrj().
				findElementsByFullName(
						"BusinessValueProfile::_Elements::Tier3Goal", 
						"Stereotype" );
	}

	public void dumpInfo(){

		System.out.print( "*************************************************\n" );

		int count = 0;

		for( java.util.Map.Entry<String, RowInfo>  nodeId : this.entrySet() ){

			count++;
			RowInfo value = nodeId.getValue();


			//String msg = count + " - " + record._DisplayedText + " (node=" + record._ShapeID + ", height=" + record._Height + ", width=" + record._Width + ")";
			String msg = count + " - " + value.getInfo();

			System.out.println( msg );
		}

		System.out.print( "\n*************************************************" );


	}

	boolean hasRelationToOrFrom( 
			RowInfo record ){
		
		boolean hasRelationToOrFrom = false;
		
		for( java.util.Map.Entry<String, RowInfo>  nodeId : this.entrySet() ){

			RowInfo candidate = nodeId.getValue();

			if( candidate._Status == RowInfoStatus.GRAPH_EDGE_FOUND ){
			
				if( candidate._xFromPosition >= record._xPosition &&
						candidate._xFromPosition <= record._xPosition + candidate._nWidth &&
						candidate._yFromPosition >= record._yPosition &&
						candidate._yFromPosition <= record._yPosition + candidate._nHeight ){
					
					_context.info( "Found " + candidate.getParsedInfo() + " is to/from " + record.getParsedInfo() );
					hasRelationToOrFrom = true;
					break;
				}
			}
		}
		
		return hasRelationToOrFrom;
	}
	
	public void addNodesToModel(
			IRPPackage theRootPkg,
			IRPDiagram theDiagram ){

		System.out.print( "*************************************************\n" );

		int count = 0;

		for( java.util.Map.Entry<String, RowInfo>  nodeId : this.entrySet() ){

			RowInfo record = nodeId.getValue();

			if( record._Status == RowInfoStatus.GRAPH_NODE_FOUND ){

				count++;

				RowInfo recordToLeft = getRowInfoToLeftOf( record );

				if( recordToLeft != null ){

					record.addAttributeToDiagram( theRootPkg, theDiagram, this );
					record.setStereotype( _measuredByStereotype );
									
				} else{
					
					//if( hasRelationToOrFrom( record ) ){						
						record.addClassToDiagram( theRootPkg, theDiagram, this );
						
						if( record._yPosition > 0 && 
								record._yPosition < 300 ){
							
							record.setStereotype( _tier1GoalStereotype );
						
						} else if( record._yPosition >= 300 && record._yPosition < 1000 ){
							
							record.setStereotype( _tier2GoalStereotype );
						
						} else if( record._yPosition >= 1000 ){
							
							record.setStereotype( _tier3GoalStereotype );
						}
						
					//} else {
					//	record.addNoteToDiagram( theRootPkg, theDiagram, this );
					//}
				}
			}
		}
		
		for( java.util.Map.Entry<String, RowInfo>  nodeId : this.entrySet() ){

			RowInfo record = nodeId.getValue();

			if( record._Status == RowInfoStatus.GRAPH_NODE_FOUND ){

				count++;

				RowInfo recordToRight = getRowInfoToLeftOf( record );

				if( recordToRight != null ){
					
					try {
						record._modelEl.setOwner( recordToRight._modelEl );

					} catch (Exception e) {
						_context.info( "Exception trying to set owner of " + _context.elInfo( record._modelEl ) + " from " + _context.elInfo( record._modelEl.getOwner() ) + " to " + recordToRight._modelEl  );
					}
				}
			}
		}
		System.out.print( "\n*************************************************" );

	}

	public void addEdgesToModel(
			IRPPackage theRootPkg,
			IRPDiagram theDiagram ){

		System.out.print( "*************************************************\n" );

		int count = 0;

		for( java.util.Map.Entry<String, RowInfo>  nodeId : this.entrySet() ){

			count++;
			RowInfo record = nodeId.getValue();

			if( record._Status == RowInfoStatus.GRAPH_EDGE_FOUND ){
				record.addDependencyToDiagram( theRootPkg, theDiagram, this );
				
				if( record._modelEl != null ){					
					record._modelEl.setStereotype(_needsStereotype);
				}
			}
		}

		System.out.print( "\n*************************************************" );
	}

	private RowInfo getRowInfoToLeftOf( 
			RowInfo theRowInfo ){

		RowInfo theOverlappingInfo = null;
		List<RowInfo> theOverlappingInfos = new ArrayList<RowInfo>();

		for( java.util.Map.Entry<String, RowInfo>  nodeId : this.entrySet() ){

			RowInfo record = nodeId.getValue();

			if( record._Status == RowInfoStatus.GRAPH_NODE_FOUND &&
					record != theRowInfo && // not same node
					theRowInfo.isToRightOf( record ) ){
				theOverlappingInfos.add( record );
			}
		}

		if( theOverlappingInfos.size() > 1 ){
			_context.warning( "Too many overlapping found");
			theOverlappingInfo = theOverlappingInfos.get( 0 );

		} else if( theOverlappingInfos.size() == 1 ){
			theOverlappingInfo = theOverlappingInfos.get( 0 );
		}

		return theOverlappingInfo;
	}

	public IRPClass getOrCreateBlockWith( String theName, IRPPackage underPackage ){

		IRPClass theClass = underPackage.findClass( theName );

		if( theClass == null ){
			theClass = underPackage.addClass( theName );
			theClass.changeTo( "Block" );


		}

		return theClass;
	}

}
