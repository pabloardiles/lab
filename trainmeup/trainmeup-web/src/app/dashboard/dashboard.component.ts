import { Component, OnInit } from '@angular/core';
import { TrainmeupService } from '../trainmeup.service'
import { MatDialog, MatDialogConfig } from '@angular/material';
import { DialogComponent } from '../dialog/dialog.component';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

  constructor(private trainService: TrainmeupService,
          private dialog: MatDialog) { }

  ngOnInit() {
  }


  test() {
  	this.trainService.test().subscribe(
  		(data: any) => this.openDialog(['Platform ready!'], false),
      	error => this.openDialog(['Not yet ready!'], true));
  }

  private openDialog(lines: string[], error: boolean) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.disableClose = true;
    dialogConfig.autoFocus = true;
    dialogConfig.minWidth = '300px';
    dialogConfig.data = {
      description: lines,
      isError: error
    };
    this.dialog.open(DialogComponent, dialogConfig);
  }
}
