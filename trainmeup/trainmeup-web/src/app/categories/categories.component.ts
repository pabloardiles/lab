import { NgModule, Component } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { DxTreeViewModule } from 'devextreme-angular';
import { QuestionMainComponent } from '../question-main/question-main.component';
import { TrainmeupService, Category } from '../trainmeup.service'
import { Observable, of } from 'rxjs';

@Component({
    selector: 'app-categories',
    templateUrl: './categories.component.html',
    styleUrls: ['./categories.component.css']
})
export class CategoriesComponent {
	categories: Category[];
    currentItem: any;

    constructor(private questionMain: QuestionMainComponent, private trainService: TrainmeupService) {}

	ngOnInit() {
	  this.trainService.getCategories().subscribe((data: Category[])=> {
	  	this.categories = data;
	  	this.updateParent(this.categories);
	  	this.currentItem = this.categories[0];
	  });
	}

	private updateParent(categories): void {
		for (let categ of categories) {
			if (! (categ.name == 'Root')) {
				categ.parentId = categ.categoryId.substring(0, categ.categoryId.lastIndexOf('_'));
			} else {
				categ.expanded = true;
			}
		}
	}

    selectItem(e) {
        this.currentItem = e.node;
    }

	closeCategories(): void {
    	this.questionMain.setCategoriesShown(false);
  	}

  	selectCategory(): void {
  		let path = "";
  		let tmpNode = this.currentItem;
  		while(tmpNode.text != "Root") {
  			path = tmpNode.text + "/" + path;
  			tmpNode = tmpNode.parent;
  		}
  		path = "/Root/" + path;
  		if (this.questionMain.questionState.categoryType == 'new') {
  			this.questionMain.questionState.categoryNewPath = path;
  		} else if (this.questionMain.questionState.categoryType == 'select') {
  			this.questionMain.questionState.categorySelectPath = path;
  		}
  		this.questionMain.questionState.categoryObj = this.currentItem.itemData;
    	this.closeCategories();
  	}
}

