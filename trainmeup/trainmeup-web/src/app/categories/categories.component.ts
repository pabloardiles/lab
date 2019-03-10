import { NgModule, Component, Input } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { DxTreeViewModule } from 'devextreme-angular';
import { TrainmeupMain } from '../trainmeup-main';
import { TrainmeupService, Category } from '../trainmeup.service'
import { Observable, of } from 'rxjs';

const rootItem = "All";

@Component({
    selector: 'app-categories',
    templateUrl: './categories.component.html',
    styleUrls: ['./categories.component.css']
})
export class CategoriesComponent {
	
	categories: Category[];
  currentItem: any;

  @Input() screenCaller: TrainmeupMain;

  constructor(private trainService: TrainmeupService) {
    
  }

	ngOnInit() {
	  this.trainService.getCategories().subscribe((data: Category[])=> {
	  	this.categories = data;
	  	this.updateParent(this.categories);
	  	this.currentItem = this.categories[0];
	  });
	}

	private updateParent(categories): void {
		for (let categ of categories) {
			if (! (categ.name == rootItem)) {
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
    	this.screenCaller.setCategoriesShown(false);
  }

	selectCategory(): void {
		let path = "";
		let tmpNode = this.currentItem;
		while(tmpNode.text != rootItem) {
			path = tmpNode.text + "/" + path;
			tmpNode = tmpNode.parent;
		}
		path = "/" + rootItem + "/" + path;

		this.screenCaller.setSelectedCategory(this.currentItem.itemData, path);
  	this.closeCategories();
	}
}

