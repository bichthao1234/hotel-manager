package com.my.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * DTO for {@link com.my.hotel.entity.Reservation}
 */
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class HistoryReservation implements Serializable {
    private Integer reservationId;
    private Date createdDate;
    private Date startDate;
    private Date endDate;
    private String status;
    private Float deposit;
    private List<ReservationDetailDto> reservationDetails;
}