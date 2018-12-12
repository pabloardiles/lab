import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { QuestionComponent } from './question/question.component';
import { AppRoutingModule } from './app-routing.module';
import { AnswerComponent } from './answer/answer.component';
import { CategoriesComponent } from './categories/categories.component';

import { DxTreeViewModule} from 'devextreme-angular';
import { QuestionMainComponent } from './question-main/question-main.component';

@NgModule({
  declarations: [
    AppComponent,
    DashboardComponent,
    QuestionComponent,
    AnswerComponent,
    CategoriesComponent,
    QuestionMainComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    DxTreeViewModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }