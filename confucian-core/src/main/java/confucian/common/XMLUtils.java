package confucian.common;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import confucian.exception.FrameworkException;

/**
 * XML公共操作方法
 */
public class XMLUtils {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * 创建文本节点
     *
     * @param document the document
     * @param element  the element
     * @param tagName  名称
     * @param data     值
     */
    public static void createTextNode(Document document, Element element, String tagName, String data) {
        if (StringUtils.isNotEmpty(tagName) && StringUtils.isNotEmpty(data)) {
            Element el = document.createElement(tagName);
            el.appendChild(document.createTextNode(data));
            element.appendChild(el);
        } else
            throw new FrameworkException("传入参数值存在空值：参数tagName：" + tagName + "  参数data：" + data);
    }

    /**
     * 创建节点
     *
     * @param document the document
     * @param tagName  the tag name
     * @return element
     */
    public static Element createElement(Document document, String tagName, String name, String value) {
        if (StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(value)) {
            Element element = document.createElement(tagName);
            element.setAttribute(name, value);
            return element;
        } else
            throw new FrameworkException("传入参数值存在空值：参数tagName：" + tagName + "  参数name：" + name + " 参数value：" + value);
    }

    /**
     * Create xml.
     *
     * @param filePath the file path
     * @param document the document
     */
    public static void createXml(String filePath, Document document) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            DOMSource source = new DOMSource(document);
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            File fd = new File(filePath);
            File fd2 = new File(fd.getParent());
            boolean b = fd2.exists();
            if (!b) {
                b = fd2.mkdirs();
            }
            if (b) {
                FileOutputStream outputStream = new FileOutputStream(filePath);
                PrintWriter pw = new PrintWriter(outputStream);
                StreamResult result = new StreamResult(pw);
                transformer.transform(source, result);
                LOGGER.info("保存XML文件成功:" + filePath);
            }
        } catch (Exception e) {
            throw new FrameworkException("保存XML文件失败:", e);
        }
    }

    /**
     * 创建XML根
     *
     * @param fileName 文件全路径
     * @param coverTag 是否覆盖
     * @return the document
     */
    public static Document createConfXML(String fileName, boolean coverTag) {
        Document document = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            try {
                document = builder.parse(fileName);
                LOGGER.debug("读取文件:" + fileName);
            } catch (FileNotFoundException | SAXParseException ex) {
                coverTag = true;
                LOGGER.warn("读取文件失败:", ex.toString());
            }
            if (coverTag) {
                LOGGER.info("XML元素根创建");
                document = builder.newDocument();
                Element confElement = document.createElement("conf");
                document.appendChild(confElement);
                LOGGER.info("XML元素根创建完毕");
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOGGER.error("XML根元素创建失败:", e);
        }
        return document;
    }
}
