import { Component, OnInit, Injectable } from '@angular/core';
import { Category } from '../trainmeup.service'
import { TrainmeupMain } from '../trainmeup-main'

@Injectable({
  providedIn: 'root',
})
@Component({
  selector: 'app-answer-main',
  templateUrl: './answer-main.component.html',
  styleUrls: ['./answer-main.component.css']
})
export class AnswerMainComponent extends TrainmeupMain implements OnInit {
  answerState: any = {
    categoryPath: "",
    categoryObj: null
  };

  constructor() { super(); }

  ngOnInit() {
  }

  setSelectedCategory(category, categoryPath) : void {
    this.answerState.categoryObj = category;
    this.answerState.categoryPath = categoryPath;
  }
}
