import { Component, OnInit, Input, ElementRef, ViewChild } from '@angular/core';
import { Location } from '@angular/common';
import { QuestionMainComponent } from '../question-main/question-main.component';
import { TrainmeupService, Category } from '../trainmeup.service'

@Component({
  selector: 'app-question',
  templateUrl: './question.component.html',
  styleUrls: ['./question.component.css']
})
export class QuestionComponent implements OnInit {

  @ViewChild("new", {read: ElementRef}) 
  newCheckbox: ElementRef;

  @ViewChild("newText", {read: ElementRef}) 
  newInput: ElementRef;

  @ViewChild("select", {read: ElementRef}) 
  selectCheckbox: ElementRef;

  @ViewChild("categPathNew", {read: ElementRef}) 
  categoryPathNewInput: ElementRef;

  @ViewChild("categPathSelect", {read: ElementRef}) 
  categoryPathSelectInput: ElementRef;

  @ViewChild("questionText", {read: ElementRef}) 
  questionTextArea: ElementRef;

  @ViewChild("ansSingle", {read: ElementRef}) 
  answerSingleCheckbox: ElementRef;

  @ViewChild("ansMulti", {read: ElementRef}) 
  answerMultiCheckbox: ElementRef;

  @ViewChild("ansSingleText", {read: ElementRef}) 
  answerSingleTextArea: ElementRef;

  @ViewChild("ansMultiText1", {read: ElementRef}) 
  answerMultiOp1Input: ElementRef;

  @ViewChild("ansMultiText2", {read: ElementRef}) 
  answerMultiOp2Input: ElementRef;

  @ViewChild("ansMultiText3", {read: ElementRef}) 
  answerMultiOp3Input: ElementRef;


  @Input() savedStatus: Object;

  constructor(private location: Location, 
  			private questionMain: QuestionMainComponent, 
  			private trainService: TrainmeupService) { 

  }

  ngOnInit() {
  	// restoring data into component...
  	this.newCheckbox.nativeElement.checked = this.questionMain.questionState.categoryType == 'new';
  	this.newInput.nativeElement.value = this.questionMain.questionState.categoryNewText;
  	this.selectCheckbox.nativeElement.checked = this.questionMain.questionState.categoryType == 'select';
  	this.categoryPathNewInput.nativeElement.innerText = this.questionMain.questionState.categoryNewPath;
  	this.categoryPathSelectInput.nativeElement.innerText = this.questionMain.questionState.categorySelectPath;

  	this.questionTextArea.nativeElement.value = this.questionMain.questionState.questionText;
  	this.answerSingleCheckbox.nativeElement.checked = this.questionMain.questionState.answerType == 'single';
  	this.answerMultiCheckbox.nativeElement.checked = this.questionMain.questionState.answerType == 'multi';
  	this.answerSingleTextArea.nativeElement.value = this.questionMain.questionState.answerSingleText;
  	this.answerMultiOp1Input.nativeElement.value = this.questionMain.questionState.answerMultiOp1Text;
  	this.answerMultiOp2Input.nativeElement.value = this.questionMain.questionState.answerMultiOp2Text;
  	this.answerMultiOp3Input.nativeElement.value = this.questionMain.questionState.answerMultiOp3Text;

  }

  goBack(): void {
  	this.location.back();
  }

  openCategories(): void {
  	//switching to categories screen, saving current data...
	var saved: Object = {
		categoryType: this.getCategoryType(),
		categoryNewText: '' + this.newInput.nativeElement.value,
	  	categoryNewPath: this.categoryPathNewInput.nativeElement.innerText,
	  	categorySelectPath: this.categoryPathSelectInput.nativeElement.innerText,
	  	questionText: '' + this.questionTextArea.nativeElement.value,
	  	answerType: this.getAnswerType(),
	  	answerSingleText: '' + this.answerSingleTextArea.nativeElement.value,
	  	answerMultiOp1Text: '' + this.answerMultiOp1Input.nativeElement.value,
	  	answerMultiOp2Text: '' + this.answerMultiOp2Input.nativeElement.value,
	  	answerMultiOp3Text: '' + this.answerMultiOp3Input.nativeElement.value
	};
	this.questionMain.saveQuestionStatus(saved);
	this.questionMain.setCategoriesShown(true);
  }

  getCategoryType(): string {
  	if (this.newCheckbox.nativeElement.checked) {
  		return "new";
  	} else if (this.selectCheckbox.nativeElement.checked) {
  		return "select";
  	}
  }

  getAnswerType(): string {
  	if (this.answerSingleCheckbox.nativeElement.checked) {
  		return "single";
  	} else if (this.answerMultiCheckbox.nativeElement.checked) {
  		return "multi";
  	}
  }

  isCategNewDisabled(): boolean {
  	return !this.newCheckbox.nativeElement.checked;
  }

  isCategSelectDisabled(): boolean {
  	return !this.selectCheckbox.nativeElement.checked;
  }

  isAnsSingleDisabled(): boolean {
  	return !this.answerSingleCheckbox.nativeElement.checked
  }

  isAnsMultiDisabled(): boolean {
  	return !this.answerMultiCheckbox.nativeElement.checked
  }

  saveQuestion(): void {
  	if (this.questionTextArea.nativeElement.value == '') {
  		alert('You must enter the question!');
  		return;
  	}

  	if (this.answerMultiCheckbox.nativeElement.checked) {
  		alert('Multi-choice not yet supported!');
  		return;
  	}

  	if (this.answerSingleCheckbox.nativeElement.checked 
  		&& this.answerSingleTextArea.nativeElement.value == '') {
  		alert('You must enter the answer!');
  		return;
  	} 

  	if (this.newCheckbox.nativeElement.checked) {
  		if (this.categoryPathNewInput.nativeElement.innerText == '') {
  			alert('You must select a category for the question!');
  			return;
  		} else if (this.newInput.nativeElement.value == '') {
  			alert('You must specify a new category!');
  			return;
  		}

  		//save category...
      let newCateg = {
        categoryParentId: this.questionMain.questionState.categoryObj.categoryId, 
        name: this.newInput.nativeElement.value
      };
  		this.trainService.saveCategory(newCateg).subscribe((data: Category) => {
        //save question..
        let newQuestion = {
          categoryParentId: data.categoryId, 
          question: this.questionTextArea.nativeElement.value,
          answer: this.answerSingleTextArea.nativeElement.value
        };
        this.trainService.saveQuestion(newQuestion).subscribe();
      });

  		

  	} else if (this.selectCheckbox.nativeElement.checked) {
  		if (this.categoryPathSelectInput.nativeElement.innerText == '') {
  			alert('You must select a category for the question!');
  			return;
  		}

  		//save question...
      let newQuestion = {
        categoryParentId: this.questionMain.questionState.categoryObj.categoryId, 
        question: this.questionTextArea.nativeElement.value,
        answer: this.answerSingleTextArea.nativeElement.value
      };
      this.trainService.saveQuestion(newQuestion).subscribe();
  	}
  }

}
