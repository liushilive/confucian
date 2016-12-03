package confucian.data.xml;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import confucian.common.Utils;
import confucian.data.IProperty;
import confucian.data.PropertyMapping;

/**
 * 传递测试数据xml返回{@link IProperty}列表
 */
public class XmlApplicationData {

    private static final Map<String, List<IProperty>> dataBucket = Maps.newHashMap();
    private static final Logger LOGGER = LogManager.getLogger();
    private static XmlApplicationData instance = null;
    private DocumentBuilder builder = null;
    private Document document = null;
    private String envType = null;

    private XmlApplicationData() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            LOGGER.error(e);
        }
    }

    /**
     * 获取实例
     *
     * @return 实例 instance
     */
    public static XmlApplicationData getInstance() {
        if (null == instance) {
            synchronized (XmlApplicationData.class) {
                if (null == instance) {
                    instance = new XmlApplicationData();
                }
            }
        }
        return instance;
    }

    /**
     * 获取应用数据列表
     *
     * @param xmlName     xml名称
     * @param environment 环境
     * @return the list
     */
    public List<IProperty> getAppData(String xmlName, String environment) {
        envType = environment;
        return getAppData(xmlName);
    }

    /**
     * 在自动化中使用键值对
     *
     * @param xmlName xml名称
     * @return 应用数据 app data
     */
    public List<IProperty> getAppData(String xmlName) {
        if (dataBucket.containsKey(xmlName)) {
            return dataBucket.get(xmlName);
        } else {
            List<IProperty> dataProperty = Lists.newArrayList();
            HashMap<String, String> keyValue;
            for (Element dataObject : getDataObjects(xmlName)) {
                String typeKeyNames = null;
                String pageKeyNames = null;
                keyValue = Maps.newHashMap();
                for (int i = 0; i < dataObject.getChildNodes().getLength(); i++) {
                    Node pageObject = dataObject.getChildNodes().item(i);
                    if (pageObject instanceof Element) {
                        Element pageElement = (Element) pageObject;
                        String appendText = pageElement.getAttribute("name");
                        pageKeyNames = pageKeyNames == null ?
                                appendText :
                                pageKeyNames + "->" + appendText;
                        // page
                        if (pageElement.getTagName().equalsIgnoreCase("page")) {
                            // 所有的bs_key页的值对
                            NodeList pageData = pageElement.getChildNodes();
                            for (int j = 0; j < pageData.getLength(); j++) {
                                Node key = pageData.item(j);
                                if (key instanceof Element) {
                                    Element keyElement = (Element) key;
                                    keyValue.put(appendText + "->" + keyElement.getNodeName(),
                                            keyElement.getTextContent());
                                }
                            }
                        } else if (pageElement.getTagName().equalsIgnoreCase("type")) {
                            String nodesNames = null;
                            String nodeNames = null;
                            // type
                            String typeKeyName = pageElement.getAttribute("name");
                            NodeList typeData = pageElement.getChildNodes();
                            typeKeyNames = typeKeyNames == null ?
                                    typeKeyName :
                                    typeKeyNames + "->" + typeKeyName;

                            for (int y = 0; y < typeData.getLength(); y++) {
                                Node typeKey = typeData.item(y);
                                if (typeKey instanceof Element) {
                                    if (((Element) typeKey).getTagName().equalsIgnoreCase("nodes")) {
                                        // 所有的bs_key页的值对
                                        String nodesName = ((Element) typeKey).getAttribute("name");
                                        nodesNames = nodesNames == null ?
                                                nodesName :
                                                nodesNames + "->" + nodesName;
                                        NodeList pageData = typeKey.getChildNodes();
                                        // nodes
                                        for (int j = 0; j < pageData.getLength(); j++) {
                                            // node
                                            Node key = pageData.item(j);
                                            if (key instanceof Element) {
                                                String keyName = ((Element) key).getAttribute("name");

                                                if (keyValue.containsKey(typeKeyName + "->" + nodesName + "->node")) {
                                                    keyValue.put(typeKeyName + "->" + nodesName + "->node",
                                                            keyValue.get(typeKeyName + "->" + nodesName + "->node") +
                                                                    "->" + keyName);
                                                } else {
                                                    keyValue.put(typeKeyName + "->" + nodesName + "->node", keyName);
                                                }

                                                NodeList keyNodeList = ((Element) key.getChildNodes()).getChildNodes();
                                                // 元素
                                                for (int x = 0; x < keyNodeList.getLength(); x++) {
                                                    Node keyNode = keyNodeList.item(x);
                                                    if (keyNode.getNodeType() == Node.ELEMENT_NODE) {

                                                        keyValue.put(
                                                                typeKeyName + "->" + nodesName + "->" + keyName + "->" +
                                                                        keyNode.getNodeName(),
                                                                keyNode.getTextContent());
                                                    }
                                                }
                                            }
                                        }
                                    } else if (typeKey.getNodeName().equalsIgnoreCase("node")) {
                                        String nodeName = ((Element) typeKey).getAttribute("name");
                                        nodeNames = nodeNames == null ?
                                                nodeName :
                                                nodeNames + "->" + nodeName;
                                        NodeList pageData = typeKey.getChildNodes();

                                        for (int j = 0; j < pageData.getLength(); j++) {
                                            Node key = pageData.item(j);
                                            if (key.getNodeType() == Node.ELEMENT_NODE) {
                                                keyValue.put(nodeName + "->" + key.getNodeName(), key.getTextContent());
                                            }
                                        }
                                    }
                                }
                            }
                            if (nodesNames != null) {
                                keyValue.put(typeKeyName + "->nodes", nodesNames);
                            }
                            if (nodeNames != null)
                                keyValue.put(typeKeyName + "->node", nodeNames);
                        }
                    }
                }
                if (StringUtils.isNotEmpty(typeKeyNames))
                    keyValue.put("type", typeKeyNames);
                keyValue.put("page", pageKeyNames);
                dataProperty.add(new PropertyMapping(keyValue));
            }
            dataBucket.put(xmlName, dataProperty);
            LOGGER.debug("读取" + xmlName + "文件完毕");
            return dataProperty;
        }
    }

    /**
     * 返回所有环境数据对象的列表
     *
     * @param xmlName xmlName
     * @return List<Element>
     */
    private List<Element> getDataObjects(String xmlName) {
        try {
            String resources = Utils.getResources(xmlName);
            if (resources != null) document = builder.parse(new FileInputStream(resources));
        } catch (SAXException | IOException e) {
            LOGGER.error(e);
        }
        List<Element> dataList = Lists.newArrayList();
        Element rootElement = document.getDocumentElement();
        NodeList nodes = rootElement.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);

            if (node instanceof Element) {
                Element data = (Element) node;
                if (envType != null) {
                    if (data.getAttribute("environment").equals(envType)) {
                        dataList.add(data);
                    }
                } else {
                    dataList.add(data);
                }
            }
        }

        if (dataList.isEmpty() && envType != null) {
            LOGGER.warn("没有这样的环境的名称:" + envType + " 在xml: " + xmlName + " 中");
        }
        return dataList;
    }


}
