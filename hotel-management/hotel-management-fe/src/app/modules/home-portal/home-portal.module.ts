import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {HomePortalRoutingModule} from './home-portal-routing.module';
import {HomePortalComponent} from './home-portal.component';
import {SharedModule} from "../../shared/shared.module";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {MainPageComponent} from './components/main-page/main-page.component';
import {SearchResultComponent} from './components/search-result/search-result.component';
import {MatListModule} from "@angular/material/list";
import {DetailRoomItemComponent} from './components/detail-room-item/detail-room-item.component';
import {
  DetailSearchResultRoomClassItemComponent
} from './components/detail-search-result-room-class-item/detail-search-result-room-class-item.component';
import {PaymentPageComponent} from './components/payment-page/payment-page.component';
import {AdvertisementSlidesComponent} from './components/advertisement-slides/advertisement-slides.component';
import {CarouselModule} from "primeng/carousel";
import { RoomClassificationDetailDialogComponent } from './components/room-classification-detail-dialog/room-classification-detail-dialog.component';
import {MatDialogModule} from "@angular/material/dialog";
import { ModalPaymentComponent } from './components/modal-payment/modal-payment.component';
import {NgxPayPalModule} from "ngx-paypal";
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { PaymentSuccessComponent } from './components/payment-success/payment-success.component';
import {DropdownModule} from "primeng/dropdown";
import {SliderModule} from "primeng/slider";
import { HistoryReservationCustomerComponent } from './components/history-reservation-customer/history-reservation-customer.component';
import {AutoCompleteModule} from "primeng/autocomplete";
import {MatIconModule} from "@angular/material/icon";
import {MatMenuModule} from "@angular/material/menu";
import {TableModule} from "primeng/table";
import {TimelineModule} from "primeng/timeline";
import {CardModule} from "primeng/card";
import {MatTooltipModule} from "@angular/material/tooltip";


@NgModule({
  declarations: [
    HomePortalComponent,
    MainPageComponent,
    SearchResultComponent,
    DetailRoomItemComponent,
    DetailSearchResultRoomClassItemComponent,
    PaymentPageComponent,
    AdvertisementSlidesComponent,
    RoomClassificationDetailDialogComponent,
    ModalPaymentComponent,
    PaymentSuccessComponent,
    HistoryReservationCustomerComponent,
  ],
  imports: [
    CommonModule,
    HomePortalRoutingModule,
    SharedModule,
    FormsModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatListModule,
    CarouselModule,
    MatDialogModule,
    NgxPayPalModule,
    MatSelectModule,
    MatFormFieldModule,
    DropdownModule,
    SliderModule,
    AutoCompleteModule,
    MatIconModule,
    MatMenuModule,
    TableModule,
    TimelineModule,
    CardModule,
    MatTooltipModule,
  ]
})
export class HomePortalModule {
}
