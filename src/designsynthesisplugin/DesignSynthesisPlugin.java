package designsynthesisplugin;

import java.util.List;

import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class DesignSynthesisPlugin extends RPUserPlugin {

	protected ExecutableMBSE_Context _context = null;
	protected PortCreator _portCreator = null;

	// called when plug-in is loaded
	public void RhpPluginInit(
			final IRPApplication theRhapsodyApp ){

		_context = new ExecutableMBSE_Context( theRhapsodyApp.getApplicationConnectionString() );

		String msg = "The ExecutableMBSE component of the SysMLHelperPlugin was loaded successfully.";		

		_context.info( msg );

		_portCreator = new PortCreator(_context);
	}

	// called when the plug-in pop-up menu (if applicable) is selected
	public void OnMenuItemSelect(
			String menuItem ){

		IRPModelElement theSelectedEl = _context.getSelectedElement();			
		List<IRPModelElement> theSelectedEls = _context.getSelectedElements();

		_context.info("Starting ("+ theSelectedEls.size() + " elements were selected) ...");

		if( !theSelectedEls.isEmpty() ){
			//selElemName = theSelectedEl.getName();	

			if (menuItem.equals(_context.getString("designsynthesisplugin.MakeAttributeAPublishFlowportMenu"))){

				if (theSelectedEl instanceof IRPAttribute){
					try {
						_portCreator.createPublishFlowportsFor( theSelectedEls );

					} catch (Exception e) {
						_context.error("Error: Exception in OnMenuItemSelect when invoking createPublishFlowportsFor");
					}
				}

			} else if (menuItem.equals(_context.getString("designsynthesisplugin.MakeAttributeASubscribeFlowportMenu"))){

				if (theSelectedEl instanceof IRPAttribute){
					try {
						_portCreator.createSubscribeFlowportsFor( theSelectedEls );

					} catch (Exception e) {
						_context.error("Error: Exception in OnMenuItemSelect when invoking createSubscribeFlowportsFor");
					}
				}

			} else if (menuItem.equals(_context.getString("designsynthesisplugin.DeleteAttributeAndRelatedElementsMenu"))){

				try {
					if( theSelectedEl instanceof IRPAttribute ){
						_portCreator.deleteAttributeAndRelatedEls( (IRPAttribute) theSelectedEl );
					} else if ( theSelectedEl instanceof IRPSysMLPort ){
						_portCreator.deleteFlowPortAndRelatedEls( (IRPSysMLPort) theSelectedEl );
					}

				} catch (Exception e) {
					_context.error("Error: Exception in OnMenuItemSelect when invoking designsynthesisplugin.DeleteAttributeAndRelatedElementsMenu");
				}

			} else {
				_context.warning( _context.elInfo( theSelectedEl ) + " was invoked with menuItem='" + menuItem + "'");
			}
		} // else No selected element

		_context.debug("... completed");
	}

	// if true is returned the plugin will be unloaded
	public boolean RhpPluginCleanup() {

		_context = null;
		_portCreator = null;
		return true; // plug-in will be unloaded now (on project close)
	}

	@Override
	public void RhpPluginInvokeItem() {		
	}

	@Override
	public void OnTrigger(String trigger) {		
	}

	@Override
	public void RhpPluginFinalCleanup() {		
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
