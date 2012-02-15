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

package org.apache.isis.runtimes.dflt.runtime.testsystem;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.util.Hashtable;
import java.util.Vector;

import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.services.ServicesInjectorDefault;
import org.apache.isis.core.metamodel.services.container.DomainObjectContainerDefault;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.runtimes.dflt.runtime.persistence.PersistenceSessionAbstract;
import org.apache.isis.runtimes.dflt.runtime.persistence.adapterfactory.AdapterFactoryAbstract;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.AdapterManagerDefault;
import org.apache.isis.runtimes.dflt.runtime.persistence.internal.RuntimeContextFromSession;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectfactory.ObjectFactoryAbstract.Mode;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceQuery;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.MessageBroker;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.UpdateNotifier;
import org.apache.isis.runtimes.dflt.runtime.transaction.IsisTransactionDefault;
import org.apache.isis.runtimes.dflt.runtime.transaction.IsisTransactionManagerAbstract;

/**
 * Static mock implementation of {@link PersistenceSession} that provides some
 * partial implementation but also has methods to spy on interactions.
 * 
 * <p>
 * Is an alternative is to using the JMock mocking library.
 * 
 * <p>
 * Previously called <tt>TestProxyPersistor</tt>.
 */
public class TestProxyPersistenceSession extends PersistenceSessionAbstract {

    protected static class AdapterFactoryTestProxyAdapter extends AdapterFactoryAbstract {
        @Override
        public TestProxyAdapter createAdapter(final Object pojo, final Oid oid) {
            final TestProxyAdapter testProxyObjectAdapter = new TestProxyAdapter();
            testProxyObjectAdapter.setupObject(pojo);
            testProxyObjectAdapter.setupOid(oid);
            testProxyObjectAdapter.setupResolveState(oid == null ? ResolveState.VALUE : oid.isTransient() ? ResolveState.TRANSIENT : ResolveState.GHOST);

            testProxyObjectAdapter.setupSpecification(IsisContext.getSpecificationLoader().loadSpecification(pojo.getClass()));

            return testProxyObjectAdapter;
        }
    }

    private final IsisTransactionManager transactionManager = new IsisTransactionManagerAbstract<IsisTransactionDefault>() {

        @Override
        public void startTransaction() {
            actions.addElement("start transaction");
            createTransaction();
        }

        @Override
        protected IsisTransactionDefault createTransaction(final MessageBroker messageBroker, final UpdateNotifier updateNotifier) {
            return new IsisTransactionDefault(this, messageBroker, updateNotifier);
        }

        @Override
        public boolean flushTransaction() {
            actions.addElement("flush transaction");
            return false;
        }

        @Override
        public void abortTransaction() {
            getTransaction().abort();
        }

        @Override
        public void endTransaction() {
            actions.addElement("end transaction");
            getTransaction().commit();
        }

    };

    private final Vector<String> actions = new Vector<String>();

    /**
     * Playing the role of the object store.
     */
    private final Hashtable<Oid, ObjectAdapter> persistedObjects = new Hashtable<Oid, ObjectAdapter>();

    public TestProxyPersistenceSession(final PersistenceSessionFactory persistenceSessionFactory) {
        super(persistenceSessionFactory, new AdapterFactoryTestProxyAdapter(), new TestObjectFactory(Mode.RELAXED) {
        }, new ServicesInjectorDefault(), new TestProxyOidGenerator(), new AdapterManagerDefault());

        final RuntimeContextFromSession runtimeContext = new RuntimeContextFromSession();
        final DomainObjectContainerDefault container = new DomainObjectContainerDefault();
        runtimeContext.injectInto(container);
        runtimeContext.setContainer(container);

        getServicesInjector().setContainer(container);

        setTransactionManager(transactionManager);
    }

    @Override
    public void doOpen() {
        getAdapterFactory().injectInto(getAdapterManager());
        getSpecificationLoader().injectInto(getAdapterManager());
        getOidGenerator().injectInto(getAdapterManager());

    }

    // //////////////////////////////////////////////////////////////
    // TestProxy equivalent implementations
    // //////////////////////////////////////////////////////////////

    @Override
    public ObjectAdapter loadObject(final Oid oid, final ObjectSpecification spec) {
        ensureThatArg(oid, is(notNullValue()));
        ensureThatArg(spec, is(notNullValue()));

        ObjectAdapter adapter = getAdapterManager().getAdapterFor(oid);

        if (adapter == null) {
            // the objectstore or client proxy implementations will load from
            // object store.
            // This test implementation similarly looks in its persisted objects
            // map.
            adapter = persistedObjects.get(oid);
        }

        if (adapter == null) {
            // the objectstore or client proxy implementations will returns null
            // if none found.
            // This test implementation *however* throws an exception.
            throw new TestProxyException("No persisted object to get for " + oid);
        }

        return adapter;
    }

    @Override
    public void makePersistent(final ObjectAdapter object) {

        // the object store implementation calls to the PersistAlgorithm that
        // interacts with
        // the ObjectStore and then ultimately for each object invokes
        // madePersistent.
        getAdapterManager().remapAsPersistent(object);

        // this is done here explicitly; in the object store impl it is a
        // responsibility of
        // the object store.
        object.setOptimisticLock(new TestProxyVersion(1));
    }

    @Override
    public void objectChanged(final ObjectAdapter object) {
        actions.addElement("object changed " + object.getOid());
        object.setOptimisticLock(((TestProxyVersion) object.getVersion()).next());
    }

    @Override
    public void destroyObject(final ObjectAdapter object) {
        actions.addElement("object deleted " + object.getOid());
    }

    // //////////////////////////////////////////////////////////////
    // TestSupport
    // //////////////////////////////////////////////////////////////

    @Override
    public void testReset() {
        getAdapterManager().reset();
    }

    // //////////////////////////////////////////////////////////////
    // Not yet implemented
    // //////////////////////////////////////////////////////////////

    @Override
    public void resolveImmediately(final ObjectAdapter object) {
        throw new NotYetImplementedException();
    }

    @Override
    public void resolveField(final ObjectAdapter object, final ObjectAssociation association) {
        actions.addElement("object deleted " + object.getOid());
    }

    @Override
    protected ObjectAdapter[] getInstances(final PersistenceQuery criteria) {
        throw new NotYetImplementedException();
    }

    @Override
    protected Oid getOidForService(ObjectSpecification serviceSpecification, final String name) {
        throw new NotYetImplementedException();
    }

    @Override
    protected void registerService(final String name, final Oid oid) {
        throw new NotYetImplementedException();
    }

    @Override
    public void reload(final ObjectAdapter adapter) {
        throw new NotYetImplementedException();
    }

    @Override
    public boolean isFixturesInstalled() {
        throw new NotYetImplementedException();
    }

    @Override
    public boolean hasInstances(final ObjectSpecification specification) {
        throw new NotYetImplementedException();
    }

    @Override
    public String debugTitle() {
        return null;
    }

}
