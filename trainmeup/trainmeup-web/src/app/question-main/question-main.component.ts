import { Component, OnInit } from '@angular/core';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
@Component({
  selector: 'app-question-main',
  templateUrl: './question-main.component.html',
  styleUrls: ['./question-main.component.css']
})
export class QuestionMainComponent implements OnInit {

  questionState: Object = {
  	categoryType: "new",
  	categoryNewText: "",
  	categoryNewPath: "/Root/",
  	categorySelectPath: "/Root/",
  	questionText: "",
  	answerType: "single",
  	answerSingleText: "",
  	answerMultiOp1Text: "",
  	answerMultiOp2Text: "",
  	answerMultiOp3Text: ""
  };
  showCategories = false;

  constructor() { }

  ngOnInit() {
  }

  setCategoriesShown(catShown) : void {
  	this.showCategories = catShown;
  }

  saveQuestionStatus(savedStatus): void {
  	this.questionState = savedStatus;
  }
}
