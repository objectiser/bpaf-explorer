/**
 * 
 */
package org.jboss.bpm.monitor.client.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Jeff Yu
 * @date 12 Mar, 2011
 */
@XmlRootElement(name="historyInstance")
public class HistoryInstance implements Serializable {
	
	private static final long serialVersionUID = 2350366078574587929L;

	private String instanceId;
	
	private String key;
	
	private long startTime;
	
	private long endTime;
	
	private String status;

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	
	
}
