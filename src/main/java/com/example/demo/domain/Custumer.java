package com.example.demo.domain;

import com.example.demo.domain.enums.CustumerType;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by loliveira on 17/11/18.
 */
@Entity
@Data
@AllArgsConstructor
public class Custumer {

    public Custumer(Integer id, String name, String email, String cpfCnpj, CustumerType custumerType) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.cpfCnpj = cpfCnpj;
        this.custumerType = custumerType.getId();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String email;
    private String cpfCnpj;
    private Integer custumerType;

    @ElementCollection
    @CollectionTable(name = "telefone")
    private Set<String> celPhones = new HashSet<>();

    @OneToMany(mappedBy = "custumer")
    private List<Address> addresses = new ArrayList<>();
}
