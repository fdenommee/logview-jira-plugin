package com.pyxis.jira.logview.actions;

import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.archive.crawler.util.LogReader;

import com.atlassian.jira.logging.JiraHomeAppender;
import com.atlassian.jira.web.action.JiraWebActionSupport;

public class LogViewAction
		extends JiraWebActionSupport {

	private int n = 200;
	private String file;
	private String header;
	private String content;

	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}

	public String getFile() {
		return file;
	}

	public String getHeader() {
		return header;
	}

	public String getContent() {
		return content;
	}

	@Override
	protected String doExecute()
			throws Exception {

		file = null;
		content = null;

		JiraHomeAppender appender = findJiraHomeAppender();

		if (appender != null) {
			file = appender.getFile();

			String[] results = LogReader.tail(file, n);
			header = results[1];
			content = results[0];
			content = content.replaceAll("\n", "<br/>");
		}

		return SUCCESS;
	}

	private JiraHomeAppender findJiraHomeAppender() {

		Enumeration appenders = Logger.getRootLogger().getAllAppenders();

		while (appenders.hasMoreElements()) {
			Appender appender = (Appender)appenders.nextElement();

			if (appender instanceof JiraHomeAppender) {
				return (JiraHomeAppender)appender;
			}
		}

		return null;
	}
}
