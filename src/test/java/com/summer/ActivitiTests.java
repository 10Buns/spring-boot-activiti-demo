package com.summer;


import com.summer.activiti.ActivitiApplication;
import com.summer.activiti.entity.QingJiaBean;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = ActivitiApplication.class)
class ActivitiTests {

    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private TaskService taskService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private HistoryService historyService;

    /**
     * @desc: 获取流程引擎名称
     * @param: []
     * @return: void
     * @author: summer
     * @date: 2020/10/15 11:38 上午
     **/
    @Test
    public void testCreateActivitiEngine() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        System.out.println("流程引擎名称:"+ processEngine.getName());
    }


    /**
     * @desc: 部署流程定义
     * @param: [] ID=5001 key = myProcess_1
     * @return: void
     * @author: summer
     * @date: 2020/10/15 11:40 上午
     **/
    @Test
    public void testDeploy(){
        Deployment deploy = repositoryService.createDeployment()
                                .addClasspathResource("processes/QingJiaActiviti.bpmn")
                                .name("请假流程单")
                                .category("办公类别")
                                .deploy();
        System.out.println("部署ID:" + deploy.getId());
        System.out.println("部署名称:" + deploy.getName());
    }

    /**
     * @desc: 开启一个流程实例
     * @param: []
     * @return: void
     * @author: summer
     * @date: 2020/10/16 9:48 上午
     **/
    @Test
    public void testStartProcess(){
        String processKey = "department_leave_bill";
        Map<String, Object> params = new HashMap<>();
        params.put("userName", "张三");
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(processKey, "1", params);
        System.out.println("流程实例ID:" + pi.getId());
        System.out.println("流程实例ID:" + pi.getProcessInstanceId());
        System.out.println("流程定义ID:" + pi.getProcessDefinitionId());
    }

    /**
     * @desc: 查询流程实例状态
     * @param: []
     * @return: void
     * @author: summer
     * @date: 2020/10/16 2:27 下午
     **/
    @Test
    public void testQueryProcessInstanceState(){
        String processKey = "department_leave_bill";
        ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                                        .processDefinitionKey(processKey)
                                        .singleResult();
        if(pi!=null){
            System.out.println("流程实例:"+ processKey +"正在运行!当前活动的任务:"+pi.getActivityId());
        }else{
            System.out.println("流程实例:"+ processKey +"已经结束！");
        }
    }

    /**
     * @desc: 查询所有任务
     * @param: []
     * @return: void
     * @author: summer
     * @date: 2020/10/16 9:48 上午
     **/
    @Test
    public void testQueryTask(){
        List<Task> list = taskService.createTaskQuery()
                                    .processDefinitionKey("department_leave_bill")
                                    .list();
        for (Task task : list){
            System.out.println("任务的办理人："+task.getAssignee());
            System.out.println("任务的id："+task.getId());
            System.out.println("任务的名称："+task.getName());
            System.out.println("-------------------------------");
        }
    }

    /**
     * @desc: 完成任务
     * @param: []
     * @return: void
     * @author: summer
     * @date: 2020/10/16 9:48 上午
     **/
    @Test
    public void testCompleteTask(){
        String taskId = "60002";
        Map<String, Object> params = new HashMap<>();
        params.put("userName", "HR.刘");
        String message = "同意";
        //添加批注，批注人通过如下方式设置
        Authentication.setAuthenticatedUserId("HR.刘");
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        taskService.addComment(taskId, task.getProcessInstanceId(), message);
        taskService.complete(taskId, params);
        System.out.println("任务执行完毕！");
    }

    /**
     * @desc: 查询流程定义
     * @param: []
     * @return: void
     * @author: summer
     * @date: 2020/10/16 9:49 上午
     **/
    @Test
    public void testFindProcessDefinition(){
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery()
                                        .orderByProcessDefinitionVersion()
                                        .desc()
                                        .list();
        for (ProcessDefinition p : list){
            System.out.println("流程定义ID:"+p.getId());
            System.out.println("流程定义的名称:"+p.getName());
            System.out.println("流程定义的key:"+p.getKey());
            System.out.println("流程定义的版本:"+p.getVersion());
            System.out.println("资源名称bpmn文件:"+p.getResourceName());
            System.out.println("资源名称png文件:"+p.getDiagramResourceName());
            System.out.println("部署对象ID："+p.getDeploymentId());
            System.out.println("-------------------------------");
        }
    }

    /**
     * @desc: 删除流程定义
     * @param: []
     * @return: void
     * @author: summer
     * @date: 2020/10/16 9:49 上午
     **/
    @Test
    public void testDeleteProcessDefinition(){
        String deployId = "7501";
        repositoryService.deleteDeployment(deployId, true);
        System.out.println("delete success");
    }


    /**
     * @desc: 查询历史流程实例
     * @param: []
     * @return: void
     * @author: summer
     * @date: 2020/10/16 2:31 下午
     **/
    @Test
    public void testQueryHistoryProcessInstance(){
        List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery()
                                                            .list();
        for (HistoricProcessInstance i : list){
            System.out.println("历史流程实例ID:" + i.getId());
            System.out.println("历史流程定义的ID:" + i.getProcessDefinitionId());
            System.out.println("历史流程实例起止时间:" + i.getStartTime()+"～" + i.getEndTime());
            System.out.println("-------------------------------");
        }
    }

    /**
     * @desc: 查询历史流程实例任务信息
     * @param: []
     * @return: void
     * @author: summer
     * @date: 2020/10/16 2:42 下午
     **/
    @Test
    public void testQueryHistoryProcessInstanceTask(){
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()
                                                        .list();
        for (HistoricTaskInstance t : list){
            System.out.print("历史流程实例任务id:" + t.getId());
            System.out.print("历史流程定义的id:" + t.getProcessDefinitionId());
            System.out.print("历史流程实例任务名称:" + t.getName());
            System.out.println("历史流程实例任务处理人:" + t.getAssignee());
            System.out.println("-------------------------------");
        }
    }

    /**
     * @desc: 设置参数
     * @param: []
     * @return: void
     * @author: summer
     * @date: 2020/10/16 9:49 上午
     **/
    @Test
    public void testSetVariable(){
        String taskId = "20004";
        QingJiaBean qjb = new QingJiaBean();
        qjb.setId(1);
        qjb.setApplicant("张三");
        qjb.setDate(new Date());
        qjb.setReason("外出请假");
        taskService.setVariable(taskId, "QingJiaBean", qjb);
        System.out.println("----------任务变量设置成功----------");
        QingJiaBean qjb2 = (QingJiaBean) taskService.getVariable(taskId, "QingJiaBean");
        System.out.println("----------读取任务变量数据----------");
        System.out.println("请假申请人:"+qjb2.getApplicant());
    }
    
}
