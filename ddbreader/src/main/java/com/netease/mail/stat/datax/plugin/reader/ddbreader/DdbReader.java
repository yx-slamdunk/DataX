/**
 * @(#)DdbReader.java, 2017年3月17日. 
 * 
 * Copyright 2017 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netease.mail.stat.datax.plugin.reader.ddbreader;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.datax.common.plugin.RecordSender;
import com.alibaba.datax.common.spi.Reader;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.plugin.rdbms.reader.CommonRdbmsReader;
import com.alibaba.datax.plugin.rdbms.reader.Constant;
import com.alibaba.datax.plugin.rdbms.util.DataBaseType;

/**
 * Datax用于读取DDB数据的Plugin。 插件参考mysqlreader开发，通过在 {@link DataBaseType}
 * 中添加DDB驱动实现支持。
 * <p>
 * ddb的jdbc url格式参考：
 * <ul>
 * <h1>${ip}:${port}?key=conf/secret.key&logdir=logs/ddb/oms</h1>
 * </ul>
 * 
 * @author hzyangxiong2014@corp.netease.com
 */
public class DdbReader extends Reader {

    private static final DataBaseType DATABASE_TYPE = DataBaseType.DDB;

    private static final int FETCHSIZE_DEFA = 10;

    public static class Job extends Reader.Job {
        private static final Logger LOG = LoggerFactory.getLogger(Job.class);

        private Configuration originalConfig = null;

        private CommonRdbmsReader.Job commonRdbmsReaderJob;

        @Override
        public void init() {
            this.originalConfig = super.getPluginJobConf();

            Integer userConfigedFetchSize = this.originalConfig
                .getInt(Constant.FETCH_SIZE);
            if (userConfigedFetchSize == null) {
                LOG.warn("ddbreader 配置默认fetchSize: " + FETCHSIZE_DEFA);
                this.originalConfig.set(Constant.FETCH_SIZE, FETCHSIZE_DEFA);
            }

            this.commonRdbmsReaderJob = new CommonRdbmsReader.Job(DATABASE_TYPE);
            this.commonRdbmsReaderJob.init(this.originalConfig);
        }

        @Override
        public void preCheck() {
            init();
            this.commonRdbmsReaderJob.preCheck(this.originalConfig,
                DATABASE_TYPE);

        }

        @Override
        public List<Configuration> split(int adviceNumber) {
            return this.commonRdbmsReaderJob.split(this.originalConfig,
                adviceNumber);
        }

        @Override
        public void post() {
            this.commonRdbmsReaderJob.post(this.originalConfig);
        }

        @Override
        public void destroy() {
            this.commonRdbmsReaderJob.destroy(this.originalConfig);
        }

    }

    public static class Task extends Reader.Task {

        private Configuration readerSliceConfig;

        private CommonRdbmsReader.Task commonRdbmsReaderTask;

        @Override
        public void init() {
            this.readerSliceConfig = super.getPluginJobConf();
            this.commonRdbmsReaderTask = new CommonRdbmsReader.Task(
                DATABASE_TYPE, super.getTaskGroupId(), super.getTaskId());
            this.commonRdbmsReaderTask.init(this.readerSliceConfig);

        }

        @Override
        public void startRead(RecordSender recordSender) {
            int fetchSize = this.readerSliceConfig.getInt(Constant.FETCH_SIZE);

            this.commonRdbmsReaderTask.startRead(this.readerSliceConfig,
                recordSender, super.getTaskPluginCollector(), fetchSize);
        }

        @Override
        public void post() {
            this.commonRdbmsReaderTask.post(this.readerSliceConfig);
        }

        @Override
        public void destroy() {
            this.commonRdbmsReaderTask.destroy(this.readerSliceConfig);
        }

    }

}
