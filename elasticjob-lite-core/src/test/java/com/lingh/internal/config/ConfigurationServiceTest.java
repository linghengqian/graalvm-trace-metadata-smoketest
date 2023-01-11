package com.lingh.internal.config;

import com.lingh.fixture.LiteYamlConstants;
import com.lingh.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.exception.JobConfigurationException;
import org.apache.shardingsphere.elasticjob.infra.exception.JobExecutionEnvironmentException;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;
import org.apache.shardingsphere.elasticjob.lite.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public final class ConfigurationServiceTest {

    @Mock
    private JobNodeStorage jobNodeStorage;

    private final ConfigurationService configService = new ConfigurationService(null, "test_job");

    @BeforeEach
    public void setUp() {
        ReflectionUtils.setFieldValue(configService, "jobNodeStorage", jobNodeStorage);
    }

    @Test
    public void assertLoadDirectly() {
        when(jobNodeStorage.getJobNodeDataDirectly("config")).thenReturn(LiteYamlConstants.getJobYaml());
        JobConfiguration actual = configService.load(false);
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getShardingTotalCount(), is(3));
    }

    @Test
    public void assertLoadFromCache() {
        when(jobNodeStorage.getJobNodeData("config")).thenReturn(LiteYamlConstants.getJobYaml());
        JobConfiguration actual = configService.load(true);
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getShardingTotalCount(), is(3));
    }

    @Test
    public void assertLoadFromCacheButNull() {
        when(jobNodeStorage.getJobNodeData("config")).thenReturn(null);
        when(jobNodeStorage.getJobNodeDataDirectly("config")).thenReturn(LiteYamlConstants.getJobYaml());
        JobConfiguration actual = configService.load(true);
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getShardingTotalCount(), is(3));
    }

    @Test
    public void assertSetUpJobConfigurationJobConfigurationForJobConflict() {
        when(jobNodeStorage.isJobRootNodeExisted()).thenReturn(true);
        when(jobNodeStorage.getJobRootNodeData()).thenReturn("org.apache.shardingsphere.elasticjob.lite.api.script.api.ScriptJob");
        try {
            assertThrows(JobConfigurationException.class,
                    () -> configService.setUpJobConfiguration(null,
                            JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").build()));
        } finally {
            verify(jobNodeStorage).isJobRootNodeExisted();
            verify(jobNodeStorage).getJobRootNodeData();
        }
    }

    @Test
    public void assertSetUpJobConfigurationNewJobConfiguration() {
        JobConfiguration jobConfig = JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").build();
        assertThat(configService.setUpJobConfiguration(ElasticJob.class.getName(), jobConfig), is(jobConfig));
        verify(jobNodeStorage).replaceJobNode("config", YamlEngine.marshal(JobConfigurationPOJO.fromJobConfiguration(jobConfig)));
    }

    @Test
    public void assertSetUpJobConfigurationExistedJobConfigurationAndOverwrite() {
        when(jobNodeStorage.isJobNodeExisted("config")).thenReturn(true);
        JobConfiguration jobConfig = JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").overwrite(true).build();
        assertThat(configService.setUpJobConfiguration(ElasticJob.class.getName(), jobConfig), is(jobConfig));
        verify(jobNodeStorage).replaceJobNode("config", YamlEngine.marshal(JobConfigurationPOJO.fromJobConfiguration(jobConfig)));
    }

    @Test
    public void assertSetUpJobConfigurationExistedJobConfigurationAndNotOverwrite() {
        when(jobNodeStorage.isJobNodeExisted("config")).thenReturn(true);
        when(jobNodeStorage.getJobNodeDataDirectly("config")).thenReturn(
                YamlEngine.marshal(JobConfigurationPOJO.fromJobConfiguration(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").build())));
        JobConfiguration jobConfig = JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").overwrite(false).build();
        JobConfiguration actual = configService.setUpJobConfiguration(ElasticJob.class.getName(), jobConfig);
        assertThat(actual, not(jobConfig));
    }

    @Test
    public void assertIsMaxTimeDiffSecondsTolerableWithDefaultValue() throws JobExecutionEnvironmentException {
        when(jobNodeStorage.getJobNodeData("config")).thenReturn(LiteYamlConstants.getJobYaml(-1));
        configService.checkMaxTimeDiffSecondsTolerable();
    }

    @Test
    public void assertIsMaxTimeDiffSecondsTolerable() throws JobExecutionEnvironmentException {
        when(jobNodeStorage.getJobNodeData("config")).thenReturn(LiteYamlConstants.getJobYaml());
        when(jobNodeStorage.getRegistryCenterTime()).thenReturn(System.currentTimeMillis());
        configService.checkMaxTimeDiffSecondsTolerable();
        verify(jobNodeStorage).getRegistryCenterTime();
    }

    @Test
    public void assertIsNotMaxTimeDiffSecondsTolerable() {
        when(jobNodeStorage.getJobNodeData("config")).thenReturn(LiteYamlConstants.getJobYaml());
        when(jobNodeStorage.getRegistryCenterTime()).thenReturn(0L);
        try {
            assertThrows(JobExecutionEnvironmentException.class, configService::checkMaxTimeDiffSecondsTolerable);
        } finally {
            verify(jobNodeStorage).getRegistryCenterTime();
        }
    }
}
