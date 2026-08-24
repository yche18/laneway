package com.laneway.service;

import com.laneway.dto.Result;

public interface IShopHeatService {
  void refresh();

  Result ranking(int windowMinutes, int limit);
}
