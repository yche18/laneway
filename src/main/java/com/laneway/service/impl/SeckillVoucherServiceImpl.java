package com.laneway.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.laneway.entity.SeckillVoucher;
import com.laneway.mapper.SeckillVoucherMapper;
import com.laneway.service.ISeckillVoucherService;
import org.springframework.stereotype.Service;

/**
 * Service implementation for flash-sale vouchers, which have a one-to-one relationship to vouchers.
 *
 * @author Yi Chen
 * @since 2026-08-24
 */
@Service
public class SeckillVoucherServiceImpl extends ServiceImpl<SeckillVoucherMapper, SeckillVoucher>
    implements ISeckillVoucherService {}
