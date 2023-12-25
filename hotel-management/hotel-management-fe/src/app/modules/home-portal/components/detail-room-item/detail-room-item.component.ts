import {Component, Input, OnInit} from '@angular/core';
import {CommonUtil} from "../../../../util/CommonUtil";
import {DatePipe} from "@angular/common";

@Component({
  selector: 'app-detail-room-item',
  templateUrl: './detail-room-item.component.html',
  styleUrls: ['./detail-room-item.component.css']
})
export class DetailRoomItemComponent implements OnInit {
  @Input() detailRoomClass: any;
  price: any;
  today = new Date();
  endDate!: Date;
  startDate!: Date;

  constructor(private datePipe: DatePipe) {
  }

  ngOnInit() {
    this.price = this.detailRoomClass.price;
    const json = sessionStorage.getItem('respDate') || null;
    if (json) {
      const respDate = JSON.parse(json);
      this.startDate = respDate.start
      this.endDate = respDate.end
    } else {
      this.startDate = new Date(this.today.getFullYear(), this.today.getMonth(), this.today.getDate());
      this.endDate = new Date(this.today.getFullYear(), this.today.getMonth(), this.today.getDate() + 1);
    }
  }

  showNumOfRoom() {
    return `${this.detailRoomClass.numOfRoom} phòng`;
  }

  getShowPrice(amount: any) {
    return CommonUtil.formatAmount(amount.toString()) ?? '';
  }

  getDisplayTextPromotionRange() {
    return '(' + this.datePipe.transform(this.price?.promotionStartDate, 'dd/MM/yyyy') + ' - '
      + this.datePipe.transform(this.price?.promotionEndDate, 'dd/MM/yyyy') + ')';
  }

  getDateRange(from: any, to: any): number {
    let rangeDate = CommonUtil.getRangeDate(new Date(from), new Date(to));
    return rangeDate < 1 ? 1 : Number(rangeDate.toFixed(0));
  }

  getTotal(): number {
    return this.price?.roomClassPrice * this.detailRoomClass.numOfRoom * this.getDateRange(this.startDate, this.endDate)
      - this.price?.promotionAmount * this.detailRoomClass.numOfRoom * this.price?.appliedDays;
  }

  getDisplayTextTotal() {
    return 'Tổng cộng: ' + this.getShowPrice(this.price?.roomClassPrice * this.detailRoomClass.numOfRoom * this.getDateRange(this.startDate, this.endDate))
    + ' - Khuyến mãi: ' + this.getShowPrice(this.price?.promotionAmount * this.detailRoomClass.numOfRoom * this.price?.appliedDays)
    + ' (' + this.price?.appliedDays + ' ngày)'
  }
}
