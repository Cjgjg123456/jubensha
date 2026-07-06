package org.example.jubensha.net.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {
    private static ConfigManager instance;
    private Properties properties;

    private ConfigManager() {
        properties = new Properties();
        try {
            java.io.File file = new java.io.File("config.properties");
            if (file.exists()) {
                java.io.FileInputStream fileInputStream = new java.io.FileInputStream(file);
                properties.load(fileInputStream);
                fileInputStream.close();
                System.out.println("从项目根目录加载配置文件: " + file.getAbsolutePath());
            } else {
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties");
                if (inputStream != null) {
                    properties.load(inputStream);
                    inputStream.close();
                    System.out.println("从 classpath 加载配置文件");
                } else {
                    System.err.println("配置文件 config.properties 未找到，使用默认值");
                    setDefaultProperties();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            setDefaultProperties();
        }
    }

    private void setDefaultProperties() {
        properties.setProperty("server.ip", "localhost");
        properties.setProperty("server.port", "8888");
        properties.setProperty("server.public-ip", "4b8c1007.r11.cpolar.top");
        properties.setProperty("server.public-port", "80");
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public int getIntProperty(String key) {
        try {
            return Integer.parseInt(properties.getProperty(key));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
}