package taumigrator;

public abstract class RhpElElement extends RhpEl {

	public RhpElElement(
			String theElementName, 
			String theElementType,
			String theElementGuid ) throws Exception{
		
		super( theElementName, theElementType, theElementGuid );
	}

	public RhpElElement(
			String theElementName, 
			String theElementType,
			String theElementGuid,
			RhpEl theParent ) throws Exception{
		
		super( theElementName, theElementType, theElementGuid, theParent );
	}
}

/**
 * Copyright (C) 2018-2019  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #251 29-MAY-2019: First official version of new TauMigratorProfile (F.J.Chadburn)

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