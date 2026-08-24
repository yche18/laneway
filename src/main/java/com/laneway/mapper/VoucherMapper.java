package com.laneway.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.laneway.entity.Voucher;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * Mapper interface.
 *
 * @author Yi Chen
 * @since 2026-08-24
 */
public interface VoucherMapper extends BaseMapper<Voucher> {

  List<Voucher> queryVoucherOfShop(@Param("shopId") Long shopId);

  List<Long> selectActiveVoucherIds(@Param("shopId") Long shopId);
}
