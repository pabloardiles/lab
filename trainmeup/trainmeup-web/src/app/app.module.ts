import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { QuestionComponent } from './question/question.component';
import { AppRoutingModule } from './app-routing.module';
import { AnswerComponent } from './answer/answer.component';
import { CategoriesComponent } from './categories/categories.component';
/*import { SideNavOuterToolbarModule, SideNavInnerToolbarModule } from './layouts';
import { FooterModule } from './shared/components/footer/footer.component';*/

import { DxTreeViewModule} from 'devextreme-angular';

@NgModule({
  declarations: [
    AppComponent,
    DashboardComponent,
    QuestionComponent,
    AnswerComponent,
    CategoriesComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    /*SideNavOuterToolbarModule,
    SideNavInnerToolbarModule,
    FooterModule,*/

    DxTreeViewModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
//platformBrowserDynamic().bootstrapModule(AppModule);