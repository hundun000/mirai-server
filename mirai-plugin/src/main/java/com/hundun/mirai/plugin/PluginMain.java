package com.hundun.mirai.plugin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hundun.mirai.bot.CustomBeanFactory;
import com.hundun.mirai.bot.configuration.PrivateSettings;
import com.hundun.mirai.bot.configuration.PublicSettings;
import com.hundun.mirai.bot.service.IConsole;

import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.yamlkt.Yaml;
import net.mamoe.yamlkt.YamlMap;


/**
 * @author hundun
 * Created on 2021/06/02
 */
@Slf4j
public class PluginMain extends JavaPlugin {
    public static final PluginMain INSTANCE = new PluginMain(); // 可以像 Kotlin 一样静态初始化单例
    ObjectMapper objectMapper = new ObjectMapper();
    
    public PluginMain() {
        super(new JvmPluginDescriptionBuilder(
                "org.example.test-plugin", // name
                "1.0.0" // version
            )
            // .author("...")
            // .info("...")
            .build());
    }
    
    private void fakeOnLoad() {
        File settingsFile = resolveConfigFile("private-settings.json");
        String content = new String(readAll(settingsFile), StandardCharsets.UTF_8);
        //YamlMap settings = Yaml.Default.decodeYamlMapFromString(content);
        
        PrivateSettings privateSettings;
        try {
            privateSettings = objectMapper.readValue(content, PrivateSettings.class);
        } catch (JsonProcessingException e) {
            privateSettings = new PrivateSettings();
            e.printStackTrace();
        }
        
        log.info("PrivateSettings = {}", privateSettings);
        
        PublicSettings publicSettings = new PublicSettings();
        
        IConsole console = new ConsoleAdapter(privateSettings);
        
        CustomBeanFactory.init(privateSettings, publicSettings, console);
    }
    
    @Override
    public void onEnable() {
        fakeOnLoad();
        
        GlobalEventChannel.INSTANCE.registerListenerHost(CustomBeanFactory.getInstance().characterRouter);
    }
    
    /**
     * Read all content from input stream.<br>
     * 从数据流读取全部数据
     * 
     * @param i the input stream<br>
     *          数据流
     * @return return all read data <br>
     *         返回读入的所有数据
     * @throws IOException Signals that an I/O exception has occurred.<br>
     *                     发生IO错误
     */
    public static byte[] readAll(InputStream i) throws IOException {
        ByteArrayOutputStream ba = new ByteArrayOutputStream(16384);
        int nRead;
        byte[] data = new byte[4096];

        try {
            while ((nRead = i.read(data, 0, data.length)) != -1) {
                ba.write(data, 0, nRead);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw e;
        }

        return ba.toByteArray();
    }
    
    /**
     * Read all content from File.<br>
     * 从文件读取全部数据
     * 
     * @param i the file<br>
     *          文件
     * @return return all read data <br>
     *         返回读入的所有数据
     */
    public static byte[] readAll(File i) {
        try (FileInputStream fis = new FileInputStream(i)) {
            return readAll(fis);
        } catch (IOException ignored) {
            // TODO Auto-generated catch block
        }
        return new byte[0];
    }


}
