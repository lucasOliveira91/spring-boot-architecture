package com.example.demo.domain;

import com.example.demo.domain.enums.PayStatus;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

/**
 * Created by loliveira on 17/11/18.
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeName("creditCardPayment")
public class CreditCardPayment extends Payment{

    private Integer numInstallments;

    public CreditCardPayment(Integer id, PayStatus payStatus, Order order, Integer numInstallments) {
        super(id, payStatus, order);
        this.numInstallments = numInstallments;
    }
}
