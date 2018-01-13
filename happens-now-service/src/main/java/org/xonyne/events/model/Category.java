package org.xonyne.events.model;

import java.lang.Long;
import java.lang.String;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * @author ridwann
 * @version 1.0
 * @created 13-Nov-2017 10:10:15 PM
 */
@Entity
@Table(name = "category")
@SequenceGenerator(name = "category_categoryId_seq", sequenceName = "category_categoryId_seq", initialValue = 1, allocationSize = 1)
public class Category {

    @GeneratedValue(generator = "category_categoryId_seq")
    @Id
    @Column(name = "category_id")
    private Long id;
    
    @Column(name = "label_key")
    private String labelKey;
    
    @OneToOne()
    @JoinColumn(name = "category_id")
    private Category parentCategory;

    public Category() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabelKey() {
        return labelKey;
    }

    public void setLabelKey(String labelKey) {
        this.labelKey = labelKey;
    }

    public Category getParentCategory() {
        return parentCategory;
    }

    public void setParentCategory(Category parentCategory) {
        this.parentCategory = parentCategory;
    }

}
