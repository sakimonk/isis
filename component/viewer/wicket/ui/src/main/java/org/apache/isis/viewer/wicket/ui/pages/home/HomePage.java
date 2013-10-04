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

package org.apache.isis.viewer.wicket.ui.pages.home;

import java.util.List;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.dashboard.DashboardFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.pages.PageAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;

/**
 * Web page representing the home page (showing a welcome message).
 */
@AuthorizeInstantiation("org.apache.isis.viewer.wicket.roles.USER")
public class HomePage extends PageAbstract {

    private static final long serialVersionUID = 1L;

    public HomePage() {
        super(new PageParameters(), ApplicationActions.INCLUDE);

        addChildComponents(null);
        buildGui();

        addBookmarkedPages();
    }

    private void buildGui() {
        final ObjectAdapter dashboardAdapter = lookupDashboard();
        if(dashboardAdapter != null) {
            Components.permanentlyHide(this, ComponentType.WELCOME); 
            getComponentFactoryRegistry().addOrReplaceComponent(this, ComponentType.ENTITY, new EntityModel(dashboardAdapter));
        } else {
            Components.permanentlyHide(this, ComponentType.ENTITY);
            getComponentFactoryRegistry().addOrReplaceComponent(this, ComponentType.WELCOME, null);
        }
    }

    private ObjectAdapter lookupDashboard() {
        List<ObjectAdapter> serviceAdapters = IsisContext.getPersistenceSession().getServices();
        for (ObjectAdapter serviceAdapter : serviceAdapters) {
            final ObjectSpecification serviceSpec = serviceAdapter.getSpecification();
            if(serviceSpec.containsFacet(DashboardFacet.class)) {
                return serviceAdapter;
            }
        }
        return null;
    }
}
