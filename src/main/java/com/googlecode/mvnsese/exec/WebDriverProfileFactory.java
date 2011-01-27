package com.googlecode.mvnsese.exec;

import org.openqa.selenium.WebDriver;

public interface WebDriverProfileFactory {

    String profileName();

    WebDriver buildWebDriver();

}
