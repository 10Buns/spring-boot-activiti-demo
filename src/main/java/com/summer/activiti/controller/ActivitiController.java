package com.summer.activiti.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: ActivitiController
 * @description:
 * @author: summer
 * @create: 2020-10-16 17:41
 **/
@Controller
public class ActivitiController {

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    HistoryService historyService;
    @Autowired
    private ObjectMapper objectMapper;

    @RequestMapping("/")
    public String index(org.springframework.ui.Model model){
        List<Model> list = repositoryService.createModelQuery().list();
        model.addAttribute("list", list);
        return "index";
    }

    /**
     * @desc: 流程编辑
     * @param: []
     * @return: java.lang.String
     * @author: summer
     * @date: 2020/10/19 10:37 上午
     **/
    @RequestMapping("/editor")
    public String editor(){
        return "modeler";
    }

    /**
     * @desc: 流程创建
     * @param: [response, name, key]
     * @return: void
     * @author: summer
     * @date: 2020/10/19 10:37 上午
     **/
    @RequestMapping("/create")
    public void create(HttpServletResponse response,String name,String key) throws IOException {
        Model model = repositoryService.newModel();
        ObjectNode modelNode = objectMapper.createObjectNode();
        modelNode.put(ModelDataJsonConstants.MODEL_NAME, name);
        modelNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, "");
        modelNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
        model.setName(name);
        model.setKey(key);
        model.setMetaInfo(modelNode.toString());
        repositoryService.saveModel(model);
        ObjectNode editorNode = objectMapper.createObjectNode();
        editorNode.put("id", "canvas");
        editorNode.put("resourceId", "canvas");
        ObjectNode stencilSetNode = objectMapper.createObjectNode();
        stencilSetNode.put("namespace","http://b3mn.org/stencilset/bpmn2.0#");
        editorNode.put("stencilset", stencilSetNode);
        try {
            byte[] bytes = editorNode.toString().getBytes("UTF-8");
            repositoryService.addModelEditorSource(model.getId(), bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.sendRedirect("/editor?modelId="+ model.getId());
    }

    /**
     * @desc: 流程发布
     * @param: [modelId]
     * @return: java.lang.Object
     * @author: summer
     * @date: 2020/10/19 10:37 上午
     **/
    @ResponseBody
    @RequestMapping("/deploy")
    public Object deploy(String modelId){
        Map<String, String> map = new HashMap<String, String>();
        try {
            Model modelData = repositoryService.getModel(modelId);
            byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());
            if (bytes == null) {
                map.put("code", "FAILURE");
                return map;
            }
            JsonNode modelNode = new ObjectMapper().readTree(bytes);
            BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
            String bpmName = modelData.getName() + ".bpmn20.xml";
            Deployment deployment = repositoryService.createDeployment()
                                                        .name(modelData.getName())
                                                        .addBpmnModel(bpmName, model)
                                                        .deploy();
            modelData.setDeploymentId(deployment.getId());
            repositoryService.saveModel(modelData);
            map.put("code", "SUCCESS");
        } catch (Exception e) {
            map.put("code", "FAILURE");
        }
        return map;
    }

    /**
     * @desc: 撤销流程发布
     * @param: [modelId]
     * @return: java.lang.Object
     * @author: summer
     * @date: 2020/10/19 10:37 上午
     **/
    @ResponseBody
    @RequestMapping("/invokeDeploy")
    public Object invoke(String modelId){
        Map<String, String> map = new HashMap<String, String>();
        Model modelData = repositoryService.getModel(modelId);
        if(null != modelData){
            try {
                // false:普通删除，有正在执行的实例会抛出异常 true:级别删除,流程所有信息包括历史
                repositoryService.deleteDeployment(modelData.getDeploymentId(),true);
                map.put("code", "SUCCESS");
            } catch (Exception e) {
                map.put("code", "FAILURE");
            }
        }
        return map;
    }

    /**
     * @desc: 删除流程实例
     * @param: []
     * @return: void
     * @author: summer
     * @date: 2020/10/19 10:38 上午
     **/
    @ResponseBody
    @RequestMapping("/deletePI")
    public Object delete(String modelId){
        Map<String, String> map = new HashMap<String, String>();
        Model modelData = repositoryService.getModel(modelId);
        if(null != modelData){
            try {
                ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                                            .processDefinitionKey(modelData.getKey())
                                            .singleResult();
                if(null != pi) {
                    runtimeService.deleteProcessInstance(pi.getId(), "");
                    historyService.deleteHistoricProcessInstance(pi.getId());
                }
                map.put("code", "SUCCESS");
            } catch (Exception e) {
                e.printStackTrace();
                map.put("code", "FAILURE");
            }
        }
        return map;
    }
}
