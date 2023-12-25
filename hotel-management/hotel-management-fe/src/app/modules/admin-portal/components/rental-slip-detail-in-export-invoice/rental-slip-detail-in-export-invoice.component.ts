import {Component, Input, OnInit} from '@angular/core';
import {CommonUtil} from "../../../../util/CommonUtil";
import {DatePipe} from "@angular/common";

@Component({
  selector: 'app-rental-slip-detail-in-export-invoice',
  templateUrl: './rental-slip-detail-in-export-invoice.component.html',
  styleUrls: ['./rental-slip-detail-in-export-invoice.component.css']
})
export class RentalSlipDetailInExportInvoiceComponent implements OnInit {
  @Input() public rentalSlipDetail: any;
  showRoom = false;
  showSurcharge = false;
  showService = false;
  constructor(private datePipe: DatePipe) { }

  ngOnInit() {
    console.log(this.rentalSlipDetail);
  }

  getPrice(price: any) {
    return CommonUtil.formatAmount(price.toString());
  }

  changeRoom() {
    this.showRoom = !this.showRoom;
  }

  changeSurcharge() {
    this.showSurcharge = !this.showSurcharge;
  }
  changeService() {
    this.showService = !this.showService;
  }

  getDateRange(from: any, to: any) {
    let rangeDate = CommonUtil.getRangeDate(new Date(from), new Date(to));
    return rangeDate < 1 ? 1 : Number(rangeDate).toFixed(0);
  }

  getTotalRoomPrice(roomPrice: any, from: any, to: any) {
    return this.getPrice(roomPrice * Number(this.getDateRange(from, to)));
  }

  getDisplayTextPromotionRange(start: Date, end: Date) {
    return '(' + this.datePipe.transform(start, 'dd/MM/yyyy') + ' - '
      + this.datePipe.transform(end, 'dd/MM/yyyy') + ')';
  }
}
