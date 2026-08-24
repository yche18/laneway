package com.laneway.service.impl;

import com.laneway.mapper.OperationAuditMapper;
import com.laneway.service.IOperationAuditService;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class OperationAuditServiceImpl implements IOperationAuditService {
  @Resource
  private OperationAuditMapper operationAuditMapper;

  @Override
  public void recordAudit(Long userId, String method, String path, Integer httpStatus) {
    operationAuditMapper.insertAudit(userId, method, path, httpStatus);
  }
}
