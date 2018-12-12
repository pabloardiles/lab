import { Component, OnInit } from '@angular/core';
import { Location } from '@angular/common';

@Component({
  selector: 'app-answer',
  templateUrl: './answer.component.html',
  styleUrls: ['./answer.component.css']
})
export class AnswerComponent implements OnInit {

  showQuestion: boolean = false;
  showAnswer: boolean = false;

  constructor(private location: Location) { }

  ngOnInit() {
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
