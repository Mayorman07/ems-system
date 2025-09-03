package com.mayorman.employees.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.Collection;

@Entity
@Table(name="roles")
@Data
public class Role implements Serializable {
    private static final long serialVersionUID = 6929482536229723029L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false, length = 23)
    private String name;
    @ManyToMany(mappedBy = "roles")
    private Collection<Employee> employees;

    @ManyToMany(cascade = CascadeType.PERSIST ,fetch = FetchType.EAGER )
    @JoinTable(name="roles_authorities", joinColumns =@JoinColumn(name = "roles_id", referencedColumnName = "id"),inverseJoinColumns
            = @JoinColumn(name = "authorities_id", referencedColumnName = "id"))
    private Collection<Authority> authorities;
    public long getId() {
        return id;
    }

    public Role() {
    }
    public Role(String name, Collection<Authority> authorities) {
        this.name = name;
        this.authorities = authorities;
    }
    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(Collection<Employee> employees) {
        this.employees = employees;
    }


}
