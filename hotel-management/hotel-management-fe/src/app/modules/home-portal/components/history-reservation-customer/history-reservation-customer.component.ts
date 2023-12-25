import { Component, OnInit } from '@angular/core';
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {ReservationService} from "../../../admin-portal/services/reservation.service";
import {SweetAlertCustomizeService} from "../../../../services/sweet-alert-customize.service";
import {DataBindingService} from "../../../../services/data-binding.service";
import {DatePipe} from "@angular/common";
import {CommonUtil} from "../../../../util/CommonUtil";

@Component({
  selector: 'app-history-reservation-customer',
  templateUrl: './history-reservation-customer.component.html',
  styleUrls: ['./history-reservation-customer.component.css']
})
export class HistoryReservationCustomerComponent implements OnInit {
  customerId = new FormGroup({
    id: new FormControl('', [Validators.required])
  })
  isSubmit: boolean = false;
  historyReservationList: any = [];
  historyReservationListShow: any = [];
  startFilterCreatedDate!: Date | null;
  endFilterCreatedDate!: Date | null;
  startFilterStartDate!: Date | null;
  endFilterStartDate!: Date | null;
  constructor(private reservationService: ReservationService,
              private sweetAlertCustomizeService: SweetAlertCustomizeService,
              private datePipe: DatePipe,
              private dataBindingService: DataBindingService) { }

  ngOnInit() {
    this.dataBindingService.sendData({isMainPage: true});
  }

  getCreatedDate(event: any) {
    this.startFilterCreatedDate = event.start ? new Date(event.start) : null;
    this.endFilterCreatedDate = event.end ? new Date(event.end) : null;

    this.filterDate();
  }

  getStartDate(event: any) {
    this.startFilterStartDate = event.start ? new Date(event.start) : null;
    this.endFilterStartDate = event.end ? new Date(event.end) : null;
    this.filterDate();
  }

  searchHistoryReservation() {
    this.isSubmit =true;
    if (this.customerId.valid) {
      let customerId = {customerId: this.customerId.getRawValue().id};
      console.log(customerId);
      this.reservationService.getHistoryReservationCustomer(customerId).subscribe((resp: any) => {
        if (resp) {
          this.historyReservationList = resp;
          this.historyReservationListShow = this.historyReservationList?.historyReservations;
          console.log(this.historyReservationListShow);
        }
      },
        (error: any) => {
          this.sweetAlertCustomizeService.showErrorAlertRightPopup(error.error.error);
        });
    }
  }

  getFormatDate(createdDate: any) {
    return this.datePipe.transform(createdDate, 'dd/MM/yyyy') ?? '';
  }

  getFormatPrice(price: any) {
    return CommonUtil.formatAmount(price.toString());
  }

  getCheckInOutDate(historyReservation: any) {
    return `${this.getFormatDate(historyReservation.startDate)} - ${this.getFormatDate(historyReservation.endDate)}`;
  }
  getRangeDate(startDate: Date, endDate: Date) {
    let diffInTime = endDate.getTime() - startDate.getTime();
    let diffInDays = diffInTime / (1000 * 3600 * 24);
    return diffInDays;
  }
  getPriceAmountRoomCls(item: any) {
    const numDate = this.getRangeDate(new Date(item.startDate), new Date(item.endDate));
    return numDate * item.numberOfRooms * item.price;
  }

  filterDate() {
    let historyReservationList = this.historyReservationList.historyReservations;
    if (!this.startFilterCreatedDate && !this.endFilterCreatedDate) {
      this.historyReservationListShow = historyReservationList;
    } else {
      this.historyReservationListShow = historyReservationList.filter((item: any) =>
        (this.startFilterCreatedDate && this.endFilterCreatedDate ? (this.startFilterCreatedDate <= new Date(item.createdDate)) && (this.endFilterCreatedDate >= new Date(item.createdDate)) : true) &&
        (this.startFilterStartDate && this.endFilterStartDate ? (this.startFilterStartDate <= new Date(item.createdDate)) && (new Date(item.createdDate) <= this.endFilterStartDate) : true)
      );
    }
  }
}
