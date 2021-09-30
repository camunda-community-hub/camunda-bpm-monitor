package com.squashedbug.camunda.bpm.monitor;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.RuntimeService;

public class IncidentCreator implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        execution.createIncident("incident", "incident", "incident");
        RuntimeService runtimeService = execution.getProcessEngine().getRuntimeService();
        Context.getCommandContext().getTransactionContext().addTransactionListener(TransactionState.COMMITTED,
                commandContext -> {
                    runtimeService.suspendProcessInstanceById(execution.getProcessInstanceId());
                });
    }

}
