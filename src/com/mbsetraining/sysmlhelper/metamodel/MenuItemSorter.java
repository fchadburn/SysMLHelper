package com.mbsetraining.sysmlhelper.metamodel;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MenuItemSorter implements Comparator<MenuItem> {
 
    private List<Comparator<MenuItem>> listComparators;
 
    public MenuItemSorter(
    		@SuppressWarnings("unchecked") Comparator<MenuItem>... comparators ){
    	
        this.listComparators = Arrays.asList( comparators );
    }
 
    public int compare(
    		MenuItem left, 
    		MenuItem right ){
    	
        for( Comparator<MenuItem> comparator : listComparators ){
        	
            int result = comparator.compare( left, right );
            if( result != 0 ){
                return result;
            }
        }
        return 0;
    }
}

/**
 * Copyright (C) 2020-2022  MBSE Training and Consulting Limited (www.executablembse.com)

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