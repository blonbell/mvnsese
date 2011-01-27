package com.googlecode.mvnsese.exec;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class DefaultWebDriverProfileFactory implements WebDriverProfileFactory {

    public static final String PROFILE = "HTMLUnitIE7";

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
                try {
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
}
