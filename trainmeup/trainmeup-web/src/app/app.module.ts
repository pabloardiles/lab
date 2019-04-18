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
import { AnswerMainComponent } from './answer-main/answer-main.component';
import { HttpClientModule }    from '@angular/common/http';

import { MatDialogModule, MatCardModule, MatButtonModule } from '@angular/material';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { DialogComponent } from './dialog/dialog.component';

@NgModule({
  declarations: [
    AppComponent,
    DashboardComponent,
    QuestionComponent,
    AnswerComponent,
    CategoriesComponent,
    QuestionMainComponent,
    AnswerMainComponent,
    DialogComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    DxTreeViewModule,
    HttpClientModule,
    BrowserAnimationsModule,
    MatDialogModule,
    MatCardModule,
    MatButtonModule
  ],
  entryComponents: [DialogComponent],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }