package com.googlecode.mvnsese.exec;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.HttpWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebResponseData;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class DefaultWebDriverProfileFactory implements WebDriverProfileFactory {

    public static final String PROFILE = "default";
    public static final Logger log = Logger.getLogger(DefaultWebDriverProfileFactory.class.getName());

    public String profileName() {
        return PROFILE;
    }

    public WebDriver buildWebDriver() {
        /*Currently only Firefox 3 profile supports the javascript
        document.implementation.hasFeature("XPath","3.0")
        function required by the selenium getText.js file.
         */
        HtmlUnitDriver driver = new HtmlUnitDriver(BrowserVersion.FIREFOX_3) {

            @Override
            protected WebClient modifyWebClient(WebClient client) {
                String[] whiteList = new String[0];
                String[] blackList = new String[0];
                log.fine("Attempting to load whitelist/blacklist");
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("default-webdriver.properties");
                if (is != null) {
                    Properties p = new Properties();
                    try {
                        p.load(is);
                        String wl = p.getProperty("whitelist");
                        if (wl != null && wl.length() > 0) {
                            whiteList = wl.split(",");
                            log.fine(String.format("Loaded whitelisted domains %s", wl));
                        }
                        String bl = p.getProperty("blacklist");
                        if (bl != null && bl.length() > 0) {
                            blackList = bl.split(",");
                            log.fine(String.format("Loaded blacklisted domains %s", bl));
                        }
                    } catch (IOException ex) {
                        log.log(Level.SEVERE, "error loading whitelist/blacklist", ex);
                    }
                }
                try {
                    client.setWebConnection(new WhiteBlackListWebConnection(whiteList, blackList, client));
                    client.setUseInsecureSSL(true);
                    client.setJavaScriptEnabled(true);
                } catch (GeneralSecurityException ex) {
                    ex.printStackTrace();
                }
                return client;
            }
        };


        Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
        Logger.getLogger("org.apache.http.client.protocol.ResponseProcessCookies").setLevel(Level.OFF);

        driver.setJavascriptEnabled(true);
        return driver;
    }

    public static class WhiteBlackListWebConnection extends HttpWebConnection {

        String[] whitelist;
        String[] blacklist;

        public WhiteBlackListWebConnection(String[] whitelist, String[] blacklist, WebClient client) {
            super(client);
            this.whitelist = whitelist;
            this.blacklist = blacklist;
        }

        @Override
        public WebResponse getResponse(WebRequest request) throws IOException {

            boolean allowed = true;
            String host = request.getUrl().getHost();
            if (whitelist.length > 0) {
                allowed = false;
                for (String d : whitelist) {
                    if (host.endsWith(d)) {
                        allowed = true;
                    }
                }
            } else if (blacklist.length > 0) {
                for (String d : blacklist) {
                    if (host.endsWith(d)) {
                        allowed = false;
                    }
                }
            }
            if (allowed) {
                log.fine(String.format("Request for %s was allowed\n", request.getUrl()));
                return super.getResponse(request);
            } else {
                log.fine(String.format("Request for %s was rejected\n", request.getUrl()));
                WebResponseData data = new WebResponseData(new byte[0], 401, "Default WebDriver blacklisted", new ArrayList<NameValuePair>());
                WebResponse response = new WebResponse(data, request, 0);
                return response;
            }
        }
    }
}
