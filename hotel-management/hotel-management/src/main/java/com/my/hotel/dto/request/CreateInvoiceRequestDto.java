package com.my.hotel.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInvoiceRequestDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Integer> rentalSlipDetailIdList;
    private String employeeId;

}
