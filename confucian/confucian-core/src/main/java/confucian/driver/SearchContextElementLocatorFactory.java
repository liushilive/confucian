package confucian.driver;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.support.pagefactory.DefaultElementLocator;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;

import java.lang.reflect.Field;

/**
 * 搜索上下文元素定位器工厂
 */
public class SearchContextElementLocatorFactory implements ElementLocatorFactory {

    private final SearchContext context;

    /**
     * Instantiates a new Search context element locator factory.
     *
     * @param context the context
     */
    public SearchContextElementLocatorFactory(SearchContext context) {
        this.context = context;
    }

    @Override
    public ElementLocator createLocator(Field field) {
        return new DefaultElementLocator(context, field);
    }

}
