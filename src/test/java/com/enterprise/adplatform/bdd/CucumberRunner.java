package com.enterprise.adplatform.bdd;

import org.junit.platform.suite.api.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = "cucumber.plugin", value = "pretty, html:target/cucumber-reports/report.html, json:target/cucumber-reports/report.json, junit:target/cucumber-reports/junit.xml")
@ConfigurationParameter(key = "cucumber.glue", value = "com.enterprise.adplatform.bdd")
@ConfigurationParameter(key = "cucumber.filter.tags", value = "not @ignore")
public class CucumberRunner {
}
