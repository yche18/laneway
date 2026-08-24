package com.laneway.mapper;

import org.apache.ibatis.annotations.Param;

/** Data access interface; SQL is maintained in the corresponding Mapper XML. */
public interface OperationAuditMapper {

  int insertAudit(
      @Param("userId") Long userId,
      @Param("method") String method,
      @Param("path") String path,
      @Param("httpStatus") Integer httpStatus);
}
