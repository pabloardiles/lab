import { Component, OnInit, Input, ElementRef, ViewChild } from '@angular/core';
import { Location } from '@angular/common';
import { QuestionMainComponent } from '../question-main/question-main.component';
import { TrainmeupService, Category, Question } from '../trainmeup.service'
import { MatDialog, MatDialogConfig } from '@angular/material';
import { DialogComponent } from '../dialog/dialog.component';

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
  			private trainService: TrainmeupService,
        private dialog: MatDialog) { 

  }

  ngOnInit() {
  	// restoring data into component...
    this.resetScreen(true);
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

  toggleDisabledCheckbox(event: MouseEvent) {
    //this method fixes rendering issue
    if ((<HTMLInputElement>event.target).id === 'new') {
      this.selectCheckbox.nativeElement.checked = false;
    } else if ((<HTMLInputElement>event.target).id === 'select') {
      this.newCheckbox.nativeElement.checked = false;
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
  		this.openDialog(['You must enter the question!'], true);
  		return;
  	}

  	if (this.answerMultiCheckbox.nativeElement.checked) {
  		this.openDialog(['Multi-choice not yet supported!'], true);
  		return;
  	}

  	if (this.answerSingleCheckbox.nativeElement.checked 
  		&& this.answerSingleTextArea.nativeElement.value == '') {
  		this.openDialog(['You must enter the answer!'], true);
  		return;
  	} 

  	if (this.newCheckbox.nativeElement.checked) {
  		if (this.categoryPathNewInput.nativeElement.innerText == '') {
  			this.openDialog(['You must select a category for the question!'], true);
  			return;
  		} else if (this.newInput.nativeElement.value == '') {
  			this.openDialog(['You must specify a new category!'], true);
  			return;
  		}

  		//save category...
      let newCateg = {
        categoryParentId: this.questionMain.questionState.categoryObj.categoryId, 
        subpath: this.newInput.nativeElement.value
      };
  		this.trainService.saveCategory(newCateg).subscribe((data: Category) => {
        //save question..
        let newQuestion = {
          categoryParentId: data.categoryId, 
          question: this.questionTextArea.nativeElement.value,
          answer: this.answerSingleTextArea.nativeElement.value
        };
        this.trainService.saveQuestion(newQuestion).subscribe((data: Question)=>{
          this.openDialog(['The new category and the question were saved!'], false);
          this.resetScreen(false);
        });
      },
        error => this.openDialog(['ERROR: save category has failed.',
          'Correct format is either "AAA/" or "AAA/BBB/CCC/"'], true)
      );

  		

  	} else if (this.selectCheckbox.nativeElement.checked) {
  		if (this.categoryPathSelectInput.nativeElement.innerText == '') {
  			this.openDialog(['You must select a category for the question!'], true);
  			return;
  		}

  		//save question...
      let newQuestion = {
        categoryParentId: this.questionMain.questionState.categoryObj.categoryId, 
        question: this.questionTextArea.nativeElement.value,
        answer: this.answerSingleTextArea.nativeElement.value
      };
      this.trainService.saveQuestion(newQuestion).subscribe((data: Question)=>{
          this.openDialog(['The new question was saved!'], false);
          this.resetScreen(false);
        });
  	}
  }

  resetScreen(callbackFromCategories: boolean): void {

    this.selectCheckbox.nativeElement.checked = callbackFromCategories ? this.questionMain.questionState.categoryType == 'select' : true;
    this.categoryPathSelectInput.nativeElement.innerText = callbackFromCategories ? this.questionMain.questionState.categorySelectPath : '';
    this.newCheckbox.nativeElement.checked = callbackFromCategories ? this.questionMain.questionState.categoryType == 'new' : false;
    this.newInput.nativeElement.value = callbackFromCategories ? this.questionMain.questionState.categoryNewText : '';
    this.categoryPathNewInput.nativeElement.innerText = callbackFromCategories ? this.questionMain.questionState.categoryNewPath : '';
    
    this.questionTextArea.nativeElement.value = callbackFromCategories ? this.questionMain.questionState.questionText : '';
    this.answerSingleCheckbox.nativeElement.checked = callbackFromCategories ? this.questionMain.questionState.answerType == 'single' : true;
    this.answerMultiCheckbox.nativeElement.checked = callbackFromCategories ? this.questionMain.questionState.answerType == 'multi' : false;
    this.answerSingleTextArea.nativeElement.value = callbackFromCategories ? this.questionMain.questionState.answerSingleText : '';
    this.answerMultiOp1Input.nativeElement.value = callbackFromCategories ? this.questionMain.questionState.answerMultiOp1Text : '';
    this.answerMultiOp2Input.nativeElement.value = callbackFromCategories ? this.questionMain.questionState.answerMultiOp2Text : '';
    this.answerMultiOp3Input.nativeElement.value = callbackFromCategories ? this.questionMain.questionState.answerMultiOp3Text : '';
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
