package com.summer.activiti.listener;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;

/**
 * @program: TaskListener
 * @description:
 * @author: summer
 * @create: 2020-10-19 17:18
 **/
@Component
public class LeaveTaskListener implements JavaDelegate, TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        delegateTask.setAssignee("张三");
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

    }
}
