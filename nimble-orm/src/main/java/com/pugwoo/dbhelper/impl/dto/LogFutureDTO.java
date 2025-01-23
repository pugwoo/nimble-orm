package com.pugwoo.dbhelper.impl.dto;

import com.pugwoo.dbhelper.model.RunningSqlData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * log调用的返回值，包含未来可以取消或标记sql完成的future
 */
public class LogFutureDTO {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogFutureDTO.class);

    /**
     * 调用sql的future
     */
    private ScheduledFuture<?> feature;

    /**
     * 当前运行的sql的uuid
     */
    private String runningSqlUuid;

    public LogFutureDTO() {
    }

    public LogFutureDTO(ScheduledFuture<?> feature, String runningSqlUuid) {
        this.feature = feature;
        this.runningSqlUuid = runningSqlUuid;
    }

    public void cancel(Map<String, RunningSqlData> runningSqlMap) {
        if (feature != null) {
            try {
                feature.cancel(false);
            } catch (Throwable e) {
                LOGGER.error("cancel logSlowScheduler fail:{}", feature, e);
            }
        }
        if (runningSqlMap != null && runningSqlUuid != null) {
            runningSqlMap.remove(runningSqlUuid);
        }
    }

    public ScheduledFuture<?> getFeature() {
        return feature;
    }

    public void setFeature(ScheduledFuture<?> feature) {
        this.feature = feature;
    }

    public String getRunningSqlUuid() {
        return runningSqlUuid;
    }

    public void setRunningSqlUuid(String runningSqlUuid) {
        this.runningSqlUuid = runningSqlUuid;
    }
}
