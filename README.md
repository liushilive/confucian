- Selenium WebDriver
- UI Automation
- TestNG
- Httpclient

特色
-
    * 并发测试
    * 数据驱动
    * 步级报告
    * 失败用例自动截图
    * 支持CI集成
    * 预期条件扩展
    * PageObjects 设计模式
    * 自动重试失败用例
    * 元素点击，输入值操作前智能等待元素出现，默认5S
    * API test
Data
-
     * CSV数据源,无浏览器配置 CSVData
     * XML数据源,无浏览器配置 XmlData
     * CSV数据源,带浏览器配置 CSVDataBrowser
     * XML数据源,带浏览器配置 XmlDataBrowser
     * 没有数据源,带浏览器配置 NoSourceBrowser
     * 无效的数据源 Invalid
执行
-
    mvn 命令执行 - target/surefire-reports/html/index.html
 报表
 -
    TestNg - test-output/html/index.html

1、PageObject
-
    1.1）Selenium3 引入了PageObject的思想，将每个页面的控件和操作封装成一个个Page类，然后在实现业务逻辑时，只需要调用对应的Page类即可。
    1.2）PageFactory类的引入是为了更好的支持页面设计模式，PageFactory提供了初始化页面元素的方法，只要页面更新一次，就会重新查找元素。
    1.3）src.main.java 存放Page类。
2、文件路径
-
    2.1）src.main.resources 存放资源文件与配置文件
        2.1.1）Framework.properties 设置框架基本属性
        2.1.2）log4j2.xml 设置日志属性
    2.2）src.test.java 存放测试用例
    2.3）src.test.resources 存放测试数据与测试配置文件
        2.3.1）TestData 文件夹，存放测试数据
        2.3.2）Browser.xml 设置浏览器配置
        2.3.3）Mapping.xml 设置测试类、测试环境与测试数据映射关系
        2.3.4）testsuite.xml 设置测试套件，此处为测试入口，遵循testNG规范
        
selenium grid
-
    1、独立启动
        java -jar selenium-server-standalone-2.53.1.jar
    2、HUB启动
        2.1）启动
            java -jar selenium-server-standalone-2.53.1.jar  -role hub
        2.2）参数
            -role：hub
                启动一个hub服务，等待webdriver客户端进行注册和请求。默认启动端口是4444，默认接收注册的地址为：http://localhost:4444/grid/register
            -hubConfig：[filename]
                一个符合selenium grid2规则的json格式的hub配置文件
            -throwOnCapabilityNotPresent: [true|false]
                默认为true，如果为true则hub只有在当前有测试代理注册的情况下才会接受测试请求；如果为false则如果当前没有代理注册也会接受请求保存到队列直到有代理注册为止。
            -capabilityMatcher:xxx
                一个实现了CapabilityMatcher接口的类，默认指向org.openqa.grid.internal.utils.DefaultCapabilityMatcher；该类用于实现grid在分布测试任务到对应代理时所使用的匹配规则，如果想要更精确的测试分配规则，那么就注册一个自己定义的匹配类。
            -prioritizer:XXXclass
                一个实现了Prioritizer接口的类。设置grid执行test任务的优先逻辑；默认为null，先来先执行。
            -port：xxx
                指定hub监听的端口
            -host：ip_or_host
                指定hub机的ip或者host值
            -newSessionWaitTimeout:XXX
                默认-1，即没有超时；指定一个新的测试session等待执行的间隔时间。即一个代理节点上前后2个测试中间的延时时间，单位为毫秒。
            -servlets: XXXserlet
                在hub上注册一个新的serlet，访问地址为/grid/admin/XXXserlet
            -browserTimeout:
                浏览器无响应的超时时间
            -grid1Yml:
                一个符合grid1规则的yml文件，仅适用于grid1
    3、node启动
        3.1）启动
            java -jar selenium-server-standalone-2.53.1.jar  -role hub
        3.2）参数
            -role: [node|wd|rc]
                为node值时表示注册的RC可以支持selenium1、selenium2两种方式的测试请求，推荐；
                为wd值时表示注册的RC仅支持selenium2的webdriver方式的测试请求，遗留；
                为rc值时表示注册的RC仅支持selenium1方式的测试请求，遗留。
            -hub：url_to_hub
                url_to_hub值为hub启动的注册地址，默认为ttp://ip_for_hub:4444/grid/register；具体的根据你启动hub时的参数所对应。
                该选项包含了-hubHost和-hubPort两个选项
            -hubHost：ip_or_host
                指定hub机的ip或者host值
            -host：ip_or_host
                同-hubHost选项
            -hubPort：xxx
                指定hub机的监听端口
            -port：XXX
                同-hubPort选项
            -registerCycle：xxx
                代理节点自动重新注册的周期，单位毫秒；适应于重启了hub时不需要重启所有的代理节点。
            -nodePolling:XXX
                hub检查代理节点的周期
            -unregisterIfStillDownAfter:XXX
                单位毫秒，设定代理节点在无响应多长时间后hub才会注销代理节点注册信息；默认1分钟
            -nodeTimeout：xxx
                客户端的无心跳超时时间
            -maxSession：xx
                一个代理节点可以同时启动的浏览器最大数量，即session数量
            -cleanupCycle:XXX
                代理节点检查超时的周期
            -nodeConfig: json_file
                一个符合selenium grid2规则的json格式的node配置文件
            -servlets: XXXserlet
                在node上注册一个新的serlet，访问地址为/grid/admin/XXXserlet
            -proxy: 代理类
                默认指向org.openqa.grid.selenium.proxy.DefaultRemoteProxy；用于代表节点的代理
            -browserTimeout:
                浏览器无响应的超时时间
            -browser:browserName=firefox,version=3.6,platform=LINUX
                设置代理节点的注册信息，这些信息同样可以在配置文件里设置;该参数可以使用多次用以同时注册多个浏览器信息。
日志等级
-
    在log4j2中，一共有五种log level，分别为TRACE, DEBUG,INFO, WARN, ERROR 以及FATAL。详细描述如下：
    FATAL：用在极端的情形中，即必须马上获得注意的情况。这个程度的错误通常需要触发运维工程师的寻呼机。
    ERROR：显示一个错误，或一个通用的错误情况，但还不至于会将系统挂起。这种程度的错误一般会触发邮件的发送，将消息发送到alert list中，运维人员可以在文档中记录这个bug并提交。
    WARN：不一定是一个bug，但是有人可能会想要知道这一情况。如果有人在读log文件，他们通常会希望读到系统出现的任何警告。
    INFO：用于基本的、高层次的诊断信息。在长时间运行的代码段开始运行及结束运行时应该产生消息，以便知道现在系统在干什么。但是这样的信息不宜太过频繁。
    DEBUG：用于协助低层次的调试。
    TRACE：用于展现程序执行的轨迹。
    