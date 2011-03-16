/**
 * 
 */
package org.jboss.bpm.monitor.client.model;

import java.io.Serializable;
import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Jeff Yu
 * @date 12 Mar, 2011
 */
@XmlRootElement(name="historyInstanceRef")
public class HistoryInstanceRef implements Serializable {
	
	private static final long serialVersionUID = -5059104140015710714L;

	private Collection<HistoryInstance> instances;
	
	@XmlElement
	public Collection<HistoryInstance> getInstances() {
		return instances;
	}

	public void setInstances(Collection<HistoryInstance> instances) {
		this.instances = instances;
	}
	
	
	
}
