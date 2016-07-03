import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;

/**
 * Created by watergate7 on 2016/4/6.
 */
public class SimpleCrawler implements PageProcessor {
    // 部分一：抓取网站的相关配置，包括编码、抓取间隔、重试次数等
    private Site site = Site.me().setRetryTimes(3).setSleepTime(100);

    @Override
    // process是定制爬虫逻辑的核心接口，在这里编写抽取逻辑
    public void process(Page page) {
        // 部分二：定义如何抽取页面信息，并保存下来
        Html html=page.getHtml();
        page.putField("category",html.$("body > div > div.main > div.mod_2b > div > div.col_b > div > div.bdbox.contD > div.mod_c > div.bdbox > p:nth-child(2)","text").toString());
        page.putField("name", html.$("body > div > div.main > div.mod_2b > div > div.col_b > div > div.bdbox.contD > div.th > span", "text").regex("(.*)的热量和营养价值").toString());
        page.putField("calory", html.$("body > div > div.main > div.mod_2b > div > div.col_a > div:nth-child(2) > div.bdbox > ul > li:nth-child(1) > em","text").toString());
        page.putField("CHO", html.$("body > div > div.main > div.mod_2b > div > div.col_a > div:nth-child(2) > div.bdbox > ul > li:nth-child(2) > em","text").toString());
        page.putField("fat", html.$("body > div > div.main > div.mod_2b > div > div.col_a > div:nth-child(2) > div.bdbox > ul > li:nth-child(3) > em","text").toString());
        page.putField("protein", html.$("body > div > div.main > div.mod_2b > div > div.col_a > div:nth-child(2) > div.bdbox > ul > li:nth-child(4) > em","text").toString());
        if (page.getResultItems().get("calory") == null) {
            //skip this page
            page.setSkip(true);
        }

        // 部分三：从页面发现后续的url地址来抓取
        page.addTargetRequests(html.links().regex("(http://tools\\.2345\\.com/reliang/list[0-9]+_[0-9]+\\.htm)").all());
        page.addTargetRequests(html.links().regex("(http://tools\\.2345\\.com/reliang/list[0-9]+\\.htm)").all());
        page.addTargetRequests(html.links().regex("(http://tools\\.2345\\.com/reliang/[0-9]+\\.htm)").all());
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        SimpleMysqlPipeline pipeline=new SimpleMysqlPipeline("jdbc:mysql://127.0.0.1:3306", "fatloss", "root", "watergate7", 200);

        Spider.create(new SimpleCrawler())
                //从"https://github.com/code4craft"开始抓
                .addUrl("http://tools.2345.com/reliang/").addPipeline(pipeline)
                //开启5个线程抓取
                .thread(15)
                        //启动爬虫
                .run();

        pipeline.executeLeftBatch();
    }
}
