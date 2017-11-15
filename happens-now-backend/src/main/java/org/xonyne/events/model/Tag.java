package org.xonyne.events.model;

import java.lang.Long;
import java.lang.String;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * @author ridwann
 * @version 1.0
 * @created 13-Nov-2017 10:10:15 PM
 */
@Entity
@Table(name="tag")
@SequenceGenerator(name="tag_tagId_seq", sequenceName="tag_tagId_seq", initialValue = 1, allocationSize = 1)
public class Tag {

	@GeneratedValue(generator="tag_tagId_seq")
	@Id
	private Long tagId;
	private String labelKey;

	public Tag(){

	}

	public Long getTagId() {
		return tagId;
	}

	public void setTagId(Long tagId) {
		this.tagId = tagId;
	}

	public String getLabelKey() {
		return labelKey;
	}

	public void setLabelKey(String labelKey) {
		this.labelKey = labelKey;
	}

	
}