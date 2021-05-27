package com.hundun.mirai.server.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hundun.mirai.plugin.CustomBeanFactory;
import com.hundun.mirai.plugin.cp.kcwiki.KancolleWikiService;
import com.hundun.mirai.plugin.cp.penguin.PenguinService;
import com.hundun.mirai.plugin.cp.penguin.domain.report.MatrixReport;
import com.hundun.mirai.plugin.cp.penguin.domain.report.StageInfoReport;
import com.hundun.mirai.plugin.cp.weibo.WeiboService;
import com.hundun.mirai.plugin.cp.weibo.domain.WeiboCardCache;

/**
 * @author hundun
 * Created on 2021/04/26
 */
@RestController
@RequestMapping("/api/data")
public class DataController {
    
    PenguinService penguinService = CustomBeanFactory.getInstance().penguinService;
    
    KancolleWikiService kancolleWikiService = CustomBeanFactory.getInstance().kancolleWikiService;
    
    WeiboService weiboService = CustomBeanFactory.getInstance().weiboService;
    
    @RequestMapping(value="/weibo/updateAndGetTopBlog", method=RequestMethod.GET)
    public List<WeiboCardCache> updateAndGetTopBlog(
            @RequestParam("uid") String uid) {
        return weiboService.updateAndGetTopBlog(uid);
    }
    
    @RequestMapping(value="/penguin/resetCache", method=RequestMethod.GET)
    public String updateItems() {
        penguinService.resetCache();
        return "OK";
    }
    
    @RequestMapping(value="/kcw/getShipDetail", method=RequestMethod.GET)
    public String getShipDetail(
            @RequestParam("fuzzyName") String fuzzyName) {
        return kancolleWikiService.getShipDetail(fuzzyName).toString();
    }

    
    @RequestMapping(value="/penguin/getTopResultNode", method=RequestMethod.GET)
    public MatrixReport getTopResultNode(
            @RequestParam("fuzzyName") String fuzzyName,
            @RequestParam("topSize") int topSize
            ) {
        return penguinService.getTopResultNode(fuzzyName, topSize);
    }
    
    @RequestMapping(value="/penguin/getStageInfoReport", method=RequestMethod.GET)
    public StageInfoReport getStageInfoReport(
            @RequestParam("stageCode") String stageCode
            ) {
        return penguinService.getStageInfoReport(stageCode);
    }
}
