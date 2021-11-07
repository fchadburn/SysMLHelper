package com.mbsetraining.sysmlhelper.common;

import java.util.Date;

import com.telelogic.rhapsody.core.IRPProject;
import com.telelogic.rhapsody.core.IRPStereotype;
import com.telelogic.rhapsody.core.IRPTag;

public class ProfileInfo {

	private static final String PROFILE_INFO = "ProfileInfo";

	protected String _profileName;	
	protected Date _profileDate;
	protected String _profileDateString;
	protected String _profileVersionString;
	protected String _profilePropertiesString;
	protected IRPTag _profileDateTag;
	protected IRPTag _profileVersionTag;
	protected IRPTag _profilePropertiesTag;
	protected Boolean _isValid;

	protected BaseContext _context;

	public ProfileInfo( 
			String profileName,
			BaseContext context ){

		_context = context;
		_profileName = profileName;

		// The stereotype in the profile will give details about current profile date/version
		IRPStereotype profileInfoStereotype = 
				_context.getExistingStereotype( 
						PROFILE_INFO, 
						_context.get_rhpPrj() );

		if( profileInfoStereotype == null ){
			_context.error( "Could not find ProfileInfo stereotype in profile" );	
		} else {
			_profileDateTag = profileInfoStereotype.getTag( "ProfileDate" );	

			if( _profileDateTag == null ){
				_context.warning( "Warning, could not find ProfileDate tag in profile" );	
			} else {

				_profileDateString = _profileDateTag.getValue();
				_profileDate = _context.getDate( _profileDateString );

				if( _profileDate == null ){
					_context.warning( "Warning, ProfileDate could not be parsed" );	
				}
			}

			_profileVersionTag = profileInfoStereotype.getTag( "ProfileVersion" );

			if( _profileVersionTag == null ){
				_context.warning( "Warning, could not find ProfileVersion tag in profile" );
			} else {
				_profileVersionString = _profileVersionTag.getValue();
			}

			_profilePropertiesTag = profileInfoStereotype.getTag( "ProfileProperties" );

			if( _profilePropertiesTag == null ){
				_context.warning( "Warning, could not find ProfileProperties tag in profile" );	
			} else {
				_profilePropertiesString = _profilePropertiesTag.getValue();
			}
		}

		if( _profileName != null &&
				_profileDate != null &&
				_profileDateString != null &&
				_profileVersionString != null &&
				_profilePropertiesString != null ){

			_isValid = true;
		} else {
			_isValid = false;
		}
	}

	public void setProjectToProfileTagValues(){

		if( _isValid ){

			IRPProject thePrj = _context.get_rhpPrj();

			_context.info( "Setting " + _context.elInfo( _profileDateTag ) + " on " + 
					_context.elInfo( thePrj ) + " to " + _profileDateString );

			thePrj.setTagValue( _profileDateTag, _profileDateString );

			_context.info( "Setting " + _context.elInfo( _profileVersionTag ) + " on " + 
					_context.elInfo( thePrj ) + " to " + _profileVersionString );

			thePrj.setTagValue( _profileVersionTag, _profileVersionString );

			_context.info( "Setting " + _context.elInfo( _profilePropertiesTag ) + " on " + 
					_context.elInfo( thePrj ) + " to " + _profilePropertiesString );

			thePrj.setTagValue( _profilePropertiesTag, _profilePropertiesString );
		}
	}
	
	public String get_profileName() {
		return _profileName;
	}

	public Date get_profileDate() {
		return _profileDate;
	}

	public String get_profileDateString(){
		return _profileDateString;
	}

	public String get_profilePropertiesString(){
		return _profilePropertiesString;
	}

	public String get_profileVersionString(){
		return _profileVersionString;
	}

	public boolean isValid(){
		return _isValid;
	}
}
