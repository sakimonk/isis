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

package org.apache.isis.viewer.wicket.ui.components.bookmarkedpages;

import com.google.inject.Inject;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.mementos.PageParameterNames;
import org.apache.isis.viewer.wicket.model.models.BookmarkTreeNode;
import org.apache.isis.viewer.wicket.model.models.BookmarkedPagesModel;
import org.apache.isis.viewer.wicket.model.models.ImageResourceCache;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;
import org.apache.isis.viewer.wicket.ui.util.Links;

public class BookmarkedPagesPanel extends PanelAbstract<BookmarkedPagesModel> {

    private static final long serialVersionUID = 1L;
    
    private static final String ID_BOOKMARK_LIST = "bookmarkList";
    private static final String ID_BOOKMARKED_PAGE_LINK = "bookmarkedPageLink";
    private static final String ID_CLEAR_BOOKMARK_LINK = "clearBookmarkLink";
    private static final String ID_BOOKMARKED_PAGE_ITEM = "bookmarkedPageItem";
    private static final String ID_BOOKMARKED_PAGE_TITLE = "bookmarkedPageTitle";
    
    private static final String ID_BOOKMARKED_PAGE_ICON = "bookmarkedPageImage";
    
    private static final String CLEAR_BOOKMARKS = "clearBookmarks";

    private static final JavaScriptResourceReference SLIDE_PANEL_JS = new JavaScriptResourceReference(BookmarkedPagesPanel.class, "slide-panel.js");

    @Inject
    private PageClassRegistry pageClassRegistry;

    public BookmarkedPagesPanel(final String id, final BookmarkedPagesModel bookmarkedPagesModel) {
        super(id, bookmarkedPagesModel);
        buildGui();
    }

    private void buildGui() {

        final WebMarkupContainer container = new WebMarkupContainer(ID_BOOKMARK_LIST) {
            private static final long serialVersionUID = 1L;
            @Override
            public void renderHead(IHeaderResponse response) {
                response.render(JavaScriptReferenceHeaderItem.forReference(SLIDE_PANEL_JS));
            }
        };
        // allow to be updated by AjaxLink
        container.setOutputMarkupId(true); 
        add(container);

        final BookmarkedPagesModel bookmarkedPagesModel = getModel();

        final AjaxLink<Void> clearAllBookmarksLink = new AjaxLink<Void>(CLEAR_BOOKMARKS){

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                BookmarkedPagesPanel.this.getModel().clear();
                setEnabled(false);
                target.add(container, this);
            }
        };
        clearAllBookmarksLink.setOutputMarkupId(true);
        add(clearAllBookmarksLink);
        clearAllBookmarksLink.setOutputMarkupId(true);

        if(getModel().isEmpty()) {
            clearAllBookmarksLink.setVisible(false);
        }


        final ListView<BookmarkTreeNode> listView = new ListView<BookmarkTreeNode>(ID_BOOKMARKED_PAGE_ITEM, bookmarkedPagesModel) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<BookmarkTreeNode> item) {
                final BookmarkTreeNode node = item.getModelObject();
                final PageParameters pageParameters = node.getPageParameters();
                
                final PageType pageType = PageParameterNames.PAGE_TYPE.getEnumFrom(pageParameters, PageType.class);
                final Class<? extends Page> pageClass = pageClassRegistry.getPageClass(pageType);

                final AjaxLink<Object> clearBookmarkLink = new AjaxLink<Object>(ID_CLEAR_BOOKMARK_LINK) {

                    private static final long serialVersionUID = 1L;
                    
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        bookmarkedPagesModel.remove(node);
                        if(bookmarkedPagesModel.isEmpty()) {
                            permanentlyHide(CLEAR_BOOKMARKS);
                        }
                        target.add(container, clearAllBookmarksLink);
                    }
                    
                };
                if(node.getDepth() == 0) {
                    clearBookmarkLink.add(new CssClassAppender("clearBookmark"));
                } else {
                    clearBookmarkLink.setEnabled(true);
                }
                item.add(clearBookmarkLink);
                
                final AbstractLink link = Links.newBookmarkablePageLink(ID_BOOKMARKED_PAGE_LINK, pageParameters, pageClass);

                final RootOid oid = BookmarkedPagesModel.oidFrom(pageParameters);
                ObjectSpecification objectSpec = null;
                if(oid != null) {
                    ObjectSpecId objectSpecId = oid.getObjectSpecId();
                    objectSpec = getSpecificationLoader().lookupBySpecId(objectSpecId);
                }
                final ResourceReference imageResource = imageCache.resourceReferenceForSpec(objectSpec);
                final Image image = new Image(ID_BOOKMARKED_PAGE_ICON, imageResource) {
                    private static final long serialVersionUID = 1L;
                    @Override
                    protected boolean shouldAddAntiCacheParameter() {
                        return false;
                    }
                };
                link.addOrReplace(image);

                String title = BookmarkedPagesModel.titleFrom(pageParameters);
                final Label label = new Label(ID_BOOKMARKED_PAGE_TITLE, title);
                link.add(label);
                item.add(link);
                if(bookmarkedPagesModel.isCurrent(pageParameters)) {
                    item.add(new CssClassAppender("currentBookmark"));
                }
                item.add(new CssClassAppender("bookmarkDepth" + node.getDepth()));
            }
        };
        container.add(listView);


    }
    
    // ///////////////////////////////////////////////
    // Dependency Injection
    // ///////////////////////////////////////////////

    @Inject
    private ImageResourceCache imageCache;

    protected ImageResourceCache getImageCache() {
        return imageCache;
    }

    
    protected SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

}
