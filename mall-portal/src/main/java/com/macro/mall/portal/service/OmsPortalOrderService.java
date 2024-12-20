package com.macro.mall.portal.service;

import com.macro.mall.common.api.CommonPage;
import com.macro.mall.model.OmsOrderItem;
import com.macro.mall.portal.domain.ConfirmOrderResult;
import com.macro.mall.portal.domain.OmsOrderDetail;
import com.macro.mall.portal.domain.OrderParam;
import com.macro.mall.portal.domain.OrderParamWithAttribute;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 前台订单管理Service
 * Created by macro on 2018/8/30.
 */
public interface OmsPortalOrderService {
    /**
     * 根据用户购物车信息生成确认单信息
     */
    ConfirmOrderResult generateConfirmOrder(List<Long> cartIds,Boolean useRedeemCode);

    /**
     * 根据提交信息生成订单
     */
    @Transactional
    Map<String, Object> generateOrder(OrderParam orderParam);

    /**
     * 支付成功后的回调
     */
    @Transactional
    Integer paySuccess(Long orderId, Integer payType);

    /**
     * 自动取消超时订单
     */
    @Transactional
    Integer cancelTimeOutOrder();

    /**
     * 取消单个超时订单
     */
    @Transactional
    void cancelOrder(Long orderId,String operator,String closeReason);


    /**
     * 确认收货
     */
    void confirmReceiveOrder(Long orderId);

    /**
     * 分页获取用户订单
     */
    CommonPage<OmsOrderDetail> list(Integer status, Integer pageNum, Integer pageSize);

    /**
     * 根据订单ID获取订单详情
     */
    OmsOrderDetail detail(Long orderId);

    /**
     * 用户根据订单ID删除订单
     */
    void deleteOrder(Long orderId);

    /**
     * 根据orderSn来实现的支付成功逻辑
     */
    @Transactional
    void paySuccessByOrderSn(String orderSn, Integer payType);

    void updateMoreInfo(String orderSn, String key,String value);

    @Transactional
    Map<String, Object> generateOrderWithAttribute(OrderParamWithAttribute orderParam);

    String hint(Long orderId);

    OmsOrderItem selectOrderItemByOrderSN(String orderSN);

    String showCards(Long orderId,Long memberId);

    void recordCards(String orderSN,String cardInfo);

    @Transactional
    String refund(Long id,String reason);

    void refundSuccess(String orderSN);

    Map<String, Set<String>> historyProperties(Long userId, Long productId);
}
