package com.mbsetraining.sysmlhelper.spreadsheetimporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.mbsetraining.sysmlhelper.businessvalueplugin.BusinessValue_Context;
import com.mbsetraining.sysmlhelper.common.BaseContext;
import com.mbsetraining.sysmlhelper.spreadsheetimporter.RowInfo.RowInfoStatus;
import com.telelogic.rhapsody.core.IRPApplication;
import com.telelogic.rhapsody.core.IRPDiagram;
import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPPackage;
import com.telelogic.rhapsody.core.IRPStereotype;
import com.telelogic.rhapsody.core.RhapsodyAppServer;

public class VisioDataImporterFromExcel {

	private static final String FILE_NAME = "C:\\Users\\frase\\Downloads\\LocationTable.xlsx";

	private static String getStringValueFrom( 
			Cell theCell ){

		String theValue = "";

		if (theCell.getCellType() == CellType.NUMERIC) {

			Double numericCellValue = theCell.getNumericCellValue();

			if( numericCellValue != null ){				
				theValue = numericCellValue.toString();
			}

		} else if( theCell.getCellType() == CellType.STRING ){

			String stringCellValue = theCell.getStringCellValue();

			if( stringCellValue != null ){
				theValue = stringCellValue;
			}
		}

		return theValue;
	}

	public static void main(String[] args) {

		try {
			FileInputStream excelFile = new FileInputStream(new File(FILE_NAME));
			Workbook workbook = new XSSFWorkbook(excelFile);
			Sheet datatypeSheet = workbook.getSheetAt(0);
			Iterator<Row> iterator = datatypeSheet.iterator();


			IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
			BusinessValue_Context context = new BusinessValue_Context( theRhpApp.getApplicationConnectionString() );
			
			RowList theRecords = new RowList( context );

			while( iterator.hasNext() ){

				Row currentRow = iterator.next();
				Iterator<Cell> cellIterator = currentRow.iterator();

				Integer currentRowNum = currentRow.getRowNum();

				RowInfo theInfo = new RowInfo( context );

				// ignore first row
				if( currentRowNum != -1 ){

					while( cellIterator.hasNext() ){

						Cell currentCell = cellIterator.next();

						String stringCellValue = getStringValueFrom( currentCell );

						int columnIndex = currentCell.getColumnIndex();

						System.out.print( stringCellValue + " (r=" + currentRowNum + ", c=" + columnIndex + ")\n" );

						if( columnIndex == 0 ){
							theInfo._ShapeID = stringCellValue;
						} else if( columnIndex == 1 ){
							theInfo._DisplayedText = stringCellValue;
						} else if( columnIndex == 2 ){
							theInfo._Col1 = stringCellValue;
						} else if( columnIndex == 3 ){
							theInfo._Col2 = stringCellValue;								
						} else if( columnIndex == 4 ){
							theInfo._Col3 = stringCellValue;;
						} else if( columnIndex == 5 ){
							theInfo._Col4 = stringCellValue;
						}
					}
					
					RowInfoStatus rowInfoStatus = theInfo.parse();
					
					if( rowInfoStatus == RowInfoStatus.GRAPH_NODE_FOUND ||
							rowInfoStatus == RowInfoStatus.GRAPH_EDGE_FOUND ){
						
						theRecords.put( currentRowNum.toString(), theInfo );
					
					} else {
						System.out.print( "Unable to parse line " + currentRowNum.toString() );
					}

					System.out.println();	
				}
			}

			theRecords.dumpInfo();

			IRPModelElement theSelectedEl = context.getSelectedElement(false);

			//IRPStereotype theTimeBlockStereotype = (IRPStereotype) theRhpPrj.
			//		findElementsByFullName("ExecutableMBSEProfile::TimeBlock", "Stereotype");

			//@SuppressWarnings("unchecked")
			//List<IRPRequirement> theRequirements = theRhpPrj.getNestedElementsByMetaClass( "Requirement", 1 ).toList();

			if( theSelectedEl instanceof IRPPackage ){

				String uniqueName = context.determineUniqueNameBasedOn( 
						"ExamplePkg", "Package", context.get_rhpPrj() );
				
				IRPStereotype thePkgStereotype = (IRPStereotype) context.get_rhpPrj().
						findElementsByFullName("BusinessValueProfile::_Packages::BusinessValuePackage", "Stereotype");
				
				IRPPackage theRootPkg = (IRPPackage) theSelectedEl.addNewAggr( "Package", uniqueName );
				theRootPkg.setStereotype( thePkgStereotype );
				
				IRPDiagram theDiagram = theRootPkg.addObjectModelDiagram( "" );
				
				IRPStereotype theBVDStereotype = (IRPStereotype) context.get_rhpPrj().
						findElementsByFullName("BusinessValueProfile::_Diagrams::BusinessValueDiagram", "Stereotype");
				
				if( theBVDStereotype != null ){
					theDiagram.setStereotype( theBVDStereotype );
				}
				
				//theDiagram.setPropertyValue( "General.Graphics.WrapNameCompartmentText", "True" );
				//theDiagram.setPropertyValue( "ObjectModelGe.Class.ShowAttributes", "None" );		
				//theDiagram.setPropertyValue( "ObjectModelGe.Class.ShowOperations", "None" );		

				
				theRecords.addNodesToModel( theRootPkg, theDiagram );
				theRecords.addEdgesToModel( theRootPkg, theDiagram );
				
				theDiagram.openDiagram();

			}
			


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
