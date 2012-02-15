/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.runtimes.dflt.runtime.persistence.objectstore;

import java.util.List;
import java.util.Vector;

import junit.framework.Assert;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.runtimes.dflt.runtime.persistence.ObjectNotFoundException;
import org.apache.isis.runtimes.dflt.runtime.persistence.UnsupportedFindException;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommandContext;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.SaveObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceQuery;
import org.apache.isis.runtimes.dflt.runtime.transaction.ObjectPersistenceException;

public class ObjectStoreSpy implements ObjectStore, ObjectStoreTransactionManagement {

    private final Vector<String> actions = new Vector<String>();

    private ObjectAdapter getObject;

    private boolean hasInstances;

    public ObjectStoreSpy() {
        super();
    }

    @Override
    public String name() {
        return null;
    }

    // /////////////////////////////////////////////////////////////
    // Open, Close
    // /////////////////////////////////////////////////////////////

    @Override
    public void open() throws ObjectPersistenceException {
    }

    @Override
    public void close() {
    }

    @Override
    public void reset() {
        actions.removeAllElements();
    }

    // /////////////////////////////////////////////////////////////
    // Transactions
    // /////////////////////////////////////////////////////////////

    @Override
    public void startTransaction() {
        actions.addElement("startTransaction");
    }

    @Override
    public void abortTransaction() {
        actions.addElement("abortTransaction");
    }

    @Override
    public void endTransaction() {
        actions.addElement("endTransaction");
    }

    @Override
    public boolean isFixturesInstalled() {
        return true;
    }

    // /////////////////////////////////////////////////////////////
    // getObject, resolve etc
    // /////////////////////////////////////////////////////////////

    /**
     * Not API
     */
    public void setGetObject(final ObjectAdapter getObject) {
        this.getObject = getObject;
    }

    @Override
    public ObjectAdapter getObject(final Oid oid, final ObjectSpecification hint) throws ObjectNotFoundException, ObjectPersistenceException {
        if (getObject == null) {
            Assert.fail("no object expected");
        }
        Assert.assertEquals(getObject.getOid(), oid);
        return getObject;
    }

    @Override
    public void resolveImmediately(final ObjectAdapter object) {
    }

    @Override
    public void resolveField(final ObjectAdapter object, final ObjectAssociation field) throws ObjectPersistenceException {
    }

    @Override
    public boolean hasInstances(final ObjectSpecification cls) {
        return hasInstances;
    }

    @Override
    public ObjectAdapter[] getInstances(final PersistenceQuery criteria) throws ObjectPersistenceException, UnsupportedFindException {
        actions.addElement("getInstances " + criteria);
        return new ObjectAdapter[0];
    }

    // /////////////////////////////////////////////////////////////
    // save
    // /////////////////////////////////////////////////////////////

    @Override
    public CreateObjectCommand createCreateObjectCommand(final ObjectAdapter object) {
        actions.addElement("createObject " + object);
        return new CreateObjectCommand() {

            @Override
            public void execute(final PersistenceCommandContext context) throws ObjectPersistenceException {
            }

            @Override
            public String toString() {
                return "CreateObjectCommand " + object.toString();
            }

            @Override
            public ObjectAdapter onObject() {
                return object;
            }
        };
    }

    @Override
    public DestroyObjectCommand createDestroyObjectCommand(final ObjectAdapter object) {
        actions.addElement("destroyObject " + object);
        return new DestroyObjectCommand() {

            @Override
            public void execute(final PersistenceCommandContext context) throws ObjectPersistenceException {
            }

            @Override
            public String toString() {
                return "DestroyObjectCommand " + object.toString();
            }

            @Override
            public ObjectAdapter onObject() {
                return object;
            }
        };
    }

    @Override
    public SaveObjectCommand createSaveObjectCommand(final ObjectAdapter object) {
        actions.addElement("saveObject " + object);
        return new SaveObjectCommand() {

            @Override
            public void execute(final PersistenceCommandContext context) throws ObjectPersistenceException {
            }

            @Override
            public String toString() {
                return "DestroyObjectCommand " + object.toString();
            }

            @Override
            public ObjectAdapter onObject() {
                return object;
            }
        };
    }

    @Override
    public void execute(final List<PersistenceCommand> commands) throws ObjectPersistenceException {
        for (final PersistenceCommand command : commands) {
            actions.addElement("execute " + command);
            command.execute(null);
        }
    }

    // /////////////////////////////////////////////////////////////
    // Services
    // /////////////////////////////////////////////////////////////

    @Override
    public void registerService(final String name, final Oid oid) {
    }

    @Override
    public Oid getOidForService(ObjectSpecification serviceSpecification, final String name) {
        return null;
    }

    // /////////////////////////////////////////////////////////////
    // Debugging
    // /////////////////////////////////////////////////////////////

    @Override
    public void debugData(final DebugBuilder debug) {
    }

    @Override
    public String debugTitle() {
        return null;
    }

    // /////////////////////////////////////////////////////////////
    // Non API
    // /////////////////////////////////////////////////////////////

    /**
     * non API
     */
    public Vector<String> getActions() {
        return actions;
    }

    /**
     * Non API
     */
    public void assertAction(final int i, final String expected) {
        Assert.assertTrue("invalid action number " + i, actions.size() > i);
        final String actual = actions.elementAt(i);

        if (expected == null && actual == null) {
            return;
        }
        if (expected != null && actual.startsWith(expected)) {
            return;
        }
        Assert.fail("action " + i + " expected: <" + expected + "> but was: <" + actual + ">");
    }

    /**
     * Non API
     */
    public void assertLastAction(final int expectedLastAction) {
        final int actualLastAction = actions.size() - 1;
        Assert.assertEquals(expectedLastAction, actualLastAction);
    }

}
