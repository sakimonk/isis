package org.nakedobjects.viewer.lightweight.view;

import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedError;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.NotPersistableException;
import org.nakedobjects.object.collection.ArbitraryCollection;
import org.nakedobjects.viewer.lightweight.ObjectDrag;
import org.nakedobjects.viewer.lightweight.RootView;
import org.nakedobjects.viewer.lightweight.util.ViewFactory;


public class ArbitraryList extends StandardList implements RootView {
    public ArbitraryList() {
        setBorder(new RootBorder());
    }

    public NakedClass forNakedClass() {
        return null;
    }

    public String getName() {
        return "ArbitraryList";
    }

    public void dropObject(ObjectDrag drag) {
        NakedObject source = drag.getSourceObject();

        if (canAdd(source).isAllowed()) {
            //	     InternalCollection target = ((InternalCollection) getObject());
            if (source instanceof NakedClass) {
                source = (NakedObject) ((NakedClass) source).acquireInstance();

                try {
                    NakedObjectManager.getInstance().makePersistent(source);
                } catch (NotPersistableException e) {
                    source = new NakedError("Failed to create object", e);

                    RootView view = ViewFactory.getViewFactory().createRootView(source);
                    view.setLocation(drag.getRelativeLocation());
                    getWorkspace().addRootView(view);

                    return;
                }

                source.created();
            }

            if (canAdd(source).isAllowed()) {
                ((ArbitraryCollection) getObject()).add(source);
                invalidateLayout();
                validateLayout();
            }
        }
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2003 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */