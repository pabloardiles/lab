import { Component, OnInit, Inject } from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";

@Component({
  selector: 'app-dialog',
  templateUrl: './dialog.component.html',
  styleUrls: ['./dialog.component.css']
})
export class DialogComponent implements OnInit {

	isError: boolean;
	description: string[];

	constructor(
	    private dialogRef: MatDialogRef<DialogComponent>,
	    @Inject(MAT_DIALOG_DATA) public data
	    ) {	

		this.isError = data.isError;
		this.description = data.description;
	}

	ngOnInit() {
	}

	save() {
	    //this.dialogRef.close(this.form.value);
	}

	close() {
	    this.dialogRef.close();
	}

}
