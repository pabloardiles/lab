import { Component, OnInit, ElementRef, ViewChild } from '@angular/core';
import { Location } from '@angular/common';
import { AnswerMainComponent } from '../answer-main/answer-main.component';

@Component({
  selector: 'app-answer',
  templateUrl: './answer.component.html',
  styleUrls: ['./answer.component.css']
})
export class AnswerComponent implements OnInit {

  showQuestion: boolean = false;
  showAnswer: boolean = false;

  @ViewChild("categPath", {read: ElementRef}) 
  categoryPath: ElementRef;

  constructor(private location: Location,
              private answerMain: AnswerMainComponent) { }

  ngOnInit() {
    this.categoryPath.nativeElement.innerText = this.answerMain.answerState.categoryPath;
  }

  openCategories(): void {
    //switching to categories screen...
    this.answerMain.setCategoriesShown(true);
  }

  goBack(): void {
  	this.location.back();
  }

  showQuestionSection(): void {
  	this.showQuestion = true;
  }

  showAnswerSection(): void {
  	this.showAnswer = true;
  }
}
