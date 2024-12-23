package com.macro.mall.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RedeemCodeRecordExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public RedeemCodeRecordExample() {
        oredCriteria = new ArrayList<>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(Long value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Long> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Long> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Long value1, Long value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Long value1, Long value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andRedeemCodeIsNull() {
            addCriterion("redeem_code is null");
            return (Criteria) this;
        }

        public Criteria andRedeemCodeIsNotNull() {
            addCriterion("redeem_code is not null");
            return (Criteria) this;
        }

        public Criteria andRedeemCodeEqualTo(String value) {
            addCriterion("redeem_code =", value, "redeemCode");
            return (Criteria) this;
        }

        public Criteria andRedeemCodeNotEqualTo(String value) {
            addCriterion("redeem_code <>", value, "redeemCode");
            return (Criteria) this;
        }

        public Criteria andRedeemCodeGreaterThan(String value) {
            addCriterion("redeem_code >", value, "redeemCode");
            return (Criteria) this;
        }

        public Criteria andRedeemCodeGreaterThanOrEqualTo(String value) {
            addCriterion("redeem_code >=", value, "redeemCode");
            return (Criteria) this;
        }

        public Criteria andRedeemCodeLessThan(String value) {
            addCriterion("redeem_code <", value, "redeemCode");
            return (Criteria) this;
        }

        public Criteria andRedeemCodeLessThanOrEqualTo(String value) {
            addCriterion("redeem_code <=", value, "redeemCode");
            return (Criteria) this;
        }

        public Criteria andRedeemCodeLike(String value) {
            addCriterion("redeem_code like", value, "redeemCode");
            return (Criteria) this;
        }

        public Criteria andRedeemCodeNotLike(String value) {
            addCriterion("redeem_code not like", value, "redeemCode");
            return (Criteria) this;
        }

        public Criteria andRedeemCodeIn(List<String> values) {
            addCriterion("redeem_code in", values, "redeemCode");
            return (Criteria) this;
        }

        public Criteria andRedeemCodeNotIn(List<String> values) {
            addCriterion("redeem_code not in", values, "redeemCode");
            return (Criteria) this;
        }

        public Criteria andRedeemCodeBetween(String value1, String value2) {
            addCriterion("redeem_code between", value1, value2, "redeemCode");
            return (Criteria) this;
        }

        public Criteria andRedeemCodeNotBetween(String value1, String value2) {
            addCriterion("redeem_code not between", value1, value2, "redeemCode");
            return (Criteria) this;
        }

        public Criteria andSkuIdIsNull() {
            addCriterion("sku_id is null");
            return (Criteria) this;
        }

        public Criteria andSkuIdIsNotNull() {
            addCriterion("sku_id is not null");
            return (Criteria) this;
        }

        public Criteria andSkuIdEqualTo(Long value) {
            addCriterion("sku_id =", value, "skuId");
            return (Criteria) this;
        }

        public Criteria andSkuIdNotEqualTo(Long value) {
            addCriterion("sku_id <>", value, "skuId");
            return (Criteria) this;
        }

        public Criteria andSkuIdGreaterThan(Long value) {
            addCriterion("sku_id >", value, "skuId");
            return (Criteria) this;
        }

        public Criteria andSkuIdGreaterThanOrEqualTo(Long value) {
            addCriterion("sku_id >=", value, "skuId");
            return (Criteria) this;
        }

        public Criteria andSkuIdLessThan(Long value) {
            addCriterion("sku_id <", value, "skuId");
            return (Criteria) this;
        }

        public Criteria andSkuIdLessThanOrEqualTo(Long value) {
            addCriterion("sku_id <=", value, "skuId");
            return (Criteria) this;
        }

        public Criteria andSkuIdIn(List<Long> values) {
            addCriterion("sku_id in", values, "skuId");
            return (Criteria) this;
        }

        public Criteria andSkuIdNotIn(List<Long> values) {
            addCriterion("sku_id not in", values, "skuId");
            return (Criteria) this;
        }

        public Criteria andSkuIdBetween(Long value1, Long value2) {
            addCriterion("sku_id between", value1, value2, "skuId");
            return (Criteria) this;
        }

        public Criteria andSkuIdNotBetween(Long value1, Long value2) {
            addCriterion("sku_id not between", value1, value2, "skuId");
            return (Criteria) this;
        }

        public Criteria andKolIdIsNull() {
            addCriterion("kol_id is null");
            return (Criteria) this;
        }

        public Criteria andKolIdIsNotNull() {
            addCriterion("kol_id is not null");
            return (Criteria) this;
        }

        public Criteria andKolIdEqualTo(String value) {
            addCriterion("kol_id =", value, "kolId");
            return (Criteria) this;
        }

        public Criteria andKolIdNotEqualTo(String value) {
            addCriterion("kol_id <>", value, "kolId");
            return (Criteria) this;
        }

        public Criteria andKolIdGreaterThan(String value) {
            addCriterion("kol_id >", value, "kolId");
            return (Criteria) this;
        }

        public Criteria andKolIdGreaterThanOrEqualTo(String value) {
            addCriterion("kol_id >=", value, "kolId");
            return (Criteria) this;
        }

        public Criteria andKolIdLessThan(String value) {
            addCriterion("kol_id <", value, "kolId");
            return (Criteria) this;
        }

        public Criteria andKolIdLessThanOrEqualTo(String value) {
            addCriterion("kol_id <=", value, "kolId");
            return (Criteria) this;
        }

        public Criteria andKolIdLike(String value) {
            addCriterion("kol_id like", value, "kolId");
            return (Criteria) this;
        }

        public Criteria andKolIdNotLike(String value) {
            addCriterion("kol_id not like", value, "kolId");
            return (Criteria) this;
        }

        public Criteria andKolIdIn(List<String> values) {
            addCriterion("kol_id in", values, "kolId");
            return (Criteria) this;
        }

        public Criteria andKolIdNotIn(List<String> values) {
            addCriterion("kol_id not in", values, "kolId");
            return (Criteria) this;
        }

        public Criteria andKolIdBetween(String value1, String value2) {
            addCriterion("kol_id between", value1, value2, "kolId");
            return (Criteria) this;
        }

        public Criteria andKolIdNotBetween(String value1, String value2) {
            addCriterion("kol_id not between", value1, value2, "kolId");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNull() {
            addCriterion("create_time is null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNotNull() {
            addCriterion("create_time is not null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeEqualTo(Date value) {
            addCriterion("create_time =", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(Date value) {
            addCriterion("create_time <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(Date value) {
            addCriterion("create_time >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("create_time >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(Date value) {
            addCriterion("create_time <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Date value) {
            addCriterion("create_time <=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIn(List<Date> values) {
            addCriterion("create_time in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotIn(List<Date> values) {
            addCriterion("create_time not in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeBetween(Date value1, Date value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotBetween(Date value1, Date value2) {
            addCriterion("create_time not between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andUseStatusIsNull() {
            addCriterion("use_status is null");
            return (Criteria) this;
        }

        public Criteria andUseStatusIsNotNull() {
            addCriterion("use_status is not null");
            return (Criteria) this;
        }

        public Criteria andUseStatusEqualTo(String value) {
            addCriterion("use_status =", value, "useStatus");
            return (Criteria) this;
        }

        public Criteria andUseStatusNotEqualTo(String value) {
            addCriterion("use_status <>", value, "useStatus");
            return (Criteria) this;
        }

        public Criteria andUseStatusGreaterThan(String value) {
            addCriterion("use_status >", value, "useStatus");
            return (Criteria) this;
        }

        public Criteria andUseStatusGreaterThanOrEqualTo(String value) {
            addCriterion("use_status >=", value, "useStatus");
            return (Criteria) this;
        }

        public Criteria andUseStatusLessThan(String value) {
            addCriterion("use_status <", value, "useStatus");
            return (Criteria) this;
        }

        public Criteria andUseStatusLessThanOrEqualTo(String value) {
            addCriterion("use_status <=", value, "useStatus");
            return (Criteria) this;
        }

        public Criteria andUseStatusLike(String value) {
            addCriterion("use_status like", value, "useStatus");
            return (Criteria) this;
        }

        public Criteria andUseStatusNotLike(String value) {
            addCriterion("use_status not like", value, "useStatus");
            return (Criteria) this;
        }

        public Criteria andUseStatusIn(List<String> values) {
            addCriterion("use_status in", values, "useStatus");
            return (Criteria) this;
        }

        public Criteria andUseStatusNotIn(List<String> values) {
            addCriterion("use_status not in", values, "useStatus");
            return (Criteria) this;
        }

        public Criteria andUseStatusBetween(String value1, String value2) {
            addCriterion("use_status between", value1, value2, "useStatus");
            return (Criteria) this;
        }

        public Criteria andUseStatusNotBetween(String value1, String value2) {
            addCriterion("use_status not between", value1, value2, "useStatus");
            return (Criteria) this;
        }

        public Criteria andUsePhoneIsNull() {
            addCriterion("use_phone is null");
            return (Criteria) this;
        }

        public Criteria andUsePhoneIsNotNull() {
            addCriterion("use_phone is not null");
            return (Criteria) this;
        }

        public Criteria andUsePhoneEqualTo(String value) {
            addCriterion("use_phone =", value, "usePhone");
            return (Criteria) this;
        }

        public Criteria andUsePhoneNotEqualTo(String value) {
            addCriterion("use_phone <>", value, "usePhone");
            return (Criteria) this;
        }

        public Criteria andUsePhoneGreaterThan(String value) {
            addCriterion("use_phone >", value, "usePhone");
            return (Criteria) this;
        }

        public Criteria andUsePhoneGreaterThanOrEqualTo(String value) {
            addCriterion("use_phone >=", value, "usePhone");
            return (Criteria) this;
        }

        public Criteria andUsePhoneLessThan(String value) {
            addCriterion("use_phone <", value, "usePhone");
            return (Criteria) this;
        }

        public Criteria andUsePhoneLessThanOrEqualTo(String value) {
            addCriterion("use_phone <=", value, "usePhone");
            return (Criteria) this;
        }

        public Criteria andUsePhoneLike(String value) {
            addCriterion("use_phone like", value, "usePhone");
            return (Criteria) this;
        }

        public Criteria andUsePhoneNotLike(String value) {
            addCriterion("use_phone not like", value, "usePhone");
            return (Criteria) this;
        }

        public Criteria andUsePhoneIn(List<String> values) {
            addCriterion("use_phone in", values, "usePhone");
            return (Criteria) this;
        }

        public Criteria andUsePhoneNotIn(List<String> values) {
            addCriterion("use_phone not in", values, "usePhone");
            return (Criteria) this;
        }

        public Criteria andUsePhoneBetween(String value1, String value2) {
            addCriterion("use_phone between", value1, value2, "usePhone");
            return (Criteria) this;
        }

        public Criteria andUsePhoneNotBetween(String value1, String value2) {
            addCriterion("use_phone not between", value1, value2, "usePhone");
            return (Criteria) this;
        }

        public Criteria andUseOrderSnIsNull() {
            addCriterion("use_order_sn is null");
            return (Criteria) this;
        }

        public Criteria andUseOrderSnIsNotNull() {
            addCriterion("use_order_sn is not null");
            return (Criteria) this;
        }

        public Criteria andUseOrderSnEqualTo(String value) {
            addCriterion("use_order_sn =", value, "useOrderSn");
            return (Criteria) this;
        }

        public Criteria andUseOrderSnNotEqualTo(String value) {
            addCriterion("use_order_sn <>", value, "useOrderSn");
            return (Criteria) this;
        }

        public Criteria andUseOrderSnGreaterThan(String value) {
            addCriterion("use_order_sn >", value, "useOrderSn");
            return (Criteria) this;
        }

        public Criteria andUseOrderSnGreaterThanOrEqualTo(String value) {
            addCriterion("use_order_sn >=", value, "useOrderSn");
            return (Criteria) this;
        }

        public Criteria andUseOrderSnLessThan(String value) {
            addCriterion("use_order_sn <", value, "useOrderSn");
            return (Criteria) this;
        }

        public Criteria andUseOrderSnLessThanOrEqualTo(String value) {
            addCriterion("use_order_sn <=", value, "useOrderSn");
            return (Criteria) this;
        }

        public Criteria andUseOrderSnLike(String value) {
            addCriterion("use_order_sn like", value, "useOrderSn");
            return (Criteria) this;
        }

        public Criteria andUseOrderSnNotLike(String value) {
            addCriterion("use_order_sn not like", value, "useOrderSn");
            return (Criteria) this;
        }

        public Criteria andUseOrderSnIn(List<String> values) {
            addCriterion("use_order_sn in", values, "useOrderSn");
            return (Criteria) this;
        }

        public Criteria andUseOrderSnNotIn(List<String> values) {
            addCriterion("use_order_sn not in", values, "useOrderSn");
            return (Criteria) this;
        }

        public Criteria andUseOrderSnBetween(String value1, String value2) {
            addCriterion("use_order_sn between", value1, value2, "useOrderSn");
            return (Criteria) this;
        }

        public Criteria andUseOrderSnNotBetween(String value1, String value2) {
            addCriterion("use_order_sn not between", value1, value2, "useOrderSn");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {
        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}