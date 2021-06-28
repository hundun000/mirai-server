package com.hundun.mirai.bot.cp.penguin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.hundun.mirai.bot.core.CustomBeanFactory;
import com.hundun.mirai.bot.cp.penguin.db.ItemRepository;
import com.hundun.mirai.bot.cp.penguin.db.MatrixReportRepository;
import com.hundun.mirai.bot.cp.penguin.db.StageInfoReportRepository;
import com.hundun.mirai.bot.cp.penguin.db.StageRepository;
import com.hundun.mirai.bot.cp.penguin.domain.DropInfo;
import com.hundun.mirai.bot.cp.penguin.domain.DropType;
import com.hundun.mirai.bot.cp.penguin.domain.Item;
import com.hundun.mirai.bot.cp.penguin.domain.ResultMatrixNode;
import com.hundun.mirai.bot.cp.penguin.domain.ResultMatrixResponse;
import com.hundun.mirai.bot.cp.penguin.domain.Stage;
import com.hundun.mirai.bot.cp.penguin.domain.report.MatrixReport;
import com.hundun.mirai.bot.cp.penguin.domain.report.MatrixReportNode;
import com.hundun.mirai.bot.cp.penguin.domain.report.StageInfoNode;
import com.hundun.mirai.bot.cp.penguin.domain.report.StageInfoReport;
import com.hundun.mirai.bot.cp.penguin.feign.PenguinApiFeignClient;

import lombok.extern.slf4j.Slf4j;

/**
 * @author hundun
 * Created on 2021/04/26
 */
@Slf4j
@Component
public class PenguinService {
    
    StageRepository stageRepository;
    
    ItemRepository itemRepository;
    
    PenguinApiFeignClient penguinApiFeignClient;
    
    MatrixReportRepository matrixReportRepository; 
    
    StageInfoReportRepository stageInfoReportRepository; 
    
    @PostConstruct
    public void manualWired() {
        this.stageRepository = CustomBeanFactory.getInstance().penguinStageRepository;
        this.itemRepository = CustomBeanFactory.getInstance().penguinItemRepository;
        this.penguinApiFeignClient = CustomBeanFactory.getInstance().penguinApiFeignClient;
        this.matrixReportRepository = CustomBeanFactory.getInstance().matrixReportRepository;
        this.stageInfoReportRepository = CustomBeanFactory.getInstance().stageInfoReportRepository;
    }
    
    public synchronized void resetCache() {
        
        List<Stage> stages = penguinApiFeignClient.stages();
        stageRepository.deleteAll();
        stageRepository.saveAll(stages);
        log.info("updateStages items size = {}", stages.size());
        
        List<Item> items = penguinApiFeignClient.items();
        itemRepository.deleteAll();
        itemRepository.saveAll(items);
        log.info("updateItems items size = {}", items.size());
        
        matrixReportRepository.deleteAll();
        stageInfoReportRepository.deleteAll();
    }
    
    private StageInfoReport getStageInfoReport(Stage stage) {
        
        String reportId = stage.getCode();
        StageInfoReport report = stageInfoReportRepository.findById(reportId);
        if (report == null) {
            report = new StageInfoReport();
            report.setApCost(stage.getApCost());
            report.setStageCode(stage.getCode());
            Map<DropType, List<StageInfoNode>> nodes = new HashMap<>();
            for (DropInfo dropInfo : stage.getDropInfos()) {
                if (dropInfo.getItemId() == null) {
                    continue;
                }
                StageInfoNode node = new StageInfoNode();
                Item item = itemRepository.findById(dropInfo.getItemId());
                if (item == null) {
                    log.warn("ItemId = {} from dropInfo, but not in itemRepository");
                    return null;
                }
                node.setItemName(item.getName());
                node.setBoundsLower(dropInfo.getBounds().getLower());
                node.setBoundsUpper(dropInfo.getBounds().getUpper());
                node.setDropType(dropInfo.getDropType());
                
                if(!nodes.containsKey(node.getDropType())) {
                    nodes.put(node.getDropType(), new ArrayList<>());
                }
                nodes.get(node.getDropType()).add(node);
            }
            report.setNodes(nodes);
            stageInfoReportRepository.save(report);
        }
        return report;
    }
    private MatrixReport getResultMatrixReport(Item item) {
        String reportId = item.getItemId();
        MatrixReport report = matrixReportRepository.findById(reportId);
        if (report == null) {
            ResultMatrixResponse response = penguinApiFeignClient.resultMatrix(item.getItemId());
            if (response != null) {
                List<MatrixReportNode> reportNodes = new ArrayList<>();
                for (ResultMatrixNode matrixNode : response.getMatrix()) {
                    Stage stage = stageRepository.findById(matrixNode.getStageId());
                    int quantity = matrixNode.getQuantity();
                    int times = matrixNode.getTimes();
                    double gainRate = 1.0 * matrixNode.getQuantity() / matrixNode.getTimes();
                    int stageCost = stage.getApCost();
                    double costExpectation = stageCost / gainRate;
                    String stageCode = stage.getCode();
                    
                    MatrixReportNode reportNode = new MatrixReportNode();
                    reportNode.setQuantity(quantity);
                    reportNode.setTimes(times);
                    reportNode.setGainRate(gainRate);
                    reportNode.setGainRateString(String.format("%.2f", gainRate));
                    reportNode.setCostExpectation(costExpectation);
                    reportNode.setCostExpectationString(String.format("%.2f", costExpectation));
                    reportNode.setStageCode(stageCode);
                    reportNode.setStageCost(stageCost);
                    reportNodes.add(reportNode);
                }
                report = new MatrixReport();
                report.setId(reportId);
                report.setItemName(item.getName());
                report.setNodes(reportNodes);
                matrixReportRepository.save(report);
            }
        }
        return report;
    }
    
    public StageInfoReport getStageInfoReport(String stageCode) {
        Stage stage = stageRepository.findOneByCode(stageCode);
        if (stage == null) {
            return null;
        }
        StageInfoReport stageInfoReport = getStageInfoReport(stage);
        return stageInfoReport;
    }
    
    public MatrixReport getTopResultNode(String fuzzyName, int topSize) {
        Item item = itemRepository.findTopByName(fuzzyName);
        
        if (item == null || item.getItemId() == null) {
            return null;
        }
        
        MatrixReport report = getResultMatrixReport(item);
        Collections.sort(report.getNodes(), new Comparator<MatrixReportNode>() {
            @Override
            public int compare(MatrixReportNode o1, MatrixReportNode o2) {
                return 1 * (int)(o1.getCostExpectation() - o2.getCostExpectation());
            }
        });
        List<MatrixReportNode> rusult = report.getNodes().subList(0, Math.min(topSize, report.getNodes().size()));
        report.setNodes(rusult);
        return report;
    }
}
