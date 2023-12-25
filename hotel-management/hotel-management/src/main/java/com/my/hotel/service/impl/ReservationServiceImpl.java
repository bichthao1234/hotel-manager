package com.my.hotel.service.impl;

import com.my.hotel.common.ObjectMapperUtils;
import com.my.hotel.common.Status;
import com.my.hotel.dto.CustomerDto;
import com.my.hotel.dto.HistoryReservation;
import com.my.hotel.dto.ReservationDetailDto;
import com.my.hotel.dto.ReservationDto;
import com.my.hotel.dto.request.BookRoomRequestDto;
import com.my.hotel.dto.request.ChangeDateReservationRequestDto;
import com.my.hotel.dto.request.CheckRoomRequestDto;
import com.my.hotel.dto.request.GetReservationListRequestDto;
import com.my.hotel.dto.request.PriceRoomClassRequestDto;
import com.my.hotel.dto.response.PriceRoomClassResponseDto;
import com.my.hotel.entity.Customer;
import com.my.hotel.entity.Promotion;
import com.my.hotel.entity.PromotionDetail;
import com.my.hotel.entity.RentalSlip;
import com.my.hotel.entity.RentalSlipDetail;
import com.my.hotel.entity.Reservation;
import com.my.hotel.entity.ReservationDetail;
import com.my.hotel.entity.RoomClassification;
import com.my.hotel.repo.CustomerRepo;
import com.my.hotel.repo.PriceRoomClassificationRepo;
import com.my.hotel.repo.PromotionDetailRepo;
import com.my.hotel.repo.ReservationDetailRepo;
import com.my.hotel.repo.ReservationRepo;
import com.my.hotel.repo.RoomClassificationRepo;
import com.my.hotel.service.PromotionDetailService;
import com.my.hotel.service.ReservationService;
import com.my.hotel.utils.Utilities;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    final private ReservationRepo reservationRepo;
    final private ReservationDetailRepo reservationDetailRepo;
    final private PriceRoomClassificationRepo priceRoomClassificationRepo;
    final private RoomClassificationRepo roomClassificationRepo;
    final private PlatformTransactionManager platformTransactionManager;
    final private CustomerRepo customerRepo;
    final private PromotionDetailRepo promotionDetailRepo;

    @Override
    public boolean bookRoom(BookRoomRequestDto requestDto) throws Throwable {
        try {
            requestDto.setRoomClassString(Utilities.listToCsv(requestDto.getRoomClassList()));
            requestDto.setNumberOfRoomsString(Utilities.listToCsv(requestDto.getNumberOfRoomsList()));
            List<Object[]> objects = reservationRepo.bookRoom(requestDto);
            return true;
        } catch (Exception e) {
            if (e.getCause() != null && e.getCause().getCause() != null) {
                throw e.getCause().getCause();
            }
            throw e;
        }
    }

    @Override
    public List<Map<String, Object>> checkRoom(CheckRoomRequestDto requestDto) throws Throwable {
        try {
            List<Map<String, Object>> ret = new ArrayList<>();
            List<Object[]> resultList = reservationRepo.checkRoom(requestDto);
            for (Object[] objects : resultList) {
                Map<String, Object> map = new HashMap<>();
                map.put("roomClassId", Utilities.parseInt(objects[0]));
                map.put("numberOfRoom", Utilities.parseInt(objects[1]));
                RoomClassification roomClassification = roomClassificationRepo.findById(map.get("roomClassId")
                        .toString()).orElse(null);
                map.put("roomTypeId", roomClassification.getRoomType().getId());
                map.put("roomKindId", roomClassification.getRoomKind().getId());
                map.put("price", getRoomClassPrice(Integer.parseInt(roomClassification.getId()), new Date()));
                ret.add(map);
            }
            return ret;
        } catch (Exception e) {
            if (e.getCause() != null && e.getCause().getCause() != null) {
                throw e.getCause().getCause();
            }
            throw e;
        }
    }

    @Override
    public Double getRoomClassPrice(Integer roomClassId, Date startDate) {
        return priceRoomClassificationRepo.getRoomClassPrice(roomClassId, startDate);
    }

    @Override
    public PriceRoomClassResponseDto getRoomClassPrice(PriceRoomClassRequestDto requestDto) {
        Double roomClassPrice = this.getRoomClassPrice(requestDto.getRoomClassId(), requestDto.getStartDate());
        List<PromotionDetail> promotionDetails = promotionDetailRepo
                .findAll().stream()
                .filter(promotionDetail ->
                        Objects.nonNull(promotionDetail.getRoomClassification()) &&
                                Objects.nonNull(promotionDetail.getPromotion()) &&
                                promotionDetail.getRoomClassification().getId().equals(requestDto.getRoomClassId().toString()) &&
                                this.isDateInRange(requestDto.getStartDate(), requestDto.getEndDate(),
                                        promotionDetail.getPromotion().getStartDate(), promotionDetail.getPromotion().getEndDate())
                        ).collect(Collectors.toList());
        Double priceAfterPromotion = 0.0;
        Double promotionPercent = 0.0;
        PromotionDetail maxPercentPromotionDetail = new PromotionDetail();
        String promotionDescription = "";
        Date promotionStartDate = null;
        Date promotionEndDate = null;
        long appliedDays = 0L;
        if (Objects.isNull(promotionDetails) || promotionDetails.isEmpty()) {
            priceAfterPromotion = roomClassPrice;
        } else {
            maxPercentPromotionDetail = promotionDetails.stream()
                    .max(Comparator.comparing(PromotionDetail::getPercent))
                    .orElse(null);

            if (maxPercentPromotionDetail != null) {
                promotionPercent = Double.valueOf(maxPercentPromotionDetail.getPercent());
                priceAfterPromotion = roomClassPrice - Utilities.calculateObtained(roomClassPrice, promotionPercent);
                promotionDescription = maxPercentPromotionDetail.getPromotion().getDescription();
                promotionStartDate = maxPercentPromotionDetail.getPromotion().getStartDate();
                promotionEndDate = maxPercentPromotionDetail.getPromotion().getEndDate();
                appliedDays = this.getAppliedDays(requestDto.getStartDate(), requestDto.getEndDate(), promotionStartDate, promotionEndDate);
                if (appliedDays == 0L) appliedDays = 1L;
            } else {
                priceAfterPromotion = roomClassPrice;
            }
        }
        return PriceRoomClassResponseDto.builder()
                .roomClassPrice(roomClassPrice)
                .hasPromotion(!priceAfterPromotion.equals(roomClassPrice))
                .percentPromote(promotionPercent)
                .priceAfterPromotion(priceAfterPromotion)
                .promotionAmount(roomClassPrice - priceAfterPromotion)
                .promotionDescription(promotionDescription)
                .promotionStartDate(promotionStartDate)
                .promotionEndDate(promotionEndDate)
                .appliedDays(appliedDays)
                .build();
    }

    private boolean isDateInRange(Date bookingStartDate, Date bookingEndDate, Date promotionStartDate, Date promotionEndDate) {
        // Case 1: Promotion starts during the booking period
        boolean case1 = !promotionStartDate.before(bookingStartDate) && !promotionStartDate.after(bookingEndDate);

        // Case 2: Promotion ends during the booking period
        boolean case2 = !promotionEndDate.before(bookingStartDate) && !promotionEndDate.after(bookingEndDate);

        // Case 3: Booking period is entirely within the promotion period
        boolean case3 = !bookingStartDate.before(promotionStartDate) && !bookingEndDate.after(promotionEndDate);

        // Case 4: Booking period is entirely outside the promotion period
        boolean case4 = bookingEndDate.before(promotionStartDate) && bookingStartDate.after(promotionEndDate);

        // Return true if any of the cases is true
        return case1 || case2 || case3 || case4;
    }

    private long getAppliedDays(Date bookingStartDate, Date bookingEndDate, Date promotionStartDate, Date promotionEndDate) {
        if (!promotionStartDate.before(bookingStartDate) && !promotionStartDate.after(bookingEndDate)) {
            // Case 1: Promotion starts during the booking period
            return Utilities.getDayDiff(promotionStartDate, bookingEndDate);
        } else if (!promotionEndDate.before(bookingStartDate) && !promotionEndDate.after(bookingEndDate)) {
            // Case 2: Promotion ends during the booking period
            return Utilities.getDayDiff(bookingStartDate, promotionEndDate);
        } else if (!bookingStartDate.before(promotionStartDate) && !bookingEndDate.after(promotionEndDate)) {
            // Case 3: Booking period is entirely within the promotion period
            return Utilities.getDayDiff(bookingStartDate, bookingEndDate);
        } else {
            // Case 4: Booking period is entirely outside the promotion period
            return Utilities.getDayDiff(bookingEndDate, promotionStartDate) + Utilities.getDayDiff(promotionEndDate, bookingEndDate);
        }
    }

    @Override
    public String getImagePathById(Integer roomClassId) {
        return roomClassificationRepo.getImagePathById(roomClassId);
    }

    @Override
    public List<ReservationDto> getReservationList(GetReservationListRequestDto requestDto) throws Throwable {
        try {
            List<ReservationDto> ret = new ArrayList<>();
            Integer status = null;
            if (Utilities.nonNull(requestDto.getStatus())) {
                status = Status.getStatusValue(requestDto.getStatus()).getValue();
            }
            List<Object[]> resultList = reservationRepo.getReservationList(requestDto.getCreatedDateFrom(), requestDto.getCreatedDateTo(),
                    requestDto.getStartDateFrom(), requestDto.getStartDateTo(), requestDto.getCustomerId(), status);
            if (Utilities.nonEmptyList(resultList)) {
                ret = resultList.stream().map(ReservationDto::new).collect(Collectors.toList());
            }
            return ret;
        } catch (Exception e) {
            if (e.getCause() != null && e.getCause().getCause() != null) {
                throw e.getCause().getCause();
            }
            throw e;
        }
    }

    @Override
    public List<ReservationDetailDto> getReservationDetail(Integer reservationId) throws Throwable {
        try {
            List<ReservationDetailDto> ret = new ArrayList<>();
            List<ReservationDetail> resultList = reservationRepo.getReservationDetail(reservationId);
            if (Utilities.nonEmptyList(resultList)) {
                int totalRooms = resultList.stream().mapToInt(ReservationDetail::getNumberOfRooms).sum();
                if (resultList.stream().allMatch(item -> Utilities.isEmptyList(item.getReservation().getRentalSlips()))) {
                    for (ReservationDetail reservationDetail : resultList) {
                        for (int i = 0; i < reservationDetail.getNumberOfRooms(); i++) {
                            ReservationDetailDto dto = new ReservationDetailDto(reservationDetail);
                            dto.setStatus(0);
                            ret.add(dto);
                        }
                    }
                } else {
                    RentalSlip rentalSlip = resultList.get(0).getReservation().getRentalSlips().get(0);
                    for (RentalSlipDetail rentalSlipDetail : rentalSlip.getRentalSlipDetails()) {
                        ReservationDetailDto dto = new ReservationDetailDto(rentalSlipDetail);
                        ret.add(dto);
                    }
                    if (ret.size() < totalRooms) {
                        Map<RoomClassification, List<ReservationDetail>> collect1 = resultList.stream().collect(Collectors.groupingBy(ReservationDetail::getRoomClassification));
                        for (Map.Entry<RoomClassification, List<ReservationDetail>> entry : collect1.entrySet()) {
                            int numberOfRooms = entry.getValue().get(0).getNumberOfRooms();
                            int checkedInReservation = (int) ret.stream()
                                    .filter(item -> item.getRoomClassId().equals(entry.getKey().getId())).count();
                            int remain = numberOfRooms - checkedInReservation;
                            for (int i = 0; i < remain; i++) {
                                ReservationDetail reservationDetail = entry.getValue().get(0);
                                ReservationDetailDto dto = new ReservationDetailDto(reservationDetail);
                                dto.setStatus(0);
                                ret.add(dto);
                            }
                        }
                    }
                }
            }
            return ret;
        } catch (Exception e) {
            if (e.getCause() != null && e.getCause().getCause() != null) {
                throw e.getCause().getCause();
            }
            throw e;
        }
    }

    @Override
    public Integer getRentalSlipId(Integer reservationId) {
        return reservationRepo.getRentalSlipId(reservationId);
    }

    @Transactional
    @Override
    public boolean cancelReservation(Integer reservationId, String employeeId) throws Throwable {
        try {
            reservationRepo.cancelReservation(reservationId, employeeId);
            return true;
        } catch (Exception e) {
            if (e.getCause() != null && e.getCause().getCause() != null) {
                throw e.getCause().getCause();
            }
            throw e;
        }
    }

    @Override
    public boolean changeDateRangeReservation(ChangeDateReservationRequestDto requestDto) throws Throwable {
        try {
            List<ReservationDetail> allByReservationId = reservationDetailRepo.findAllByReservationId(requestDto.getReservationId());
            for (ReservationDetail reservationDetail : allByReservationId) {
                CheckRoomRequestDto checkRoomRequestDto = new CheckRoomRequestDto();
                checkRoomRequestDto.setStartDate(requestDto.getStartDate());
                checkRoomRequestDto.setEndDate(requestDto.getEndDate());
                checkRoomRequestDto.setNumberOfRoom(String.valueOf(reservationDetail.getNumberOfRooms()));
                checkRoomRequestDto.setRoomClass(reservationDetail.getRoomClassification().getId());
                checkRoomRequestDto.setReservationId(reservationDetail.getReservation().getId());
                reservationRepo.checkRoom(checkRoomRequestDto);
            }
        } catch (Exception e) {
            if (e.getCause() != null && e.getCause().getCause() != null) {
                throw e.getCause().getCause();
            }
            throw e;
        }
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = platformTransactionManager.getTransaction(def);
        reservationRepo.changeDateRangeReservation(requestDto.getReservationId(), requestDto.getStartDate(), requestDto.getEndDate());
        platformTransactionManager.commit(status);
        return true;
    }

    @Override
    public Map<String, Object> getHistoryReservationCustomer(String customerId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Customer customer = this.customerRepo.findById(customerId).orElse(null);
            if (Objects.isNull(customer)) {
                throw new RuntimeException("Không tìm thấy thông tin khách hàng!");
            }
            List<Reservation> reservations = this.reservationRepo.findAll();
            List<HistoryReservation> historyReservations = new ArrayList<>();
            historyReservations = reservations.stream()
                    .filter(reservation -> Objects.nonNull(reservation.getCustomer()) && reservation.getCustomer().getId().equals(customerId))
                    .sorted(Comparator.comparing(Reservation::getCreatedDate))
                    .map(reservation -> {
                        HistoryReservation historyReservation = new HistoryReservation();
                        historyReservation.setReservationId(reservation.getId());
                        historyReservation.setCreatedDate(reservation.getCreatedDate());
                        historyReservation.setStartDate(reservation.getStartDate());
                        historyReservation.setEndDate(reservation.getEndDate());
                        historyReservation.setStatus(reservation.getStatus());
                        historyReservation.setDeposit(reservation.getDeposit());
                        historyReservation.setReservationDetails(
                                reservation.getReservationDetails()
                                        .stream()
                                        .map(ReservationDetailDto::new)
                                        .collect(Collectors.toList()));
                        return historyReservation;
                    })
                    .collect(Collectors.toList());
            result.put("customer", ObjectMapperUtils.map(customer, CustomerDto.class));
            result.put("historyReservations", historyReservations);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
