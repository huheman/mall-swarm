package com.macro.mall.service.impl;

import cn.hutool.json.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.macro.mall.common.api.CommonPage;
import com.macro.mall.dao.OmsOrderDao;
import com.macro.mall.dao.OmsOrderOperateHistoryDao;
import com.macro.mall.dto.*;
import com.macro.mall.mapper.DirectChargeMapper;
import com.macro.mall.mapper.OmsOrderMapper;
import com.macro.mall.mapper.OmsOrderOperateHistoryMapper;
import com.macro.mall.mapper.UmsMemberMapper;
import com.macro.mall.model.*;
import com.macro.mall.service.OmsOrderService;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.OutputStreamWriter;
import java.util.*;
import java.util.stream.Collectors;

import static cn.hutool.core.date.DateUtil.formatDateTime;

/**
 * 订单管理Service实现类
 * Created by macro on 2018/10/11.
 */
@Service
public class OmsOrderServiceImpl implements OmsOrderService {
    @Autowired
    private OmsOrderMapper orderMapper;
    @Autowired
    private OmsOrderDao orderDao;
    @Autowired
    private OmsOrderOperateHistoryDao orderOperateHistoryDao;
    @Autowired
    private OmsOrderOperateHistoryMapper orderOperateHistoryMapper;
    @Autowired
    private DirectChargeMapper directChargeMapper;
    @Autowired
    private UmsMemberMapper memberMapper;

    private List<String> csvHead = Arrays.asList("标号", "订单标题", "实付金额", "充值方式", "支付方式", "订单状态", "直充结果", "下单人手机号", "下单时间", "支付时间",
            "发货时间", "订单编号","订单来源","订单kol","推荐首单kol","操作系统");

    @Override
    public CommonPage<OmsOrderWithDirectCharge> list(OmsOrderQueryParam queryParam, Integer pageSize, Integer pageNum) {
        Page<OmsOrder> objects = PageHelper.startPage(pageNum, pageSize)
                .doSelectPage(() -> orderDao.getList(queryParam));
        List<Long> orderIds = objects.stream().map(omsOrder -> omsOrder.getId()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(orderIds)) {
            return CommonPage.restPage(new ArrayList<>(), 0L);
        }
        DirectChargeExample directChargeExample = new DirectChargeExample();
        directChargeExample.createCriteria().andOrderIdIn(orderIds);
        Map<Long, DirectCharge> map = directChargeMapper.selectByExample(directChargeExample).stream().collect(Collectors.toMap(DirectCharge::getOrderId, directCharge -> directCharge));
        Set<Long> memberIds = objects.stream().map(OmsOrder::getMemberId).collect(Collectors.toSet());
        UmsMemberExample umsMemberExample = new UmsMemberExample();
        umsMemberExample.createCriteria().andIdIn(new ArrayList<>(memberIds));
        List<UmsMember> umsMembers = memberMapper.selectByExample(umsMemberExample);
        Map<Long, UmsMember> memberMap = umsMembers.stream().collect(Collectors.toMap(UmsMember::getId, directCharge -> directCharge));
        List<OmsOrderWithDirectCharge> list = objects.parallelStream().map(omsOrder -> {
            OmsOrderWithDirectCharge omsOrderWithDirectCharge = new OmsOrderWithDirectCharge();
            BeanUtils.copyProperties(omsOrder, omsOrderWithDirectCharge);
            DirectCharge directCharge = map.get(omsOrder.getId());
            if (directCharge != null) {
                omsOrderWithDirectCharge.setDirectChargeStatus(directCharge.getChargeStatus());
                omsOrderWithDirectCharge.setDirectChargeFailReason(directCharge.getFailReason());
                omsOrderWithDirectCharge.setDirectChargeDetail(directCharge.getMoreInfo());
            }
            UmsMember umsMember = memberMap.get(omsOrder.getMemberId());
            if (umsMember != null) {
                omsOrderWithDirectCharge.setFirstInviteKol(umsMember.getFirstInviteKol());
            }
            return omsOrderWithDirectCharge;
        }).toList();
        return CommonPage.restPage(list, objects.getTotal());
    }

