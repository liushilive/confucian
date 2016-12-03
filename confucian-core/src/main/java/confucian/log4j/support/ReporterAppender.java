package confucian.log4j.support;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAliases;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.testng.Reporter;

import java.io.Serializable;

@Plugin(name = ReporterAppender.PLUGIN_NAME, category = "Core", elementType = Appender.ELEMENT_TYPE, printObject = true)
public class ReporterAppender extends AbstractAppender {
    static final String PLUGIN_NAME = "Reporter";

    private ReporterAppender(final String name, final Filter filter, final Layout layout) {
        super(name, filter, layout, false);
    }

    @PluginFactory
    public static ReporterAppender createAppender(
            @PluginAttribute("name") @Required(message = "必须指定一个附加目的地的名称") final String name,
            @PluginElement("Filter") final Filter filter,
            @PluginElement("Layout") Layout<? extends Serializable> layout) {
        return new ReporterAppender(name, filter, layout);
    }

    // @PluginFactory
    public static ReporterAppender createAppender(@PluginElement("AppenderRef") final AppenderRef[] appenderRefs,
                                                  @PluginAttribute("errorRef") @PluginAliases("error-ref") final String errorRef,
                                                  @PluginAttribute("blocking") final String blocking,
                                                  @PluginAttribute("bufferSize") final String size,
                                                  @PluginAttribute("name") final String name,
                                                  @PluginAttribute("includeLocation") final String includeLocation,
                                                  @PluginElement("Filter") final Filter filter,
                                                  @PluginConfiguration final Configuration config,
                                                  @PluginAttribute("ignoreExceptions") final String ignore) {
        return null;
    }

    @Override
    public void append(final LogEvent event) {
        final Layout<? extends Serializable> layout = getLayout();
        if (layout != null && layout instanceof AbstractStringLayout) {
            Reporter.log(((AbstractStringLayout) layout).toSerializable(event) + "</br>");
        } else {
            Reporter.log(event.getMessage().getFormattedMessage() + "</br>", true);
        }
    }
}
