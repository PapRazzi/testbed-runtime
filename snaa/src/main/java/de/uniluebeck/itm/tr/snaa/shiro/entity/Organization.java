package de.uniluebeck.itm.tr.snaa.shiro.entity;
// Generated 26 juil. 2013 14:38:11 by Hibernate Tools 3.2.2.GA


import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Organization generated by hbm2java
 */
@Entity
@Table(name="ORGANIZATION"
    ,catalog="trauth"
)
public class Organization  implements java.io.Serializable {


     private String name;
     private String url;
     private Set<UsersCert> usersCerts = new HashSet<UsersCert>(0);

    public Organization() {
    }

	
    public Organization(String name, String url) {
        this.name = name;
        this.url = url;
    }
    public Organization(String name, String url, Set<UsersCert> usersCerts) {
       this.name = name;
       this.url = url;
       this.usersCerts = usersCerts;
    }
   
     @Id 
    
    @Column(name="NAME", unique=true, nullable=false, length=50)
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @Column(name="URL", nullable=false, length=150)
    public String getUrl() {
        return this.url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="organization")
    public Set<UsersCert> getUsersCerts() {
        return this.usersCerts;
    }
    
    public void setUsersCerts(Set<UsersCert> usersCerts) {
        this.usersCerts = usersCerts;
    }




}

