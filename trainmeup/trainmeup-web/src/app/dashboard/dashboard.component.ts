import { Component, OnInit } from '@angular/core';
import { TrainmeupService } from '../trainmeup.service'

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

  constructor(private trainService: TrainmeupService) { }

  ngOnInit() {
  }


  test() {
  	this.trainService.test().subscribe(
  		(data: any) => alert('Platform ready!'),
      	error => alert('Not yet ready!'));
  }
}
