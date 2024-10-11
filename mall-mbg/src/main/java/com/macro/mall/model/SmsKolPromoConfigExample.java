package com.macro.mall.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SmsKolPromoConfigExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public SmsKolPromoConfigExample() {
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

        public Criteria andKolNameIsNull() {
            addCriterion("kol_name is null");
            return (Criteria) this;
        }

        public Criteria andKolNameIsNotNull() {
            addCriterion("kol_name is not null");
            return (Criteria) this;
        }

        public Criteria andKolNameEqualTo(String value) {
            addCriterion("kol_name =", value, "kolName");
            return (Criteria) this;
        }

        public Criteria andKolNameNotEqualTo(String value) {
            addCriterion("kol_name <>", value, "kolName");
            return (Criteria) this;
        }

        public Criteria andKolNameGreaterThan(String value) {
            addCriterion("kol_name >", value, "kolName");
            return (Criteria) this;
        }

        public Criteria andKolNameGreaterThanOrEqualTo(String value) {
            addCriterion("kol_name >=", value, "kolName");
            return (Criteria) this;
        }

        public Criteria andKolNameLessThan(String value) {
            addCriterion("kol_name <", value, "kolName");
            return (Criteria) this;
        }

        public Criteria andKolNameLessThanOrEqualTo(String value) {
            addCriterion("kol_name <=", value, "kolName");
            return (Criteria) this;
        }

        public Criteria andKolNameLike(String value) {
            addCriterion("kol_name like", value, "kolName");
            return (Criteria) this;
        }

        public Criteria andKolNameNotLike(String value) {
            addCriterion("kol_name not like", value, "kolName");
            return (Criteria) this;
        }

        public Criteria andKolNameIn(List<String> values) {
            addCriterion("kol_name in", values, "kolName");
            return (Criteria) this;
        }

        public Criteria andKolNameNotIn(List<String> values) {
            addCriterion("kol_name not in", values, "kolName");
            return (Criteria) this;
        }

        public Criteria andKolNameBetween(String value1, String value2) {
            addCriterion("kol_name between", value1, value2, "kolName");
            return (Criteria) this;
        }

        public Criteria andKolNameNotBetween(String value1, String value2) {
            addCriterion("kol_name not between", value1, value2, "kolName");
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

        public Criteria andKolQrCodeIsNull() {
            addCriterion("kol_qr_code is null");
            return (Criteria) this;
        }

        public Criteria andKolQrCodeIsNotNull() {
            addCriterion("kol_qr_code is not null");
            return (Criteria) this;
        }

        public Criteria andKolQrCodeEqualTo(String value) {
            addCriterion("kol_qr_code =", value, "kolQrCode");
            return (Criteria) this;
        }

        public Criteria andKolQrCodeNotEqualTo(String value) {
            addCriterion("kol_qr_code <>", value, "kolQrCode");
            return (Criteria) this;
        }

        public Criteria andKolQrCodeGreaterThan(String value) {
            addCriterion("kol_qr_code >", value, "kolQrCode");
            return (Criteria) this;
        }

        public Criteria andKolQrCodeGreaterThanOrEqualTo(String value) {
            addCriterion("kol_qr_code >=", value, "kolQrCode");
            return (Criteria) this;
        }

        public Criteria andKolQrCodeLessThan(String value) {
            addCriterion("kol_qr_code <", value, "kolQrCode");
            return (Criteria) this;
        }

        public Criteria andKolQrCodeLessThanOrEqualTo(String value) {
            addCriterion("kol_qr_code <=", value, "kolQrCode");
            return (Criteria) this;
        }

        public Criteria andKolQrCodeLike(String value) {
            addCriterion("kol_qr_code like", value, "kolQrCode");
            return (Criteria) this;
        }

        public Criteria andKolQrCodeNotLike(String value) {
            addCriterion("kol_qr_code not like", value, "kolQrCode");
            return (Criteria) this;
        }

        public Criteria andKolQrCodeIn(List<String> values) {
            addCriterion("kol_qr_code in", values, "kolQrCode");
            return (Criteria) this;
        }

        public Criteria andKolQrCodeNotIn(List<String> values) {
            addCriterion("kol_qr_code not in", values, "kolQrCode");
            return (Criteria) this;
        }

        public Criteria andKolQrCodeBetween(String value1, String value2) {
            addCriterion("kol_qr_code between", value1, value2, "kolQrCode");
            return (Criteria) this;
        }

        public Criteria andKolQrCodeNotBetween(String value1, String value2) {
            addCriterion("kol_qr_code not between", value1, value2, "kolQrCode");
            return (Criteria) this;
        }

        public Criteria andKolH5LinkIsNull() {
            addCriterion("kol_h5_link is null");
            return (Criteria) this;
        }

        public Criteria andKolH5LinkIsNotNull() {
            addCriterion("kol_h5_link is not null");
            return (Criteria) this;
        }

        public Criteria andKolH5LinkEqualTo(String value) {
            addCriterion("kol_h5_link =", value, "kolH5Link");
            return (Criteria) this;
        }

        public Criteria andKolH5LinkNotEqualTo(String value) {
            addCriterion("kol_h5_link <>", value, "kolH5Link");
            return (Criteria) this;
        }

        public Criteria andKolH5LinkGreaterThan(String value) {
            addCriterion("kol_h5_link >", value, "kolH5Link");
            return (Criteria) this;
        }

        public Criteria andKolH5LinkGreaterThanOrEqualTo(String value) {
            addCriterion("kol_h5_link >=", value, "kolH5Link");
            return (Criteria) this;
        }

        public Criteria andKolH5LinkLessThan(String value) {
            addCriterion("kol_h5_link <", value, "kolH5Link");
            return (Criteria) this;
        }

        public Criteria andKolH5LinkLessThanOrEqualTo(String value) {
            addCriterion("kol_h5_link <=", value, "kolH5Link");
            return (Criteria) this;
        }

        public Criteria andKolH5LinkLike(String value) {
            addCriterion("kol_h5_link like", value, "kolH5Link");
            return (Criteria) this;
        }

        public Criteria andKolH5LinkNotLike(String value) {
            addCriterion("kol_h5_link not like", value, "kolH5Link");
            return (Criteria) this;
        }

        public Criteria andKolH5LinkIn(List<String> values) {
            addCriterion("kol_h5_link in", values, "kolH5Link");
            return (Criteria) this;
        }

        public Criteria andKolH5LinkNotIn(List<String> values) {
            addCriterion("kol_h5_link not in", values, "kolH5Link");
            return (Criteria) this;
        }

        public Criteria andKolH5LinkBetween(String value1, String value2) {
            addCriterion("kol_h5_link between", value1, value2, "kolH5Link");
            return (Criteria) this;
        }

        public Criteria andKolH5LinkNotBetween(String value1, String value2) {
            addCriterion("kol_h5_link not between", value1, value2, "kolH5Link");
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