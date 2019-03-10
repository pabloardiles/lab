
export abstract class TrainmeupMain {

	showCategories = false;	
	caller: TrainmeupMain;
	constructor() { }

	ngOnInit() { }
	
	setCategoriesShown(categShown) : void {
		this.showCategories = categShown;
		this.caller = this;
  	}

  	abstract setSelectedCategory(category, categoryPath) : void ;
}