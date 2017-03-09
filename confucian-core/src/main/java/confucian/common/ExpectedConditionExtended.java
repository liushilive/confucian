package confucian.common;

import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * {@link ExpectedConditions} 功能扩展，在 DriverUtility.waitFor() 中使用。
 * 此类中的方法弥补 WebDriverWait 中没有抛出异常。
 * 在所有自定义 FluentWait 中抛出 {@link NoSuchElementException} 。
 */
public interface ExpectedConditionExtended {

    Logger LOGGER = LogManager.getLogger();

    /**
     * 检查WebElement是否可见并启用
     *
     * @param element : WebElement
     *
     * @return 可点击返回元素 ，否则返回null
     */
    static ExpectedCondition<WebElement> elementToBeClickable(final WebElement element) {
        return new ExpectedCondition<WebElement>() {
            ExpectedCondition<WebElement> visibilityOfElement = ExpectedConditions.visibilityOf(element);

            @Override
            public WebElement apply(WebDriver driver) {
                WebElement element = visibilityOfElement.apply(driver);
                try {
                    if (element != null && element.isDisplayed() && element.isEnabled()) {
                        return element;
                    } else {
                        return null;
                    }
                } catch (StaleElementReferenceException | NoSuchElementException e) {
                    LOGGER.warn(e);
                    return null;
                }
            }

            @Override
            public String toString() {
                return "Element 没有启用: " + element.toString();
            }
        };
    }

    /**
     * 检查 WebElement 列表是否都可点击
     *
     * @param elements WebElements 列表
     *
     * @return 全部可点击返回true expected condition
     */
    static ExpectedCondition<Boolean> elementToBeClickable(final List<WebElement> elements) {
        final List<Boolean> statusList = Lists.newArrayList();
        return new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                if (elements.isEmpty()) {
                    return false;
                }
                statusList.clear();
                for (WebElement w : elements) {
                    try {
                        if (w != null && w.isEnabled() && w.isDisplayed()) {
                            statusList.add(true);
                        } else {
                            return false;
                        }
                    } catch (StaleElementReferenceException e) {
                        LOGGER.warn(e);
                        return false;
                    }
                }
                LOGGER.debug("element 总数量:" + elements.size() + "  可点击个数:" + statusList.size());
                return statusList.size() == elements.size();
            }

