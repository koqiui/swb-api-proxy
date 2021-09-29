package com.swb.tool.web;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.DispatcherServlet;

import com.swb.tool.cmn.SwbApiHelper;

public class AppInitializer implements WebApplicationInitializer {
	private final Log logger = LogFactory.getLog(this.getClass());

	public void onStartup(ServletContext servletContext) throws ServletException {
		String confDir = servletContext.getInitParameter("confDir");

		// 请求/响应编码过滤
		CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter("UTF-8", false);
		FilterRegistration.Dynamic filter = servletContext.addFilter("encodingFilter", encodingFilter);
		filter.addMappingForUrlPatterns(null, false, "/*");
		// root app
		XmlWebApplicationContext appCtx = new XmlWebApplicationContext();
		appCtx.setServletContext(servletContext);
		appCtx.setConfigLocation("file:///" + confDir + "/application.xml");
		appCtx.refresh();

		// 加载app调用配置信息
		Properties appConf = new Properties();
		String appConfPath = Paths.get(confDir, "app.properties").toString();
		try {
			appConf.load(new FileInputStream(appConfPath));

			SwbApiHelper.config(appConf);

			// 启动Token刷新
			SwbApiHelper.startTokenRefresher();
		} catch (IOException e) {
			logger.error("读取 " + appConfPath + " 失败！");
			logger.error(e);
			//
			System.exit(-1);
		}

		ContextLoaderListener loadListener = new ContextLoaderListener(appCtx);
		servletContext.addListener(loadListener);

		// mvc app
		XmlWebApplicationContext mvcCtx = new XmlWebApplicationContext();
		mvcCtx.setConfigLocation("file:///" + confDir + "/application-mvc.xml");

		DispatcherServlet dispServlet = new DispatcherServlet(mvcCtx);

		ServletRegistration.Dynamic servlet = servletContext.addServlet("springMvc", dispServlet);
		servlet.addMapping("/");
		servlet.setLoadOnStartup(1);
	}

}
