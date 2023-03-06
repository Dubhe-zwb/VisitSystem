package com.tonsail.visit.utils;

import java.io.Serializable;
import java.util.List;

/**
 * 登录后返回的首页信息
 *
 */
public  class Visitor implements Serializable {
    private List<ContentBean> content;
    private Integer totalElements;

    public List<ContentBean> getContent() {
        return content;
    }

    public void setContent(List<ContentBean> content) {
        this.content = content;
    }

    public Integer getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Integer totalElements) {
        this.totalElements = totalElements;
    }

    public static class ContentBean implements Serializable{
        private Integer id;//自增id
        private String name;//访客姓名
        private String telphone;//访客电话
        private String plateNumber;//访客车牌
        private String company;//来访者所属公司
        private String reason;//来访事由
        private String time;//到访时间
        private String targetCompany;//到访公司
        private Integer type;//来访类型 0 主动来访 1 邀请来访
        private String receiverName;//接待人昵称
        private Integer receiverId;//接待人id
        private Object receiverTime;//接待时间
        private Object tenantId;//租户id
        private Object accompanyingPerson;//同行人员
        private Object leaveTime;//离开时间
        private Object inviteTime;//邀请时间
        private Integer status;//访客状态 -2 已驳回 -1 提交申请草稿 0 未到访  1 确认到访  2 已离开
        private Object confirmTime;//确认到访时间
        private Object createtime;//创建时间
        private String updatetime;//修改时间

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTelphone() {
            return telphone;
        }

        public void setTelphone(String telphone) {
            this.telphone = telphone;
        }

        public String getPlateNumber() {
            return plateNumber;
        }

        public void setPlateNumber(String plateNumber) {
            this.plateNumber = plateNumber;
        }

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getTargetCompany() {
            return targetCompany;
        }

        public void setTargetCompany(String targetCompany) {
            this.targetCompany = targetCompany;
        }

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public String getReceiverName() {
            return receiverName;
        }

        public void setReceiverName(String receiverName) {
            this.receiverName = receiverName;
        }

        public Integer getReceiverId() {
            return receiverId;
        }

        public void setReceiverId(Integer receiverId) {
            this.receiverId = receiverId;
        }

        public Object getReceiverTime() {
            return receiverTime;
        }

        public void setReceiverTime(Object receiverTime) {
            this.receiverTime = receiverTime;
        }

        public Object getTenantId() {
            return tenantId;
        }

        public void setTenantId(Object tenantId) {
            this.tenantId = tenantId;
        }

        public Object getAccompanyingPerson() {
            return accompanyingPerson;
        }

        public void setAccompanyingPerson(Object accompanyingPerson) {
            this.accompanyingPerson = accompanyingPerson;
        }

        public Object getLeaveTime() {
            return leaveTime;
        }

        public void setLeaveTime(Object leaveTime) {
            this.leaveTime = leaveTime;
        }

        public Object getInviteTime() {
            return inviteTime;
        }

        public void setInviteTime(Object inviteTime) {
            this.inviteTime = inviteTime;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public Object getConfirmTime() {
            return confirmTime;
        }

        public void setConfirmTime(Object confirmTime) {
            this.confirmTime = confirmTime;
        }

        public Object getCreatetime() {
            return createtime;
        }

        public void setCreatetime(Object createtime) {
            this.createtime = createtime;
        }

        public String getUpdatetime() {
            return updatetime;
        }

        public void setUpdatetime(String updatetime) {
            this.updatetime = updatetime;
        }
    }


}
