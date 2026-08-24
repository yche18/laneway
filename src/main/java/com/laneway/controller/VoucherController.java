package com.laneway.controller;

import com.laneway.dto.Result;
import com.laneway.entity.Voucher;
import com.laneway.service.IVoucherService;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller.
 *
 * @author Yi Chen
 */
@RestController
@RequestMapping("/voucher")
public class VoucherController {

  @Resource
  private IVoucherService voucherService;

  @GetMapping("/manage/capabilities")
  public Result capabilities() {
    return voucherService.capabilities();
  }

  /**
   * Create a flash-sale voucher.
   *
   * @param voucher voucher data including flash-sale details
   * @return voucher ID
   */
  @PostMapping("seckill")
  public Result addSeckillVoucher(@RequestBody Voucher voucher) {
    return voucherService.createSeckillVoucher(voucher);
  }

  /**
   * Create a regular voucher.
   *
   * @param voucher voucher data
   * @return voucher ID
   */
  @PostMapping
  public Result addVoucher(@RequestBody Voucher voucher) {
    return voucherService.createVoucher(voucher);
  }

  /**
   * List vouchers for a shop.
   *
   * @param shopId shop ID
   * @return voucher list
   */
  @GetMapping("/list/{shopId}")
  public Result queryVoucherOfShop(@PathVariable("shopId") Long shopId) {
    return voucherService.queryVoucherOfShop(shopId);
  }
}
