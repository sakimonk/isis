package org.nakedobjects.viewer.skylark;

import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.object.reflect.SetValueCommand;
import org.nakedobjects.object.reflect.Value;
import org.nakedobjects.security.Session;

public class ValueField extends ObjectField implements ValueContent{
	private NakedValue value;
	
	public ValueField(NakedObject parent, NakedValue value, Value field) {
	    super(parent, field);
		this.value = value;
	}

	public String debugDetails() {
	    return super.debugDetails() +  "  object:" + value + "\n";  
	}
	
	public Value getValueField() {
		return (Value) getField();
	}

	public NakedValue getValue() {
		return value;
	}

	public void menuOptions(MenuOptionSet options) {
	}
	
	public String toString() {
		return value + "/"  + getField();
	}

    public void updateDerivedValue(NakedValue object) {
        this.value = object;
    }
    
    public boolean canChangeValue() {
    	Value objectField = getValueField();
        boolean persistent = !objectField.isDerived();
        boolean fieldReadable = objectField.getAbout(Session.getSession().getSecurityContext(), getParent()).canUse().isAllowed();
        boolean parentReadable = getParent().about().canUse().isAllowed();
        boolean objectEditable = getValue().about().canUse().isAllowed();

        return persistent && fieldReadable && parentReadable && objectEditable;
    }
    
    public void parseEntry(final String entryText) throws InvalidEntryException {
         try {
	        NakedObject parent = getParent();
	//        getViewManager().getUndoStack().add(new SetValueCommand(parent, getObjectField()));
	        getValueField().parseAndSave(parent, entryText);
	  //      getState().setValid();
        } catch(InvalidEntryException e) {
        //    getState().setInvalid();
            throw e;
        }
    }

    public void refresh() {
        Value field = getValueField();

        if (field.isDerived()) {
            getValue().copyObject(field.get(getParent()));
        }
    }


}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/