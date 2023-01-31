package org.apache.airavata.metascheduler.metadata.analyzer.impl;

import org.apache.airavata.common.utils.ThriftClientPool;
import org.apache.airavata.metascheduler.core.engine.DataAnalyzer;
import org.apache.airavata.metascheduler.core.utils.Utils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.RegistryService.Client;

public class DataAnalyzerImpl implements DataAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataAnalyzerImpl.class);

    protected static ThriftClientPool<RegistryService.Client> registryClientPool = Utils.getRegistryServiceClientPool();


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        RegistryService.Client client = null;



    }


}
