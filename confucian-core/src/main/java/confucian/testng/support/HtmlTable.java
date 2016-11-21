package confucian.testng.support;

import confucian.common.Arithmetic;
import confucian.driver.Driver;
import org.testng.asserts.IAssert;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 创建或更新 HTML 报告
 */
public class HtmlTable {
    private LinkedHashMap<IAssert, String> assertMap;
    private StringBuilder sb = new StringBuilder();
    private boolean screenShotFlag = Driver.getBrowserConfig() != null && Driver.getBrowserConfig().isScreenShotFlag();
    private String testCaseName;

    /**
     * 实例化一个新的Html
     *
     * @param assertMap    断言映射
     * @param testCaseName 测试用例的名字
     */
    public HtmlTable(LinkedHashMap<IAssert, String> assertMap, String testCaseName) {
        this.assertMap = assertMap;
        this.testCaseName = testCaseName;
    }

    /**
     * 在Reporter.log()追加字符串数据表和所有行的数据
     *
     * @return table table
     */
    public String getTable() {
        sb.append("<style type=\"text/css\">" + "*{font-size:1em}" +
                "table{border-collapse: collapse;border: 1px solid black;color: #008000} " +
                "table td{ border: 1px solid black;}" + "table tr{ border: 1px solid black;}" +
                "table th{border: 1px solid black; font-weight: bold;color: #54B948;;background-color:#2F4F4F}" +
                "</style>");
        sb.append("<table>" + "<tr>").append(screenShotFlag ?
                ("<th colspan='6'>") :
                ("<th colspan='5'>")).append(testCaseName).append("</th>").append("</tr>").append("<tr>")
                .append("<th>ID</th>").append("<th>描述</th>").append("<th>状态</th>").append("<th>期望</th>")
                .append("<th>实际</th>");
        if (screenShotFlag) {
            sb.append("<th>屏幕截图</th>");
        }
        sb.append("</tr>");
        int i = 0;
        for (Map.Entry<IAssert, String> assertM : assertMap.entrySet()) {
            if (null == assertM)
                continue;
            boolean result = Objects.equals(assertM.getValue(), "true");
            String printResult = (result) ?
                    "通过" :
                    "失败";
            i++;
            sb.append("<tr>" + "<td>").append(i).append("</td>").append("<td>").append(assertM.getKey().getMessage())
                    .append("</td>");
            if (result) {
                sb.append("<td style=\"color: #000080\">").append(printResult).append("</td>");
            } else {
                sb.append("<td style=\"color: #FF0000\">").append(printResult).append("</td>");
            }
            sb.append("<td>").append(assertM.getKey().getExpected() instanceof Double ?
                    Arithmetic.mul((Double) assertM.getKey().getExpected(), 1) :
                    assertM.getKey().getExpected()).append("</td>").append("<td>")
                    .append(assertM.getKey().getActual() instanceof Double ?
                            Arithmetic.mul(1, (Double) assertM.getKey().getActual()) :
                            assertM.getKey().getActual()).append("</td>");
            if (!result && screenShotFlag) {
                sb.append("<td>" + "<a href='").append(assertM.getValue()).append("' target='_blank'>截图链接</a>")
                        .append("</td>");
            }
            sb.append("</tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }
}
