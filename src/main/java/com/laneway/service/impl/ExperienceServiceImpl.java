package com.laneway.service.impl;

import com.laneway.cache.CacheReadTrace;
import com.laneway.cache.RequestCacheScope;
import com.laneway.dto.Result;
import com.laneway.entity.Shop;
import com.laneway.mapper.BehaviorMapper;
import com.laneway.mapper.MessageMapper;
import com.laneway.service.IBehaviorJournalService;
import com.laneway.service.IExperienceService;
import com.laneway.service.IShopService;
import com.laneway.service.IVoucherOrderService;
import com.laneway.utils.UserHolder;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ExperienceServiceImpl implements IExperienceService {
  @Resource
  private IShopService shopService;

  @Resource
  private IBehaviorJournalService behaviorJournalService;

  @Resource
  private IVoucherOrderService voucherOrderService;

  @Resource
  private BehaviorMapper behaviorMapper;

  @Resource
  private MessageMapper messageMapper;

  @Resource
  private MeterRegistry metrics;

  @Value("${laneway.experience.enabled:false}")
  private boolean enabled;

  @Value("${laneway.cache.enabled:true}")
  private boolean cacheEnabled;

  @Value("${laneway.analytics.enabled:false}")
  private boolean analyticsEnabled;

  @Value("${laneway.mq.enabled:false}")
  private boolean mqEnabled;

  @Value("${laneway.cdc.enabled:false}")
  private boolean cdcEnabled;

  @Override
  public Result queryExperienceCapabilities() {
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("enabled", enabled);
    if (enabled) {
      result.put("cacheEnabled", cacheEnabled);
      result.put("analyticsEnabled", analyticsEnabled);
      result.put("mqEnabled", mqEnabled);
      result.put("cdcEnabled", cdcEnabled);
      result.put("cdcConnected", readBooleanGauge("laneway.cdc.connected"));
      result.put("mqClientStarted", readBooleanGauge("laneway.mq.client.started"));
      result.put("kafkaConsumerRunning", readBooleanGauge("laneway.kafka.consumer.running"));
      result.put("sampledAt", System.currentTimeMillis());
    }
    return Result.ok(result);
  }

  private Boolean readBooleanGauge(String name) {
    Gauge gauge = metrics.find(name).gauge();
    return gauge == null ? null : gauge.value() == 1;
  }

  /**
   * Read the same shop twice within one request cache scope, recording cache layers and latency for
   * each read.
   *
   * <p>Observe request-level reuse without clearing caches to manufacture hits.
   */
  @Override
  public Result traceShopCacheReads(Long shopId) {
    if (!enabled) {
      return Result.fail("体验观察接口未启用");
    }
    if (shopId == null || shopId <= 0) {
      return Result.fail("请选择有效店铺");
    }
    List<Map<String, Object>> reads = new ArrayList<>();
    String shopName = null;
    try (RequestCacheScope scope = RequestCacheScope.open()) {
      for (int index = 0; index < 2; index++) {
        try (CacheReadTrace trace = CacheReadTrace.open()) {
          long started = System.nanoTime();
          Result loaded = shopService.queryById(shopId);
          if (!Boolean.TRUE.equals(loaded.getSuccess()) || loaded.getData() == null) {
            return loaded;
          }
          shopName = ((Shop) loaded.getData()).getName();
          Map<String, Object> read = new LinkedHashMap<>();
          read.put("sequence", index + 1);
          read.put("durationMicros", TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - started));
          read.put("events", trace.snapshot());
          reads.add(read);
        }
      }
    }
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("requestId", UUID.randomUUID().toString());
    result.put("shopId", shopId);
    result.put("shopName", shopName);
    result.put("reads", reads);
    return Result.ok(result);
  }

  /**
   * Submit three experience visit events and return their IDs so the browser can poll persistence
   * results.
   *
   * <p>submitted counts collection attempts, not Kafka acknowledgements or successful database
   * writes.
   */
  @Override
  public Result submitVisitEvents(Long shopId) {
    if (!enabled || !analyticsEnabled) {
      return Result.fail("Kafka 访问体验未启用");
    }
    if (shopId == null || shopId <= 0) {
      return Result.fail("请选择有效店铺");
    }
    Result shop = shopService.queryById(shopId);
    if (!Boolean.TRUE.equals(shop.getSuccess())) {
      return shop;
    }
    List<String> eventIds = new ArrayList<>();
    Long userId = UserHolder.getUser() == null ? null : UserHolder.getUser().getId();
    for (int index = 0; index < 3; index++) {
      String id = behaviorJournalService.submitShopVisitEvent(shopId, userId);
      if (id == null) {
        return Result.fail("访问采集未启用");
      }
      eventIds.add(id);
    }
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("eventIds", eventIds);
    result.put("submitted", eventIds.size());
    result.put("shopId", shopId);
    return Result.ok(result);
  }

  @Override
  public Result queryVisitEventReceipts(List<String> eventIds) {
    if (!enabled || !analyticsEnabled) {
      return Result.fail("Kafka 访问体验未启用");
    }
    if (eventIds == null || eventIds.isEmpty() || eventIds.size() > 3) {
      return Result.fail("一次仅查询 1～3 个体验事件");
    }
    for (String id : eventIds) {
      if (id == null || !id.matches("[a-f0-9]{8}(-[a-f0-9]{4}){3}-[a-f0-9]{12}")) {
        return Result.fail("体验事件号格式无效");
      }
    }
    List<Map<String, Object>> receipts = new ArrayList<>();
    for (String id : eventIds) {
      Map<String, Object> stored = behaviorMapper.selectEvent(id);
      Map<String, Object> receipt = new LinkedHashMap<>();
      receipt.put("eventId", id);
      receipt.put("state", stored == null ? "PENDING" : "PERSISTED");
      if (stored != null) {
        receipt.put("shopId", stored.get("shop_id"));
      }
      receipts.add(receipt);
    }
    return Result.ok(receipts);
  }

  @Override
  public Result queryMyOrderProgress(Long orderId) {
    if (!enabled) {
      return Result.fail("体验观察接口未启用");
    }
    if (orderId == null || orderId <= 0 || UserHolder.getUser() == null) {
      return Result.fail("请登录后查询自己的订单");
    }
    Result owned = voucherOrderService.queryMyOrder(orderId);
    if (!Boolean.TRUE.equals(owned.getSuccess())) {
      return owned;
    }
    return Result.ok(messageMapper.selectOrderProgress(orderId, UserHolder.getUser().getId()));
  }
}
