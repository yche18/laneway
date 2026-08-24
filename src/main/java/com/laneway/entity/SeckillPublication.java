package com.laneway.entity;

import java.sql.Timestamp;
import lombok.Data;

/** Current stock, time window, and availability required to publish an activity. */
@Data
public class SeckillPublication {
  private Long voucherId;
  private Integer stock;
  private Timestamp beginTime;
  private Timestamp endTime;
  private Integer status;
}
