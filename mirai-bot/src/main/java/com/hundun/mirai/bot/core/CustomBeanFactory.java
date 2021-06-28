package com.hundun.mirai.bot.core;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import com.hundun.mirai.bot.core.character.Amiya;
import com.hundun.mirai.bot.core.character.Neko;
import com.hundun.mirai.bot.core.character.PrinzEugen;
import com.hundun.mirai.bot.core.character.ZacaMusume;
import com.hundun.mirai.bot.core.data.configuration.AppPrivateSettings;
import com.hundun.mirai.bot.core.data.configuration.AppPublicSettings;
import com.hundun.mirai.bot.core.function.AmiyaChatFunction;
import com.hundun.mirai.bot.core.function.GuideFunction;
import com.hundun.mirai.bot.core.function.JapaneseFunction;
import com.hundun.mirai.bot.core.function.KancolleWikiQuickSearchFunction;
import com.hundun.mirai.bot.core.function.MiraiCodeFunction;
import com.hundun.mirai.bot.core.function.PenguinFunction;
import com.hundun.mirai.bot.core.function.PrinzEugenChatFunction;
import com.hundun.mirai.bot.core.function.QuickSearchFunction;
import com.hundun.mirai.bot.core.function.QuizHandler;
import com.hundun.mirai.bot.core.function.RepeatConsumer;
import com.hundun.mirai.bot.core.function.WeiboFunction;
import com.hundun.mirai.bot.core.function.reminder.RemiderTaskRepository;
import com.hundun.mirai.bot.core.function.reminder.RemiderTaskRepositoryImplement;
import com.hundun.mirai.bot.core.function.reminder.ReminderFunction;
import com.hundun.mirai.bot.core.function.reminder.ReminderTask;
import com.hundun.mirai.bot.cp.FeignLogger;
import com.hundun.mirai.bot.cp.kcwiki.KancolleWikiService;
import com.hundun.mirai.bot.cp.kcwiki.feign.KcwikiApiFeignClient;
import com.hundun.mirai.bot.cp.penguin.PenguinService;
import com.hundun.mirai.bot.cp.penguin.db.ItemRepository;
import com.hundun.mirai.bot.cp.penguin.db.ItemRepositoryImplement;
import com.hundun.mirai.bot.cp.penguin.db.MatrixReportRepository;
import com.hundun.mirai.bot.cp.penguin.db.MatrixReportRepositoryImplement;
import com.hundun.mirai.bot.cp.penguin.db.StageInfoReportRepository;
import com.hundun.mirai.bot.cp.penguin.db.StageInfoReportRepositoryImplement;
import com.hundun.mirai.bot.cp.penguin.db.StageRepository;
import com.hundun.mirai.bot.cp.penguin.db.StageRepositoryImplement;
import com.hundun.mirai.bot.cp.penguin.domain.Item;
import com.hundun.mirai.bot.cp.penguin.domain.Stage;
import com.hundun.mirai.bot.cp.penguin.domain.report.MatrixReport;
import com.hundun.mirai.bot.cp.penguin.domain.report.StageInfoReport;
import com.hundun.mirai.bot.cp.penguin.feign.PenguinApiFeignClient;
import com.hundun.mirai.bot.cp.quiz.QuizService;
import com.hundun.mirai.bot.cp.weibo.WeiboService;
import com.hundun.mirai.bot.cp.weibo.db.WeiboCardCacheRepository;
import com.hundun.mirai.bot.cp.weibo.db.WeiboCardCacheRepositoryImplement;
import com.hundun.mirai.bot.cp.weibo.db.WeiboUserInfoCacheRepository;
import com.hundun.mirai.bot.cp.weibo.db.WeiboUserInfoCacheRepositoryImplement;
import com.hundun.mirai.bot.cp.weibo.domain.WeiboCardCache;
import com.hundun.mirai.bot.cp.weibo.domain.WeiboUserInfoCache;
import com.hundun.mirai.bot.cp.weibo.feign.WeiboApiFeignClient;
import com.hundun.mirai.bot.cp.weibo.feign.WeiboPictureApiFeignClient;
import com.hundun.mirai.bot.export.IConsole;
import com.hundun.mirai.bot.helper.db.CollectionSettings;
import com.hundun.mirai.bot.helper.file.FileOperationDelegate;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.zaca.stillstanding.api.StillstandingApiFeignClient;

import feign.Feign;
import feign.Logger;
import feign.Logger.Level;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 一步步把SpringBean替换掉
 * @author hundun
 * Created on 2021/05/25
 */
@Slf4j
public class CustomBeanFactory {

    
    private static CustomBeanFactory instance = new CustomBeanFactory();

