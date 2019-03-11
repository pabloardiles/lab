import { Component, OnInit, ElementRef, ViewChild } from '@angular/core';
import { Location } from '@angular/common';
import { AnswerMainComponent } from '../answer-main/answer-main.component';
import { TrainmeupService, Question } from '../trainmeup.service'

@Component({
  selector: 'app-answer',
  templateUrl: './answer.component.html',
  styleUrls: ['./answer.component.css']
})
export class AnswerComponent implements OnInit {

  showQuestion: boolean = false;
  showAnswer: boolean = false;
  private question: Question;

  @ViewChild("categPath", {read: ElementRef}) 
  categoryPath: ElementRef;

  @ViewChild("questionText", {read: ElementRef}) 
  questionTextArea: ElementRef;

  @ViewChild("correct", {read: ElementRef}) 
  correct: ElementRef;

  @ViewChild("incorrect", {read: ElementRef}) 
  incorrect: ElementRef;

  constructor(private location: Location,
              private answerMain: AnswerMainComponent,
              private trainService: TrainmeupService) { }

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
    this.showAnswer = false;
    this.trainService.hit(this.answerMain.answerState.categoryObj.categoryId).subscribe((data: Question)=> {
      this.question = data;
      this.questionTextArea.nativeElement.value = data.question;
    });
  	this.showQuestion = true;
  }

  showAnswerSection(): void {
    this.showAnswer = true;
  }

  saveResult(): void {
    let res: string = 'partial';
    if(this.correct.nativeElement.checked) {
      res = 'correct';
    } else if (this.incorrect.nativeElement.checked) {
      res = 'incorrect';
    }
    this.trainService.score(this.question.questionId, res).subscribe((data: Question) => {
      alert("You've tried this question " + data.attempts + " times.\nCurrently you're a " + data.rank + " on this question.");
    });
  }
}