            @Override
            public String toString() {
                return "存在一个以上元素无法点击:";
            }
        };
    }

    /**
     * 检查元素被禁用
     *
     * @param element : WebElement
     *
     * @return true 表示元素被禁止
     */
    static ExpectedCondition<Boolean> elementToBeDisabled(final WebElement element) {
        return new ExpectedCondition<Boolean>() {
            ExpectedCondition<WebElement> visibilityOfElement = ExpectedConditions.visibilityOf(element);

            @Override
            public Boolean apply(WebDriver driver) {
                boolean isDisabled = false;
                WebElement element = visibilityOfElement.apply(driver);
                try {
                    if (element != null && !(element.isEnabled())) {
                        isDisabled = true;
                    }
                    return isDisabled;
                } catch (StaleElementReferenceException e) {
                    LOGGER.warn("未找到元素: " + (element != null ?
                            element.toString() :
                            null), e);
                    return isDisabled;
                }
            }

            @Override
            public String toString() {
                return "元素是可见的: " + element;
            }
        };
    }

    /**
     * 检查 WebElement 列表是否都可见
     *
     * @param elements WebElements列表
     *
     * @return 全部可见返回true expected condition
     */
    static ExpectedCondition<Boolean> elementToBeDisplayed(final List<WebElement> elements) {
        final List<Boolean> statusList = Lists.newArrayList();
        return new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                for (WebElement w : elements) {
                    try {
                        if (w != null && w.isDisplayed()) {
                            statusList.add(true);
                        } else {
                            return false;
                        }
                    } catch (StaleElementReferenceException e) {
                        LOGGER.warn(e);
                        return false;
                    }
                }
                return statusList.size() == elements.size();
            }

            @Override
            public String toString() {
                return "存在一个以上元素不可见:";
            }
        };
    }

    /**
     * 检查WebElements 列表中元素是否可点击，如果存在任何WebElement不可点击将返回false
     *
     * @param elements WebElements 列表
     *
     * @return 存在任何WebElement不可点击将返回false expected condition
     */
    static ExpectedCondition<Boolean> elementsToBeClickable(final WebElement... elements) {
        final List<Boolean> statusList = Lists.newArrayList();

        return new ExpectedCondition<Boolean>() {
            final StringBuilder sb = new StringBuilder();

            @Override
            public Boolean apply(WebDriver driver) {
                for (WebElement w : elements) {
                    try {
                        if (w.isDisplayed() && w.isEnabled()) {
                            statusList.add(true);
                        } else {
                            statusList.add(false);
                        }
                    } catch (StaleElementReferenceException e) {
                        LOGGER.warn(e);
                        statusList.add(false);
                    }
                }
                if (statusList.contains(false)) {
                    statusList.clear();
                    return false;
                }
                return true;
            }

            @Override
            public String toString() {
                return "元素可以点击: " + sb;
            }
        };
    }

    /**
     * 检查一个元素是否可见或DOM中是否存在
     *
     * @param locator 要检查的元素
     *
     * @return true 表示元素可见或DOM中存在
     */
    static ExpectedCondition<Boolean> invisibilityOfElementLocated(final By locator) {
        return invisibilityOfElementLocated(locator, null);
    }

    /**
     * 检查一个元素是否可见或DOM中是否存在
     *
     * @param locator       要检查的元素
     * @param parentElement the parent element
     *
     * @return true 表示元素可见或DOM中存在
     */
    static ExpectedCondition<Boolean> invisibilityOfElementLocated(final By locator,
                                                                   final WebElement parentElement) {
        return new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                try {
                    if (parentElement == null) {
                        return !driver.findElements(locator).isEmpty();
                    } else
                        return !parentElement.findElements(locator).isEmpty();
                } catch (Exception e) {
                    LOGGER.warn(e);
                    return false;
                }
            }

            @Override
            public String toString() {
                return "元素在WebElement:" + parentElement.toString() + "可见: By " + locator.toString();
            }
        };
    }

    /**
     * 检查一个元素是否可见或DOM中是否存在
     *
     * @param webelement 要检查的元素
     *
     * @return true 表示元素可见或DOM中存在
     */
    static ExpectedCondition<Boolean> invisibilityOfElementLocated(final WebElement webelement) {
        return new ExpectedCondition<Boolean>() {

            @Override
            public Boolean apply(WebDriver driver) {
                try {
                    return webelement.isDisplayed();
                } catch (Exception e) {
                    LOGGER.warn(e);
                    return false;
                }
            }

            @Override
            public String toString() {
                return "元素可见: " + webelement.toString();
            }
        };
    }

    /**
     * 检查页面是否加载完毕
     *
     * @return the expected condition
     */
    static ExpectedCondition<Boolean> pageLoad() {
        return new ExpectedCondition<Boolean>() {
            private Object value = null;

            @Override
            public Boolean apply(WebDriver driver) {
                try {
                    value = ((JavascriptExecutor) driver).executeScript("return document.readyState");
                } catch (UnhandledAlertException | NoAlertPresentException alert) {
                    LOGGER.warn(alert);
                    return true;
                } catch (Exception e) {
                    LOGGER.warn(e);
                }
                if (value == null) {
                    return false;
                } else if (value instanceof String) {
                    return StringUtils.isNotBlank((String) value) && ((String) value).contains("complete");
                }

                return false;
            }

            @Override
            public String toString() {
                return "页面加载未完成" + value;
            }
        };
    }
}