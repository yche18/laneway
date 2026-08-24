package com.laneway.mapper;

import org.apache.ibatis.annotations.Param;

/** Data access interface; SQL is maintained in the corresponding Mapper XML. */
public interface CacheCheckpointMapper {

  int saveCheckpoint(
      @Param("destination") String destination,
      @Param("batchId") Long batchId,
      @Param("binlogFile") String binlogFile,
      @Param("binlogOffset") Long binlogOffset,
      @Param("eventTimeMs") Long eventTimeMs);
}
