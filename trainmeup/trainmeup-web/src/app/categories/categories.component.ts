import { NgModule, Component } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { DxTreeViewModule } from 'devextreme-angular';
import { QuestionMainComponent } from '../question-main/question-main.component';

export class Category {
    id: string;
    text: string;
    expanded?: boolean;
    items?: Category[];
}

@Component({
    selector: 'app-categories',
    templateUrl: './categories.component.html',
    styleUrls: ['./categories.component.css']
})
export class CategoriesComponent {
	categories: Category[];
    currentItem: any;

    constructor(private questionMain: QuestionMainComponent) {
        this.categories = [{
			    id: "1",
			    text: "Root",
			    expanded: true,
			    items: [{
			        id: "1_1",
			        text: "Cloud",
			        items: [{
			            id: "1_1_1",
			            text: "Video Players",
			            items: [{
			                id: "1_1_1_1",
			                text: "HD Video Player"
			            }, {
			                id: "1_1_1_2",
			                text: "SuperHD Video Player"
			            }]
			        }, {
			            id: "1_1_2",
			            text: "Televisions",
			            items: [{
			                id: "1_1_2_1",
			                text: "SuperLCD 42"
			            }, {
			                id: "1_1_2_2",
			                text: "SuperLED 42"
			            }, {
			                id: "1_1_2_3",
			                text: "SuperLED 50"
			            }, {
			                id: "1_1_2_4",
			                text: "SuperLCD 55"
			            }, {
			                id: "1_1_2_5",
			                text: "SuperLCD 70"
			            }]
			        }, {
			            id: "1_1_3",
			            text: "Monitors",
			            items: [{
			                id: "1_1_3_1",
			                text: "19\"",
			                items: [{
			                    id: "1_1_3_1_1",
			                    text: "DesktopLCD 19"
			                }]
			            }, {
			                id: "1_1_3_2",
			                text: "21\"",
			                items: [{
			                    id: "1_1_3_2_1",
			                    text: "DesktopLCD 21"
			                }, {
			                    id: "1_1_3_2_2",
			                    text: "DesktopLED 21"
			                }]
			            }]
			        }, {
			            id: "1_1_4",
			            text: "Projectors",
			            items: [{
			                id: "1_1_4_1",
			                text: "Projector Plus"
			            }, {
			                id: "1_1_4_2",
			                text: "Projector PlusHD"
			            }]
			        }]

			    }, {
			        id: "1_2",
			        text: "Backend",
			        items: [{
			            id: "1_2_1",
			            text: "Video Players",
			            items: [{
			                id: "1_2_1_1",
			                text: "HD Video Player"
			            }, {
			                id: "1_2_1_2",
			                text: "SuperHD Video Player"
			            }]
			        }, {
			            id: "1_2_2",
			            text: "Televisions",
			            items: [{
			                id: "1_2_2_1",
			                text: "SuperPlasma 50"
			            }, {
			                id: "1_2_2_2",
			                text: "SuperPlasma 65"
			            }]
			        }, {
			            id: "1_2_3",
			            text: "Monitors",
			            items: [{
			                id: "1_2_3_1",
			                text: "19\"",
			                items: [{
			                    id: "1_2_3_1_1",
			                    text: "DesktopLCD 19"
			                }]
			            }, {
			                id: "1_2_3_2",
			                text: "21\"",
			                items: [{
			                    id: "1_2_3_2_1",
			                    text: "DesktopLCD 21"
			                }, {
			                    id: "1_2_3_2_2",
			                    text: "DesktopLED 21"
			                }]
			            }]
			        }]

			    }, {
			        id: "1_3",
			        text: "AI",
			        items: [{
			            id: "1_3_1",
			            text: "Video Players",
			            items: [{
			                id: "1_3_1_1",
			                text: "HD Video Player"
			            }, {
			                id: "1_3_1_2",
			                text: "SuperHD Video Player"
			            }]
			        }, {
			            id: "1_3_3",
			            text: "Monitors",
			            items: [{
			                id: "1_3_3_1",
			                text: "19\"",
			                items: [{
			                    id: "1_3_3_1_1",
			                    text: "DesktopLCD 19"
			                }]
			            }, {
			                id: "1_3_3_2",
			                text: "21\"",
			                items: [{
			                    id: "1_3_3_2_1",
			                    text: "DesktopLCD 21"
			                }]
			            }]
			        }]
			    }, {
			        id: "1_4",
			        text: "Agile",
			        items: [{
			            id: "1_4_1",
			            text: "Video Players",
			            items: [{
			                id: "1_4_1_1",
			                text: "HD Video Player"
			            }, {
			                id: "1_4_1_2",
			                text: "SuperHD Video Player"
			            }]
			        }, {
			            id: "1_4_2",
			            text: "Televisions",
			            items: [{
			                id: "1_4_2_1",
			                text: "SuperLCD 42"
			            }, {
			                id: "1_4_2_2",
			                text: "SuperLED 42"
			            }, {
			                id: "1_4_2_3",
			                text: "SuperLED 50"
			            }, {
			                id: "1_4_2_4",
			                text: "SuperLCD 55"
			            }, {
			                id: "1_4_2_5",
			                text: "SuperLCD 70"
			            }, {
			                id: "1_4_2_6",
			                text: "SuperPlasma 50"
			            }]
			        }, {
			            id: "1_4_3",
			            text: "Monitors",
			            items: [{
			                id: "1_4_3_1",
			                text: "19\"",
			                items: [{
			                    id: "1_4_3_1_1",
			                    text: "DesktopLCD 19"
			                }]
			            }, {
			                id: "1_4_3_2",
			                text: "21\"",
			                items: [{
			                    id: "1_4_3_2_1",
			                    text: "DesktopLCD 21"
			                }, {
			                    id: "1_4_3_2_2",
			                    text: "DesktopLED 21"
			                }]
			            }]
			        }, {
			            id: "1_4_4",
			            text: "Projectors",
			            items: [{
			                id: "1_4_4_1",
			                text: "Projector Plus"
			            }, {
			                id: "1_4_4_2",
			                text: "Projector PlusHD"
			            }]
			        }]

			    }]
			}];
        this.currentItem = this.categories[0];
    }

    selectItem(e) {
        this.currentItem = e.node;
    }

	closeCategories(): void {
    	this.questionMain.setCategoriesShown(false);
  	}

  	selectCategory(): void {
  		var path = "";
  		var tmpNode = this.currentItem;
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
  		
    	this.closeCategories();
  	}
}