    public static CustomBeanFactory getInstance() {
        return instance;
    }
    



    
    public void lateInit(AppPrivateSettings appPrivateSettings, AppPublicSettings appPublicSettings, IConsole console, ApplicationContext context) {
        this.appPrivateSettings = appPrivateSettings;
        this.appPublicSettings = appPublicSettings;
        this.console = console;
        
        
        context.getBeansOfType(IPostConsoleBind.class).values().forEach(item -> item.postConsoleBind());
        
        
    }

    
    private CustomBeanFactory() {
        
        
        
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        this.mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("mirai").withCodecRegistry(pojoCodecRegistry);
        // ----- weibo -----
        this.weiboUserInfoCacheRepository = new WeiboUserInfoCacheRepositoryImplement(new CollectionSettings<>(
                database, 
                "weiboUserInfoCache", 
                WeiboUserInfoCache.class, 
                "uid", 
                (item -> item.getUid()),
                ((item, id) -> item.setUid(id))
                ));
        this.weiboCardCacheRepository = new WeiboCardCacheRepositoryImplement(new CollectionSettings<>(
                database, 
                "weiboCardCache", 
                WeiboCardCache.class, 
                "itemid", 
                (item -> item.getItemid()),
                ((item, id) -> item.setItemid(id))
                ));
        // ----- pengin -----
        this.matrixReportRepository = new MatrixReportRepositoryImplement(new CollectionSettings<>(
                database, 
                "matrixReport", 
                MatrixReport.class, 
                "id", 
                (item -> item.getId()),
                ((item, id) -> item.setId(id))
                ));
        this.penguinStageRepository = new StageRepositoryImplement(new CollectionSettings<>(
                database, 
                "penguinStage", 
                Stage.class, 
                "stageId", 
                (item -> item.getStageId()),
                ((item, id) -> item.setStageId(id))
                ));
        this.penguinItemRepository = new ItemRepositoryImplement(new CollectionSettings<>(
                database, 
                "penguinItem", 
                Item.class, 
                "itemId", 
                (item -> item.getItemId()),
                ((item, id) -> item.setItemId(id))
                ));
        this.stageInfoReportRepository = new StageInfoReportRepositoryImplement(new CollectionSettings<>(
                database, 
                "penguinStageInfoReport", 
                StageInfoReport.class, 
                "stageCode", 
                (item -> item.getStageCode()),
                ((item, id) -> item.setStageCode(id))
                ));
        this.matrixReportRepository = new MatrixReportRepositoryImplement(new CollectionSettings<>(
                database, 
                "matrixReport", 
                MatrixReport.class, 
                "id", 
                (item -> item.getId()),
                ((item, id) -> item.setId(id))
                ));
        // ----- reminder -----
        this.reminderTaskRepository = new RemiderTaskRepositoryImplement(new CollectionSettings<>(
                database, 
                "reminderTask", 
                ReminderTask.class, 
                "id", 
                (item -> item.getId()),
                ((item, id) -> item.setId(id))
                ));
        
        
        
        Level feignLogLevel = Level.NONE;
        Logger feignLogger = new FeignLogger("feign", "debug");
        Encoder feignEncoder = new JacksonEncoder();
        Decoder feignDecoder = new JacksonDecoder();
        this.kcwikiApiFeignClient = Feign.builder()
                .encoder(feignEncoder)
                .decoder(feignDecoder)
                .logger(feignLogger)
                .logLevel(feignLogLevel)
                .target(KcwikiApiFeignClient.class, "http://api.kcwiki.moe")
                ;
        this.weiboApiFeignClient = Feign.builder()
                .encoder(feignEncoder)
                .decoder(feignDecoder)
                .logger(feignLogger)
                .logLevel(feignLogLevel)
                .target(WeiboApiFeignClient.class, "https://m.weibo.cn")
                ;
        this.weiboPictureApiFeignClient = Feign.builder()
                .encoder(feignEncoder)
                .decoder(feignDecoder)
                .logger(feignLogger)
                .logLevel(feignLogLevel)
                .target(WeiboPictureApiFeignClient.class, "https://wx2.sinaimg.cn")
                ;
        this.penguinApiFeignClient = Feign.builder()
                .encoder(feignEncoder)
                .decoder(feignDecoder)
                .logger(feignLogger)
                .logLevel(feignLogLevel)
                .target(PenguinApiFeignClient.class, "https://penguin-stats.io/PenguinStats/api")
                ;
        
        this.stillstandingApiFeignClient = Feign.builder()
                .encoder(feignEncoder)
                .decoder(feignDecoder)
                .logger(feignLogger)
                .logLevel(feignLogLevel)
                .target(StillstandingApiFeignClient.class, "http://localhost:10100/api/game")
                ;
    }
    
    public AppPrivateSettings appPrivateSettings;
    public AppPublicSettings appPublicSettings;
    public IConsole console;
    
    MongoClient mongoClient;
    
    // ----- weibo -----
    public WeiboApiFeignClient weiboApiFeignClient;
    public WeiboPictureApiFeignClient weiboPictureApiFeignClient;

    public WeiboUserInfoCacheRepository weiboUserInfoCacheRepository;
    public WeiboCardCacheRepository weiboCardCacheRepository;

    // ----- Stillstanding -----
    public StillstandingApiFeignClient stillstandingApiFeignClient;

    // ----- Penguin -----
    public PenguinApiFeignClient penguinApiFeignClient;
    
    public StageRepository penguinStageRepository;
    public ItemRepository penguinItemRepository;
    public MatrixReportRepository matrixReportRepository; 
    public StageInfoReportRepository stageInfoReportRepository;
    // ----- Kcwiki -----
    public KcwikiApiFeignClient kcwikiApiFeignClient;

    
    
    public RemiderTaskRepository reminderTaskRepository;




    
}
