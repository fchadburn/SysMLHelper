package functionalanalysisplugin;

import java.util.List;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class FunctionalAnalysisSettings_ExecutableMBSE extends FunctionalAnalysisSettings {

	public FunctionalAnalysisSettings_ExecutableMBSE(
			ExecutableMBSE_Context context ) {

		super( context );
	}
	
	@Override
	public void setupFunctionalAnalysisTagsFor(
			IRPPackage theRootPackage,
			IRPClass theAssemblyBlockUnderDev,
			IRPPackage thePackageForEventsAndInterfaces, 
			IRPPackage thePackageForActorsAndTest,
			IRPPackage thePackageForBlocks ){

		if( theRootPackage != null ){

			String theStereotypeName = 
					((ExecutableMBSE_Context) _context).getSimulationPackageStereotype( theRootPackage );

			setElementTagValueOn( 
					theRootPackage, 
					theStereotypeName, 
					tagNameForAssemblyBlockUnderDev, 
					theAssemblyBlockUnderDev );

			setElementTagValueOn( 
					theRootPackage, 
					theStereotypeName, 
					tagNameForPackageForActorsAndTest, 
					thePackageForActorsAndTest );

			setElementTagValueOn( 
					theRootPackage, 
					theStereotypeName, 
					tagNameForPackageForEventsAndInterfaces, 
					thePackageForEventsAndInterfaces );

			setElementTagValueOn( 
					theRootPackage, 
					theStereotypeName, 
					tagNameForPackageForBlocks, 
					thePackageForBlocks );	
		}	
	}
	
	@Override
	public IRPPackage getSimulationSettingsPackageBasedOn(
			IRPModelElement theContextEl ){

		IRPPackage theSettingsPkg = null;

		if( theContextEl instanceof IRPProject ){

			List<IRPModelElement> thePackageEls = 
					_context.findElementsWithMetaClassAndStereotype(
							"Package", 
							((ExecutableMBSE_Context) _context).getSimulationPackageStereotype( theContextEl ), 
							theContextEl.getProject(), 
							1 );

			if( thePackageEls.isEmpty() ){

				_context.warning( "Warning in getSimulationSettingsPackageBasedOn, unable to find use case settings package");

			} else if( thePackageEls.size()==1){

				theSettingsPkg = (IRPPackage) thePackageEls.get(0);

			} else {
				_context.error( "Error in getSimulationSettingsPackageBasedOn, unable to find use case settings package");

				IRPModelElement theUserSelectedPkg = 
						UserInterfaceHelper.launchDialogToSelectElement(
								thePackageEls, 
								"Choose which settings to use", 
								true );

				if( theUserSelectedPkg != null ){
					theSettingsPkg = (IRPPackage) theUserSelectedPkg;
				}
			}

		} else if( theContextEl instanceof IRPPackage &&
				_context.hasStereotypeCalled(
						((ExecutableMBSE_Context) _context).getSimulationPackageStereotype( theContextEl ), 
						theContextEl ) ){

			_context.debug( "getSimulationSettingsPackageBasedOn, is returning " + _context.elInfo( theContextEl ) );

			theSettingsPkg = (IRPPackage) theContextEl;

		} else if( theContextEl instanceof IRPPackage &&
				_context.hasStereotypeCalled(
						((ExecutableMBSE_Context) _context).getUseCasePackageStereotype( theContextEl ), 
						theContextEl ) ){

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theReferences = theContextEl.getReferences().toList();

			for( IRPModelElement theReference : theReferences ){

				if( theReference instanceof IRPDependency ){

					IRPDependency theDependency = (IRPDependency)theReference;
					IRPModelElement theDependent = theDependency.getDependent();

					if( theDependent instanceof IRPPackage &&
							_context.hasStereotypeCalled(
									((ExecutableMBSE_Context) _context).getSimulationPackageStereotype( theContextEl ), 
									theDependent ) ){

						theSettingsPkg = (IRPPackage) theDependent;
					}
				}
			}

		} else {

			// recurse
			theSettingsPkg = getSimulationSettingsPackageBasedOn(
					theContextEl.getOwner() );
		}

		return theSettingsPkg;
	}
}

/**
 * Copyright (C) 2016-2021  MBSE Training and Consulting Limited (www.executablembse.com)

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
