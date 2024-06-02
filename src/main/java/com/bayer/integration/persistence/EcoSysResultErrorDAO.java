package com.bayer.integration.persistence;

public class EcoSysResultErrorDAO {

		protected long id;
	    protected String status;
	    protected String errorMsg;
		protected String rootCostObjectID;
	    
	    public long getId() {
			return id;
		}
		public void setId(long id) {
			this.id = id;
		}
		public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}
		public String getErrorMsg() {
			return errorMsg;
		}
		public void setErrorMsg(String errorMsg) {
			this.errorMsg = errorMsg;
		}
	    public String getRootCostObjectID() {
			return rootCostObjectID;
		}
		public void setRootCostObjectID(String rootCostObjectID) {
			this.rootCostObjectID = rootCostObjectID;
		}
	}