    @Override
    /*因为即氪发货就可以认为已经确认收货了*/
    public int delivery(List<OmsOrderDeliveryParam> deliveryParamList) {
        //批量发货
        int count = orderDao.delivery(deliveryParamList);
        //添加操作记录
        List<OmsOrderOperateHistory> operateHistoryList = deliveryParamList.stream()
                .map(omsOrderDeliveryParam -> {
                    OmsOrderOperateHistory history = new OmsOrderOperateHistory();
                    history.setOrderId(omsOrderDeliveryParam.getOrderId());
                    history.setCreateTime(new Date());
                    history.setOperateMan("后台管理员");
                    history.setOrderStatus(2);
                    history.setNote("完成发货");
                    return history;
                }).collect(Collectors.toList());
        orderOperateHistoryDao.insertList(operateHistoryList);
        return count;
    }

    @Override
    public int close(List<Long> ids, String note) {
        OmsOrder record = new OmsOrder();
        record.setStatus(4);
        OmsOrderExample example = new OmsOrderExample();
        example.createCriteria().andDeleteStatusEqualTo(0).andIdIn(ids);
        int count = orderMapper.updateByExampleSelective(record, example);
        List<OmsOrderOperateHistory> historyList = ids.stream().map(orderId -> {
            OmsOrderOperateHistory history = new OmsOrderOperateHistory();
            history.setOrderId(orderId);
            history.setCreateTime(new Date());
            history.setOperateMan("后台管理员");
            history.setOrderStatus(4);
            history.setNote("订单关闭:" + note);
            return history;
        }).collect(Collectors.toList());
        orderOperateHistoryDao.insertList(historyList);
        return count;
    }

    @Override
    public int delete(List<Long> ids) {
        OmsOrder record = new OmsOrder();
        record.setDeleteStatus(1);
        OmsOrderExample example = new OmsOrderExample();
        example.createCriteria().andDeleteStatusEqualTo(0).andIdIn(ids);
        return orderMapper.updateByExampleSelective(record, example);
    }

    @Override
    public OmsOrderDetail detail(Long id) {
        return orderDao.getDetail(id);
    }

    @Override
    public int updateReceiverInfo(OmsReceiverInfoParam receiverInfoParam) {
        OmsOrder order = new OmsOrder();
        order.setId(receiverInfoParam.getOrderId());
        order.setReceiverName(receiverInfoParam.getReceiverName());
        order.setReceiverPhone(receiverInfoParam.getReceiverPhone());
        order.setReceiverPostCode(receiverInfoParam.getReceiverPostCode());
        order.setReceiverDetailAddress(receiverInfoParam.getReceiverDetailAddress());
        order.setReceiverProvince(receiverInfoParam.getReceiverProvince());
        order.setReceiverCity(receiverInfoParam.getReceiverCity());
        order.setReceiverRegion(receiverInfoParam.getReceiverRegion());
        order.setModifyTime(new Date());
        int count = orderMapper.updateByPrimaryKeySelective(order);
        //插入操作记录
        OmsOrderOperateHistory history = new OmsOrderOperateHistory();
        history.setOrderId(receiverInfoParam.getOrderId());
        history.setCreateTime(new Date());
        history.setOperateMan("后台管理员");
        history.setOrderStatus(receiverInfoParam.getStatus());
        history.setNote("修改收货人信息");
        orderOperateHistoryMapper.insert(history);
        return count;
    }

    @Override
    public int updateMoneyInfo(OmsMoneyInfoParam moneyInfoParam) {
        OmsOrder order = new OmsOrder();
        order.setId(moneyInfoParam.getOrderId());
        order.setFreightAmount(moneyInfoParam.getFreightAmount());
        order.setDiscountAmount(moneyInfoParam.getDiscountAmount());
        order.setModifyTime(new Date());
        int count = orderMapper.updateByPrimaryKeySelective(order);
        //插入操作记录
        OmsOrderOperateHistory history = new OmsOrderOperateHistory();
        history.setOrderId(moneyInfoParam.getOrderId());
        history.setCreateTime(new Date());
        history.setOperateMan("后台管理员");
        history.setOrderStatus(moneyInfoParam.getStatus());
        history.setNote("修改费用信息");
        orderOperateHistoryMapper.insert(history);
        return count;
    }

    @Override
    public int updateNote(Long id, String note, Integer status) {
        OmsOrder order = new OmsOrder();
        order.setId(id);
        order.setNote(note);
        order.setModifyTime(new Date());
        int count = orderMapper.updateByPrimaryKeySelective(order);
        OmsOrderOperateHistory history = new OmsOrderOperateHistory();
        history.setOrderId(id);
        history.setCreateTime(new Date());
        history.setOperateMan("后台管理员");
        history.setOrderStatus(status);
        history.setNote("修改备注信息：" + note);
        orderOperateHistoryMapper.insert(history);
        return count;
    }

