package confucian.data.xml;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import confucian.common.Utils;
import confucian.data.IMappingData;
import confucian.data.driverConfig.IBrowserConfig;
import confucian.data.driverConfig.PrepareDriverConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 返回{@link IBrowserConfig} 基于Xml文件的名称
 */
public class BrowserXmlParser {
    private static final Map<String, List<IBrowserConfig>> xmlBrowserMap = Maps.newHashMap();
    private static final Logger LOGGER = LogManager.getLogger();
    private static BrowserXmlParser instance = null;
    private DocumentBuilder builder = null;
    private Document document = null;

    private BrowserXmlParser() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            LOGGER.error(e);
        }
    }

    /**
     * 获取浏览器的xml解析器实例
     *
     * @return the browser xml parser
     */
    public static BrowserXmlParser getInstance() {
        if (instance == null) {
            synchronized (BrowserXmlParser.class) {
                if (instance == null) {
                    instance = new BrowserXmlParser();
                }
            }
        }
        return instance;
    }

    /**
     * 返回{@link IBrowserConfig}列表从{@link IMappingData}
     *
     * @param methodXml methodXml
     * @return 浏览器配置 browser config
     */
    public List<IBrowserConfig> getBrowserConfig(IMappingData methodXml) {
        return getBrowserConfig(methodXml.getClientEnvironment());
    }

    /**
     * 返回{@link IBrowserConfig}列表从smlList
     *
     * @param xmlList xmlList
     * @return 浏览器配置
     */
    private List<IBrowserConfig> getBrowserConfig(List<String> xmlList) {
        List<IBrowserConfig> returnList = Lists.newArrayList();
        for (String xml : xmlList) {
            if (xmlBrowserMap.containsKey(xml)) {
                returnList.addAll(xmlBrowserMap.get(xml));
            } else {
                //读取所有值准备列表
                List<IBrowserConfig> singleXmlBrowser = getBrowserForSingleXml(xml);
                xmlBrowserMap.put(xml, singleXmlBrowser);
                returnList.addAll(singleXmlBrowser);
            }
        }
        return returnList;
    }

    private List<IBrowserConfig> getBrowserForSingleXml(String xml) {
        return readXml(xml).stream()
                .map(keyValue -> new PrepareDriverConfig(keyValue).refineBrowserValues().checkForRules().get())
                .collect(Collectors.toList());
    }

    /**
     * 读取XML
     *
     * @param xmlN xml文件名
     * @return ArrayList
     */
    private List<HashMap<String, String>> readXml(String xmlN) {

        List<HashMap<String, String>> singleXMlList = Lists.newArrayList();
        try {
            document = builder.parse(Utils.getResources(xmlN));
        } catch (SAXException | IOException e) {
            LOGGER.error(e);
        }

        Element rootElement = document.getDocumentElement();
        NodeList nodes = rootElement.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element) {
                Element instanceBConf = (Element) node;
                singleXMlList.add(getKeyValue(instanceBConf));
            }

        }
        return singleXMlList;
    }

    /**
     * 返回键值对
     *
     * @param keyElement keyElement
     * @return HashMap
     */
    private HashMap<String, String> getKeyValue(Element keyElement) {

        HashMap<String, String> browserData = Maps.newHashMap();
        NamedNodeMap browser = keyElement.getAttributes();
        for (int i = 0; i < browser.getLength(); i++) {
            Node attr = browser.item(i);
            browserData.put(attr.getNodeName(), attr.getNodeValue());
        }
        return browserData;
    }

}
