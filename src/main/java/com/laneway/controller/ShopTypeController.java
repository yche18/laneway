package com.laneway.controller;

import com.laneway.dto.Result;
import com.laneway.service.IShopTypeService;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller.
 *
 * @author Yi Chen
 */
@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {
  @Resource
  private IShopTypeService typeService;

  @GetMapping("list")
  public Result queryTypeList() {
    return typeService.queryTypeList();
  }
}