    @Override
    @SneakyThrows
    public void download(OmsOrderQueryParam queryParam, OutputStreamWriter outputStream) {
        int currentPage = 1;
        int pageSize = 100;
        String head = String.join(",", csvHead) + "\n";
        outputStream.write(head);
        while (true) {
            CommonPage<OmsOrderWithDirectCharge> list = this.list(queryParam, pageSize, currentPage);
            for (OmsOrderWithDirectCharge omsOrderWithDirectCharge : list.getList()) {
                List<String> tmp = new ArrayList<>();
                tmp.add(omsOrderWithDirectCharge.getId() + "");
                JSONObject moreInfo = new JSONObject(omsOrderWithDirectCharge.getMoreInfo());
                String title = omsOrderWithDirectCharge.getTitle();
                String chargeType = "";
                String platform = "";
                String payerPhone = "=\"" + omsOrderWithDirectCharge.getPayerPhone() + "\"";
                String payWay = formatPayWay(omsOrderWithDirectCharge.getPayType());
                String orderStatus = formatStatus(omsOrderWithDirectCharge.getStatus());
                if (moreInfo != null) {
                    String attr = moreInfo.getStr("attr");
                    if (StringUtils.indexOf(attr, "直充") >= 0) {
                        chargeType = "直充";
                    }
                    if (StringUtils.indexOf(attr, "代充") >= 0) {
                        chargeType = "代充";
                    }

                    platform = StringUtils.trimToEmpty(moreInfo.getStr("platform"));

                }

                tmp.add(title);
                tmp.add(omsOrderWithDirectCharge.getPayAmount().stripTrailingZeros().toPlainString());
                tmp.add(chargeType);
                tmp.add(payWay);
                tmp.add(orderStatus);
                tmp.add(formatDirectChargeStatus(omsOrderWithDirectCharge));
                tmp.add(payerPhone);
                tmp.add(StringUtils.trimToEmpty(formatDateTime(omsOrderWithDirectCharge.getCreateTime())));
                tmp.add(StringUtils.trimToEmpty(formatDateTime(omsOrderWithDirectCharge.getPaymentTime())));
                tmp.add(StringUtils.trimToEmpty(formatDateTime(omsOrderWithDirectCharge.getDeliveryTime())));
                tmp.add("=\"" + (omsOrderWithDirectCharge.getOrderSn() + '"'));
                tmp.add(formatSourceType(omsOrderWithDirectCharge.getSourceType()));
                tmp.add(StringUtils.trimToEmpty(omsOrderWithDirectCharge.getKolId()));
                tmp.add(StringUtils.trimToEmpty(omsOrderWithDirectCharge.getFirstInviteKol()));
                tmp.add(platform);
                outputStream.write(String.join(",", tmp) + "\n");
            }
            if (list.getTotal() <= (long) currentPage * pageSize) {
                break;
            } else {
                currentPage++;
            }
        }
    }

    private String formatSourceType(Integer sourceType) {
        if (sourceType == null) {
            return "";
        }
        return switch (sourceType) {
            case 0 -> "手机浏览器";
            case 1 -> "微信小程序";
            case 2 -> "微信H5";
            case 3 -> "电脑浏览器";
            default -> "未知来源";
        };
    }

    private String formatDirectChargeStatus(OmsOrderWithDirectCharge omsOrderWithDirectCharge) {
        if (omsOrderWithDirectCharge.getDirectChargeStatus() == null) {
            return "";
        }
        if (omsOrderWithDirectCharge.getDirectChargeStatus() == 1) {
            return "充值中";
        } else if (omsOrderWithDirectCharge.getDirectChargeStatus() == 3) {
            return "充值失败" ;
        } else if (omsOrderWithDirectCharge.getDirectChargeStatus() == 2) {
            return "充值成功";
        }
        return "";
    }

    private String formatPayWay(Integer payType) {
        if (payType == 1) {
            return "支付宝";
        } else if (payType == 2) {
            return "微信";
        }
        return "";
    }

    private String formatStatus(Integer status) {
        if (status == 1) {
            return "代发货";
        } else if (status == 2) {
            return "已发货";
        } else if (status == 3) {
            return "已完成";
        } else if (status == 4) {
            return "已关闭";
        } else if (status == 5) {
            return "无效订单";
        } else if (status == 6) {
            return "退款中";
        } else if (status == 7) {
            return "已退款";
        } else if (status == 0) {
            return "待付款";
        }
        return "";
    }


}
