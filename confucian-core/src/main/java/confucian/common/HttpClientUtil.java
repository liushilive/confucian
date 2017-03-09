package confucian.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.sync.CloseableHttpClient;
import org.apache.hc.client5.http.impl.sync.HttpClients;
import org.apache.hc.client5.http.methods.CloseableHttpResponse;
import org.apache.hc.client5.http.methods.HttpGet;
import org.apache.hc.client5.http.methods.HttpPost;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import confucian.exception.FrameworkException;

/**
 * httpclient 公共类
 */
public class HttpClientUtil {
    public static final String CHARSET = "UTF-8";
    private final static Logger LOGGER = LogManager.getLogger();
    private static CloseableHttpClient httpClient = null;
    private static PoolingHttpClientConnectionManager cm = null;

    private HttpClientUtil() {
    }

    /**
     * HTTP Get 获取内容
     *
     * @param url 请求的url地址 ?之前的地址
     *
     * @return 页面内容
     */
    public static String doGet(String url) {
        return doGet(url, Maps.newConcurrentMap());
    }

    /**
     * HTTP Get 获取内容
     *
     * @param url    请求的url地址 ?之前的地址
     * @param params 请求的参数
     *
     * @return 页面内容
     */
    public static String doGet(String url, Map<String, String> params) {
        if (StringUtils.isBlank(url)) {
            LOGGER.warn("URL为空");
            return "";
        }

        if (params != null && !params.isEmpty()) {
            List<NameValuePair> pairs = Lists.newArrayList();
            for (String key : params.keySet())
                pairs.add(new BasicNameValuePair(key, params.get(key)));
            try {
                url += "?" + EntityUtils.toString(new UrlEncodedFormEntity(pairs, Charset.forName(CHARSET)));
            } catch (IOException | ParseException e) {
                throw new FrameworkException(e);
            }
        }
        HttpGet httpGet = new HttpGet(url);
        try {
            HttpContext context = new BasicHttpContext();
            CloseableHttpClient httpClient = HttpClientUtil.getInstance();
            CloseableHttpResponse response = httpClient.execute(httpGet, context);
            int statusCode = response.getStatusLine().getStatusCode();
            LOGGER.debug("Get url：" + httpGet.getURI() + " " + statusCode);
            if (statusCode != 200) {
                httpGet.abort();
                LOGGER.warn("HttpClient,错误状态码：" + statusCode);
                // throw new RuntimeException("HttpClient,错误状态码：" + statusCode);
            }
            HttpEntity entity = response.getEntity();
            if (entity != null)
                return EntityUtils.toString(entity, CHARSET);
        } catch (Exception e) {
            throw new FrameworkException("GET " + httpGet.getURI(), e);
        }
        return null;
    }

    /**
     * HTTP Post 获取内容
     *
     * @param url 请求的url地址 ?之前的地址
     *
     * @return 页面内容
     */
    public static String doPost(String url) {
        return doPost(url, Maps.newConcurrentMap());
    }

    /**
     * HTTP Post 获取内容
     *
     * @param url    请求的url地址 ?之前的地址
     * @param params 请求的参数
     *
     * @return 页面内容
     */
    public static String doPost(String url, Map<String, String> params) {
        if (StringUtils.isBlank(url)) {
            LOGGER.warn("URL为空");
            return "";
        }

        List<NameValuePair> ps = Lists.newArrayList();
        for (String pKey : params.keySet()) {
            ps.add(new BasicNameValuePair(pKey, params.get(pKey)));
        }
        HttpPost httpPost = new HttpPost(url);
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(ps, Charset.forName(CHARSET)));
            CloseableHttpClient httpClient = HttpClientUtil.getInstance();
            CloseableHttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            LOGGER.debug("Post url：" + httpPost.getURI() + " " + statusCode);
            if (statusCode != 200) {
                httpPost.abort();
                LOGGER.warn("HttpClient,错误状态码：" + statusCode);
                // throw new RuntimeException("HttpClient,错误状态码：" + statusCode);
            }
            HttpEntity httpEntity = response.getEntity();
            return EntityUtils.toString(httpEntity, CHARSET);
        } catch (Exception e) {
            throw new FrameworkException("Post " + url, e);
        }
    }

    public static CloseableHttpClient getInstance() {
        if (httpClient == null) {
            if (cm == null) {
                cm = new PoolingHttpClientConnectionManager();
                cm.setMaxTotal(500);
            }
            httpClient = HttpClients.custom()
                    .setConnectionManager(cm)
                    .build();
        }
        return httpClient;
    }
}
