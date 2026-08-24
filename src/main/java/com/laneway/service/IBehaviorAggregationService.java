package com.laneway.service;

import com.laneway.analytics.BehaviorEvent;
import java.util.List;
import java.util.Map;

/** Business processing service. */
public interface IBehaviorAggregationService {
  void accept(BehaviorEvent event);

  List<Map<String, Object>> ranking(int minutes, int limit);
}
