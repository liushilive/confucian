package confucian.mail;

import javax.mail.Message;
import java.util.List;

/**
 * 电子邮件接口
 */
interface IEmail {

    /**
     * 删除消息
     *
     * @param message 信息
     * @return boolean
     */
    boolean deleteMessage(Message message);

    /**
     * 按列表筛选电子邮件
     *
     * @param searchCat  {@link FilterEmails}
     * @param messages   消息
     * @param filterText 过滤文本
     * @return list
     */
    List<Message> filterEmailsBy(FilterEmails searchCat, List<Message> messages, String filterText);

    /**
     * 获取邮件正文
     *
     * @param message 信息
     * @return 电子邮件正文
     */
    String getEmailBody(Message message);

    /**
     * 获取电子邮件
     *
     * @param searchCat  {@link FilterEmails}
     * @param filterText 过滤文本
     * @return 电子邮件
     */
    List<Message> getEmailsBy(FilterEmails searchCat, String filterText);

    /**
     * 返回Html链接在电子邮件的正文文本
     *
     * @param message                        从HTML链接解析消息
     * @param textAfterWhichToFetchHtmlLinks 搜索HTML链接后的文本
     * @return Html链接在电子邮件的正文文本
     */
    String getHTMLLinkAfterText(Message message, String textAfterWhichToFetchHtmlLinks);

    /**
     * 获取邮件格式
     *
     * @param msg 消息
     * @return 邮件格式
     */
    String getMailFormat(Message msg);

    /**
     * 设置文件夹
     *
     * @param folderName 文件夹名称
     */
    void setFolder(String folderName);

    /**
     * 电子邮件匹配字符序列
     *
     * @param message       信息
     * @param patterToMatch 要匹配的字符序列
     * @return boolean
     */
    boolean verifyPatternInEmail(Message message, String patterToMatch);

}
