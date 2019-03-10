import { Component, OnInit } from '@angular/core';
import { Injectable } from '@angular/core';
import { Category } from '../trainmeup.service'
import { TrainmeupMain } from '../trainmeup-main'
export class QuestionState {
	categoryType: string;
  	categoryNewText: string;
  	categoryNewPath: string;
  	categorySelectPath: string;
  	questionText: string;
  	answerType: string;
  	answerSingleText: string;
  	answerMultiOp1Text: string;
  	answerMultiOp2Text: string;
  	answerMultiOp3Text: string;
    categoryObj: Category;
}

@Injectable({
  providedIn: 'root',
})
@Component({
  selector: 'app-question-main',
  templateUrl: './question-main.component.html',
  styleUrls: ['./question-main.component.css']
})
export class QuestionMainComponent extends TrainmeupMain implements OnInit {

  questionState: QuestionState = {
  	categoryType: "new",
  	categoryNewText: "",
  	categoryNewPath: "",
  	categorySelectPath: "",
  	questionText: "",
  	answerType: "single",
  	answerSingleText: "",
  	answerMultiOp1Text: "",
  	answerMultiOp2Text: "",
  	answerMultiOp3Text: "",
    categoryObj: null
  };

  constructor() { super(); }

  ngOnInit() {
  }

  saveQuestionStatus(savedStatus): void {
  	this.questionState = savedStatus;
  }

  setSelectedCategory(category, categoryPath) : void {
    if (this.questionState.categoryType == 'new') {
      this.questionState.categoryNewPath = categoryPath;
      this.questionState.categorySelectPath = '';
    } else if (this.questionState.categoryType == 'select') {
      this.questionState.categorySelectPath = categoryPath;
      this.questionState.categoryNewPath = '';
    }
    this.questionState.categoryObj = category;
  }
    
}
