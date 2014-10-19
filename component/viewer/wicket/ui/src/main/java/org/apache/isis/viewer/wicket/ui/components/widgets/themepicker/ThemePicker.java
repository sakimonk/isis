package org.apache.isis.viewer.wicket.ui.components.widgets.themepicker;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.settings.ActiveThemeProvider;
import de.agilecoders.wicket.core.settings.IBootstrapSettings;
import de.agilecoders.wicket.core.settings.ITheme;
import de.agilecoders.wicket.core.settings.SingleThemeProvider;
import de.agilecoders.wicket.core.util.Attributes;
import de.agilecoders.wicket.themes.markup.html.bootstrap.BootstrapThemeTheme;
import de.agilecoders.wicket.themes.markup.html.bootswatch.BootswatchTheme;
import de.agilecoders.wicket.themes.markup.html.bootswatch.BootswatchThemeProvider;
import de.agilecoders.wicket.themes.markup.html.vegibit.VegibitTheme;
import de.agilecoders.wicket.themes.markup.html.vegibit.VegibitThemeProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

/**
 * A panel used as a Navbar item to change the application theme/skin
 */
public class ThemePicker extends Panel {

    /**
     * Constructor
     *
     * @param id component id
     */
    public ThemePicker(String id) {
        super(id);

        ListView<String> themesView = new ListView<String>("themes", getThemeNames()) {

            @Override
            protected void populateItem(ListItem<String> item) {
                final String themeName = item.getModelObject();

                if (themeName.equals(getActiveThemeProvider().getActiveTheme().name())) {
                    item.add(AttributeModifier.append("class", "active"));
                }
                item.add(new AjaxLink<Void>("themeLink") {
                    // use Ajax link because Link's url looks like /ENTITY:3 and this confuses the browser
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        IBootstrapSettings bootstrapSettings = Bootstrap.getSettings();
                        ITheme theme = getTheme(themeName);
                        getActiveThemeProvider().setActiveTheme(theme);
                        if (theme instanceof BootstrapThemeTheme) {
                            bootstrapSettings.setThemeProvider(new SingleThemeProvider(theme));
                        } else if (theme instanceof BootswatchTheme) {
                            bootstrapSettings.setThemeProvider(new BootswatchThemeProvider((BootswatchTheme) theme));
                        } else if (theme instanceof VegibitTheme) {
                            bootstrapSettings.setThemeProvider(new VegibitThemeProvider((VegibitTheme) theme));
                        }
                        target.add(getPage()); // repaint the whole page
                    }
                }.setBody(Model.of(themeName)));
            }
        };
        add(themesView);
    }

    private ITheme getTheme(String themeName) {
        ITheme theme;
        if ("bootstrap-theme".equals(themeName)) {
            theme = new BootstrapThemeTheme();
        } else if (themeName.startsWith("veg")) {
            theme = VegibitTheme.valueOf(themeName);
        } else {
            theme = BootswatchTheme.valueOf(themeName);
        }
        return theme;
    }

    private ActiveThemeProvider getActiveThemeProvider() {
        return Bootstrap.getSettings().getActiveThemeProvider();
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);

        tag.setName("li");
        Attributes.addClass(tag, "dropdown");
    }

    private List<String> getThemeNames() {
        final BootstrapThemeTheme bootstrapTheme = new BootstrapThemeTheme();
        List<BootswatchTheme> bootswatchThemes = Arrays.asList(BootswatchTheme.values());
        List<VegibitTheme> vegibitThemes = Arrays.asList(VegibitTheme.values());

        List<String> allThemes = new ArrayList<>();
        for (ITheme theme : bootswatchThemes) {
            allThemes.add(theme.name());
        }
        for (ITheme theme : vegibitThemes) {
            allThemes.add(theme.name());
        }
        allThemes.add(bootstrapTheme.name());


        return allThemes;
    }
}
