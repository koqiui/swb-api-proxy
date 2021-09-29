package com.swb.tool.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletException;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.Server;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.web.SpringServletContainerInitializer;

import com.swb.common.helper.FileHelper;
import com.swb.tool.cmn.SwbApiHelper;

/**
 * 启动主类
 * 
 * @author koqiui
 * @date 2021年9月26日 下午10:13:24
 * @email koqiui@163.com
 */
public class App {
	private final static Log logger = LogFactory.getLog(App.class);

	//
	public static void main(String[] args) throws LifecycleException, ServletException {
		// 关闭时的清除处理逻辑
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				SwbApiHelper.stopTokenRefresher();
			}
		});

		// 确定Home目录
		String binDir = FileHelper.getClassJarFileDir(App.class);
		boolean isPacked = binDir != null;

		String homeDir = null;
		if (isPacked) {// 已经打包了
			homeDir = new File(binDir).getParent();
		} else {// 开发状态
			String projDir = new File("").getAbsolutePath();
			homeDir = Paths.get(projDir, "dist").toString();
		}

		File tmpFile = null;

		// 加载配置
		String confDir = Paths.get(homeDir, "conf").toString();
		// 1 - base.properties
		Properties baseConfig = new Properties();
		String basePropPath = Paths.get(confDir, "base.properties").toString();
		try {
			baseConfig.load(new FileInputStream(basePropPath));
		} catch (IOException e) {
			System.err.println("读取 " + basePropPath + " 失败！");
			System.err.println(e);
			//
			System.exit(-1);
		}

		String host = baseConfig.getProperty("host", "localhost");
		String port = baseConfig.getProperty("port", "9900");
		String shutdownPort = baseConfig.getProperty("shutdown.port", "9901");
		String shutdownCmd = baseConfig.getProperty("shutdown.cmd", "SHUTDOWN");
		String logsDir = baseConfig.getProperty("logsDir", "logs");

		//
		tmpFile = new File(logsDir);
		if (!tmpFile.isAbsolute()) {
			tmpFile = new File(homeDir, logsDir);
		}
		logsDir = tmpFile.getAbsolutePath();
		System.out.println("日志目录：" + logsDir);

		Properties log4jConf = new Properties();
		String log4jPath = Paths.get(confDir, "log4j.properties").toString();
		try {
			log4jConf.load(new FileInputStream(log4jPath));
			//
			String filePath = log4jConf.getProperty("log4j.appender.FileAppender.File", "${logsDir}/logview-log.txt");
			filePath = filePath.replace("${logsDir}", logsDir);
			log4jConf.put("log4j.appender.FileAppender.File", filePath);
			//
			PropertyConfigurator.configure(log4jConf);
		} catch (IOException e) {
			System.err.println("读取 " + log4jPath + " 失败！");
			System.err.println(e);
			//
			System.exit(-1);
		}

		// -----------------------------------------------------------
		App.logger.debug("正配置 tomcat ...");

		Tomcat tomcat = new Tomcat();

		String wwwDir = Paths.get(homeDir, "www").toString();

		App.logger.debug("wwwDir " + wwwDir);

		tomcat.setBaseDir(wwwDir);

		tomcat.setSilent(false);

		tomcat.setHostname(host);

		tomcat.setPort(Integer.valueOf(port));

		App.logger.debug(host + ":" + port);

		// 设置uri自动编码（很重要！！！）
		Connector connector = tomcat.getConnector();
		connector.setURIEncoding("UTF-8");

		String webRoot = Paths.get(wwwDir, "root").toString();
		App.logger.debug("webRoot " + webRoot);

		StandardContext stdCtx = (StandardContext) tomcat.addWebapp("", webRoot);
		// 设置要传递的参数：
		stdCtx.addParameter("confDir", confDir);// 总的配置目录

		if (!isPacked) {// 注册启动类（注意：开发模式下 tomcat 7 不支持自动查找，需要主动处理）
			Set<Class<?>> initClasses = new HashSet<>();
			initClasses.add(AppInitializer.class);
			stdCtx.addServletContainerInitializer(new SpringServletContainerInitializer(), initClasses);
		}

		//
		Server server = tomcat.getServer();
		// 停闭配置
		server.setPort(Integer.valueOf(shutdownPort));
		server.setShutdown(shutdownCmd);

		App.logger.debug("shutdown:" + server.getShutdown());
		//
		tomcat.start();

		App.logger.debug("已启动 tomcat");

		tomcat.getServer().await();
	}

}
