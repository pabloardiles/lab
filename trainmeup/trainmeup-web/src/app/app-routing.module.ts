import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent }      from './dashboard/dashboard.component';
import { QuestionMainComponent }      from './question-main/question-main.component';
import { AnswerMainComponent }      from './answer-main/answer-main.component';
import { AnswerComponent }      from './answer/answer.component';


const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'warmup', component: AnswerMainComponent },
  { path: 'newquestion', component: QuestionMainComponent }
];

@NgModule({
  exports: [ RouterModule ],
  //declarations: [HomeComponent, ProfileComponent, DisplayDataComponent],
  imports: [
    CommonModule,
    RouterModule.forRoot(routes)
  ]
})
export class AppRoutingModule { }
