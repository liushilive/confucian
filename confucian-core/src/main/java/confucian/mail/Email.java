package confucian.mail;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Reporter;

import javax.mail.*;
import javax.mail.Message.RecipientType;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 电子邮件
 */
public class Email implements IEmail {
    private static final int maxCountEMailCheck = 10;
    private static final Logger LOGGER = LogManager.getLogger();
    final private String host;
    final private String password;
    final private String port;
    final private String userName;
    private Folder folder;
    private String folderName;
    private Properties props = System.getProperties();
    private boolean sslEnabled;

    /**
     * 邮件对象
     *
     * @param builder {@link Builder}
     */
    private Email(Builder builder) {
        MailProtocol protocol;

        this.host = builder.host;
        this.port = builder.port;
        this.userName = builder.userName;
        this.password = builder.password;
        this.sslEnabled = builder.sslEnabled;
        protocol = builder.protocol;
        this.folderName = "Inbox";
        connect(protocol);
    }

    /**
     * 设置连接协议
     *
     * @param protocol {@link MailProtocol}协议
     */
    private void connect(MailProtocol protocol) {
        Session session;
        switch (protocol) {
            case POP3:
                setPop3Config();
                break;
            case IMAP:
                setImapConfig();
                break;
            case SMTP:
                setSmtpConfig();
                break;
            default:
                setPop3Config();
                break;
        }

        session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password);
            }
        });
        connectToStore(protocol.toString().toLowerCase(), session);
    }

    /**
     * 设置Pop3
     */
    private void setPop3Config() {
        props.setProperty("mail.pop3.host", host);
        props.setProperty("mail.pop3.port", port);
        props.setProperty("mail.store.protocol", "pop3s");
        props.setProperty("mail.pop3.user", userName);
        if (sslEnabled) {
            props.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.setProperty("mail.pop3.socketFactory.fallback", "true");
        }
    }

    /**
     * 设置Imap
     */
    private void setImapConfig() {
        props.setProperty("mail.imap.host", host);
        props.setProperty("mail.imap.port", port);
        if (sslEnabled) {
            props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.setProperty("mail.imap.socketFactory.fallback", "false");
        }
    }

    /**
     * 设置SMTP
     */
    private void setSmtpConfig() {
        props.setProperty("mail.smtp.host", host);
        props.setProperty("mail.smtp.port", port);
        props.setProperty("mail.smtp.auth", "true");
        if (sslEnabled) {
            props.setProperty("mail.smtp.starttls.enable", "true");
        }
    }

    /**
     * 连接到存储
     *
     * @param protocol 协议
     * @param session  会话
     */
    private void connectToStore(String protocol, Session session) {
        Store store;

        try {
            store = session.getStore(protocol);
            store.connect(userName, password);
            folder = store.getFolder(folderName);
        } catch (MessagingException e) {
            LOGGER.error(e);
        }
    }

    /**
     * 从文件夹中删除邮件
     *
     * @param message 消息从文件夹中删除
     * @return boolean
     */
    public boolean deleteMessage(Message message) {
        int mailCountBefore = getMailCount();
        try {
            folder.open(Folder.READ_WRITE);
            message.setFlag(Flags.Flag.DELETED, true);
            folder.close(true);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return mailCountBefore > getMailCount();
    }

    /**
     * 返回通过邮件筛选器的列表 {@link FilterEmails}
     *
     * @param searchCat  enum
     * @param messages   消息筛选器列表
     * @param filterText :电子邮件的主题文本
     * @return 消息列表
     */
    public List<Message> filterEmailsBy(FilterEmails searchCat, List<Message> messages, String filterText) {
        try {
            return filterFromToSubject(searchCat, filterText, messages.toArray(new Message[messages.size()]));
        } catch (MessagingException e) {
            // TODO Auto-generated catch block
            LOGGER.error(e);
        }
        return null;
    }

    /**
     * 获得电子邮件的正文
     *
     * @param message 消息以获得邮件正文
     */
    public String getEmailBody(Message message) {
        Object content;
        StringBuilder messageBody = new StringBuilder();
        try {
            folder.open(Folder.READ_ONLY);
            content = message.getContent();
            if (content instanceof Multipart) {
                Multipart mp = (Multipart) content;
                for (int i = 0; i < mp.getCount(); i++) {
                    BodyPart bp = mp.getBodyPart(i);
                    if (Pattern.compile(Pattern.quote("text/html"), Pattern.CASE_INSENSITIVE)
                            .matcher(bp.getContentType()).find()) {
                        messageBody.append(bp.getContent());
                    } else {
                        messageBody.append(bp.getContent());
                    }
                }
            } else {
                messageBody.append(content);
            }
            folder.close(true);
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return messageBody.toString();
    }

    /**
     * 邮件筛选器列表返回通过{@link FilterEmails}元素0是最新的一个
     *
     * @param searchCat  enum
     * @param filterText 电子邮件的主题文本
     * @return 消息列表
     */
    public List<Message> getEmailsBy(FilterEmails searchCat, String filterText) {
        Stopwatch sw = Stopwatch.createStarted();
        sw.start();

        int inboxMessageCount = getMailCount();

        List<Message> returnMessages = Lists.newArrayList();
        try {
            folder.open(Folder.READ_ONLY);
            Message[] messages;
            LOGGER.info("文件夹中的邮件数: " + folder.getName() + " 有: " + inboxMessageCount);

            int end = folder.getMessageCount();
            int start = 1;

            if (inboxMessageCount >= 10) {
                start = end - maxCountEMailCheck + 1;
            }

            messages = folder.getMessages(start, end);

            returnMessages = filterFromToSubject(searchCat, filterText, messages);
            folder.close(true);
        } catch (MessagingException e) {
            LOGGER.error(e);
        }
        sw.stop();
        LOGGER.info("获取消息的时间是: " + sw.elapsed(TimeUnit.SECONDS));
        return returnMessages;
    }

    /**
     * 返回Html链接在电子邮件的正文文本
     *
     * @param message                        从HTML链接解析消息
     * @param textAfterWhichToFetchHtmlLinks 搜索HTML链接后的文本
     */
    public String getHTMLLinkAfterText(Message message, String textAfterWhichToFetchHtmlLinks) {

        String text = getEmailBody(message);
        String filteredText;
        String httpLink = null;
        if (text.contains(textAfterWhichToFetchHtmlLinks)) {
            filteredText = text.substring(text.indexOf(textAfterWhichToFetchHtmlLinks));
            httpLink = filteredText.substring(filteredText.indexOf("http")).split(" ")[0].split(">")[0].split("\"")[0];
        } else {
            Reporter.log(text);
        }
        return httpLink;
    }

    /**
     * 返回电子邮件消息的格式
     *
     * @param msg 消息中返回邮件格式
     */
    public String getMailFormat(Message msg) {
        String format = null;

        try {
            format = msg.getContentType();
        } catch (MessagingException e) {
            LOGGER.error(e);
        }
        return format;
    }

    /**
     * 如果没有设置，那么文件夹名称默认为收件箱，注意在POP3协议的文件夹将永远是收件箱
     *
     * @param folderName 设置文件夹名称
     */
    public void setFolder(String folderName) {
        this.folderName = folderName;
    }

    public boolean verifyPatternInEmail(Message message, String patterToMatch) {
        String messageBody = getEmailBody(message);
        return Pattern.matches(messageBody, patterToMatch);
    }

    /**
     * 筛选邮件 {@link FilterEmails}
     *
     * @param searchCat  {@link FilterEmails}
     * @param filterText 电子邮件地址或主题
     * @param messages   消息筛选器列表
     * @return 消息列表
     */
    private List<Message> filterFromToSubject(FilterEmails searchCat, String filterText, Message[] messages)
            throws MessagingException {
        switch (searchCat) {
            case FROM:
                return filterFrom(filterText, messages);
            case TO:
                return filterTo(filterText, messages);
            case SUBJECT:
                return filterSubject(filterText, messages);
            default:
                break;
        }
        return null;
    }

    /**
     * 发件人筛选邮件
     *
     * @param filterText 要过滤的电子邮件地址
     * @param messages   消息筛选器列表
     * @return 消息列表
     */
    private List<Message> filterFrom(String filterText, Message[] messages) throws MessagingException {
        List<Message> returnMessages = Lists.newArrayList();
        for (Message msg : messages) {
            if (msg.getFrom()[0].toString().contains(filterText)) {
                returnMessages.add(msg);
            }
        }
        return returnMessages;
    }

    /**
     * 收件人筛选邮件
     *
     * @param filterText 要过滤的电子邮件地址
     * @param messages   消息筛选器列表
     * @return 消息列表
     */
    private List<Message> filterTo(String filterText, Message[] messages) throws MessagingException {
        List<Message> returnMessages = Lists.newArrayList();
        for (Message msg : messages) {
            for (Address address : msg.getRecipients(RecipientType.TO)) {
                if (address.toString().contains(filterText)) {
                    returnMessages.add(msg);
                }
            }
        }
        return returnMessages;
    }

    /**
     * 主题筛选邮件
     *
     * @param filterText 过滤器
     * @param messages   消息筛选器列表
     * @return 消息列表
     */
    private List<Message> filterSubject(String filterText, Message[] messages) throws MessagingException {
        List<Message> returnMessages = Lists.newArrayList();
        for (Message msg : messages) {
            if (msg.getSubject().equalsIgnoreCase(filterText)) {
                returnMessages.add(msg);
            }
        }
        return returnMessages;
    }

    /**
     * 获取文件夹中的邮件计数
     *
     * @return 文件夹中的邮件计数
     */
    private int getMailCount() {
        int mailCount = -1;
        try {
            folder.open(Folder.READ_ONLY);
            mailCount = folder.getMessageCount();
            folder.close(true);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return mailCount;
    }

    /**
     * 生成电子邮件对象
     */
    public static class Builder {

        private String host;
        private String password;
        private String port;
        private MailProtocol protocol = MailProtocol.POP3;
        private boolean sslEnabled = true;
        private String userName;

        /**
         * 构建电子邮件
         *
         * @return email email
         */
        public Email build() {
            return new Email(this);
        }

        /**
         * 设置IP地址
         *
         * @param host 设置IP地址
         * @return 设置IP地址 host
         */
        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        /**
         * 设置密码
         *
         * @param password 设置密码
         * @return 设置密码 password
         */
        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        /**
         * 设置端口
         *
         * @param portNo 设置端口
         * @return 设置端口 port
         */
        public Builder setPort(String portNo) {
            this.port = portNo;
            return this;
        }

        /**
         * 设置协议
         *
         * @param protocol 协议
         * @return 协议 protocol
         */
        public Builder setProtocol(MailProtocol protocol) {
            this.protocol = protocol;
            return this;
        }

        /**
         * 设置ssl
         *
         * @param sslEnabled 启用ssl
         * @return ssl ssl
         */
        public Builder setSSL(boolean sslEnabled) {
            this.sslEnabled = sslEnabled;
            return this;
        }

        /**
         * 设置用户名
         *
         * @param userName 用户名
         * @return 用户名 user name
         */
        public Builder setUserName(String userName) {
            this.userName = userName;
            return this;
        }
    }
}