import { Component, OnInit } from '@angular/core';
import {Router} from "@angular/router";

@Component({
  selector: 'app-footer-user',
  templateUrl: './footer-user.component.html',
  styleUrls: ['./footer-user.component.css']
})
export class FooterUserComponent implements OnInit {

  constructor(private router: Router) { }

  ngOnInit() {
  }

  goToHistoryReservationPage() {
    this.router.navigate(['/history-reservation']).then(r => {});
  }
}
